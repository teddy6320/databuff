<template>
  <div class="hot-methods-wrapper">
    <el-alert
      v-if="spanTimeRange"
      @close="clearSpanTimeRange"
      :title="$t('modules.views.appMonitor.hotMethods.s_aed294b1', { value0: spanTimeRange.startStr, value1: spanTimeRange.endStr })"
      type="info"
      class="mb-10 flex-none font-12 span-time-range" />

    <el-alert
      v-if="isOverOneHour"
      :title="$t('modules.views.appMonitor.hotMethods.s_7f725a8d', { value0: timeParams.fromTime, value1: timeParams.toTime })"
      :closable="false"
      type="warning"
      class="mb-10 flex-none font-12" />

    <div class="flex-h">
      <search-group
        ref="searchGroup"
        :timeParams="timeParams"
        @on-change="searchChangeHandle"
        @on-types-loaded="typesLoadedHandle"
        class="search-group"
      />

      <db-radio
        v-if="graphTypeList.length"
        v-model="graphTypeModel"
        :options="graphTypeList"
        @change="getProfileSource"
        class="ml-16" />
    </div>

    <profile-graph
      ref="profileGraph"
      :source="profileSource"
      :loading="profileLoading"
      v-loading="profileLoading"
      class="profile-graph" />

  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Getter } from 'vuex-class';
import dayjs from 'dayjs';
import SearchGroup from './search-group.vue';
import ProfileGraph from './flame-graph.vue';
import { calcInterval } from '@/utils/timeFormat'
import { toAsyncWait } from '@/utils/common';
import ServiceApi from '@/api/service';

@Component({
  components: {
    SearchGroup,
    ProfileGraph,
  }
})
export default class HotMethods extends Vue {
  @Getter('globalTime') private globalTimeFunc!: any;
  @Getter('globalTimeInited') private globalTimeInited!: boolean;

  public $refs!: {
    searchGroup: SearchGroup
    profileGraph: ProfileGraph
  }

  get globalTime () {
    return this.globalTimeFunc()
  }
  @Watch('globalTime', { deep: true })
  private watchGlobalTime() {
    if (!this.globalTimeInited) {
      return
    }
    this.clearSpanTimeRange()
  }

  get serviceIdNameMapping () {
    const mapping: any = {}
    const basicServiceMap = this.$store.getters['Service/basicServiceMap']
    Object.keys(basicServiceMap).forEach((t: string) => {
      mapping[t] = basicServiceMap[t].name
    });
    return mapping
  }

  private isOverOneHour: boolean = false // 时间范围是否超过1小时
  private spanTimeRange: any = null // 链路下钻的span时间范围
  private timeParams = {
    fromTime: '',
    toTime: '',
    fromTimeNs: '',
    toTimeNs: '',
    interval: 3600,
  }

  private queryParams: any = {}

  private graphTypeModel = 'cpu'
  private allGraphTypeList: any[] = [
    { label: 'On CPU', value: 'cpu', disabled: true },
    { label: 'Off CPU', value: 'wall', disabled: true },
    { label: i18n.t('modules.views.appMonitor.hotMethods.s_9932551c') as string, labelKey: 'modules.views.appMonitor.hotMethods.s_9932551c', value: 'alloc', disabled: true },
    { label: i18n.t('modules.views.appMonitor.hotMethods.s_c376dfb0') as string, labelKey: 'modules.views.appMonitor.hotMethods.s_c376dfb0', value: 'lock', disabled: true },
  ]
  get graphTypeList () {
    return this.allGraphTypeList.filter(item => !item.disabled)
  }

  private profileSource: any[] = []
  private profileLoading = false

  private created() {
    const { sid } = this.$route.query;
    this.queryParams.serviceId = decodeURIComponent(sid as string || '')
    // 设置面包屑
    this.$nextTick(() => {
      const _sn = this.serviceIdNameMapping[this.queryParams.serviceId] || ''
      this.$store.commit('UPDATE_BREADCRUMB', [{
        name: i18n.t('modules.views.appMonitor.hotMethods.s_72a673f1', { value0: _sn }) as string,
        path: '/appMonitor/hotMethods',
      }]);
    })

    const { fromTimeNs, toTimeNs } = this.$route.query
    if (fromTimeNs && toTimeNs) {
      let start = +(`${fromTimeNs}`).substring(0, 13)
      const end = +(`${toTimeNs}`).substring(0, 13)
      this.spanTimeRange = {
        startStr: dayjs(start).format('YYYY-MM-DD HH:mm:ss.SSS'),
        endStr: dayjs(end).format('YYYY-MM-DD HH:mm:ss.SSS'),
      }
      this.isOverOneHour = end - start > 3600 * 1000
      if (this.isOverOneHour) {
        start = end - 3600 * 1000
      }
      this.timeParams = {
        fromTime: dayjs(start).format('YYYY-MM-DD HH:mm:ss'),
        toTime: dayjs(end).format('YYYY-MM-DD HH:mm:ss'),
        fromTimeNs: `${fromTimeNs}`,
        toTimeNs: `${toTimeNs}`,
        interval: calcInterval(start, end),
      }
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
    if (!this.spanTimeRange) {
      this.regetGlobalTime()
    }
    if (!this.queryParams.serviceId) {
      return
    }
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        if (data.si) {
          data.serviceInstance = data.si
          delete data.si
        }
        this.queryParams = { ...data, serviceId: this.queryParams.serviceId }
        this.getProfileSource();
      })
    })
  }

  private regetGlobalTime () {
    const { fromTime, toTime, interval } = this.globalTimeFunc()
    let start = +fromTime
    const end = +toTime
    this.isOverOneHour = end - start > 3600 * 1000
    if (this.isOverOneHour) {
      start = end - 3600 * 1000
    }
    this.timeParams = {
      fromTime: dayjs(start).format('YYYY-MM-DD HH:mm:ss'),
      toTime: dayjs(end).format('YYYY-MM-DD HH:mm:ss'),
      fromTimeNs: `${start * 1000 * 1000}`,
      toTimeNs: `${end * 1000 * 1000}`,
      interval: calcInterval(start, end),
    }
  }

  private searchChangeHandle (data: any) {
    if (data.si) {
      data.serviceInstance = data.si
      delete data.si
    }
    data.serviceId = this.queryParams.serviceId
    if (JSON.stringify(data) === JSON.stringify(this.queryParams)) {
      return
    }
    this.queryParams = { ...data }
    if (!this.queryParams.serviceId) {
      return
    }
    this.getProfileSource();
  }

  private clearSpanTimeRange () {
    this.spanTimeRange = null
    const _query = { ...this.$route.query }
    if (_query.fromTimeNs && _query.toTimeNs) {
      delete _query.fromTimeNs
      delete _query.toTimeNs
      this.$router.replace({ query: { ..._query } })
    }
    this.durationChangeHandle()
  }

  private typesLoadedHandle (list: string[]) {
    this.allGraphTypeList.forEach(item => {
      item.disabled = !list.includes(item.value)
    });
    this.graphTypeModel = (this.graphTypeList[0] || {}).value || ''
  }

  private async getProfileSource () {
    const { fromTimeNs, toTimeNs } = this.timeParams
    const params: any = {
      ...this.queryParams,
      fromTimeNs,
      toTimeNs,
      eventType: this.graphTypeModel,
    }
    Object.entries(params).forEach(([key, value]) => {
      if (value === '') {
        delete params[key]
      }
    })
    this.profileLoading = true;
    const { result, error } = await toAsyncWait(ServiceApi.getProfilingFlame(params));
    this.profileLoading = false;
    if (!error) {
      const data: any[] = (result || {}).data || [];
      this.profileSource = data.map(t => ([
        t.y || 0,
        t.x || 0,
        t.width || 0,
        t.type || 0,
        t.title || '',
        { samples: t.samples || t.width || 0, }
      ]));
    }
  }
}
</script>

<style lang="scss" scoped>
.hot-methods-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .flex-none {
    flex: none;
  }

  .span-time-range {
    background-color: var(--bg-color);
    color: var(--color-text-regular);
  }

  .profile-graph {
    margin-top: 16px;
    flex: 1;
    min-height: 400px;
    overflow: hidden;
  }
}
</style>
