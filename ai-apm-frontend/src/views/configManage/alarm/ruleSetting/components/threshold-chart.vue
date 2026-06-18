<template>
  <div class="basic-chart-wrapper">
    <div class="basic-chart-cont" ref="chart" />
    <div class="empty-show" v-if="showEmpty || empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Getter, State } from 'vuex-class';
import { isHexColor, formatToHexOpacity } from '@/utils/formatColor';
import { debounce } from '@/utils/common'
import getUnitData from '@/utils/getUnitData';
import humanFormat from 'human-format';
import dayjs from 'dayjs'

/* {
  name: 'item1',  // seriesName
  data: [{ key: '2022-06-22 18:10', value: 6734 }],   // 图表数据
  area: false,    // (选) 是否为面积图
  smooth: true,  // (选) 线条是否平滑显示
  unit: '',       // (选) 单位
  thresholdLine: '', // (选) 阈值线 critical | warning
} */
interface SourceItem {
  name: string;
  data: Array<{
    key: string,
    value: number
  }>;
  area?: boolean;
  smooth?: boolean;
  unit?: string;
  thresholdLine?: string;
}

interface Threshold {
  comparison: '<' | '<=' | '>' | '>=',
  critical?: number;
  warning?: number;
  unit?: string;
}

const comparisons = ['<', '<=', '>', '>=']

const needFormatUnits = [
  'bytes', 'percentage', 'time', // family 字段
  'core',
]

// 补全轴标签
const completeAxisLabels = (arr: number[], min: number, max: number, step: number) => {
  if (!arr?.length || step <= 0 || !min || !max) {
    return [...arr];
  }

  const fill = (start: number, delta: number, condition: any) => {
    const result = []
    let val = start + delta;
    while (condition(val)) {
      if (!arr.includes(val)) {
        result.push(val);
      }
      val += delta;
    }
    return delta > 0 ? result : result.reverse();
  };

  return [
    ...fill(arr[0], -step, (v: number) => v >= min),
    ...arr,
    ...fill(arr[0], step, (v: number) => v < max)
  ].sort((a, b) => a - b);
};

// 过滤轴标签，显示合适的个数
const filterAxisLabels = (axis: string[], maxLength = 8) => {
  const getStep = (list: string[]) => {
    let step = 60
    if (list.length > 1) {
      const _step = Math.abs(Math.floor((+new Date(list[1]) - +new Date(list[0])) / 1000))
      step = Math.max(_step, step)
    }
    return step;
  }
  if (axis.length <= maxLength) {
    return { axis: [...axis], step: getStep(axis) }
  }

  let _interval = 60;
  let _axis: string[] = [];
  const intervalArr = [ // 间隔：秒
    1, 2, 5, 10, 15, 20, 30,
    1 * 60, 2 * 60, 3 * 60, 4 * 60, 6 * 60, 8 * 60, 12 * 60,
    1 * 24 * 60,
  ].map(t => t * 60);
  const timestampMap: any = {}
  axis.forEach(t => {
    timestampMap[t] = (+new Date(t) - +new Date(axis[0].substring(0, 10) + ' 00:00')) / 1000
  })

  // 找到合适的间隔
  for (const [i, v] of intervalArr.entries()) {
    _interval = v;
    const list = axis.filter(t => timestampMap[t] % _interval === 0);
    if (list.length && list.length <= maxLength) {
      _axis = list;
      break;
    } else if (!list.length) {
      _interval = intervalArr[i - 1] || intervalArr[0];
      _axis = axis.filter(t => timestampMap[t] % _interval === 0);
      break;
    }
  }

  // intervalArr全部不合适，扩展并继续找合适的间隔
  const lastInterval = intervalArr.slice(-1)[0]
  if (_interval === lastInterval && !_axis.length) {
    let i = 1;
    while (i <= 30 && !_axis.length) {
      i++;
      _interval = lastInterval * i;
      const list = axis.filter(t => timestampMap[t] % _interval === 0);
      if (list.length && list.length <= maxLength) {
        _axis = list;
      }
    }
  }

  // 还没找到合适的间隔，可能是数据不规则，根据maxLength强制截取
  if (!_axis.length || _axis.length > maxLength) {
    const arr = [..._axis.length ? _axis : axis]
    const step = Math.floor((_axis.length - 1) / (maxLength - 1))
    _axis = Array.from({ length: maxLength }, (_, i) => arr[i * step])
  }

  _axis = _axis.length ? _axis : [...axis];
  return { axis: _axis, step: getStep(_axis) };
}

@Component
export default class ThresholdChart extends Vue {
  @Getter('themeVariables') private themeVars!: any;
  @State('themeChanged') private themeChanged!: boolean;

  @Prop({ default: () => [] }) private source!: SourceItem[];
  @Prop({ default: () => ({}) }) private threshold!: Threshold; // 阈值
  @Prop({ default: () => null }) private grid!: any;      // 网格
  @Prop({ default: false }) private showEmpty!: boolean;  // 数据是否为空
  @Prop({ default: '' }) private title!: string;          // (选) 标题
  @Prop({ default: false }) private showLegend!: boolean; // (选) 是否显示legend
  @Prop({ default: null }) private minInterval!: number | null; // (选) y坐标轴最小间隔
  @Prop({ default: null }) private group!: string | null; // (选) 图表联动 group id
  @Prop() private clickEvent: any;                        // (选) 鼠标事件
  @Prop() private tooltipFormat: any;                     // (选) Tooltip处理方法
  @Prop({ default: () => [] }) private colors!: any[];    // (选) 图表颜色
  @Prop({ default: () => [] }) private yAxisLabels!: any[]; // (选) y轴对应的label列表
  @Prop({ default: 1.5 }) private lineWidth!: number;   // (选) 折线图line宽度
  @Prop({ default: false }) private large!: boolean;      // (选) 是否开启大数据量优化
  @Prop({ default: true }) private animation!: boolean;   // (选) 是否开启动画
  @Prop({ default: true }) private tooltipEnterable!: boolean; // (选) tooltip鼠标是否可进入
  @Prop({ default: true }) private tooltipConfine!: boolean;   // (选) tooltip是否限制在图表区域
  @Prop({ default: 8 }) private showAxisLabelCount!: number;  // (选) 显示x轴label的最大个数
  @Prop({ default: '' }) private fromTime!: number | string; // (选) 起始时间
  @Prop({ default: '' }) private toTime!: number | string;   // (选) 结束时间
  @Prop({ default: null }) private interval!: number | null;   // (选) 时间间隔，单位秒

  public $refs!: {
    chart: HTMLDivElement
  }

  private myChart: any = {};
  private resizeHandler: any = null;

  private empty = false;

  get chartColors () {
    return [...(this.colors && this.colors.length ? this.colors : this.themeVars.colors)]
  }

  get propDataStr () {
    return JSON.stringify({
      source: this.source,
      threshold: this.threshold,
      grid: this.grid,
      title: this.title,
      showLegend: this.showLegend,
      minInterval: this.minInterval,
      group: this.group,
      colors: this.colors,
      yAxisLabels: this.yAxisLabels,
      tooltipEnterable: this.tooltipEnterable,
      tooltipConfine: this.tooltipConfine,
    })
  }
  @Watch('propDataStr', { deep: true })
  private onPropDataStrChanged(newVal: any, oldVal: any) {
    if (newVal === oldVal) {
      return
    }
    if (this.myChart) {
      this.updateEcharts();
    } else {
      this.drawEcharts();
    }
  }
  @Watch('themeChanged')
  private onThemeChanged(value: boolean) {
    if (value) {
      if (this.myChart) {
        this.updateEcharts();
      } else {
        this.drawEcharts();
      }
    }
  }

  get hasThresholdLine () { // 是否有阈值线
    return !!Array.from(this.source || []).filter((item: any) => item.thresholdLine).length
  }
  get thresholdBase () {
    if (!this.hasThresholdLine) {
      return 0
    }
    const values = Array.from(new Set((this.source || []).map((item: any) => item.data).flat().map(t => t.value)))
    const base = -values.filter(val => typeof val === 'number' && !isNaN(val)).reduce((min, val) => Math.floor(Math.min(min, val)), Infinity);
    return base < 0 ? 0 : base
  }

  get isTimeKey () { // x轴是否为时间格式
    /*
      '2022-09-02 14:51:34'
      '2022-09-02 14:52'
      '2022/09/02 14:51:34'
      '2022/09/02 14:52'
    */
    const keys = Array.from(new Set((this.source || []).map((item: any) => item.data).flat().map(t => t.key)))
    const timeReg = new RegExp(/^\d{4}([-|\/]\d{2}){2} \d{2}(:\d{2}){1,2}$/)
    // const timeReg = new RegExp(/(^(\d{4}[-|\/])?\d{1,2}(-|\/)\d{1,2}( \d{1,2}(:\d{1,2}){1,2})?$)|(^\d{1,2}(:\d{1,2}){1,2}$)/)
    return !!keys.length && keys.every(key => timeReg.test(key))
  }

  get xAxisData () { // 数据全量keys，时间格式时会根据最小间隔补充
    const keys = Array.from(new Set((this.source || []).map((item: any) => item.data).flat().map(t => t.key)))
    if (!keys.length || !this.isTimeKey) {
      return keys
    }
    const formatter = `YYYY-MM-DD HH:mm${keys[0].length === 16 ? '' : ':ss'}`
    const _keys = keys.map(t => +new Date(t)).sort((a, b) => a - b);
    const isComplete = !!this.interval && !!(this.fromTime || this.toTime) // 时间范围补全
    if (_keys.length <= 2 && !isComplete) {
      return _keys.map(k => dayjs(k).format(formatter))
    }

    let _interval = isComplete ? this.interval as number * 1000 : Infinity;
    if (!isComplete) {
      // 计算最小时间间隔
      for (let n = 1; n < _keys.length; n++) {
        _interval = Math.min(_interval, _keys[n] - _keys[n - 1]);
      }
    }
    const min = this.fromTime ? +new Date(this.fromTime) : _keys[0]
    const max = this.toTime ? +new Date(this.toTime) : _keys[_keys.length - 1]
    return completeAxisLabels(_keys, min, max, _interval).map(k => dayjs(k).format(formatter))
  }

  get yAxisMinInterval () { // y轴最小间隔
    if (!this.yAxisLabels.length) {
      return this.minInterval || 0
    }
    const values = this.yAxisLabels.map(t => Math.abs(t.value)).sort((a, b) => a - b);
    const len = values.length;
    if (len < 2) {
      return 1
    }
    let minVal = values[len - 1];
    let poor = 0;
    for (let i = 0; i < len; ++i) {
      poor = values[i] - values[i - 1];
      if (poor < minVal) {
        minVal = poor;
      }
    }
    return Math.ceil(minVal) || 1;
  }

  private mounted() {
    this.drawEcharts();
    this.resizeHandler = debounce(() => {
      if (this.myChart) {
        this.myChart.resize()
      }
    }, 100)
    window.addEventListener('resize', this.resizeHandler)
  }

  private beforeDestroy() {
    window.removeEventListener('resize', this.resizeHandler)
    if (!this.myChart) {
      return
    }
    if (this.myChart.off) {
      this.myChart.off('click');
    }
    if (this.myChart.dispose) {
      this.myChart.dispose();
    }
    this.myChart = null
  }

  private drawEcharts() {
    this.myChart = this.$echarts.init(this.$refs.chart, '', { renderer: 'svg' });
    this.myChart.setOption(this.getOption());
    this.$nextTick(() => {
      this.myChart.resize();
    });
    if (this.group) {
      this.myChart.group = this.group;
      this.$echarts.connect(this.group);
    }

    if (this.clickEvent) {
      // 元素的点击事件
      this.myChart.on('click', (params: any) => {
        if (!this.clickEvent) {
          return;
        }
        this.clickEvent(params);
      });
    }
  }

  private updateEcharts () {
    this.myChart.clear();
    this.myChart.setOption(this.getOption(), true); // clear cache
  }

  private getOption () {
    const { source, units, legendData, xAxisData, seriesData } = this.formatData();
    const { axis: xAxisDataShowed, step: xAxisDataShowedStep } = filterAxisLabels(xAxisData, this.showAxisLabelCount);

    this.empty = !!source.length && !xAxisData.length;

    // 单位
    const yAxisUnits = Array.from(new Set(units))

    // xAxis
    const xAxisOption: any = {
      type: 'category',
      data: xAxisData,
      axisTick: {
        show: false,
        lineStyle: { color: this.themeVars.borderColorLighter },
        alignWithLabel: true,
      },
      splitLine: { show: false },
      axisLine: {
        show: !!xAxisData.length,
        lineStyle: { color: this.themeVars.borderColorLighter }
      },
      axisLabel: {
        color: this.themeVars.axisLabelColor,
        interval: 0,
        fontSize: 12,
        margin: 10,
        formatter: (val: string, index: number) => {
          if (!xAxisDataShowed.includes(val)) {
            // NOTE: 空字符串会引起X轴axisLabel高度计算异常，第一个用空格占位
            return index !== 0 ? '' : ' ';
          } else if (this.isTimeKey) {
            return xAxisDataShowedStep >= 24 * 60 * 60 ? val.substring(5, 10) : val.substring(11, 16)
          } else {
            return val.length > 20 ? `${val.slice(0, 10)}\n${val.slice(10, 17)}...` :
                val.length > 10 ? `${val.slice(0, 10)}\n${val.slice(10, 20)}` : val
          }
        },
      },
      axisPointer: {
        show: true,
        snap: true,
        label: {
          show: true,
          margin: 5,
          fontSize: 10,
          backgroundColor: '#fff',
          color: '#45474A',
          shadowColor: 'rgba(119, 122, 126, .3)',
          shadowBlur: 5,
          // backgroundColor: this.themeVars.tooltipBgColor,
          height: 14,
          lineHeight: 14,
        }
      },
    }

    // yAxis
    const yAxisOption: any[] = (yAxisUnits.length ? yAxisUnits : ['']).map((unit: string, i) => ({
      type: 'value',
      // splitNumber: 2,
      minInterval: this.yAxisMinInterval,
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: {
        show: i === 0,
        lineStyle: { color: this.themeVars.borderColorLighter, type: 'dashed' },
      },
      axisLabel: {
        show: i < 2,
        color: this.themeVars.axisLabelColor,
        fontSize: 12,
        formatter: (val: number) => {
          val = val - this.thresholdBase
          const _value = this.valTickFormat(val, unit, true)
          if (!this.yAxisLabels.length) {
            return _value
          }
          const t: any = this.yAxisLabels.find(s => val === s.value) || {}
          return t.label || _value
        },
      },
    }));

    // series
    const seriesOption: any[] = source.map((item: any, idx) => {
      const color = this.chartColors[idx % this.chartColors.length]
      const symbol = item.symbol || 'emptyCircle'
      const symbolSize = item.symbolSize || 4
      const yAxisIndex = yAxisUnits.findIndex(t => t === (item.unit || ''))
      return {
        name: item.name,
        type: 'line',
        stack: item.thresholdLine,
        smooth: item.smooth ?? true,
        smoothMonotone: 'x',
        yAxisIndex,
        color,
        data: seriesData[idx],
        showSymbol: true,
        showAllSymbol: true,
        symbol: !item.thresholdLine ? symbol : 'none',
        symbolSize,
        lineStyle: {
          width: !item.thresholdLine ? this.lineWidth || 1 : 0,
          // 折线数据大小相同时，会导致折线无法正常显示
          // shadowColor: formatToHexOpacity(color, .2),
          // shadowBlur: 2,
          // shadowOffsetY: 2,
        },
        itemStyle: {
          opacity: +!!item.symbol,
        },
        emphasis: {
          itemStyle: {
            opacity: 1,
          }
        },
        areaStyle: !item.thresholdLine && item.area ? {
          opacity: 0.5,
          color: new this.$echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: formatToHexOpacity(color, 0.6),
            }, {
              offset: 0.9,
              color: isHexColor(color) ? formatToHexOpacity(color, 0) : 'rgba(0,0,0,0)',
            }, {
              offset: 1,
              color: 'rgba(0,0,0,0)',
            }
          ]),
        } : item.thresholdLine && item.name.includes('high') ? {
          color: item.thresholdLine === 'critical' ?
            formatToHexOpacity(this.themeVars.colorDanger, 0.15) :
            formatToHexOpacity('#00ff00', 0.3),
        } : null,
        markArea: idx === 0 ? {
          silent: true,
          label: {
            position: 'insideLeft',
            fontSize: 10,
          },
          data: this.getMarkAreaData(this.threshold),
        } : null,
        sampling: this.large ? 'lttb' : null,
      }
    });

    const option = {
      title: {
        text: this.title || '',
        left: 10,
        top: 6,
        textStyle: {
          color: this.themeVars.colorTextRegular,
          fontWeight: 'normal',
          fontSize: 14,
          lineHeight: 22,
        },
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          lineStyle: {
            color: this.themeVars.colorPrimary
          },
        },
        backgroundColor: this.themeVars.tooltipBgColor,
        borderWidth: 0,
        padding: [8, 8],
        textStyle: {
          fontSize: 12,
          color: this.themeVars.colorTextRegular,
        },
        position: (point: any, params: any, dom: any, rect: any, size: any) => {
          // 固定在顶部
          // const fixRight = size.viewSize[0] - size.contentSize[0];
          // const left = point[0] + 20 > fixRight ? fixRight : point[0] + 20
          const left = point[0] > size.viewSize[0] / 2 ? point[0] - 20 - size.contentSize[0] : point[0] + 20
          return [left, '10%'];
        },
        enterable: this.tooltipEnterable,
        confine: this.tooltipConfine,
        appendToBody: true, // 是否将tootip放到body内
        transitionDuration: 0.3,
        extraCssText: `box-shadow:1px 1px 4px 0 ${this.themeVars.tooltipShadowColor};max-width:400px;max-height:300px;overflow:auto;word-break:break-all;white-space:normal;backdrop-filter: blur(2px);`,
        formatter: (params: any[]) => {
          let _axisValueLabel = ''
          const tips = params.map((item: any, index: number) => {
            const { seriesName = '', marker, axisValueLabel, componentIndex, data } = item;
            const _value = data._value
            _axisValueLabel = axisValueLabel;
            let val = this.valTickFormat(_value, units[componentIndex])
            if (this.yAxisLabels.length) {
              const t: any = this.yAxisLabels.find(s => _value === s.value) || {}
              val = t.label || val
            }
            return {
              value: _value,
              str: `<div style="overflow:hidden;margin-top:4px;line-height:18px;">
                  ${marker} ${seriesName}
                  <span style="float:right;margin-left:20px;font-weight:bold;">${val}</span>
                </div>
                <div style="clear:both"></div>`,
            }
          })
          const tipStrs = [
            ...tips.filter(t => typeof t.value === 'number').sort((a: any, b: any) => b.value - a.value),
            ...tips.filter(t => typeof t.value !== 'number'),
          ].map(t => t.str);
          tipStrs.unshift(_axisValueLabel)
          if (this.tooltipFormat) {
            // 对 Tooltip 处理
            return [...this.tooltipFormat(params, tipStrs, this.valTickFormat)].join('')
          }
          return tipStrs.join('')
        }
      },
      legend: {
        show: this.showLegend,
        type: 'scroll',
        data: legendData,
        bottom: 1,
        left: 6,
        itemGap: 20,
        itemWidth: 6,
        itemHeight: 6,
        icon: 'rect',
        textStyle: {
          color: this.themeVars.colorTextRegular,
          fontSize: 12,
          lineHeight: 22,
          overflow: 'truncate',
        },
        itemStyle: {
          opacity: 1,
        },
        pageIconColor: this.themeVars.colorTextRegular,
        pageIconInactiveColor: this.themeVars.colorTextPlaceholder,
        pageTextStyle: {
          color: this.themeVars.colorTextRegular,
        },
      },
      grid: this.grid ? this.grid : {
        top: this.title ? 50 : 20,
        bottom: this.showLegend ? 30 : 10,
        left: 10,
        right: 20,
        containLabel: true,
      },
      xAxis: xAxisOption,
      yAxis: yAxisOption,
      series: seriesOption,
      color: this.chartColors,
      animation: this.animation && !this.large,
    };
    return option;
  }

  private formatData () {
    const source: any[] = JSON.parse(JSON.stringify(this.source || []));
    source.forEach((item: any) => {
      item.data = item.data.map((t: any) => ({
        ...t,
        value: typeof t.value === 'number' ? +t.value.toFixed(10) : '-', // 最多保留10位数字
      }))
    });
    // 将阈值的high线放到后面
    if (this.hasThresholdLine) {
      source.sort((a, b) => a.name.includes('high') - b.name.includes('high'));
    }

    const xAxisData = [...this.xAxisData]
    const units: string[] = source.map((item: any) => item.unit || '')
    const legendData: string[] = []
    const seriesData: any[][] = []
    const isNum = (num: any) => typeof num === 'number'
    const getValue = (list: any[], k: string) => {
      const val = (list.find((t: any) => t.key === k) || {}).value
      return isNum(val) ? val : '-'
    }
    source.forEach((item) => {
      // 移除名称中的换行符
      item.name = (item.name || '').replaceAll('\n', '');
      legendData.push(item.name);
      seriesData.push(xAxisData.map((key, idx) => {
        const _value = getValue(item.data, key)
        let value = _value
        if (isNum(value) && this.hasThresholdLine) {
          if (['critical_high', 'warning_high'].includes(item.name)) {
            const lowName = item.thresholdLine === 'critical' ? 'critical_low' : 'warning_low'
            const lowItem = source.find(t => t.name === lowName) || {}
            let lowValue = getValue(lowItem.data || [], key)
            lowValue = isNum(lowValue) ? lowValue : 0
            value = value - lowValue
          } else {
            value = value + this.thresholdBase
          }
        }
        if (!isNum(value) || item.thresholdLine) {
          return { value, _value }
        }
        const prev = idx > 0 ? getValue(item.data, xAxisData[idx - 1]) : '-'
        const next = idx < xAxisData.length - 1 ? getValue(item.data, xAxisData[idx + 1]) : '-'
        if (isNum(prev) || isNum(next)) { // 上一个或下一个为数字
          return { value, _value }
        }
        return {
          value,
          _value,
          symbol: item.symbol || 'emptyCircle',
          itemStyle: {
            opacity: 1,
          }
        }
      }));
    })
    return { source, units, legendData, xAxisData, seriesData }
  }

  private valTickFormat(val: number, unit: string = '', isAxis: boolean = false) {
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
    const needFormatUnit = !isAxis || needFormatUnits.indexOf(family || unit) > -1
    return `${value}${scale.separator}${vData.prefix}${needFormatUnit ? scale.unit + sub_unit : ''}`.trim()
  }

  public resize () {
    if (this.myChart && this.myChart.resize) {
      this.myChart.resize()
    }
  }

  // markArea数据
  private getMarkAreaData (threshold: any) {
    const formatNum = (num: any) => num || num === 0 ? +num : NaN
    const { comparison: symbol, unit } = threshold
    const alert = formatNum(threshold.critical)
    const warn = formatNum(threshold.warning)
    if (!comparisons.includes(symbol) || (isNaN(alert) && isNaN(warn))) {
      return []
    }

    const areas: any[] = [];
    const alertArea: any = {
      color: formatToHexOpacity(this.themeVars.colorDanger, 0.15),
      textColor: this.themeVars.colorDanger,
      include: symbol.indexOf('=') !== -1
    }
    const warnArea: any = {
      color: formatToHexOpacity(this.themeVars.colorWarning, 0.15),
      textColor: this.themeVars.colorWarning,
      include: symbol.indexOf('=') !== -1
    }
    const isLess = symbol === '<' || symbol === '<='
    if (!isNaN(alert)) {
      alertArea.reverse = isLess
      alertArea.min = alert
      areas.push(alertArea)
      warnArea.max = alert
    }
    if (!isNaN(warn) && (isNaN(alert) || (isLess ? alert < warn : alert > warn))) {
      warnArea.reverse = isLess
      warnArea.min = warn
      areas.push(warnArea)
    }

    const getArea = (t: any) => {
      const { min, max, reverse = false, include = false } = t;
      const hasMin = typeof min === 'number'
      const hasMax = typeof max === 'number'
      const getTitle = () => {
        if (t.title) {
          return t.title;
        } else if (hasMin || hasMax) {
          // 判断是否反向 > & >= 向上 reverse: false | < & <= 向下 reverse: true
          if (reverse) {
            return `${hasMax ? `${this.valTickFormat(max as number, unit)} ${include ? '<' : '<='} ` : ''}y${hasMin ? ` ${include ? '<=' : '<'} ${this.valTickFormat(min, unit)}` : ''}`
          } else {
            return `${hasMin ? `${this.valTickFormat(min, unit)} ${include ? '<=' : '<'} ` : ''}y${hasMax ? ` ${include ? '<' : '<='} ${this.valTickFormat(max as number, unit)}` : ''}`
          }
        }
        return ''
      }
      return [
        {
          name: getTitle(),
          yAxis: !reverse && hasMin ? min : reverse && hasMax ? max : null,
          itemStyle: { color: t.color },
          label: { color: t.textColor },
        },
        {
          yAxis: !reverse && hasMax ? max : reverse && hasMin ? min : null,
          itemStyle: { color: t.color },
          label: { color: t.textColor },
        },
      ]
    }

    return areas.map(t => getArea(t))
  }
}
</script>

<style lang="scss" scoped>
.basic-chart-wrapper{
  width: 100%;
  height: 100%;
  position: relative;

  .basic-chart-cont {
    height: 100%;
  }

  .empty-show{
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 13px;
    color: var(--color-text-secondary);
    background-color: var(--bg-color);
    border-radius: 5px;
    z-index: 9;
  }
}
</style>