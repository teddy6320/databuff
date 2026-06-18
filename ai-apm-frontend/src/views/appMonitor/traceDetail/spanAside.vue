<template>
  <!-- span详情 -->
  <div class="trace-span-detail flex-v ovh">
    <div v-if='currentSpan' class="span-detail-header mb-20 pr-16">
      <div class="span-header-main flex-h-jc ovh">
        <div class="flex-1 ovh ell">
          <span class="node-type-icon" :data-type='currentSpan.service_type'>
            <i :class='["db-icon font-16 mr-8", "db-icon-" + (currentSpan.type || currentSpan.service_type)]'></i>
          </span>
          <span class="font-14 fw-500">{{ currentSpan.resource }}</span>
        </div>
      </div>
      <div class="span-header-info">
        <span>{{ $t('modules.views.appMonitor.traceDetail.s_d8dc9309') }}<span>{{ currentSpan.duration | NsFilter }}</span></span>
        <span>{{ $t('modules.views.appMonitor.traceDetail.s_0f6f5c0f') }} <span>{{ durationPct | PercentFilter }}</span></span>
      </div>
      <div class="tl mt-5">
        <span
          v-if="currentSpan.hotspot && isDatabuffSource"
          @click.stop="viewHotMethodHandle(currentSpan.span_id)"
          class="db-blue cp font-12 fw-normal ml-8 flex-0">{{ $t('modules.views.appMonitor.serviceAnalysis.s_a3d9cb1f') }}</span>
      </div>
    </div>

    <db-tabnav v-if='detailOptions.length > 1' v-model='detailModel' :tabnavs='detailOptions' :slim='true'></db-tabnav>

    <div class="span-detail-main flex-1">
      <span-detail :type='detailModel' :row='currentSpan' :spanParents='spanParents' :totalDuration='totalDuration' :activeName='detailModel'></span-detail>
    </div>
  </div>
</template>

<script lang="ts">
import i18n from '@/i18n';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import SpanDetail from './spanDetail.vue'

@Component({
  components: {
    SpanDetail
  }
})
export default class SpanAside extends Vue {
  @Prop() private currentSpan!: any
  @Prop() private spanParents!: any[]
  @Prop() private totalDuration!: any
  @Prop() private totalExectime!: any


  @Watch('currentSpan', { immediate: true })
  private onCurrentSpan (newVal: any) {
    if (newVal) {
      if (!this.hasProfiling && this.detailModel === 'profiling') {
        this.detailModel = 'tags'
      }
    }
  }

  get excutePct () {
    const _excutePct = this.currentSpan?.exectime / this.totalExectime;
    return !isNaN(_excutePct) && isFinite(_excutePct) ? _excutePct : '-';
  }
  get durationPct () {
    const _durationPct = this.currentSpan?.duration / this.totalDuration;
    return !isNaN(_durationPct) && isFinite(_durationPct) ? _durationPct : '-';
  }

  get getSeriviceMapInfo () {
    return this.$store.state.Service.basicServiceMap?.[this.currentSpan?.serviceId] || {}
  }

  get isDatabuffSource () {
    const datasource = String(this.getSeriviceMapInfo?.datasource || '').toLowerCase();
    const virtual_service = this.getSeriviceMapInfo?.virtual_service;
    return (datasource === 'df-javaagent' || datasource === 'databuff') && !virtual_service;
  }

  // span详情
  public detailModel = 'tags'

  get detailOptions () {
    const tabs = [
      { label: i18n.t('modules.utils.static.s_24d67862') as string, labelKey: 'modules.utils.static.s_24d67862', value: 'tags' },
    ]
    if (this.hasProfiling && this.isDatabuffSource) {
      tabs.push({ label: i18n.t('modules.views.appMonitor.traceDetail.s_9687d0eb') as string, labelKey: 'modules.views.appMonitor.traceDetail.s_9687d0eb', value: 'profiling' })
    }
    return tabs
  }

  // 是否有性能剖析
  get hasProfiling () {
    return +this.currentSpan.onCpu > 0
  }


  // 跳转到Profiling
  private viewHotMethodHandle (id: string) {
    const data = this.currentSpan
    // 自定义时间范围参数，纳秒级
    const query: any = {
      sn: encodeURIComponent(data.service),
      sid: encodeURIComponent(data.serviceId),
      si: encodeURIComponent(data.serviceInstance || ''),
      resource: encodeURIComponent(data.resource),
      traceId: encodeURIComponent(data.trace_id),
      fromTimeNs: `${data._start}`,
      toTimeNs: `${data.end}`,
    }
    this.$router.push({
      path: '/appMonitor/hotMethods',
      query,
    })
  }

}
</script>

<style lang="scss" scoped>
.trace-span-detail {
  padding: 16px 0 0 16px;
  flex: 0 0 auto;
  height: 100%;
  border-left: 1px solid var(--border-color-light);
  border-top: 1px solid var(--border-color-light);

}
.span-detail-header {
  .span-header-main {
    line-height: 26px;
  }
  .span-header-info {
    font-size: 12px;
  }
}
.span-detail-main {
  overflow-y: auto;
}
</style>
