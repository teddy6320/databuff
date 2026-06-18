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

@Component
export default class PieChart extends Vue {
  @Getter('themeVariables') private themeVars!: any;
  @State('themeChanged') private themeChanged!: boolean;

  @Prop({ default: () => [
    /* {
      key: 'item1',  // seriesName
      value: 30,
    } */
  ] }) private source!: any[];
  @Prop({ default: false }) private showEmpty!: boolean;  // 数据是否为空
  @Prop({ default: '' }) private title!: string;          // (选) 标题
  @Prop({ default: '' }) private unit!: string;           // (选) 单位
  @Prop({ default: false }) private showTooltip!: boolean;  // (选) 是否显示tooltip
  @Prop({ default: true }) private showLegend!: boolean;  // (选) 是否显示legend
  @Prop({ default: true }) private showLabel!: boolean;   // (选) 是否显示label
  @Prop({ default: '' }) private labelFormatter!: string;   // (选) labelFormatter
  @Prop({ default: 'center' }) private labelPosition!: 'outside' | 'center'; // (选) Label位置
  @Prop({ default: false }) private showTotal!: boolean;  // (选) 显示总数（只在labelPosition为outside时生效）
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

  get chartColors () {
    return [...(this.colors && this.colors.length ? this.colors : this.themeVars.colors)]
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

  get option() {
    const source = Array.from(this.sourceCopy || []);
    const { axisData, seriesData, maxIdx, total } = this.formatData(source)
    this.lastHoverItem = maxIdx
    let center = ['50%', '50%']
    if (this.showLegend && !this.title) {
      center = ['50%', '45%']
    } else if (!this.showLegend && this.title) {
      center = ['50%', '55%']
    }
    const title: any[] = []
    if (this.title) {
      title.push({
        text: this.title,
        textStyle: {
          color: this.themeVars.colorTextRegular,
          fontWeight: 'normal',
          fontSize: 14,
          lineHeight: 22,
        },
        left: 10,
        top: 6,
      })
    }
    let label: any = { show: false }
    if (this.showLabel) {
      if (this.labelPosition !== 'outside') {
        label = {
          show: false,
          position: 'center',
          formatter: [
            '{name|{b}}',
            // '{value|{c}}',
            '{percent|{d}%}'
          ].join('\n'),
          rich: {
            name: {
              fontSize: 14,
              color: this.themeVars.colorTextRegular,
              lineHeight: 24
            },
            value: {
              fontSize: 13,
              color: this.themeVars.colorTextRegular
            },
            percent: {
              fontSize: 12,
              color: this.themeVars.colorTextSecondary,
            }
          },
        }
      } else {
        label = {
          show: true,
          position: 'outside',
          formatter: this.labelFormatter || '{b}: {c}',
          fontSize: 13,
          color: this.themeVars.colorTextRegular
        }
        if (this.showTotal) {
          title.push({
            text: '{name|总量}\n{val|' + this.valTickFormat(total, this.unit) + '}',
            top: 'center',
            left: 'center',
            textStyle: {
              rich: {
                name: {
                  fontSize: 14,
                  fontWeight: 'normal',
                  color: this.themeVars.colorTextRegular,
                  padding: [2, 0],
                },
                val: {
                  fontSize: 20,
                  fontWeight: 'normal',
                  color: this.themeVars.colorTextPrimary,
                },
              },
            },
          })
        }
      }
    }

    const option = {
      title,
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
        show: this.showLegend,
        data: axisData,
        bottom: 5,
        itemGap: 20,
        itemWidth: 16,
        itemHeight: 6,
        icon: 'rect',
        textStyle: {
          color: this.themeVars.colorTextRegular,
          fontSize: 12,
          lineHeight: 22,
          overflow: 'truncate',
        },
        pageIconColor: this.themeVars.colorTextRegular,
        pageIconInactiveColor: this.themeVars.colorTextPlaceholder,
        pageTextStyle: {
          color: this.themeVars.colorTextRegular,
        },
      },
      grid: {
        top: this.title ? 50 : 20,
        left: 20,
        right: 20,
        bottom: this.showLegend ? 40 : 20,
        containLabel: true
      },
      series: [{
        type: 'pie',
        radius: ['45%', '65%'],
        center,
        // hoverAnimation: false,
        showEmptyCircle: false,
        label,
        emphasis: {
          label: {
            show: this.showLabel,
          }
        },
        data: seriesData,
      }],
      color: this.chartColors,
      animation: this.animation,
    }
    return option;
  }
  @Watch('option', { deep: true })
  private onoptionChanged(newVal: any, oldVal: any) {
    if (this.myChart) {
      if (newVal) {
        this.updateChart(newVal)
      } else {
        this.updateChart(oldVal)
      }
    } else {
      this.drawEcharts();
    }
  }
  @Watch('themeChanged')
  private onThemeChanged(value: boolean) {
    if (value) {
      if (this.myChart) {
        this.updateChart(this.option);
      } else {
        this.drawEcharts();
      }
    }
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
    this.updateChart(this.option);
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
    if (this.labelPosition === 'outside') {
      return
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

  private updateChart (option: any) {
    this.myChart.setOption(option, true); // clear cache
    if (this.labelPosition === 'outside') {
      return
    }
    this.myChart.dispatchAction({
      type: 'highlight',
      seriesIndex: 0,
      dataIndex: this.lastHoverItem,
    })
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
      value = +value.toFixed(3)
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
