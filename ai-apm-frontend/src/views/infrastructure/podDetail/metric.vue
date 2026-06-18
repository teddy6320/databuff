<template>
  <div class="detail-metric">
    <div class="flex-h-jc">
      <div class="section-item">
        <div class="section-title">{{ chartData.cpu.titleKey ? $t(chartData.cpu.titleKey) : chartData.cpu.title }}</div>
        <div class="section-cont">
          <basic-chart
            :source="chartData.cpu.source"
            :minInterval="1"
            :showLegend="true"
            :showEmpty="!chartData.cpu.loading && !chartData.cpu.source.length"
            v-loading="chartData.cpu.loading"
          />
        </div>
      </div>

      <div class="section-item">
        <div class="section-title">{{ chartData.memory.titleKey ? $t(chartData.memory.titleKey) : chartData.memory.title }}</div>
        <div class="section-cont">
          <basic-chart
            :source="chartData.memory.source"
            :showLegend="true"
            :showEmpty="!chartData.memory.loading && !chartData.memory.source.length"
            v-loading="chartData.memory.loading"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import { orderBy } from 'lodash';
import BasicChart from '@/components/charts/basic-chart.vue';
import MetricApi from '@/api/metric';

@Component({
  components: {
    BasicChart,
  },
})
export default class DetailMetric extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: () => ({}) }) private detail!: any;

  private chartData: any = {
    cpu: {
      title: 'CPU',
      metrics: [{
        name: 'kubernetes.cpu.usage.total',
        nameCn: i18n.t('modules.views.infrastructure.clusterDetail.s_ce2ed8c2') as string, nameCnKey: 'modules.views.infrastructure.clusterDetail.s_ce2ed8c2',
        type: 'usage',
        scale: 1e-9,
        unit: 'core',
        color: '#2962FF',
      }, {
        name: 'kubernetes.cpu.usage.pct',
        nameCn: i18n.t('modules.views.infrastructure.clusterDetail.s_41d8b224') as string, nameCnKey: 'modules.views.infrastructure.clusterDetail.s_41d8b224',
        type: 'usage',
        color: '#967EFF',
      }, {
        name: 'kubernetes.cpu.requests',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_ae1e7b60') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_ae1e7b60',
        type: 'requested',
        unit: 'core',
        color: '#00AFF4',
      }, {
        name: 'kubernetes.cpu.limits',
        nameCn: i18n.t('modules.views.infrastructure.clusterDetail.s_df9c9706') as string, nameCnKey: 'modules.views.infrastructure.clusterDetail.s_df9c9706',
        type: 'limit',
        unit: 'core',
        color: '#ED3B3B',
      }],
      loading: false,
      source: [],
    },
    memory: {
      title: i18n.t('modules.views.appMonitor.hotMethods.s_9932551c') as string, titleKey: 'modules.views.appMonitor.hotMethods.s_9932551c',
      metrics: [{
        name: 'kubernetes.memory.rss',
        nameCn: i18n.t('modules.views.infrastructure.clusterDetail.s_ce2ed8c2') as string, nameCnKey: 'modules.views.infrastructure.clusterDetail.s_ce2ed8c2',
        type: 'usage',
        color: '#2962FF',
      }, {
        name: 'kubernetes.memory.requests',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_ae1e7b60') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_ae1e7b60',
        type: 'requested',
        color: '#00AFF4',
      }, {
        name: 'kubernetes.memory.limits',
        nameCn: i18n.t('modules.views.infrastructure.clusterDetail.s_df9c9706') as string, nameCnKey: 'modules.views.infrastructure.clusterDetail.s_df9c9706',
        type: 'limit',
        color: '#ED3B3B',
      }],
      loading: false,
      source: [],
    },
  }

  public getData () {
    this.getChartData()
  }

  private async getChartData () {
    const { clusterId, namespaceName, podName, fromTime, toTime, interval } = this.queryParams
    const params = {
      query: {
        types: [],
        metric: '',
        by: [],
        aggs: 'sum',
        from: [
          { left: 'clusterId', operator: '=', right: clusterId, connector: 'AND' },
          { left: 'kube_namespace', operator: '=', right: namespaceName, connector: 'AND' },
          { left: 'pod_name', operator: '=', right: podName, connector: 'AND' },
        ],
      },
      start: Math.floor(+new Date(fromTime) / 1000),
      end: Math.floor(+new Date(toTime) / 1000),
      interval,
    }
    const cpuData = this.chartData.cpu
    const memoryData = this.chartData.memory
    this.getChartSource(cpuData.metrics, params, cpuData)
    this.getChartSource(memoryData.metrics, params, memoryData)
  }
  private getChartSource (metrics: any[], params: any, chartDataItem: any, total?: number) {
    chartDataItem.source = []
    metrics.forEach((metric, index) => {
      chartDataItem.loading = true
      MetricApi.getMetricChart({
        ...params,
        query: { A: { ...params.query, metric: metric.name }, expr: 'A' },
      }).then((rst: any) => {
        chartDataItem.loading = false
        if (rst.status === 200 && rst.message.toLocaleLowerCase() === 'success' && Array.isArray(rst.data)) {
          const item = rst.data[0] || {}
          const values: any[] = item.values || []
          if (values.length) {
            const data: any[] = values.map(([key, ...value]: any) => {
              value = value[0]
              value = typeof value === 'number' ? value * (metric.scale || 1) : value
              if (metric.format && typeof value === 'number') {
                value = typeof total === 'number' ? metric.format(value, total) : '-'
              }
              return {
                key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
                value,
              }
            })
            chartDataItem.source.push({
              name: metric.nameCn,
              unit: metric.unit || (item.units || [])[1],
              smooth: true,
              index,
              data,
              color: metric.color,
            })
            chartDataItem.source.sort((a: any, b: any) => a.index - b.index)
          }
        }
      })
      .catch(err => {
        chartDataItem.loading = false
        if (err.message !== 'interrupt') {
          this.$message.error(err.message);
        }
      })
    })
  }
}
</script>

<style lang="scss" scoped>
.section-item {
  width: calc(50% - 8px);
  height: 268px;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  color: var(--color-text-primary);

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
