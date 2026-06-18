<template>
  <div class="event-detail-metric" v-loading="chartLoading">
    <template v-if="!chartLoading">
      <div v-for="(item, index) in trendData" :key="index" class="event-detail-metric-item">
        <div class="font-13 lh-18 pl-20 pr-20">
          <span v-if="(metricInfoMapping[item.metric] || {}).metric">
            <template v-if="!metricInfoMapping[item.metric].metricCn">{{ item.metric }}</template>
            <template v-else>{{ metricInfoMapping[item.metric].metricCn }}（{{ item.metric }}）</template>
          </span>
          <span v-else>{{ item.metric || '-' }}</span>
        </div>
        <div :style="{
            paddingLeft: `${chartGrid.left}px`,
            paddingRight: `${chartGrid.right}px`,
          }"
          class="status-cont">
          <div
            v-for="t in ((trendMapping[index + 1]) || {}).statusData"
            :key="t.key"
            @mouseover="statusMouseoverHandle($event, t)"
            @mouseout="statusMouseoutHandle($event, t)"
            :style="{ flex: t.size }"
            class="status-t">
            <div :class="{ 'bg-red': t.status === 3, 'bg-yellow': t.status === 2 }" class="status-t-bg"></div>
          </div>
        </div>
        <basic-chart
          :source="item.data"
          :grid="chartGrid"
          :showEmpty="!chartLoading && !item.data.length"
          :yAxisLabels="[]"
          :tooltipEnterable="false"
          :tooltipConfine="true"
          @on-formated="data => chartSourceFormatedHandle(data, index)"
          class="chart-cont" />
      </div>

      <div class="empty-show" v-if="!trendData.length">{{ $t('modules.components.charts.s_21efd88b') }}</div>
    </template>

    <div v-if="statusTooltip"
      :style="{
        top: statusTooltip.top,
        left: statusTooltip.left,
        right: statusTooltip.right,
      }"
      class="status-tooltip">
      <div class="mb-8">{{ statusTooltip.timeText }}</div>
      <div>
        <div
          :class="{
            'bg-red': statusTooltip.status === 3,
            'bg-yellow': statusTooltip.status === 2,
          }"
          class="status-marker icon-vm"></div>{{ $t('modules.views.alarmCenter.alarmDetail.s_50a153a1', { value0: statusTooltip.status | AlarmStatusFilter }) }}
      </div>
      <div class="pl-12">{{ statusTooltip.metric }}{{ statusTooltip | statusThresholdFilter }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import dayjs from 'dayjs';
import humanFormat from 'human-format';
import i18n from '@/i18n';
import getUnitData from '@/utils/getUnitData';
import { AlarmStatusFilter } from '@/utils/filters/alarm'
import { toAsyncWait } from '@/utils/common'
import AlarmApi from '@/api/alarm';

const valTickFormat = (val: number, unit: string = '') => {
  if (typeof val !== 'number') {
    return val
  }
  const { scale_factor, scale, sub_unit, family } = getUnitData(unit);
  const vData = humanFormat.raw(Number(val) * scale_factor, {
    ...scale,
    decimals: 12,
  })
  let value = vData.value
  if (Math.abs(value) >= 1 || value === 0) {
    value = +value.toFixed(3)
  } else if (Math.abs(value) < 0.0001) {
    value = (+value.toPrecision(3)).toExponential()
  } else if (Math.abs(value) < 0.01) {
    value = +value.toPrecision(3)
  } else {
    value = +value.toFixed(4)
  }
  const needFormatUnit = ['bytes', 'percentage', 'time', 'core'].indexOf(family || unit) > -1
  return `${value}${scale.separator}${vData.prefix}${needFormatUnit ? scale.unit + sub_unit : ''}`.trim()
}

@Component({
  filters: {
    statusThresholdFilter: (data: any) => {
      const { status, symbol, unit, alert, warn } = data;
      let text = symbol === '>' || symbol === '>='
        ? i18n.t('modules.views.alarmCenter.eventDetail.s_41c5ecd6') as string
        : i18n.t('modules.views.alarmCenter.eventDetail.s_ed9aa931') as string;
      text = i18n.t('modules.views.alarmCenter.eventDetail.s_6934eef7', { value0: text, value1: AlarmStatusFilter(status) }) as string;
      text = `${text}（${symbol}${valTickFormat(status === 3 ? alert : warn, unit)}）`
      return text
    }
  },
})
export default class TabMetric extends Vue {
  @Prop({ default: {} }) private detail!: any;

  get isSystemEvent () {
    return this.$route.path === '/sysManage/eventDetail'
  }

  get getHostName () {
    const trigger = this.detail?.trigger || {}
    return trigger.host || ''
  }
  get getServiceName () {
    const trigger = this.detail?.trigger || {}
    return trigger.service || ''
  }
  get getServiceId () {
    const trigger = this.detail?.trigger || {}
    const tags = this.detail?.tags || {}
    const serviceId = (tags.serviceId || [])[0]
    return trigger.service && serviceId ? serviceId : ''
  }
  get serviceInfo () {
    return this.$store.getters['Service/basicServiceMap'][this.getServiceId] || {}
  }

  get metricInfoMapping () {
    const mapping: any = {};
    this.trendData.map((t: any) => t.metric).forEach((metric: string) => {
      const info = this.$store.getters['Common/metricInfoMap'][metric]
      mapping[metric] = info
    })
    return mapping
  }

  private chartLoading = true;
  private trendData: any[] = [];
  private trendMapping: any = {};
  private statusTooltip: any = null;

  private chartGrid: any = {
    top: 20,
    left: 90,
    right: 50,
    bottom: 30,
    containLabel: false,
  }

  public getData () {
    const chartTime = this.detail.chartTime
    const from = Object.entries(this.detail.trigger).map(([key, value]: any) => ({
      left: key,
      operator: '=',
      right: value,
      connector: 'AND',
    }))
    const _query = JSON.parse(JSON.stringify(this.detail?.query || {}));
    if (_query[1]) {
      const _config = JSON.parse(JSON.stringify(_query[1]));
      const thresholds = _config.thresholds || {};
      const criticalThreshold = thresholds.critical
      this.$set(this.trendMapping, 1, {
        way: _config.way,
        threshold: {
          comparison: _config.comparison
            || (criticalThreshold && typeof criticalThreshold === 'object' ? criticalThreshold.comparator : '')
            || '>',
          critical: thresholds.critical,
          warning: thresholds.warning,
          unit: _config.unit,
        },
        statusData: [],
      })

      Object.keys(_query[1]).filter(key => /^[A-Z]$/.test(key) && _query[1][key]).forEach(key => {
        delete _query[1][key].metrics
        if (!_query[1][key].from.length) {
          _query[1][key].from = [...from]
        } else {
          _query[1][key].from = [{
            left: _query[1][key].from,
            operator: '=',
            right: [...from],
            connector: 'AND',
          }]
        }
      })
    }

    const params = {
      ..._query,
      start: chartTime.start,
      end: chartTime.end,
      interval: chartTime.interval,
    }
    this.getChartSource(params)
  }

  private async getChartSource (params: any) {
    this.chartLoading = true
    const fetchUrl = this.isSystemEvent ? 'getSystemEventDetailChartTrend' : 'getEventDetailChartTrend'
    const { result, error } = await toAsyncWait(AlarmApi[fetchUrl](params));
    this.chartLoading = false
    if (!error) {
      const { data = {} } = result || {};
      this.trendData = Object.entries(data || {}).map(([name, items]: any) => {
        const [queryIdx, ..._metric] = (name || '').split(':')
        const unit = params[queryIdx]?.unit || ''
        const series = this.formatCarouselPreviewData(items.length ? [items[0]] : [], unit)
        return {
          metric: _metric.join(':'),
          data: series[0] || [],
        }
      })
    }
  }

  private formatCarouselPreviewData (data: any[], unit: string) {
    const limitTags = ['host_id', 'service_id', 'serviceId', 'hostId', 'source', 'collectorType', 'collector', 'spuid']
    return data.map((item: any) => {
      const tag = Object.entries(item.tags || {}).filter(([key]: any) => limitTags.indexOf(key) === -1).map(([k, v]) => `${k}=${v || ''}`).join(';')
      const chartData: any[] = [];
      const values = item.values || [];
      const keys: string[] = values.map(([key]: any) => dayjs(Number(key)).format('YYYY-MM-DD HH:mm'));
      (item.columns || []).forEach((col: string, idx: number) => {
        const isCritical = idx > 1 && col.indexOf('critical') > -1
        const isWarning = idx > 1 && col.indexOf('warning') > -1
        if (idx > 0) {
          chartData.push({
            name: idx === 1 ? tag : col,
            unit,
            thresholdLine: isCritical ? 'critical' : isWarning ? 'warning' : '',
            data: keys.map((key, i) => ({ key, value: (values[i] || [])[idx], })),
          })
        }
      })
      return chartData;
    });
  }

  private chartSourceFormatedHandle ({ xAxisData }: any, index: number) {
    const trendMappingItem = this.trendMapping[index + 1]
    const trendDataItem = this.trendData[index] || {}
    if (!trendMappingItem) {
      return;
    }

    const formatNum = (num: any) => {
      if (num == null || num === '') return NaN
      if (typeof num === 'number') return +num
      if (typeof num === 'object' && num.value != null) return +num.value
      return +num
    }
    const { comparison: symbol, unit } = trendMappingItem.threshold
    const alert = formatNum(trendMappingItem.threshold.critical)
    const warn = formatNum(trendMappingItem.threshold.warning)

    if (!['<', '<=', '>', '>='].includes(symbol) || (isNaN(alert) && isNaN(warn))) {
      return trendMappingItem.statusData = []
    }

    const statusData: any[] = []
    xAxisData.forEach((key: string) => {
      const statusItem = {
        key,
        start: +new Date(key),
        end: +new Date(key),
        status: 0,
        size: 1,
        timeText: key,
        symbol,
        unit,
        alert,
        warn,
        metric: trendDataItem.metric,
      }
      for (const item of (trendDataItem?.data || [])) {
        const value = formatNum(item.data.find((t: any) => t.key === key)?.value);
        if (isNaN(value)) {
          continue;
        }
        if (!isNaN(alert)) {
          const isAlert = symbol === '<' ? value < alert :
              symbol === '<=' ? value <= alert :
              symbol === '>' ? value > alert : value >= alert
          if (isAlert) {
            statusItem.status = 3
            break;
          }
        }
        if (!isNaN(warn)) {
          const isWarn = symbol === '<' ? value < warn :
              symbol === '<=' ? value <= warn :
              symbol === '>' ? value > warn : value >= warn
          if (isWarn) {
            statusItem.status = 2
          }
        }
      }
      const prevStatusItem = statusData.slice(-1)[0];
      if (prevStatusItem && prevStatusItem.status === statusItem.status) {
        prevStatusItem.end = statusItem.end
        prevStatusItem.size += 1
        prevStatusItem.timeText = `${prevStatusItem.key} ~ ${key}`
        if (prevStatusItem.key.slice(0, 10) === key.slice(0, 10)) {
          prevStatusItem.timeText = `${prevStatusItem.key} ~ ${key.slice(11, 16)}`
        }
      } else {
        statusData.push(statusItem)
      }
    })
    return trendMappingItem.statusData = statusData;
  }

  private statusMouseoverHandle (event: any, item: any) {
    if (!item.status) {
      return;
    }
    const $target = event.target || event.srcElement
    const rect = $target.getBoundingClientRect(); // 获取元素的位置信息
    const right = window.innerWidth - rect.right; // 计算屏幕右侧的距离
    const halfWidth = rect.width / 2
    this.statusTooltip = {
      ...item,
      top: rect.top + rect.height + 'px',
      left: right > 400 ? rect.left + halfWidth + 'px' : 'auto',
      right: right <= 400 ? right + halfWidth + 'px' : 'auto',
    }
  }
  private statusMouseoutHandle (event: any, item: any) {
    this.statusTooltip = null;
  }

}
</script>

<style lang="scss" scoped>
.event-detail-metric {
  padding: 18px 0 !important;
  overflow: auto !important;

  .event-detail-metric-item {
    & + .event-detail-metric-item {
      margin-top: 8px;
    }
  }

  .chart-cont {
    height: 200px;
  }

  .status-cont {
    margin: 14px 0 -14px 0;
    height: 16px;
    display: flex;
    position: relative;
    z-index: 1;
    .status-t {
      padding: 4px 0;
      flex: 1;
      .status-t-bg {
        width: 100%;
        height: 100%;
        pointer-events: none;
        &::before {
          content: '';
          display: none;
          width: 100%;
          height: 168px;
          opacity: 0.12;
          pointer-events: none;
        }
      }
      &:hover .bg-red::before {
        display: block;
        background: var(--color-danger);
      }
      &:hover .bg-yellow::before {
        display: block;
        background: var(--color-warning);
      }
    }
  }

  .empty-show {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 13px;
    color: var(--color-text-secondary);
  }
}

.status-tooltip {
  padding: 10px 12px;
  max-width: 400px;
  max-height: 300px;
  box-shadow: 1px 1px 4px 0 var(--tooltip-shadow-color);
  background: rgba(255, 255, 255, .75);
  border-radius: 4px;
  overflow: auto;
  font-size: 12px;
  line-height: 18px;
  word-break: break-all;
  white-space: normal;
  backdrop-filter: blur(2px);
  position: fixed;
  top: -1000px;
  left: -1000px;
  z-index: 2;
  .status-marker {
    margin-right: 6px;
    width: 6px;
    height: 6px;
    border-radius: 1px;
  }
}
</style>
