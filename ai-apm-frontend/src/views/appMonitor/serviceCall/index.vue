<template>
  <div class="service-call-wrap"
    v-loading="viewLoading">
    <db-tabnav
      v-model="viewType"
      :tabnavs="viewTypeList"
      @on-change="toggleTabHandle"
      class="service-call-tabnav" />

    <request-view
      v-if="viewGroup === 'request'"
      ref="requestView"
      :params="params"
      :timeParams="timeParams"
      class="service-call-content" />

    <pool-view
      v-else-if="viewGroup === 'pool'"
      ref="poolView"
      :poolType="viewType"
      :poolList="poolNamesMap[viewType] || []"
      :params="params"
      :timeParams="timeParams"
      :poolMetrics="poolMetrics"
      :metricInfoMapping="metricInfoMapping"
      @metrics-loaded="metricsLoadedHandle"
      class="service-call-content" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Getter } from 'vuex-class';
import dayjs from 'dayjs';
import RequestView from './request-view.vue';
import PoolView from './pool-view.vue';
import { toAsyncWait } from '@/utils/common';
import ServiceApi from '@/api/service';

const RequestTypeMapping: any = {
  'service.http': i18n.t('modules.views.appMonitor.resourceDetail.s_669262cd') as string,
  'service.rpc': i18n.t('modules.views.appMonitor.resourceDetail.s_4111408b') as string,
  'service.mq': i18n.t('modules.views.appMonitor.resourceDetail.s_82375db4') as string,
  'service.db': i18n.t('modules.views.appMonitor.resourceDetail.s_b3bf0a2d') as string,
  'service.redis': i18n.t('modules.views.appMonitor.resourceDetail.s_218e2ad9') as string,
  'service.config': i18n.t('modules.views.appMonitor.resourceDetail.s_88bdaf32') as string,
  'service.remote': i18n.t('modules.views.appMonitor.resourceDetail.s_71f31c96') as string,
  'service.other': i18n.t('modules.views.appMonitor.resourceDetail.s_b5667e29') as string,
  'service.browser': i18n.t('modules.views.appMonitor.serviceCall.s_ef367e82') as string,
  'service.ios': 'IOS',
  'service.android': 'Android',
}

@Component({
  components: {
    RequestView,
    PoolView,
  }
})
export default class ServiceCall extends Vue {
  @Getter('globalTime') private globalTimeFunc!: any;
  @Getter('globalTimeInited') private globalTimeInited!: boolean;

  public $refs!: {
    requestView: RequestView
    poolView: PoolView
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

  private viewLoading = false;
  private viewType = ''
  private viewTypeList = [{ label: i18n.t('modules.views.appMonitor.serviceCall.s_9fd2acba') as string, labelKey: 'modules.views.appMonitor.serviceCall.s_9fd2acba', value: 'request', group: 'request' }]
  get viewGroup () {
    const item = this.viewTypeList.find(t => t.value === this.viewType)
    return item ? item.group : ''
  }

  private poolNamesMap: any = {}

  private timeParams = {
    fromTime: '',
    toTime: '',
    interval: 3600,
  }

  private params: any = {} // 默认筛选参数

  private created () {
    const { componentType, sid, srcSid } = this.$route.query
    const _sid = decodeURIComponent(String(sid || ''))
    const _sn = this.serviceIdNameMapping[_sid] || ''
    const _srcSid = decodeURIComponent(String(srcSid || ''))
    const _srcSn = this.serviceIdNameMapping[_srcSid] || ''
    // this.$nextTick(() => {
    //   const _componentTypeName = RequestTypeMapping[componentType as string] || componentType || ''
    //   this.$store.commit('UPDATE_BREADCRUMB', [{
    //     name: i18n.t('modules.views.appMonitor.serviceCall.s_f99ce16b', { value0: _componentTypeName, value1: _srcSn, value2: _sn }) as string,
    //     path: '/appMonitor/serviceCall',
    //   }]);
    // });
    if (RequestTypeMapping[componentType as string]) {
      const requestItem = this.viewTypeList.find(t => t.value === 'request')
      requestItem && (requestItem.label = RequestTypeMapping[componentType as string])
    }
    this.params = {
      componentType,
      serviceId: _sid,
      srcServiceId: _srcSid,
    }
    this.durationChangeHandle()
  }

  private mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
  }

  private beforeDestroy () {
    // 清空面包屑
    this.$store.commit('CLEAR_BREADCRUMB');
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh');
  }

  private async durationChangeHandle () {
    this.regetGlobalTime()

    await this.getViewTypeList();

    // 设置viewType
    const viewType = this.$route.query.viewType as string
    if (this.viewTypeList.find(t => t.value === viewType)) {
      this.viewType = viewType
    } else {
      this.viewType = (this.viewTypeList[0] || {}).value || ''
      const query = {
        ...this.$route.query,
        viewType: this.viewType,
      }
      this.$router.replace({ query })
    }
    this.getViewDate()
  }

  private regetGlobalTime () {
    const { fromTime, toTime, interval } = this.globalTimeFunc()
    this.timeParams.fromTime = dayjs(fromTime).format('YYYY-MM-DD HH:mm:ss')
    this.timeParams.toTime = dayjs(toTime).format('YYYY-MM-DD HH:mm:ss')
    this.timeParams.interval = interval
  }

  private async getViewTypeList () {
    const { fromTime, toTime } = this.timeParams
    this.viewLoading = true;
    const { result, error } = await toAsyncWait(ServiceApi.getServiceCallPools({
      ...this.params,
      fromTime,
      toTime,
    }))
    const requestItem: any = this.viewTypeList.find(t => t.value === 'request');
    this.viewLoading = false;
    if (!error) {
      const poolName = {
        'service.object.pool.get': ['object', i18n.t('modules.views.appMonitor.serviceCall.s_6c4bc2a1') as string],
        'service.http.connection.pool.get': ['httpConn', i18n.t('modules.views.appMonitor.serviceCall.s_d83f0fed') as string],
        'service.db.connection.pool.get': ['dbConn', i18n.t('modules.views.appMonitor.serviceCall.s_8ffb0339') as string],
      }
      const data = result.data || {}
      const poolList: any[] = []
      const poolNamesMap: any = {}
      Object.entries(poolName).forEach(([key, t]) => {
        if (data[key]) {
          poolList.push({ label: t[1], value: t[0], group: 'pool' })
          poolNamesMap[t[0]] = data[key]
        }
      });
      this.viewTypeList = [requestItem, ...poolList];
      this.poolNamesMap = poolNamesMap;
    } else {
      this.viewTypeList = [requestItem];
    }
  }

  private getViewDate () {
    // v-if 切换后子组件 ref 需要多等一帧才能挂载完成
    this.$nextTick(() => {
      this.$nextTick(() => {
        if (this.viewGroup === 'request' && this.$refs.requestView) {
          this.$refs.requestView.getData()
        } else if (this.viewGroup === 'pool' && this.$refs.poolView) {
          this.$refs.poolView.getData()
        }
      })
    })
  }

  private toggleTabHandle(tab: any) {
    const { value } = tab;
    this.viewType = value
    const query = {
      ...this.$route.query,
      viewType: value,
    }
    this.$router.replace({ query })
    this.getViewDate()
  }

  private poolMetrics: any = {}
  private metricInfoMapping: any = {}
  private metricsLoadedHandle (data: any, key: string) {
    this.metricInfoMapping = { ...this.metricInfoMapping, ...data };
    this.$set(this.poolMetrics, key, Object.keys(data));
  }
}
</script>

<style lang="scss" scoped>
.service-call-wrap {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;
}

.service-call-tabnav {
  height: 44px;
}

.service-call-content {
  margin-top: 16px;
  flex: 1;
}
</style>
