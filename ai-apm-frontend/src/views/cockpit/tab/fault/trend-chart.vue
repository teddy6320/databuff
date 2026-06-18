<template>
  <div class="trend-chart-wrapper">
    <div class="flex-h-jc lh-14 height-14 mb-10">
      <span>{{ source[0] && source[0].key }}</span>
      <span>{{ source[source.length - 1] && source[source.length - 1].key }}</span>
    </div>

    <div ref="sliderChart" :class="{ hide: loading }" class="slider-chart"></div>
    <div :class="{ hide: !minuteSliderTotal }" class="minute-slider">
      <span v-for="i in minuteSliderTotal" :key="i"
        :class="{
          active: (typeof activeIndex === 'number' && i >= activeIndex - 2 && i <= activeIndex + 2) ||
              (typeof hoverIndex === 'number' && i >= hoverIndex - 2 && i <= hoverIndex + 2),
          warning: minuteSource[i - 1] && minuteSource[i - 1].trafficLight === 'yellow',
          critical: minuteSource[i - 1] && minuteSource[i - 1].trafficLight === 'red',
        }"
        @mouseenter="minuteSliderMouseEnter(i)" @mouseleave="hoverIndex = null"
        @click="minuteSliderClick"
        class="minute-t"></span>
      <span
        v-show="typeof activeIndex === 'number'"
        :style="{ left: (activeIndex - 0.5) / minuteSliderTotal * 100 + '%' }"
        class="active-arrow"></span>
      <span
        v-show="typeof hoverIndex === 'number'"
        :style="{ left: (hoverIndex - 0.5) / minuteSliderTotal * 100 + '%' }"
        class="hover-arrow"></span>
      <span
        v-show="typeof hoverIndex === 'number'"
        :style="{ left: (hoverIndex - 0.5) / minuteSliderTotal * 100 + '%' }"
        class="hover-tooltip sub-describe">{{ hoverKeysTooltip }}</span>
    </div>
    <div class="flex-h-jc lh-14 height-14 mt-6 font-12 sub-describe">
      <span>{{ source[zoomStartValue] && source[zoomStartValue].key }}</span>
      <span>{{ source[zoomEndValue] && source[zoomEndValue].key }}</span>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import debounce from 'lodash/debounce';

const getIndex = (index: number, total: number) => {
  if (total < 5) {
    return Math.ceil(total / 2)
  } else if (index + 2 > total) {
    return total - 2
  } else if (index - 2 <= 0) {
    return 3
  } else {
    return index
  }
}

@Component
export default class TrendChart extends Vue {
  @Prop({ default: false }) private loading!: boolean;
  @Prop({ default: () => [] }) private source!: any[];
  @Prop({ default: '' }) private sourceKey!: string;

  public $refs!: {
    sliderChart: HTMLDivElement
  }

  @Watch('sourceKey', { immediate: true })
  private onSourceKeyChange () {
    if (!this.source.length) {
      this.minuteSource = [];
      this.activeIndex = null;
      this.hoverIndex = null;
      this.prevEndKey = '';
      this.zoomStartValue = null;
      this.zoomEndValue = null;
      if (this.myChart) {
        if (this.myChart.off) {
          this.myChart.off('datazoom');
        }
        if (this.myChart.dispose) {
          this.myChart.dispose();
        }
        this.myChart = null;
      }
      return;
    }
    this.minuteSource = this.source.slice(-60);
    this.activeIndex = getIndex(this.minuteSliderTotal, this.minuteSliderTotal);
    this.prevEndKey = this.minuteSource[this.minuteSliderTotal - 1]?.key || '';
    this.zoomEndValue = this.source.length - 1;
    this.zoomStartValue = Math.max(0, this.source.length - this.minuteSliderTotal);
    this.initChart();
  }

  private myChart: any = null;

  private zoomStartValue: number | null = null;
  private zoomEndValue: number | null = null;

  private resizeHandler: any = null;

  private minuteSource: any[] = [];
  private activeIndex: number | null = null; // 1 - 60
  private hoverIndex: number | null = null;
  private prevEndKey: string = '';
  get minuteSliderTotal () {
    return this.minuteSource.length;
  }
  get hoverKeysTooltip () {
    if (typeof this.hoverIndex === 'number' && this.minuteSource.length) {
      const start = Math.max(this.hoverIndex - 2, 1) - 1;
      const end = Math.min(this.hoverIndex + 2, this.minuteSliderTotal) - 1;
      if (start !== end) {
        return (this.minuteSource[start]?.key || '').slice(11, 16) + ' ~ ' + (this.minuteSource[end]?.key || '').slice(11, 16);
      }
      return (this.minuteSource[start]?.key || '').slice(11, 16);
    }
    return '';
  }

  private beforeDestroy () {
    window.removeEventListener('resize', this.resizeHandler)
    if (!this.myChart) {
      return
    }
    if (this.myChart.off) {
      this.myChart.off('datazoom');
    }
    if (this.myChart.dispose) {
      this.myChart.dispose();
    }
    this.myChart = null
  }

  private initChart () {
    if (!this.source.length || !this.$refs.sliderChart) {
      return;
    }
    const axisData: string[] = []
    const seriesData: number[] = []
    this.source.forEach((i: any) => {
      if (!i || !i.key) {
        return;
      }
      axisData.push(i.key)
      seriesData.push(i.value)
    })
    if (!axisData.length) {
      return;
    }
    let zoom: any = {
      startValue: this.zoomStartValue,
      endValue: this.zoomEndValue,
    }
    if (this.zoomStartValue === this.zoomEndValue || this.zoomEndValue === null || this.zoomStartValue === null) {
      zoom = { start: 0, end: 100 }
    }
    const options = {
      grid: {
        left: 0,
        right: 0,
        top: 0,
        bottom: 0,
      },
      xAxis: {
        show: false,
        type: 'category',
        boundaryGap: [0, 0],
        data: axisData || [],
      },
      yAxis: {
        show: false,
        boundaryGap: [0, 0],
        type: 'value',
      },
      dataZoom: [
        {
          type: 'slider',
          zoomLock: true,
          brushSelect: false,
          realtime: false,
          // throttle: 100,
          ...zoom,
          backgroundColor: '#F5F6F7',
          dataBackground: {
            lineStyle: {
              width: 1,
              color: '#DFE0E2',
            },
            areaStyle: {
              opacity: 0,
            },
          },
          selectedDataBackground: {
            lineStyle: {
              width: 1,
              color: '#B2C6FF',
            },
            areaStyle: {
              color: '#B2C6FF',
              opacity: 0.2,
            },
          },
          fillerColor: 'rgba(131,164,255,0.5)',
          borderColor: '#DFE0E2',
          borderRadius: 2,
          handleIcon: 'M0,0 v10 h0.1 v-10 z',
          handleSize: 5,
          handleStyle: {
            borderWidth: 1,
            borderColor: '#83A4FF',
            borderCap: 'round',
            borderJoin: 'round',
            opacity: 1,
          },
          textStyle: {
            color: '#45474A',
            fontSize: 8,
          },
          left: -2.5,
          right: 2.5,
          top: 1.5,
          bottom: 2.5,
        },
      ],
      series: [
        {
          type: 'line',
          name: i18n.t('modules.views.alarmCenter.problemDetail.s_1d4cbadb') as string, nameKey: 'modules.views.alarmCenter.problemDetail.s_1d4cbadb',
          showSymbol: false,
          silent: true,
          animation: false,
          lineStyle: {
            opacity: 0,
          },
          data: seriesData || []
        }
      ]
    }
    if (!this.myChart && this.source.length) {
      this.myChart = this.$echarts.init(this.$refs.sliderChart, '', { renderer: 'svg' });
      this.myChart.setOption(options);

      this.myChart.on('datazoom', debounce((e) => {
        // console.log('datazoom', this.myChart.getOption().dataZoom[0]);
        const { startValue, endValue } = this.myChart.getOption().dataZoom[0]
        this.zoomStartValue = startValue;
        this.zoomEndValue = endValue;
        this.minuteSource = this.source.slice(startValue, endValue + 1);
        this.activeIndex = getIndex(this.minuteSliderTotal, this.minuteSliderTotal);
        if (this.prevEndKey !== (this.minuteSource[this.minuteSliderTotal - 1]?.key || '')) {
          this.prevEndKey = this.minuteSource[this.minuteSliderTotal - 1]?.key || ''
          this.$emit('change', this.prevEndKey);
        }
      }, 300))
    } else if (this.myChart) {
      this.myChart.clear();
      this.myChart.setOption(options, true); // clear cache
    }

    if (!this.resizeHandler && this.myChart) {
      this.resizeHandler = debounce(() => {
        this.myChart.resize()
      }, 100)
      window.addEventListener('resize', this.resizeHandler)
    }
  }

  private minuteSliderMouseEnter (index: number) {
    this.hoverIndex = getIndex(index, this.minuteSliderTotal);
  }
  private minuteSliderClick () {
    this.activeIndex = this.hoverIndex
    if (typeof this.activeIndex === 'number') {
      const end = Math.min(this.activeIndex + 2, this.minuteSliderTotal) - 1;
      if (this.prevEndKey !== (this.minuteSource[end]?.key || '')) {
        this.prevEndKey = this.minuteSource[end]?.key || '';
        this.$emit('change', this.prevEndKey);
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.trend-chart-wrapper {
  .height-14 {
    height: 14px;
  }
  .hide {
    opacity: 0;
    pointer-events: none;
    z-index: -1;
  }
}

.slider-chart {
  width: 100%;
  height: 14px;
  position: relative;
  overflow: hidden;
}

.minute-slider {
  padding-top: 12px;
  width: calc(100% + 3px);
  height: 40px;
  display: flex;
  align-items: flex-end;
  position: relative;

  .minute-t {
    flex: 1;
    margin-right: 3px;
    height: 24px;
    border-radius: 2px;
    background: rgba(147, 227, 169, 0.7);
    transition: all 0.1s;
    cursor: pointer;
    position: relative;
    &.warning {
      background: rgba(249, 217, 142, 0.7);
    }
    &.critical {
      background: rgba(247, 149, 141, 0.7);
    }
    &:hover,
    &.active {
      height: 28px;
      background: #08BE7E;
      &.warning {
        background: #F79532;
      }
      &.critical {
        background: #E12828;
      }
    }
    &::before {
      content: '';
      width: 3px;
      height: 100%;
      position: absolute;
      right: -3px;
      top: 0;
    }
  }

  .hover-arrow,
  .active-arrow {
    margin-left: -6.5px;
    display: block;
    border-top: 6px solid var(--color-text-regular);
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    position: absolute;
    top: 4px;
    left: 0;
    pointer-events: none;
  }

  .hover-tooltip {
    padding: 0 10px;
    height: 30px;
    border-radius: 4px;
    background: rgba(255, 255, 255, 0.85);
    border: 1px solid #FFFFFF;
    backdrop-filter: blur(10px);
    box-shadow: 0px 4px 10px 0px rgba(119, 122, 126, 0.14);
    white-space: nowrap;
    font-size: 12px;
    line-height: 30px;
    position: absolute;
    top: -30px;
    left: 0;
    pointer-events: none;
    transform: translate(-50%, 0);
  }

  .hover-arrow,
  .hover-tooltip {
    transition: all .1s ease;
  }
}
</style>
