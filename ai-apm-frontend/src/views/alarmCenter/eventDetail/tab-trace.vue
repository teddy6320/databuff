<template>
  <db-table
    v-if="!empty"
    ref="listTable"
    :queryApi="queryApi"
    :queryParams="queryParams"
    :offsetMode="true"
    :timeMode="false"
    :autoRefresh="false"
    :columnConfig="columnConfig"
    :formatFunc="formatFunc"
    @sort-change="tableRefresh"
    @row-click="showDetailHandle"
    :row-style="{ cursor: 'pointer' }"
    class="event-detail-trace list">
    <span slot='total' slot-scope="{ total }" class="describe">
      <i class="db-icon-date mr-4 font-16 icon-vm"></i>
      <span class="mr-4">{{ queryParams.fromTime.slice(0, 16) }} - {{ queryParams.toTime.slice(0, 16) }}</span>
      <span>{{ $t('modules.views.alarmCenter.alarm.s_9ad9830c', { value0: total }) }}</span>
    </span>
  </db-table>

  <div v-else class="event-detail-trace empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import ApmApi from '@/api/apm';

// 链路筛选区支持的类型
const FilterTypes = [
  { label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c', value: 'resources', key: 'resource' },
  { label: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_47d68cd0', value: 'serviceIds', key: 'serviceId' },
  { label: i18n.t('modules.views.alarmCenter.alarm.s_71673bab') as string, labelKey: 'modules.utils.filters.s_71673bab', value: 'serviceInstances', key: 'serviceInstance' },
  { label: i18n.t('modules.views.alarmCenter.eventDetail.s_e739425d') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_e739425d', value: 'srcServiceIds', key: 'srcServiceId' },
  { label: i18n.t('modules.views.alarmCenter.eventDetail.s_6fcfca3f') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_6fcfca3f', value: 'srcServiceInstances', key: 'srcServiceInstance' },
  { label: i18n.t('modules.views.alarmCenter.eventDetail.s_ea340b9d') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_ea340b9d', value: 'methods', key: 'httpMethod' },
  { label: i18n.t('modules.views.alarmCenter.eventDetail.s_771d897d') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_771d897d', value: 'httpCodes', key: 'httpStatusCode' },
  { label: i18n.t('modules.views.alarmCenter.eventDetail.s_226b0912') as string, labelKey: 'modules.views.aiPlatform.experts.s_226b0912', value: 'types', key: 'type' },
  { label: 'URL', value: 'urls', key: 'url' },
  { label: i18n.t('modules.views.alarmCenter.eventDetail.s_f700c855') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_f700c855', value: 'errorTypes', key: 'errorType' },
  { label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369', value: 'hosts', key: 'host' },
  { label: 'Topic', value: 'topics', key: 'topic' },
  { label: 'ConsumerGroup', value: 'groups', key: 'group' },
  { label: 'Partition', value: 'partitions', key: 'partition' },
  { label: 'Broker', value: 'brokers', key: 'broker' },
  { label: i18n.t('modules.views.alarmCenter.eventDetail.s_5ccbbd01') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_5ccbbd01', value: 'sqlDatabases', key: 'sqlDatabase' },
  { label: i18n.t('modules.views.alarmCenter.eventDetail.s_c26c0d60') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_c26c0d60', value: 'dbTypes', key: 'dbType' },
  { label: i18n.t('modules.views.alarmCenter.eventDetail.s_de9cc3dd') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_de9cc3dd', value: 'sqlOperations', key: 'sqlOperation' },
];

@Component
export default class TabTrace extends Vue {
  @Prop({ default: {}}) private detail!: any;

  public $refs!: {
    listTable: any;
  };

  get isSystemEvent () {
    return this.$route.path === '/sysManage/eventDetail'
  }

  get getBasicServiceMap () {
    return this.$store.getters['Service/basicServiceMap']
  }

  get tagParams () {
    const params: any = {}
    const trigger = (this.detail || {}).trigger || {}
    Object.keys(trigger).filter(key => !!trigger[key]).forEach(key => {
      if (!['serviceId', 'serviceInstance'].includes(key)) {
        params[key] = trigger[key]
      } else {
        params[`exception${key[0].toUpperCase()}${key.slice(1)}`] = trigger[key]
      }
    })
    if (!params.resource && params.httpMethod && params.url) {
      params.resource = `${trigger.httpMethod} ${trigger.url}`
      delete params.httpMethod
      delete params.url
    }
    return params
  }
  get empty () {
    return !Object.keys(this.tagParams).length
  }

  get queryParams () {
    const { _start, _end } = this.detail || {}
    return {
      ...this.tagParams,
      fromTime: dayjs(_start).format('YYYY-MM-DD HH:mm:ss'),
      toTime: dayjs(_end).format('YYYY-MM-DD HH:mm:ss'),
      error: 1,
    }
  }

  private queryApi = ApmApi.getEventSpanList

  private allColumnConfig: any[] = [
    { field: 'start', prop: 'start', label: i18n.t('modules.views.alarmCenter.eventDetail.s_592c5958') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_592c5958', unit: 'time', width: 135, defaultShow: true, sortable: true, defaultSort: 'desc' },
    { field: 'trace_id', prop: 'trace_id', label: 'TraceID', width: 150, defaultShow: true },
    { field: 'resource', prop: 'resource', label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c', minWidth: 120, defaultShow: true, sortable: true, handleClick: this.showDetailHandle },
    { field: 'error', prop: 'error', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', type: 'healthStatus', warningText: i18n.t('modules.views.alarmCenter.eventDetail.s_1efeae37') as string, warningTextKey: 'modules.utils.filters.s_1efeae37', minWidth: 80, defaultShow: true, sortable: true },
    { field: 'duration', prop: 'duration', label: i18n.t('modules.views.alarmCenter.eventDetail.s_39f1374d') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_39f1374d', unit: 'ns', minWidth: 100, defaultShow: true, sortable: true, type: 'progress' },
    { field: 'service', prop: 'service', label: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_47d68cd0', type: 'service', minWidth: 120, defaultShow: true, sortable: true, handleClick: this.showServiceDetailHandle },
    // { field: 'serviceInstance', prop: 'serviceInstance', label: i18n.t('modules.views.alarmCenter.alarm.s_71673bab') as string, labelKey: 'modules.utils.filters.s_71673bab', minWidth: 120, defaultShow: false, sortable: true },
    { field: 'type', prop: 'type', label: i18n.t('modules.views.alarmCenter.eventDetail.s_226b0912') as string, labelKey: 'modules.views.aiPlatform.experts.s_226b0912', minWidth: 80, defaultShow: true, sortable: true },
    // { field: 'meta.http.method', prop: 'meta.http.method', label: i18n.t('modules.views.alarmCenter.eventDetail.s_ea340b9d') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_ea340b9d', minWidth: 80, defaultShow: false, sortable: true },
    { field: 'meta.http.status_code', prop: 'meta.http.status_code', label: i18n.t('modules.views.alarmCenter.eventDetail.s_771d897d') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_771d897d', minWidth: 80, defaultShow: true, sortable: true },
    // { field: 'meta.error.type', prop: 'meta.error.type', label: i18n.t('modules.views.alarmCenter.eventDetail.s_f700c855') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_f700c855', minWidth: 120, defaultShow: false, sortable: true },
    // { field: 'meta.peer.hostname', prop: 'meta.peer.hostname', label: 'http host', minWidth: 120, defaultShow: false, sortable: true },
    { field: 'hostName', prop: 'hostName', label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369', minWidth: 120, defaultShow: true, sortable: true },
  ];

  get columnConfig () {
    const columns = ['start', 'trace_id', 'resource', 'error', 'duration']
    return this.allColumnConfig.filter(t => this.isSystemEvent || columns.includes(t.field))
  }

  public getData () {
    if (!this.empty) {
      this.tableRefresh();
    }
  }

  public resize () {
    if (!this.empty) {
      (this.$refs.listTable as any)?.getHeightHandle();
    }
  }

  private formatFunc (data: any) {
    data.forEach((i: any) => {
      i.start = String(i.start).substring(0, 13);
      i['meta.http.method'] = i?.meta?.['http.method'];
      i['meta.http.status_code'] = i?.meta?.['http.status_code'];
      i['meta.error.type'] = i?.meta?.['error.type'];
      i['meta.peer.hostname'] = i?.meta?.['peer.hostname'];
      const { service_type, type } = (this.getBasicServiceMap || {})[i?.serviceId] || {}
      i.service_type = type || service_type
    });
  }

  private tableRefresh () {
    this.$refs.listTable?.refresh()
  }

  // 查看详情
  private showDetailHandle (row: any) {
    const spanStart = String(row?.start).substring(0, 13);
    const spanEnd = String(row?.end).substring(0, 13);
    this.$router.push({
      path: '/appMonitor/traceDetail',
      query: {
        spid: encodeURIComponent(row.span_id),
        tid: encodeURIComponent(row.trace_id),
        ft: `${+spanStart}`,
        tt: `${+spanEnd}`,
      }
    })
  }

  // 查看服务详情
  private showServiceDetailHandle (row: any) {
    this.$router.push({
      path: '/appMonitor/serviceDetail',
      query: {
        ...this.getRouteTimeOrRange,
        sid: encodeURIComponent(row.serviceId)
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.event-detail-trace {
  height: 100%;
  &.list {
    padding-top: 10px !important;
  }
  &.empty {
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 13px;
    color: var(--color-text-secondary);
  }

  .detail-trace-list {
    height: calc(100% - 32px);
  }
}
</style>
