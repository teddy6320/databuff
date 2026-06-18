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
    :formatFunc='formatFunc'
    showSetting
    tableKey='INFRA_CLUSTER_DETAIL_NODE_LIST'
    @row-click="jumpNodeDetail"
    :row-style="{ cursor: 'pointer' }"
    ref='listTable'>
    <div slot='total' slot-scope="{ total }" class="flex-h">
      <i class="db-icon-node mr-8"></i>
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
export default class NodeTable extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: '' }) private listTitle!: string;

  private columnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 160, defaultSort: 'asc', handleClick: this.jumpNodeDetail, canClick: this.canDrillDown },
    { field: 'internalIP', label: i18n.t('modules.views.configManage.entity.s_2dc9105c') as string, labelKey: 'modules.views.configManage.entity.s_2dc9105c', minWidth: 120, },
    { field: 'status', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', minWidth: 80, },
    { field: 'cpuUsage', label: 'CPU usage', unit: 'percent', type: 'progress', minWidth: 90, },
    { field: 'cpuRequest', label: 'CPU requests', unit: 'count', suffix: ' core', minWidth: 90, },
    { field: 'cpuLimit', label: 'CPU limits', unit: 'count', suffix: ' core', minWidth: 90, },
    { field: 'cpuAvailable', label: i18n.t('modules.views.infrastructure.clusterDetail.s_be5c3255') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_be5c3255', unit: 'count', suffix: ' core', minWidth: 90, },
    { field: 'memoryUsage', label: i18n.t('modules.views.infrastructure.clusterDetail.s_a1cfd1b6') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_a1cfd1b6', unit: 'percent', type: 'progress', minWidth: 90, },
    { field: 'memoryRequest', label: i18n.t('modules.views.infrastructure.cluster.s_c43bdd6e') as string, labelKey: 'modules.views.infrastructure.cluster.s_c43bdd6e', unit: 'b', minWidth: 90, },
    { field: 'memoryLimit', label: i18n.t('modules.views.infrastructure.clusterDetail.s_306b4b1d') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_306b4b1d', unit: 'b', minWidth: 90, },
    { field: 'memoryAvailable', label: i18n.t('modules.views.infrastructure.clusterDetail.s_14eceb82') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_14eceb82', unit: 'b', minWidth: 90, },
  ]

  private queryApi = KubernetesApi.getNodeList

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

  public clear () {
    (this.$refs.listTable as any)?.clear()
  }

  private formatFunc (data: any) {
    data.forEach((t: any) => {
      const capacity = (t.status || {}).capacity || {}
      const cpu = (+capacity.cpu || 0) / 1000
      const memory = +capacity.memory || 0
      const isMaster = (t.labels || []).join(',').toLocaleLowerCase().includes('master')
      t.id = t.uid
      t.internalIP = ((t.status || {}).nodeAddresses || {}).InternalIP || '-'
      t.status = (t.status || {}).status || '-'
      t.cpuUsage = typeof t.cpuUsage === 'number' && cpu !== 0 ? t.cpuUsage / cpu : '-'
      if (isMaster && t.cpuUsage === 0) {
        t.cpuUsage = '-'
      }
      t.cpuAvailable = cpu - t.cpuRequest
      t.memoryUsage = typeof t.memUsage === 'number' && memory !== 0 ? t.memUsage / memory : '-'
      t.memoryRequest = t.memRequest || t.memRequest === 0 ? t.memRequest || 0 : '-'
      t.memoryLimit = t.memLimit || t.memLimit === 0 ? t.memLimit || 0 : '-'
      t.memoryAvailable = memory - t.memUsage
    });
  }

  public refresh () {
    (this.$refs.listTable as any)?.refresh()
  }

  // 跳转到Node详情
  private jumpNodeDetail (data: any) {
    if (data.status.toLowerCase() === 'notready' || data.status === '-') {
      return
    }
    this.$router.push({
      path: '/infrastructure/nodeDetail',
      query: {
        kid: encodeURIComponent(data.id as string),
        cid: encodeURIComponent(data.clusterId as string),
        kn: encodeURIComponent(data.name as string),
      }
    })
  }

  // 跳转到主机详情
  private jumpHostDetail (data: any) {
    if (!this.canDrillDown(data)) {
      return
    }
    this.$router.push({
      path: '/infrastructure/hostDetail',
      query: { hostName: encodeURIComponent(data.hostName), }
    })
  }

  private canDrillDown (row: any) {
    return row.status.toLowerCase() !== 'notready' && row.status !== '-'
  }
}
</script>
