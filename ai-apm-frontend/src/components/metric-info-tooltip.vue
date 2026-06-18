<template>
  <div class="metric-info" :class="{ tooltip }">
    <div class="mt-info-item">
      <span class="mt-info-item-title">{{ $t('modules.components.s_2247bf68') }}</span>
      <span class="mt-info-item-value">{{ detail.metric || detail.identifier || '-' }}</span>
    </div>
    <div class="mt-info-item">
      <span class="mt-info-item-title">{{ $t('modules.components.s_6c5f7a12') }}</span>
      <span class="mt-info-item-value">{{ detail.metricCn || '-' }}</span>
    </div>
    <div class="mt-info-item">
      <span class="mt-info-item-title">{{ $t('modules.components.s_5ec7d388') }}</span>
      <span class="mt-info-item-value">{{ detail.describeCn || detail.desc || '-' }}</span>
    </div>
    <div class="mt-info-item">
      <span class="mt-info-item-title">{{ $t('modules.components.s_cc8bc220') }}</span>
      <span class="mt-info-item-value">{{ typesStr || '-' }}</span>
    </div>
    <div class="mt-info-item">
      <span class="mt-info-item-title">{{ $t('modules.components.s_84d6a988') }}</span>
      <span class="mt-info-item-value">{{ detail.isOpen ? $t('modules.components.s_0a60ac8f') : $t('modules.components.s_c9744f45')  }}</span>
    </div>
    <div class="mt-info-item">
      <span class="mt-info-item-title">{{ $t('modules.components.s_26f1c5e9') }}</span>
      <span class="mt-info-item-value">{{ detail.unitCn || detail.unit || '-' }}</span>
    </div>
    <!-- <div class="mt-info-item">
      <span class="mt-info-item-title">{{ $t('modules.components.s_43635833') }}</span>
      <span class="mt-info-item-value">{{ detail.type || '-' }}</span>
    </div>
    <div class="mt-info-item">
      <span class="mt-info-item-title">{{ $t('modules.components.s_af9083c5') }}</span>
      <span class="mt-info-item-value">{{ detail.interval || '-' }}</span>
    </div>
    <div class="mt-info-item">
      <span class="mt-info-item-title">{{ $t('modules.components.s_20fa7575') }}</span>
      <span class="mt-info-item-value">{{ detail.comment || '-' }}</span>
    </div>
    <div class="mt-info-item">
      <span class="mt-info-item-title">{{ $t('modules.components.s_306c1567') }}</span>
      <span class="mt-info-item-value">{{ detail.judgment || '-' }}</span>
    </div> -->
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';

@Component
export default class MetricInfoTooltip extends Vue {
  @Prop({ default: () => ({}) }) private detail!: any;
  @Prop({ default: true }) private tooltip!: boolean;

  get typesStr () {
    const detail = this.detail || {};
    const _types: string[] = detail._types || [];
    if (!_types.length) {
      detail.type1 && _types.push(detail.type1)
      detail.type1 && detail.type2 && _types.push(detail.type2)
      detail.type1 && detail.type2 && detail.type3 && _types.push(detail.type3)
    }
    return _types.join('/');
  }
}
</script>

<style lang="scss" scoped>
.metric-info {
  width: 100%;
  padding: 12px;
  border-radius: 4px;
  overflow: hidden;

  &.tooltip {
    display: none;
    position: fixed;
    width: 400px;
    background-color: var(--bg-color);
    border: solid 1px var(--border-color-base);
    box-shadow: 0 2px 12px rgba(0, 0, 0, .1);
    z-index: 110;
  }

  .mt-info-item {
    line-height: 20px;
    display: flex;
    align-items: flex-start;

    & + .mt-info-item {
      margin-top: 6px;
    }

    .mt-info-item-title {
      box-sizing: border-box;
      padding-right: 10px;
      min-width: 65px;
      font-size: 12px;
      color: var(--color-text-regular);
    }
    .mt-info-item-value {
      flex: 1;
      font-size: 12px;
      word-break: break-all;
      white-space: normal;
      color: var(--color-text-primary);
    }
    .metric-overview-label {
      font-size: 12px;
      max-width: 150px;
      background-color: var(--bg-color03);
      border-radius: 3px;
      height: 26px;
      line-height: 26px;
      padding: 0 10px;
      & > i {
        font-style: normal;
      }
    }
  }
}
</style>

<style lang="scss">
.el-popover.metric-info-popper {
  padding: 0;
}
</style>
