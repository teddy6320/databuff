<template>
  <div class="card-cont p-16 flex-v bg-color border-1-lighter br-4">
    <div class="card-header font-14 fw-500 flex-none">{{ getTitle }}</div>
    <div class="flex-1 flex-h-jc">
      <el-tooltip effect="light" placement="top" :visible-arrow='false' popper-class="bg-color">
        <div class="fw-500">
          <span class="font-32 red vb">{{ getErrCount }}</span>
          <span class="font-20 vb ml-5 mr-5">/</span>
          <span class="font-20 vb">{{ getTotalCount }}</span>
        </div>
        <div slot="content" class="mw-200 p-6">
          <div>{{ getTimeRangeStr }}</div>
          <div class="flex-h-jc mt-8 mb-8"><span>{{ $t('modules.views.cockpit.component.s_b9a7b0dc', { value0: getTitle }) }}</span><span>{{ getErrCount }}</span></div>
          <div class="flex-h-jc"><span>{{ $t('modules.views.cockpit.component.s_ae6acff5', { value0: getTitle }) }}</span><span>{{ getTotalCount }}</span></div>
        </div>
      </el-tooltip>
    </div>
    <div class="height-40 flex-none">
      <div class="chart h-100p" ref='chart'></div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import { debounce } from '@/utils/common'

@Component
export default class ContComp extends Vue {
  @Prop({}) private value!: any;

  @Watch('value', { deep: true })
  private onValueChange (newVal: any, oldVal: any) {
    this.updateChart();
  }

  private myChart: any = null;
  private resizeHandler: any = null;

  public $refs!: {
    chart: HTMLDivElement
  }

  get getTitle () {
    return this?.value?.title || '';
  }
  get getErrCount () {
    return this?.value?.data?.errCnt ?? 0;
  }
  get getTotalCount () {
    return this?.value?.data?.total ?? 0;
  }

  get getTimeRangeStr () {
    const { fromTime, toTime } = this.getGlobalTimeV2();
    // 如果是同一天，显示 YYYY-MM-DD HH:mm - HH:mm
    // 如果不是同一天，显示 YYYY-MM-DD HH:mm - YYYY-MM-DD HH:mm
    const fromDateStr = fromTime.slice(0, 10);
    const toDateStr = toTime.slice(0, 10);
    const fromTimeStr = fromTime.slice(11, 16);
    const toTimeStr = toTime.slice(11, 16);
    if (fromDateStr === toDateStr) {
      return `${fromDateStr} ${fromTimeStr} ～ ${toTimeStr}`;
    } else {
      return `${fromDateStr} ${fromTimeStr} ～ ${toDateStr} ${toTimeStr}`;
    }
  }

  private async mounted () {
    this.drawChart();
    this.resizeHandler = debounce(() => {
      if (this.myChart) {
        this.myChart.resize()
      }
    }, 100)
    window.addEventListener('resize', this.resizeHandler)
  }

  private beforeDestroy() {
    window.removeEventListener('resize', this.resizeHandler)
    if (this.myChart && this.myChart.dispose) {
      this.myChart.dispose();
    }
    this.myChart = null;
  }

  private drawChart () {
    this.myChart = this.$echarts.init(this.$refs.chart, '', { renderer: 'svg' });
    this.myChart.setOption(this.getOption());
  }
  private updateChart () {
    this.myChart.clear();
    this.myChart.setOption(this.getOption(), true); // clear cache
  }

  private getOption () {
    const { trend = [] } = this?.value?.data || {};
    const xAxisData: any[] = [];
    const yAxisData: any[] = [];
    if (Array.isArray(trend) && trend.length) {
      trend.forEach((item: any) => {
        xAxisData.push(item.key);
        yAxisData.push(item.value);
      });
    }
    const maxVal = Math.max(...yAxisData);
    const minVal = Math.min(...yAxisData);
    return {
      tooltip: {
        show: true,
        extraCssText: `box-shadow:1px 1px 4px 0 #ACACAC;max-width:400px;max-height:300px;overflow:auto;word-break:break-all;white-space:normal;backdrop-filter: blur(2px);`,
        backgroundColor: '#fff',
        borderWidth: 0,
        padding: [8, 8],
      },
      grid: {
        left: 0,
        right: 0,
        bottom: 0,
        top: 0,
      },
      yAxis: {
        type: 'value',
        show: false,
        min: minVal * 0.9,
        max: maxVal * 1.2,
      },
      xAxis: {
        type: 'category',
        show: false,
        // boundaryGap: 0,
        data: xAxisData,
      },
      series: [
        {
          data: yAxisData,
          type: 'bar',
          symbol: 'none',
          smooth: true,
          color: '#f56c6c',
        }
      ]
    }
  }
}
</script>

<style lang="scss" scoped>
.card-cont{
  height: 144px;
}
.height-40 {
  height: 30px;
}
.font-32 {
  font-size: 32px;
}
.mw-200 {
  min-width: 200px;
}
</style>