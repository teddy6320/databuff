<template>
  <div class="span-profiling-wrapper">
    <div class="">
      <div v-if="metrics['invoke.line.number']" class="info-item mb-20">
        <div class="label">{{ $t('modules.views.appMonitor.traceDetail.s_7cff89f6') }}</div>
        <div class="value">{{ metrics['invoke.line.number'] }}</div>
      </div>

      <h4 class="info-title">{{ $t('modules.views.appMonitor.traceDetail.s_b8dbfb58') }}</h4>
      <div class="info-item">
        <div class="label">{{ $t('modules.views.appMonitor.traceDetail.s_61e84eb5') }}</div>
        <div class="value">{{ detail.startTime | TimesToDateFilter('YYYY-MM-DD HH:mm:ss.SSS') }}</div>
      </div>
      <div class="info-item">
        <div class="label">{{ $t('modules.views.appMonitor.traceDetail.s_645adff0') }}</div>
        <div class="value">{{ detail.duration | NsFilter }}</div>
      </div>
      <div class="info-item">
        <div class="label">{{ $t('modules.views.appMonitor.traceDetail.s_63256a8a') }}</div>
        <div class="value">{{ detail.exectime | NsFilter }}</div>
      </div>
      <div v-if="metrics['gap.cpu.time.cost.ns']" class="info-item">
        <div class="label">{{ $t('modules.views.appMonitor.traceDetail.s_aecf151e') }}</div>
        <div class="value">{{ metrics['gap.cpu.time.cost.ns'] | NsFilter }}</div>
      </div>

      <div class="info-item consuming-detail"><div class="label">{{ $t('modules.views.appMonitor.traceDetail.s_1511afcd') }}</div></div>
      <el-progress
        :percentage="cpuPercent"
        color="#C459F7"
        :show-text="false"
        :stroke-width="8"
        class="info-item process-item"></el-progress>

      <div class="info-item attr-item">
        <span class="label on-label">On CPU</span>
        <span class="value">{{ detail.onCpu | NsFilter }}</span>
      </div>
      <div class="info-item attr-item">
        <span class="label off-label">OFF CPU</span>
        <span class="value">{{ offCpu | NsFilter }}</span>
      </div>
    </div>

    <div class="mt-16">
      <h4 class="info-title">{{ $t('modules.views.appMonitor.traceDetail.s_b379f248') }}</h4>
      <div class="info-item">
        <div class="label">{{ $t('modules.views.appMonitor.traceDetail.s_dbe3cb04') }}</div>
        <div class="value">{{ metrics['memory.alloc.byte'] | BytesFilter }}</div>
      </div>
      <div v-if="metrics['gap.memory.alloc.byte']" class="info-item">
        <div class="label">{{ $t('modules.views.appMonitor.traceDetail.s_a6bf6629') }}</div>
        <div class="value">{{ metrics['gap.memory.alloc.byte'] | BytesFilter }}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';

@Component
export default class SpanProfiling extends Vue {
  @Prop({ default: () => ({}) }) private detail!: any;

  get metrics () {
    return (this.detail || {}).metrics || {}
  }

  get offCpu () {
    const detail = this.detail || {}
    return Math.max(detail.exectime - detail.onCpu, 0)
  }

  get cpuPercent () {
    const detail = this.detail || {}
    if (detail.onCpu) {
      return (detail.onCpu / (detail.onCpu + this.offCpu)) * 100
    }
    return 0
  }
}
</script>

<style lang="scss" scoped>
.span-profiling-wrapper {
  padding: 16px 16px 0 0;

  .info-title{
    margin: 0;
    margin-bottom: 16px;
    font-size: 13px;
    font-weight: 500;

    &:not(:first-of-type){
      margin-top: 20px;
    }
  }

  .info-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 12px;
    overflow: hidden;
    & + .info-item {
      margin-top: 10px;
    }

    .label {
      flex: none;
      color: var(--color-text-regular);
    }
    .value {
      flex: 1;
      word-break: break-word;
    }
  }

  .info-item.consuming-detail {
    margin-top: 20px;
  }

  .process-item {
    :deep(.el-progress-bar__outer){
      border-radius: 0;
      background-color: #6E93FA;
      .el-progress-bar__inner{
        border-radius: 0;
        transition: none;
      }
    }
  }

  .attr-item {
    .label {
      min-width: 160px;
      flex: 1;
      display: flex;
      align-items: center;
      position: relative;
      white-space: nowrap;
      &::after {
        content: '';
        flex: 1;
        margin: 0 10px;
        border-bottom: 1px currentColor dashed;
        transform: scale(1, 0.5);
      }
    }
    .value {
      flex: none;
      // min-width: 120px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
    }
    .on-label,
    .off-label {
      padding-left: 18px;
      &::before {
        content: '';
        width: 8px;
        height: 8px;
        background-color: #6E93FA;
        transform: translate(0, -50%);
        position: absolute;
        top: 50%;
        left: 0;
      }
    }
    .on-label::before {
      background-color: #C459F7;
    }
  }
}
</style>
