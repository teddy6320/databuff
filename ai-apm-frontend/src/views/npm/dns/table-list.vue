<template>
  <div class="table-list"
     :class="`table-list-${layoutType}`"
    v-loading="(queryParams.pageNum === 1 && isLoading) || queryLoading">
    <div class="list-head flex-h-jc">
      <div class="flex-h lh-22">
        <span v-if="layoutType === 'detail'" class="mr-10 font-14">{{ $t('modules.views.npm.dns.s_82e9c074') }}</span>
        <span class="describe">{{ $t('modules.views.alarmCenter.alarm.s_9ad9830c', { value0: listTotal }) }}</span>
      </div>

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
        <el-table-column v-if="layoutType !== 'detail'" :label="$t('modules.views.npm.analysis.s_efc6882b')" prop="_client" min-width="150" show-overflow-tooltip />
        <el-table-column
          :label="layoutType !== 'detail' ? 'DNS IP' : 'DOMAIN'"
          prop="_server" min-width="150" show-overflow-tooltip />

        <el-table-column
          v-for="col in getColumns.filter(col => !col.group)"
          :key="col.value"
          :label="col.label"
          :prop="col.prop"
          :sortable="col.prop ? 'custom' : false"
          :sort-orders="['descending', 'ascending', null]"
          :min-width="col.minWidth"
          show-overflow-tooltip>
          <template slot-scope="{ row }">{{ row[col.value] | valueFilter(col.unit) }}</template>
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
  @Prop({ default: 'list' }) private layoutType!: 'list' | 'detail';

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

  private tableKey = 'TCWs_NPM_Dns' // 唯一标识，用于存储 localStorage
  private allList: any = []
  private isLoading = false
  private listTotal = 0
  private tableList: any[] = []
  get noMore () {
    return this.tableList.length >= this.listTotal
  }

  private columnsFullLabels = [
    { label: i18n.t('modules.views.npm.dns.s_b1ccc6a6') as string, labelKey: 'modules.views.npm.dns.s_b1ccc6a6', value: 'dns.cnt', prop: 'dns.cnt', type: 'metric', unit: 'requests', minWidth: 110 },
    { label: i18n.t('modules.views.appMonitor.relationMap.s_207c26c9') as string, labelKey: 'modules.views.appMonitor.relationMap.s_207c26c9', value: 'dns.response_time', prop: 'dns.response_time', type: 'metric', unit: 'ns', minWidth: 95 },
    { label: i18n.t('modules.views.npm.dns.s_e64ca10b') as string, labelKey: 'modules.views.npm.dns.s_e64ca10b', value: 'dns.errors.pct', prop: 'dns.errors.pct', type: 'metric', unit: 'percent', minWidth: 95 },
    { label: i18n.t('modules.views.npm.dns.s_866eed51') as string, labelKey: 'modules.views.npm.dns.s_866eed51', value: 'dns.errors.timeout.pct', prop: 'dns.errors.timeout.pct', type: 'metric', unit: 'percent', minWidth: 95 },
    { label: i18n.t('modules.views.npm.dns.s_3e383114') as string, labelKey: 'modules.views.npm.dns.s_3e383114', value: 'dns.errors.error.code.pct', prop: 'dns.errors.error.code.pct', type: 'metric', unit: 'percent', minWidth: 105 },
    { label: i18n.t('modules.views.npm.dns.s_23ebc05c') as string, labelKey: 'modules.views.npm.dns.s_23ebc05c', value: 'dns.errors.nxdomain.pct', prop: 'dns.errors.nxdomain.pct', type: 'metric', unit: 'percent', minWidth: 135 },
    { label: i18n.t('modules.views.npm.dns.s_6d758835') as string, labelKey: 'modules.views.npm.dns.s_6d758835', value: 'dns.errors.servfail.pct', prop: 'dns.errors.servfail.pct', type: 'metric', unit: 'percent', minWidth: 120 },
    { label: i18n.t('modules.views.npm.dns.s_d1df3c31') as string, labelKey: 'modules.views.npm.dns.s_d1df3c31', value: 'dns.errors.other.pct', prop: 'dns.errors.other.pct', type: 'metric', unit: 'percent', minWidth: 130 },
  ];
  private columnsModels: any = {
    'dns.cnt': true,
    'dns.response_time': true,
    'dns.errors.pct': false,
    'dns.errors.timeout.pct': true,
    'dns.errors.error.code.pct': true,
    'dns.errors.nxdomain.pct': true,
    'dns.errors.servfail.pct': true,
    'dns.errors.other.pct': false,
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
    if (this.layoutType === 'detail') {
      this.tableKey = 'TCWs_NPM_Dns_Detail'
    }
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
    const { result, error } = await toAsyncWait(NpmApi.getDnsPerformanceList(params))
    this.isLoading = false;
    if (!error) {
      const data = result.data || []
      const fieldsArr = [
        ['dns.errors.pct', 'dns.errors'],
        ['dns.errors.timeout.pct', 'dns.errors.timeout'],
        ['dns.errors.error.code.pct', 'dns.errors.error.code'],
        ['dns.errors.nxdomain.pct', 'dns.errors.nxdomain'],
        ['dns.errors.servfail.pct', 'dns.errors.servfail'],
        ['dns.errors.other.pct', 'dns.errors.other'],
      ];
      data.forEach((item: any) => {
        const tags = item.tags || {}
        item._client = tags[client] || '-'
        item._server = tags[server] || '-'
        fieldsArr.forEach(([pctKey, cntKey]) => {
          if (item[pctKey] === 0 && item[cntKey] > 0) {
            item[pctKey] = 0.0001
          }
        })
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

  &.table-list-detail {
    margin-top: 16px;
    padding: 0;
  }
}
</style>
