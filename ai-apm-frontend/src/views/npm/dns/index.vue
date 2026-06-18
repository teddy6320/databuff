<template>
  <div class="analysis-wrapper">
    <search-group
      ref="searchGroup"
      :isDns="true"
      @on-change="searchChangeHandle" />

    <div class="body">
      <choose-collapse
        v-show="filterActive"
        ref="chooseCollapse"
        :isDns="true"
        :query="queryParams"
        :timeParams="timeParams"
        @on-filter-change="filterChangeHandle"
        class="list-collapse-choose"
      />

      <div class="content">
        <chart-group
          ref="chartGroup"
          :query="{
            from: queryParams.from,
          }"
          :filter="queryFilter"
          :timeParams="timeParams"
          :queryLoading="queryLoading"
          @on-toggle-chart="toggleChartHandle"
          @on-toggle-filter="toggleFilterHandle"
        />

        <table-list
          ref="tableList"
          :showChart="showChart"
          :query="queryParams"
          :filter="queryFilter"
          :timeParams="timeParams"
          :queryLoading="queryLoading"
          @show-detail="toggleDetailHandle"
          :class="['list', { 'hide-chart': !showChart }]"
        />
      </div>
    </div>

    <!-- 流量详情 -->
    <el-drawer :visible.sync='showDetail' destroy-on-close size="742px">
      <div slot='title' class="drawer-title">
        {{ detail._client || '-' }} ⇆ {{ detail._server || '-' }}
        <span class="drawer-title-time">{{ $t('modules.views.npm.analysis.s_f01b219a', { value0: (detail._toTime || '').substring(0, 16) }) }}</span>
      </div>

      <traffic-detail v-if="showDetail" :detail="detail" :timeParams="timeParams" />
    </el-drawer>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import { Getter } from 'vuex-class';
import dayjs from 'dayjs';
import SearchGroup from '../analysis/search-group.vue';
import ChooseCollapse from '../analysis/choose-collapse.vue'
import ChartGroup from './chart-group.vue'
import TableList from './table-list.vue'
import TrafficDetail from './detail/index.vue'

@Component({
  components: {
    SearchGroup,
    ChooseCollapse,
    ChartGroup,
    TableList,
    TrafficDetail,
  }
})
export default class Dns extends Vue {
  @Getter('globalTime') private globalTimeFunc!: any;
  @Getter('globalTimeInited') private globalTimeInited!: boolean;

  public $refs!: {
    searchGroup: SearchGroup
    chooseCollapse: ChooseCollapse
    chartGroup: ChartGroup
    tableList: TableList
  }

  get globalTime () {
    return this.globalTimeFunc()
  }
  @Watch('globalTime', { deep: true })
  private watchGlobalTime() {
    if (!this.globalTimeInited) {
      return
    }
    this.durationChangeHandle()
  }

  private timeParams = {
    fromTime: '',
    toTime: '',
    interval: '',
  }

  private queryParams = {
    client: '',
    server: '',
    from: [],
  }
  private queryFilter: any = {}
  private queryLoading = true

  private async mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });

    this.durationChangeHandle()
  }

  private beforeDestroy () {
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh');
  }

  private durationChangeHandle () {
    this.regetGlobalTime()
    this.queryLoading = true;
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        this.queryParams = { ...data }
        this.$nextTick(() => {
          this.$refs.chooseCollapse.init().then((filter: any) => {
            this.queryFilter = { ...filter }
            this.getChartAndListData()
            this.queryLoading = false;
          }).catch(() => this.queryLoading = false)
        })
      })
    })
  }

  private regetGlobalTime () {
    const { fromTime, toTime, interval } = this.globalTimeFunc()
    this.timeParams.fromTime = dayjs(fromTime).format('YYYY-MM-DD HH:mm:ss')
    this.timeParams.toTime = dayjs(toTime).format('YYYY-MM-DD HH:mm:ss')
    this.timeParams.interval = interval
  }

  private searchChangeHandle (data: any) {
    if (JSON.stringify(data) === JSON.stringify(this.queryParams)) {
      return
    }
    this.queryParams = { ...data }
    this.queryLoading = true;
    this.$nextTick(() => {
      this.$refs.chooseCollapse.init().then((filter: any) => {
        this.queryFilter = { ...filter }
        this.getChartAndListData()
        this.queryLoading = false;
      }).catch(() => this.queryLoading = false)
    })
  }

  private filterChangeHandle (data: any) {
    if (JSON.stringify(data) === JSON.stringify(this.queryFilter)) {
      return
    }
    this.queryFilter = { ...data }
    this.getChartAndListData()
  }

  private getChartAndListData () {
    this.$nextTick(() => {
      this.$refs.chartGroup && this.$refs.chartGroup.getData()
      this.$refs.tableList && this.$refs.tableList.getData()
    })
  }

  private showChart = true
  private toggleChartHandle (show: boolean) {
    this.showChart = show
  }

  private filterActive: boolean = true
  private toggleFilterHandle (show: boolean) {
    this.filterActive = show
  }

  private showDetail = false
  private detail: any = {}
  private toggleDetailHandle (detail?: any) {
    this.showDetail = !!detail
    this.detail = detail || {}
  }
}
</script>

<style lang="scss" scoped>
.analysis-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .body {
    margin-top: 16px;
    flex: 1;
    min-height: 700px;
    display: flex;
  }

  .list-collapse-choose {
    height: 100%;
    margin-right: 16px;
  }

  .content {
    height: 100%;
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .list {
      margin-top: 16px;
      height: calc(100% - 330px);
      &.hide-chart {
        height: calc(100% - 70px);
      }
    }
  }
}

.drawer-title {
  flex: none;
  display: inline-block;
  max-width: calc(100% - 32px);
  padding-right: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  position: relative;
  .drawer-title-time {
    color: var(--color-text-regular);
    text-align: center;
    font-size: 13px;
    position: absolute;
    top: 0px;
    right: 16px;
  }
}
</style>
