<template>
  <db-table
    ref="listTable"
    :queryApi="queryApi"
    :queryParams="getQueryParams"
    :timeMode="false"
    :autoRefresh="false"
    :offsetMode="true"
    :showTotal="true"
    :columnConfig="columnConfig"
    :formatFunc="formatFunc"
    @sort-change="getData"
    @row-click="jumpTraceDetail"
    :row-style="{ cursor: 'pointer' }">
  </db-table>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import ApmApi from '@/api/apm';

@Component
export default class DetailTrace extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;

  private queryApi = ApmApi.getSpanList
  private columnConfig = [
    { field: '_start', prop: 'start', label: i18n.t('modules.views.alarmCenter.eventDetail.s_592c5958') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_592c5958', unit: 'time', sortable: true, minWidth: 140, },
    { field: 'resource', label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c', sortable: true, minWidth: 200, handleClick: this.jumpTraceDetail, },
    { field: 'error', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', type: 'healthStatus', sortable: true, minWidth: 80 },
    { field: 'duration', label: i18n.t('modules.views.alarmCenter.eventDetail.s_39f1374d') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_39f1374d', unit: 'ns', type: 'progress', sortable: true, minWidth: 100, },
    { field: 'service', label: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_47d68cd0', type: 'service', sortable: true, minWidth: 120, handleClick: this.jumpServiceDetail, },
    { field: 'type', label: i18n.t('modules.views.alarmCenter.eventDetail.s_226b0912') as string, labelKey: 'modules.views.aiPlatform.experts.s_226b0912', sortable: true, minWidth: 80 },
    { field: 'meta.http.status_code', label: i18n.t('modules.views.alarmCenter.eventDetail.s_771d897d') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_771d897d', sortable: true, minWidth: 80 },
  ]

  get getQueryParams () {
    const params: any = {
      ...this.queryParams,
      parentId: '0',
      sortField: 'startTime',
      sortOrder: 'desc',
    }
    delete params.interval
    return params;
  }

  public getData () {
    (this.$refs.listTable as any)?.refresh()
  }

  private formatFunc (data: any) {
    return data.map((t: any) => ({
      ...t,
      '_start': +String(t.start).substring(0, 13),
      'meta.http.status_code': t?.meta?.['http.status_code'],
    }));
  }

  // 跳转到进程详情
  private jumpTraceDetail (data: any) {
    this.$router.push({
      path: '/appMonitor/traceDetail',
      query: {
        spid: encodeURIComponent(data.span_id || ''),
        tid: encodeURIComponent(data.trace_id),
        ft: `${+new Date(this.queryParams.fromTime)}`,
        tt: `${+new Date(this.queryParams.toTime)}`,
      }
    })
  }

  // 跳转到服务详情
  private jumpServiceDetail (row: any) {
    this.$router.push({
      path: '/appMonitor/serviceDetail',
      query: {
        ...this.getRouteTimeOrRange,
        sid: encodeURIComponent(row.serviceId),
      }
    })
  }
}
</script>
