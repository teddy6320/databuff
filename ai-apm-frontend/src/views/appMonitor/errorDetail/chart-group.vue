<template>
  <div class="chart-group-wrap">
    <div v-loading="chartLoading1" class="chart-item">
      <div class="item-title">{{ $t('modules.views.appMonitor.errorDetail.s_4eb644dc') }}</div>
      <div class="item-cont">
        <basic-chart
          :source="chartSource1"
          :colors="['#ED3B3B']"
          :yAxisSplitNum="3"
          :textSmallMode="true"
          :showLegend="true"
          :showEmpty="!chartLoading1 && !chartSource1.length"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import BasicChart from '@/components/charts/basic-chart.vue';
import { toAsyncWait } from '@/utils/common'
import MetricApi from '@/api/metric';

@Component({
  components: {
    BasicChart,
  }
})
export default class ChartGroup extends Vue {
  @Prop({ default: () => ({}) }) private query!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;

  private chartLoading1 = false;
  private chartSource1: any[] = [];

  public getData () {
    this.getErrorRequestTrend()
  }

  // 获取错误请求趋势
  private async getErrorRequestTrend () {
    const { fromTime, toTime, interval } = this.timeParams
    const params: any = {
      start: Math.floor(+new Date(fromTime) / 1000),
      end: Math.floor(+new Date(toTime) / 1000),
      interval,
      query: { expr: 'A', A: {
        metric: 'service.exception.cnt',
        from: [],
        aggs: '', by: [], types: [],
      }},
    }
    const _query = params.query.A
    Object.entries(this.query).filter(([key, value]) => !!value).forEach(([key, value]) => {
      if (key === 'resourceQuery') {
        _query.from.push({ left: 'resource', operator: 'like', right: value, connector: 'AND' })
      } else if (key === 'rootResourceQuery') {
        _query.from.push({ left: 'rootResource', operator: 'like', right: value, connector: 'AND' })
      } else if (key === 'exception') {
        _query.from.push({ left: 'exceptionName', operator: 'like', right: value, connector: 'AND' })
      } else {
        _query.from.push({ left: key, operator: '=', right: value, connector: 'AND' })
      }
    });
    this.chartLoading1 = true
    const { result, error } = await toAsyncWait(MetricApi.getMetricChart(params))
    this.chartLoading1 = false
    if (!error) {
      const data = (result.data || [])[0] || {}
      this.chartSource1 = [{
        name: i18n.t('modules.views.appMonitor.errorDetail.s_5c7753a2') as string, nameKey: 'modules.views.appMonitor.errorDetail.s_5c7753a2',
        unit: (data.units || [])[1] || '',
        data: (data.values || []).map(([key, value]: any) => ({
          key: dayjs(key * 1000).format('YYYY-MM-DD HH:mm'),
          value,
        })),
        area: true,
      }]
    } else {
      this.chartSource1 = []
    }
  }
}
</script>

<style lang="scss" scoped>
.chart-group-wrap {
  display: flex;
  margin-top: 20px;
  flex-wrap: wrap;
  justify-content: space-between;
  border: 1px solid var(--border-color-base);
  border-radius: 4px;

  .chart-item {
    width: 100%;
    background-color: var(--bg-color);
    .item-title {
      height: 44px;
      padding: 16px 20px 0;
      font-size: 16px;
      line-height: 22px;
      color: var(--color-text-primary);
    }
    .item-cont {
      height: 222px;
      padding: 0 10px;
    }
  }
}
</style>
