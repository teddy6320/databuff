<template>
  <div class="bar-chart-wrapper">
    <div ref="chart" style='height: 100%;' />
    <div class="empty-show describe" v-if='showEmpty'>{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">import i18n from '@/i18n';

  import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
  import { Getter, State } from 'vuex-class';
  import getUnitData from '@/utils/getUnitData';
  import humanFormat from 'human-format';

  @Component
  export default class ChartLatency extends Vue {
    @Getter('themeVariables') private themeVars!: any;

    @Prop({ default: false }) private showEmpty!: boolean;
    @Prop({ default: 0 }) private interval: any;
    @Prop({ default: false }) private logModel: any;
    @Prop() private source: any;
    @Prop() private unit!: string;
    @Prop() private markLineSource: any;
    @Prop() private clickEvent: any;
    @Prop({ default: () => [] }) private colors!: any[];

    private myChart: any = {};
    private mounted(): void {
      this.drawEcharts();
      window.addEventListener('resize', this.myChart.resize);
    }
    private beforeDestroy(): void {
      window.removeEventListener('resize', this.myChart.resize);
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
    @Watch('option', { deep: true })
    private onoptionChanged(newVal: any, oldVal: any): void {
      if (this.myChart) {
        if (newVal) {
          this.myChart.clear()
          this.myChart.setOption(newVal, true); // clear cache
        } else {
          this.myChart.clear()
          this.myChart.setOption(oldVal, true);
        }
      } else {
        this.drawEcharts();
      }
    }
    @Watch('markLineSource', { deep: true })
    private onMarkLineChanged(newVal: any, oldVal: any): void {
      if (this.myChart) {
        this.myChart.clear()
        this.myChart.setOption(this.option, true);
      } else {
        this.drawEcharts();
      }
    }

    @Watch('logModel')
    private onLogModelChanged(): void {
      if (this.myChart) {
        this.myChart.clear()
        this.myChart.setOption(this.option, true);
      } else {
        this.drawEcharts();
      }
    }

    get option() {
      const that = this;
      const source = Array.from(this.source || []);
      const seriesData: any = source.map(() => ({ type: 'bar', barWidth: '80%', barMaxWidth: 20 }));
      const dimensions = ['seriesName', ...source.map((item: any) => item.name)]
      // 获取全量的xAxis Label，防止部分数据不完整
      const datas = source.map((item: any) => item.data);

      const xAxisMapping = [...new Set(datas.reduce((prev: any, curr: any) => {
        return prev.concat(curr.map((subItem: any) => subItem.key))
      }, []))];

      const valueMapping = [...new Set(datas.reduce((prev: any, curr: any) => {
        return prev.concat(curr.map((subItem: any) => subItem.value))
      }, []))];
      const sortVals = valueMapping.sort((a: any, b: any) => (+b) - (+a));
      const maxVal = sortVals && sortVals.length ? sortVals[0] : 0;

      // console.log('xAxisMapping', xAxisMapping)
      const sourceValues = xAxisMapping.map((item: any) => {
        const values = datas.map((data: any) => {
          const sameKeyItem = data.find((subItem: any) => subItem.key === item);
          return sameKeyItem ? (this.logModel && sameKeyItem.value ? Math.log10(sameKeyItem.value).toFixed(4) : sameKeyItem.value) : 0;
        })
        // @ts-ignore
        return [item, ...values];
        // return [item, ...values, Math.ceil(maxVal * 1.1)];
      });

      const optionSource = [
        dimensions,
        ...sourceValues,
      ];

      // markLine
      if (seriesData && seriesData.length && this.markLineSource && this.markLineSource.length) {
        seriesData[0].markLine = {
          data: this.markLineSource.map((item: any) => {
            return {
              name: item.name,
              xAxis: xAxisMapping.findIndex((val: any) => val === item.value)
            }
          }),
          symbol: ['none', 'none'],
          silent: true,
          label: {
            color: this.themeVars.colorTextRegular,
            formatter: '{b}'
          },
          lineStyle: {
            color: this.themeVars.colorTextRegular,
          }
        }
      }

      const option: any = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
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
          formatter: (val: any) => {
            const { data } = val[0];
            const key = data[0]
            const value = that.logModel ? Math.round(Math.pow(10, data[1])) :  data[1]
            const index = xAxisMapping.findIndex(t => t === key) || 0
            const prev: any = xAxisMapping[index - 1] || 0
            return i18n.t('modules.views.appMonitor.response.s_9591b219', { value0: that.valTickFormat(prev), value1: that.valTickFormat(Number(key)), value2: value }) as string
          }
        },
        grid: {
          top: 20,
          left: 10,
          right: 10,
          bottom: 10,
          containLabel: true,
        },
        xAxis: {
          type: 'category',
          axisTick: {
            lineStyle: { color: this.themeVars.borderColorLighter },
            alignWithLabel: true,
          },
          splitLine: { show: false },
          axisLine: {
            show: !!sourceValues.length,
            lineStyle: { color: this.themeVars.borderColorLighter }
          },
          axisLabel: {
            color: this.themeVars.colorTextRegular,
            fontSize: '12',
            margin: 15,
            formatter: (val: string) => {
              if (!val) {
                return '-';
              }
              const key = that.valTickFormat(Number(val));
              return key;
            },
          },
        },
        yAxis: {
          type: this.logModel ? 'log' : 'value',
          boundaryGap: ['0', '40%'],
          splitNumber: 3,
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { color: this.themeVars.borderColorLighter, type: 'dashed' } },
          axisLabel: {
            color: this.themeVars.colorTextRegular,
            fontSize: '12',
            formatter: (val: string) => {
              if (!val && String(val) !== '0') {
                return '-';
              }
              const key = humanFormat(Number(val), { decimals: Number(val) > 1000 ? 1 : 0 });
              return key;
            },
          },
        },
        dataset: {
          source: optionSource,
        },
        series: seriesData,
        color: this.colors.length ? this.colors : this.themeVars.colors2,
      };

      // if (maxVal && valueMapping.length) {
      //   // @ts-ignore
      //   option.yAxis.max = Math.ceil(maxVal * 1.1)
      // }

      return option;
    }
    private drawEcharts(): void {
      const el: any = this.$refs.chart;
      this.myChart = this.$echarts.init(el, '', { renderer: 'svg' });
      this.myChart.setOption(this.option);
      if (this.clickEvent) {
        this.myChart.on('click', (params: any) => {
          if (!this.clickEvent) {
            return;
          }
          this.clickEvent(params);
        });
      }
    }

    private valTickFormat(val: number) {
      const num = Number(val);
      const { scale_factor, scale, sub_unit } = getUnitData(this.unit);
      const value = humanFormat(num * scale_factor, { ...scale })
      return value + sub_unit;
    }
  }
</script>

<style lang="scss" scoped>
.bar-chart-wrapper{
  width: 100%;
  height: 100%;
  position: relative;

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
    z-index: 9;
  }
}
</style>
