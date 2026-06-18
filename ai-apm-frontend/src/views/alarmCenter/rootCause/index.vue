<template>
  <div class="problem-wrapper">
    <div v-if="noExpireLimit" v-loading="filterLoading" class="problem-content pt-20 pb-20">
      <div class="pl-20 pr-20 flex-h">
        <search-group
          ref="searchGroup"
          :filterData="filterData"
          @on-change="searchChangeHandle" />

        <span
          @click="toggleChartHandle"
          :class="{ active: showChart }"
          class="toggle-chart-btn db-icon-chart1 ml-10 font-16 lh-30 tc br-4 flex-none cp"></span>
      </div>

      <div class="problem-content-body pl-20 pr-20 pt-20">
        <choose-collapse
          ref="chooseCollapse"
          :filterData="filterData"
          @on-filter-change="filterChangeHandle"
          @on-filter-toggle="filterToggleHandle"
          class="collapse-choose" />

        <div class="problem-list flex-v">
          <div v-if="showChart" v-loading="chartLoading" class="section-item flex-none mb-10">
            <div class="section-title">{{ $t('modules.views.alarmCenter.problemAnalysis.s_5dc99f6e') }}</div>
            <div class="section-cont">
              <basic-chart
                ref="problemChart"
                :source="chartSource"
                :minInterval="1"
                :barMaxWidth="6"
                :colors="['#CF3C33']"
                :showEmpty="!chartLoading && !chartSource.length" />
            </div>
          </div>

          <db-table
            ref="listTable"
            :queryApi="queryApi"
            :queryParams="getQueryParams"
            :timeMode="false"
            :autoRefresh="false"
            showSetting
            tableKey="ALARM_PROBLEM_LIST"
            :columnConfig="columnConfig"
            :formatFunc="formatFunc"
            @sort-change="tableRefresh"
            @row-click="viewProblemDetail"
            :row-style="{ cursor: 'pointer' }"
            class="problem-table">
          </db-table>
        </div>
      </div>
    </div>

    <div v-else class="problem-content">
      <div class="describe expire-limit-info">
        {{ $t('modules.views.alarmCenter.rootCause.s_4f216394') }}
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator'
import dayjs from 'dayjs';
import i18n from '@/i18n';
import SearchGroup from './search-group.vue';
import ChooseCollapse from './choose-collapse.vue';
import BasicChart from '@/components/charts/basic-chart.vue';
import { toAsyncWait } from '@/utils/common';
import RootCauseApi from '@/api/rootCause';

@Component({
  components: {
    SearchGroup,
    ChooseCollapse,
    BasicChart,
  },
})
export default class RootCause extends Vue {
  public $refs!: {
    searchGroup: SearchGroup
    chooseCollapse: ChooseCollapse
    listTable: any
    problemChart: BasicChart
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private queryParams: any = {}
  private queryFilter: any = {}
  private timeParams: any = {}

  get getQueryParams () {
    const params: any = {
      ...this.queryParams,
      ...this.queryFilter,
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
    }
    Object.keys(params).forEach(k => {
      if (params[k] === '' || Array.isArray(params[k]) && !params[k].length) {
        delete params[k]
      }
    })
    return params;
  }

  private queryApi = RootCauseApi.getProblemList
  private columnConfig = [
    { field: 'problemShowId', label: i18n.t('modules.views.alarmCenter.rootCause.s_82a9a9ef') as string, labelKey: 'modules.views.alarmCenter.problemDetail.s_82a9a9ef', width: 150, disabled: true, handleClick: this.viewProblemDetail },
    { field: 'problemDesc', label: i18n.t('modules.views.alarmCenter.problemAnalysis.s_5dc99f6e') as string, labelKey: 'modules.views.alarmCenter.problemAnalysis.s_5dc99f6e', minWidth: 250 },
    { field: 'problemCauseType', label: i18n.t('modules.views.alarmCenter.rootCause.s_d5a57c1c') as string, labelKey: 'modules.views.alarmCenter.problemAnalysis.s_d5a57c1c', sortable: true, minWidth: 130 },
    { field: 'problemService', label: i18n.t('modules.views.alarmCenter.rootCause.s_ec46bb5e') as string, labelKey: 'modules.views.alarmCenter.problemAnalysis.s_ec46bb5e', sortable: true, minWidth: 130 },
    { field: 'problemTimeRange', label: i18n.t('modules.views.alarmCenter.rootCause.s_cd649f76') as string, labelKey: 'modules.views.alarmCenter.rootCause.s_cd649f76', minWidth: 170 },
    { field: 'problemStartTime', label: i18n.t('modules.views.alarmCenter.eventDetail.s_592c5958') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_592c5958', sortable: true, minWidth: 125, defaultShow: false },
    { field: 'beginToActionTime', label: i18n.t('modules.views.alarmCenter.rootCause.s_df713dfb') as string, labelKey: 'modules.views.alarmCenter.problemDetail.s_df713dfb', sortable: true, minWidth: 125, defaultShow: false },
    { field: 'problemEndTime', label: i18n.t('modules.views.alarmCenter.alarm.s_f782779e') as string, labelKey: 'modules.views.alarmCenter.alarm.s_f782779e', sortable: true, minWidth: 125, defaultShow: false },
    { field: 'problemRepair', prop: 'mttr', label: i18n.t('modules.views.alarmCenter.rootCause.s_f102c038') as string, labelKey: 'modules.views.alarmCenter.problemDetail.s_f102c038', unit: 'count', suffix: ' min', sortable: true, width: 135 },
    { field: 'problemAction', prop: 'mtta', label: i18n.t('modules.views.alarmCenter.rootCause.s_1148b783') as string, labelKey: 'modules.views.alarmCenter.problemDetail.s_1148b783', unit: 'count', suffix: ' min', sortable: true, width: 135 },
  ]

  private showChart = true
  private chartLoading = false
  private chartSource: any[] = []

  get noExpireLimit () {
    const expireLimit = this.$store.getters['User/isExpireLimit']
    const authFinalStatus = this.$store.state.finalStatus
    return authFinalStatus === 0 || (authFinalStatus === 2 && !expireLimit)
  }

  private mounted () {
    if (!this.noExpireLimit) {
      return
    }
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
    this.durationChangeHandle()
  }

  private beforeDestroy () {
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh')
  }

  private formatFunc (data: any) {
    return data.map((t: any) => {
      delete t.influence
      const problemDesc = t.problemDesc || ''
      const startTime = t.problemStartTime || '-'
      const endTime = t.problemEndTime || '-'
      const actionTime = t.beginToActionTime || '-'
      let problemTimeRange = `${startTime.slice(0, 16)} ~ ${endTime.slice(0, 16)}`
      if (startTime.slice(0, 10) === endTime.slice(0, 10)) {
        problemTimeRange = `${startTime.slice(0, 16)} ~ ${endTime.slice(11, 16)}`
      }
      return {
        ...t,
        problemDesc: problemDesc.length > 1500 ? problemDesc.substring(0, 1500) + '...' : problemDesc,
        problemTimeRange,
        problemStartTime: startTime.slice(0, 16),
        problemEndTime: endTime.slice(0, 16),
        beginToActionTime: actionTime.slice(0, 16),
        problemRepair: (+new Date(endTime) - +new Date(startTime)) / 1000 / 60 + 1,
        problemAction: (+new Date(actionTime) - +new Date(startTime)) / 1000 / 60 + 1,
      }
    });
  }
  private tableRefresh () {
    (this.$refs.listTable as any)?.refresh()
  }

  // 时间范围改变
  private async durationChangeHandle () {
    this.timeParams = { ...this.getGlobalTimeV2() }
    await this.getFilterData()
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        this.queryParams = { ...data };
        this.$nextTick(() => {
          this.$refs.chooseCollapse.init().then((filter: any) => {
            this.queryFilter = { ...filter }
            this.getChartSource()
            this.tableRefresh()
          })
        })
      })
    })
  }

  private searchChangeHandle (data: any) {
    this.queryParams = { ...data };
    this.$nextTick(() => {
      this.$refs.chooseCollapse.init().then((filter: any) => {
        this.queryFilter = { ...filter }
        this.getChartSource()
        this.tableRefresh()
      })
    })
  }

  // 快捷筛选
  private filterChangeHandle (filter: any) {
    this.queryFilter = { ...filter }
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        this.queryParams = { ...data };
        this.getChartSource()
        this.tableRefresh()
      })
    })
  }
  private filterToggleHandle () {
    this.$nextTick(() => {
      this.$refs.problemChart && this.$refs.problemChart.resize();
    })
  }

  private toggleChartHandle () {
    this.showChart = !this.showChart;
    this.$nextTick(() => {
      (this.$refs.listTable as any)?.getHeightHandle();
    });
  }

  // 获取问题趋势
  private async getChartSource () {
    const params = { ...this.getQueryParams, interval: this.timeParams.interval }
    this.chartLoading = true;
    const { result, error } = await toAsyncWait(RootCauseApi.getInfluenceTrend(params))
    this.chartLoading = false;
    if (!error) {
      this.chartSource = [{
        name: i18n.t('modules.views.alarmCenter.problemAnalysis.s_5dc99f6e') as string, nameKey: 'modules.views.alarmCenter.problemAnalysis.s_5dc99f6e',
        type: 'bar',
        data: (result.data || []).map((t: any[]) => ({
          key: dayjs(t[0]).format('YYYY-MM-DD HH:mm'),
          value: t[1],
        })),
      }]
    } else {
      this.chartSource = []
    }
  }

  // 跳转到问题详情
  private viewProblemDetail (row: any) {
    this.$router.push({
      path: '/alarmCenter/problemDetail',
      query: {
        id: row.id,
        // fromTime: `${+new Date(row.analyseStartTime)}`,
        // toTime: `${+new Date(row.analyseEndTime)}`,
      },
    });
  }

  // 获取筛选数据
  private filterLoading = false;
  private filterData: any = {}
  private async getFilterData () {
    const params: any = {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
    }
    this.filterLoading = true
    const { result, error } = await toAsyncWait(RootCauseApi.getProblemQueryParams(params))
    this.filterLoading = false
    if (!error) {
      const data = result.data || {}
      data.rootCauseType = data.problemcausetype
      data.rootCauseNode = data.problemservice
      delete data.problemcausetype
      delete data.problemservice
      Object.keys(data).forEach(k => {
        if (Array.isArray(data[k])) {
          data[k] = data[k].filter((v: string) => !!v).sort()
        }
      })
      this.filterData = data
    } else {
      this.filterData = {}
    }
  }
}
</script>

<style lang="scss" scoped>
.problem-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .problem-content {
    flex: 1;
    min-height: 600px;
    border-radius: 4px;
    background-color: var(--bg-color);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .problem-content-body {
    flex: 1;
    overflow: hidden;
    display: flex;
    .collapse-choose {
      height: 100%;
      background-color: var(--bg-color);
    }
  }

  .toggle-chart-btn {
    box-sizing: border-box;
    width: 32px;
    height: 32px;
    border: 1px solid var(--border-color-base);
    transition: all .3s ease;
    &.active {
      color: var(--color-primary);
    }
  }

  .problem-list {
    flex: 1;
    overflow: hidden;

    .problem-table {
      margin-top: -10px;
      flex: 1;
    }
  }
}

.section-item {
  width: 100%;
  height: 200px;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  display: inline-block;
  vertical-align: top;
  color: var(--color-text-primary);

  .section-title {
    display: flex;
    justify-content: space-between;
    padding: 16px 20px 0;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
  }

  .section-cont {
    height: calc(100% - 38px);
    padding: 0 10px;
  }
}
</style>
