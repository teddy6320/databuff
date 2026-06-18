<template>
  <div class="chart-group-wrap">
    <div class="chart-group-cont flex-h">
      <div
        v-for="item in chartTypes"
        :key="item.value"
        v-loading="chartData[item.value].loading"
        class="chart-group-item">
        <div class="chart-title">{{ item.labelKey ? $t(item.labelKey) : item.label }}
          <div>
            <el-select
              v-model="chartData[item.value].order"
              @change="getChartItemData(item.value)"
              size="mini" class="chart-select">
              <el-option label="Top" value="top"></el-option>
              <el-option label="Bottom" value="bottom"></el-option>
            </el-select>
            <el-select
              v-model="chartData[item.value].limit"
              @change="getChartItemData(item.value)"
              size="mini" class="chart-select ml-6 w65">
              <el-option label="5" :value="5"></el-option>
              <el-option label="10" :value="10"></el-option>
              <el-option label="20" :value="20"></el-option>
            </el-select>
          </div>
        </div>
        <div class="chart-cont">
          <basic-chart
            :ref="item.value"
            :source="chartData[item.value].source"
            :showEmpty="!chartData[item.value].loading && !chartData[item.value].source.length"
            :showAxisLabelCount="6"
            :showLegend="true"
            :tooltipEnterable="true">
          </basic-chart>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import ServiceApi from '@/api/service';
import { toAsyncWait } from '@/utils/common';

@Component
export default class ChartGroup extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;

  private chartTypes: any[] = [
    { label: i18n.t('modules.views.appMonitor.cache.s_96a0c062') as string, labelKey: 'modules.views.appMonitor.cache.s_96a0c062', value: 'avgTime', unit: 'ns' },
    { label: i18n.t('modules.views.appMonitor.relationMap.s_ae1e7b60') as string, labelKey: 'modules.views.appMonitor.relationMap.s_ae1e7b60', value: 'reqCount' },
    { label: i18n.t('modules.views.appMonitor.cache.s_0c8524d7') as string, labelKey: 'modules.views.appMonitor.cache.s_0c8524d7', value: 'errRate', unit: '%' },
  ]

  private chartData: any = {}

  private created () {
    this.chartTypes.forEach(t => {
      this.$set(this.chartData, t.value, {
        ...t,
        loading: false,
        loaded: false,
        order: 'top',
        limit: 10,
        source: [],
      });
    });
  }

  public getData () {
    Object.values(this.chartData).forEach((chartItem: any) => {
      chartItem.loading = false;
      chartItem.loaded = false;
    });
    this.getChartData()
  }

  private async getChartData () {
    this.chartTypes.forEach(t => {
      if (!this.chartData[t.value].loaded) {
        this.getChartItemData(t.value);
      }
    });
  }

  private async getChartItemData (metric: string) {
    const chartItem = this.chartData[metric]
    const { fromTime, toTime, interval } = this.timeParams
    const params: any = {
      startTime: fromTime,
      endTime: toTime,
      interval,
      type: 'topService',
      sortOrder: chartItem.order !== 'bottom' ? 'desc' : 'asc',
      limit: chartItem.limit,
      metric,
    }
    Object.keys(this.queryParams).forEach(k => {
      if (this.queryParams[k] !== '') {
        params[k] = this.queryParams[k];
      }
    });
    chartItem.loading = true;
    const { result, error } = await toAsyncWait(ServiceApi.getServiceListTrendChart(params));
    chartItem.loading = false;
    if (!error) {
      const data: any[] = (result.data || []).filter((item: any) => item?.columns?.length > 1 && item?.values?.length);
      chartItem.source = data.map((item: any) => ({
        name: item.tags?.service || '',
        unit: chartItem.unit || '',
        data: item.values.map(([key, value]: any) => ({
          key: dayjs(Number(key)).format('YYYY-MM-DD HH:mm'),
          value,
        })),
      }));
      chartItem.loaded = true;
    } else {
      chartItem.source = [];
      chartItem.loaded = false;
    }
  }
}
</script>

<style lang="scss" scoped>
.chart-group-wrap {
  padding-top: 16px;

  .chart-group-cont {
    height: 250px;
  }

  .chart-group-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: 100%;
    border: 1px solid var(--border-color-light);
    border-radius: 4px;
    overflow: hidden;
    & + .chart-group-item {
      margin-left: 16px;
    }

    .chart-title {
      flex: none;
      padding: 10px 16px 6px;
      font-size: 14px;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .chart-select {
      width: 90px;
      &.w65 {
        width: 65px;
      }
    }

    .chart-cont {
      flex: 1;
      padding: 0 8px;
    }
  }
}
</style>
