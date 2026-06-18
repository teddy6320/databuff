<template>
  <div class="db-workload-wrapper">
    <div class="db-workload">
      <query-filter
        :groupData="filterGroupData"
        :groupMapping="filterGroupMapping"
        @query-change="searchChangeHandle"
        @type-change="typeChangeHandle"
      />

      <div class="db-list">
        <db-table
          :queryApi='queryApi'
          :queryParams='getQueryParams'
          :timeMode="false"
          :autoRefresh="false"
          :offsetMode='true'
          :columnConfig='columnConfig'
          @on-table-inited='tableInitedHandle'
          @sort-change='refresh'
          :formatFunc='formatFunc'
          showSetting
          tableKey='INFRA_WORKLOAD_LIST'
          @row-click="jumpWorkloadDetail"
          :row-style="{ cursor: 'pointer' }"
          ref='listTable'>
        </db-table>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import KubernetesApi from '@/api/kubernetes';
import { toAsyncWait } from '@/utils/common';
import QueryFilter from '../cluster/query-filter.vue'

const WorkloadTypeMapping: any = {
  42: 'ReplicaSet',
  43: 'Deployment',
  49: 'DaemonSet',
  50: 'StatefulSet',
}

@Component({
  components: {
    QueryFilter,
  },
})
export default class Workload extends Vue {
  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private filterGroupData: any[] = [
    {
      title: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, titleKey: 'modules.views.infrastructure.cluster.s_85fe5099',
      name: 'cluster',
      options: [
        { label: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, labelKey: 'modules.views.infrastructure.cluster.s_85fe5099', value: 'clusterId' },
      ]
    },
    {
      title: 'Namespace',
      name: 'namespace',
      options: [
        { label: i18n.t('modules.views.infrastructure.namespace.s_34f14035') as string, labelKey: 'modules.views.infrastructure.namespace.s_34f14035', value: 'namespaceName' },
      ]
    },
    {
      title: 'Workload',
      name: 'workload',
      options: [
        { label: i18n.t('modules.views.infrastructure.pod.s_dbfbf71f') as string, labelKey: 'modules.views.infrastructure.pod.s_dbfbf71f', value: 'workloadName' },
        { label: i18n.t('modules.views.infrastructure.pod.s_4186ca50') as string, labelKey: 'modules.views.infrastructure.pod.s_4186ca50', value: 'workloadType' },
      ]
    },
    {
      title: 'Pod',
      name: 'pod',
      options: [
        { label: i18n.t('modules.views.infrastructure.pod.s_916ad079') as string, labelKey: 'modules.views.infrastructure.pod.s_916ad079', value: 'podStatus' },
      ]
    },
  ]
  private filterGroupMapping: any = {}

  private queryParams: any = {
    tagData: {},
  }
  private timeParams: any = {}

  private columnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 200, defaultSort: 'asc', handleClick: this.jumpWorkloadDetail, },
    { field: 'type', label: i18n.t('modules.views.alarmCenter.eventDetail.s_226b0912') as string, labelKey: 'modules.views.aiPlatform.experts.s_226b0912', minWidth: 100, unit: 'clusterType', },
    { field: 'pods', label: 'Pods', minWidth: 100, },
    { field: 'namespace', label: 'Namespace', minWidth: 100, },
    { field: 'clusterName', label: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, labelKey: 'modules.views.infrastructure.cluster.s_85fe5099', minWidth: 100, },
  ]

  private queryApi = KubernetesApi.getWorkloadList

  get getQueryParams () {
    const { tagData } = this.queryParams
    return {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      clusterId: tagData.clusterId || '',
      nsName: tagData.namespaceName || '',
      resourceName: tagData.workloadName || '',
      resourceType: tagData.workloadType || '',
      status: tagData.podStatus || '',
    };
  }

  private mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
  }

  private beforeDestroy () {
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh')
  }

  private tableInitedHandle () {
    this.durationChangeHandle()
  }

  private formatFunc (data: any) {
    data.forEach((t: any) => {
      t.id = t.uid
      t.pods = `${t.readyReplicas || 0} of ${t.replicasDesired || 0}`
    });
  }

  public refresh () {
    (this.$refs.listTable as any)?.refresh()
  }

  // 时间范围改变
  private durationChangeHandle () {
    this.timeParams = { ...this.getGlobalTimeV2() }
    this.filterGroupMapping = {}
    this.refresh()
  }

  // 搜索
  private searchChangeHandle(tags: any[]) {
    const tagData: any = {}
    tags.forEach(tag => {
      tagData[tag.type] = tag.value
    })
    this.queryParams.tagData = tagData
    this.refresh()
  }

  // 搜索框 type 切换
  private typeChangeHandle (data: any) {
    if (this.filterGroupMapping[data.value]) { // 已存在
      return
    }
    const item: any = {
      type: data.value,
      loading: false,
      list: [],
    }
    if (data.value === 'workloadType') {
      item.list = Object.entries(WorkloadTypeMapping).map(([value, label]) => ({ label, value }))
      this.$set(this.filterGroupMapping, data.value, { ...item });
    } else if (data.value === 'podStatus') {
      item.list = [
        { label: i18n.t('modules.views.appMonitor.service.s_fd6e80f1') as string, labelKey: 'modules.components.db-table.s_fd6e80f1', value: '1' },
        { label: 'Succeeded', value: 'Succeeded', sub: true },
        { label: 'Running', value: 'Running', sub: true },
        { label: 'Pending', value: 'Pending', sub: true },
        { label: 'Terminating', value: 'Terminating', sub: true },
        { label: 'Completed', value: 'Completed', sub: true },
        { label: i18n.t('modules.views.appMonitor.service.s_c195df63') as string, labelKey: 'modules.components.db-table.s_c195df63', value: '0' },
        { label: 'Failed', value: 'Failed', sub: true },
        { label: 'Unknown', value: 'Unknown', sub: true },
        { label: 'ImagePullBackOff', value: 'ImagePullBackOff', sub: true },
        { label: 'CrashLoopBackOff', value: 'CrashLoopBackOff', sub: true },
        { label: 'OOMKilled', value: 'OOMKilled', sub: true },
        { label: 'SysctlForbidden', value: 'SysctlForbidden', sub: true },
        { label: 'ContainerCreating', value: 'ContainerCreating', sub: true },
        { label: 'InvalidImageName', value: 'InvalidImageName', sub: true },
        { label: 'ImageInspectError', value: 'ImageInspectError', sub: true },
        { label: 'ErrImageNeverPull', value: 'ErrImageNeverPull', sub: true },
        { label: 'RegistryUnavailable', value: 'RegistryUnavailable', sub: true },
        { label: 'CreateContainerConfigError', value: 'CreateContainerConfigError', sub: true },
        { label: 'CreateContainerError', value: 'CreateContainerError', sub: true },
        { label: 'm.internalLifecycle.PreStartContainer', value: 'm.internalLifecycle.PreStartContainer', sub: true },
        { label: 'RunContainerError', value: 'RunContainerError', sub: true },
        { label: 'PostStartHookError', value: 'PostStartHookError', sub: true },
        { label: 'ContainersNotInitialized', value: 'ContainersNotInitialized', sub: true },
        { label: 'ContainersNotReady', value: 'ContainersNotReady', sub: true },
        { label: 'PodInitializing', value: 'PodInitializing', sub: true },
        { label: 'DockerDaemonNotReady', value: 'DockerDaemonNotReady', sub: true },
        { label: 'NetworkPluginNotReady', value: 'NetworkPluginNotReady', sub: true },
        // { label: 'Evicted', value: 'Evicted', sub: true },
        // { label: 'ErrImagePull', value: 'ErrImagePull', sub: true },
        // { label: 'ResourceConstraints', value: 'ResourceConstraints', sub: true },
        // { label: 'Init:CrashLoopBackOff', value: 'Init:CrashLoopBackOff', sub: true },
      ]
      this.$set(this.filterGroupMapping, data.value, { ...item });
    } else {
      this.$set(this.filterGroupMapping, data.value, { ...item });
      this.getFilterList(data.value)
    }
  }
  private async getFilterList (type: string) {
    const getFetchUrl: any = {
      clusterId: KubernetesApi.getClusterSelectList,
      namespaceName: KubernetesApi.getNamespaceSelectList,
      workloadName: KubernetesApi.getWorkloadSelectList,
    }
    const filterItem = this.filterGroupMapping[type]
    if (!getFetchUrl[type] || filterItem.loading) {
      return;
    }
    const params: any = {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      offset: 0,
      size: 9999,
      // scrollId: '',
    }
    if (type !== 'clusterId') {
      params.sortField = 'name'
      params.sortOrder = 'asc'
    }
    filterItem.loading = true
    const { result, error } = await toAsyncWait(getFetchUrl[type](params));
    filterItem.loading = false
    if (!error) {
      const { data } = result
      if (type === 'clusterId') {
        filterItem.list = Object.entries(data || {}).map(([id, name]) => ({ label: `${name} (${id})`, value: id }))
      } else {
        filterItem.list = Object.entries(data || {}).map(([name]) => ({ label: name, value: name }))
      }
    }
  }

  // 跳转到Workload详情
  private jumpWorkloadDetail (data: any) {
    this.$router.push({
      path: '/infrastructure/workloadDetail',
      query: {
        kid: encodeURIComponent(data.id),
        kn: encodeURIComponent(data.name),
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.db-workload-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .db-workload {
    flex: 1;
    min-height: 300px;
    padding: 20px;
    border-radius: 4px;
    background-color: var(--bg-color);
  }

  .db-list {
    height: calc(100% - 32px);
  }
}
</style>
