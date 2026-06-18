<template>
  <div class="baseinfo-cont">
    <div class="baseinfo-wrapper">
      
      <div class="chart-group">
        <div v-for='value,key in chartGroup' :key='key' class="chart-item br-4">
          <h3 class="fw-normal font-14 chart-title">{{ key | chartTitleFilter }}</h3>
          <basic-chart
            :showEmpty="!chartGroup[key].loading && !chartGroup[key].source.length"
            :key='key'
            :showLegend='true'
            :compactGrid="true"
            :textSmallMode="true"
            :minInterval="1"
            :min="0"
            group='baseinfo'
            :yAxisSplitNum="3"
            :interval="timeParams.interval"
            :source='chartGroup[key].source'
            :tsSource='chartGroup[key].tsSource'
            @on-ts-tooltip-show='onTsTooltipShow'
          >
            <template slot='ts'>
              <ChartTsSlot :current='currentTsItem' />
            </template>
          </basic-chart>
        </div>
      </div>

      <div class="attribute-group mt-20">
        <h3 class="m-0 fw-normal font-14">{{ $t('modules.views.appMonitor.resourceDetail.s_c5ea2ca1') }}</h3>
        <div class="mt-10 attribute-group-wrapper">
          <div class="attribute-item">
            <label class="attribute-item-label">
              <i></i>
              <span>{{ $t('modules.views.appMonitor.serviceDetail.s_8124816e') }}</span>
            </label>
            <span class="">{{ getServiceInstance || '-' }}</span>
          </div>
          <div class="attribute-item">
            <label class="attribute-item-label">
              <i></i>
              <span>{{ getServiceShowTitle }}</span>
            </label>
            <span class="">{{ getServiceName || '-' }}</span>
          </div>
          <div class="attribute-item">
            <label class="attribute-item-label">
              <i></i>
              <span>{{ getServiceTypeTitle }}</span>
            </label>
            <span class="">{{ getServiceOriginType | ServiceTypeFilter }}</span>
          </div>
          <div class="attribute-item">
            <label class="attribute-item-label">
              <i></i>
              <span>{{ getServiceTechTitle }}</span>
            </label>
            <span class="">
              <el-tag v-for='tag in getServiceTechnology' :key='tag' type='info' effect="plain" size="mini" class="mr-5 mb-5">{{ tag }}</el-tag>
              <span v-if='!getServiceTechnology.length'>-</span>
            </span>
          </div>
          <div class="attribute-item">
            <label class="attribute-item-label">
              <i></i>
              <span>{{ $t('modules.views.appMonitor.serviceInstance.s_e14c464e', { value0: getServiceOriginType !== 'db' ? '' : $t('modules.views.appMonitor.relationMapNew.s_68051bf4') }) }}</span>
            </label>
            <span class="">{{ getServiceHost || '-' }}</span>
          </div>
          <div class="attribute-item">
            <label class="attribute-item-label">
              <i></i>
              <span>{{ $t('modules.views.appMonitor.serviceInstance.s_c1c46a15', { value0: getServiceOriginType !== 'db' ? '' : $t('modules.views.appMonitor.relationMapNew.s_68051bf4') }) }}</span>
            </label>
            <span class="">{{ getServiceHostIP || '-' }}</span>
          </div>
          <div class="attribute-item">
            <label class="attribute-item-label">
              <i></i>
              <span>{{ $t('modules.views.appMonitor.service.s_a094e5b7') }}</span>
            </label>
            <span class="">{{ displayDatasource || '-' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang='ts'>
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import { toAsyncWait } from '@/utils/common';
import ServiceApi from '@/api/service';
import ChartTsSlot from '@/views/appMonitor/serviceAnalysis/chart-ts-slot.vue';

@Component({
  components: { ChartTsSlot },
  filters: {
    chartTitleFilter (key: string) {
      switch (key) {
        case 'response':
          return i18n.t('modules.views.appMonitor.relationMap.s_207c26c9') as string;
        case 'error':
          return i18n.t('modules.views.appMonitor.cache.s_0c8524d7') as string;
        case 'request':
          return i18n.t('modules.views.appMonitor.resourceDetail.s_0f81e359') as string;
      }
    }
  }
})
export default class TabBaseinfo extends Vue {
  @Prop({ default: {} }) private current!: any;
  @Prop() private getDatabuffSource!: string;

  get displayDatasource () {
    return this.getDatabuffSource || this.current?.datasource || ''
  }

  @Watch('current', { immediate: true })
  private onCurrentChange (val: any, oldVal: any) {
    if (val && val?.serviceInstance !== oldVal?.serviceInstance && val?.serviceId !== oldVal?.serviceId  && this.isMounted) {
      this.fetchAllData();
    }
  }

  private isMounted = false;

  private timeParams = {
    fromTime: '',
    toTime: '',
    interval: 60,
  }

  private chartGroup: any = {
    response: {
      loading: true,
      source: [],
      tsSource: [],
    },
    request: {
      loading: true,
      source: [],
      tsSource: [],
    },
    error: {
      loading: true,
      source: [],
      tsSource: [],
    },
  }
  private currentTsItem: any = null;

  get getServiceInstance () {
    return this.current?.serviceInstance || '-'
  }
  get getServiceName () {
    return this.current && this.current.name || '-'
  }

  get getServiceOriginType () {
    return this.current && this.current.service_type || '-'
  }

  get getServiceShowTitle () {
    switch (this.getServiceOriginType) {
      case 'db':
        return i18n.t('modules.views.appMonitor.database.s_d5f399b9') as string
      case 'mq':
        return i18n.t('modules.views.appMonitor.msgQueue.s_b12299c1') as string
      default:
        return i18n.t('modules.views.alarmCenter.alarm.s_8f3747c0') as string
    }
  }

  get getServiceTypeTitle () {
    switch (this.getServiceOriginType) {
      case 'db':
      case 'mq':
        return i18n.t('modules.views.aiPlatform.experts.s_226b0912') as string
      default:
        return i18n.t('modules.views.appMonitor.serviceInstance.s_9b6daae9') as string
    }
  }

  get getServiceTechTitle () {
    switch (this.getServiceOriginType) {
      case 'db':
      case 'mq':
        return i18n.t('modules.views.appMonitor.serviceDetail.s_a95dd3e1') as string
      default:
        return i18n.t('modules.views.appMonitor.serviceInstance.s_652c65cd') as string
    }
  }

  get getServiceTechnology () {
    return this.current && this.current.technology && typeof this.current.technology === 'string' ? this.current.technology.split(',') : []
  }

  get getServiceHost () {
    return this.current?.hostName || '-'
  }
  get getServiceHostIP () {
    return this.current?.hostIp || '-'
  }

  get isLoading () {
    return this.chartGroup.response.loading || this.chartGroup.request.loading || this.chartGroup.error.loading
  }

  @Watch('isLoading')
  private onIsLoading (newVal: boolean) {
    if (!newVal) {
      this.$emit('on-loaded')
    }
  }

  private created () {
    this.$emit('on-created');
    this.resetTimeParams();
  }
  private mounted () {
    if (this.current?.serviceId ) {
      this.refresh();
    }
    this.isMounted = true;
  }

  public refresh () {
    this.fetchAllData();
  }

  private resetTimeParams () {
    const { fromTime, toTime, interval } = this.getGlobalTimeV2();
    this.timeParams = { fromTime, toTime, interval };
  }

  private fetchAllData () {
    this.resetTimeParams();
    this.fetchResponseSource();
    this.fetchErrorSource();
    this.fetchRequestSource();
  }

  // 响应时间
  private async fetchResponseSource () {
    const serviceId = this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid));
    const _params: any = {
      serviceId,
      serviceInstance: this.current?.serviceInstance || decodeURIComponent(String(this.$route.query.si)),
      startTime: this.timeParams.fromTime,
      endTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
    }
    this.chartGroup.response.loading = true;
    // 需请求两个指标
    const avgTimeRst = await toAsyncWait(ServiceApi.getServiceRequestMetric({ ..._params, metric: 'avgTime' }))
    const avgTimeData = avgTimeRst?.result?.data || []
    const reqCntRst = await toAsyncWait(ServiceApi.getServiceRequestMetric({ ..._params, metric: 'reqCount' }))
    const reqCntData = reqCntRst?.result?.data || []
    if (!avgTimeData.length && !reqCntData.length) {
      this.chartGroup.response.source = []
    } else {
      this.chartGroup.response.source = [{
        name: i18n.t('modules.views.appMonitor.relationMap.s_207c26c9') as string, nameKey: 'modules.views.appMonitor.relationMap.s_207c26c9',
        type: 'line',
        area: true,
        unit: 'ns',
        data: (avgTimeData[0]?.values as any[] || []).map(([timestamp, value]) => ({
          key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
          value,
        }))
      }, {
        name: i18n.t('modules.views.appMonitor.relationMap.s_ae1e7b60') as string, nameKey: 'modules.views.appMonitor.relationMap.s_ae1e7b60',
        type: 'bar',
        data: (reqCntData[0]?.values as any[] || []).map(([timestamp, value]) => ({
          key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
          value,
        }))
      }]
    }
    // v2.9.1 ++
    if (avgTimeData?.length || reqCntData?.length) {
      const avgRootDetails = (avgTimeData || []).map((i: any) => i?.rootDetails || []).filter((i: any) => i.length).flat();
      const reqRootDetails = (reqCntData || []).map((i: any) => i?.rootDetails || []).filter((i: any) => i.length).flat();
      const rootDetails =  avgRootDetails.length ? avgRootDetails : reqRootDetails.length ? reqRootDetails : [];
      const tsSource = rootDetails.filter((i: any) => i.abnormalStartTime && i.abnormalEndTime).map((i: any) => {
        const { abnormalStartTime, abnormalEndTime } = i;
        return {
          duration: [
            Number(abnormalStartTime),
            Number(abnormalEndTime)
          ],
          status: 'danger',
          info: {
            ...i
          }
        }
      });
      this.chartGroup.response.tsSource = tsSource;
    } else {
      this.chartGroup.response.tsSource = [];
    }
    this.chartGroup.response.loading = false;
  }
  // 错误率
  private async fetchErrorSource () {
    const serviceId = this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid));
    const _params: any = {
      serviceId,
      serviceInstance: this.current?.serviceInstance || decodeURIComponent(String(this.$route.query.si)),
      startTime: this.timeParams.fromTime,
      endTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
    }
    this.chartGroup.error.loading = true;
    // 需请求两个指标
    const typeErrorRst = await toAsyncWait(ServiceApi.getServiceRequestMetric({ ..._params, metric: 'typeErrCount' }))
    const typeErrorData = typeErrorRst?.result?.data || []
    const errorRateRst = await toAsyncWait(ServiceApi.getServiceRequestMetric({ ..._params, metric: 'errRate' }))
    const errorRateData = errorRateRst?.result?.data || []
    if (!typeErrorData.length && !errorRateData.length) {
      this.chartGroup.error.source = []
    } else {
      this.chartGroup.error.source = [
        ...(typeErrorData.map((item: any) => ({
          name: item?.tags?.errorType || '',
          type: 'bar',
          stack: 'total',
          data: (item?.values as any[] || []).map(([timestamp, value]) => ({
            key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
            value,
          }))
        }))),
        {
          name: i18n.t('modules.views.appMonitor.cache.s_0c8524d7') as string, nameKey: 'modules.views.appMonitor.cache.s_0c8524d7',
          unit: '%',
          data: (errorRateData[0]?.values as any[] || []).map(([timestamp, value]) => ({
            key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
            value,
          }))
        }
      ]
    }
    // v2.9.1 ++
    if (errorRateData?.length) {
      const rootDetails = (errorRateData || []).map((i: any) => i?.rootDetails || []).flat();
      const tsSource = rootDetails.filter((i: any) => i.abnormalStartTime && i.abnormalEndTime).map((i: any) => {
        const { abnormalStartTime, abnormalEndTime } = i;
        return {
          duration: [
            Number(abnormalStartTime),
            Number(abnormalEndTime)
          ],
          status: 'danger',
          info: {
            ...i
          }
        }
      });
      this.chartGroup.error.tsSource = tsSource;
    } else {
      this.chartGroup.error.tsSource = [];
    }
    this.chartGroup.error.loading = false;
  }
  // 请求数
  private async fetchRequestSource () {
    const serviceId = this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid));
    const _params: any = {
      serviceId,
      serviceInstance: this.current?.serviceInstance || decodeURIComponent(String(this.$route.query.si)),
      startTime: this.timeParams.fromTime,
      endTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
    }
    this.chartGroup.request.loading = true;
    // 需请求两个指标
    const successRst = await toAsyncWait(ServiceApi.getServiceRequestMetric({ ..._params, metric: 'succReqCount' }))
    const successData = successRst?.result?.data || []
    const failedRst = await toAsyncWait(ServiceApi.getServiceRequestMetric({ ..._params, metric: 'errReqCount' }))
    const failedData = failedRst?.result?.data || []
    if (!successData.length && !failedData.length) {
      this.chartGroup.request.source = []
    } else {
      this.chartGroup.request.source = [{
        name: i18n.t('modules.views.alarmCenter.notice.s_330363df') as string, nameKey: 'modules.utils.filters.s_330363df',
        type: 'bar',
        stack: 'total',
        color: '#2962FF',
        data: (successData[0]?.values as any[] || []).map(([timestamp, value]) => ({
          key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
          value,
        }))
      }, {
        name: i18n.t('modules.views.alarmCenter.notice.s_acd5cb84') as string, nameKey: 'modules.utils.filters.s_acd5cb84',
        type: 'bar',
        stack: 'total',
        color: '#F37370',
        data: (failedData[0]?.values as any[] || []).map(([timestamp, value]) => ({
          key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
          value,
        }))
      }]
    }
    // v2.9.1 ++
    if (successData?.length || failedData?.length) {
      // const rootDetails = new Array().concat((successData || []).map((i: any) => i?.rootDetails || []), (failedData || []).map((i: any) => i?.rootDetails || [])).flat()
      const avgRootDetails = (successData || []).map((i: any) => i?.rootDetails || []).filter((i: any) => i.length).flat();
      const reqRootDetails = (failedData || []).map((i: any) => i?.rootDetails || []).filter((i: any) => i.length).flat();
      const rootDetails =  avgRootDetails.length ? avgRootDetails : reqRootDetails.length ? reqRootDetails : [];
      const tsSource = rootDetails.filter((i: any) => i.abnormalStartTime && i.abnormalEndTime).map((i: any) => {
        const { abnormalStartTime, abnormalEndTime } = i;
        return {
          duration: [
            Number(abnormalStartTime),
            Number(abnormalEndTime)
          ],
          status: 'danger',
          info: {
            ...i
          }
        }
      });
      this.chartGroup.request.tsSource = tsSource;
    } else {
      this.chartGroup.request.tsSource = [];
    }
    this.chartGroup.request.loading = false;
  }

  private onTsTooltipShow (row: any) {
    this.currentTsItem = row.info || null;
  }

}
</script>

<style lang='scss' scoped>
.baseinfo-cont {
  overflow: hidden;
  position: relative;

}
.chart-group {
  display: flex;
  flex-wrap: wrap;
  overflow: hidden;

  .chart-item {
    flex: 1 0 auto;
    width: calc( 50% - 16px );
    height: 208px;
    overflow: hidden;
    margin: 8px 4px;
    padding: 35px 15px 15px;
    border: 1px solid var(--border-color-base);
    position: relative;

    &:nth-child(odd) {
      margin-right: 12px;
    }
  }
  .chart-title {
    position: absolute;
    top: 15px;
    left: 15px;
    line-height: 1;
    margin: 0;
  }
}
.attribute-group {
  margin-left: 4px;
}
.attribute-group-wrapper {
  border: 1px solid var(--border-color-base);
  border-radius: 4px;
  padding: 16px 20px;
}
.attribute-item {
  display: flex;

  .attribute-item-label {
    width: 140px;
    flex: none;
    margin-bottom: 12px;
  }
}
</style>
