<template>
  <div
    v-loading="queryLoading"
    class="service-analysis-wrapper">
    <div class="bg-color p20">
      <search-group
        ref="searchGroup"
        :timeParams="timeParams"
        :serviceList="serviceList"
        @on-change="searchChangeHandle"
        @on-search-loading="searchLoadingHandle" />

      <chart-group
        ref="chartGroup"
        :query="queryParams"
        :timeParams="timeParams"
        class="chart-group" />

      <table-list
        ref="tableList"
        :query="queryParams"
        :timeParams="timeParams"
        @add-query="durationChangeHandle"
        class="service-analysis-list" />
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Getter } from 'vuex-class';
import dayjs from 'dayjs';
import { orderBy } from 'lodash';
import SearchGroup from './search-group.vue';
import ChartGroup from './chart-group.vue';
import TableList from './table-list.vue'
import { toAsyncWait } from '@/utils/common';
import ServiceApi from '@/api/service';

@Component({
  components: {
    SearchGroup,
    ChartGroup,
    TableList,
  }
})
export default class ServiceErrors extends Vue {
  @Getter('globalTime') private globalTimeFunc!: any;
  @Getter('globalTimeInited') private globalTimeInited!: boolean;

  public $refs!: {
    searchGroup: SearchGroup
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
    interval: 3600,
  }

  private queryLoading = false;
  private queryParams: any = {}

  private serviceList: any[] = [];
  private resourceList: any[] = [];

  private async mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
    this.queryLoading = true;
    await this.queryServiceIdNames();
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
      this.$refs.searchGroup.init().then(async (data: any) => {
        await this.searchChangeHandle(data, true)
        this.queryLoading = false;
      }).catch(() => {
        this.queryLoading = false;
      })
    })
  }

  private regetGlobalTime () {
    const { fromTime, toTime, interval } = this.globalTimeFunc()
    this.timeParams.fromTime = dayjs(fromTime).format('YYYY-MM-DD HH:mm:ss')
    this.timeParams.toTime = dayjs(toTime).format('YYYY-MM-DD HH:mm:ss')
    this.timeParams.interval = interval
  }

  private async searchChangeHandle (data: any, timeChange?: boolean) {
    this.queryLoading = false;
    if (data.sid) {
      data.serviceId = data.sid
      delete data.sid
    }
    if (data.si) {
      data.serviceInstance = data.si
      delete data.si
    }
    if (!timeChange && JSON.stringify(data) === JSON.stringify(this.queryParams)) {
      return
    }
    this.queryParams = { ...data }
    this.$nextTick(() => {
      this.$refs.chartGroup && this.$refs.chartGroup.getData()
      this.$refs.tableList && this.$refs.tableList.getData()
    })
  }

  private searchLoadingHandle (loading: boolean) {
    this.queryLoading = loading
  }

  private async queryServiceIdNames () {
    const { result, error } = await toAsyncWait(ServiceApi.getServicesIds({ fromTime: '', toTime: '', ignoreTime: 1 }))
    if (!error) {
      const { data = [] } = result || {};
      const serviceNameIdMap: any = {}
      data.forEach((t: any) => {
        serviceNameIdMap[t.name] = t.id
      });
      this.serviceList = orderBy(Object.keys(serviceNameIdMap), [t => t.toLocaleLowerCase()], ['asc'])
          .map(t => ({ label: t, value: serviceNameIdMap[t] }))
    }
  }
}
</script>

<style lang="scss" scoped>
.service-analysis-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .service-analysis-list {
    margin-top: 20px;
    flex: 1;
    min-height: 400px;
    overflow: hidden;
  }
}
</style>
