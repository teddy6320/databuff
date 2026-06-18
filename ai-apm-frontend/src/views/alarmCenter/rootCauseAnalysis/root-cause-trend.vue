<template>
  <div class="root-cause-trend pt-20" :class="{ 'hide-chart': !showChart }">
    <div class="chart-title">
      <div @click="toggleChartHandle(!showChart)" class="chart-title-text">
        <i class="db-icon-down"></i> {{ $t('modules.views.alarmCenter.rootCauseAnalysis.s_13b3fbc7') }}
      </div>
    </div>
    <div v-if="showChart" v-loading="loading" class="chart-cont">
      <piecewise-chart
        :source="chartSource"
        :showLegend="true"
        :minInterval="1"
        :showEmpty="!loading && !chartSource.length"
        :clickEvent="($event) => chartClickHandle($event)"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import dayjs from 'dayjs';
import PiecewiseChart from '@/components/charts/piecewise-chart.vue';

@Component({
  components: {
    PiecewiseChart,
  },
})
export default class RootCauseTrend extends Vue {
  @Prop({ default: () => [] }) private list!: any[];
  @Prop({ default: false }) private loading!: any[];

  private showChart = false;

  get chartSource () {
    if (!this.list.length) {
      return [];
    }
    const start = Math.min(...this.list.map(t => t.start));
    const end = Math.max(...this.list.map(t => t.end));
    return (this.list || []).map(item => {
      const data: any[] = []
      for (let i = start; i <= end; i += 60 * 1000) {
        data.push({
          key: dayjs(i).format('YYYY-MM-DD HH:mm'),
          value: item.start <= i && i <= item.end ? 1 : '-',
        });
      }
      return { name: item.label, data, }
    })
  }

  public toggleChartHandle (show: boolean) {
    this.showChart = show
    this.$emit('on-toggle-chart', this.showChart)
  }

  private chartClickHandle (data: any) {
    this.$emit('chart-click', data.name)
  }
}
</script>

<style lang="scss" scoped>
.root-cause-trend {
  .chart-title {
    height: 16px;
    font-size: 14px;
    line-height: 16px;
    color: var(--color-text-primary);
    .chart-title-text {
      padding-right: 16px;
      display: inline-block;
      cursor: pointer;
    }
    .db-icon-down {
      display: inline-block;
      transition: all 0.3s;
    }
  }

  .chart-cont {
    height: 190px;
  }

  &.hide-chart {
    padding-bottom: 20px;
    .db-icon-down {
      transform: rotate(-90deg);
    }
  }
}
</style>
