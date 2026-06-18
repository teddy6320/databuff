<template>
  <div class="table-list"
    v-loading="queryParams.pageNum === 1 && isLoading">
    <div class="list-head flex-h-jc">
      <span class="list-total describe">{{ $t('modules.views.alarmCenter.alarm.s_9ad9830c', { value0: listTotal }) }}</span>
    </div>

    <div ref="tableWrap" class="list-body">
      <el-table
        ref="table"
        :data="tableList"
        @row-click="showDetailHandle"
        @sort-change="sortChangeHandle"
        :default-sort="{ prop: 'reqOutCnt', order: 'descending' }"
        :height="tableHeight"
        :empty-text="!isLoading ? $t('modules.views.appMonitor.errorDetail.s_21efd88b') : ' '"
        highlight-current-row size="small"
        tooltip-effect="light"
        :row-style="{ cursor: 'pointer' }"
        class="table"
      >
        <el-table-column
          v-for="col in columns.filter(col => !col.group)"
          :key="col.value"
          :label="col.label"
          :prop="col.prop"
          :sortable="col.prop ? 'custom' : false"
          :sort-orders="['descending', 'ascending', null]"
          :min-width="col.minWidth"
          show-overflow-tooltip>
          <template slot-scope="{ row }">{{ row[col.value] || '-' }}</template>
        </el-table-column>

        <el-table-column :label="$t('modules.views.appMonitor.serviceCall.s_5ffaaba6')">
          <el-table-column
          v-for="col in columns.filter(col => col.group === '发出调用')"
          :key="col.value"
          :label="col.label"
          :prop="col.prop"
          :sortable="col.prop ? 'custom' : false"
          :sort-orders="['descending', 'ascending', null]"
          :min-width="col.minWidth"
          show-overflow-tooltip>
            <template slot-scope="{ row }">
              <template v-if="col.valueType === 'number'">{{ row[col.value] | NumberFilter(col.formatValue && !!row[col.formatValue]) }}{{ col.unit ? ` ${col.unit}` : '' }}</template>
              <template v-else-if="col.valueType === 'bytes'">{{ row[col.value] | BytesFilter(col.formatValue && !!row[col.formatValue]) }}{{ col.unit || '' }}</template>
              <template v-else-if="col.valueType === 'time'">{{ row[col.value] | NsFilter }}</template>
              <template v-else-if="col.value === 'reqOutErrRate'">{{ row[col.value] | PercentFilter(!!row.reqOutErrCnt) }}</template>
              <template v-else>{{ row[col.value] || '-' }}</template>
            </template>
          </el-table-column>
        </el-table-column>

        <el-table-column :label="$t('modules.views.appMonitor.serviceCall.s_827c74b5')">
          <el-table-column
          v-for="col in columns.filter(col => col.group === '接收请求')"
          :key="col.value"
          :label="col.label"
          :prop="col.prop"
          :sortable="col.prop ? 'custom' : false"
          :sort-orders="['descending', 'ascending', null]"
          :min-width="col.minWidth"
          show-overflow-tooltip>
            <template slot-scope="{ row }">
              <template v-if="col.valueType === 'number'">{{ row[col.value] | NumberFilter(col.formatValue && !!row[col.formatValue]) }}{{ col.unit ? ` ${col.unit}` : '' }}</template>
              <template v-else-if="col.valueType === 'bytes'">{{ row[col.value] | BytesFilter(col.formatValue && !!row[col.formatValue]) }}{{ col.unit || '' }}</template>
              <template v-else-if="col.valueType === 'time'">{{ row[col.value] | NsFilter }}</template>
              <template v-else-if="col.value === 'reqInErrRate'">{{ row[col.value] | PercentFilter(!!row.reqInErrCnt) }}</template>
              <template v-else>{{ row[col.value] || '-' }}</template>
            </template>
          </el-table-column>
        </el-table-column>

        <div v-if="queryParams.pageNum > 1" slot="append" class="table-load-tips">
          <template v-if="!noMore">
            <i class="el-icon-loading"></i> {{ $t('modules.views.appMonitor.errorDetail.s_f09b1233') }}
          </template>
          <template v-else>{{ $t('modules.views.appMonitor.errorDetail.s_37c38521') }}</template>
        </div>
      </el-table>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Table } from 'element-ui'
import { debounce } from '@/utils/common';
import ServiceApi from '@/api/service';
import { toAsyncWait } from '@/utils/common';

@Component
export default class TableList extends Vue {
  @Prop({ default: () => ({}) }) private query!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;
  @Prop({ default: '' }) private componentType!: string;

  public $refs!: {
    tableWrap: HTMLDivElement,
    table: Table,
  }

  private timer: any = null;
  private scrollContainer: any = null;
  private scrollHandle: any = null;
  private tableHeight: number = 360;

  private queryParams: any = {
    pageNum: 1,
    pageSize: 50,
    sortField: 'reqOutCnt',
    sortOrder: 'desc',
  }

  private isLoading = false
  private listTotal = 0
  private tableList: any[] = []
  get noMore () {
    return this.tableList.length >= this.listTotal
  }

  private columnsFullLabels = [
    { label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c', value: 'resource', minWidth: 200 },
    // HTTP请求 service.http
    { label: i18n.t('modules.views.alarmCenter.eventDetail.s_ea340b9d') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_ea340b9d', value: 'httpMethod', type: 'service.http', minWidth: 100 },
    // RPC请求 service.rpc
    // { label: i18n.t('modules.views.appMonitor.serviceAnalysis.s_5b26b249') as string, labelKey: 'modules.views.appMonitor.serviceAnalysis.s_5b26b249', value: 'type', type: 'service.rpc', minWidth: 100 },
    // MQ消费 service.mq
    { label: 'Topic', value: 'topic', type: 'service.mq', minWidth: 80 },
    { label: 'ConsumerGroup', value: 'group', type: 'service.mq', minWidth: 130 },
    { label: 'Partition', value: 'partition', type: 'service.mq', minWidth: 80 },
    { label: 'MQ Type', value: 'type', type: 'service.mq', minWidth: 80 },
    { label: 'Broker', value: 'broker', type: 'service.mq', minWidth: 80 },
    // SQL调用 service.db
    { label: i18n.t('modules.views.alarmCenter.eventDetail.s_de9cc3dd') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_de9cc3dd', value: 'sqlOperation', type: 'service.db', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.serviceAnalysis.s_84b916da') as string, labelKey: 'modules.views.appMonitor.serviceAnalysis.s_84b916da', value: 'dbType', type: 'service.db', minWidth: 100 },
    { label: i18n.t('modules.views.alarmCenter.eventDetail.s_5ccbbd01') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_5ccbbd01', value: 'sqlDatabase', type: 'service.db', minWidth: 100 },
    // 发出调用
    { label: i18n.t('modules.views.appMonitor.serviceCall.s_afa1ca56') as string, labelKey: 'modules.views.appMonitor.serviceCall.s_afa1ca56', value: 'reqOutCnt', prop: 'reqOutCnt', group: i18n.t('modules.views.appMonitor.serviceCall.s_5ffaaba6') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_5ffaaba6', valueType: 'number', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.serviceCall.s_0d652d40') as string, labelKey: 'modules.views.appMonitor.serviceCall.s_0d652d40', value: 'reqOutAvgLatency', prop: 'reqOutAvgLatency', group: i18n.t('modules.views.appMonitor.serviceCall.s_5ffaaba6') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_5ffaaba6', valueType: 'time', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.cache.s_0c8524d7') as string, labelKey: 'modules.views.appMonitor.cache.s_0c8524d7', value: 'reqOutErrRate', prop: 'reqOutErrRate', group: i18n.t('modules.views.appMonitor.serviceCall.s_5ffaaba6') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_5ffaaba6', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.resourceDetail.s_0951ecd4') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_0951ecd4', value: 'reqOutAvgReqBodyLength', prop: 'reqOutAvgReqBodyLength', type: ['service.http', 'service.rpc', 'service.redis'], group: i18n.t('modules.views.appMonitor.serviceCall.s_5ffaaba6') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_5ffaaba6', valueType: 'bytes', formatValue: 'reqOutSumReqBodyLength', unit: '/req', minWidth: 110 },
    { label: i18n.t('modules.views.appMonitor.resourceDetail.s_878d0b66') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_878d0b66', value: 'reqOutAvgRespBodyLength', prop: 'reqOutAvgRespBodyLength', type: ['service.http', 'service.rpc', 'service.redis'], group: i18n.t('modules.views.appMonitor.serviceCall.s_5ffaaba6') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_5ffaaba6', valueType: 'bytes', formatValue: 'reqOutSumRespBodyLength', unit: '/req', minWidth: 110 },
    { label: i18n.t('modules.views.appMonitor.serviceCall.s_37647b60') as string, labelKey: 'modules.views.appMonitor.serviceCall.s_37647b60', value: 'reqOutAvgDelay', prop: 'reqOutAvgDelay', type: 'service.mq', group: i18n.t('modules.views.appMonitor.serviceCall.s_5ffaaba6') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_5ffaaba6', valueType: 'time', minWidth: 125 },
    { label: i18n.t('modules.views.appMonitor.serviceCall.s_94351feb') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_94351feb', value: 'reqOutAvgMqBodyLength', prop: 'reqOutAvgMqBodyLength', type: 'service.mq', group: i18n.t('modules.views.appMonitor.serviceCall.s_5ffaaba6') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_5ffaaba6', valueType: 'bytes', formatValue: 'reqOutSumMqBodyLength', unit: '/req', minWidth: 110 },
    { label: i18n.t('modules.views.appMonitor.serviceCall.s_5734b2db') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_5734b2db', value: 'reqOutAvgReadRows', prop: 'reqOutAvgReadRows', type: 'service.db', group: i18n.t('modules.views.appMonitor.serviceCall.s_5ffaaba6') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_5ffaaba6', valueType: 'number', formatValue: 'reqOutSumReadRows', unit: 'row/reqs', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.resourceDetail.s_d181886c') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_d181886c', value: 'reqOutAvgUpdateRows', prop: 'reqOutAvgUpdateRows', type: 'service.db', group: i18n.t('modules.views.appMonitor.serviceCall.s_5ffaaba6') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_5ffaaba6', valueType: 'number', formatValue: 'reqOutSumUpdateRows', unit: 'row/reqs', minWidth: 100 },
    // 接收请求
    { label: i18n.t('modules.views.appMonitor.serviceCall.s_ab34a512') as string, labelKey: 'modules.views.appMonitor.serviceCall.s_ab34a512', value: 'reqInCnt', prop: 'reqInCnt', group: i18n.t('modules.views.appMonitor.serviceCall.s_4374959d') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_4374959d', valueType: 'number', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.serviceCall.s_0d652d40') as string, labelKey: 'modules.views.appMonitor.serviceCall.s_0d652d40', value: 'reqInAvgLatency', prop: 'reqInAvgLatency', group: i18n.t('modules.views.appMonitor.serviceCall.s_4374959d') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_4374959d', valueType: 'time', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.cache.s_0c8524d7') as string, labelKey: 'modules.views.appMonitor.cache.s_0c8524d7', value: 'reqInErrRate', prop: 'reqInErrRate', group: i18n.t('modules.views.appMonitor.serviceCall.s_4374959d') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_4374959d', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.resourceDetail.s_0951ecd4') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_0951ecd4', value: 'reqInAvgReqBodyLength', prop: 'reqInAvgReqBodyLength', type: ['service.http', 'service.rpc', 'service.redis'], group: i18n.t('modules.views.appMonitor.serviceCall.s_4374959d') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_4374959d', valueType: 'bytes', formatValue: 'reqInSumReqBodyLength', unit: '/req', minWidth: 110 },
    { label: i18n.t('modules.views.appMonitor.resourceDetail.s_878d0b66') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_878d0b66', value: 'reqInAvgRespBodyLength', prop: 'reqInAvgRespBodyLength', type: ['service.http', 'service.rpc', 'service.redis'], group: i18n.t('modules.views.appMonitor.serviceCall.s_4374959d') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_4374959d', valueType: 'bytes', formatValue: 'reqInSumRespBodyLength', unit: '/req', minWidth: 110 },
    { label: i18n.t('modules.views.appMonitor.serviceCall.s_37647b60') as string, labelKey: 'modules.views.appMonitor.serviceCall.s_37647b60', value: 'reqInAvgDelay', prop: 'reqInAvgDelay', type: 'service.mq', group: i18n.t('modules.views.appMonitor.serviceCall.s_4374959d') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_4374959d', valueType: 'time', minWidth: 125 },
    { label: i18n.t('modules.views.appMonitor.serviceCall.s_94351feb') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_94351feb', value: 'reqInAvgMqBodyLength', prop: 'reqInAvgMqBodyLength', type: 'service.mq', group: i18n.t('modules.views.appMonitor.serviceCall.s_4374959d') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_4374959d', valueType: 'bytes', formatValue: 'reqInSumMqBodyLength', unit: '/req', minWidth: 110 },
    { label: i18n.t('modules.views.appMonitor.serviceCall.s_5734b2db') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_5734b2db', value: 'reqInAvgReadRows', prop: 'reqInAvgReadRows', type: 'service.db', group: i18n.t('modules.views.appMonitor.serviceCall.s_4374959d') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_4374959d', valueType: 'number', formatValue: 'reqInSumReadRows', unit: 'row/reqs', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.resourceDetail.s_d181886c') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_d181886c', value: 'reqInAvgUpdateRows', prop: 'reqInAvgUpdateRows', type: 'service.db', group: i18n.t('modules.views.appMonitor.serviceCall.s_4374959d') as string, groupKey: 'modules.views.appMonitor.serviceCall.s_4374959d', valueType: 'number', formatValue: 'reqInSumUpdateRows', unit: 'row/reqs', minWidth: 100 },
  ];
  get columns () {
    return this.columnsFullLabels.filter((item) => {
      if (Array.isArray(item.type)) {
        return item.type.includes(this.componentType)
      } else {
        return !item.type || item.type === this.componentType
      }
    })
  }

  private mounted () {
    this.getTableHeight()
    window.addEventListener('resize', this.getTableHeight);
  }

  private beforeDestroy () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    if (this.scrollContainer) {
      this.scrollContainer.removeEventListener('scroll', this.scrollHandle)
    }
    window.removeEventListener('resize', this.getTableHeight);
  }

  public async getData () {
    this.getTableList()
  }

  private async getTableList(page = 1) {
    if (this.isLoading || (page !== 1 && this.noMore)) {
      return;
    }
    this.queryParams.pageNum = page
    const { fromTime, toTime } = this.timeParams
    const { pageSize, sortOrder, sortField } = this.queryParams
    const params: any = {
      ...this.query,
      fromTime, toTime,
      offset: (page - 1) * pageSize,
      size: pageSize,
    }
    Object.entries(params).forEach(([key, value]) => {
      if (value === '') {
        delete params[key]
      }
    })
    if (sortOrder) {
      params.sortOrder = sortOrder
      params.sortField = sortField
    }
    this.isLoading = true;
    const { result, error } = await toAsyncWait(ServiceApi.getServiceCallEndpoints(params));
    this.isLoading = false;
    if (!error) {
      if (page === 1 && this.scrollContainer) {
        // 滚动区域 scrollTop 置为 0
        this.scrollContainer.scrollTop = 0
      }
      const data = result.data || []
      this.listTotal = result.total || 0
      this.tableList = page === 1 ? data : Array.from(this.tableList).concat(data);
      this.$nextTick(() => {
        if (!this.scrollContainer) {
          this.loop();
        }
      })
    }
  }

  // 列表排序
  private sortChangeHandle (data: any) {
    const { prop, order } = data
    this.queryParams.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
    this.queryParams.sortField = order ? prop : ''
    this.getTableList()
  }

  // 调用分析详情
  private showDetailHandle (row: any) {
    const {
      componentType, sn, sid, st, srcSn, srcSid, srcSt,
      serviceInstance, srcServiceInstance, rootResourceQuery,
      durationRange, fromTime, toTime,
    } = this.$route.query as any
    const query: any = {
      componentType,
      resource: encodeURIComponent(row.resource || ''),
      sn, sid, st, srcSn, srcSid, srcSt,
    }
    if (fromTime && toTime) {
      query.fromTime = fromTime
      query.toTime = toTime
    } else if (durationRange) {
      query.durationRange = durationRange
    }
    if (serviceInstance) {
      query.serviceInstance = serviceInstance
    }
    if (srcServiceInstance) {
      query.srcServiceInstance = srcServiceInstance
    }
    if (rootResourceQuery) {
      query.rootResourceQuery = rootResourceQuery
    }
    const params: any = {}
    this.columns.filter(t => t.type && !t.group).forEach(t => {
      params[t.value] = typeof row[t.value] === 'number' ? row[t.value] : row[t.value] || ''
    })
    query.params = encodeURIComponent(JSON.stringify(params))
    this.$router.push({
      path: '/appMonitor/serviceCallDetail',
      query: { ...query }
    });
  }

  // 滚动加载相关
  private loop () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    this.timer = setTimeout(() => {
      const scrollContainer = this.$refs.tableWrap.querySelector('.el-table__body-wrapper');
      if (!scrollContainer) {
        this.loop();
      } else {
        this.scrollContainer = scrollContainer;
        // 滚动到底加载更多
        this.scrollHandle = debounce(() => {
          const { scrollHeight, scrollTop, clientHeight } = scrollContainer
          if (!this.noMore && !this.isLoading && scrollHeight - clientHeight - scrollTop < 50) {
            this.getTableList(this.queryParams.pageNum + 1)
          }
        }, 17)
        scrollContainer.addEventListener('scroll', this.scrollHandle)
      }
    }, 100)
  }
  private getTableHeight () {
    this.$nextTick(() => {
      const { clientHeight } = this.$refs.tableWrap
      this.tableHeight = clientHeight
    })
  }
}
</script>

<style lang="scss" scoped>
.table-list {
  padding: 16px 20px 20px;
  background: var(--bg-color);
  .list-head {
    margin-bottom: 10px;
    font-size: 13px;
    line-height: 22px;
  }
  .list-body {
    height: calc(100% - 32px);
    .table {
      height: 100%;
      overflow: visible;
    }
  }
}
</style>
