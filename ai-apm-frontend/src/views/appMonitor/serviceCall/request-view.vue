<template>
  <div class="service-call-view-wrap">
    <search-group
      ref="searchGroup"
      :componentType="params.componentType"
      @on-change="searchChangeHandle"
      class="search-group" />

    <div v-loading="infoLoading" class="service-call-header">
      <div class="header-item initiator">
        <div class="header-item-title">
          <span class="db-icon">{{ info.srcServiceType | getDbIconFilter }}</span>
          {{ info.srcServiceName || '-' }}
        </div>
        <div class="header-item-cont">
          <div class="info-t">{{ $t('modules.views.appMonitor.serviceCall.s_7ffd54cc') }}<span class="count">{{ info.reqOutCnt | NumberFilter }}</span></div>
          <div class="info-t">{{ $t('modules.views.appMonitor.serviceCall.s_0d652d40') }}<span class="count">{{ info.reqOutAvgTime | NsFilter }}</span></div>
          <div class="info-t">{{ $t('modules.views.appMonitor.cache.s_0c8524d7') }}<span class="count">{{ info.reqOutErrRate | PercentFilter }}</span></div>
        </div>
      </div>
      <div class="header-split">{{ $t('modules.views.appMonitor.serviceCall.s_97d29d84') }}<span class="arrow">➞</span></div>
      <div class="header-item receiver">
        <div class="header-item-title">
          <span class="db-icon">{{ info.serviceType | getDbIconFilter }}</span>
          {{ info.serviceName || '-' }}
        </div>
        <div class="header-item-cont">
          <div class="info-t">{{ $t('modules.views.appMonitor.serviceCall.s_ac4a2e0c') }}<span class="count">{{ info.reqInCnt | NumberFilter }}</span></div>
          <div class="info-t">{{ $t('modules.views.appMonitor.serviceCall.s_0d652d40') }}<span class="count">{{ info.reqInAvgTime | NsFilter }}</span></div>
          <div class="info-t">{{ $t('modules.views.appMonitor.cache.s_0c8524d7') }}<span class="count">{{ info.reqInErrRate | PercentFilter }}</span></div>
        </div>
      </div>
    </div>

    <chart-group
      ref="chartGroup"
      :query="allParams"
      :timeParams="timeParams" />

    <table-list
      ref="tableList"
      :query="allParams"
      :timeParams="timeParams"
      :componentType="params.componentType"
      class="service-call-list" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { toAsyncWait } from '@/utils/common';
import ServiceApi from '@/api/service';
import SearchGroup from './search-group.vue';
import ChartGroup from './chart-group.vue';
import TableList from './table-list.vue'
import { DbIconFilter } from '@/utils/filters/common';

@Component({
  components: {
    SearchGroup,
    ChartGroup,
    TableList,
  },
  filters: {
    getDbIconFilter (type: string) {
      switch (type) {
        case 'business':
          return DbIconFilter('bs-list');
        case 'subbus':
          return DbIconFilter('subbs-list');
        case 'web':
        case 'custom':
          return DbIconFilter('service-fill');
        case 'browser':
          return DbIconFilter('browser');
        case 'ios':
          return DbIconFilter('ios');
        case 'android':
          return DbIconFilter('android');
        default:
          return DbIconFilter(`${type}-fill`);
      }
    }
  }
})
export default class RequestView extends Vue {
  @Prop({ default: () => ({}) }) private params!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;

  public $refs!: {
    searchGroup: SearchGroup
    chartGroup: ChartGroup
    tableList: TableList
  }

  private info: any = {} // 发起端、接收端的详细信息
  private infoLoading = false

  private queryParams: any = {}
  get allParams () {
    return {
      ...this.params,
      ...this.queryParams,
    }
  }

  private created () {
    const { componentType, sn, sid, st, srcSn, srcSid, srcSt } = this.$route.query
    this.info = {
      componentType,
      serviceName: decodeURIComponent(sn as string),
      serviceType: st,
      serviceId: decodeURIComponent(sid as string),
      srcServiceName: decodeURIComponent(srcSn as string),
      srcServiceType: srcSt,
      srcServiceId: decodeURIComponent(srcSid as string),
    }
  }

  public getData () {
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        this.queryParams = { ...data }
        this.getServiceCallInfo()
        this.getChartAndListData()
      })
    })
  }

  private searchChangeHandle (data: any) {
    if (JSON.stringify(data) === JSON.stringify(this.queryParams)) {
      return
    }
    this.queryParams = { ...data }
    this.getServiceCallInfo()
    this.getChartAndListData()
  }

  private async getChartAndListData () {
    this.$nextTick(() => {
      this.$refs.chartGroup && this.$refs.chartGroup.getData()
      this.$refs.tableList && this.$refs.tableList.getData()
    })
  }

  private async getServiceCallInfo () {
    const { componentType, sn, sid, st, srcSn, srcSid, srcSt } = this.$route.query
    const { fromTime, toTime } = this.timeParams
    const params: any = {
      ...this.allParams,
      fromTime,
      toTime,
    }
    this.infoLoading = true;
    const { result, error } = await toAsyncWait(ServiceApi.getServiceCallInfo(params));
    this.infoLoading = false;
    if (!error) {
      const data = (result || {}).data || {}
      const { reqOutCnt, reqOutTime, reqInCnt, reqInTime } = data
      data.reqOutAvgTime = reqOutCnt > 0 ? (reqOutTime || 0) / reqOutCnt : '-'
      data.reqOutErrRate = reqOutCnt > 0 ? (data.reqOutErrCnt || 0) / reqOutCnt : '-'
      data.reqInAvgTime = reqInCnt > 0 ? (reqInTime || 0) / reqInCnt : '-'
      data.reqInErrRate = reqInCnt > 0 ? (data.reqInErrCnt || 0) / reqInCnt : '-'
      this.info = {
        serviceName: this.info.serviceName,
        serviceType: this.info.serviceType,
        serviceId: this.info.serviceId,
        srcServiceName: this.info.srcServiceName,
        srcServiceType: this.info.srcServiceType,
        srcServiceId: this.info.srcServiceId,
        ...data,
      }
      if (data?.componentType === 'service.browser' || data?.componentType === 'service.ios' || data?.componentType === 'service.android') {
        this.info.srcServiceName = data?.srcServiceName || decodeURIComponent(srcSn as string)
        this.info.srcServiceType = data?.srcServiceType || srcSt
        this.info.srcServiceId = data?.srcServiceId || decodeURIComponent(srcSid as string)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.service-call-view-wrap {
  display: flex;
  flex-direction: column;
}

.service-call-header {
  margin-top: 16px;
  display: flex;
  .header-split {
    width: 10%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    .arrow {
      display: block;
      font-size: 40px;
      line-height: 1;
      transform: scale(1.2);
    }
  }
  .header-item {
    width: 45%;
    background: var(--bg-color);
  }
  .header-item-title {
    padding: 16px 20px 12px 46px;
    font-size: 16px;
    line-height: 22px;
    color: var(--color-text-primary);
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    position: relative;

    .db-icon {
      padding: 18px 0 0 20px;
      font-size: 20px;
      line-height: 1;
      text-align: center;
      display: block;
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      pointer-events: none;
    }
  }
  .header-item-cont {
    padding: 0 20px 16px;
    display: flex;
    justify-content: space-between;
    .info-t {
      width: 33%;
      display: flex;
      flex-direction: column;
      align-items: center;
      line-height: 18px;
      font-size: 12px;
      color: var(--color-text-regular);
      .count {
        padding: 4px 6px 0;
        min-width: 60px;
        font-size: 22px;
        line-height: 26px;
        color: var(--color-text-primary);
        text-align: center;
        white-space: nowrap;
      }
    }
  }
}

.service-call-list {
  margin-top: 16px;
  flex: 1;
  min-height: 400px;
  overflow: hidden;
}
</style>
