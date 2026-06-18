<template>
  <div class="event-aside-wrapper" :class="{ 'event-detail-wrapper': !(eventDetail || {}).id }">
    <div v-if="detail || isLoading" class="event-detail" v-loading="isLoading">
      <div class="event-detail-title">
        <i class="db-icon-log font-16 lh-20 mr-6 flex-none"></i>
        <text-expand
          @on-toggle="titleToggleHandle"
          :content="(detail || {}).message || '-'"
          :lineHeight="20"
          :maxLines="1"
          class="title-text" />
      </div>

      <db-tabnav
        v-model="activeName"
        :tabnavs="tabs.filter(item => !item.hide)"
        @on-change="toggleTabHandle"
        :thin="true"
        class="detail-tabs" />

      <component
        :ref="activeName"
        :is="activeName"
        :detail="detail || {}"
        class="detail-tabs-pane-wrap" />
    </div>
    <div v-else class="event-detail empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { isEqual, uniqBy } from 'lodash';
import deepClone from 'lodash/cloneDeep';
import { StringIsEmpty } from '@/utils/common';
import { getTimeRange } from '@/utils/timeFormat';
import TextExpand from '@/components/text-expand/index.vue';
import TabBaseinfo from './tab-baseinfo.vue';
import TabMetric from './tab-metric.vue';
import { toAsyncWait } from '@/utils/common';
import AlarmApi from '@/api/alarm';

@Component({
  components: {
    TextExpand,
    TabBaseinfo,
    TabMetric,
  }
})
export default class EventDetail extends Vue {
  @Prop({ default: () => ({}) }) private eventDetail!: any;

  public $refs!: {
    tabBaseinfo: TabBaseinfo
    tabMetric: TabMetric
  }

  private detail: any = null
  private isLoading = false

  @Watch('eventDetail.id', { immediate: true })
  private onEventDetailChange () {
    if (this.eventDetail?.id) {
      this.getData()
    } else {
      this.detail = null
    }
  }

  get isSystemEvent () {
    return this.$route.path === '/sysManage/eventDetail'
  }

  get eventId () {
    return this.eventDetail?.id || this.detail?.id || this.$route.query.eid;
  }

  private tabs = [
    { label: i18n.t('modules.views.alarmCenter.eventDetail.s_9e5ffa06') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_9e5ffa06', value: 'tabBaseinfo' },
    { label: i18n.t('modules.views.alarmCenter.eventDetail.s_87ae1624') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_87ae1624', value: 'tabMetric' },
  ]
  private activeName: string = 'tabMetric'

  private created () {
    const eType: any = this.$route.query.eType
    if (this.tabs.find(t => t.value === eType)) {
      this.activeName = eType
    }
  }

  private mounted () {
    if (this.eventId && !this.eventDetail?.id) {
      this.getData()
    }
  }

  public async getData () {
    if (!this.eventId) {
      return;
    }
    this.isLoading = true;
    await this.getDetail()
    this.isLoading = false;
    this.$nextTick(() => {
      const $refComp = (this.$refs as any)[this.activeName]
      if ($refComp && $refComp.getData) {
        $refComp.getData();
      }
    })
  }

  public resize () {
    this.titleToggleHandle();
  }

  private toggleTabHandle(tab: any) {
    const { value } = tab;
    this.activeName = value
    const eType = this.$route.query.eType
    if (value !== eType) {
      this.$router.replace({ query: { ...this.$route.query, eType: value } })
    }
    this.getData();
  }

  // 展开/收起标题
  private titleToggleHandle () {
    this.$nextTick(async () => {
      const $refComp = (this.$refs as any)[this.activeName]
      if ($refComp && $refComp.resize) {
        $refComp.resize();
      }
    })
  }

  private formatDetail (data: any) {
    // trigger 格式化为 {key: value}
    data.trigger = data.trigger || {}
    Object.keys(data.trigger).forEach(key => {
      data.trigger[key] = key ? Array.isArray(data.trigger[key]) ? data.trigger[key][0] : data.trigger[key] : ''
    })
    // tags 格式化为 {key: [value1, value2]}
    data.tags = data.tags || {}
    Object.keys(data.tags).forEach(key => {
      if (!Array.isArray(data.tags[key])) {
        data.tags[key] = !StringIsEmpty(data.tags[key]) ? [data.tags[key]] : []
      } else {
        data.tags[key] = data.tags[key].filter((val: string) => !StringIsEmpty(val))
      }
    })
    data.classification = data.classification || (data.tags.classification || []).join(', ') || ''
    data.startTriggerTime = data.startTriggerTime || data.triggerTime

    // 计算图表的开始时间和结束时间
    const _timeRange = getTimeRange(data.startTriggerTime, data.triggerTime, 15 * 60 * 1000)
    data.chartTime = {
      start: _timeRange.start,
      end: _timeRange.end + 60 * 1000,
      interval: _timeRange.interval,
    }

    if (data.query) {
      const _query = data.query || {}
      const getMetrics = (q: any): any[] => {
        const metric = q.A?.metric
        return metric ? [{ metric, isCustom: false }] : []
      }
      if (data.classification === 'singleMetric' && data.query) {
        let list: any[] = []
        if (data.query[1]) {
          list = getMetrics(data.query[1])
        }
        data.metricsFormat = uniqBy(list, isEqual)
      } else if (data.query) {
        data.metricsFormat = uniqBy(getMetrics(_query), isEqual)
      }
    } else {
      data.metricsFormat = uniqBy((data.metrics || []).map((t: string) => ({
        metric: t,
        isCustom: false,
      })), isEqual)
    }

    return data
  }

  // 获取详情
  private async getDetail () {
    const detailPath = this.isSystemEvent ? 'getSystemEventDetail' : 'getEventDetailV2'
    const { error, result } = await toAsyncWait(AlarmApi[detailPath]({ eventId: this.eventId }))
    const data = (result || {}).data
    if (!error && data?.id) {
      this.detail = this.formatDetail(deepClone(data))
    } else {
      this.detail = {}
      this.$confirm(i18n.t('modules.views.alarmCenter.eventDetail.s_8dc09ebe') as string + (this.isSystemEvent ? i18n.t('modules.views.alarmCenter.eventDetail.s_8a8b895f') as string : '') + i18n.t('modules.views.alarmCenter.eventDetail.s_de842dfc') as string, i18n.t('common.hint') as string, {
        confirmButtonText: i18n.t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') as string, confirmButtonTextKey: 'modules.views.alarmCenter.alarmDetail.s_38cf16f2',
        closeOnClickModal: false,
        showCancelButton: false,
        showClose: false,
        type: 'warning'
      }).then(() => {
        this.$router.replace({
          path: this.isSystemEvent ? '/sysManage/systemEvent' : '/alarmCenter/alarm',
          query: { ...this.getRouteTimeOrRange }
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.event-aside-wrapper {
  height: 100%;
  &.event-detail-wrapper {
    flex: 1;
    padding: 16px;
    font-size: 13px;
    color: var(--color-text-primary);
    overflow: auto;
  }
}

.event-detail {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-color);
  border-radius: 4px;
  &.empty {
    justify-content: center;
    align-items: center;
    font-size: 13px;
    color: var(--color-text-secondary);
  }

  .event-detail-title {
    flex: none;
    padding: 18px 20px;
    display: flex;
    .title-text {
      font-size: 16px;
      line-height: 20px;
      font-weight: 500;
    }
  }

  .detail-tabs {
    flex: none;
    padding: 0 20px;
  }

  .detail-tabs-pane-wrap {
    flex: 1;
    padding: 20px;
    overflow: hidden;
  }
}
</style>
