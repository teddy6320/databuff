<template>
  <div class="problem-analysis">
    <div class="chart-group">
      <div v-loading="chartLoading1" class="chart-item">
        <div class="item-title">{{ $t('modules.views.alarmCenter.problemAnalysis.s_d5a57c1c') }}</div>
        <div class="item-cont">
          <pie-chart
            :source="chartSource1"
            :tooltipFormat="tooltipFormat"
            :showEmpty="!chartLoading1 && !chartSource1.length"
            :clickEvent="($event) => chartClickHandle($event, 'rootCauseTypes')"
          />
        </div>
      </div>

      <div v-loading="chartLoading2" class="chart-item ml-16">
        <div class="item-title">{{ $t('modules.views.alarmCenter.problemAnalysis.s_ec46bb5e') }}</div>
        <div class="item-cont">
          <pie-chart
            :source="chartSource2"
            :tooltipFormat="tooltipFormat"
            :showEmpty="!chartLoading2 && !chartSource2.length"
            :clickEvent="($event) => chartClickHandle($event, 'rootCauseNodes')"
          />
        </div>
      </div>

      <div v-loading="chartLoading3" class="chart-item ml-16">
        <div class="item-title">{{ $t('modules.views.alarmCenter.problemAnalysis.s_3bd38b6d') }}</div>
        <div class="item-cont convergence-cont">
          <convg-chart
            :list="convergenceList"
            :showEmpty="convergenceEmpty"
          />
        </div>
      </div>

      <div v-loading="chartLoading5" class="chart-item h-342 mt-16">
        <div class="mtt-cont">
          <div class="mtt-cont-item">
            <div class="item-title">{{ $t('modules.views.alarmCenter.problemAnalysis.s_1c9d8b71') }}</div>
            <!-- <div class="flex-h">
              <div class="info-t">{{ $t('modules.views.infrastructure.clusterDetail.s_9ed7d3ad') }}<br><span class="count">{{ problemTotal | NumberFilter }}</span></div>
            </div> -->
          </div>
        </div>
        <div class="item-cont trend-chart-cont">
          <basic-chart
            :source="trendSource"
            :showLegend="true"
            :minInterval="1"
            :barMaxWidth="6"
            :colors="['#CF3C33']"
            :showEmpty="!chartLoading5 && !trendSource.length"
            :title="basicChartTitle"
            :legend="basicChartLegend"
            :grid="basicChartGrid"
            :axisClickEvent="($event) => trendChartClickHandle($event)"
            :showAxisLabelCount="6"
          />
        </div>
      </div>

      <div v-loading="chartLoading4" class="chart-item w-double h-342 ml-16 mt-16">
        <div class="mtt-cont">
          <div class="mtt-cont-item">
            <div class="item-title">{{ $t('modules.views.alarmCenter.problemAnalysis.s_6e5e3dc9') }}</div>
            <div class="flex-h">
              <div class="info-t">{{ $t('modules.views.alarmCenter.problemAnalysis.s_d6393b06') }}<br><span class="count">{{ mttData.mttrAvg | NumberFilter }}</span>min</div>
              <div class="info-t">{{ $t('modules.views.npm.topology.s_5da89314') }}<br><span class="count">{{ mttData.mttrMax | NumberFilter }}</span>min</div>
              <div class="info-t">{{ $t('modules.views.npm.topology.s_c322edb8') }}<br><span class="count">{{ mttData.mttrMin | NumberFilter }}</span>min</div>
            </div>
          </div>
          <div class="mtt-cont-item">
            <div class="item-title">{{ $t('modules.views.alarmCenter.problemAnalysis.s_4479a62f') }}</div>
            <div class="flex-h">
              <div class="info-t">{{ $t('modules.views.appMonitor.serviceFlow.s_96a0c062') }}<br><span class="count">{{ mttData.mttaAvg | NumberFilter }}</span>min</div>
              <div class="info-t">{{ $t('modules.views.npm.topology.s_5da89314') }}<br><span class="count">{{ mttData.mttaMax | NumberFilter }}</span>min</div>
              <div class="info-t">{{ $t('modules.views.npm.topology.s_c322edb8') }}<br><span class="count">{{ mttData.mttaMin | NumberFilter }}</span>min</div>
            </div>
          </div>
        </div>
        <div class="item-cont mtt-chart-cont">
          <basic-chart
            :source="mttSource"
            :showLegend="true"
            :showEmpty="!chartLoading4 && !mttSource.length"
            :colors="['#2962FF', '#967EFF']"
            :title="basicChartTitle"
            :legend="basicChartLegend"
            :grid="basicChartGrid"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { State } from 'vuex-class';
import dayjs from 'dayjs';
import BasicChart from '@/components/charts/basic-chart.vue';
import PieChart from '@/components/charts/pie-chart-new.vue';
import ConvgChart from './convg-chart.vue';
import { toAsyncWait } from '@/utils/common'
import RootCauseApi from '@/api/rootCause';

@Component({
  components: {
    BasicChart,
    PieChart,
    ConvgChart,
  }
})
export default class ProblemAnalysis extends Vue {
  @State('theme') private theme!: 'dark' | 'light';

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private timeParams = {
    fromTime: '',
    toTime: '',
    interval: 3600,
  }

  // 问题类型分布
  private chartLoading1 = false;
  private chartSource1: any[] = [];

  // 问题节点分布
  private chartLoading2 = false;
  private chartSource2: any[] = [];

  // 问题收敛
  private chartLoading3 = false;
  private convergenceList: any[] = [
    { name: i18n.t('modules.utils.static.s_10b2761d') as string, nameKey: 'modules.utils.static.s_10b2761d', value: '', warn: 0, nodata: 0, width: 100 },
    { name: i18n.t('modules.views.alarmCenter.alarm.s_aa0eab9d') as string, nameKey: 'modules.views.alarmCenter.alarm.s_aa0eab9d', value: '', warn: 0, nodata: 0, width: 66 },
    { name: i18n.t('modules.views.alarmCenter.problemAnalysis.s_5dc99f6e') as string, nameKey: 'modules.views.alarmCenter.problemAnalysis.s_5dc99f6e', value: '', width: 10 },
    { name: i18n.t('modules.views.alarmCenter.problemAnalysis.s_3bd38b6d') as string, nameKey: 'modules.views.alarmCenter.problemAnalysis.s_3bd38b6d', value: '', unit: 'percent', type: 'text' },
  ];
  get convergenceEmpty () {
    return !this.chartLoading3 && this.convergenceList.filter(t => t.type !== 'text').every((item: any) => !item.value);
  }

  // MTT
  private chartLoading4 = false;
  private mttData: any = {
    mttrAvg: 0,
    mttrMin: 0,
    mttrMax: 0,
    mttaAvg: 0,
    mttaMin: 0,
    mttaMax: 0
  };
  private mttSource: any[] = [];

  // 问题趋势
  private chartLoading5 = false;
  private problemTotal: any = '';
  private trendSource: any[] = [];

  private basicChartTitle = {
    text: i18n.t('modules.views.alarmCenter.problemAnalysis.s_1a57bc0c') as string, textKey: 'modules.views.alarmCenter.problemAnalysis.s_1a57bc0c',
    top: 0,
    left: 3,
  }
  get basicChartLegend () {
    return {
      top: 0,
      right: 3,
      left: 'auto',
      // itemWidth: 6,
      // icon: 'roundRect',
      // textStyle: {
      //   lineHeight: 14,
      //   fontWeight: 400,
      //   fontSize: 12,
      //   color: this.theme === 'dark' ? '#8B8E93' : '#626467',
      // }
    }
  }
  private basicChartGrid = {
    top: 50,
    left: 10,
    right: 20,
    bottom: 15,
    containLabel: true,
  }

  private mounted () {
    this.durationChangeHandle()
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
  }

  private beforeDestroy () {
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh')
  }

  // 时间范围改变
  private durationChangeHandle () {
    this.regetGlobalTime()
    this.getConvergenceData()
    this.getRootCauseTypeDist()
    this.getRootCauseNodeDist()
    this.getMttData()
    this.getInfluenceTrend()
  }

  private regetGlobalTime () {
    const { fromTime, toTime, interval } = this.getGlobalTimeV2()
    this.timeParams = { fromTime, toTime, interval }
  }

  // 获取问题类型分布
  private async getRootCauseTypeDist () {
    const params: any = {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      topN: 10,
    }
    this.chartLoading1 = true;
    const { result, error } = await toAsyncWait(RootCauseApi.getRootCauseTypes(params))
    this.chartLoading1 = false;
    if (!error) {
      let data: any[] = (result.data || []).map((t: any) => ({
        key: t.rootCauseType,
        value: t.cnt,
      }));
      if (!this.chartLoading3 && +this.problemTotal) {
        this.chartSource1 = this.formatPieSource(data, +this.problemTotal);
      } else {
        data = data.filter(t => t.value > 0).slice(0, 10);
        this.chartSource1 = [
          ...data.filter(t => t.key !== '其他'),
          ...data.filter(t => t.key === '其他'),
        ];
      }
    } else {
      this.chartSource1 = []
    }
  }

  // 获取问题节点分布
  private async getRootCauseNodeDist () {
    const params: any = {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      topN: 10,
    }
    this.chartLoading2 = true;
    const { result, error } = await toAsyncWait(RootCauseApi.getRootCauseNodes(params))
    this.chartLoading2 = false;
    if (!error) {
      let data: any[] = (result.data || []).map((t: any) => ({
        key: t.rootCauseNode,
        value: t.cnt,
      }));
      if (!this.chartLoading3 && +this.problemTotal) {
        this.chartSource2 = this.formatPieSource(data, +this.problemTotal);
      } else {
        data = data.filter(t => t.value > 0).slice(0, 10);
        this.chartSource2 = [
          ...data.filter(t => t.key !== '其他'),
          ...data.filter(t => t.key === '其他'),
        ];
      }
    } else {
      this.chartSource2 = []
    }
  }

  private formatPieSource (_data: any[], total: number) {
    let data = _data.filter((t: any) => t.key !== i18n.t('modules.views.alarmCenter.problemAnalysis.s_0d98c747') as string && t.value > 0).slice(0, 10);
    let other = +this.problemTotal - data.reduce((acc: number, cur: any) => acc + cur.value, 0);
    if (other > 0) {
      if (data.length === 10) {
        const lastItem = data[data.length - 1];
        other += lastItem.value;
        data = data.slice(0, 9);
      }
      data.push({ key: i18n.t('modules.views.alarmCenter.problemAnalysis.s_0d98c747') as string, value: other })
    }
    return data;
  }

  private tooltipFormat (params: any) {
    const { key, value, _value } = params.data
    return i18n.t('modules.views.alarmCenter.problemAnalysis.s_9300621b', { value0: key, value1: _value }) as string
  }

  // 获取问题收敛
  private async getConvergenceData () {
    this.chartLoading3 = true;
    const { result, error } = await toAsyncWait(RootCauseApi.getInfluenceConvergence(this.timeParams))
    this.chartLoading3 = false;
    if (!error) {
      const data = result.data || {};
      const event = (data.majorEvent || 0) + (data.minorEvent || 0) + (data.noDataEvent || 0);
      const alarm = (data.majorAlarm || 0) + (data.minorAlarm || 0) + (data.noDataAlarm || 0);
      this.convergenceList = [
        { name: i18n.t('modules.utils.static.s_10b2761d') as string, nameKey: 'modules.utils.static.s_10b2761d', value: event, warn: data.minorEvent || 0, nodata: data.noDataEvent || 0, width: 100 },
        { name: i18n.t('modules.views.alarmCenter.alarm.s_aa0eab9d') as string, nameKey: 'modules.views.alarmCenter.alarm.s_aa0eab9d', value: alarm, warn: data.minorAlarm || 0, nodata: data.noDataAlarm || 0, width: 66 },
        { name: i18n.t('modules.views.alarmCenter.problemAnalysis.s_5dc99f6e') as string, nameKey: 'modules.views.alarmCenter.problemAnalysis.s_5dc99f6e', value: data.problem, width: 10 },
        { name: i18n.t('modules.views.alarmCenter.problemAnalysis.s_3bd38b6d') as string, nameKey: 'modules.views.alarmCenter.problemAnalysis.s_3bd38b6d', value: data.convergenceRatio, unit: 'percent', type: 'text' },
      ]
      this.problemTotal = data.problem;

      if (!this.chartLoading1 && +this.problemTotal) {
        this.chartSource1 = this.formatPieSource(this.chartSource1, +this.problemTotal);
      }
      if (!this.chartLoading2 && +this.problemTotal) {
        this.chartSource2 = this.formatPieSource(this.chartSource2, +this.problemTotal);
      }
    } else {
      this.convergenceList = [
        { name: i18n.t('modules.utils.static.s_10b2761d') as string, nameKey: 'modules.utils.static.s_10b2761d', value: '', warn: 0, nodata: 0, width: 100 },
        { name: i18n.t('modules.views.alarmCenter.alarm.s_aa0eab9d') as string, nameKey: 'modules.views.alarmCenter.alarm.s_aa0eab9d', value: '', warn: 0, nodata: 0, width: 66 },
        { name: i18n.t('modules.views.alarmCenter.problemAnalysis.s_5dc99f6e') as string, nameKey: 'modules.views.alarmCenter.problemAnalysis.s_5dc99f6e', value: '', width: 10 },
        { name: i18n.t('modules.views.alarmCenter.problemAnalysis.s_3bd38b6d') as string, nameKey: 'modules.views.alarmCenter.problemAnalysis.s_3bd38b6d', value: '', unit: 'percent', type: 'text' },
      ]
      this.problemTotal = '';
    }
  }

  // 获取MTT
  private async getMttData () {
    this.chartLoading4 = true;
    const { result, error } = await toAsyncWait(RootCauseApi.getInfluenceMtt(this.timeParams))
    this.chartLoading4 = false;
    if (!error) {
      const data = result.data || {};
      const mttr = data.mttr || {};
      const mtta = data.mtta || {};
      this.mttData = {
        mttrAvg: mttr.avg || 0,
        mttrMin: mttr.min || 0,
        mttrMax: mttr.max || 0,
        mttaAvg: mtta.avg || 0,
        mttaMin: mtta.min || 0,
        mttaMax: mtta.max || 0
      }
      this.mttSource = [
        {
          name: 'MTTR',
          unit: 'min',
          area: true,
          smooth: true,
          data: (mttr.values || []).map((t: any[]) => ({
            key: dayjs(t[0]).format('YYYY-MM-DD HH:mm'),
            value: t[1],
          })),
        },
        {
          name: 'MTTA',
          unit: 'min',
          area: true,
          smooth: true,
          data: (mtta.values || []).map((t: any[]) => ({
            key: dayjs(t[0]).format('YYYY-MM-DD HH:mm'),
            value: t[1],
          })),
        },
      ]
    } else {
      this.mttData = {
        mttrAvg: 0,
        mttrMin: 0,
        mttrMax: 0,
        mttaAvg: 0,
        mttaMin: 0,
        mttaMax: 0
      }
      this.mttSource = [];
    }
  }

  // 问题趋势
  private async getInfluenceTrend () {
    this.chartLoading5 = true;
    const { result, error } = await toAsyncWait(RootCauseApi.getInfluenceTrend(this.timeParams))
    this.chartLoading5 = false;
    if (!error) {
      this.trendSource = [{
        name: i18n.t('modules.views.alarmCenter.problemAnalysis.s_5dc99f6e') as string, nameKey: 'modules.views.alarmCenter.problemAnalysis.s_5dc99f6e',
        type: 'bar',
        data: (result.data || []).map((t: any[]) => ({
          key: dayjs(t[0]).format('YYYY-MM-DD HH:mm'),
          value: t[1],
        })),
      }]
    } else {
      this.trendSource = []
    }
  }

  private chartClickHandle (data: any, type: string) {
    this.$router.push({
      path: '/alarmCenter/rootCause',
      query: {
        [type]: encodeURIComponent(data.name),
      },
    });
  }

  private trendChartClickHandle (params: { xAxisName: string }) {
    const { xAxisName } = params
    const interval = this.timeParams.interval
    const fromTime = +new Date(xAxisName + ':00');
    const toTime = Math.min(fromTime + interval * 1000, +new Date(this.timeParams.toTime));
    this.$router.push({
      path: '/alarmCenter/rootCause',
      query: {
        fromTime: `${fromTime}`,
        toTime: `${toTime}`,
      },
    });
  }
}
</script>

<style lang="scss" scoped>
.problem-analysis {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .chart-group {
    flex: 1;
    min-height: 682px;
    overflow: hidden;
  }

  .chart-item {
    display: inline-block;
    vertical-align: top;
    width: calc((100% - 32px) / 3);
    height: 324px;
    background-color: var(--bg-color);
    font-size: 14px;
    line-height: 20px;
    color: #626467;
    &.ml-16 {
      margin-left: 16px;
    }
    &.mt-16 {
      margin-top: 16px;
    }
    &.w-double {
      width: calc((100% - 32px) / 3 * 2 + 16px);
    }
    &.h-342 {
      height: 342px;
    }
    .item-title {
      height: 40px;
      padding: 20px 20px 0;
      font-weight: 500;
      color: #121317;
    }
    .item-cont {
      height: calc(100% - 44px);
      padding: 0 10px;
    }
  }

  .item-cont.convergence-cont {
    padding: 0 20px;
  }

  .mtt-cont {
    display: flex;
    justify-content: space-between;
    .mtt-cont-item {
      width: calc(50% - 8px);
      padding-left: 20px;
      .item-title {
        padding-left: 0;
      }
    }
    .info-t {
      height: 70px;
      padding-top: 16px;
      font-weight: 400;
      font-size: 14px;
      color: #626467;
      line-height: 20px;
      & + .info-t {
        margin-left: 40px;
      }
      .count {
        margin-top: 4px;
        display: inline-block;
        font-weight: 500;
        font-size: 22px;
        color: #121317;
        line-height: 30px;
      }
    }
  }

  .item-cont.mtt-chart-cont {
    padding: 10px 15px 0;
    height: calc(100% - 110px);
  }
  .item-cont.trend-chart-cont {
    padding: 10px 15px 0;
    height: calc(100% - 40px);
  }
}

// 黑色主题
:root[data-theme=dark] .problem-analysis {
  .chart-item,
  .mtt-cont .info-t {
    color: #8B8E93;
  }
  .chart-item .item-title,
  .mtt-cont .info-t .count {
    color: #EBEBED;
  }
}
</style>
