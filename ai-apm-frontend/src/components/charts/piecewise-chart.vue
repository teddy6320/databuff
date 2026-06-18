<template>
  <div class="piecewise-chart-wrapper">
    <div class="piecewise-chart-cont" key="chart" ref="chart"></div>
    <div class="empty-show" key="empty" v-if="showEmpty || empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Getter, State } from 'vuex-class';
import { debounce } from '@/utils/common'
import dayjs from 'dayjs';
import deepClone from 'lodash/cloneDeep';

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

  // x轴是否为时间格式
  get isTimeKey () {
    // 原始数据全量keys
    const sourceKeys = Array.from(new Set((this.source || []).map((item: any) => item.data).flat().map(t => t.key)))
    /*
      '2022-09-02 14:51:34'
      '2022-09-02 14:52'
      '2022/09/02 14:51:34'
      '2022/09/02 14:52'
    */
    const timeReg = new RegExp(/^\d{4}([-|\/]\d{2}){2} \d{2}(:\d{2}){1,2}$/)
    // const timeReg = new RegExp(/(^(\d{4}[-|\/])?\d{1,2}(-|\/)\d{1,2}( \d{1,2}(:\d{1,2}){1,2})?$)|(^\d{1,2}(:\d{1,2}){1,2}$)/)
    return !!sourceKeys.length && sourceKeys.every(key => timeReg.test(key))
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
    const { legendData, xAxisData, seriesData } = this.formatData();
    const timeSpan = this.getTimeSpan(xAxisData)

    this.empty = !!seriesData.length && !xAxisData.length;

    // xAxis
    const xAxisOption: any = {
      type: 'category',
      data: xAxisData,
      axisLabel: {
        color: this.themeVars.colorTextRegular,
        fontSize: '12',
        margin: timeSpan.span === 'H' ? 15 : 8,
        formatter: (val: string) => {
          if (!val) {
            return '-';
          } else if (!this.isTimeKey) {
            return val.length > 20 ? `${val.slice(0, 10)}\n${val.slice(10, 17)}...` :
                val.length > 10 ? `${val.slice(0, 10)}\n${val.slice(10, 20)}` : val
          }
          return val.substring(timeSpan.start).split(' ').reverse().join('\n');
        },
      },
      axisLine: {
        show: !!xAxisData.length,
        lineStyle: { color: this.themeVars.borderColorLighter }
      },
      axisTick: {
        lineStyle: { color: this.themeVars.borderColorLighter },
        alignWithLabel: true,
      },
      splitLine: { show: false },
    }

    // yAxis
    const yAxisOption: any = {
      type: 'value',
      minInterval: 1,
      max: legendData.length + 1,
      axisLabel: { show: false },
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: {
        show: true,
        lineStyle: { color: this.themeVars.borderColorLighter },
      },
    }

    // series
    const seriesOption: any[] = legendData.map((name: any, idx) => ({
      name,
      type: 'custom',
      renderItem: this.renderItem,
      encode: {
        x: [1, 2],
        y: 0
      },
      data: seriesData[idx],
    }));

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
        formatter: (params: any) => {
          return params.marker + params.name;
        },
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
        pageIconColor: this.themeVars.colorTextRegular,
        pageIconInactiveColor: this.themeVars.colorTextPlaceholder,
        pageTextStyle: {
          color: this.themeVars.colorTextRegular,
        },
      },
      grid: {
        top: 20,
        left: 10,
        right: 20,
        bottom: this.showLegend ? 40 : 15,
        containLabel: true,
      },
      xAxis: xAxisOption,
      yAxis: yAxisOption,
      series: seriesOption,
      color: this.chartColors,
    };
    return option;
  }

  private renderItem (params: any, api: any) {
    const categoryIndex = api.value(0);
    const start = api.coord([api.value(1), categoryIndex]);
    const end = api.coord([api.value(2), categoryIndex]);
    const heightStep = api.size([0, 1])[1] * 0.9;
    const height = Math.min(this.chartLineWidth, heightStep);
    const rectShape = this.$echarts.graphic.clipRectByRect(
      {
        x: start[0] - height / 2,
        y: start[1] - height / 2,
        width: end[0] - start[0] + height,
        height
      },
      {
        x: params.coordSys.x,
        y: params.coordSys.y,
        width: params.coordSys.width,
        height: params.coordSys.height
      }
    );
    // 圆角
    if (rectShape) {
      rectShape.r = [height / 2]
    }
    return (
      rectShape && {
        type: 'rect',
        transition: ['shape'],
        shape: rectShape,
        style: api.style()
      }
    );
  }

  private formatData () {
    const source: any[] = deepClone(this.source || []).filter(item => (item.data || []).length);
    source.forEach((item: any) => {
      const tData: any[] = item.data || [];
      const keys = tData.map(t => t.key)
      const formatter = `YYYY-MM-DD HH:mm${keys[0].length === 16 ? '' : ':ss'}`
      item.data = keys.map((key, i) => ({
        key: this.isTimeKey ? dayjs(key).format(formatter) : key,
        value: tData[i].value,
      }));
    });

    const xAxisData: string[] = Array.from(new Set(source.map((item: any) => item.data).flat().map(t => t.key)))
    if (this.isTimeKey) { // 时间排序
      xAxisData.sort()
    }
    const legendData: string[] = []
    const seriesData: any[][] = []
    source.forEach((item, index) => {
      legendData.push(item.name);
      seriesData.push(this.transformData(xAxisData, item.data, item.name, index + 1));
    });
    return { legendData, xAxisData, seriesData }
  }

  private transformData (keys: string[], data: any[], name: string, value: number) {
    const isNum = (num: any) => typeof num === 'number'
    const getValue = (list: any[], k: string) => {
      const val = (list.find((t: any) => t.key === k) || {}).value
      return isNum(val) ? val : '-'
    }

    const result = [];
    let currentGroup: any = null;

    keys.forEach((key) => {
      const val = getValue(data, key);

      // 如果 val 是数字
      if (isNum(val)) {
        // 如果当前组还没有开始，创建一个新的组
        if (!currentGroup) {
          currentGroup = {
            name,
            value: [value, key, key, 0] // [value, start, end, count]
          };
        } else {
          // 更新当前组的结束时间
          currentGroup.value[2] = key;
          // 更新 count (连续的次数)
          currentGroup.value[3]++;
        }
      } else {
        // 如果遇到空值并且当前组存在，则保存当前组并清空
        if (currentGroup) {
          result.push(currentGroup);
          currentGroup = null;
        }
      }
    })

    // 如果循环结束后还有未保存的组，保存它
    if (currentGroup) {
      result.push(currentGroup);
    }

    return result;
  }

  private getTimeSpan (keys: string[]) {
    if (this.isTimeKey && keys.length) {
      const ymd01 = keys[0].split(' ')[0].split('-');
      const ymd02 = keys.slice(-1)[0].split(' ')[0].split('-');
      if (ymd01[0] !== ymd02[0]) {
        return { span: 'Y', start: 0 };
      } else if (ymd01[1] !== ymd02[1]) {
        return { span: 'M', start: 5 };
      } else if (ymd01[2] !== ymd02[2]) {
        return { span: 'D', start: 5 };
      }
    }
    return { span: 'H', start: 11 };
  }

  public resize () {
    if (this.myChart && this.myChart.resize) {
      this.myChart.resize()
    }
  }
}
</script>

<style lang="scss" scoped>
.piecewise-chart-wrapper{
  width: 100%;
  height: 100%;
  position: relative;

  .piecewise-chart-cont {
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