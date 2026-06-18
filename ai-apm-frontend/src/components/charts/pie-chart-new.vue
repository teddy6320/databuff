<template>
  <div class="pie-chart-wrapper">
    <div class="pie-chart-cont" key='chart' ref="chart"></div>
    <div class="empty-show" key='empty' v-if="showEmpty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import { Getter, State } from 'vuex-class';
import i18n from '@/i18n';
import { debounce } from '@/utils/common'
import getUnitData from '@/utils/getUnitData';
import humanFormat from 'human-format';

export const getNameByWith = (name: string, width: number, fontsize: number) => {
  const scale = fontsize / 12
  const _width = width / scale
  const span = document.createElement('span')
  span.style.visibility = 'hidden'
  span.style.whiteSpace = 'nowrap'
  span.style.fontFamily = 'sans-serif'
  span.style.fontSize = '12px'
  document.body.appendChild(span)

  const getText = (str: string, suffix?: string) => {
    let text = ''
    for (let j = 0; j < str.length; j++) {
      span.innerText = text + str[j] + (suffix || '')
      if (span.offsetWidth <= _width) {
        text += str[j]
      } else {
        break
      }
    }
    return text  + (suffix || '')
  }

  let showName = getText(name)
  if (showName.length < name.length) {
    showName = getText(name, '…')
  }
  document.body.removeChild(span)
  return showName
}


@Component
export default class PieChartNew extends Vue {
  @State('theme') private theme!: 'dark' | 'light';
  @Getter('themeVariables') private themeVars!: any;
  @State('themeChanged') private themeChanged!: boolean;

  @Prop({ default: () => [
    /* {
      key: 'item1',  // seriesName
      value: 30,
      color: '#ff0000', // (选)
    } */
  ] }) private source!: any[];
  @Prop({ default: false }) private showEmpty!: boolean;  // 数据是否为空
  @Prop({ default: '' }) private unit!: string;           // (选) 单位
  @Prop({ default: true }) private showTooltip!: boolean;  // (选) 是否显示tooltip
  @Prop({ default: true }) private showLegend!: boolean;  // (选) 是否显示legend
  @Prop({ default: true }) private showTotal!: boolean;  // (选) 显示总数
  @Prop({ default: 'hover' }) private highlightTrigger!: 'hover' | 'click'; // highlight的触发方式
  @Prop({ default: true }) private animation!: boolean;   // (选) 是否开启动画
  @Prop() private clickEvent: any;                        // (选) 鼠标事件
  @Prop() private tooltipFormat: any;                     // (选) Tooltip处理方法
  @Prop({ default: () => [] }) private colors!: any[];    // (选) 图表颜色

  public $refs!: {
    chart: HTMLDivElement
  }

  private myChart: any = {};
  private resizeHandler: any = null;

  private lastHoverItem: number = 0

  private chartWidth = 300;

  get chartColors () {
    const colors = ['#2962FF', '#00AFF4', '#967EFF', '#1FC2BA', '#F79532', '#FD5151'];
    return [...(this.colors && this.colors.length ? this.colors : colors)]
  }

  get propDataStr () {
    return JSON.stringify({
      source: this.source,
      unit: this.unit,
      showTooltip: this.showTooltip,
      showLegend: this.showLegend,
      showTotal: this.showTotal,
      highlightTrigger: this.highlightTrigger,
      colors: this.colors,
    })
  }
  @Watch('propDataStr')
  private onPropDataStrChanged(newVal: any, oldVal: any) {
    if (newVal === oldVal) {
      return
    }
    if (this.myChart) {
      this.updateChart()
    } else {
      this.drawEcharts();
    }
  }
  @Watch('themeChanged')
  private onThemeChanged(value: boolean) {
    if (value) {
      if (this.myChart) {
        this.updateChart();
      } else {
        this.drawEcharts();
      }
    }
  }

  get sourceCopy () {
    let count = 0;
    return (this.source || []).map(t => {
      if (!t.key) {
        count++;
      }
      return { ...t, key: `${t.key || 'undefined'}${count > 1 ? `${count}` : ''}` }
    });
  }

  private mounted() {
    this.chartWidth = this.$refs.chart?.clientWidth || 300;

    this.drawEcharts();
    this.resizeHandler = debounce(() => {
      if (this.myChart) {
        this.chartWidth = this.$refs.chart?.clientWidth || 300;
        if (this.showLegend) {
          this.updateChart();
        }
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
      this.myChart.off('mouseover');
      this.myChart.off('highlight');
      this.myChart.off('globalout');
      this.myChart.off('click');
    }
    if (this.myChart.dispose) {
      this.myChart.dispose();
    }
    this.myChart = null
  }

  private drawEcharts() {
    this.myChart = this.$echarts.init(this.$refs.chart, '', { renderer: 'svg' });
    this.updateChart();
    if (this.clickEvent) {
      this.myChart.on('click', (params: any) => {
        if (!this.clickEvent) {
          return;
        }
        if (this.highlightTrigger !== 'hover') {
          this.highlight(params)
        }
        this.clickEvent(params);
      });
    }
    if (this.highlightTrigger === 'hover') {
      this.myChart.on('mouseover', (e: any) => {
        this.highlight(e)
      })
    }
    this.myChart.on('highlight', (e: any) => {
      if (Object.prototype.hasOwnProperty.call(e, 'dataIndex')) {
        //
      } else {
        // 如果当前名称不是高亮的节点，取消高亮
        if (Object.prototype.hasOwnProperty.call(e, 'name') && e.name !== this.sourceCopy[this.lastHoverItem].key) {
          this.myChart.dispatchAction({
            type: 'downplay',
            seriesIndex: 0,
            dataIndex: this.lastHoverItem
          })
        }
      }
    })
    this.myChart.on('globalout', (e: any) => {
      this.myChart.dispatchAction({
        type: 'highlight',
        seriesIndex: 0,
        dataIndex: this.lastHoverItem || 0
      })
    })
  }

  private highlight (e: any) {
    this.myChart.dispatchAction({
      type: 'downplay',
      seriesIndex: 0,
      dataIndex: this.lastHoverItem
    })
    if (typeof e.dataIndex === 'number') {
      this.myChart.dispatchAction({
        type: 'highlight',
        seriesIndex: 0,
        dataIndex: e.dataIndex
      })
      this.lastHoverItem = e.dataIndex
    }
  }

  private updateChart () {
    this.myChart.clear();
    this.myChart.setOption(this.getOption(), true); // clear cache
    this.myChart.dispatchAction({
      type: 'highlight',
      seriesIndex: 0,
      dataIndex: this.lastHoverItem,
    })
  }

  private getOption () {
    const source = Array.from(this.sourceCopy || []);
    const { axisData, seriesData, maxIdx, total } = this.formatData(source)
    this.lastHoverItem = maxIdx
    let center = ['50%', '50%']
    if (this.showLegend) {
      center = ['25%', '50%']
    }
    const placeholderData: any[] = [];
    if (this.showTotal) {
      placeholderData.push({
        key: '2',
        value: 1,
        itemStyle: {
          color: this.theme === 'dark' ? '#242424' : '#F4F5F7',
        },
        label: {
          show: true,
          position: 'center',
          formatter: '{name|总量}\r\n{val|' + this.valTickFormat(total, this.unit) + '}',
          rich: {
            name: {
              fontSize: 14,
              color: this.theme === 'dark' ? '#8B8E93' : '#121317',
              fontWeight: 400,
              padding: [0, 0, 5, 0],
            },
            val: {
              fontSize: 20,
              color: this.theme === 'dark' ? '#EBEBED' : '#121317',
              fontWeight: 500,
            },
          },
        }
      })
    }

    const legendLabelWidth = this.chartWidth / 2 - 85;

    const option = {
      tooltip: {
        show: this.showTooltip,
        backgroundColor: this.themeVars.tooltipBgColor,
        borderWidth: 0,
        padding: [10, 14],
        textStyle: {
          fontSize: 12,
          color: this.themeVars.colorTextRegular,
        },
        enterable: true,
        confine: true,
        appendToBody: true, // 是否将tootip放到body内
        extraCssText: `box-shadow:1px 1px 4px 0 ${this.themeVars.tooltipShadowColor};max-width:400px;max-height:300px;overflow:auto;word-break:break-all;white-space:normal;`,
        // formatter: '{b}: {c}<br/> 占比: {d}%',
        formatter: (params: any) => {
          if (params.name === 'placeholder') {
            return ''
          }
          if (this.tooltipFormat) {
            // 对 Tooltip 处理
            return this.tooltipFormat(params, this.valTickFormat)
          }
          return i18n.t('modules.components.charts.s_d263ec4e', { value0: params.name, value1: params.data._value, value2: params.percent }) as string
        },
      },
      legend: {
        type: 'scroll',
        orient: 'vertical',
        show: this.showLegend,
        data: axisData,
        left: '50%',
        top: 'center',
        itemGap: 8,
        itemWidth: 6,
        itemHeight: 6,
        icon: 'roundRect',
        textStyle: {
          lineHeight: 18,
          rich: {
            name: {
              width: legendLabelWidth,
              padding: [0, 0, 0, 3],
              fontSize: 13,
              color: this.theme === 'dark' ? '#8B8E93' : '#626467',
              fontWeight: 400,
            },
            value: {
              width: 50,
              padding: [0, 0, 0, 16],
              fontSize: 13,
              color: this.theme === 'dark' ? '#8B8E93' : '#626467',
              fontWeight: 400,
            },
          },
        },
        formatter: (name: string) => {
          const item = seriesData.find((t: any) => t.name === name);
          return '{name|' + getNameByWith(name, legendLabelWidth, 13) + '}{value|' + ((item || {})._value || '') + '}';
        },
        pageIconColor: this.themeVars.colorTextRegular,
        pageIconInactiveColor: this.themeVars.colorTextPlaceholder,
        pageTextStyle: {
          color: this.themeVars.colorTextRegular,
        },
      },
      grid: {
        top: 20,
        left: 20,
        right: 20,
        bottom: 20,
        containLabel: true
      },
      series: [{
        type: 'pie',
        radius: this.showLegend ? ['41%', '50%'] : ['48%', '57%'],
        center,
        showEmptyCircle: false,
        label: { show: false },
        data: seriesData,
      }, {
        name: 'placeholder',
        type: 'pie',
        silent: true,
        radius: this.showLegend ? ['38%', '53%'] : ['45%', '60%'],
        center,
        emptyCircleStyle: {
          color: '#F4F5F7',
        },
        data: placeholderData,
        z: -1,
      }],
      color: this.chartColors,
      animation: this.animation,
    }
    return option;
  }

  private formatData (source: any[]) {
    const axisData: any[] = []
    const seriesData: any[] = []
    let max = 0
    let maxIdx = 0
    let total = 0
    source.forEach((item: any, index: number) => {
      axisData.push(item.key)
      seriesData.push({
        ...item,
        name: item.key,
        value: item.value,
        _value: this.valTickFormat(item.value, this.unit),
        itemStyle: item.color ? { color: item.color } : null,
      })
      total += item.value
      if (item.value > max) {
        max = item.value
        maxIdx = index
      }
    })
    return {
      axisData, seriesData, maxIdx, total
    }
  }

  private valTickFormat(val: number, unit: string = '') {
    if (typeof val !== 'number') {
      return val
    }
    const { scale_factor, scale, sub_unit } = getUnitData(unit);
    const vData = humanFormat.raw(Number(val) * scale_factor, {
      ...scale,
      decimals: 12,
    })
    let value = vData.value
    if (Math.abs(value) >= 1 || value === 0) {
      value = +value.toFixed(0)
    } else if (Math.abs(value) < 0.0001) {
      value = (+value.toPrecision(3)).toExponential()
    } else if (Math.abs(value) < 0.01) {
      value = +value.toPrecision(3)
    } else {
      value = +value.toFixed(4)
    }
    return `${value}${scale.separator}${vData.prefix}${scale.unit + sub_unit}`.trim()
  }
}
</script>

<style lang="scss" scoped>
.pie-chart-wrapper{
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;

  .pie-chart-cont {
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
