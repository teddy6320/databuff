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
    tableKey='INFRA_CLUSTER_DETAIL_SERVICE_LIST'
    @row-click="jumpServiceDetail"
    :row-style="{ cursor: 'pointer' }"
    ref='listTable'>
    <div slot='total' slot-scope="{ total }" class="flex-h">
      <i class="db-icon-service mr-8"></i>
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
export default class ServiceTable extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: '' }) private listTitle!: string;

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
