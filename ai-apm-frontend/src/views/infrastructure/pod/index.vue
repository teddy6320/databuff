<template>
  <div class="db-pod-wrapper">
    <div class="db-pod">
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
          tableKey='INFRA_POD_LIST'
          @row-click="jumpPodDetail"
          :row-style="{ cursor: 'pointer' }"
          ref='listTable'>
          <div slot='status' slot-scope="{ row }" class="flex-h">
            <i v-if='!row.healthStatus' class="db-icon db-icon-error-pie font-12 mr-5 db-red vm"></i>
            <i v-else class="db-icon db-icon-right-pie font-12 mr-5 db-green vm"></i>
            {{ row.status }}
          </div>
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
export default class Pod extends Vue {
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
        { label: i18n.t('modules.views.infrastructure.pod.s_4349badf') as string, labelKey: 'modules.views.infrastructure.pod.s_4349badf', value: 'podName' },
        { label: i18n.t('modules.views.infrastructure.pod.s_916ad079') as string, labelKey: 'modules.views.infrastructure.pod.s_916ad079', value: 'podStatus' },
      ]
    },
    {
      title: 'Node',
      name: 'node',
      options: [
        { label: i18n.t('modules.views.infrastructure.pod.s_992e61c9') as string, labelKey: 'modules.views.infrastructure.pod.s_992e61c9', value: 'nodeName' },
      ]
    },
  ]
  private filterGroupMapping: any = {}

  private queryParams: any = {
    tagData: {},
  }
  private timeParams: any = {}

  private columnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 160, defaultSort: 'asc', handleClick: this.jumpPodDetail, },
    { field: 'type', label: i18n.t('modules.views.alarmCenter.eventDetail.s_226b0912') as string, labelKey: 'modules.views.aiPlatform.experts.s_226b0912', minWidth: 60, unit: 'clusterType', },
    { field: 'status', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', slot: 'status', minWidth: 120, },
    { field: 'clusterName', label: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, labelKey: 'modules.views.infrastructure.cluster.s_85fe5099', minWidth: 120, },
    { field: 'namespace', label: 'Namespace', minWidth: 120, },
    { field: 'workload', label: 'Workloads', minWidth: 120, },
    { field: 'nodeName', label: 'Node', minWidth: 120, },
    { field: 'age', label: i18n.t('modules.views.infrastructure.clusterDetail.s_a01a9963') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_a01a9963', unit: 'sDuration', minWidth: 100, },
    { field: 'ip', label: i18n.t('modules.views.configManage.entity.s_2dc9105c') as string, labelKey: 'modules.views.configManage.entity.s_2dc9105c', minWidth: 120, },
    { field: 'container', label: i18n.t('modules.views.appMonitor.serviceDetail.s_22c79904') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_22c79904', unit: 'count', minWidth: 60, },
    { field: 'containerRestart', label: i18n.t('modules.views.infrastructure.clusterDetail.s_ce6b4ac9') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_ce6b4ac9', unit: 'count', minWidth: 100, },
  ]

  private queryApi = KubernetesApi.getPodList

  get getQueryParams () {
    const { tagData } = this.queryParams
    return {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      clusterId: tagData.clusterId || '',
      nsName: tagData.namespaceName || '',
      wlName: tagData.workloadName || '',
      wlKind: WorkloadTypeMapping[tagData.workloadType] || tagData.workloadType || '',
      resourceName: tagData.podName || '',
      status: tagData.podStatus || '',
      statusType: tagData.podStatusType ? +tagData.podStatusType : '',
      nodeName: tagData.nodeName || '',
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
      t.workload = t.wlName
      t.ip = t.IP
      t.age = t.creationTimestamp ? +new Date() / 1000 - t.creationTimestamp : '-'
      t.container = (t.containerStatuses || []).length
      t.containerRestart = (t.containerStatuses || []).reduce((n: number, c: any) => n + (c.restartCount || 0), 0)
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
      if (tag.type === 'podStatus' && (tag.value === '0' || tag.value === '1')) {
        tagData.podStatusType = tag.value
      } else {
        tagData[tag.type] = tag.value
      }
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
      podName: KubernetesApi.getPodSelectList,
      nodeName: KubernetesApi.getNodeSelectList,
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

  // 跳转到Pod详情
  private jumpPodDetail (data: any) {
    this.$router.push({
      path: '/infrastructure/podDetail',
      query: {
        kid: encodeURIComponent(data.id),
        kn: encodeURIComponent(data.name),
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.db-pod-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .db-pod {
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
