<template>
  <div v-if="normalizedCharts.length" class="ai-trend-charts">
    <div
      v-for="(chart, index) in normalizedCharts"
      :key="`${chart.title}_${chart.seriesName}_${index}`"
      class="ai-trend-chart-panel"
    >
      <div class="ai-trend-chart-head">
        <div class="ai-trend-chart-title ell">{{ chart.titleKey ? $t(chart.titleKey) : chart.title }}</div>
        <div v-if="chart.unit" class="ai-trend-chart-unit">{{ chart.unit }}</div>
      </div>
      <div class="ai-trend-chart-body">
        <basic-chart
          :source="[chart.source]"
          :show-empty="!chart.source.data.length"
          :show-legend="false"
          :compact-grid="true"
          :brush-mode="false"
          :tooltip-confine="true"
          :line-width="2"
          :show-axis-label-count="6"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import i18n from '@/i18n';
import BasicChart from '@/components/charts/basic-chart.vue';

interface TrendChartSource {
  name: string;
  type: string;
  smooth: boolean;
  area: boolean;
  color: string;
  symbolSize: number;
  unit: string;
  data: Array<{ key: string; value: number }>;
}

interface TrendChartView {
  title: string;
  seriesName: string;
  unit: string;
  source: TrendChartSource;
}

@Component({
  name: 'AiTrendCharts',
  components: { BasicChart },
})
export default class AiTrendCharts extends Vue {
  @Prop({ default: () => [] })
  private charts!: Array<Record<string, unknown>>;

  private get normalizedCharts (): TrendChartView[] {
    return (this.charts || [])
      .map((item) => this.normalizeChart(item))
      .filter((item): item is TrendChartView => Boolean(item));
  }

  private normalizeChart (chart: Record<string, unknown> | null | undefined): TrendChartView | null {
    if (!chart || chart.success === false) {
      return null;
    }
    const labels = Array.isArray(chart.labels) ? chart.labels : [];
    const values = Array.isArray(chart.values) ? chart.values : [];
    const data = labels.map((label, index) => ({
      key: String(label ?? ''),
      value: this.toNumber(values[index]),
    })).filter(item => item.key);
    if (!data.length) {
      return null;
    }
    const title = this.firstText(chart.title, i18n.t('modules.views.aiPlatform.chat.s_1a57bc0c') as string);
    const seriesName = this.firstText(chart.seriesName, title);
    const unit = this.firstText(chart.unit, '');
    return {
      title,
      seriesName,
      unit,
      source: {
        name: seriesName,
        type: 'line',
        smooth: true,
        area: true,
        color: this.chartColor(title, seriesName),
        symbolSize: 3,
        unit,
        data,
      },
    };
  }

  private firstText (value: unknown, fallback: string): string {
    const text = String(value ?? '').trim();
    return text || fallback;
  }

  private toNumber (value: unknown): number {
    const numberValue = Number(value);
    return Number.isFinite(numberValue) ? numberValue : 0;
  }

  private chartColor (title: string, seriesName: string): string {
    const colors = ['#2563eb', '#0f9f75', '#d97706', '#7c3aed', '#dc2626'];
    const seed = `${title}${seriesName}`.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0);
    return colors[seed % colors.length];
  }
}
</script>

<style lang="scss" scoped>
.ai-trend-charts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 14px;
  width: 100%;
}

.ai-trend-chart-panel {
  min-width: 0;
  border: 1px solid #dbe4f0;
  border-radius: 8px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  overflow: hidden;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.ai-trend-chart-head {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 0 14px;
  border-bottom: 1px solid #e5ebf4;
  background: #fff;
}

.ai-trend-chart-title {
  min-width: 0;
  color: #172033;
  font-size: 13px;
  font-weight: 750;
}

.ai-trend-chart-unit {
  flex: none;
  padding: 2px 7px;
  border-radius: 999px;
  color: #516071;
  background: #f1f5f9;
  font-size: 12px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.ai-trend-chart-body {
  height: 238px;
  padding: 10px 12px 12px;
  background: linear-gradient(180deg, rgba(248, 251, 255, 0.72), #fff);
}
</style>
