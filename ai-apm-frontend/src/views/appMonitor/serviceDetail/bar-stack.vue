<template>
  <div class="bar-chart-wrapper">
    <div ref="chart" style='height: 100%;' />
    <div class="empty-show describe" v-if='showEmpty'>{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>
<script lang="ts">
  import { Vue, Component, Prop, Watch } from 'vue-property-decorator';

  @Component
  export default class BarStack extends Vue {
    @Prop({ default: false }) private showEmpty!: boolean;
    @Prop() private source: any;
    @Prop({ default: () => [] }) private colors!: any;

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
      if (this.myChart.dispose) {
        this.myChart.dispose();
      }
      this.myChart = null
    }
    @Watch('option', { deep: true })
    private onoptionChanged(newVal: any, oldVal: any): void {
      if (this.myChart) {
        if (newVal) {
          this.myChart.setOption(newVal, true); // clear cache
        } else {
          this.myChart.setOption(oldVal, true);
        }
      } else {
        this.drawEcharts();
      }
    }

    get option() {
      const that = this;
      const source = Array.from(this.source || []);
      const seriesData = source.map((item, idx) => ({
        type: 'bar',
        showSymbol: false,
        barMaxWidth: 20,
        stack: 'all',
        silent: true,
      }));
      const dimensions = ['seriesName', ...source.map((item: any) => item.name)]
      // 获取全量的xAxis Label，防止部分数据不完整
      const datas = source.map((item: any) => item.data);
      const xAxisMapping = [...new Set(datas.reduce((prev: any, curr: any) => {
        return prev.concat(curr.map((subItem: any) => subItem.key))
      }, []))];
      // console.log('xAxisMapping', xAxisMapping)
      const sourceValues = xAxisMapping.map((item: any) => {
        const values = datas.map((data: any) => {
          const sameKeyItem = data.find((subItem: any) => subItem.key === item);
          return sameKeyItem ? sameKeyItem.value : 0;
        })
        return [item, ...values];
      });

      const optionSource = [
        dimensions,
        ...sourceValues,
      ];

      // console.log('optionSource', optionSource)

      const option = {
        grid: {
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
        },
        dataset: {
          source: optionSource,
        },
        xAxis: {
          type: 'category',
          show: false,
        },
        yAxis: {
          type: 'value',
          show: false,
        },
        series: seriesData,
        color: this.colors,
      };
      return option;
    }
    private drawEcharts(): void {
      const el: any = this.$refs.chart;
      this.myChart = this.$echarts.init(el, '', { renderer: 'svg' });
      this.myChart.setOption(this.option);
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
      font-size: 16px;
      background-color: var(--bg-color);
      z-index: 9;
    }
  }
</style>