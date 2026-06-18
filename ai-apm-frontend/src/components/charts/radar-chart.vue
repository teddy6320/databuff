<template>
  <div class="radar-chart-wrapper">
    <div class="radar-chart-cont" key="chart" ref="chart"></div>
    <div class="empty-show" key="empty" v-if="showEmpty || empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Getter, State } from 'vuex-class';
import { debounce } from '@/utils/common'
import dayjs from 'dayjs';
import deepClone from 'lodash/cloneDeep';
import { values } from 'lodash';

/* {
  name: 'item1',
  data: [
    { key: '2022-06-22 18:10', value: 1 }],
    { key: '2022-06-22 18:11', value: 1 }],
    { key: '2022-06-22 18:12', value: '-' }],
    { key: '2022-06-22 18:13', value: '-' }],
    { key: '2022-06-22 18:14', value: 1 }],
    { key: '2022-06-22 18:15', value: '-' }],
} */
interface SourceItem {
  name: string;     // seriesName
  data: Array<{     // 图表数据
    key: string,    // x轴key
    value: number | string // 仅用于判断是否连续，前后点同为数字则表示连续，数字本身无特殊意义
  }>;
}

@Component
export default class PiecewiseChart extends Vue {
  @Getter('themeVariables') private themeVars!: any;
  @State('themeChanged') private themeChanged!: boolean;

  @Prop({ default: () => [] }) private source!: SourceItem[];
  @Prop({ default: false }) private showEmpty!: boolean;  // 数据是否为空
  @Prop({ default: true }) private showLegend!: boolean;  // (选) 是否显示legend
  @Prop({ default: null }) private group!: string | null; // (选) 图表联动 group id
  @Prop({ default: 10 }) private lineWidth!: number;      // (选) 线条宽度
  @Prop() private clickEvent: any;                        // (选) 鼠标事件
  @Prop() private tooltipFormat: any;                     // (选) Tooltip处理方法
  @Prop({ default: '' }) private title: any;                     // (选) Title
  @Prop({ default: () => [] }) private colors!: any[];    // (选) 图表颜色

  public $refs!: {
    chart: HTMLDivElement
  }

  private myChart: any = {};
  private resizeHandler: any = null;

  private empty = false;

  get chartColors () {
    return [...(this.colors && this.colors.length ? this.colors : this.themeVars.colors)]
  }

  get chartLineWidth () {
    return this.lineWidth || 10
  }

  get propDataStr () {
    return JSON.stringify({
      source: this.source,
      showLegend: this.showLegend,
      group: this.group,
      lineWidth: this.lineWidth,
      colors: this.colors,
    })
  }
  @Watch('propDataStr')
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
    if (this.group) {
      this.myChart.group = this.group;
      this.$echarts.connect(this.group);
    }

    // 元素的点击事件
    if (this.clickEvent) {
      this.myChart.on('click', (params: any) => {
        this.clickEvent(params);
      });
    }
  }

  private updateEcharts () {
    this.myChart.clear();
    this.myChart.setOption(this.getOption(), true); // clear cache
  }

  private getOption () {
    const { legendData, seriesData, indicatorData } = this.formatData();

    this.empty = !seriesData.length && !indicatorData.length;

    const option = {
      tooltip: {
        backgroundColor: this.themeVars.tooltipBgColor,
        borderWidth: 0,
        padding: [10, 14],
        textStyle: {
          fontSize: 12,
          color: this.themeVars.colorTextRegular,
        },
        enterable: false,
        confine: false,
        appendToBody: true, // 是否将tootip放到body内
        extraCssText: `box-shadow:1px 1px 4px 0 ${this.themeVars.tooltipShadowColor};max-width:400px;max-height:300px;overflow:auto;word-break:break-all;white-space:normal;`,
        // formatter: (params: any) => {
        //   console.log(params)
        //   return params.marker + params.name;
        // },
      },
      legend: {
        show: this.showLegend,
        type: 'scroll',
        data: legendData,
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
      },
      grid: {
        top: 20,
        left: 10,
        right: 20,
        bottom: this.showLegend ? 40 : 15,
        containLabel: true,
      },
      radar: {
        indicator: indicatorData,
        axisLine: { show: false },
        radius: '60%',
        axisName: {
          fontSize: 10,
        },
        axisNameGap: 8,
      },
      series: {
        name: this.title,
        type: 'radar',
        areaStyle: {
          opacity: 0.2
        },
        symbol: 'none',
        data: seriesData,
      },
      color: this.chartColors,
    };
    return option;
  }

  private formatData () {
    // { key, value, name }
    const legendData: string[] = []
    const indicatorData: any[] = []
    const seriesData: any[] = []
    const _values: number[] = []
    const source: any[] = deepClone(this.source || []);
    source.forEach((item: any) => {
      indicatorData.push({ name: item.name, max: 100 });
      _values.push(item.value)
    });
    if (_values && _values.length) {
      seriesData.push({
        value: _values,
        name: ''
      })
    }

    return { legendData, seriesData, indicatorData }
  }

  public resize () {
    if (this.myChart && this.myChart.resize) {
      this.myChart.resize()
    }
  }
}
</script>

<style lang="scss" scoped>
.radar-chart-wrapper{
  width: 100%;
  height: 100%;
  position: relative;

  .radar-chart-cont {
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