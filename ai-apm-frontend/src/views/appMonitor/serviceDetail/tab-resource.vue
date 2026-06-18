<template>
  <div class="baseinfo-cont">
    <div class="baseinfo-wrapper">
      
      <div>
        <db-radio v-model='avgModel' :options='avgOptions' @change='changeAvgHandle'></db-radio>
      </div>
      <div class="chart-group">
        <div v-for='value,key in chartGroup' :key='key' class="chart-item chart-item-33 br-4">
          <h3 class="fw-normal font-14 chart-title">{{ value.nameKey ? $t(value.nameKey) : value.name }}</h3>
          <basic-chart
            :showEmpty="!chartGroup[key].loading && !chartGroup[key].source.length"
            :key='key'
            :colors='chartGroup[key].colors'
            :showLegend='true'
            :compactGrid="true"
            :textSmallMode="true"
            :minInterval="1"
            :min="0"
            :brushMode='true'
            group='resource'
            :yAxisSplitNum="3"
            :interval="timeParams.interval"
            :source='chartGroup[key].source'
            :tsSource='chartGroup[key].tsSource'
            @on-ts-tooltip-show='onTsTooltipShow'
          >
            <template slot='ts'>
              <ChartTsSlot v-if='avgModel === "service"' :current='currentTsItem' />
            </template>
          </basic-chart>
        </div>
      </div>

      <div class="mt-20">
        <db-radio v-model='sumModel' :options='sumOptions' @change='changeSumHandle'></db-radio>
      </div>
      <div class="chart-group">
        <div v-for='value,key in chartGroupBottom' :key='key' class="chart-item chart-item-50 br-4">
          <h3 class="fw-normal font-14 chart-title">{{ value.nameKey ? $t(value.nameKey) : value.name }}</h3>
          <basic-chart
            :showEmpty="!chartGroupBottom[key].loading && !chartGroupBottom[key].source.length"
            :key='key'
            :colors='chartGroupBottom[key].colors'
            :showLegend='true'
            :compactGrid="true"
            :textSmallMode="true"
            :minInterval="1"
            :min="0"
            :brushMode='true'
            group='bottomResource'
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
import MetricApi from '@/api/metric';
import dayjs from 'dayjs';
import ChartTsSlot from '@/views/appMonitor/serviceAnalysis/chart-ts-slot.vue';

@Component({
  components: { ChartTsSlot },
})
export default class TabResource extends Vue {
  @Prop({ default: {} }) private current!: any;

  @Watch('current', { immediate: true })
  private onCurrentChange (val: any, oldVal: any) {
    if (val && val?.serviceId !== oldVal?.serviceId && this.isMounted) {
      this.fetchAllData();
    }
  }

  private isMounted = false;
  private showCharts = false;

  private avgOptions = [
    { label: i18n.t('modules.views.appMonitor.serviceDetail.s_7fa16e94') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_7fa16e94', value: 'service' },
    { label: i18n.t('modules.views.appMonitor.serviceDetail.s_1d0fd439') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_1d0fd439', value: 'top' },
  ]
  private avgModel = 'service';
  private sumOptions = [
    { label: i18n.t('modules.views.appMonitor.serviceDetail.s_df2d0879') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_df2d0879', value: 'service' },
    { label: i18n.t('modules.views.appMonitor.serviceDetail.s_1d0fd439') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_1d0fd439', value: 'top' },
  ]
  private sumModel = 'service';

  private timeParams = {
    fromTime: Math.floor(+new Date() / 1000),
    toTime: Math.floor(+new Date() / 1000),
    interval: 60,
  }

  private chartGroup: any = {
    cpu: {
      query: {
        metric: 'service.cpu.usage_pct',
        aggs: 'avg',
      },
      loading: true,
      source: [],
      tsSource: [],
      colors: ['#2962FF', '#00AFF4'],
      name: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, nameKey: 'modules.views.appMonitor.relationMap.s_7054bc34'
    },
    mem: {
      query: {
        metric: 'service.mem.usage_pct',
        aggs: 'avg',
      },
      loading: true,
      source: [],
      tsSource: [],
      colors: ['#2962FF'],
      name: i18n.t('modules.views.appMonitor.relationMap.s_31cb8d97') as string, nameKey: 'modules.views.appMonitor.relationMap.s_31cb8d97'
    },
    heap: {
      query: {
        metric: 'jvm.memory.heap.pct',
        aggs: 'avg',
      },
      loading: true,
      source: [],
      tsSource: [],
      colors: ['#2962FF', '#00AFF4'],
      name: i18n.t('modules.views.appMonitor.serviceDetail.s_fa9243ca') as string, nameKey: 'modules.views.appMonitor.serviceDetail.s_fa9243ca'
    },
  }

  private chartGroupBottom = {
    networkSend: {
      query: {
        metric: 'service.net.bytes_sent',
        aggs: 'sum',
      },
      loading: true,
      source: [],
      tsSource: [],
      colors: ['#2962FF', '#00AFF4'],
      name: i18n.t('modules.views.appMonitor.serviceDetail.s_90d6afb7') as string, nameKey: 'modules.views.appMonitor.serviceDetail.s_90d6afb7'
    },
    networkReceive: {
      query: {
        metric: 'service.net.bytes_rcvd',
        aggs: 'sum',
      },
      loading: true,
      source: [],
      tsSource: [],
      colors: ['#8DCFF8'],
      name: i18n.t('modules.views.appMonitor.serviceDetail.s_3e54c81c') as string, nameKey: 'modules.views.appMonitor.serviceDetail.s_3e54c81c'
    },
    tcpSend: {
      query: {
        metric: 'service.tcp.retransmit',
        aggs: 'sum',
      },
      loading: true,
      source: [],
      tsSource: [],
      colors: ['#2962FF', '#F37370'],
      name: i18n.t('modules.views.appMonitor.serviceDetail.s_5b545e42') as string, nameKey: 'modules.views.appMonitor.serviceDetail.s_5b545e42'
    },
    tcpBuild: {
      query: {
        metric: 'service.tcp.conns_established',
        aggs: 'sum',
      },
      loading: true,
      source: [],
      tsSource: [],
      colors: ['#2962FF', '#F37370'],
      name: i18n.t('modules.views.appMonitor.serviceDetail.s_2347150f') as string, nameKey: 'modules.views.appMonitor.serviceDetail.s_2347150f'
    },
  }

  private currentTsItem: any = null;

  get isLoading () {
    const chartLoading = !!(Object.values(this.chartGroup).find((i: any) => i.loading));
    const bottomLoading = !!(Object.values(this.chartGroupBottom).find((i) => i.loading));
    return chartLoading || bottomLoading
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

  private resetTimeParams () {
    const { fromTime, toTime, interval } = this.getGlobalTime();
    this.timeParams = {
      fromTime: Math.floor(fromTime.valueOf() / 1000),
      toTime: Math.floor(toTime.valueOf() / 1000),
      interval
    };
  }

  private fetchAllData () {
    this.resetTimeParams();
    this.fetchTopData();
    this.fetchBottomData();
  }

  private fetchTopData () {
    this.fetchSource(this.chartGroup.cpu, this.avgModel);
    this.fetchSource(this.chartGroup.mem, this.avgModel);
    this.fetchSource(this.chartGroup.heap, this.avgModel);
  }

  private fetchBottomData () {
    this.fetchSource(this.chartGroupBottom.networkSend, this.sumModel);
    this.fetchSource(this.chartGroupBottom.networkReceive, this.sumModel);
    this.fetchSource(this.chartGroupBottom.tcpSend, this.sumModel);
    this.fetchSource(this.chartGroupBottom.tcpBuild, this.sumModel);
  }

  // 获取指标趋势
  private async fetchSource (chartItem: any, model: string) {
    const serviceId = this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid));
    const metricQuery: any = { ...chartItem.query, from: [], types: [], by: [], }
    metricQuery.from.push({ left: 'serviceId', operator: '=', right: serviceId, connector: 'AND' });
    if (model === 'top') {
      metricQuery.by.push('serviceInstance');
      metricQuery.order = { code: metricQuery.metric, func: 'top', limit: 5 };
    }
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
        const name = Object.entries(item.tags || {}).map(([k, v]) => `${k}:${v || ''}`).join(';');
        chartData.push({
          name: model !== 'top' ? chartItem.name : name,
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
    // v2.9.1 ++
    if (model === 'service' && _data?.length) {
      const rootDetails = (_data || []).map((i: any) => i?.rootDetails || []).flat()
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
    chartItem.loading = false;
  }

  private changeAvgHandle (option: any) {
    const { value } = option;
    this.fetchTopData();
  }
  private changeSumHandle (option: any) {
    const { value } = option;
    this.fetchBottomData();
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
    height: 208px;
    overflow: hidden;
    margin: 8px 4px;
    padding: 35px 15px 15px;
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
    position: absolute;
    top: 15px;
    left: 15px;
    line-height: 1;
    margin: 0;
  }
}
</style>