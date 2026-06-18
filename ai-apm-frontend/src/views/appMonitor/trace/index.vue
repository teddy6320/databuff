<template>
  <div class="trace-container">
    <div class="trace-wrap">
      <el-alert v-if="traceIdOversize" :title="$t('modules.views.appMonitor.trace.s_513788d5')" type="error" :closable="false" class="mb-10" />

      <search-group
        ref="searchGroup"
        :timeParams="timeParams"
        :serviceList="serviceList"
        @on-change="searchChangeHandle" />

      <chart-group
        ref="chartGroup"
        :query="queryParams"
        :timeParams="timeParams"
        @chart-click="chartClickHandle"
        :searchInitLoading='searchInitLoading'
        :hasRequestAttrParams='hasRequestAttrParams'
        class="chart-group" />
      
      <div v-if="showList" :class='["trace-cont", collapsed || hasRequestAttrParams ? "is-collapsed" : ""]'>
        <span v-show='collapsed' @click="toggleFilterHandle(false)" class="db-icon db-icon-unfold cp font-12 fixed-btn"></span>

        <choose-collapse
          v-show='!hasRequestAttrParams'
          :collapsed='collapsed'
          ref="chooseCollapse"
          componentType="service.trace"
          :query="queryParams"
          @on-filter-change="filterChangeHandle"
          @on-toggle-filter="toggleFilterHandle"
          class="list-collapse-choose"
        />

        <table-list
          ref="tableList"
          :query="queryParams"
          :filter="queryFilter"
          :queryLoading="queryLoading"
          class="list"
        />
      </div>

      <div v-else class="trace-cont hide-list">{{ $t('modules.views.appMonitor.serviceCallDetail.s_16853987') }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Getter } from 'vuex-class';
import dayjs from 'dayjs';
import ChartGroup from './overviewChart.vue';
import SearchGroup from './search-group.vue';
import ChooseCollapse from './choose-collapse.vue'
import TableList from './table-list.vue'
import { toAsyncWait } from '@/utils/common';
import ApmApi from '@/api/service';
import { cloneDeep, orderBy } from 'lodash';

const StaticParams = {
  parentId: '0',
}

@Component({
  components: {
    ChartGroup,
    SearchGroup,
    ChooseCollapse,
    TableList,
  }
})
export default class Trace extends Vue {
  public $refs!: {
    chartGroup: ChartGroup
    searchGroup: SearchGroup
    chooseCollapse: ChooseCollapse
    tableList: TableList
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private timeParams = {
    fromTime: '',
    toTime: '',
    interval: 3600,
  }

  private showList = false; // 是否显示列表区域，默认不显示
  private queryParams: any = {}
  private queryFilter: any = {}
  private queryLoading = false;

  private searchInitLoading = false;

  private serviceList: any[] = [];

  private traceIdOversize = false;

  get hasRequestAttrParams () {
    return Object.keys(this.queryParams?.tags || {}).length
  }

  private async mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
    await this.queryServiceIdNames();
    await this.durationChangeHandle();
    const { sf, st } = this.$route.query
    if (sf && st && !isNaN(Number(sf)) && !isNaN(Number(st)) && sf.length === 13 && st.length === 13) {
      const _multisearch = decodeURIComponent(this.$route.query.multisearch as string || '');
      const _multiList: string[] = _multisearch.split(';')
      const _errorIdx = _multiList.findIndex((item: string) => item.indexOf('error=1') === 0);
      this.chartClickHandle(dayjs(Number(sf)).format('YYYY-MM-DD HH:mm'), _errorIdx > -1 ? 'error' : '')
    }
  }

  private beforeDestroy () {
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh');
  }

  private async durationChangeHandle () {
    this.showList = false;
    this.regetGlobalTime()
    this.searchInitLoading = true
    await this.$refs.searchGroup.init(cloneDeep(this.timeParams)).then((payload: any) => {
      const { filter: data } = payload;

      if (data.sid) {
        data.serviceId = data.sid
        delete data.sid
      }
      if (data.si) {
        data.serviceInstance = data.si
        delete data.si
      }
      this.traceIdOversize = (data.traceIds || []).length > 100
      if (data.traceIds) {
        data.traceIds = data.traceIds.slice(0, 100)
      }
      this.queryParams = { ...StaticParams, ...data };
      this.$nextTick(() => {
        this.$refs.chartGroup && this.$refs.chartGroup.getData()
      })
    }).finally(() => {
      this.searchInitLoading = false
    })
  }

  private regetGlobalTime () {
    const { fromTime, toTime, interval } = this.getGlobalTimeV2()
    this.timeParams = { fromTime, toTime, interval }
  }

  // 图表点击事件回调
  private chartClickHandle (xAxisName: string, type?: string) {
    const { toTime, interval } = this.timeParams
    this.queryParams.fromTime = xAxisName + ':00'
    const _toTime = +new Date(xAxisName) + interval * 1000
    if (_toTime <= +new Date(toTime)) {
      this.queryParams.toTime = dayjs(_toTime).format('YYYY-MM-DD HH:mm:ss')
    } else {
      this.queryParams.toTime = toTime
    }
    const isErrorChart = type === 'error'
    // 错误统计图表点击，需要选中状态为“错误”的筛选项；其他图表清除状态筛选
    let _multisearch = decodeURIComponent(this.$route.query.multisearch as string || '');
    if (!this.showList) {
      this.showList = true;
      _multisearch = '';
    }
    const _multiList: string[] = _multisearch.split(';')
    const _errorIdx = _multiList.findIndex((item: string) => item.indexOf('error=') === 0);
    if (_errorIdx === -1 && isErrorChart) {
      _multiList.push('error=1')
    } else if (_errorIdx > -1 && isErrorChart) {
      const [_key, ...remain] = _multiList[_errorIdx].split('=');
      const values = remain.join('=').split(',')
      if (!values.includes('1')) {
        values.push('1')
        _multiList.splice(_errorIdx, 1, `${_key}=${values.join(',')}`)
      }
    } else if (_errorIdx > -1) {
      _multiList.splice(_errorIdx, 1)
    }
    this.$router.replace({
      query: {
        ...this.$route.query,
        multisearch: encodeURIComponent(_multiList.join(';')),
        sf: String(new Date(this.queryParams.fromTime).valueOf()),
        st: String(new Date(this.queryParams.fromTime).valueOf()),
      }
    })
    this.queryLoading = true;
    this.$nextTick(() => {
      this.$refs.chooseCollapse.init().then((filter: any) => {
        this.queryFilter = { ...filter }
        this.$nextTick(() => {
          this.$refs.tableList && this.$refs.tableList.refresh()
        })
        this.queryLoading = false;
      }).catch(() => this.queryLoading = false)
    })
  }

  private searchChangeHandle (data: any, routerQuery: any) {
    if (data.sid) {
      data.serviceId = data.sid
      delete data.sid
    }
    if (data.si) {
      data.serviceInstance = data.si
      delete data.si
    }
    if (JSON.stringify(data) === JSON.stringify(this.queryParams)) {
      return
    }
    this.showList = false;
    this.clearFilter(routerQuery);
    this.traceIdOversize = (data.traceIds || []).length > 100
    if (data.traceIds) {
      data.traceIds = data.traceIds.slice(0, 100)
    }
    this.queryParams = { ...StaticParams, ...data }
    this.$nextTick(() => {
      this.$refs.chartGroup && this.$refs.chartGroup.getData()
    })
  }

  private filterChangeHandle (data: any) {
    if (JSON.stringify(data) === JSON.stringify(this.queryFilter)) {
      return
    }
    this.queryFilter = { ...data }
    this.$nextTick(() => {
      this.$refs.tableList && this.$refs.tableList.refresh()
    })
  }

  // 清空filter
  private clearFilter (routerQuery: any) {
    this.queryFilter = {}
    const query = { ...routerQuery };
    delete query.multisearch;
    delete query.sf;
    delete query.st;
    this.$router.replace({ query });
  }

  private collapsed: boolean = false
  private toggleFilterHandle (show: boolean) {
    this.collapsed = show
  }

  private async queryServiceIdNames () {
    const { result, error } = await toAsyncWait(ApmApi.getServicesIds({ fromTime: '', toTime: '', ignoreTime: 1 }))
    if (!error) {
      const { data = [] } = result || {};
      const serviceNameIdMap: any = {};
      data.forEach((t: any) => {
        serviceNameIdMap[t.name] = t.id
      });
      this.serviceList = orderBy(Object.keys(serviceNameIdMap), [t => t.toLocaleLowerCase()], ['asc']).map(t => ({ label: t, value: serviceNameIdMap[t] }))
    }
  }
}
</script>

<style lang="scss" scoped>
.trace-container {
  flex: 1;
  height: 100%;
  padding: 16px;
  overflow: hidden;
}
.trace-wrap {
  flex: 1;
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;
  background-color: var(--bg-color);

  .chart-group {
    margin: 16px -8px 20px;
  }

  .trace-cont {
    flex: 1;
    min-height: 300px;
    display: flex;
    position: relative;

    &:not(.hide-list) {
      padding-left: 188px;
      transition: padding-left .3s ease;

      &.is-collapsed {
        padding-left: 0;
      }
    }
  
    &.hide-list {
      align-items: center;
      justify-content: center;
      background-color: var(--bg-color);
      font-size: 14px;
      color: var(--color-text-regular);
    }

    .db-icon-unfold {
      width: 17px;
      height: 25px;
      line-height: 25px;
      background-color: var(--bg-color);
      border-radius: 0 4px 4px 0;
      box-shadow: 0px 1px 4px 0px rgba(139, 142, 147, 0.3);
    }
    .fixed-btn {
      position: absolute;
      left: -20px;
      top: 5px;
    }
  }

  .list-collapse-choose {
    height: 100%;
    position: absolute;
    left: 0;
    top: 0;
  }

  .list {
    flex: 1;
    overflow: hidden;
  }
}
</style>
