<template>
  <div class="table-list"
    v-loading="queryParams.pageNum === 1 && isLoading">
    <tag-input @on-change="tagChangeHandle" class="query-tag-input" />

    <div class="list-head flex-h-jc mt-10">
      <span class="list-total describe"></span>
      <!-- 发现 {{ listTotal }} 条数据 -->

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
        @sort-change="sortChangeHandle"
        :default-sort="{ prop: 'time', order: 'descending' }"
        highlight-current-row size="small"
        :height="tableHeight"
        :empty-text="!isLoading ? $t('modules.views.appMonitor.errorDetail.s_21efd88b') : ' '"
        class="table"
      >
        <el-table-column :label="$t('modules.views.alarmCenter.eventDetail.s_19fcb9eb')" prop="time" sortable="custom" :sort-orders="['descending', 'ascending', null]" min-width="150" show-overflow-tooltip>
          <template slot-scope="{ row }">{{ row._time || '-' }}</template>
        </el-table-column>

        <el-table-column
          v-for="col in getColumns.filter(col => !col.group)"
          :key="col.value"
          :label="col.label"
          :prop="col.prop"
          :sortable="col.prop ? 'custom' : false"
          :sort-orders="['descending', 'ascending', null]"
          :min-width="col.minWidth"
          show-overflow-tooltip>
          <template slot-scope="{ row }">
            <template v-if="col.type === 'metric'">{{ row[col.value] | valueFilter(col.unit) }}</template>
            <template v-else>{{ row[col.value] || '-' }}</template>
          </template>
        </el-table-column>

        <!-- <div v-if="queryParams.pageNum > 1" slot="append" class="table-load-tips">
          <template v-if="!noMore">
            <i class="el-icon-loading"></i> {{ $t('modules.views.appMonitor.errorDetail.s_f09b1233') }}
          </template>
          <template v-else>{{ $t('modules.views.appMonitor.errorDetail.s_37c38521') }}</template>
        </div> -->
      </el-table>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Getter } from 'vuex-class';
import { Table } from 'element-ui'
import TagInput from '../tag-input.vue';
import dayjs from 'dayjs';
import { orderBy } from 'lodash';
import humanFormat from 'human-format';
import getUnitData from '@/utils/getUnitData';
import { debounce } from '@/utils/common';
import { toAsyncWait } from '@/utils/common';
import NpmApi from '@/api/npm';

@Component({
  components: {
    TagInput,
  },
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
export default class Traffic extends Vue {
  @Getter('globalTimeInited') private globalTimeInited!: boolean;
  @Prop({ default: '' }) private timeParamsStr!: string;
  @Prop({ default: () => [] }) private tagList!: string[];

  public $refs!: {
    tableWrap: HTMLDivElement,
    table: Table,
  }

  get queryFrom () {
    const from = this.tagList.map((tag, index) => {
      const [key, ...values] = tag.split(':')
      return { left: key, operator: '=', right: values.join(':'), connector: 'AND' }
    })
    const direction: any = { left: 'islocalportephemeral', operator: '=', right: 'ephemeralTrue', connector: 'AND' }
    return [...from, direction]
  }

  @Watch('timeParamsStr')
  private onTimeParamsChange (newVal: any, oldVal: any) {
    if (newVal !== oldVal) {
      const { fromTime, toTime } = JSON.parse(this.timeParamsStr || '{}')
      this.queryParams.fromTime = fromTime
      this.queryParams.toTime = toTime
      this.getAllList()
    }
  }

  private timer: any = null;
  private scrollContainer: any = null;
  private scrollHandle: any = null;
  private tableHeight: number = 360;

  private queryParams: any = {
    from: [],
    fromTime: '',
    toTime: '',
    pageNum: 1,
    pageSize: 50,
    sortField: 'time',
    sortOrder: 'desc',
  }

  private tableKey = 'TCWs_NPM_Analysis_Traffic' // 唯一标识，用于存储 localStorage
  private isLoading = false
  private allList: any = []
  private listTotal = 0
  private tableList: any[] = []
  get noMore () {
    return this.tableList.length >= this.listTotal
  }

  private columnsFullLabels = [
    { label: i18n.t('modules.views.npm.analysis.s_c6606424') as string, labelKey: 'modules.views.npm.analysis.s_c6606424', value: '_laddr_ip_port', minWidth: 150 },
    { label: i18n.t('modules.views.appMonitor.resourceDetail.s_dfac0132') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_dfac0132', value: '_raddr_ip_port', minWidth: 150 },
    // { label: 'Pre-NAT IP', value: 'preNatIp', minWidth: 150 },
    { label: i18n.t('modules.views.npm.analysis.s_997befb2') as string, labelKey: 'modules.views.npm.analysis.s_997befb2', value: 'dns', minWidth: 150 },
    { label: i18n.t('modules.views.npm.analysis.s_3582f399') as string, labelKey: 'modules.views.npm.analysis.s_3582f399', value: 'laddr_hostname', minWidth: 150 },
    // { label: i18n.t('modules.views.npm.analysis.s_d6ef5b2d') as string, labelKey: 'modules.views.npm.analysis.s_d6ef5b2d', value: 'clientTag', minWidth: 150 },
    // { label: i18n.t('modules.views.npm.analysis.s_f4bd4f2a') as string, labelKey: 'modules.views.npm.analysis.s_f4bd4f2a', value: 'serverTag', minWidth: 150 },
    { label: i18n.t('modules.views.npm.analysis.s_120d479d') as string, labelKey: 'modules.views.npm.analysis.s_120d479d', value: 'ip_type', minWidth: 140 },
    { label: 'PID', value: 'pid', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.serviceDetail.s_90d6afb7') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_90d6afb7', value: 'npm.volume_sent', prop: 'npm.volume_sent', type: 'metric', unit: 'bytes',  minWidth: 100 },
    { label: i18n.t('modules.views.npm.analysis.s_6809f17b') as string, labelKey: 'modules.views.npm.analysis.s_6809f17b', value: 'npm.throughput_sent', prop: 'npm.throughput_sent', type: 'metric', unit: 'bytes/s', minWidth: 150 },
    { label: i18n.t('modules.views.appMonitor.serviceDetail.s_3e54c81c') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_3e54c81c', value: 'npm.volume_rcvd', prop: 'npm.volume_rcvd', type: 'metric', unit: 'bytes', minWidth: 100 },
    { label: i18n.t('modules.views.npm.analysis.s_0c701c5d') as string, labelKey: 'modules.views.npm.analysis.s_0c701c5d', value: 'npm.throughput_rcvd', prop: 'npm.throughput_rcvd', type: 'metric', unit: 'bytes/s', minWidth: 150 },
    { label: i18n.t('modules.views.npm.analysis.s_db732ecb') as string, labelKey: 'modules.views.npm.analysis.s_db732ecb', value: 'npm.tcp_latency', prop: 'npm.tcp_latency', type: 'metric', unit: 'µs', minWidth: 100 },
    { label: 'RTT', value: 'npm.rtt', prop: 'npm.rtt', type: 'metric', unit: 'µs', minWidth: 100 },
    { label: i18n.t('modules.views.npm.analysis.s_3b3b5967') as string, labelKey: 'modules.views.npm.analysis.s_3b3b5967', value: 'npm.tcp_jitter', prop: 'npm.tcp_jitter', type: 'metric', unit: 'µs', minWidth: 100 },
    { label: i18n.t('modules.views.appMonitor.serviceDetail.s_5b545e42') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_5b545e42', value: 'npm.tcp_retransmit', prop: 'npm.tcp_retransmit', type: 'metric', unit: 'requests', minWidth: 130 },
    { label: i18n.t('modules.views.npm.analysis.s_8d2f3486') as string, labelKey: 'modules.views.npm.analysis.s_8d2f3486', value: 'npm.tcp.conns_established.rate', prop: 'npm.tcp.conns_established.rate', type: 'metric', unit: 'conns/s', minWidth: 150 },
  ];
  private columnsModels: any = {
    '_laddr_ip_port': true,
    '_raddr_ip_port': true,
    'preNatIp': false,
    'dns': true,
    'laddr_hostname': true,
    'clientTag': true,
    'serverTag': true,
    'ip_type': true,
    'pid': true,
    'npm.volume_sent': true,
    'npm.throughput_sent': true,
    'npm.volume_rcvd': true,
    'npm.throughput_rcvd': true,
    'npm.tcp_latency': true,
    'npm.rtt': true,
    'npm.tcp_jitter': true,
    'npm.tcp_retransmit': true,
    'npm.tcp.conns_established.rate': true,
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

    const { fromTime, toTime } = JSON.parse(this.timeParamsStr || '{}')
    this.queryParams.fromTime = fromTime
    this.queryParams.toTime = toTime
    this.getAllList()
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

  // 获取列表数据
  private async getAllList () {
    // if (this.isLoading) {
    //   return;
    // }
    const { fromTime, toTime } = JSON.parse(this.timeParamsStr || '{}')
    const { sortOrder, sortField } = this.queryParams
    const params: any = {
      start: Math.floor(+new Date(fromTime)),
      end: Math.floor(+new Date(toTime)),
      metrics: [
        'npm.volume_sent',
        'npm.throughput_sent',
        'npm.volume_rcvd',
        'npm.throughput_rcvd',
        'npm.tcp_latency',
        'npm.rtt',
        'npm.tcp_jitter',
        'npm.tcp_retransmit',
        'npm.tcp.conns_established.rate',
      ],
      from: [{
        left: [...this.queryFrom],
        connector: 'AND',
        right: [...this.queryParams.from],
      }],
      order: {
        code: sortField || 'time',
        func: sortOrder === 'asc' ? 'BOTTOM' : 'TOP',
        limit: 500,
      }
    }
    this.isLoading = true;
    const { result, error } = await toAsyncWait(NpmApi.getPerformanceVolumeList(params))
    this.isLoading = false;
    if (!error) {
      const resData = result.data || {}
      const columns: string[] = resData.columns || []
      const data: any[] = (resData.values || []).map((value: any) => {
        const item: any = {}
        columns.forEach((key: any, index) => {
          item[key] = value[index]
        })
        return item
      })
      data.forEach(item => {
        item._time = dayjs(item.time).format('YYYY-MM-DD HH:mm:ss')
        item._laddr_ip_port = `${item.laddr_ip || '-'}:${item.laddr_port || '*'}`
        item._raddr_ip_port = `${item.raddr_ip || '-'}:${item.raddr_port || '*'}`
      })
      if (sortOrder) {
        this.allList = orderBy(data, [sortField || 'time'], [sortOrder]);
      } else {
        this.allList = data
      }
      this.listTotal = data.length
    } else {
      this.allList = []
      this.listTotal = 0
    }
    this.getTableList()
  }

  // 分页
  private getTableList(page = 1) {
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

  private tagChangeHandle (from: any[]) {
    if (JSON.stringify(from) === JSON.stringify(this.queryParams.from)) {
      return
    }
    this.queryParams.from = from
    this.getAllList()
  }
  // 表格排序
  private sortChangeHandle (data: any) {
    const { prop, order } = data
    this.queryParams.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
    this.queryParams.sortField = order ? prop : ''
    this.getAllList()
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
  height: 100%;

  .list-head {
    margin-bottom: 10px;
    font-size: 13px;
    line-height: 22px;
  }
  .list-body {
    height: calc(100% - 116px);
    .table {
      height: 100%;
    }
  }
}
</style>
