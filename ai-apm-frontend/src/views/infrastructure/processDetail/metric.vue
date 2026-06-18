<template>
  <div class="detail-metric" v-loading="isLoading">
    <div ref="chartWrap" class="chart-wrapper">
      <div v-for="(item, index) in chartSource" :key="`${index}-${item.name}`"
        class="section-item">
        <div class="section-title">
          <el-popover
            @show="loadMetricInfo(item.name)"
            placement="left-start"
            width="400"
            trigger="hover"
            popper-class="metric-info-popper">
            <div slot="reference" class="tit">
              <div class="ell">{{ item.nameKey ? $t(item.nameKey) : item.name }}</div>
              <div class="desc ell">{{ $t('modules.views.appMonitor.dbConnPool.s_5eaa60a4', { value0: item.desc || '-' }) }}</div>
            </div>
            <metric-info-tooltip
              v-loading="metricInfoMapping[item.name].loading"
              :detail="metricInfoMapping[item.name].info"
              :tooltip="false"
            />
          </el-popover>

          <el-select
            v-model="item.active"
            @change="getProcessGraphByPid(item)"
            :disabled="item.loading"
            size="mini" :placeholder="$t('modules.views.metrics.list.s_708c9d6d')"
            class="select">
            <el-option :label="$t('modules.views.infrastructure.processDetail.s_235bbf67')" value="process"></el-option>
            <el-option :label="$t('modules.views.infrastructure.processDetail.s_a6082df7')" value="pid"></el-option>
          </el-select>
        </div>

        <div class="section-cont">
          <basic-chart
            v-if="item.active !== 'pid'"
            :source="item.source"
            :showEmpty="!item.source.length"
            :tooltipEnterable="true" />
          <basic-chart
            v-else
            v-loading="item.loading"
            :source="item.pidSource"
            :showEmpty="!item.loading && !item.pidSource.length"
            :showLegend="true"
            :tooltipEnterable="true" />
        </div>
      </div>
    </div>

    <div v-if="!isLoading && !chartSource.length" class="empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import dayjs from 'dayjs';
import { debounce } from '@/utils/common';
import { toAsyncWait } from '@/utils/common';
import MetricApi from '@/api/metric';
import InfraApi from '@/api/infrastructure';
import BasicChart from '@/components/charts/basic-chart.vue';
import MetricInfoTooltip from '@/components/metric-info-tooltip.vue';

@Component({
  components: {
    BasicChart,
    MetricInfoTooltip,
  },
})
export default class DetailMetric extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;

  public $refs!: {
    chartWrap: HTMLDivElement,
  }

  private isLoading = false;
  private chartSource: any[] = [];
  private listTotal: number = 0;
  get noMore () {
    return this.chartSource.length >= this.listTotal
  }

  private metricInfoMapping: any = {}

  get getQueryParams () {
    const { pname, hostName, fromTime, toTime, interval } = this.queryParams
    const params: any = {
      start: +new Date(fromTime),
      end: +new Date(toTime),
      interval,
      from: [
        { left: 'pname', operator: '=', right: pname, connector: 'AND' },
        { left: 'host', operator: '=', right: hostName, connector: 'AND' },
      ],
    }
    return params;
  }

  private lastKey = '';

  private timer: any = null;
  private scrollContainer: any = null;
  private scrollHandle: any = null;

  private beforeDestroy () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    if (this.scrollContainer) {
      this.scrollContainer.removeEventListener('scroll', this.scrollHandle);
    }
  }

  public getData () {
    this.getProcessGraph(true)
  }

  private async getProcessGraph (reset = true) {
    if (this.isLoading || (!reset && this.noMore)) {
      return;
    }
    const params: any = {
      ...this.getQueryParams,
      lastKey: this.lastKey,
      pageSize: 12,
    }
    if (reset) {
      delete params.lastKey
    }
    this.isLoading = true;
    const { result, error } = await toAsyncWait(InfraApi.getProcessGraph(params));
    this.isLoading = false;
    if (!error) {
      const { data, total, lastKey } = result?.data || {}
      const list = (data || []).map((item: any) => {
        const columns: string[] = (item.columns || []).slice(1, 2)
        const describeCns: string[] = (item.describeCns || []).slice(1, 2)
        const values: any[] = item.values || []
        return {
          name: columns[0] || '',
          desc: describeCns[0] || '',
          source: columns.map((col, idx) => ({
            name: col || '',
            unit: (item.units || [])[idx + 1] || '',
            area: true,
            smooth: true,
            data: values.map(([key, ...value]: any) => ({
              key: dayjs(Number(key)).format('YYYY-MM-DD HH:mm'),
              value: value[idx],
            })),
          })),
          active: 'process',
          pidSource: [],
          loading: false,
          loaded: false,
        }
      })
      this.chartSource = reset ? list : Array.from(this.chartSource).concat(list);
      list.forEach((item: any) => {
        if (!this.metricInfoMapping[item.name]) {
          this.$set(this.metricInfoMapping, item.name, {
            info: {},
            loading: false,
            loaded: false,
          });
        }
      })
      if (lastKey) {
        this.lastKey = lastKey
        if (reset) {
          this.listTotal = total || 0;
        }
      } else {
        this.listTotal = this.chartSource.length;
      }

      this.$nextTick(() => {
        if (!this.scrollContainer) {
          this.loop();
        } else {
          // 未撑满容器，继续加载
          this.continueLoad()
        }
      })
    }
  }

  // 按pid获取图表
  private async getProcessGraphByPid (chartItem: any) {
    if (chartItem.active !== 'pid' || chartItem.loaded || chartItem.loading) {
      return;
    }
    const params: any = {
      ...this.getQueryParams,
      metric: chartItem.name,
      by: ['pid'],
      lastKey: '',
      pageSize: 10,
    }
    chartItem.loading = true;
    const { result, error } = await toAsyncWait(InfraApi.getProcessGraph(params));
    chartItem.loading = false;
    if (!error) {
      const list = result?.data?.data || []
      chartItem.pidSource = list.map((item: any) => ({
        name: 'pid:' + (item.tags || {}).pid || '',
        unit: (item.units || [])[1] || '',
        smooth: true,
        data: (item.values || []).map(([key, value]: any) => ({
          key: dayjs(Number(key)).format('YYYY-MM-DD HH:mm'),
          value,
        }))
      }));
      chartItem.loaded = true;
    }
  }

  // 显示指标详细信息
  private async loadMetricInfo (metric: any) {
    const currInfo = this.metricInfoMapping[metric]
    if (currInfo.loaded || currInfo.loading) {
      return
    }
    currInfo.loading = true
    await this.$store.dispatch('Common/GET_METRIC_INFOS', [metric]);
    const info = this.$store.getters['Common/metricInfoMap'][metric] || {}
    currInfo.info = { metric, ...info }
    currInfo.loading = false
    currInfo.loaded = true
  }

  // 滚动加载相关
  private loop () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    this.timer = setTimeout(() => {
      const scrollContainer = this.$refs.chartWrap;
      if (!scrollContainer) {
        this.loop();
      } else {
        this.scrollContainer = scrollContainer;
        // 滚动到底加载更多
        this.scrollHandle = debounce(() => {
          this.continueLoad()
        }, 17)
        scrollContainer.addEventListener('scroll', this.scrollHandle)

        // 未撑满容器，继续加载
        this.continueLoad()
      }
    }, 100)
  }

  // 继续加载
  private continueLoad () {
    const { scrollHeight, scrollTop, clientHeight } = this.scrollContainer
    if (!this.noMore && !this.isLoading && scrollHeight - clientHeight - scrollTop < 50) {
      this.getProcessGraph(false)
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-metric {
  height: 100%;
  overflow: hidden;

  .chart-wrapper {
    height: 100%;
    display: flex;
    flex-wrap: wrap;
    overflow: auto;
  }
}

.section-item {
  margin: 0 0 16px 16px;
  width: calc((100% - 16px) / 2);
  height: 286px;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  color: var(--color-text-primary);
  &:nth-child(2n + 1) {
    margin-left: 0;
  }

  .section-title {
    height: 54px;
    padding: 12px 135px 0 20px;
    font-size: 14px;
    line-height: 22px;
    position: relative;
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
    .desc {
      margin-top: 5px;
      font-size: 12px;
      line-height: 14px;
      color: var(--color-text-secondary);
    }
    .select {
      width: 110px;
      position: absolute;
      right: 16px;
      top: 16px;
    }
  }

  .section-cont {
    height: calc(100% - 54px);
    padding: 0 10px;
  }
}
</style>
