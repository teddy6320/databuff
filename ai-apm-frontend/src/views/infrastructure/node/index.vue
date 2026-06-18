<template>
  <div
    v-loading="clusterLoading"
    class="db-node-wrapper">
    <div class="db-node-chart">
      <div class="chart-item">
        <div class="chart-title">{{ chartData.cpu.titleKey ? $t(chartData.cpu.titleKey) : chartData.cpu.title }}</div>
        <div class="chart-cont">
          <basic-chart
            :source="chartData.cpu.source"
            :minInterval="1"
            :showLegend="true"
            :showEmpty="!chartData.cpu.loading && !chartData.cpu.source.length"
            v-loading="chartData.cpu.loading"
          />
        </div>
      </div>

      <div class="chart-item">
        <div class="chart-title">{{ chartData.memory.titleKey ? $t(chartData.memory.titleKey) : chartData.memory.title }}</div>
        <div class="chart-cont">
          <basic-chart
            :source="chartData.memory.source"
            :showLegend="true"
            :showEmpty="!chartData.memory.loading && !chartData.memory.source.length"
            v-loading="chartData.memory.loading"
          />
        </div>
      </div>
    </div>

    <div class="db-list">
      <db-table
        :queryApi='queryApi'
        :queryParams='getQueryParams'
        :timeMode="false"
        :autoRefresh="false"
        :offsetMode='true'
        :columnConfig='columnConfig'
        @on-table-inited='tableInitedHandle'
        @sort-change='refresh'
        :formatFunc='formatFunc'
        showSetting
        tableKey='INFRA_NODE_LIST'
        @row-click="jumpNodeDetail"
        :row-style="{ cursor: 'pointer' }"
        ref='listTable'>
        <template slot="suffix">
          <el-table-column :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" width="90">
            <template slot-scope="{ row }">
              <a v-if="canDrillDown(row)"
                @click.stop="jumpHostDetail(row)"
                class="table-item-with-action blue cphu"
              >{{ $t('modules.views.infrastructure.node.s_e707a9aa') }}</a>
              <span v-else>{{ $t('modules.views.infrastructure.node.s_e707a9aa') }}</span>
            </template>
          </el-table-column>
        </template>
      </db-table>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import { toAsyncWait } from '@/utils/common';
import BasicChart from '@/components/charts/basic-chart.vue';
import KubernetesApi from '@/api/kubernetes';
import MetricApi from '@/api/metric';

@Component({
  components: {
    BasicChart,
  },
})
export default class Node extends Vue {
  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private clusterData: any = {}

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
      }, {
        name: 'kubernetes.cpu.usage.total',
        nameCn: i18n.t('modules.views.infrastructure.clusterDetail.s_ad6b7038') as string, nameCnKey: 'modules.views.infrastructure.clusterDetail.s_ad6b7038',
        type: 'available',
        scale: 1e-9,
        unit: 'core',
        format: (val: number, total: number) => total - val,
        color: '#08BE7E',
      }],
      loading: false,
      source: [],
    },
    memory: {
      title: i18n.t('modules.views.appMonitor.hotMethods.s_9932551c') as string,
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
      }, {
        name: 'kubernetes.memory.rss',
        nameCn: i18n.t('modules.views.infrastructure.clusterDetail.s_ad6b7038') as string, nameCnKey: 'modules.views.infrastructure.clusterDetail.s_ad6b7038',
        type: 'available',
        format: (val: number, total: number) => total - val,
        color: '#08BE7E',
      }],
      loading: false,
      source: [],
    },
  }

  private clusterId: string = ''

  private timeParams: any = {}

  private columnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 160, defaultSort: 'asc', handleClick: this.jumpNodeDetail, canClick: this.canDrillDown },
    { field: 'internalIP', label: i18n.t('modules.views.configManage.entity.s_2dc9105c') as string, labelKey: 'modules.views.configManage.entity.s_2dc9105c', minWidth: 120, },
    { field: 'status', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', minWidth: 80, },
    { field: 'cpuUsage', label: 'CPU usage', unit: 'percent', type: 'progress', minWidth: 90, },
    { field: 'cpuRequest', label: 'CPU requests', unit: 'count', suffix: ' core', minWidth: 90, },
    { field: 'cpuLimit', label: 'CPU limits', unit: 'count', suffix: ' core', minWidth: 90, },
    { field: 'cpuAvailable', label: i18n.t('modules.views.infrastructure.clusterDetail.s_be5c3255') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_be5c3255', unit: 'count', suffix: ' core', minWidth: 90, },
    { field: 'memoryUsage', label: i18n.t('modules.views.infrastructure.clusterDetail.s_a1cfd1b6') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_a1cfd1b6', unit: 'percent', type: 'progress', minWidth: 90, },
    { field: 'memoryRequest', label: i18n.t('modules.views.infrastructure.cluster.s_c43bdd6e') as string, labelKey: 'modules.views.infrastructure.cluster.s_c43bdd6e', unit: 'b', minWidth: 90, },
    { field: 'memoryLimit', label: i18n.t('modules.views.infrastructure.clusterDetail.s_306b4b1d') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_306b4b1d', unit: 'b', minWidth: 90, },
    { field: 'memoryAvailable', label: i18n.t('modules.views.infrastructure.clusterDetail.s_14eceb82') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_14eceb82', unit: 'b', minWidth: 90, },
  ]

  private queryApi = KubernetesApi.getNodeList

  get getQueryParams () {
    return {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      clusterId: this.clusterId || '',
    };
  }

  private created() {
    const { kid = '' } = this.$route.query;
    this.clusterId = decodeURIComponent(kid as string);
  }

  private mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
  }

  private beforeDestroy () {
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh')
  }

  private async getChartData () {
    const { clusterId, fromTime, toTime, interval } = this.getQueryParams
    const params = {
      query: {
        types: [],
        metric: '',
        by: [],
        aggs: 'sum',
        from: [{ left: 'clusterId', operator: '=', right: clusterId, connector: 'AND' }],
      },
      start: Math.floor(+new Date(fromTime) / 1000),
      end: Math.floor(+new Date(toTime) / 1000),
      interval,
    }
    const cpuData = this.chartData.cpu
    const memoryData = this.chartData.memory
    this.getChartSource(cpuData.metrics, params, cpuData, this.clusterData._cpuCapacity || 0)
    this.getChartSource(memoryData.metrics, params, memoryData, this.clusterData._memoryCapacity || 0)
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
            if (metric.name === 'kubernetes.memory.rss' && metric.type === 'usage') {
              chartDataItem.source.push({
                name: i18n.t('modules.views.infrastructure.clusterDetail.s_41d8b224') as string, nameKey: 'modules.views.infrastructure.clusterDetail.s_41d8b224',
                unit: '%',
                smooth: true,
                index: 1,
                color: '#967EFF',
                data: data.map((t) => ({
                  key: t.key,
                  value: typeof t.value === 'number' && total ? t.value / total * 100 : '-',
                })),
              })
            }
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

  private tableInitedHandle () {
    this.durationChangeHandle()
  }

  private formatFunc (data: any) {
    data.forEach((t: any) => {
      const capacity = (t.status || {}).capacity || {}
      const cpu = (+capacity.cpu || 0) / 1000
      const memory = +capacity.memory || 0
      const isMaster = (t.labels || []).join(',').toLocaleLowerCase().includes('master')
      t.id = t.uid
      t.internalIP = ((t.status || {}).nodeAddresses || {}).InternalIP || '-'
      t.status = (t.status || {}).status || '-'
      t.cpuUsage = typeof t.cpuUsage === 'number' && cpu !== 0 ? t.cpuUsage / cpu : '-'
      if (isMaster && t.cpuUsage === 0) {
        t.cpuUsage = '-'
      }
      t.cpuAvailable = cpu - t.cpuRequest
      t.memoryUsage = typeof t.memUsage === 'number' && memory !== 0 ? t.memUsage / memory : '-'
      t.memoryRequest = t.memRequest || t.memRequest === 0 ? t.memRequest || 0 : '-'
      t.memoryLimit = t.memLimit || t.memLimit === 0 ? t.memLimit || 0 : '-'
      t.memoryAvailable = memory - t.memUsage
    });
  }

  public refresh () {
    (this.$refs.listTable as any)?.refresh()
  }

  // 时间范围改变
  private async durationChangeHandle () {
    this.timeParams = { ...this.getGlobalTimeV2() }
    if (!this.clusterId) {
      return
    }
    await this.getClusterDetail()
    this.getChartData()
    this.refresh()
  }

  // 获取集群详情
  private clusterLoading = false
  private async getClusterDetail () {
    const { fromTime, toTime, interval, clusterId } = this.getQueryParams
    const params: any = {
      clusterId,
      fromTime,
      toTime,
      interval,
      offset: 0,
      size: 1,
    }
    this.clusterLoading = true;
    const { result, error } = await toAsyncWait(KubernetesApi.getClusterList(params));
    this.clusterLoading = false;
    if (!error) {
      const data = (result.data || [])[0]
      if (!data) {
        this.clusterData = {}
        return
      }
      this.clusterData = {
        id: data.clusterId,
        _cpuCapacity: (+data.cpuCapacity || 0) / 1000,
        _memoryCapacity: +data.memoryCapacity,
      }
    }
  }

  // 跳转到Node详情
  private jumpNodeDetail (data: any) {
    if (!this.canDrillDown(data)) {
      return
    }
    this.$router.push({
      path: '/infrastructure/nodeDetail',
      query: {
        kid: encodeURIComponent(data.id as string),
        cid: encodeURIComponent(data.clusterId as string),
        kn: encodeURIComponent(data.name as string),
      }
    })
  }

  // 跳转到主机详情
  private jumpHostDetail (data: any) {
    if (!this.canDrillDown(data)) {
      return
    }
    this.$router.push({
      path: '/infrastructure/hostDetail',
      query: { hostName: encodeURIComponent(data.hostName), }
    })
  }

  private canDrillDown (row: any) {
    return row.status.toLowerCase() !== 'notready' && row.status !== '-'
  }
}
</script>

<style lang="scss" scoped>
.db-node-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .db-node-chart {
    padding: 20px 20px 0;
    background-color: var(--bg-color);
    border-radius: 4px 4px 0 0;
    display: flex;
    justify-content: space-between;

    .chart-item {
      width: calc(50% - 8px);
      height: 268px;
      border: 1px solid var(--border-color-lighter);
      border-radius: 4px;
    }

    .chart-title {
      padding: 16px 20px 0;
      font-size: 14px;
      font-weight: 500;
      line-height: 22px;
      color: var(--color-text-primary);
    }

    .chart-cont {
      height: calc(100% - 38px);
      padding: 0 10px;
    }
  }

  .db-list {
    flex: 1;
    min-height: 300px;
    height: calc(100% - 288px);
    padding: 12px 20px 20px;
    background-color: var(--bg-color);
    border-radius: 0 0 4px 4px;
  }
}
</style>
