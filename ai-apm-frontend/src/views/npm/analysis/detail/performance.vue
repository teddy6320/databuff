<template>
  <div class="performance-wrap">
    <tag-input @on-change="tagChangeHandle" class="query-tag-input" />

    <div class="chart-list">
      <div style="margin:0 -8px;overflow:hidden">
        <div
          v-for="item in chartTypes"
          :key="item.value"
          class="chart-item-wrap g-sm-6">
          <div
            v-loading="chartData[item.value].loading"
            class="chart-item">
            <div class="chart-title">{{ item.labelKey ? $t(item.labelKey) : item.label }}</div>
            <div
              class="chart-cont">
              <basic-chart
                :source="chartData[item.value].source"
                :showEmpty="!chartData[item.value].loading && !chartData[item.value].source.length"
                :showAxisLabelCount="4"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import dayjs from 'dayjs';
import BasicChart from '@/components/charts/basic-chart.vue';
import TagInput from '../tag-input.vue';
import NpmApi from '@/api/npm';
import { toAsyncWait } from '@/utils/common';

@Component({
  components: {
    BasicChart,
    TagInput,
  }
})
export default class Performance extends Vue {
  @Prop({ default: '' }) private timeParamsStr!: string;
  @Prop({ default: () => [] }) private chartTypes!: any[];
  @Prop({ default: () => [] }) private tagList!: string[];

  private chartData: any = {}
  private chartLoading = false

  get queryFrom () {
    const from = this.tagList.map((tag, index) => {
      const [key, ...values] = tag.split(':')
      return { left: key, operator: '=', right: values.join(':'), connector: 'AND' }
    })
    return from
  }

  @Watch('timeParamsStr')
  private onTimeParamsChange (newVal: any, oldVal: any) {
    if (newVal !== oldVal) {
      const { fromTime, toTime, interval } = JSON.parse(this.timeParamsStr || '{}')
      this.queryParams.fromTime = fromTime
      this.queryParams.toTime = toTime
      this.queryParams.interval = interval

      Object.values(this.chartData).forEach((chartItem: any) => {
        chartItem.loaded = false;
      });
      this.getChartData()
    }
  }

  private queryParams: any = {
    from: [],
    fromTime: '',
    toTime: '',
    interval: 300,
  }

  private created() {
    const { fromTime, toTime, interval } = JSON.parse(this.timeParamsStr || '{}')
    this.queryParams.fromTime = fromTime
    this.queryParams.toTime = toTime
    this.queryParams.interval = interval

    // 初始化
    this.chartTypes.forEach(t => {
      this.$set(this.chartData, t.value, {
        ...t,
        loading: false,
        loaded: false,
        source: [],
      });
    });

    // 加载图表数据
    this.getChartData()
  }

  private async getChartData () {
    const { fromTime, toTime, interval } = this.queryParams
    const params: any = {
      metrics: [],
      start: Math.floor(+new Date(fromTime)),
      end: Math.floor(+new Date(toTime)),
      interval,
      by: [],
      from: [{
        left: [...this.queryFrom],
        connector: 'AND',
        right: [...this.queryParams.from],
      }],
    }
    this.chartTypes.forEach(t => {
      const chartItem = this.chartData[t.value]
      params.metrics.push(t.value)
      chartItem.loading = true
    });
    if (!params.metrics.length) {
      return;
    }
    const { result, error } = await toAsyncWait(NpmApi.getPerformanceMetricsData(params));
    if (!error) {
      const data = (result.data || [])[0] || {}
      params.metrics.forEach((t: any) => {
        const index = (data.columns || []).findIndex((col: string) => col === t)
        const unit = (data.units || [])[index] || ''
        const chartItem = this.chartData[t]
        if (index >= 0) {
          chartItem.source = [{
            name: chartItem.label,
            unit,
            data: (data.values || []).map((v: any) => ({
              key: dayjs(Number(v[0])).format('YYYY-MM-DD HH:mm'),
              value: v[index],
            })),
          }]
          chartItem.loaded = true;
        } else {
          chartItem.source = []
        }
        chartItem.loading = false;
      });
    } else {
      params.metrics.forEach((t: any) => {
        const chartItem = this.chartData[t]
        chartItem.source = []
        chartItem.loading = false;
      })
    }
  }

  private tagChangeHandle (from: any[]) {
    if (JSON.stringify(from) === JSON.stringify(this.queryParams.from)) {
      return
    }
    this.queryParams.from = from
    Object.values(this.chartData).forEach((chartItem: any) => {
      chartItem.loaded = false;
    });
    this.getChartData()
  }
}
</script>

<style lang="scss" scoped>
.performance-wrap {
  height: 100%;
  .query-tag-input {
    margin-bottom: 16px;
  }

  .chart-list {
    height: calc(100% - 90px);
    overflow-x: hidden;
    overflow-y: auto;
  }

  .chart-item-wrap {
    margin-bottom: 16px;
    padding: 0 8px;

    .chart-item {
      height: 250px;
      display: flex;
      flex-direction: column;
      border: 1px solid var(--border-color-light);
      position: relative;
    }

    .chart-title {
      height: 34px;
      padding: 12px 16px 6px;
      font-size: 14px;
      line-height: 16px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
    }

    .chart-cont {
      flex: 1;
      padding: 0 8px;
      height: calc(100% - 34px);
      position: relative;
    }
  }
}
</style>
