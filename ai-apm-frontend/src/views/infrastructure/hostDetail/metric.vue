<template>
  <div class="detail-metric" v-loading="isLoading">
    <div class="flex-h mb-10">
      <el-select
        v-model="chartParams.type"
        @change="typeChangeHandle"
        size="small"
        :placeholder="$t('modules.views.infrastructure.hostDetail.s_e7aa7158')"
        class="app-select mr-10 flex-0">
        <el-option v-for="t in types" :key="t" :label="t" :value="t"></el-option>
      </el-select>

      <el-input
        v-model="chartParams.query"
        @change="getChartList()"
        :disabled="isLoading"
        clearable size="small"
        maxlength="100"
        prefix-icon="db-icon-search"
        :placeholder="$t('modules.views.infrastructure.hostDetail.s_5a420b22')"
        class="flex-1" />
    </div>

    <div v-if="isLoading || subTypeList.length" class="mb-10">
      <db-radio
        v-model="chartParams.subType"
        :options="subTypeList"
        @change="subTypeChangeHandle(chartParams.type, chartParams.subType)" />
    </div>

    <div ref="chartCont" class="detail-metric-cont">
      <template v-for="(metric, index) in chartList">
        <div
          v-if="chartSource[metric]"
          :key="`${metric}__${index}`"
          v-loading="chartSource[metric].loading"
          class="section-item">
          <div class="section-title">
            <el-popover
              placement="left-start"
              width="400"
              trigger="hover"
              popper-class="metric-info-popper">
              <div slot="reference" class="tit">
                <div class="name">{{ metric }}</div>
                <div class="desc">{{ $t('modules.views.appMonitor.dbConnPool.s_5eaa60a4', { value0: metricInfoMapping[metric].desc || '-' }) }}</div>
              </div>
              <metric-info-tooltip
                :detail="metricInfoMapping[metric]"
                :tooltip="false" />
            </el-popover>
          </div>

          <div class="section-cont">
            <basic-chart
              :source="chartSource[metric].source"
              :showEmpty="!chartSource[metric].loading && !chartSource[metric].source.length" />
          </div>
        </div>
      </template>

      <div v-show="!isLoading && !chartList.length" class="empty-show">{{ $t('modules.components.charts.s_21efd88b') }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import { orderBy } from 'lodash';
import BasicChart from '@/components/charts/basic-chart.vue';
import MetricInfoTooltip from '@/components/metric-info-tooltip.vue';
import { debounce } from '@/utils/common';
import { toAsyncWait } from '@/utils/common';
import MetricApi, { formatMetricInfos } from '@/api/metric';

// 忽略的指标
const IgnoreSystemMetrics = [
  'system.load.1',
  'system.load.5',
  'system.load.15',
  'system.io.avg_rq_sz',
  'system.io.await',
  'system.io.bytes_per_s',
  'system.io.svctm',
  'system.io.rrqm_s',
  'system.io.wrqm_s',
  'system.net.conntrack.buckets',
  'system.net.conntrack.count',
  'system.net.conntrack.early_drop',
  'system.net.conntrack.drop',
  'system.net.conntrack.error',
  'system.net.conntrack.ignore',
  'system.net.conntrack.insert',
  'system.net.conntrack.insert_failed',
  'system.net.conntrack.invalid',
  'system.net.conntrack.log_invalid',
  'system.net.conntrack.max',
  'system.net.conntrack.search_restart',
];
// 聚合方式为sum的指标
const SumMetrics = [
  'system.net.bytes_sent',
  'system.net.bytes_rcvd',
];

@Component({
  components: {
    BasicChart,
    MetricInfoTooltip,
  },
})
export default class DetailMetric extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: () => ({}) }) private detail!: any;

  public $refs!: {
    chartCont: HTMLDivElement,
  }

  private timer: any = null;
  private scrollContainer: any = null;
  private scrollHandle: any = null;

  private chartParams: any = {
    type: '', // 应用
    subType: '', // 应用下的分类
    query: '',
    pageNum: 1,
    pageSize: 10,
  }

  get types () {
    const apps: string[] = this.detail?.apps || []
    return ['system', ...apps.filter((a: string) => a !== 'system')]
  }

  get subTypeList () {
    const { type = '' } = this.chartParams
    return (this.typeMap[type] || []).map((t: string) => ({
      label: t,
      value: t,
    }))
  }

  private isLoading: boolean = false
  private typeMap: any = {} // 应用对用的子分类map
  private typeMetrics: any = {}
  private metricInfoMapping: any = {}

  private chartList: any[] = [] // 已加载的图表
  private chartTotal = 0
  private chartSource: any = {}

  get noMore () {
    return this.chartList.length >= this.chartTotal
  }

  private beforeDestroy () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    if (this.scrollContainer) {
      this.scrollContainer.removeEventListener('scroll', this.scrollHandle)
    }
  }

  public getData () {
    const { app = '' } = this.$route.query;
    const _app = decodeURIComponent(app as string);
    const _type = this.types.includes(_app) ? _app : this.types[0]
    this.chartParams.type = _type
    this.typeChangeHandle()
  }

  // 获取应用下的指标
  private async getHostMetrics (type?: string) {
    type = type || this.types[0]
    this.$set(this.typeMap, type, []);
    this.isLoading = true;
    const { result, error } = await toAsyncWait(MetricApi.getAllMetricListByQuery({
      app: type,
      host: this.queryParams.hostName,
    }));
    this.isLoading = false;
    if (!error) {
      const data: any = {}
      const typeMetrics: any = {}
      const subTypes: string[] = []
      Object.values(result.data || {}).filter((item: any) => {
        if (type === 'system') {
          const isSystem = item.type1 === i18n.t('modules.views.appMonitor.traceDetail.s_1801d7de') as string && item.type2 === i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string && item.type3
          const noIgnore = !IgnoreSystemMetrics.find(t => t === item.identifier)
          return isSystem && noIgnore
        }
        return item.type3
      }).forEach((item: any) => {
        data[item.identifier] = item
        const _type = `${type}::::::${item.type3}`
        if (!typeMetrics[_type]) {
          typeMetrics[_type] = []
          subTypes.push(item.type3)
        }
        typeMetrics[_type] = orderBy([...typeMetrics[_type], item.identifier], [t => t.toLocaleLowerCase()], ['asc']);
      })
      this.metricInfoMapping = { ...this.metricInfoMapping, ...data }
      this.typeMap[type] = orderBy(subTypes, [t => t.toLocaleLowerCase()], ['asc'])
      this.typeMetrics = { ...this.typeMetrics, ...typeMetrics }
      this.$store.commit('Common/SET_METRIC_INFOS', formatMetricInfos(result.data || {}));
    }
  }

  // 切换应用
  private async typeChangeHandle () {
    const type = this.chartParams.type
    this.chartParams.query = ''
    this.$router.replace({
      query: {
        ...this.$route.query,
        app: type,
      }
    })
    if (!this.typeMap[type] || !this.typeMap[type].length) {
      await this.getHostMetrics(type)
    }
    this.subTypeChangeHandle(type, this.typeMap[type][0] || '')
  }

  // 切换应用下分类
  private subTypeChangeHandle (type: string, subType: string) {
    this.chartParams.subType = subType
    this.chartParams.pageNum = 1
    this.chartList = []
    this.chartTotal = 0
    this.chartSource = {}
    const _type = `${type}::::::${subType}`
    if (this.typeMetrics[_type] && this.typeMetrics[_type].length) {
      this.getChartList()
    }
  }

  private async getChartList (page = 1) {
    const { type, subType, query, pageSize } = this.chartParams
    if (page !== 1 && this.noMore) {
      return;
    }
    if (page === 1 && this.scrollContainer) {
      // 滚动区域 scrollTop 置为 0
      this.scrollContainer.scrollTop = 0
    }
    const q = query.toLocaleLowerCase()
    const _type = `${type}::::::${subType}`
    const metrics = (this.typeMetrics[_type] || []).filter((m: string) => {
      const info = this.metricInfoMapping[m] || {}
      const inName = m.toLocaleLowerCase().includes(q)
      const inMetricCn = (info.metricCn || '').toLocaleLowerCase().includes(q)
      // const inDesc = (info.desc || '').toLocaleLowerCase().includes(q)
      return inName || inMetricCn;
    })
    const list = metrics.slice((page - 1) * pageSize, page * pageSize)
    this.chartList = page === 1 ? list : Array.from(this.chartList).concat(list)
    this.chartTotal = metrics.length
    this.chartParams.pageNum = page

    list.forEach((metric: string) => {
      this.$set(this.chartSource, metric, {
        title: metric,
        source: [],
        active: 'avg',
        loading: false,
        empty: false,
      });
      this.getChartSource(metric)
    });

    this.$nextTick(() => {
      if (!this.scrollContainer) {
        this.loop();
      }
    })
  }
  private async getChartSource (metric: string) {
    const chartDataItem = this.chartSource[metric]
    const { hostName, fromTime, toTime, interval } = this.queryParams
    const params = {
      query: { A: {
        metric,
        aggs: SumMetrics.includes(metric) ? 'sum' : 'avg',
        from: [{ left: 'host', operator: '=', right: hostName, connector: 'AND' }],
        by: [],
        types: [],
      }, expr: 'A' },
      start: Math.floor(+new Date(fromTime) / 1000),
      end: Math.floor(+new Date(toTime) / 1000),
      interval,
    }
    chartDataItem.loading = true
    const { result, error } = await toAsyncWait(MetricApi.getMetricChart(params));
    chartDataItem.loading = false
    if (!error) {
      const data = (result?.data || [])[0] || {}
      const values: any[] = data.values || []
      if (values.length) {
        chartDataItem.source = [{
          name: metric,
          unit: (data.units || [])[1],
          smooth: true,
          data: values.map(([key, ...value]: any) => ({
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value: value[0],
          })),
        }]
      } else {
        chartDataItem.source = []
        chartDataItem.empty = true
      }
    } else {
      chartDataItem.source = []
      chartDataItem.empty = true
    }
  }

  // 滚动加载相关
  private loop () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    this.timer = setTimeout(() => {
      const scrollContainer = this.$refs.chartCont;
      if (!scrollContainer) {
        this.loop();
      } else {
        this.scrollContainer = scrollContainer;
        // 滚动到底加载更多
        this.scrollHandle = debounce(() => {
          const { scrollHeight, scrollTop, clientHeight } = scrollContainer
          if (!this.noMore && !this.isLoading && scrollHeight - clientHeight - scrollTop < 50) {
            this.getChartList(this.chartParams.pageNum + 1)
          }
        }, 17)
        scrollContainer.addEventListener('scroll', this.scrollHandle)
      }
    }, 100)
  }
}
</script>

<style lang="scss" scoped>
.detail-metric {
  height: 100%;
  overflow: hidden;
  position: relative;

  .app-select {
    width: 216px;
  }

  .detail-metric-cont {
    position: relative;
    height: calc(100% - 84px);
    overflow: auto;
  }

  .empty-show {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--color-text-regular);
    font-size: 14px;
    position: absolute;
    top: 0;
    left: 0;
  }
}

.section-item {
  margin-bottom: 16px;
  width: calc(50% - 8px);
  height: 238px;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  display: inline-block;
  vertical-align: top;
  color: var(--color-text-primary);

  &:nth-child(2n) {
    margin-left: 16px;
  }

  &:nth-last-child(1),
  &:nth-last-child(2) {
    margin-bottom: 0;
  }

  .section-title {
    height: 54px;
    padding: 12px 20px 0;
    font-size: 14px;
    line-height: 22px;
    position: relative;
    > * {
      vertical-align: top;
    }
    > span {
      display: inline-block;
      max-width: 100%;
    }
    :deep(.el-popover__reference-wrapper),
    .tit {
      display: block;
    }
    .desc {
      margin-top: 5px;
      font-size: 12px;
      line-height: 14px;
      color: var(--color-text-secondary);
    }
  }

  .section-cont {
    height: calc(100% - 54px);
    padding: 0 10px;
  }
}
</style>
