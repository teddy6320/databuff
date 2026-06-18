<template>
  <div class="db-cluster-wrapper">
    <div class="db-cluster">
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
          tableKey='INFRA_CLUSTER_LIST'
          @row-click="jumpClusterDetail"
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
import QueryFilter from './query-filter.vue'

@Component({
  components: {
    QueryFilter,
  },
})
export default class Cluster extends Vue {
  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private filterGroupData: any[] = [
    {
      title: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, titleKey: 'modules.views.infrastructure.cluster.s_85fe5099',
      name: 'cluster',
      options: [
        { label: i18n.t('modules.views.infrastructure.cluster.s_c3f28b34') as string, labelKey: 'modules.views.infrastructure.cluster.s_c3f28b34', value: 'clusterName' },
        { label: i18n.t('modules.views.infrastructure.cluster.s_7329a263') as string, labelKey: 'modules.views.infrastructure.cluster.s_7329a263', value: 'clusterId' },
      ]
    },
  ]
  private filterGroupMapping: any = {}

  private queryParams: any = {
    tagData: {},
  }
  private timeParams: any = {}

  private columnConfig = [
    { field: 'name', prop: 'clusterName', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 150, defaultSort: 'asc', handleClick: this.jumpClusterDetail, },
    { field: 'id', label: i18n.t('modules.views.infrastructure.cluster.s_7329a263') as string, labelKey: 'modules.views.infrastructure.cluster.s_7329a263', minWidth: 250, },
    { field: 'nsCount', label: 'Namespace', unit: 'count', minWidth: 100, handleClick: this.jumpNamespace, },
    { field: 'wlCount', label: 'Workloads', unit: 'count', minWidth: 100, handleClick: this.jumpWorkload, },
    { field: 'nodeCount', label: i18n.t('modules.views.infrastructure.cluster.s_a6967bfc') as string, labelKey: 'modules.views.infrastructure.cluster.s_a6967bfc', unit: 'count', minWidth: 100, handleClick: this.jumpNode, },
    { field: 'podCount', label: 'Pods', unit: 'count', minWidth: 100, handleClick: this.jumpPod, },
    { field: 'svcCount', label: 'Services', unit: 'count', minWidth: 100, handleClick: this.jumpService, },
    { field: 'cpuCores', label: i18n.t('modules.views.infrastructure.cluster.s_9fe399cf') as string, labelKey: 'modules.views.infrastructure.cluster.s_9fe399cf', unit: 'count', suffix: ' core', minWidth: 100, },
    { field: 'memRequest', label: i18n.t('modules.views.infrastructure.cluster.s_c43bdd6e') as string, labelKey: 'modules.views.infrastructure.cluster.s_c43bdd6e', unit: 'b', minWidth: 100, },
    { field: 'memoryCapacity', label: i18n.t('modules.views.infrastructure.cluster.s_c983743f') as string, labelKey: 'modules.views.infrastructure.cluster.s_c983743f', unit: 'b', minWidth: 100, },
  ]

  private queryApi = KubernetesApi.getClusterList

  get getQueryParams () {
    const { tagData } = this.queryParams
    return {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      clusterName: tagData.clusterName || '',
      clusterId: tagData.clusterId || '',
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
      t.id = t.clusterId
      t.cpuCores = (+t.cpuCapacity || 0) / 1000
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
    if (data.value === 'clusterName' || data.value === 'clusterId') {
      this.$set(this.filterGroupMapping, 'clusterName', { ...item });
      this.$set(this.filterGroupMapping, 'clusterId', { ...item });
      this.getFilterList(data.value)
    } else {
      this.$set(this.filterGroupMapping, data.value, { ...item });
      this.getFilterList(data.value)
    }
  }
  private async getFilterList (type: string) {
    const filterItem = this.filterGroupMapping[type]
    const params: any = {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
    }
    const getFetchUrl: any = {
      clusterName: KubernetesApi.getClusterSelectList,
      clusterId: KubernetesApi.getClusterSelectList,
    }
    if (!getFetchUrl[type]) {
      return
    }
    filterItem.loading = true
    const { result, error } = await toAsyncWait(getFetchUrl[type](params));
    filterItem.loading = false
    if (!error) {
      const { data } = result
      if (type === 'clusterName' || type === 'clusterId') {
        const ids = Object.keys(data || {})
        const names = Array.from(new Set(Object.values(data || {}))).sort()
        this.filterGroupMapping.clusterName.list = names.map(name => ({ label: name, value: name }))
        this.filterGroupMapping.clusterId.list = ids.map(id => ({ label: id, value: id }))
      }
    }
  }

  // 跳转到集群详情
  private jumpClusterDetail (data: any) {
    this.$router.push({
      path: '/infrastructure/clusterDetail',
      query: {
        kid: encodeURIComponent(data.id),
        kn: encodeURIComponent(data.name),
      }
    })
  }
  // 跳转到Node分析
  private jumpNode (data: any) {
    this.$router.push({
      path: '/infrastructure/node',
      query: { kid: encodeURIComponent(data.id), }
    })
  }
  // 跳转到Namespace
  private jumpNamespace (data: any) {
    this.$router.push({
      path: '/infrastructure/namespace',
      query: { tags: [`clusterId:${data.id}:${data.name} (${data.id})`], }
    })
  }
  // 跳转到Workload
  private jumpWorkload (data: any) {
    this.$router.push({
      path: '/infrastructure/workload',
      query: { tags: [`clusterId:${data.id}:${data.name} (${data.id})`], }
    })
  }
  // 跳转到POD
  private jumpPod (data: any) {
    this.$router.push({
      path: '/infrastructure/pod',
      query: { tags: [`clusterId:${data.id}:${data.name} (${data.id})`], }
    })
  }
  // 跳转到Service
  private jumpService (data: any) {
    this.$router.push({
      path: '/infrastructure/service',
      query: { tags: [`clusterId:${data.id}:${data.name} (${data.id})`], }
    })
  }
}
</script>

<style lang="scss" scoped>
.db-cluster-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .db-cluster {
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
