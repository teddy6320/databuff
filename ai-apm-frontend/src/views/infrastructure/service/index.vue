<template>
  <div class="db-service-wrapper">
    <div class="db-service">
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
          tableKey='INFRA_SERVICE_LIST'
          @row-click="jumpServiceDetail"
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
import KubernetesApi from '@/api/kubernetes';
import QueryFilter from '../cluster/query-filter.vue'

@Component({
  components: {
    QueryFilter,
  },
})
export default class Service extends Vue {
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
      title: 'Kubernetes service',
      name: 'kubernetesService',
      options: [
        { label: i18n.t('modules.views.infrastructure.service.s_c6cad0cb') as string, labelKey: 'modules.views.infrastructure.service.s_c6cad0cb', value: 'serviceName' },
      ]
    },
  ]
  private filterGroupMapping: any = {}

  private queryParams: any = {
    tagData: {},
  }
  private timeParams: any = {}

  private columnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 200, defaultSort: 'asc', handleClick: this.jumpServiceDetail, },
    { field: 'age', label: i18n.t('modules.views.infrastructure.clusterDetail.s_a01a9963') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_a01a9963', unit: 'sDuration', minWidth: 100, },
    { field: 'type', label: i18n.t('modules.views.alarmCenter.eventDetail.s_226b0912') as string, labelKey: 'modules.views.aiPlatform.experts.s_226b0912', minWidth: 100, },
    { field: 'clusterIP', label: 'Cluster IP', minWidth: 100, },
    { field: 'externalIPs', label: 'External IP', minWidth: 100, },
    { field: 'ports', label: 'Ports', minWidth: 150, },
    { field: 'namespace', label: 'Namespace', minWidth: 100, },
  ]

  private queryApi = KubernetesApi.getServiceList

  get getQueryParams () {
    const { tagData } = this.queryParams
    return {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      clusterId: tagData.clusterId || '',
      nsName: tagData.namespaceName || '',
      resourceName: tagData.serviceName || '',
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
      const spec = t.spec || {}
      t.id = t.uid
      t.age = t.creationTimestamp ? +new Date() / 1000 - t.creationTimestamp : '-'
      t.type = spec.type || '-'
      t.clusterIP = spec.clusterIP || '-'
      t.externalIPs = (spec.externalIPs || []).join(',') || '-'
      t.ports = (spec.ports || []).map((p: any) => `${[p.port, p.nodePort].filter(c => !!c).join(':') || '-'}/${p.protocol}`).join(', ') || '-'
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
    this.$set(this.filterGroupMapping, data.value, { ...item });
    this.getFilterList(data.value)
  }
  private async getFilterList (type: string) {
    const getFetchUrl: any = {
      clusterId: KubernetesApi.getClusterSelectList,
      namespaceName: KubernetesApi.getNamespaceSelectList,
      serviceName: KubernetesApi.getServiceSelectList,
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

  // 跳转到Service详情
  private jumpServiceDetail (data: any) {
    this.$router.push({
      path: '/infrastructure/serviceDetail',
      query: {
        kid: encodeURIComponent(data.id),
        kn: encodeURIComponent(data.name),
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.db-service-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .db-service {
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
