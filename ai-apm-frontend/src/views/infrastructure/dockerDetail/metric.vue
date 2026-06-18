<template>
  <div class="detail-metric" :class="{ 'detail-metric-loading': chartLoading }" v-loading="chartLoading">
    <template v-for="(item, index) in chartData">
      <div v-if="!item.empty" :key="index" class="section-item">
        <div class="section-title">{{ item.titleKey ? $t(item.titleKey) : item.title }}</div>
        <div class="section-cont">
          <basic-chart
            :source="item.source"
            :showLegend="item.source.length > 1"
            :showEmpty="!item.loading && !item.source.length"
            v-loading="item.loading"
          />
        </div>
      </div>
    </template>

    <div class="empty-show" v-if="!chartLoading && chartEmpty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import { orderBy } from 'lodash';
import BasicChart from '@/components/charts/basic-chart.vue';
import { toAsyncWait } from '@/utils/common';
import MetricApi from '@/api/metric';

@Component({
  components: {
    BasicChart,
  },
})
export default class DetailMetric extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;

  private chartData: any[] = [
    {
      title: i18n.t('modules.views.infrastructure.dockerDetail.s_1bec817a') as string, titleKey: 'modules.views.infrastructure.dockerDetail.s_1bec817a',
      metrics: [{
        name: 'docker.cpu.usage',
        nameCn: i18n.t('modules.views.infrastructure.dockerDetail.s_1bec817a') as string, nameCnKey: 'modules.views.infrastructure.dockerDetail.s_1bec817a',
      }],
      aggregation: 'avg',
      loading: true,
      source: [],
      empty: false,
    },
    {
      title: i18n.t('modules.views.infrastructure.dockerDetail.s_198d2669') as string, titleKey: 'modules.views.infrastructure.dockerDetail.s_198d2669',
      metrics: [{
        name: 'docker.data.free',
        nameCn: i18n.t('modules.views.infrastructure.dockerDetail.s_198d2669') as string, nameCnKey: 'modules.views.infrastructure.dockerDetail.s_198d2669',
      }],
      aggregation: 'max',
      loading: true,
      source: [],
      empty: false,
    },
    {
      title: i18n.t('modules.views.appMonitor.relationMap.s_2c8a6d17') as string, titleKey: 'modules.views.appMonitor.relationMap.s_2c8a6d17',
      metrics: [{
        name: 'docker.data.percent',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_2c8a6d17') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_2c8a6d17',
      }],
      aggregation: 'avg',
      loading: true,
      source: [],
      empty: false,
    },
    {
      title: i18n.t('modules.views.infrastructure.dockerDetail.s_88ff6d7b') as string, titleKey: 'modules.views.infrastructure.dockerDetail.s_88ff6d7b',
      metrics: [{
        name: 'docker.mem.limit',
        nameCn: i18n.t('modules.views.infrastructure.dockerDetail.s_88ff6d7b') as string, nameCnKey: 'modules.views.infrastructure.dockerDetail.s_88ff6d7b',
      }],
      aggregation: 'avg',
      loading: true,
      source: [],
      empty: false,
    },
    {
      title: i18n.t('modules.views.appMonitor.relationMap.s_31cb8d97') as string, titleKey: 'modules.views.appMonitor.relationMap.s_31cb8d97',
      metrics: [{
        name: 'docker.mem.in_use',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_31cb8d97') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_31cb8d97',
      }],
      aggregation: 'avg',
      loading: true,
      source: [],
      empty: false,
    },
    {
      title: i18n.t('modules.views.infrastructure.dockerDetail.s_a2d519e8') as string, titleKey: 'modules.views.infrastructure.dockerDetail.s_a2d519e8',
      metrics: [{
        name: 'docker.thread.count',
        nameCn: i18n.t('modules.views.infrastructure.dockerDetail.s_a2d519e8') as string, nameCnKey: 'modules.views.infrastructure.dockerDetail.s_a2d519e8',
      }],
      aggregation: 'avg',
      loading: true,
      source: [],
      empty: false,
    },
    {
      title: i18n.t('modules.views.infrastructure.dockerDetail.s_ac53f077') as string, titleKey: 'modules.views.infrastructure.dockerDetail.s_ac53f077',
      metrics: [{
        name: 'docker.io.read_bytes',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_085cf12e') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_085cf12e',
      }, {
        name: 'docker.io.write_bytes',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_ea22704d') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_ea22704d',
      }],
      aggregation: 'avg',
      loading: true,
      source: [],
      empty: false,
    },
    {
      title: i18n.t('modules.views.appMonitor.traceDetail.s_f4055f69') as string, titleKey: 'modules.views.appMonitor.relationMap.s_f4055f69',
      metrics: [{
        name: 'docker.net.bytes_rcvd',
        nameCn: i18n.t('modules.views.infrastructure.dockerDetail.s_15e9238b') as string, nameCnKey: 'modules.views.infrastructure.dockerDetail.s_15e9238b',
      }, {
        name: 'docker.net.bytes_sent',
        nameCn: i18n.t('modules.views.infrastructure.dockerDetail.s_1535fcfa') as string, nameCnKey: 'modules.views.infrastructure.dockerDetail.s_1535fcfa',
      }],
      aggregation: 'avg',
      loading: true,
      source: [],
      empty: false,
    },
  ];

  get chartLoading () {
    return this.chartData.some(t => t.loading)
  }

  get chartEmpty () {
    return !this.chartData.filter(t => !t.empty).length
  }

  public getData () {
    this.getChartData()
  }

  private async getChartData () {
    const { containerId, fromTime, toTime, interval } = this.queryParams
    this.chartData.forEach((item, index) => {
      item.source = []
      item.metrics.forEach((t: any) => {
        this.getChartSource({
          query: { A: {
            metric: t.name,
            aggs: item.aggregation,
            from: [{ left: 'container_id', operator: '=', right: containerId, connector: 'AND' }],
            by: [],
            types: [],
          }, expr: 'A' },
          start: Math.floor(+new Date(fromTime) / 1000),
          end: Math.floor(+new Date(toTime) / 1000),
          interval,
        }, index, t)
      })
    })
  }
  private async getChartSource (params: any, idx: number, metric: any) {
    this.chartData[idx].loading = true
    const { result, error } = await toAsyncWait(MetricApi.getMetricChart(params));
    this.chartData[idx].loading = false
    if (!error) {
      const metrics: string[] = this.chartData[idx].metrics.map((t: any) => t.nameCn)
      const data = (result?.data || [])[0] || {}
      const values: any[] = data.values || []
      const source = [
        ...this.chartData[idx].source,
        {
          name: metric.nameCn,
          unit: (data.units || [])[1],
          smooth: true,
          area: true,
          data: values.map(([key, ...value]: any) => ({
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value: value[0],
          })),
        }
      ].sort((a, b) => {
        return metrics.findIndex(t => t === a.name) - metrics.findIndex(t => t === b.name)
      })
      this.chartData[idx].source = source
      this.chartData[idx].empty = !source.filter(t => t.data.length).length
    } else {
      const source = [...this.chartData[idx].source]
      this.chartData[idx].empty = !source.filter(t => t.data.length).length
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-metric {
  height: 100%;
  overflow: auto;
  position: relative;

  &.detail-metric-loading {
    overflow: hidden;
    .section-item {
      opacity: 0;
    }
  }

  .empty-show {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--color-text-regular);
    font-size: 14px;
    position: absolute;
    top: 0;
    left: 0;
  }
}

.section-item {
  margin-bottom: 16px;
  width: calc(50% - 8px);
  height: 238px;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  display: inline-block;
  vertical-align: top;
  color: var(--color-text-primary);

  &:nth-child(2n) {
    margin-left: 16px;
  }

  &:nth-last-child(1),
  &:nth-last-child(2) {
    margin-bottom: 0;
  }

  .section-title {
    display: flex;
    justify-content: space-between;
    padding: 16px 20px 0;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
  }

  .section-cont {
    height: calc(100% - 38px);
    padding: 0 10px;
  }
}
</style>
