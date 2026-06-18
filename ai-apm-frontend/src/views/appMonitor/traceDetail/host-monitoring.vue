<template>
  <div class="host-monitoring-wrap clear">
    <div
      v-for="(item, index) in chartData"
      :key="index"
      class="chart-item-wrapper"
    >
      <div class="chart-item-title ell">
        {{ item.titleKey ? $t(item.titleKey) : item.title }} 
      </div>

      <div v-loading="item.loading" class="chart-item-body">
        <basic-chart
          :source="item.source"
          :sourceTop="item.toggle === 'single' ? item.sourceTop : 0"
          :showLegend="true"
          :compactGrid="true"
          :textSmallMode="true"
          :yAxisSplitNum="2"
          :showEmpty="!item.loading && !item.source.length"
          :interval="queryParams.interval"
          :tooltipEnterable="false"
          :tooltipConfine="true"
          :animation='false'
        />
      </div>
    </div>
  </div>
</template>

<script lang='ts'>
import { Vue, Component, Watch, Prop } from 'vue-property-decorator'
import { Getter, Mutation, namespace } from 'vuex-class';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import BasicChart from '@/components/charts/basic-chart.vue';
import InfraApi from '@/api/infrastructure';
import MetricApi from '@/api/metric';
import { toAsyncWait } from '@/utils/common';

@Component({
  components: {
    BasicChart,
  }
})
export default class HostMonitoring extends Vue {
  @Prop() private hostName!: string;
  @Prop() private isWindows!: boolean;
  @Getter('globalTime') private globalTimeFunc!: any;
  @Getter('globalTimeInited') private globalTimeInited!: boolean;

  private queryParams: any = {
    hostName: '',
    fromTime: '',
    toTime: '',
    interval: 60,
  }

  private chartData: any[] = [];
  private allChartData: any[] = [
    {
      type: 'cpu',
      title: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, titleKey: 'modules.views.appMonitor.relationMap.s_7054bc34',
      toggle: 'all', // all | single
      metrics: [{
        name: 'system.cpu.usage',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_7054bc34',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'cpu',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_6f59d143') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_6f59d143',
      system: '!windows',
      metrics: [{
        name: 'system.load.1',
        nameCn: 'System Load 1',
      }, {
        name: 'system.load.5',
        nameCn: 'System Load 5',
      }, {
        name: 'system.load.15',
        nameCn: 'System Load 15',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'memory',
      title: i18n.t('modules.views.appMonitor.relationMap.s_31cb8d97') as string, titleKey: 'modules.views.appMonitor.relationMap.s_31cb8d97',
      metrics: [{
        name: 'system.mem.usage',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_31cb8d97') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_31cb8d97',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'memory',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_06dbe8e8') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_06dbe8e8',
      metrics: [{
        name: 'system.swap.pct_free',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_06dbe8e8') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_06dbe8e8',
      }],
      format: (val: number) => 100 - val,
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'networkCard',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_f4055f69') as string, titleKey: 'modules.views.appMonitor.relationMap.s_f4055f69',
      metrics: [{
        name: 'system.net.bytes_sent',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_97ecc1bb') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_97ecc1bb',
      }, {
        name: 'system.net.bytes_rcvd',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_80c86063') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_80c86063',
      }],
      aggregation: 'sum',
      loading: false,
      source: [],
    },
    {
      type: 'networkCard',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_8e844ba5') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_8e844ba5',
      metrics: [{
        name: 'system.net.packets_out.count',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_7ba26ee3') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_7ba26ee3',
      }, {
        name: 'system.net.packets_in.count',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_d212209d') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_d212209d',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'networkCard',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_dfea4fef') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_dfea4fef',
      metrics: [{
        name: 'system.net.packets_out.error',
        names: ['system.net.packets_out.error', 'system.net.packets_out.count'],
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_b85e0a0d') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_b85e0a0d',
      }, {
        name: 'system.net.packets_in.error',
        names: ['system.net.packets_in.error', 'system.net.packets_in.count'],
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_515d4827') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_515d4827',
      }],
      unit: 'percent',
      format: (a: number, b: number) => b !== 0 ? a / b : 0,
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'disk',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_0c039677') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_0c039677',
      toggle: 'all', // all | single
      metrics: [{
        name: 'system.disk.pct_used',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_4e83182b') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_4e83182b',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
      sourceTop: 10,
    },
    {
      type: 'disk',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_618d651a') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_618d651a',
      toggle: 'single', // all | single
      metrics: [{
        name: 'system.disk.pct_used',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_4e83182b') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_4e83182b',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
      sourceTop: 10,
    },
    {
      type: 'disk',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_4665a7a9') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_4665a7a9',
      toggle: 'all', // all | single
      metrics: [{
        name: 'system.io.wkb_s',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_ea22704d') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_ea22704d',
      }, {
        name: 'system.io.rkb_s',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_085cf12e') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_085cf12e',
      }],
      aggregation: 'sum',
      loading: false,
      source: [],
      sourceTop: 10,
    },
    {
      type: 'disk',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_7313dcf8') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_7313dcf8',
      toggle: 'single', // all | single
      metrics: [{
        name: 'system.io.wkb_s',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_ea22704d') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_ea22704d',
      }, {
        name: 'system.io.rkb_s',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_085cf12e') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_085cf12e',
      }],
      aggregation: 'sum',
      loading: false,
      source: [],
      sourceTop: 10,
    },
    {
      type: 'disk',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_2563881c') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_2563881c',
      metrics: [{
        name: 'system.io.w_await',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_dbcd6064') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_dbcd6064',
      }, {
        name: 'system.io.r_await',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_8bafad84') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_8bafad84',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
    },
  ];

  get globalTime () {
    return this.globalTimeFunc()
  }
  @Watch('globalTime', { deep: true })
  private watchGlobalTime() {
    if (!this.globalTimeInited) {
      return
    }
    this.getChartData()
  }

  @Watch('hostName')
  private onHostNameChange (newVal: string) {
    if (!newVal) {
      return
    }
    if (this.isWindows) {
      this.chartData = this.allChartData.filter(t => t.system !== '!windows')
    } else {
      this.chartData = this.allChartData
    }
    this.getChartData()
  }

  private created() {
    this.queryParams.hostName = this.hostName
    if (this.isWindows) {
      this.chartData = this.allChartData.filter(t => t.system !== '!windows')
    } else {
      this.chartData = this.allChartData
    }
    this.getChartData()
  }

  private getChartData () {
    this.regetGlobalTime()
    this.chartData.forEach((item, index) => {
      this.getChartItem(item, index)
    })
  }
  private getChartItem (item: any, index: number) {
    const params: any = {
      tagList: [`host:${this.queryParams.hostName}`],
      startTime: this.queryParams.fromTime,
      endTime: this.queryParams.toTime,
      interval: this.queryParams.interval,
      groups: [],
    }
    if (item.toggle !== 'single') {
      item.metrics.forEach((t: any) => {
        if (!t.names) {
          this.exploreMetricByGroupGraph({
            ...params,
            metric: t.name,
            aggregation: item.aggregation,
          }, index, t)
        } else {
          this.exploreMultMetricGraph({
            ...params,
            aggregation: item.aggregation,
          }, index, t)
        }
      })
    } else if (item.title === i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string) {
      this.exploreMetricByGroupGraph({
        ...params,
        metric: 'system.core.usage',
        groups: ['core'],
        aggregation: 'avg',
      }, index, null)
    } else if (item.title === i18n.t('modules.views.appMonitor.traceDetail.s_618d651a') as string) {
      this.exploreMetricByGroupGraph({
        ...params,
        metric: 'system.disk.in_use',
        groups: ['device_name'],
        aggregation: 'avg',
      }, index, null)
    } else if (item.title === i18n.t('modules.views.appMonitor.traceDetail.s_7313dcf8') as string) {
      item.metrics.forEach((t: any) => {
        this.exploreMetricByGroupGraph({
          ...params,
          metric: t.name,
          groups: ['device_name'],
          aggregation: 'avg',
        }, index, { nameCn: t.nameCn })
      })
    }
  }
  private async exploreMultMetricGraph (params: any, idx: number, metric: any) {
    this.chartData[idx].loading = true
    const { unit, format } = this.chartData[idx]
    const len = metric.names.length
    const resultDataArr: any[] = [] // 多个指标请求结果数组
    for (const t of metric.names) {
      const metricQuery = {
        types: [],
        metric: t,
        by: params.groups,
        aggs: params.aggregation,
        from: params.tagList.map((tag: any, index: number) => {
          const [key, ...values] = tag.split(':')
          return { left: key, operator: '=', right: values.join(':'), connector: 'AND' }
        }),
      }
      const { result, error } = await toAsyncWait(MetricApi.getMetricChart({
        query: { A: metricQuery, expr: 'A' },
        start: Math.floor(+new Date(params.startTime) / 1000),
        end: Math.floor(+new Date(params.endTime) / 1000),
        interval: params.interval,
      }));
      resultDataArr.push(!error ? (result?.data || [])[0] || {} : {})
    }
    const data: any = {} // 数据聚合，全量keys
    resultDataArr.forEach((t, i) => {
      const values = t.values || []
      values.forEach(([key, value]: any) => {
        if (!data[key]) {
          data[key] = Array(len).fill(0)
        }
        data[key][i] = value
      })
    })

    const metrics: string[] = this.chartData[idx].metrics.map((t: any) => t.nameCn)
    this.chartData[idx].source = [
      ...this.chartData[idx].source,
      {
        name: metric.nameCn,
        unit,
        area: metrics.length <= 2,
        smooth: true,
        data: Object.entries(data).map(([key, value]: any) => ({
          key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
          value: value.every((t: any) => typeof t === 'number') ? format(...value) : '-',
        }))
      },
    ].sort((a, b) => {
      return metrics.findIndex(t => t === a.name) - metrics.findIndex(t => t === b.name)
    })
    this.chartData[idx].loading = false
  }
  private async exploreMetricByGroupGraph (params: any, idx: number, metric: any) {
    this.chartData[idx].loading = true
    const metricQuery = {
      types: [],
      metric: params.metric,
      by: params.groups,
      aggs: params.aggregation,
      from: params.tagList.map((tag: any, index: number) => {
        const [key, ...values] = tag.split(':')
        return { left: key, operator: '=', right: values.join(':'), connector: 'AND' }
      }),
    }
    const { result, error } = await toAsyncWait(MetricApi.getMetricChart({
      query: { A: metricQuery, expr: 'A' },
      start: Math.floor(+new Date(params.startTime) / 1000),
      end: Math.floor(+new Date(params.endTime) / 1000),
      interval: params.interval,
    }));
    if (!error) {
      const metrics: string[] = this.chartData[idx].metrics.map((t: any) => t.nameCn)
      const data = result.data || []
      const { format } = this.chartData[idx]
      const source = [
        ...this.chartData[idx].source,
        ...data.map((item: any) => {
          const tag = Object.entries(item.tags || {}).map(([k, v]) => `${k}:${v || ''}`).join(';')
          return {
            name: !params.groups.length ? metric?.nameCn : tag + (metric ? ` ${metric.nameCn}` : ''),
            unit: (item.units || [])[1] || '',
            area: !params.groups.length && metrics.length <= 2,
            smooth: true,
            data: (item.values || []).map(([key, value]: any) => ({
              key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
              value: typeof value === 'number' && format ? format(value) : value,
            }))
          }
        })
      ]
      if (!params.groups.length) {
        this.chartData[idx].source = source.sort((a, b) => {
          return metrics.findIndex(t => t === a.name) - metrics.findIndex(t => t === b.name)
        })
      } else {
        this.chartData[idx].source = source
      }
    }
    this.chartData[idx].loading = false
  }

  private regetGlobalTime () {
    const { fromTime, toTime, interval = 300 } = this.globalTimeFunc()
    this.queryParams.fromTime = dayjs(fromTime).format('YYYY-MM-DD HH:mm:ss')
    this.queryParams.toTime = dayjs(toTime).format('YYYY-MM-DD HH:mm:ss')
    this.queryParams.interval = interval
  }
}
</script>

<style lang='scss' scoped>
.host-monitoring-wrap {
  padding-top: 6px;
  .chart-item-wrapper {
    height: 160px;
    position: relative;
    & + .chart-item-wrapper {
      margin-top: 16px;
    }

    .chart-item-title {
      height: 34px;
      padding: 12px 0 6px 0;
      line-height: 16px;
      font-size: 13px;
      color: var(--color-text-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      position: relative;

      .chart-item-tool-btn {
        height: 20px;
        width: 20px;
        text-align: center;
        line-height: 20px;
        position: absolute;
        top: 10px;
        right: 5px;
      }
    }
    .chart-item-body {
      height: calc( 100% - 34px );
      position: relative;
    }
    :deep(.el-popover__reference-wrapper) {
      display: block;
      line-height: 0;
    }
    :deep(.el-loading-mask) {
      left: 8px;
      right: 8px;
    }
  }
}
</style>
