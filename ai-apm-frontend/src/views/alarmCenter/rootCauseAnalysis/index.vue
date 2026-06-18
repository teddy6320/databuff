<template>
  <div class="cause-analysis-wrapper">
    <div class="cause-analysis" :class="{ 'show-trend': showTrend }">
      <div class="flex-none flex-h-jc pl-20 pr-20 pt-20 pb-16 font-14 lh-14">
        {{ $t('modules.views.alarmCenter.rootCauseAnalysis.s_34efb47b', { value0: (analysisInfo.startTime || '-').substring(0, 16), value1: (analysisInfo.endTime || '-').substring(0, 16) }) }}
        <analysis-record
          v-if="analysisInfo.logs && analysisInfo.logs.length"
          :list="analysisInfo.logs"
          class="analysis-record" />
      </div>

      <div class="flex-none flex-h pl-20 pr-20">
        <scroll-select
          v-model="serviceNames"
          @change="serviceChangeHandle()"
          :options="serviceList"
          :multiple="true"
          :showTitle="true"
          :collapseTags="false"
          :placeholder="$t('modules.views.alarmCenter.rootCauseAnalysis.s_f6d1476c')"
          class="flex-1" />

        <el-button
          @click="analysisHandle"
          :disabled="!reanalysis"
          :type="reanalysis ? 'primary' : ''"
          size="small"
          class="flex-none ml-10">{{ $t('modules.views.alarmCenter.rootCauseAnalysis.s_2f6db36d') }}</el-button>
      </div>

      <cause-trend
        ref="causeTrend"
        :list="topoRootList"
        :loading="isLoading"
        @chart-click="chartClickHandle"
        @on-toggle-chart="toggleChartHandle"
        class="flex-none pl-20 pr-20" />

      <div class="analysis-cause-tree-wrap" v-loading="isLoading">
        <scroll-select
          v-model="topoRoot"
          @change="topoRootChangeHandle()"
          :options="topoRootList"
          :showTitle="true"
          :clearable="false"
          :placeholder="$t('modules.views.metrics.list.s_708c9d6d')"
          class="root-node-select" />

        <el-button
          v-if="currTopoRoot.showSurface"
          @click="viewProblemDetail()"
          size="small"
          class="root-node-btn">{{ $t('modules.views.alarmCenter.alarmDetail.s_e8f54f03') }}</el-button>

        <cause-tree
          ref="causeTree"
          :source="causeTreeData"
          chartType="cause"
          class="analysis-cause-tree" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import dayjs from 'dayjs';
import { orderBy } from 'lodash';
import { v4 as uuidv4 } from 'uuid';
import AnalysisRecord from './analysis-record.vue';
import CauseTrend from './root-cause-trend.vue';
import CauseTree from '@/views/alarmCenter/problemDetail/cause-tree.vue';
import { toAsyncWait } from '@/utils/common';
import RootCauseApi from '@/api/rootCause';

@Component({
  components: {
    AnalysisRecord,
    CauseTrend,
    CauseTree,
  }
})
export default class RootCauseAnalysis extends Vue {
  public $refs!: {
    causeTrend: CauseTrend
    causeTree: CauseTree
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    if (this.routePathChanging) {
      this.routePathChanging = false;
      return
    }
    this.reanalysis = true;
  }

  get serviceList () {
    const names: string[] = []
    const basicServiceMap = this.$store.getters['Service/basicServiceMap']
    Object.values(basicServiceMap).forEach((item: any) => {
      names.push(item.name)
    });
    return orderBy([...new Set(names)], [t => t.toLocaleLowerCase()], ['asc'])
  }

  private serviceNames: string[] = [];

  private analysisInfo: any = {}; // 根因分析信息
  private reanalysis = false; // 重新分析

  private isLoading = false;
  private topoRootList: any[] = [] // 根因列表
  private topoRoot: string = '' // 当前根因
  private chartDataMapping: any = {} // 根因对应的拓扑数据
  get causeTreeData () { // 当前根因拓扑数据
    return this.chartDataMapping[this.topoRoot] || []
  }
  get currTopoRoot () { // 当前根因详情数据
    return this.topoRootList.find((item: any) => item.value === this.topoRoot) || {};
  }

  private showTrend = false;

  private routePathChanging = false; // 防止详情时间更新路由后重复请求

  private timeParams: any = {}

  get queryParams () {
    const { disableExpand } = this.$route.query
    const params: any = {
      service: this.serviceNames.join(','),
      fromTime: +new Date(this.timeParams.fromTime),
      toTime: +new Date(this.timeParams.toTime),
    }
    if (disableExpand === 'true') {
      params.disableExpand = true
    }
    return params
  }

  private created() {
    const { sns } = this.$route.query
    if (sns) {
      this.serviceNames = decodeURIComponent(sns as string).split(',')
    }
    this.timeParams = { ...this.getGlobalTimeV2() }
    this.analysisInfo = {
      startTime: this.timeParams.fromTime,
      endTime: this.timeParams.toTime,
    }
  }

  private async mounted () {
    await this.getChartData();
    this.$nextTick(() => {
      this.$refs.causeTree && this.$refs.causeTree.getData();
      this.$refs.causeTrend && this.$refs.causeTrend.toggleChartHandle(this.topoRootList.length > 1);
    })
  }

  private serviceChangeHandle () {
    this.reanalysis = true;
  }

  // 重新分析
  private async analysisHandle () {
    this.reanalysis = false;
    this.timeParams = { ...this.getGlobalTimeV2() }
    this.analysisInfo = {
      startTime: this.timeParams.fromTime,
      endTime: this.timeParams.toTime,
    }
    this.$router.replace({
      query: {
        ...this.$route.query,
        sns: encodeURIComponent(this.serviceNames.join(',')),
      }
    });
    await this.getChartData();
    this.$nextTick(() => {
      this.$refs.causeTree && this.$refs.causeTree.getData();
      this.$refs.causeTrend && this.$refs.causeTrend.toggleChartHandle(this.topoRootList.length > 1);
    })
  }

  private chartClickHandle (name: string) {
    const topoRoot = this.topoRootList.find((item: any) => item.label === name);
    if (topoRoot) {
      this.topoRoot = topoRoot.value;
      this.topoRootChangeHandle();
    }
  }

  private topoRootChangeHandle () {
    this.$nextTick(() => {
      this.$refs.causeTree && this.$refs.causeTree.getData();
    })
  }

  private toggleChartHandle (show: boolean) {
    this.showTrend = show;
    this.$nextTick(() => {
      this.$refs.causeTree && this.$refs.causeTree.resize();
    })
  }

  private async getChartData () {
    if (!this.serviceNames.length) {
      this.clearData();
      return
    }

    this.isLoading = true
    const { result, error } = await toAsyncWait(RootCauseApi.getRootCauseAnalysis(this.queryParams))
    this.isLoading = false
    if (!error) {
      const data = result?.data || {};
      const list = data?.rootAnalyse || [];
      const topoRootList: any[] = []
      const chartDataMapping: any = {}
      list.forEach((item: any) => {
        const id = uuidv4();
        const abnormalDetail: any = item.abnormalDetail || {};
        delete abnormalDetail.shakeTypes;
        const start = item.startTime + (abnormalDetail.abnormalFirstIndex || 0) * 60 * 1000;
        const end = item.startTime + (abnormalDetail.abnormalLastIndex || 0) * 60 * 1000;
        topoRootList.push({
          label: `${item.service} (${dayjs(start).format('YYYY-MM-DD HH:mm')})`,
          value: id,
          start,
          end,
          abnormalDetail,
          service: item.service,
          showSurface: Array.isArray(item.roots) && !!item.roots.length,
        })
        chartDataMapping[id] = [item];
      })

      if (topoRootList.length) {
        this.topoRootList = topoRootList;
        this.topoRoot = topoRootList[0].value;
        this.chartDataMapping = chartDataMapping;
      } else {
        this.clearData();
      }

      this.analysisInfo = {
        startTime: data.startTime,
        endTime: data.endTime,
        logs: (data.logs || []).reverse(),
      }
      console.log({ ...this.analysisInfo })
      if (+new Date(data.startTime) !== this.queryParams.fromTime || +new Date(data.endTime) !== this.queryParams.toTime) {
        const _query: any = { ...this.$route.query }
        this.routePathChanging = true;
        delete _query.durationRange
        _query.fromTime = `${+new Date(data.startTime)}`
        _query.toTime = `${+new Date(data.endTime)}`
        this.$router.replace({ query: { ..._query } });
      }
    } else {
      this.clearData();
    }
  }

  private clearData () {
    this.topoRootList = [];
    this.topoRoot = '';
    this.chartDataMapping = {};
  }

  private viewProblemDetail () {
    const topoRoot: any = this.currTopoRoot;
    const query: any = {
      sn: encodeURIComponent(topoRoot.service),
      abnormalFirstTime: `${topoRoot.start}`,
      isRoot: '1',
      fromTime: `${this.queryParams.fromTime}`,
      toTime: `${this.queryParams.toTime}`,
      __nw: 't',
    }
    if (topoRoot.abnormalDetail) {
      query.abnormalDetail = encodeURIComponent(JSON.stringify(topoRoot.abnormalDetail))
    }
    const routeData = this.$router.resolve({
      path: '/alarmCenter/problemDetail',
      query,
    });
    window.open(routeData.href, '_blank');
  }
}
</script>

<style lang="scss" scoped>
.cause-analysis-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  font-size: 13px;
  color: var(--color-text-primary);
  overflow: auto;

  .cause-analysis {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 600px;
    border-radius: 4px;
    background-color: var(--bg-color);
    overflow: hidden;
    &.show-trend {
      min-height: 700px;
    }
  }

  .analysis-cause-tree-wrap {
    flex: 1;
    background: #FAFAFA;
    border-top: 1px solid #EEEFF1;
    position: relative;
    overflow: hidden;
    :deep(.empty-show) {
      background: #FAFAFA;
    }
    :deep(.node-query-input) {
      left: 230px;
    }
    .root-node-select {
      width: 200px;
      position: absolute;
      top: 20px;
      left: 20px;
      z-index: 1;
    }
    .root-node-btn {
      position: absolute;
      top: 20px;
      right: calc(50% + 20px);
      z-index: 1;
    }
  }
}
</style>
