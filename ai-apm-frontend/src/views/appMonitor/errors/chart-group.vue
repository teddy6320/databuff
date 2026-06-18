<template>
  <div class="chart-group-wrap">
    <div v-if="showServiceChart" v-loading="chartLoading3" class="chart-item">
      <div class="item-title">{{ $t('modules.views.appMonitor.errors.s_29e2250c') }}</div>
      <div class="item-cont">
        <pie-chart-new
          :source="chartSource3"
          :tooltipFormat="servicePieTooltipFormat"
          labelPosition="outside"
          :showLegend="false"
          :showTooltip="true"
          :showTotal="true"
          :showEmpty="!chartLoading3 && !chartSource3.length"
        />
      </div>
    </div>

    <div v-loading="chartLoading1" class="chart-item">
      <div class="item-title">{{ $t('modules.views.appMonitor.errorDetail.s_4eb644dc') }}</div>
      <div class="item-cont">
        <basic-chart
          :source="chartSource1"
          :colors="['#ED3B3B']"
          :showLegend="true"
          :yAxisSplitNum="3"
          :compactGrid="true"
          :textSmallMode="true"
          :showEmpty="!chartLoading1 && !chartSource1.length"
        />
      </div>
    </div>

    <div v-loading="chartLoading2" class="chart-item">
      <div class="item-title">{{ $t('modules.views.appMonitor.errors.s_f9e57bb8') }}</div>
      <div class="item-cont">
        <pie-chart-new
          :source="chartSource2"
          labelPosition="outside"
          labelFormatter="{b}: {d}%"
          :showLegend="false"
          :showTooltip="true"
          :showTotal="true"
          :showEmpty="!chartLoading2 && !chartSource2.length"
        />
      </div>
    </div>

    <div v-if="!showServiceChart" v-loading="chartLoading4" class="chart-item">
      <div class="item-title">{{ $t('modules.views.appMonitor.errors.s_9123c0be') }}</div>
      <div class="item-cont">
        <basic-chart
          :source="chartSource4"
          :reversalAxis="true"
          :showEmpty="!chartLoading4 && !chartSource4.length"
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
import PieChartNew from '@/components/charts/pie-chart-new.vue';
import { toAsyncWait } from '@/utils/common'
import ServiceApi from '@/api/service';
import MetricApi from '@/api/metric';

@Component({
  components: {
    BasicChart,
    PieChartNew,
  }
})
export default class ChartGroup extends Vue {
  @Prop({ default: () => ({}) }) private query!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;

  get showServiceChart () {
    return !this.query.serviceId
  }

  // 错误请求趋势
  private chartLoading1 = false;
  private chartSource1: any[] = [];

  // 错误原因分布
  private chartLoading2 = false;
  private chartSource2: any[] = [];

  // 服务的错误数统计Top5
  private chartLoading3 = false;
  private chartSource3: any[] = [];

  // 报错请求Top
  private chartLoading4 = false;
  private chartSource4: any[] = [];

  public getData () {
    this.getErrorRequestTrend()
    this.getErrorReasonDist()
    if (this.showServiceChart) {
      this.getServiceErrorDist()
    } else {
      this.getErrorRequestTop()
    }
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

  // 获取错误原因分布
  private async getErrorReasonDist () {
    const params: any = {
      ...this.query,
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      groupBy: 'exceptionName',
      offset: 0,
      size: 9,
      sortField: 'errCnt',
      sortOrder: 'desc',
    }
    this.chartLoading2 = true;
    const { result, error } = await toAsyncWait(ServiceApi.getErrorDistList(params))
    this.chartLoading2 = false;
    if (!error) {
      const data = (result.data || []).map((t: any) => ({
        key: t.exceptionName,
        value: t.errCnt,
        // percent: t.percentage,
      }))
      const other = (result.totalError || 0) - data.reduce((acc: number, cur: any) => acc + cur.value, 0);
      if (other > 0) {
        data.push({ key: i18n.t('modules.views.alarmCenter.problemAnalysis.s_0d98c747') as string, value: other })
      }
      this.chartSource2 = data
    } else {
      this.chartSource2 = []
    }
  }

  // 获取服务的错误数统计Top5
  private async getServiceErrorDist () {
    const params: any = {
      ...this.query,
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      groupBy: 'serviceId',
      offset: 0,
      size: 5,
      sortField: 'errCnt',
      sortOrder: 'desc',
    }
    this.chartLoading3 = true;
    const { result, error } = await toAsyncWait(ServiceApi.getErrorDistList(params))
    this.chartLoading3 = false;
    if (!error) {
      const data = (result.data || []).map((t: any) => ({
        key: t.service,
        value: t.errCnt,
        percent: t.percentage,
      }))
      const other = (result.totalError || 0) - data.reduce((acc: number, cur: any) => acc + cur.value, 0);
      if (other > 0) {
        const totalError = result.totalError || 0;
        data.push({
          key: i18n.t('modules.views.alarmCenter.problemAnalysis.s_0d98c747') as string,
          value: other,
          percent: totalError > 0 ? other * 100 / totalError : 0,
        })
      }
      this.chartSource3 = data
    } else {
      this.chartSource3 = []
    }
  }
  private servicePieTooltipFormat (params: any, valTickFormat: any) {
    const { key, value, percent } = params.data
    return i18n.t('modules.components.charts.s_d263ec4e', { value0: key, value1: valTickFormat(value), value2: +percent.toFixed(2) }) as string
  }

  // 获取报错请求Top
  private async getErrorRequestTop () {
    const params: any = {
      ...this.query,
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      groupBy: 'rootResource',
      notEmptyFields: 'rootResource',
      offset: 0,
      size: 5,
      sortField: 'errCnt',
      sortOrder: 'desc',
    }
    this.chartLoading4 = true;
    const { result, error } = await toAsyncWait(ServiceApi.getErrorDistList(params))
    this.chartLoading4 = false;
    if (!error) {
      this.chartSource4 = [{
        name: i18n.t('modules.views.appMonitor.errors.s_8731f2a8') as string, nameKey: 'modules.views.appMonitor.errors.s_8731f2a8',
        type: 'bar',
        data: (result.data || []).map((t: any) => ({
          key: t.rootResource,
          value: t.errCnt,
        })),
      }]
    } else {
      this.chartSource4 = []
    }
  }
}
</script>

<style lang="scss" scoped>
.chart-group-wrap {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;

  .chart-item {
    margin-top: 16px;
    width: calc((100% - 32px) / 3);
    border: 1px solid var(--border-color-base);
    border-radius: 4px;
    background-color: var(--bg-color);
    .item-title {
      height: 34px;
      padding: 20px 20px 0;
      font-size: 14px;
      line-height: 14px;
      color: var(--color-text-primary);
    }
    .item-cont {
      height: 204px;
      padding: 15px 20px;
    }
  }
}
</style>
