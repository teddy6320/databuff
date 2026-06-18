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
    tableKey='INFRA_CLUSTER_DETAIL_POD_LIST'
    @row-click="jumpPodDetail"
    :row-style="{ cursor: 'pointer' }"
    ref='listTable'>
    <div slot='total' slot-scope="{ total }" class="flex-h">
      <i class="db-icon-pod2 mr-8"></i>
      <span class="fw-500 mr-8">{{ listTitle }}</span>
      {{ total }}
    </div>
    <div slot='status' slot-scope="{ row }" class="flex-h">
      <i v-if='!row.healthStatus' class="db-icon db-icon-error-pie font-12 mr-5 db-red vm"></i>
      <i v-else class="db-icon db-icon-right-pie font-12 mr-5 db-green vm"></i>
      {{ row.status }}
    </div>
  </db-table>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import KubernetesApi from '@/api/kubernetes';

@Component
export default class PodTable extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: '' }) private listTitle!: string;

  private columnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 160, defaultSort: 'asc', handleClick: this.jumpPodDetail, },
    { field: 'type', label: i18n.t('modules.views.alarmCenter.eventDetail.s_226b0912') as string, labelKey: 'modules.views.aiPlatform.experts.s_226b0912', minWidth: 60, unit: 'clusterType', },
    { field: 'status', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', slot: 'status', minWidth: 120, },
    // { field: 'clusterName', label: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, labelKey: 'modules.views.infrastructure.cluster.s_85fe5099', minWidth: 120, },
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
