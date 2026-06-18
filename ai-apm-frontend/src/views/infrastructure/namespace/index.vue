<template>
  <div class="db-namespace-wrapper">
    <div class="db-namespace">
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
          showSetting
          tableKey='INFRA_NAMESPACE_LIST'
          @row-click="jumpNamespaceDetail"
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
import { toAsyncWait } from '@/utils/common';
import QueryFilter from '../cluster/query-filter.vue'
import KubernetesApi from '@/api/kubernetes';

@Component({
  components: {
    QueryFilter,
  },
})
export default class Namespace extends Vue {
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
  ]
  private filterGroupMapping: any = {}

  private queryParams: any = {
    tagData: {},
  }
  private timeParams: any = {}

  private columnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 200, defaultSort: 'asc', handleClick: this.jumpNamespaceDetail, },
    { field: 'clusterName', label: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, labelKey: 'modules.views.infrastructure.cluster.s_85fe5099', minWidth: 200, },
    { field: 'wlCount', label: 'Workloads', unit: 'count', minWidth: 100, handleClick: this.jumpWorkload, },
    { field: 'podCount', label: 'Pods', unit: 'count', minWidth: 100, handleClick: this.jumpPod, },
    { field: 'svcCount', label: 'Services', unit: 'count', minWidth: 100, handleClick: this.jumpService, },
    { field: 'cpuRequest', label: 'CPU requests', unit: 'count', suffix: ' core', minWidth: 100, },
    { field: 'cpuLimit', label: 'CPU limits', unit: 'count', suffix: ' core', minWidth: 100, },
    { field: 'memRequest', label: i18n.t('modules.views.infrastructure.cluster.s_c43bdd6e') as string, labelKey: 'modules.views.infrastructure.cluster.s_c43bdd6e', unit: 'b', minWidth: 100, },
    { field: 'memLimit', label: i18n.t('modules.views.infrastructure.clusterDetail.s_306b4b1d') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_306b4b1d', unit: 'b', minWidth: 100, },
  ]

  private queryApi = KubernetesApi.getNamespaceList

  get getQueryParams () {
    const { tagData } = this.queryParams
    return {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      clusterId: tagData.clusterId || '',
      resourceName: tagData.namespaceName || '',
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
    this.$set(this.filterGroupMapping, data.value, { ...item });
    this.getFilterList(data.value)
  }
  private async getFilterList (type: string, page = 1) {
    const getFetchUrl: any = {
      clusterId: KubernetesApi.getClusterSelectList,
      namespaceName: KubernetesApi.getNamespaceSelectList
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
      const { data, total } = result
      if (type === 'clusterId') {
        filterItem.list = Object.entries(data || {}).map(([id, name]) => ({ label: `${name} (${id})`, value: id }))
      } else {
        filterItem.list = Object.entries(data || {}).map(([name]) => ({ label: name, value: name }))
      }
    }
  }

  // 跳转到Namespace详情
  private jumpNamespaceDetail (data: any) {
    this.$router.push({
      path: '/infrastructure/namespaceDetail',
      query: {
        kid: encodeURIComponent(data.uid),
        kn: encodeURIComponent(data.name as string),
      }
    })
  }
  // 跳转到Workload
  private jumpWorkload (data: any) {
    const { clusterId, clusterName, name } = data
    this.$router.push({
      path: '/infrastructure/workload',
      query: { tags: [`clusterId:${clusterId}:${clusterName} (${clusterId})`, `namespaceName:${name}`], }
    })
  }
  // 跳转到POD
  private jumpPod (data: any) {
    const { clusterId, clusterName, name } = data
    this.$router.push({
      path: '/infrastructure/pod',
      query: { tags: [`clusterId:${clusterId}:${clusterName} (${clusterId})`, `namespaceName:${name}`], }
    })
  }
  // 跳转到Service
  private jumpService (data: any) {
    const { clusterId, clusterName, name } = data
    this.$router.push({
      path: '/infrastructure/service',
      query: { tags: [`clusterId:${clusterId}:${clusterName} (${clusterId})`, `namespaceName:${name}`], }
    })
  }
}
</script>

<style lang="scss" scoped>
.db-namespace-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .db-namespace {
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
