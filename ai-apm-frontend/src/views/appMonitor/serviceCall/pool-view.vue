<template>
  <div class="service-call-view-wrap">
    <search-group
      ref="searchGroup"
      :poolType="poolType"
      :poolList="poolList"
      @on-change="searchChangeHandle"
      class="search-group" />

    <div class="type-list">
      <span
        v-for="item in metricTypes"
        :key="item"
        @click="toggleMetricTypeHandle(item)"
        :class="['type-item', { active: currMetricType === item }]"
      >{{ item | poolMetricType(poolType) }}</span>
    </div>

    <div class="chart-list">
      <div
        v-for="(metric, index) in (poolMetrics[`${poolType}_${currMetricType}`] || [])"
        :key="`${metric}__${index}`"
        v-loading="(chartSource[metric] || {}).loading"
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

        <div v-if="chartSource[metric]" class="item-cont">
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
import i18n from '@/i18n';
import dayjs from 'dayjs';
import SearchGroup from './pool-search-group.vue';
import BasicChart from '@/components/charts/basic-chart.vue';
import MetricInfoTooltip from '@/components/metric-info-tooltip.vue';
import { toAsyncWait } from '@/utils/common';
import MetricApi, { formatMetricInfos } from '@/api/metric';

type MetricType = 'pool' | 'poolGet'

@Component({
  components: {
    SearchGroup,
    BasicChart,
    MetricInfoTooltip,
  },
  filters: {
    poolMetricType (metricType: string, type: string) {
      const _type = `${type}_${metricType}`
      switch (_type) {
        case 'object_pool':
          return i18n.t('modules.views.appMonitor.serviceCall.s_215a7f10') as string;
        case 'httpConn_pool':
        case 'dbConn_pool':
          return i18n.t('modules.views.appMonitor.serviceCall.s_71c5c9ca') as string;
        case 'object_poolGet':
          return i18n.t('modules.views.appMonitor.serviceCall.s_d80c539d') as string;
        case 'httpConn_poolGet':
        case 'dbConn_poolGet':
          return i18n.t('modules.views.appMonitor.serviceCall.s_f77746f1') as string;
        default:
          return metricType || '-'
      }
    },
  },
})
export default class PoolView extends Vue {
  @Prop({ default: '' }) private poolType!: string;
  @Prop({ default: () => [] }) private poolList!: string[];
  @Prop({ default: () => ({}) }) private poolMetrics!: any;
  @Prop({ default: () => ({}) }) private metricInfoMapping!: any;
  @Prop({ default: () => ({}) }) private params!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;

  public $refs!: {
    searchGroup: SearchGroup
  }

  private metricTypes: MetricType[] = ['pool', 'poolGet']

  private currMetricType: MetricType = 'pool'

  private queryParams: any = {}

  private chartSource: any = {}

  private created() {
    const { metricType } = this.$route.query
    if (this.metricTypes.includes(metricType as MetricType)) {
      this.currMetricType = metricType as MetricType
    }
  }

  public getData () {
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        this.queryParams = { ...data }
        this.getChartList()
      })
    })
  }

  private searchChangeHandle (data: any) {
    if (JSON.stringify(data) === JSON.stringify(this.queryParams)) {
      return
    }
    this.queryParams = { ...data }
    this.getChartList()
  }

  private getChartList () {
    this.getMetricList(this.poolType, this.currMetricType).then((list: string[]) => {
      list.forEach((metric: string) => {
        this.$set(this.chartSource, metric, {
          title: metric,
          source: [],
          loading: false,
        });
        this.getChartSource(metric, this.currMetricType === 'poolGet')
      });
    });
  }

  private async getChartSource (metric: string, isPoolGet: boolean) {
    const chartDataItem = this.chartSource[metric]
    const { fromTime, toTime, interval } = this.timeParams
    const params: any = {
      query: { A: { metric, types: [], by: [], aggs: '', from: this.getFrom(isPoolGet) }, expr: 'A' },
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

  private getFrom (isPoolGet: boolean) {
    const { componentType, serviceId, srcServiceId } = this.params
    const { poolName, srcServiceInstance, serviceInstance } = this.queryParams
    const from: any[] = []
    if (isPoolGet) {
      from.push({ left: 'poolComponentType', operator: '=', right: componentType, connector: 'AND' });
      from.push({ left: 'serviceId', operator: '=', right: serviceId, connector: 'AND' });
      from.push({ left: 'srcServiceId', operator: '=', right: srcServiceId, connector: 'AND' });
      if (serviceInstance) {
        from.push({ left: 'serviceInstance', operator: '=', right: serviceInstance, connector: 'AND' });
      }
      if (srcServiceInstance) {
        from.push({ left: 'srcServiceInstance', operator: '=', right: srcServiceInstance, connector: 'AND' });
      }
    } else {
      from.push({ left: 'serviceId', operator: '=', right: srcServiceId, connector: 'AND' });
      if (srcServiceInstance) {
        from.push({ left: 'serviceInstance', operator: '=', right: srcServiceInstance, connector: 'AND' });
      }
    }
    if (poolName) {
      const poolNameTag = isPoolGet ? 'poolName' : this.poolType === 'object' ? 'objectPoolName' :
          this.poolType === 'httpConn' ? 'httpConnectionPoolName' : 'connectionPoolName'
      from.push({ left: poolNameTag, operator: '=', right: poolName, connector: 'AND' });
    }
    return from
  }

  private async getMetricList (type: string, metricType: MetricType) {
    const key = `${type}_${metricType}`
    if (this.poolMetrics[key]) {
      return this.poolMetrics[key]
    }
    const isPoolGet = metricType === 'poolGet';
    const type3s = type === 'object' ? [i18n.t('modules.views.appMonitor.objectPool.s_be3f9ead') as string, i18n.t('modules.views.appMonitor.serviceCall.s_af673d4a') as string] :
        type === 'httpConn' ? [i18n.t('modules.views.appMonitor.serviceCall.s_ec025f7b') as string, i18n.t('modules.views.appMonitor.serviceCall.s_211d2a8e') as string] : [i18n.t('modules.views.appMonitor.dbConnPool.s_a70c4620') as string, i18n.t('modules.views.appMonitor.serviceCall.s_a346b966') as string]
    const params = { type1: i18n.t('modules.views.appMonitor.dbConnPool.s_6b522b81') as string, type2: i18n.t('modules.views.appMonitor.dbConnPool.s_1ab064f1') as string, type3: type3s[+isPoolGet] }
    const { result, error } = await toAsyncWait(MetricApi.getAllMetricListByQuery(params));
    if (!error) {
      const data = result.data || {}
      this.$emit('metrics-loaded', data, key)
      this.$store.commit('Common/SET_METRIC_INFOS', formatMetricInfos(data));
      return Object.keys(data)
    }
    return []
  }

  private toggleMetricTypeHandle (type: MetricType) {
    this.currMetricType = type
    const query = {
      ...this.$route.query,
      metricType: type,
    }
    this.$router.replace({ query })
    this.getChartList()
  }
}
</script>

<style lang="scss" scoped>
.type-list {
  margin: 6px 0 -4px -4px;
  display: flex;
  flex-wrap: wrap;

  .type-item {
    margin: 4px;
    padding: 0 12px;
    max-width: 100%;
    height: 28px;
    box-sizing: border-box;
    background-color: var(--bg-color);
    border: 1px solid var(--border-color-base);
    border-radius: 2px;
    line-height: 26px;
    color: var(--color-text-regular);
    text-overflow: ellipsis;
    overflow: hidden;
    cursor: pointer;
    transition: all .3s;

    &:hover {
      color: var(--color-text-link);
    }

    &.active {
      background-color: #5273E0;
      border-color: #5273E0;
      color: #fff;
    }
  }
}

.chart-item {
  margin-top: 16px;
  display: inline-block;
  vertical-align: top;
  width: calc(50% - 8px);
  height: 280px;
  background-color: var(--bg-color);
  overflow: hidden;

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
</style>
