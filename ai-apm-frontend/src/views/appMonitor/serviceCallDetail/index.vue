<template>
  <div class="call-detail-wrapper">
    <div class="call-cont-header">
      <code-view :code="info.resource" class="mb-10" />
      <div class="info-t">{{ $t('modules.views.appMonitor.serviceCallDetail.s_b990de66') }}<span>{{ info._send || '-' }}</span></div>
      <div class="info-t">{{ $t('modules.views.appMonitor.serviceCallDetail.s_cc0a5732') }}<span>{{ info._receive || '-' }}</span></div>
    </div>

    <chart-group
      ref="chartGroup"
      :timeParams="timeParams"
      :query="params"
      :showBody="false"
      :aiDisabled='true'
      @chart-click="chartClickHandle" />

    <div v-if="showList || isEbpf" class="query-list-cont">
      <table-list
        v-if="!isEbpf"
        ref="tableList"
        :componentType="info.componentType"
        :query="params"
        :timeParams="queryParams"
        class="list"
      />
      <ebpf-table-list
        v-else
        ref="tableList"
        :query="params"
        :timeParams="timeParams"
        class="list"
      />
    </div>
    <div v-else class="query-list-cont hide-list">{{ $t('modules.views.appMonitor.serviceCallDetail.s_16853987') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Getter } from 'vuex-class';
import dayjs from 'dayjs';
import CodeView from '@/components/code-view.vue';
import ChartGroup from '../serviceCall/chart-group.vue';
import TableList from './table-list.vue';
import EbpfTableList from './ebpf-table-list.vue';

@Component({
  components: {
    CodeView,
    ChartGroup,
    TableList,
    EbpfTableList,
  }
})
export default class ServiceCallDetail extends Vue {
  @Getter('globalTime') private globalTimeFunc!: any;
  @Getter('globalTimeInited') private globalTimeInited!: boolean;

  public $refs!: {
    chartGroup: ChartGroup
    tableList: TableList | EbpfTableList
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

  get serviceIdNameMapping () {
    const mapping: any = {}
    const basicServiceMap = this.$store.getters['Service/basicServiceMap']
    Object.keys(basicServiceMap).forEach((t: string) => {
      mapping[t] = basicServiceMap[t].name
    });
    return mapping
  }

  get isEbpf () {
    const { serviceId, srcServiceId } = this.info
    const basicServiceMap = this.$store.getters['Service/basicServiceMap']
    const serviceEbpf = basicServiceMap[serviceId]?.datasource === 'DF-ebpf';
    const srcServiceEbpf = basicServiceMap[srcServiceId]?.datasource === 'DF-ebpf';
    return serviceEbpf || srcServiceEbpf;
  }

  private info: any = {} // 发起端、接收端的详细信息

  private timeParams = {
    fromTime: '',
    toTime: '',
    interval: 3600,
  }

  private showList = false; // 是否显示列表区域，默认不显示
  private params: any = {} // 默认筛选参数
  private queryParams: any = {}

  private created () {
    const { resource, componentType, sid, st, srcSid, srcSt, serviceInstance, srcServiceInstance, rootResourceQuery, params, srcSn, sn } = this.$route.query as any
    // 设置面包屑
    this.$nextTick(() => {
      const _query: any = { componentType, sid, st, srcSid, srcSt, }
      if (serviceInstance) {
        _query.serviceInstance = serviceInstance
      }
      if (srcServiceInstance) {
        _query.srcServiceInstance = srcServiceInstance
      }
      if (rootResourceQuery) {
        _query.rootResourceQuery = rootResourceQuery
      }
      this.$store.commit('UPDATE_BREADCRUMB', [{
        path: '/appMonitor/serviceCall',
        query: _query,
      }]);
    })

    const _resource = decodeURIComponent(resource || '')
    const _serviceId = decodeURIComponent(sid || '')
    const _serviceName = decodeURIComponent(sn || '') || this.serviceIdNameMapping[_serviceId] || ''
    const _serviceInstance = decodeURIComponent(serviceInstance || '')
    const _srcServiceId = decodeURIComponent(srcSid || '')
    const _srcServiceName = this.serviceIdNameMapping[_srcServiceId] || decodeURIComponent(srcSn) || ''
    const _srcServiceInstance = decodeURIComponent(srcServiceInstance || '')
    const _rootResourceQuery = decodeURIComponent(rootResourceQuery || '')
    this.info = {
      resource: _resource,
      componentType,
      // 发出
      srcServiceName: _srcServiceName,
      srcServiceType: srcSt,
      srcServiceId: _srcServiceId,
      srcServiceInstance: _srcServiceInstance,
      _send: !_srcServiceInstance ? _srcServiceName : (i18n.t('modules.views.appMonitor.serviceCallDetail.s_f52ab8c1', { value0: _srcServiceInstance, value1: _srcServiceName }) as string),
      // 接收
      serviceName: _serviceName,
      serviceType: st,
      serviceId: _serviceId,
      serviceInstance: _serviceInstance,
      _receive: !_serviceInstance ? _serviceName : (i18n.t('modules.views.appMonitor.serviceCallDetail.s_f52ab8c1', { value0: _serviceInstance, value1: _serviceName }) as string),
    }
    this.params = {
      resource: _resource,
      componentType,
      serviceId: decodeURIComponent(sid as string),
      srcServiceId: decodeURIComponent(srcSid as string),
      ...JSON.parse(decodeURIComponent(params || '{}')),
    }
    if (_serviceInstance) {
      this.params.serviceInstance = _serviceInstance
    }
    if (_srcServiceInstance) {
      this.params.srcServiceInstance = _srcServiceInstance
    }
    if (_rootResourceQuery) {
      this.params.rootResourceQuery = _rootResourceQuery
    }
  }

  private async mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
    this.durationChangeHandle()
  }

  private beforeDestroy () {
    // 清空面包屑
    this.$store.commit('CLEAR_BREADCRUMB');
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh');
  }

  private durationChangeHandle () {
    this.showList = false;
    this.regetGlobalTime()
    this.$refs.chartGroup && this.$refs.chartGroup.getData()
    if (this.isEbpf) {
      this.$refs.tableList && this.$refs.tableList.getData()
    }
  }

  private regetGlobalTime () {
    const { fromTime, toTime, interval } = this.globalTimeFunc()
    this.timeParams.fromTime = dayjs(fromTime).format('YYYY-MM-DD HH:mm:ss')
    this.timeParams.toTime = dayjs(toTime).format('YYYY-MM-DD HH:mm:ss')
    this.timeParams.interval = interval
  }

  // 图表点击事件回调
  private chartClickHandle (xAxisName: string) {
    if (this.isEbpf) {
      return;
    }
    this.showList = true;
    const { toTime, interval } = this.timeParams
    this.queryParams.fromTime = xAxisName + ':00'
    const _toTime = +new Date(xAxisName) + interval * 1000
    if (_toTime <= +new Date(toTime)) {
      this.queryParams.toTime = dayjs(_toTime).format('YYYY-MM-DD HH:mm:ss')
    } else {
      this.queryParams.toTime = toTime
    }
    this.$nextTick(() => {
      this.$refs.tableList && this.$refs.tableList.getData()
    })
  }
}
</script>

<style lang="scss" scoped>
.call-detail-wrapper{
  flex: 1;
  height: 100%;
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: auto;

  .call-cont-header{
    padding: 16px;
    background-color: var(--bg-color);

    .info-t {
      margin-top: 5px;
      display: flex;
      align-items: center;
      color: var(--color-text-regular);
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      span {
        color: var(--color-text-primary);
      }
    }
  }

  .query-list-cont {
    flex: 1;
    margin-top: 16px;
    min-height: 460px;
    display: flex;
    overflow: hidden;

    &.hide-list {
      align-items: center;
      justify-content: center;
      background-color: var(--bg-color);
      font-size: 14px;
      color: var(--color-text-regular);
    }
  }

  .list {
    flex: 1;
  }
}
</style>
