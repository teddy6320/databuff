<template>
  <div class="network-wrapper">
    <div>
      <db-radio v-model='typeModel' :options='typeOptions' @change='getAllList'></db-radio>
    </div>

    <db-table
      :data='tableList'
      :total='tableTotal'
      :columnConfig='columnConfig'
      @sort-change="tableSortHandle"
      @on-table-scroll="tableScrollHandle"
      showSetting
      tableKey='APM_SERVICE_DETAIL_NETWORK'
      class="network-list">
    </db-table>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { orderBy } from 'lodash';
import { toAsyncWait } from '@/utils/common';
import NpmApi from '@/api/npm';

@Component
export default class TabNetwork extends Vue {
  @Prop({ default: {} }) private current!: any;

  @Watch('current', { immediate: true })
  private onCurrentChange (val: any, oldVal: any) {
    if (val && val?.serviceId !== oldVal?.serviceId) {
      this.getAllList();
    }
  }

  private typeOptions = [
    { label: i18n.t('modules.views.appMonitor.serviceDetail.s_e8f4909c') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_e8f4909c', value: 'out' },
    { label: i18n.t('modules.views.appMonitor.serviceDetail.s_e5f55566') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_e5f55566', value: 'in' },
  ]
  private typeModel = 'out';

  private queryParams: any = {
    pageNum: 1,
    pageSize: 50,
    sortField: '',
    sortOrder: '',
  }

  private columnConfig: any = [
    { field: '_client', label: i18n.t('modules.views.appMonitor.serviceDetail.s_ae5f22ab') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_ae5f22ab', minWidth: 180 },
    { field: '_server', label: i18n.t('modules.views.appMonitor.serviceDetail.s_f9198c69') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_f9198c69', minWidth: 180 },
    { field: 'npm.volume_sent', label: i18n.t('modules.views.appMonitor.serviceDetail.s_134ecfbe') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_134ecfbe', unit: 'b', minWidth: 120 },
    { field: 'npm.volume_rcvd', label: i18n.t('modules.views.appMonitor.serviceDetail.s_26605df2') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_26605df2', unit: 'b', minWidth: 120 },
    { field: 'npm.tcp_latency', label: i18n.t('modules.views.appMonitor.serviceDetail.s_db705ca9') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_db705ca9', unit: 'ms', sortable: true, minWidth: 100 },
    { field: 'npm.tcp_retransmit', label: i18n.t('modules.views.appMonitor.serviceDetail.s_5b545e42') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_5b545e42', unit: 'requests', sortable: true, minWidth: 120 },
    { field: 'npm.rtt', label: 'RTT', unit: 'ms', sortable: true, minWidth: 100 },
    { field: 'npm.tcp_jitter', label: i18n.t('modules.views.appMonitor.serviceDetail.s_f1b8e0c6') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_f1b8e0c6', unit: 'ms', sortable: true, minWidth: 100 },
    { field: 'npm.tcp.conns_established', label: i18n.t('modules.views.appMonitor.serviceDetail.s_2347150f') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_2347150f', unit: 'conns', sortable: true, minWidth: 150, defaultShow: false },
    { field: 'npm.tcp.conns_established.rate', label: i18n.t('modules.views.appMonitor.serviceDetail.s_74012477') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_74012477', unit: 'conns/s', sortable: true, minWidth: 160, defaultShow: false },
    { field: 'npm.tcp.conns_closed', label: i18n.t('modules.views.appMonitor.serviceDetail.s_0d3eb0bd') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_0d3eb0bd', unit: 'conns', sortable: true, minWidth: 150, defaultShow: false },
    { field: 'npm.tcp.conns_closed.rate', label: i18n.t('modules.views.appMonitor.serviceDetail.s_4ce6d987') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_4ce6d987', unit: 'conns/s', sortable: true, minWidth: 160, defaultShow: false },
  ];

  private isLoading = false;
  private tableTotal = 0;
  private allList: any[] = [];
  private tableList: any[] = [];

  private created () {
    this.$emit('on-created');
  }

  public refresh () {
    this.getAllList();
  }

  private async getAllList () {
    if (!this.current?.serviceId) {
      return;
    }
    const { fromTime, toTime, interval } = this.getGlobalTime();
    const fromRight = this.typeModel !== 'in' ? [
      { left: 'srcServiceId', operator: '=', right: this.current?.serviceId, connector: 'AND' },
      { left: 'isOut', right: '1', operator: '=', connector: 'AND' },
    ] : [
      { left: 'serviceId', operator: '=', right: this.current?.serviceId, connector: 'AND' },
      { left: 'isIn', right: '1', operator: '=', connector: 'AND' },
    ];
    const params: any = {
      start: +new Date(fromTime),
      end: +new Date(toTime),
      interval,
      by: ['srcIp', 'ip:port'],
      from: [{ left: [], right: fromRight, connector: 'AND' }],
    }
    this.isLoading = true;
    const { result, error } = await toAsyncWait(NpmApi.getPerformanceList(params))
    this.isLoading = false;
    if (!error) {
      const µsToMs = (value: any) => {
        if ((!value && String(value) !== '0') || isNaN(+value)) {
          return value;
        }
        return Number(value) / 1000;
      };
      const data = result.data || []
      data.forEach((item: any) => {
        const tags = item.tags || {}
        item._client = (tags.srcIp || '-') + (this.typeModel !== 'in' ? i18n.t('modules.views.appMonitor.serviceDetail.s_262f23ba') as string : '');
        item._server = (tags['ip:port'] || '-') + (this.typeModel === 'in' ? i18n.t('modules.views.appMonitor.serviceDetail.s_262f23ba') as string : '');
        item['npm.tcp_latency'] = µsToMs(item['npm.tcp_latency']);
        item['npm.rtt'] = µsToMs(item['npm.rtt']);
        item['npm.tcp_jitter'] = µsToMs(item['npm.tcp_jitter']);
      })
      const { sortOrder, sortField } = this.queryParams
      if (sortOrder) {
        this.allList = orderBy(data, [sortField], [sortOrder]);
      } else {
        this.allList = orderBy(data, ['_client', '_server'], ['asc', 'asc']);
      }
      this.tableTotal = data.length
    } else {
      this.allList = []
      this.tableTotal = 0
    }
    this.getTableList()
    this.$emit('on-loaded')
  }

  // 分页
  private async getTableList(page = 1) {
    const { pageSize } = this.queryParams
    this.queryParams.pageNum = page
    this.tableList = this.allList.slice(0, page * pageSize);
  }

  // 排序
  private tableSortHandle (data: any) {
    const { prop, order } = data
    this.queryParams.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : null
    this.queryParams.sortField = order ? prop : ''
    const { sortOrder, sortField } = this.queryParams
    if (sortOrder) {
      this.allList = orderBy(this.allList, [sortField], [sortOrder]);
    } else {
      this.allList = orderBy(this.allList, ['_client', '_server'], ['asc', 'asc']);
    }
    this.getTableList()
  }
  // 滚动加载
  private tableScrollHandle () {
    if (this.tableTotal > this.tableList.length) {
      this.getTableList(this.queryParams.pageNum + 1);
    }
  }
}
</script>

<style lang="scss" scoped>
.network-wrapper {
  padding-left: 4px;
  height: 100%;
  display: flex;
  flex-direction: column;

  .network-list {
    flex: 1;
    min-height: 286px;
  }
}
</style>
