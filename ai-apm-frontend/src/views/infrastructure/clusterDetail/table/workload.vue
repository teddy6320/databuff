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
    tableKey='INFRA_CLUSTER_DETAIL_WORKLOAD_LIST'
    @row-click="jumpWorkloadDetail"
    :row-style="{ cursor: 'pointer' }"
    ref='listTable'>
    <div slot='total' slot-scope="{ total }" class="flex-h">
      <i class="db-icon-workload mr-8"></i>
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
export default class WorkloadTable extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: '' }) private listTitle!: string;

  private columnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 200, defaultSort: 'asc', handleClick: this.jumpWorkloadDetail, },
    // { field: 'type', label: i18n.t('modules.views.alarmCenter.eventDetail.s_226b0912') as string, labelKey: 'modules.views.aiPlatform.experts.s_226b0912', minWidth: 100, unit: 'clusterType', },
    { field: 'pods', label: 'Pods', minWidth: 100, },
    { field: 'namespace', label: 'Namespace', minWidth: 100, },
    // { field: 'clusterName', label: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, labelKey: 'modules.views.infrastructure.cluster.s_85fe5099', minWidth: 100, },
  ]

  private queryApi = KubernetesApi.getWorkloadList

  get getQueryParams () {
    return {
      fromTime: this.queryParams.fromTime,
      toTime: this.queryParams.toTime,
      interval: this.queryParams.interval,
      clusterId: this.queryParams.clusterId || '',
      query: this.queryParams.queryText || '',
      resourceType: this.queryParams.resourceType || '',
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
      t.pods = `${t.readyReplicas || 0} of ${t.replicasDesired || 0}`
    });
  }

  public refresh () {
    (this.$refs.listTable as any)?.refresh()
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
