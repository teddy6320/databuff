<template>
  <div class="baseinfo-cont">
    <div class="baseinfo-wrapper">
      
      <div class="chart-group">
        <div v-for='value,key in chartGroup' :key='key' class="chart-item chart-item-33 br-4">
          <h3 class="fw-normal font-14 chart-title">
            <div class="name mb-8 ell">{{ value.titleKey ? $t(value.titleKey) : value.title }}</div>
            <div class="describe ell">{{ value.desc }}</div>
          </h3>
          <basic-chart
            :showEmpty="!chartGroup[key].loading && !chartGroup[key].source.length"
            :key='key'
            :colors='chartGroup[key].colors'
            :showLegend='true'
            :compactGrid="true"
            :textSmallMode="true"
            :minInterval="1"
            :min="0"
            group='topGroup'
            :yAxisSplitNum="3"
            :interval="timeParams.interval"
            :source='chartGroup[key].source'
            :tsSource='chartGroup[key].tsSource'
            @on-ts-tooltip-show='onTsTooltipShow'
          >
            <template slot='ts'>
              <ChartTsSlot :current='currentTsItem' />
            </template>
          </basic-chart>
        </div>
      </div>

      <div class="flex-h-jc mt-20">
        <db-radio v-model='typeModel' :options='typeOptions' @change='changeTypeHandle'></db-radio>
      </div>
      <div class="chart-group">
        <div v-for='value,key in chartGroupBottom' :key='key' class="chart-item chart-item-33 br-4">
          <h3 class="fw-normal font-14 chart-title">
            <el-popover
              placement="left-start"
              width="400"
              trigger="hover"
              popper-class="metric-info-popper">
              <div slot="reference" class="tit">
                <div class="name mb-8 ell">{{ value.titleKey ? $t(value.titleKey) : value.title }}</div>
                <div class="describe ell">{{ metricInfoMapping[value.title].desc }}</div>
              </div>
              <metric-info-tooltip
                :detail="metricInfoMapping[value.title]"
                :tooltip="false"
              />
            </el-popover>
          </h3>
          <basic-chart
            :showEmpty="!chartGroupBottom[key].loading && !chartGroupBottom[key].source.length"
            :key='key'
            :colors='chartGroupBottom[key].colors'
            :showLegend='true'
            :compactGrid="true"
            :textSmallMode="true"
            :minInterval="1"
            :min="0"
            group='bottomGroup'
            :yAxisSplitNum="3"
            :interval="timeParams.interval"
            :source='chartGroupBottom[key].source'></basic-chart>
        </div>
      </div>

    </div>

  </div>
</template>
<script lang='ts'>
import { toAsyncWait } from '@/utils/common';
import i18n from '@/i18n';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import dayjs from 'dayjs';
import MetricApi, { formatMetricInfos } from '@/api/metric';
import { orderBy } from 'lodash';
import MetricInfoTooltip from '@/components/metric-info-tooltip.vue';
import ChartTsSlot from '@/views/appMonitor/serviceAnalysis/chart-ts-slot.vue';

@Component({
  components: {
    MetricInfoTooltip, ChartTsSlot
  },
})
export default class TabJvm extends Vue {
  @Prop({ default: {} }) private current!: any;

  @Watch('current', { immediate: true })
  private onCurrentChange (val: any, oldVal: any) {
    if (val && val?.serviceId !== oldVal?.serviceId && this.isMounted) {
      this.fetchAllData();
    }
  }

  private isMounted = false;
  private showCharts = false;
  private metricLoading = true;
  private typeMetrics: any = {};
  private metricInfoMapping: any = {};
  private cacheSourceByType: any = {
    service: {},
    top: {},
  };

  private avgModel = 'top';
  private typeOptions: any[] = []
  private typeModel = '';

  private timeParams = {
    fromTime: Math.floor(+new Date() / 1000),
    toTime: Math.floor(+new Date() / 1000),
    interval: 60,
  }

  private chartGroup: any[] = [
    {
      type: 'jvm',
      title: i18n.t('modules.views.appMonitor.serviceDetail.s_aadb83b2') as string, titleKey: 'modules.views.appMonitor.serviceDetail.s_aadb83b2',
      desc: i18n.t('modules.views.appMonitor.serviceDetail.s_45378d2f') as string, descKey: 'modules.views.appMonitor.serviceDetail.s_45378d2f',
      metrics: [{
        name: 'jvm.thread_count',
        nameCn: i18n.t('modules.views.appMonitor.serviceDetail.s_aadb83b2') as string, nameCnKey: 'modules.views.appMonitor.serviceDetail.s_aadb83b2',
        topAble: true,
      }],
      aggs: 'mean',
      select: ['avg', 'top'],
      active: 'avg',
      loading: true,
      source: [],
      tsSource: [],
    },
    {
      type: 'jvm',
      title: i18n.t('modules.views.appMonitor.serviceDetail.s_573d00b8') as string, titleKey: 'modules.views.appMonitor.serviceDetail.s_573d00b8',
      desc: i18n.t('modules.views.appMonitor.serviceDetail.s_0469fa2d') as string, descKey: 'modules.views.appMonitor.serviceDetail.s_0469fa2d',
      metrics: [{
        name: 'jvm.gc.major_collection_count',
        nameCn: i18n.t('modules.views.appMonitor.serviceDetail.s_5044b6b4') as string, nameCnKey: 'modules.views.appMonitor.serviceDetail.s_5044b6b4',
      }, {
        name: 'jvm.gc.minor_collection_count',
        nameCn: i18n.t('modules.views.appMonitor.serviceDetail.s_e3063625') as string, nameCnKey: 'modules.views.appMonitor.serviceDetail.s_e3063625',
        topAble: true,
      }],
      aggs: 'mean',
      select: ['avg', 'top'],
      active: 'avg',
      loading: true,
      source: [],
      tsSource: [],
    },
    {
      type: 'jvm',
      title: i18n.t('modules.views.appMonitor.serviceDetail.s_42dd95a5') as string, titleKey: 'modules.views.appMonitor.serviceDetail.s_42dd95a5',
      desc: i18n.t('modules.views.appMonitor.serviceDetail.s_813efcd0') as string, descKey: 'modules.views.appMonitor.serviceDetail.s_813efcd0',
      metrics: [{
        name: 'jvm.gc.major_collection_time',
        nameCn: i18n.t('modules.views.appMonitor.serviceDetail.s_bddee411') as string, nameCnKey: 'modules.views.appMonitor.serviceDetail.s_bddee411',
      }, {
        name: 'jvm.gc.minor_collection_time',
        nameCn: i18n.t('modules.views.appMonitor.serviceDetail.s_5038ae94') as string, nameCnKey: 'modules.views.appMonitor.serviceDetail.s_5038ae94',
        topAble: true,
      }],
      aggs: 'mean',
      select: ['avg', 'top'],
      active: 'avg',
      loading: true,
      source: [],
      tsSource: [],
    },
  ]

  private chartGroupBottom = [];
  private currentTsItem: any = null;

  get isLoading () {
    const chartLoading = !!(Object.values(this.chartGroup).find((i) => i.loading));
    const bottomLoading = this.chartGroupBottom.find((i: any) => i.loading);
    return this.metricLoading || chartLoading || bottomLoading
  }

  @Watch('isLoading')
  private onIsLoading (newVal: boolean) {
    if (!newVal) {
      this.$emit('on-loaded')
    }
  }

  private created () {
    this.$emit('on-created');
    this.resetTimeParams();
  }
  private mounted () {
    if (this.current?.serviceId ) {
      this.refresh();
    }
    this.isMounted = true;
  }

  public refresh () {
    this.fetchAllData();
  }

  private fetchAllData () {
    this.cacheSourceByType = {
      service: {},
      top: {},
    };
    this.typeMetrics = {}
    this.metricInfoMapping = {}
    this.resetTimeParams();
    this.getJvmMetrics();
    this.fetchTopData();
  }

  private resetTimeParams () {
    const { fromTime, toTime, interval } = this.getGlobalTime();
    this.timeParams = {
      fromTime: Math.floor(fromTime.valueOf() / 1000),
      toTime: Math.floor(toTime.valueOf() / 1000),
      interval
    };
  }

  private fetchTopData (cItem?: any, cIdx?: number) {
    this.chartGroup.forEach(t => t.source = []);
    if (!cItem) {
      this.chartGroup.forEach((item, index) => {
        item.metrics.forEach((t: any, i: number) => {
          if (item.active !== 'top' || t.topAble) {
            this.getChartItem(item, index, t)
          }
        })
      })
    } else {
      cItem.source = []
      cItem.metrics.forEach((t: any, i: number) => {
        if (cItem.active !== 'top' || t.topAble) {
          this.getChartItem(cItem, cIdx as number, t)
        }
      })
    }
  }

  private async getChartItem (chartItem: any, idx: number, metric: any) {
    const serviceId = this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid));
    const serviceInstance = this.current?.serviceInstance || decodeURIComponent(String(this.$route.query.si));
    const metricQuery: any = { metric: metric.name, aggs: chartItem.aggs, from: [], types: [], by: ['serviceInstance'], }
    metricQuery.from.push({ left: 'serviceId', operator: '=', right: serviceId, connector: 'AND' });
    metricQuery.from.push({ left: 'serviceInstance', operator: '=', right: serviceInstance, connector: 'AND' });
    const params: any = {
      query: { A: metricQuery, expr: 'A' },
      start: this.timeParams.fromTime,
      end: this.timeParams.toTime,
      interval: this.timeParams.interval,
    }
    chartItem.loading = true
    const { result, error } = await toAsyncWait(MetricApi.getMetricChart(params))
    if (!error) {
      const data = result.data || []
      const chartData: any[] = [...chartItem.source];
      for (const item of data) {
        const values: any[] = item.values || []
        if (!values.length) {
          continue;
        }
        const name = Object.entries(item.tags || {}).map(([k, v]) => `${k}:${v || ''}`).join(';')
        chartData.push({
          name: metric.nameCn || name,
          unit: (item.units || [])[1] || '',
          area: true,
          data: values.map(([key, value]: any) => ({
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          })),
        })
      }
      const metrics: string[] = chartItem.metrics.map((t: any) => t.nameCn)
      chartData.sort((a, b) => {
        return metrics.findIndex(t => t === a.name) - metrics.findIndex(t => t === b.name)
      })
      chartItem.source = chartData;

      // v2.9.1 ++
      if (data?.length) {
        const rootDetails = (data || []).map((i: any) => i?.rootDetails || []).flat()
        const tsSource = rootDetails.filter((i: any) => i.abnormalStartTime && i.abnormalEndTime).map((i: any) => {
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
        chartItem.tsSource = tsSource;
      } else {
        chartItem.tsSource = [];
      }
    }
    chartItem.loading = false
  }

  private async getJvmMetrics () {
    const params: any = { type1: i18n.t('modules.views.appMonitor.serviceDetail.s_6b522b81') as string, type2: i18n.t('modules.views.appMonitor.serviceDetail.s_84d31a52') as string }
    this.metricLoading = true;
    const { result, error } = await toAsyncWait(MetricApi.getAllMetricListByQuery(params));
    if (!error) {
      const data = result.data || {}
      this.metricInfoMapping = data
      const typeMetrics: any = {}
      Object.values(data).filter((item: any) => item.type3).forEach((item: any) => {
        if (!typeMetrics[item.type3]) {
          typeMetrics[item.type3] = []
        }
        typeMetrics[item.type3] = orderBy([...typeMetrics[item.type3], item.identifier], [t => t.toLocaleLowerCase()], ['asc']);
      })
      this.typeMetrics = typeMetrics;
      const types = orderBy(Object.keys(typeMetrics), [t => t.toLocaleLowerCase()], ['asc']);
      this.typeOptions = types.map((t) => ({ label: t, value: t }));
      if (types.length) {
        this.typeModel = types[0];
        this.changeTypeHandle({ value: types[0] });
      }
      this.$store.commit('Common/SET_METRIC_INFOS', formatMetricInfos(data));
    }
    this.$nextTick(() => {
      this.metricLoading = false;
    })
  }

  // 获取指标趋势
  private async fetchSource (chartItem: any, model: string) {
    const serviceId = this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid));
    const serviceInstance = this.current?.serviceInstance || decodeURIComponent(String(this.$route.query.si));
    const metricQuery: any = { ...chartItem.query, from: [], types: [], by: ['serviceInstance'], }
    metricQuery.from.push({ left: 'serviceId', operator: '=', right: serviceId, connector: 'AND' });
    metricQuery.from.push({ left: 'serviceInstance', operator: '=', right: serviceInstance, connector: 'AND' });
    const params: any = {
      query: { A: metricQuery, expr: 'A' },
      start: this.timeParams.fromTime,
      end: this.timeParams.toTime,
      interval: this.timeParams.interval,
    }
    chartItem.loading = true;
    // 需请求两个指标
    const { result, error } = await toAsyncWait(MetricApi.getMetricChart(params))
    const _data = result?.data || []
    if (!Array.isArray(_data) || !_data.length || error) {
      chartItem.source = []
    } else {
      const chartData: any[] = [];
      for (const item of _data) {
        const values: any[] = item.values || []
        if (!values.length) {
          continue;
        }
        const name = Object.entries(item.tags || {}).map(([k, v]) => `${k}:${v || ''}`).join(';')
        chartData.push({
          name: chartItem.title || name,
          unit: (item.units || [])[1] || '',
          area: true,
          data: values.map(([key, value]: any) => ({
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          })),
        });
      }
      chartItem.source = chartData
    }
    if (!this.cacheSourceByType[this.typeModel]) {
      this.cacheSourceByType[this.typeModel] = [];
    }
    this.cacheSourceByType[this.typeModel].push(chartItem);
    chartItem.loading = false;
  }

  private changeAvgHandle (option: any) {
    const { value } = option;
    this.fetchTopData();
  }
  private changeTypeHandle (option: any) {
    const { value } = option;
    if (!this.cacheSourceByType[value] && Array.isArray(this.typeMetrics[value])) {
      const chartGroup = this.typeMetrics[value].map((metric: string) => ({
        title: metric,
        query: {
          metric, aggs: 'mean'
        },
        loading: true,
        source: [],
        colors: ['#2962FF', '#00AFF4'],
      }));
      this.chartGroupBottom = chartGroup;
      this.cacheSourceByType[value] = [];
      this.chartGroupBottom.forEach((chartItem) => {
        this.fetchSource(chartItem, 'top');
      });
    } else {
      this.chartGroupBottom = this.cacheSourceByType[value]
    }
  }

  private changeSumHandle (option: any) {
    const { value } = option;
    this.changeTypeHandle({ value: this.typeModel });
  }

  private onTsTooltipShow (row: any) {
    this.currentTsItem = row.info || null;
  }

}
</script>
<style lang='scss' scoped>
.baseinfo-cont {
  overflow: hidden;
  position: relative;
}
.chart-group {
  display: flex;
  flex-wrap: wrap;
  overflow: hidden;

  .chart-item {
    flex: 0 0 auto;
    height: 218px;
    overflow: hidden;
    margin: 8px 4px;
    padding: 54px 15px 15px;
    border: 1px solid var(--border-color-base);
    position: relative;

    &.chart-item-33 {
      width: calc( 33.33% - 16px );
      &:not(:last-child) {
        margin-right: 12px;
      }
    }
    &.chart-item-50 {
      width: calc( 50% - 16px );
      &:nth-child(odd) {
        margin-right: 12px;
      }
    }
  }
  .chart-title {
    width: calc(100% - 30px);
    position: absolute;
    top: 14px;
    left: 16px;
    font-size: 12px;
    line-height: 14px;
    margin: 0;
  }
}
</style>