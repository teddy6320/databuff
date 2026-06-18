<template>
  <div class="chart-group">
    <div class="group-header flex-h-jc">
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

      <el-popover
        placement="bottom"
        width="280"
        trigger="click"
      >
        <div class="flex-h-jc default-text mb-10">{{ $t('modules.views.npm.analysis.s_b56e72db') }}
          <el-button @click="resetChartTypesHandle(true)" size="mini">{{ $t('modules.views.authorization.s_4b9c3271') }}</el-button>
        </div>
        <ul class="columns-setting">
          <li v-for="item in chartTypes" :key="item.value" class="columns-setting-item">
            <span class="columns-setting-item-label">{{ item.labelKey ? $t(item.labelKey) : item.label }}</span>
            <el-switch
              v-model="chartModels[item.value]"
              @change="chartModelsChangeHandle()"
              :disabled="(!chartModels[item.value] && showChartTypes.length >= 3) ||
                  (chartModels[item.value] && showChartTypes.length === 1)"
              class="columns-setting-switch" />
          </li>
        </ul>
        <span slot="reference" class="list-setting-btn blue cp">
          <i class="btn-icon el-icon-setting"></i> {{ $t('modules.views.appMonitor.service.s_e366ccf1') }}
        </span>
      </el-popover>
    </div>

    <div v-if="showChart" v-loading="queryLoading" class="group-cont flex-h">
      <div
        v-for="item in showChartTypes"
        :key="item.value"
        v-loading="chartData[item.value].loading"
        :style="{ width: chartWidth }"
        class="chart-item">
        <div class="chart-title">{{ item.labelKey ? $t(item.labelKey) : item.label }}</div>
        <div class="chart-cont">
          <basic-chart
            :ref="item.value"
            :source="chartData[item.value].source"
            :showEmpty="!chartData[item.value].loading && !chartData[item.value].source.length"
            :showAxisLabelCount="4"
            :tsSource='chartData[item.value].tsSource'
            @on-ts-tooltip-show='onTsTooltipShow'
          >
            <template slot='ts'>
              <ChartTsSlot :current='currentTsItem' />
            </template>
          </basic-chart>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import dayjs from 'dayjs';
import deepClone from 'lodash/cloneDeep';
import Simplebar from 'simplebar-vue'
import BasicChart from '@/components/charts/basic-chart.vue';
import NpmApi from '@/api/npm';
import { toAsyncWait } from '@/utils/common';
import ChartTsSlot from '@/views/appMonitor/serviceAnalysis/chart-ts-slot.vue';

@Component({
  components: {
    Simplebar: Simplebar as any,
    BasicChart,
    ChartTsSlot,
  },
})
export default class ChartGroup extends Vue {
  @Prop({ default: 'NPM_ChartGroup' }) private groupKey!: string;
  @Prop({ default: () => [] }) private chartTypes!: any[];
  @Prop({ default: () => [] }) private defaultTypes!: string[];
  @Prop({ default: () => ({}) }) private query!: any;
  @Prop({ default: () => ({}) }) private filter!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;
  @Prop({ default: false }) private queryLoading!: boolean;

  private filterActive = true;

  private showChart = true;

  private chartModels: any = {}

  private chartData: any = {}

  private currentTsItem: any = {}

  get showChartTypes () {
    return this.chartTypes.filter(t => this.chartModels[t.value])
  }

  get chartWidth () {
    const length = this.showChartTypes.length
    return `calc((100% - (${length - 1} * 16px)) / ${length})`
  }

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

  @Watch('chartTypes', { deep: true })
  private onChartTypesChange (newVal: any) {
    // 初始化
    this.chartTypes.forEach(t => {
      this.$set(this.chartModels, t.value, false);
      this.$set(this.chartData, t.value, {
        ...t,
        loading: false,
        loaded: false,
        source: [],
        tsSource: [],
      });
    });

    // 获取要显示的图表
    this.getLocalCharts()
    if (!Object.values(this.chartModels).filter(t => t).length) {
      this.resetChartTypesHandle()
    }
  }

  public getData () {
    // 清空load状态
    Object.values(this.chartData).forEach((chartItem: any) => {
      chartItem.loading = false;
      chartItem.loaded = false;
    });
    // 加载图表数据
    this.getChartData()
  }

  private async getChartData () {
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
    this.showChartTypes.forEach(t => {
      const chartItem = this.chartData[t.value]
      if (!chartItem.loading && !chartItem.loaded) {
        params.metrics.push(t.value)
        chartItem.loading = true
      }
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
        // v2.9.1 ++
        const _rootDetails = data
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
          if (chartItem?.tsSource) {
            // @ts-ignore
            chartItem.tsSource = tsSource;
          }
        } else {
          if (chartItem && chartItem?.tsSource) {
            chartItem.tsSource = []
          }
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

  private toggleChartHandle () {
    this.showChart = !this.showChart
    this.$emit('on-toggle-chart', this.showChart)
    if (this.showChart) {
      this.getChartData()
    }
  }

  private chartModelsChangeHandle () {
    this.showChartTypes.forEach(t => {
      this.$nextTick(() => {
        const $chart = this.$refs[t.value] as BasicChart[]
        if ($chart && $chart[0]) {
          $chart[0].resize()
        }
      });
    })
    if (this.showChart) {
      this.getChartData()
    }
    if (window && window.localStorage) {
      const _charts = this.showChartTypes.map(t => t.value)
      if (_charts.join(',') !== this.defaultTypes.join(',')) {
        window.localStorage.setItem(this.groupKey, JSON.stringify(_charts))
      } else {
        window.localStorage.removeItem(this.groupKey); // 删除 localStorage
      }
    }
  }

  private resetChartTypesHandle (loadData = false) {
    Object.keys(this.chartModels).forEach(key => {
      this.chartModels[key] = this.defaultTypes.includes(key)
    })
    if (window && window.localStorage) {
      window.localStorage.removeItem(this.groupKey); // 删除 localStorage
    }
    if (loadData) {
      this.getChartData()
    }
  }

  private getLocalCharts () {
    if (window && window.localStorage) {
      const _chartsStr = window.localStorage.getItem(this.groupKey)
      if (!_chartsStr) {
        return
      }
      try {
        const _charts = JSON.parse(_chartsStr);
        Object.keys(this.chartModels).forEach(key => {
          this.chartModels[key] = _charts.includes(key)
        })
      } catch (err) {
        console.log(err)
      }
    }
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

  private onTsTooltipShow (row: any) {
    this.currentTsItem = row.info || null;
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
}
</style>
