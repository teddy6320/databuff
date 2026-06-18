<template>
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
    tableKey='INFRA_CLUSTER_DETAIL_NAMESPACE_LIST'
    @row-click="jumpNamespaceDetail"
    :row-style="{ cursor: 'pointer' }"
    ref='listTable'>
    <div slot='total' slot-scope="{ total }" class="flex-h">
      <i class="db-icon-namespace mr-8"></i>
      <span class="fw-500 mr-8">{{ listTitle }}</span>
      {{ total }}
    </div>
  </db-table>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import KubernetesApi from '@/api/kubernetes';

@Component
export default class NamespaceTable extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: '' }) private listTitle!: string;

  private columnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 200, defaultSort: 'asc', handleClick: this.jumpNamespaceDetail, },
    // { field: 'clusterName', label: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, labelKey: 'modules.views.infrastructure.cluster.s_85fe5099', minWidth: 200, },
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
    return {
      fromTime: this.queryParams.fromTime,
      toTime: this.queryParams.toTime,
      interval: this.queryParams.interval,
      clusterId: this.queryParams.clusterId || '',
      query: this.queryParams.queryText || '',
    };
  }

  private tableInitedHandle () {
    // this.refresh()
  }

  public refresh () {
    (this.$refs.listTable as any)?.refresh()
  }

  public clear () {
    (this.$refs.listTable as any)?.clear()
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
