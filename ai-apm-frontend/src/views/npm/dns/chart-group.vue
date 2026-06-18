<template>
  <div class="chart-group" :class="`chart-group-${layoutType}`">
    <div v-if="layoutType !== 'detail'" class="group-header flex-h-jc">
      <div class="flex-h">
        <span
          @click="toggleFilterActiveHandle"
          class="blue cp mr-15">
          <i :class='filterActive ? "el-icon-caret-left" : "el-icon-caret-right"'></i>
          {{ filterActive ? $t('modules.views.npm.analysis.s_a298e3cd') : $t('modules.views.npm.analysis.s_e9566b49') }}
        </span>

        <div @click="toggleChartHandle" class="group-title">
          <i :class="['el-icon-arrow-down', { hide: !showChart }]"></i> {{ $t('modules.views.npm.analysis.s_c6b32e25') }}
        </div>
      </div>
    </div>

    <div v-if="showChart" v-loading="queryLoading" class="group-cont flex-h">
      <div
        v-for="item in showChartTypes"
        :key="item.value"
        v-loading="chartData[item.value].loading"
        class="chart-item">
        <div class="chart-title">{{ item.labelKey ? $t(item.labelKey) : item.label }}</div>
        <div class="chart-cont">
          <basic-chart
            :ref="item.value"
            :source="chartData[item.value].source"
            :showEmpty="!chartData[item.value].loading && !chartData[item.value].source.length"
            :textSmallMode="layoutType === 'detail'"
            :showAxisLabelCount="layoutType === 'detail' ? 3 : 4" />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import deepClone from 'lodash/cloneDeep';
import BasicChart from '@/components/charts/basic-chart.vue';
import NpmApi from '@/api/npm';
import { toAsyncWait } from '@/utils/common';

@Component({
  components: {
    BasicChart,
  },
})
export default class ChartGroup extends Vue {
  @Prop({ default: () => ({}) }) private query!: any;
  @Prop({ default: () => ({}) }) private filter!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;
  @Prop({ default: false }) private queryLoading!: boolean;
  @Prop({ default: 'list' }) private layoutType!: 'list' | 'detail';

  private filterActive = true;

  private showChart = true;

  private chartData: any = {}

  private showChartTypes = [
    { label: i18n.t('modules.views.npm.dns.s_b1ccc6a6') as string, labelKey: 'modules.views.npm.dns.s_b1ccc6a6', value: 'dns.cnt', chartType: 'bar' },
    { label: i18n.t('modules.views.npm.dns.s_0285f9f7') as string, labelKey: 'modules.views.npm.dns.s_0285f9f7', value: 'dns.errors.pct', chartType: 'line' },
    { label: i18n.t('modules.views.appMonitor.relationMap.s_207c26c9') as string, labelKey: 'modules.views.appMonitor.relationMap.s_207c26c9', value: 'dns.response_time', chartType: 'line' },
  ]

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

  private created () {
    this.showChartTypes.forEach(t => {
      this.$set(this.chartData, t.value, {
        ...t,
        loading: false,
        loaded: false,
        source: [],
      });
    });
  }

  public getData () {
    // 清空load状态
    Object.values(this.chartData).forEach((chartItem: any) => {
      chartItem.loading = false;
      chartItem.loaded = false;
    });
    // 加载图表数据
    const metrics = this.showChartTypes.map(t => t.value);
    this.getChartData(metrics.filter(t => t !== 'dns.errors.pct'));
    this.getChartData(['dns.errors.pct'], true);
  }

  private async getChartData (metrics: string[], isError = false) {
    const { fromTime, toTime, interval } = this.timeParams
    const params: any = {
      metrics: [],
      start: Math.floor(+new Date(fromTime)),
      end: Math.floor(+new Date(toTime)),
      interval,
      by: [],
      from: [{
        left: [...this.query.from],
        connector: 'AND',
        right: [...this.filterFrom],
      }],
    }
    if (!this.query.from.length && !this.filterFrom.length) {
      params.from = []
    }
    metrics.forEach(t => {
      const chartItem = this.chartData[t]
      if (!chartItem.loading && !chartItem.loaded) {
        params.metrics.push(t)
        chartItem.loading = true
      }
    });
    if (!params.metrics.length) {
      return;
    }
    if (isError) {
      params.by = ['dnsRcode']
      params.order = {
        code: params.metrics[0],
        limit: 5,
      }
      const errorFrom = [{
        left: 'dnsRcode',
        operator: '!=',
        right: i18n.t('modules.views.npm.dns.s_1e9fd7ea') as string,
        connector: 'AND',
      }]
      params.from = [{ left: errorFrom, connector: 'AND', right: [...params.from] }]
    }
    const { result, error } = await toAsyncWait(NpmApi.getPerformanceMetricsData(params));
    if (!error) {
      const dataList = result?.data || []
      const sourceMap: any = {};
      dataList.forEach((data: any) => {
        params.metrics.forEach((t: any) => {
          sourceMap[t] = sourceMap[t] || [];
          const index = (data.columns || []).findIndex((col: string) => col === t)
          const unit = (data.units || [])[index] || ''
          const dnsRcode = data.tags?.dnsRcode || ''
          if (index >= 0) {
            sourceMap[t].push({
              name: !isError ? this.chartData[t]?.label : dnsRcode,
              unit,
              type: this.chartData[t]?.chartType,
              data: (data.values || []).map((v: any) => ({
                key: dayjs(Number(v[0])).format('YYYY-MM-DD HH:mm'),
                value: v[index],
              })),
            });
          }
        });
      });
      params.metrics.forEach((t: any) => {
        const chartItem = this.chartData[t]
        chartItem.source = sourceMap[t] || []
        chartItem.loading = false;
        chartItem.loaded = true;
      })
    } else {
      params.metrics.forEach((t: any) => {
        const chartItem = this.chartData[t]
        chartItem.source = []
        chartItem.loading = false;
      })
    }
  }

  private toggleChartHandle () {
    this.showChart = !this.showChart
    this.$emit('on-toggle-chart', this.showChart)
  }

  private toggleFilterActiveHandle () {
    this.filterActive = !this.filterActive
    this.$emit('on-toggle-filter', this.filterActive)
    this.showChartTypes.forEach(t => {
      this.$nextTick(() => {
        const $chart = this.$refs[t.value] as BasicChart[]
        if ($chart && $chart[0]) {
          $chart[0].resize()
        }
      });
    })
  }
}
</script>

<style lang="scss" scoped>
.chart-group {
  padding: 16px;
  background: var(--bg-color);
  color: var(--color-text-primary);

  .group-header {
    line-height: 22px;

    .group-title {
      cursor: pointer;
      font-size: 16px;
      line-height: 22px;
      .el-icon-arrow-down {
        transition: all 0.3s;
        &.hide {
          transform: rotate(-90deg);
        }
      }
    }
  }

  .group-cont {
    margin-top: 10px;
    height: 250px;
  }

  .chart-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    width: calc((100% - 32px) / 3);
    height: 100%;
    border: 1px solid var(--border-color-light);
    & + .chart-item {
      margin-left: 16px;
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
    }
  }

  &.chart-group-detail {
    padding: 0;
    .group-cont {
      height: 220px;
    }
    .chart-title {
      height: 26px;
      padding: 10px 12px 0;
      font-size: 13px;
      line-height: 16px;
    }
    .chart-cont {
      padding: 0;
    }
  }
}
</style>
