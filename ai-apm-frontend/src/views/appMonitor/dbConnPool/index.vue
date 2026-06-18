<template>
  <div
    v-loading="isLoading"
    class="pool-wrapper">
    <search-group
      ref="searchGroup"
      :timeParams="timeParams"
      @on-change="searchChangeHandle"
      class="search-group"
    />

    <div class="chart-list">
      <div
        v-for="(metric, index) in metricList"
        :key="`${metric}__${index}`"
        v-loading="chartSource[metric].loading"
        class="chart-item">
        <div class="item-title">
          <el-popover
            placement="left-start"
            width="400"
            trigger="hover"
            popper-class="metric-info-popper">
            <div slot="reference" class="tit">
              <div class="name">{{ metric }}</div>
              <div class="desc">{{ $t('modules.views.appMonitor.dbConnPool.s_5eaa60a4', { value0: metricInfoMapping[metric].desc || '-' }) }}</div>
            </div>
            <metric-info-tooltip
              :detail="metricInfoMapping[metric]"
              :tooltip="false"
            />
          </el-popover>
        </div>

        <div class="item-cont">
          <basic-chart
            :source="chartSource[metric].source"
            :showEmpty="!chartSource[metric].loading && !chartSource[metric].source.length"
            group="pool"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Getter } from 'vuex-class';
import dayjs from 'dayjs';
import { orderBy } from 'lodash';
import SearchGroup from './search-group.vue';
import BasicChart from '@/components/charts/basic-chart.vue';
import MetricInfoTooltip from '@/components/metric-info-tooltip.vue';
import { toAsyncWait } from '@/utils/common';
import MetricApi, { formatMetricInfos } from '@/api/metric';
import i18n from '@/i18n';

const PoolMetricTypes = { type1: i18n.t('modules.views.appMonitor.dbConnPool.s_6b522b81') as string, type2: i18n.t('modules.views.appMonitor.dbConnPool.s_1ab064f1') as string, type3: i18n.t('modules.views.appMonitor.dbConnPool.s_a70c4620') as string }
const PoolNameTag = 'connectionPoolName'

@Component({
  components: {
    SearchGroup,
    BasicChart,
    MetricInfoTooltip,
  }
})
export default class DatabaseConnectionPool extends Vue {
  @Getter('globalTime') private globalTimeFunc!: any;
  @Getter('globalTimeInited') private globalTimeInited!: boolean;

  public $refs!: {
    searchGroup: SearchGroup
  }

  get globalTime () {
    return this.globalTimeFunc()
  }
  @Watch('globalTime', { deep: true })
  private watchGlobalTime() {
    if (!this.globalTimeInited) {
      return
    }
    this.durationChangeHandle()
  }

  private timeParams = {
    fromTime: '',
    toTime: '',
    interval: 300,
  }

  private queryParams: any = {}

  private metricList: string[] = []
  private metricInfoMapping: any = {}

  private chartSource: any = {}
  private isLoading = false

  private async created() {
    this.isLoading = true
    this.regetGlobalTime()
    await this.getMetricList()
    this.durationChangeHandle()
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
    this.$eventBus.$off('GlobalRefresh');
  }

  private durationChangeHandle () {
    this.regetGlobalTime()
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        this.queryParams = { ...data }
        this.getChartList()
      }).finally(() => {
        this.isLoading = false
      })
    })
  }

  private regetGlobalTime () {
    const { fromTime, toTime, interval } = this.globalTimeFunc()
    this.timeParams.fromTime = dayjs(fromTime).format('YYYY-MM-DD HH:mm:ss')
    this.timeParams.toTime = dayjs(toTime).format('YYYY-MM-DD HH:mm:ss')
    this.timeParams.interval = interval
  }

  private searchChangeHandle (data: any) {
    if (JSON.stringify(data) === JSON.stringify(this.queryParams)) {
      return
    }
    this.queryParams = { ...data }
    this.getChartList()
  }

  private async getMetricList () {
    const { result, error } = await toAsyncWait(MetricApi.getAllMetricListByQuery(PoolMetricTypes));
    if (!error) {
      const data = result.data || {}
      this.metricInfoMapping = data
      this.metricList = orderBy(Object.keys(data), [t => t.toLocaleLowerCase()], ['asc']);
      this.metricList.forEach(metric => {
        this.$set(this.chartSource, metric, {
          title: metric,
          source: [],
          loading: false,
        });
      })
      this.$store.commit('Common/SET_METRIC_INFOS', formatMetricInfos(data));
    }
  }

  private getChartList () {
    this.metricList.forEach((metric: string) => {
      this.getChartSource(metric)
    });
  }

  private async getChartSource (metric: string) {
    const chartDataItem = this.chartSource[metric]
    const { fromTime, toTime, interval } = this.timeParams
    const { poolName, sid, si } = this.queryParams
    const metricQuery: any = { metric, from: [], types: [], by: [], aggs: '', }
    if (poolName) {
      metricQuery.from.push({ left: PoolNameTag, operator: '=', right: poolName, connector: 'AND' });
    }
    if (sid) {
      metricQuery.from.push({ left: 'serviceId', operator: '=', right: sid, connector: 'AND' });
      if (si) {
        metricQuery.from.push({ left: 'serviceInstance', operator: '=', right: si, connector: 'AND' });
      }
    }
    const params: any = {
      query: { A: metricQuery, expr: 'A' },
      start: Math.floor(+new Date(fromTime) / 1000),
      end: Math.floor(+new Date(toTime) / 1000),
      interval,
    }
    chartDataItem.loading = true
    const { result, error } = await toAsyncWait(MetricApi.getMetricChart(params));
    chartDataItem.loading = false
    if (!error) {
      const data = (result || {}).data || []
      const source: any[] = []
      for (const item of data) {
        const values: any[] = item.values || []
        if (!values.length) {
          continue;
        }
        source.push({
          name: metric,
          unit: (item.units || [])[1] || '',
          area: true,
          data: values.map(([key, value]: any) => ({
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          })),
        })
      }
      chartDataItem.source = source;
    } else {
      chartDataItem.source = [];
    }
  }
}
</script>

<style lang="scss" scoped>
.pool-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .search-group {
    flex-wrap: nowrap;
  }

  .chart-list {
    flex: 1;
    margin-top: 16px;
    min-height: 300px;
    overflow: auto;
  }

  .chart-item {
    display: inline-block;
    vertical-align: top;
    width: calc(50% - 8px);
    height: 280px;
    background-color: var(--bg-color);
    overflow: hidden;

    &:nth-child(n + 3) {
      margin-top: 16px;
    }
    &:nth-child(2n) {
      margin-left: 16px;
    }

    .item-title {
      height: 60px;
      padding: 12px 20px 0;
      font-size: 16px;
      line-height: 28px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      > * {
        vertical-align: top;
      }
      > span {
        display: inline-block;
        max-width: 100%;
      }
      :deep(.el-popover__reference-wrapper),
      .tit {
        display: block;
      }
      .name {
        text-overflow: ellipsis;
        white-space: nowrap;
        overflow: hidden;
      }
      .desc {
        line-height: 20px;
        font-size: 13px;
        color: var(--color-text-secondary);
        text-overflow: ellipsis;
        white-space: nowrap;
        overflow: hidden;
      }
    }

    .item-cont {
      height: calc(100% - 60px);
      padding: 0 10px 5px;
      position: relative;
    }
  }
}
</style>
