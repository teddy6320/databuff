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

    <div class="section-item pods-section-item mt-16">
      <div class="section-title">Workloads</div>
      <div class="section-sub-title">{{ chartData.pods.titleKey ? $t(chartData.pods.titleKey) : chartData.pods.title }}</div>
      <div class="section-cont">
        <basic-chart
          :source="chartData.pods.source"
          :showLegend="true"
          :colors="['#2962FF', '#00AFF4', '#D1B567', '#1FC2BA']"
          :showEmpty="!chartData.pods.loading && !chartData.pods.source.length"
          :tooltipFormat="podsChartTooltipFormat"
          v-loading="chartData.pods.loading"
        />
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

  private chartData: any = {
    cpu: {
      title: 'CPU',
      metrics: [{
        name: 'kubernetes.cpu.requests',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_ae1e7b60') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_ae1e7b60',
        type: 'requested',
        unit: 'core',
      }, {
        name: 'kubernetes.cpu.limits',
        nameCn: i18n.t('modules.views.infrastructure.clusterDetail.s_df9c9706') as string, nameCnKey: 'modules.views.infrastructure.clusterDetail.s_df9c9706',
        type: 'limit',
        unit: 'core',
      }],
      loading: false,
      source: [],
    },
    memory: {
      title: i18n.t('modules.views.appMonitor.hotMethods.s_9932551c') as string,
      metrics: [{
        name: 'kubernetes.memory.requests',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_ae1e7b60') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_ae1e7b60',
        type: 'requested',
      }, {
        name: 'kubernetes.memory.limits',
        nameCn: i18n.t('modules.views.infrastructure.clusterDetail.s_df9c9706') as string, nameCnKey: 'modules.views.infrastructure.clusterDetail.s_df9c9706',
        type: 'limit',
      }],
      loading: false,
      source: [],
    },
    pods: {
      title: 'Pods',
      metrics: [{
        name: 'kubernetes.pods.running',
        nameCn: i18n.t('modules.views.infrastructure.clusterDetail.s_fdaa7484') as string, nameCnKey: 'modules.views.infrastructure.clusterDetail.s_fdaa7484',
      }],
      loading: false,
      source: [],
    },
  }

  public getData () {
    this.getChartData()
    this.getPodsChartData()
  }

  private async getChartData () {
    const { clusterId, namespaceName, fromTime, toTime, interval } = this.queryParams
    const params = {
      query: {
        types: [],
        metric: '',
        by: [],
        aggs: 'sum',
        from: [
          { left: 'clusterId', operator: '=', right: clusterId, connector: 'AND' },
          { left: 'kube_namespace', operator: '=', right: namespaceName, connector: 'AND' },
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

  private async getPodsChartData () {
    const chartDataItem: any = this.chartData.pods
    const metric = chartDataItem.metrics[0]
    const { clusterId, namespaceName, fromTime, toTime, interval } = this.queryParams
    const group = 'kube_ownerref_kind'
    const params = {
      query: { A: {
        types: [],
        metric: metric.name,
        by: [group],
        aggs: 'sum',
        from: [
          { left: 'clusterId', operator: '=', right: clusterId, connector: 'AND' },
          { left: 'kube_namespace', operator: '=', right: namespaceName, connector: 'AND' },
        ],
      }, expr: 'A' },
      start: Math.floor(+new Date(fromTime) / 1000),
      end: Math.floor(+new Date(toTime) / 1000),
      interval,
    }
    chartDataItem.source = []
    chartDataItem.loading = true
    MetricApi.getMetricChart(params).then((rst: any) => {
      chartDataItem.loading = false
      if (rst.status === 200 && rst.message.toLocaleLowerCase() === 'success' && Array.isArray(rst.data)) {
        const sourceList: any[] = []
        rst.data.forEach((item: any) => {
          const values: any[] = item.values || []
          if (values.length) {
            sourceList.push({
              name: (item.tags || {})[group] || '其他',
              type: 'bar',
              stack: 'total',
              data: values.map(([key, ...value]: any) => {
                value = value[0]
                return {
                  key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
                  value: typeof value === 'number' ? value * (metric.scale || 1) : value,
                }
              }),
            })
          }
        })
        chartDataItem.source = orderBy(sourceList, ['name'], ['asc']);
      }
    }).catch(err => {
      chartDataItem.loading = false
      if (err.message !== 'interrupt') {
        this.$message.error(err.message);
      }
    })
  }

  private podsChartTooltipFormat (params: any[], tipStrs: string[], valueFormat: (val: number, unit?: string) => string) {
    const values = params.map((item: any) => item.value).filter(t => typeof t === 'number')
    const total = values.length ? values.reduce((a: any, b: any) => a + b, 0) : '-';
    return [...tipStrs, `<div style="overflow:hidden;margin-top:4px;line-height:18px;">
        <span style="display:inline-block;margin-right:14px"></span> 总数
        <span style="float:right;margin-left:20px;font-weight:bold;">${valueFormat(total)}</span>
      </div>`]
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

.pods-section-item {
  width: 100%;

  .section-sub-title {
    padding: 8px 20px 0;
    font-size: 12px;
    line-height: 14px;
  }

  .section-cont {
    height: calc(100% - 60px);
  }
}
</style>
