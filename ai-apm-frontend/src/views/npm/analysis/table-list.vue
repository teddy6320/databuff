<template>
  <div class="table-list"
    v-loading="(queryParams.pageNum === 1 && isLoading) || queryLoading">
    <div class="list-head flex-h-jc">
      <span class="list-total describe">{{ $t('modules.views.alarmCenter.alarm.s_9ad9830c', { value0: listTotal }) }}</span>

      <el-popover
        placement="bottom"
        width="216"
        trigger="click"
      >
        <ul class="columns-setting">
          <li v-for='item in columnsFullLabels' :key='item.value' class="columns-setting-item">
            <span class="columns-setting-item-label">{{ (item.group || '') + item.label }}</span>
            <el-switch v-model='columnsModels[item.value]' class="columns-setting-switch"></el-switch>
          </li>
        </ul>
        <span slot="reference" class="list-setting-btn blue cp">
          <i class="btn-icon el-icon-setting"></i> {{ $t('modules.views.appMonitor.service.s_e366ccf1') }}
        </span>
      </el-popover>
    </div>

    <div ref="tableWrap" class="list-body">
      <el-table
        ref="table"
        :data="tableList"
        @row-click="rowClickHandle"
        @sort-change="sortChangeHandle"
        highlight-current-row size="small"
        :height="tableHeight"
        :empty-text="!isLoading ? $t('modules.views.appMonitor.errorDetail.s_21efd88b') : ' '"
        :row-style="{ cursor: 'pointer' }"
        class="table"
      >
        <el-table-column :label="$t('modules.views.npm.analysis.s_efc6882b')" prop="_client" min-width="150" show-overflow-tooltip />
        <el-table-column :label="$t('modules.views.npm.analysis.s_55abea2d')" prop="_server" min-width="150" show-overflow-tooltip />

        <el-table-column
          v-for="col in getColumns.filter(col => !col.group)"
          :key="col.value"
          :label="col.label"
          :prop="col.prop"
          :sortable="col.prop ? 'custom' : false"
          :sort-orders="['descending', 'ascending', null]"
          :min-width="col.minWidth"
          show-overflow-tooltip>
          <template slot-scope="{ row }">{{ row[col.value] || '-' }}</template>
        </el-table-column>

        <el-table-column
          v-if="getColumns.filter(col => col.group === '客户端到服务端').length"
          :label="$t('modules.views.npm.analysis.s_b4fb338d')">
          <el-table-column
          v-for="col in getColumns.filter(col => col.group === '客户端到服务端')"
          :key="col.value"
          :label="col.label"
          :prop="col.prop"
          :sortable="col.prop ? 'custom' : false"
          :sort-orders="['descending', 'ascending', null]"
          :min-width="col.minWidth"
          show-overflow-tooltip>
            <template slot-scope="{ row }">
              {{ row[col.value] | valueFilter(col.unit) }} /
              <span v-if="col.value === 'npm.volume_sent'">{{ row['npm.throughput_sent'] | valueFilter('bytes/s') }}</span>
            </template>
          </el-table-column>
        </el-table-column>

        <el-table-column
          v-if="getColumns.filter(col => col.group === '服务端到客户端').length"
          :label="$t('modules.views.npm.analysis.s_0b663e56')">
          <el-table-column
          v-for="col in getColumns.filter(col => col.group === '服务端到客户端')"
          :key="col.value"
          :label="col.label"
          :prop="col.prop"
          :sortable="col.prop ? 'custom' : false"
          :sort-orders="['descending', 'ascending', null]"
          :min-width="col.minWidth"
          show-overflow-tooltip>
            <template slot-scope="{ row }">
              {{ row[col.value] | valueFilter(col.unit) }} /
              <span v-if="col.value === 'npm.volume_rcvd'">{{ row['npm.throughput_rcvd'] | valueFilter('bytes/s') }}</span>
            </template>
          </el-table-column>
        </el-table-column>

        <el-table-column
          v-if="getColumns.filter(col => col.group === 'TCP').length"
          label="TCP">
          <el-table-column
          v-for="col in getColumns.filter(col => col.group === 'TCP')"
          :key="col.value"
          :label="col.label"
          :prop="col.prop"
          :sortable="col.prop ? 'custom' : false"
          :sort-orders="['descending', 'ascending', null]"
          :min-width="col.minWidth"
          show-overflow-tooltip>
            <template slot-scope="{ row }">{{ row[col.value] | valueFilter(col.unit) }}</template>
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
import { orderBy } from 'lodash';
import deepClone from 'lodash/cloneDeep';
import humanFormat from 'human-format';
import { debounce } from '@/utils/common';
import getUnitData from '@/utils/getUnitData';
import NpmApi from '@/api/npm';
import { toAsyncWait } from '@/utils/common';

@Component({
  filters: {
    valueFilter (value: number, unit: string) {
      if (!value && String(value) !== '0' || isNaN(+value) || !isFinite(+value)) {
        return '-'
      }
      const _val = Number(value)
      if (_val === 0) {
        return '0'
      }
      const { scale_factor, scale, sub_unit, family } = getUnitData(unit);
      const vData = humanFormat.raw(Number(value) * scale_factor, {
        ...scale,
        decimals: 2,
      })
      if (!['time', 'bytes'].includes(family)) {
        const _value = _val < 0.1 ? '< 0.1' : `${vData.value}${vData.prefix}`
        return `${_value} ${scale.unit}${sub_unit}`
      }
      return `${vData.value} ${vData.prefix}${scale.unit}${sub_unit}`
    }
  }
})
export default class TableList extends Vue {
  @Prop({ default: true }) private showChart!: boolean;
  @Prop({ default: () => ({}) }) private query!: any;
  @Prop({ default: () => ({}) }) private filter!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;
  @Prop({ default: false }) private queryLoading!: boolean;

  public $refs!: {
    tableWrap: HTMLDivElement,
    table: Table,
  }

  get filterFrom () {
    let _from: any[] = []
    Object.entries(this.filter || {}).forEach(([key, value]: any) => {
      const list: any[] = (value || []).map((v: any, i: number) => {
        return { left: key, right: v, operator: '=', connector: 'OR' }
      })
      _from = !_from ? list : [{
        left: deepClone(_from),
        connector: 'AND',
        right: list,
      }]
    })
    return _from
  }

  @Watch('showChart')
  private watchShowChart() {
    this.getTableHeight()
  }

  private timer: any = null;
  private scrollContainer: any = null;
  private scrollHandle: any = null;
  private tableHeight: number = 360;

  private queryParams: any = {
    pageNum: 1,
    pageSize: 50,
    sortField: '',
    sortOrder: '',
  }

  private tableKey = 'TCWs_NPM_Analysis' // 唯一标识，用于存储 localStorage
  private allList: any = []
  private isLoading = false
  private listTotal = 0
  private tableList: any[] = []
  get noMore () {
    return this.tableList.length >= this.listTotal
  }

  private columnsFullLabels = [
    { label: i18n.t('modules.views.npm.analysis.s_c6e3373a') as string, labelKey: 'modules.views.npm.analysis.s_c6e3373a', value: 'npm.volume_sent', group: i18n.t('modules.views.npm.analysis.s_26c18fa7') as string, groupKey: 'modules.views.npm.analysis.s_26c18fa7', unit: 'bytes', minWidth: 150 },
    { label: i18n.t('modules.views.npm.analysis.s_c6e3373a') as string, labelKey: 'modules.views.npm.analysis.s_c6e3373a', value: 'npm.volume_rcvd', group: i18n.t('modules.views.npm.analysis.s_b2ceaf37') as string, groupKey: 'modules.views.npm.analysis.s_b2ceaf37', unit: 'bytes', minWidth: 150 },
    { label: i18n.t('modules.views.npm.analysis.s_911d8e70') as string, labelKey: 'modules.views.npm.analysis.s_911d8e70', value: 'npm.tcp_retransmit', prop: 'npm.tcp_retransmit', group: 'TCP', unit: 'requests', minWidth: 100 },
    { label: i18n.t('modules.views.npm.analysis.s_db732ecb') as string, labelKey: 'modules.views.npm.analysis.s_db732ecb', value: 'npm.tcp_latency', prop: 'npm.tcp_latency', group: 'TCP', unit: 'µs', minWidth: 100 },
    { label: 'RTT', value: 'npm.rtt', prop: 'npm.rtt', group: 'TCP', unit: 'µs', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.serviceDetail.s_f1b8e0c6') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_f1b8e0c6', value: 'npm.tcp_jitter', prop: 'npm.tcp_jitter', group: 'TCP', unit: 'µs', minWidth: 100 },
    { label: i18n.t('modules.views.npm.analysis.s_04971445') as string, labelKey: 'modules.views.npm.analysis.s_04971445', value: 'npm.tcp.conns_established', prop: 'npm.tcp.conns_established', group: 'TCP', unit: 'conns', minWidth: 140 },
    { label: i18n.t('modules.views.npm.analysis.s_8d2f3486') as string, labelKey: 'modules.views.npm.analysis.s_8d2f3486', value: 'npm.tcp.conns_established.rate', prop: 'npm.tcp.conns_established.rate', group: 'TCP', unit: 'conns/s', minWidth: 140 },
    { label: i18n.t('modules.views.npm.analysis.s_203ba11e') as string, labelKey: 'modules.views.npm.analysis.s_203ba11e', value: 'npm.tcp.conns_closed', prop: 'npm.tcp.conns_closed', group: 'TCP', unit: 'conns', minWidth: 140 },
    { label: i18n.t('modules.views.npm.analysis.s_8ff5d43d') as string, labelKey: 'modules.views.npm.analysis.s_8ff5d43d', value: 'npm.tcp.conns_closed.rate', prop: 'npm.tcp.conns_closed.rate', group: 'TCP', unit: 'conns/s', minWidth: 140 },
  ];
  private columnsModels: any = {
    'npm.volume_sent': true,
    'npm.volume_rcvd': true,
    'npm.tcp_retransmit': true,
    'npm.tcp_latency': true,
    'npm.rtt': true,
    'npm.tcp_jitter': true,
    'npm.tcp.conns_established': false,
    'npm.tcp.conns_established.rate': false,
    'npm.tcp.conns_closed': false,
    'npm.tcp.conns_closed.rate': false,
  }
  get getColumns () {
    return this.columnsFullLabels.filter((item) => this.columnsModels[item.value])
  }
  @Watch('columnsModels', { deep: true })
  private onColumnsModelsChange (newVal: any) {
    if (window && window.localStorage) {
      window.localStorage.setItem(this.tableKey + '_COLUMNS', JSON.stringify(newVal))
    }
  }

  private created() {
    this.getLocalColumnsModels()
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
    // if (this.isLoading) {
    //   return;
    // }
    const { fromTime, toTime, interval } = this.timeParams
    const { client, server } = this.query
    const params: any = {
      start: Math.floor(+new Date(fromTime)),
      end: Math.floor(+new Date(toTime)),
      interval,
      by: [client, server].filter(t => t),
      from: [{
        left: [...this.query.from],
        connector: 'AND',
        right: [...this.filterFrom],
      }],
    }
    this.isLoading = true;
    const { result, error } = await toAsyncWait(NpmApi.getPerformanceList(params))
    this.isLoading = false;
    if (!error) {
      const data = result.data || []
      data.forEach((item: any) => {
        const tags = item.tags || {}
        item._client = tags[client] || '-'
        item._server = tags[server] || '-'
      })
      const { sortOrder, sortField } = this.queryParams
      if (sortOrder) {
        this.allList = orderBy(data, [sortField], [sortOrder]);
      } else {
        this.allList = orderBy(data, ['_client', '_server'], ['asc', 'asc']);
      }
      this.listTotal = data.length
    } else {
      this.allList = []
      this.listTotal = 0
    }
    this.getTableList()
  }

  // 分页
  private async getTableList(page = 1) {
    const { pageSize } = this.queryParams
    this.queryParams.pageNum = page
    if (page === 1 && this.scrollContainer) {
      // 滚动区域 scrollTop 置为 0
      this.scrollContainer.scrollTop = 0
    }
    this.tableList = [
      ...(page === 1 ? [] : this.tableList),
      ...this.allList.slice((page - 1) * pageSize, page * pageSize)
    ];
    this.$nextTick(() => {
      if (!this.scrollContainer) {
        this.loop();
      }
    })
  }

  private rowClickHandle (row: any) {
    this.$emit('show-detail', {
      ...row,
      _fromTime: this.timeParams.fromTime,
      _toTime: this.timeParams.toTime,
    })
  }

  // 表格排序
  private sortChangeHandle (data: any) {
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

  private getLocalColumnsModels () {
    if (window && window.localStorage) {
      const _columnsModelsStr = window.localStorage.getItem(this.tableKey + '_COLUMNS')
      if (!_columnsModelsStr) {
        window.localStorage.setItem(this.tableKey + '_COLUMNS', JSON.stringify(this.columnsModels));
        return
      }
      try {
        const _columnsModels = JSON.parse(_columnsModelsStr);
        if (Object.keys(_columnsModels)) {
          for (const key in this.columnsModels) {
            if (Object.prototype.hasOwnProperty.call(_columnsModels, key) && typeof _columnsModels[key] === 'boolean') {
              this.columnsModels[key] = _columnsModels[key]
            }
          }
        }
      } catch (err) {
        console.log(err)
      }
    }
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
    }
  }
}
</style>
