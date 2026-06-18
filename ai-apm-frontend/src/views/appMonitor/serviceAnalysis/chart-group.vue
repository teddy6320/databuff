<template>
  <div class="chart-group-wrap">
    <div class="chart-tools flex-h-jc">
      <db-radio v-model='currType' :options='getOptions' @change="typeChangeHandle"></db-radio>
    </div>

    <div v-loading="currChartData.loading" class="chart-cont">
      
      <basic-chart
        :source="currChartData.source"
        :colors="currChartData.colors"
        :showEmpty="!currChartData.loading && !currChartData.source.length"
        :showLegend="true"
        :textSmallMode="true"
        :yAxisSplitNum="3"
        :minInterval="1"
        :animation='firstAnimate'
        :tsSource='currChartData.tsSource'
        @on-ts-tooltip-show='onTsTooltipShow'>
        <template slot='ts'>
          <ChartTsSlot :current='currentTsItem' />
        </template>
      </basic-chart>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import i18n from '@/i18n';
import ChartTsSlot from './chart-ts-slot.vue';
import dayjs from 'dayjs';
import BasicChart from '@/components/charts/basic-chart.vue';
import { toAsyncWait } from '@/utils/common'
import ApmApi from '@/api/apm';
// import Mock from './mock'

type ChartType = 'response' | 'error' | 'request'

@Component({
  components: {
    BasicChart,
    ChartTsSlot,
  }
})
export default class ChartGroup extends Vue {
  @Prop({ default: () => ({}) }) private query!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;
  @Prop({ default: '' }) private componentType!: string;

  private currType: ChartType = 'response';
  private chartTypes = [
    { label: i18n.t('modules.views.appMonitor.relationMap.s_207c26c9') as string, labelKey: 'modules.views.appMonitor.relationMap.s_207c26c9', value: 'response' },
    { label: i18n.t('modules.views.appMonitor.cache.s_0c8524d7') as string, labelKey: 'modules.views.appMonitor.cache.s_0c8524d7', value: 'error' },
    { label: i18n.t('modules.views.appMonitor.cache.s_8bc42b53') as string, labelKey: 'modules.views.appMonitor.cache.s_8bc42b53', value: 'request' },
  ]
  get getOptions () {
    return this.chartTypes;
  }

  get queryParams () {
    const params: any = {}
    Object.entries(this.query).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '' && key !== 'isIn') {
        if (key === 'sid') {
          params.serviceId = value
        } else if (key === 'si') {
          params.serviceInstance = value
        } else if (key === 'srcSid') {
          params.srcServiceId = value
        } else {
          params[key] = value
        }
      }
    })
    return params
  }

  private chartData = {
    response: {
      metrics: ['avgLatencys', 'callCnts'],
      colors: ['#2962FF', '#00AFF4'],
      loading: false,
      loaded: false,
      source: [],
      tsSource: [],
    },
    error: {
      metrics: ['errorRates'],
      loading: false,
      loaded: false,
      source: [],
      tsSource: [],
    },
    request: {
      metrics: ['callCnts', 'errorCnts'],
      colors: ['#5273E0', '#F37370'],
      loading: false,
      loaded: false,
      source: [],
      tsSource: [],
    },
  }
  get currChartData () {
    return this.chartData[this.currType]
  }

  private initChartData () {
    Object.values(this.chartData).forEach((item) => {
      item.loading = false
      item.loaded = false
      item.source = []
    })
  }

  private firstAnimate = true
  public isMounted = false;
  public tsSource: any[] = [];
  
  private currentTsItem: any = null;

  private mounted () {
    this.isMounted = true;
  }

  private typeChangeHandle () {
    this.getServiceTrendChart(this.currType)
  }

  public getData () {
    this.initChartData()
    this.getServiceTrendChart(this.currType)
    this.firstAnimate = false
  }

  // 获取图表数据
  private async getServiceTrendChart (type: ChartType) {
    const chartDataItem = this.chartData[type]
    if (chartDataItem.loading || chartDataItem.loaded) {
      return
    }
    const params = {
      ...this.timeParams,
      ...this.queryParams,
      isIn: 1,
      componentType: this.componentType,
      graphStats: chartDataItem.metrics,
    }
    chartDataItem.loading = true
    const { result, error } = await toAsyncWait(ApmApi.getServiceGraph(params))
    if (params.componentType !== this.componentType) {
      // 切换componentType后，取消处理逻辑
      return;
    }
    chartDataItem.loading = false
    chartDataItem.loaded = true
    if (!error) {
      const data = result.data || {}
      // const data: any = Mock?.data || {};
      let keys = chartDataItem.metrics.map((metric) => Object.keys(data[metric] || {}).map(t => Number(t))).flat();
      keys = [...new Set(keys)].sort((a, b) => a - b);
      if (!keys.length) {
        chartDataItem.source = []
      } else {

        let _rootDetails: any[] = [];
        if (type === 'response') {
          chartDataItem.source = this.formatResponseData(data, keys);
          if (Array.isArray(data.details?.avgLatencys) || Array.isArray(data.details?.callCnts)) {
            _rootDetails = data.details?.avgLatencys?.length ? data.details?.avgLatencys : data.details?.callCnts?.length ? data.details?.callCnts : [];
          }
        } else if (type === 'error') {
          chartDataItem.source = this.formatErrorData(data, keys);
          if (Array.isArray(data.details?.errorRates)) {
            _rootDetails = data.details.errorRates || [];
          }
        } else if (type === 'request') {
          chartDataItem.source = this.formatRequestData(data, keys);
          if (Array.isArray(data.details?.errorCnts) || Array.isArray(data.details?.callCnts)) {
            _rootDetails = data.details?.errorCnts?.length ? data.details?.errorCnts : data.details?.callCnts?.length ? data.details?.callCnts : [];
          }
        }
        // v2.9.1 ++
        if (Array.isArray(_rootDetails)) {
          const tsSource = _rootDetails.filter((i: any) => i.abnormalStartTime && i.abnormalEndTime).map((i: any) => {
            const { abnormalStartTime, abnormalEndTime } = i;
            return {
              duration: [
                Number(abnormalStartTime),
                Number(abnormalEndTime)
              ],
              status: 'danger',
              info: {
                ...i
              }
            }
          });
          if (chartDataItem?.tsSource) {
            // @ts-ignore
            chartDataItem.tsSource = tsSource;
          }
        } else {
          chartDataItem.tsSource = []
        }
      }
    } else {
      chartDataItem.source = []
    }
  }
  // 响应时间
  private formatResponseData (data: any, keys: number[]): any {
    const avgLatencys = data.avgLatencys || {}
    const callCnts = data.callCnts || {}
    return [{
      name: i18n.t('modules.views.appMonitor.relationMap.s_207c26c9') as string, nameKey: 'modules.views.appMonitor.relationMap.s_207c26c9',
      type: 'line',
      area: true,
      unit: 'ns',
      data: keys.map((key) => ({
        key: dayjs(key).format('YYYY-MM-DD HH:mm'),
        value: avgLatencys[key],
      })),
    }, {
      name: i18n.t('modules.views.appMonitor.cache.s_8bc42b53') as string, nameKey: 'modules.views.appMonitor.cache.s_8bc42b53',
      type: 'bar',
      data: keys.map((key) => ({
        key: dayjs(key).format('YYYY-MM-DD HH:mm'),
        value: callCnts[key],
      })),
    }];
  }
  // 错误率
  private formatErrorData (data: any, keys: number[]): any {
    const errorRates = data.errorRates || {}
    return [{
      name: i18n.t('modules.views.appMonitor.cache.s_0c8524d7') as string, nameKey: 'modules.views.appMonitor.cache.s_0c8524d7',
      unit: '%',
      data: keys.map((key) => ({
        key: dayjs(key).format('YYYY-MM-DD HH:mm'),
        value: errorRates[key],
      })),
    }];
  }
  // 请求数
  private formatRequestData (data: any, keys: number[]): any {
    const callCnts = data.callCnts || {}
    const errorCnts = data.errorCnts || {}
    return [{
      name: i18n.t('modules.views.appMonitor.resourceDetail.s_7c8b41ec') as string, nameKey: 'modules.views.appMonitor.resourceDetail.s_7c8b41ec',
      type: 'bar',
      stack: 'total',
      data: keys.map((key) => {
        const err = typeof errorCnts[key] === 'number' ? errorCnts[key] : 0
        return {
          key: dayjs(key).format('YYYY-MM-DD HH:mm'),
          value: typeof callCnts[key] === 'number' ? callCnts[key] - err : '-',
        }
      }),
    }, {
      name: i18n.t('modules.views.appMonitor.resourceDetail.s_8592da3e') as string, nameKey: 'modules.views.appMonitor.resourceDetail.s_8592da3e',
      type: 'bar',
      stack: 'total',
      data: keys.map((key) => ({
        key: dayjs(key).format('YYYY-MM-DD HH:mm'),
        value: errorCnts[key],
      })),
    }];
  }

  private onTsTooltipShow (row: any) {
    this.currentTsItem = row.info || null;
  }

}
</script>

<style lang="scss" scoped>
.chart-group-wrap {
  padding: 0 0 20px;
  background-color: var(--bg-color);
  border-bottom: 1px solid var(--border-color-lighter);

  .chart-tools {
    margin-bottom: 10px;
    align-items: flex-start;
  }

  .chart-cont {
    height: 240px;
  }
}

</style>