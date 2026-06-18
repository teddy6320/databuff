<template>
  <div class="flex-h">
    <div class="span-flame-graph" ref="graphWrapper">
      <canvas id="flameGraphChart"></canvas>
    </div>
    <div class="service-info">
      <div class="service-info-header flex-h">
        <div class="sub-describe font-12">{{ $t('modules.views.alarmCenter.problemDetail.s_47d68cd0') }}</div>
        <div class="service-info-header-r sub-describe tr font-12">{{ $t('modules.views.appMonitor.traceDetail.s_5100743a') }}</div>
      </div>
      <div class="service-info-body">
        <div v-for='(item, i) in serviceInfoList' :key="i" class="service-info-item flex-v mt-5">
          <div class="flex-h mb-5">
            <span class="service-type-name ell sub-describe flex-1" :title="item.key">{{ item.key }}</span>
            <span class="sub-describe">{{ item.value | PercentFilter(true) }}</span>
          </div>
          <div class="process-info flex-h">
            <span class="process-bar flex-h">
              <span class="process-bar-inner" :style='{ width: `${Math.min(item.value * 100, 100)}%` }'></span>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Getter, State } from 'vuex-class';
import humanFormat from 'human-format';
import FlameChart from '@/components/flame-chart-js';

const nsFormat = new humanFormat.Scale({
  ns: 1,
  µs: 1000,
  ms: 1000000,
  s: 1e9,
  min: 60 * 1e9,
})
const msFormat = new humanFormat.Scale({
  ms: 1,
  s: 1000,
  min: 60 * 1000,
  h: 60 * 60 * 1000,
});

const defaultColors = [
  '#5180FF', '#8368FA', '#35C8C0', '#2BB9F1', '#5180FF',
  '#F79F46', '#91c7ae', '#a0a7e6', '#2f4554',
  '#61a0a8', '#d48265', '#749f83', '#ca8622',
  '#bda29a', '#6e7074', '#546570', '#c4ccd3'
]

@Component
export default class FlameChartComp extends Vue {
  @Getter('themeVariables') private themeVars!: any;
  @State('themeChanged') private themeChanged!: boolean;

  @Prop({ default: () => [] }) private spanParents!: any[];
  @Prop({ default: () => [] }) private spanTypes!: any[];
  @Prop({ default: () => [] }) private serviceInfoList!: any[];
  @Prop({ default: () => ({}) }) private selectedSpan!: any;

  @Watch('themeChanged')
  private onThemeChanged(value: boolean) {
    if (value && this.flameInited && this.flameChart) {
      this.makeFlameCanvas();
    }
  }

  @Watch('selectedSpan')
  private onShowChanged(span: any) {
    //
  }

  @Watch('spanTypes', { immediate: true })
  private onSpanTypesChanged() {
    const colors: any = {};
    this.spanTypes.forEach((type: string, index: number) => {
      colors[type] = defaultColors[index % defaultColors.length]
    })
    this.flameColors = colors
  }

  public $refs!: {
    graphWrapper: HTMLDivElement
  }

  private flameColors: any[] = [];
  private flameInited = false;
  private flameChart: any = null;
  private flameCanvas: any = null;

  private mounted () {
    this.$nextTick(() => {
      if (this.selectedSpan && !this.flameInited) {
        const canvas = document.getElementById('flameGraphChart');
        (canvas as HTMLCanvasElement).width = this.$refs.graphWrapper.clientWidth;
        (canvas as HTMLCanvasElement).height = this.$refs.graphWrapper.clientHeight;
        this.flameCanvas = canvas;
        this.makeFlameCanvas()
      } else if (this.selectedSpan && this.flameInited && this.flameChart && this.flameChart.setData) {
        // 选中节点
        const chartPlugin = this.flameChart.plugins.find((plugin: any) => plugin.firstSelected)
        if (chartPlugin && this.selectedSpan) {
          chartPlugin.firstSelected = this.selectedSpan
        }
        this.flameChart.setData(this.spanParents)
      }
    })
  }

  private beforeDestroy () {
    this.flameChart = null;
  }

  private async makeFlameCanvas () {
    const themeVars = this.themeVars

    const flameChart = new (FlameChart as any)({
      canvas: this.flameCanvas,
      data: this.spanParents,
      colors: this.flameColors,
      firstSelected: this.selectedSpan,
      settings: {
        timeUnits: 'ns',
        styles: {
          main: {
            blockHeight: 24,
            font: '10px Roboto',
            backgroundColor: themeVars.bgColor,
            // fontColor: themeVars.colorTextPrimary,
            fontColor: 'white',
            tooltipHeaderFontColor: themeVars.colorTextRegular,
            tooltipBodyFontColor: themeVars.colorTextRegular,
            tooltipBackgroundColor: themeVars.tooltipBgColor,
          },
          timeGrid: {
            color: 'transparent',
          },
          timeGridPlugin: {
            font: '7px Roboto',
            fontColor: themeVars.colorTextRegular,
          },
          timeframeSelectorPlugin: {
            font: '7px Roboto',
            backgroundColor: themeVars.bgColor,
            fontColor: themeVars.colorTextRegular,
            overlayColor: 'rgba(112, 112, 112, 0.15)',
            graphStrokeColor: themeVars.borderColorBase,
            graphFillColor: 'rgba(196, 200, 225, 0.1)',
            bottomLineColor: themeVars.borderColorLighter,
            height: 40
          },
        },
        tooltip: (hoveredRegion: any, renderEngine: any, mouse: any) => {
          if (hoveredRegion) {
            const { data: { start, duration, children, name } } = hoveredRegion;
            // console.log(start, duration, children, name)
            const timeUnits = renderEngine.getTimeUnits();
            const selfTime = duration - (children ? children.reduce((prev: any, curr: any) => prev + curr.duration, 0) : 0);
            // console.log(timeUnits)
            let formatedStart = start;
            let formatedDuration = duration;
            let formatedSelfTime = selfTime;
            if (timeUnits === 'ns') {
              formatedStart = humanFormat(Number(start), { scale: nsFormat })
              formatedDuration = humanFormat(Number(duration), { scale: nsFormat })
              formatedSelfTime = humanFormat(Number(selfTime), { scale: nsFormat })
            } else if (timeUnits === 'ms') {
              formatedStart = humanFormat(Number(start), { scale: msFormat })
              formatedDuration = humanFormat(Number(duration), { scale: msFormat })
              formatedSelfTime = humanFormat(Number(selfTime), { scale: msFormat })
            } else {
              formatedStart = humanFormat(Number(start), { decimals: Number(start) > 1000 ? 1 : 0 })
              formatedDuration = humanFormat(Number(duration), { decimals: Number(duration) > 1000 ? 1 : 0 })
              formatedSelfTime = humanFormat(Number(selfTime), { decimals: Number(selfTime) > 1000 ? 1 : 0 })
            }

            const header = `${name}`;
            const dur = i18n.t('modules.views.appMonitor.traceDetail.s_5475349c', { value0: formatedDuration }) as string
            const st = i18n.t('modules.views.appMonitor.traceDetail.s_d21755cb', { value0: formatedStart }) as string
            renderEngine.renderTooltipFromData(
              [{ text: header }, { text: dur }, { text: st }],
              mouse
            )
            // const dur = `duration: ${duration.toFixed(nodeAccuracy)} ${timeUnits} ${children && children.length ? `(self ${selfTime.toFixed(nodeAccuracy)} ${timeUnits})` : ''}`;
            // const st = `start: ${start.toFixed(nodeAccuracy)}`;
          }
        }
      }
    });
    flameChart.on('select', (node: any) => this.$emit('on-change', node));
    this.flameInited = true;
    this.flameChart = flameChart
  }
}
</script>

<style lang="scss" scoped>
.span-flame-graph{
  width: 100%;
  font-size: 0;
  height: 100%;
}

.service-info{
  flex: none;
  margin: -9px -10px 0 0;
  width: 200px;
  height: calc(100% + 20px);
  overflow-y: auto;
  position: relative;
  padding: 9px 10px 10px;
  background: var(--bg-color03);
  border-left: 1px solid var(--border-color-base);

  .service-info-header{
    justify-content: space-between;
    height: 28px;
    line-height: 28px;
    border-bottom: 1px solid var(--border-color-base);
    overflow: hidden;
    & > div{
      flex: 1;
    }
    .service-info-header-r{
      width: 100px;
      flex: none;
    }
  }
  .service-info-body{
    .service-info-item{
      justify-content: space-between;
      overflow: hidden;
      // height: 50px;
      margin-bottom: 10px;

      .service-type-name{
        padding-right: 8px;
      }

      .process-info{
        width: 100%;
        flex: none;
        justify-content: space-between;
      }

      & > div{
        flex: 1;
        overflow: hidden;
        font-size: 12px;
      }

      .process-bar{
        width: 100%;
        height: 2px;
        background-color: var(--border-color-light);
        font-size: 0;
        justify-content: space-between;

        .process-bar-inner{
          display: inline-block;
          height: 100%;
          background-color: var(--color-primary);
          vertical-align: top;
        }
      }
    }
  }
}

:root[data-theme=light] .service-info {
  background-color: #fff;
}
</style>
