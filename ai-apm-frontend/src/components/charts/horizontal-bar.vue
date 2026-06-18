<template>
  <div class="horizontal-bar-wrap">
    <div
      v-for="(item, index) in chartSource"
      :key="index"
      :style="`margin-top:${itemGap}px`"
      :class="{ 'top-layout': valuePosition === 'top' }"
      class="horizontal-bar-item">
      <div class="item-title ell">
        {{ item.nameKey ? $t(item.nameKey) : item.name }}<span v-if="valuePosition === 'top'" class="count">
          <template v-if="unit === '%'">{{ item.value | PercentFilter }}</template>
          <template v-else-if="unit === 's'">{{ item.value | SecondFilter }}</template>
          <template v-else-if="unit === 'B'">{{ item.value | BytesFilter }}</template>
          <template v-else>{{ item.value | NumberFilter }} {{ unit }}</template>
        </span>
      </div>
      <div class="item-cont">
        <div class="bar-wrap">
          <div :style="{ width: `${item.progress}%`, background: `${item.color}` }" class="progress"></div>
        </div>
        <div v-if="valuePosition !== 'top'" class="count">
          <template v-if="unit === '%'">{{ item.value | PercentFilter }}</template>
          <template v-else-if="unit === 's'">{{ item.value | SecondFilter }}</template>
          <template v-else-if="unit === 'B'">{{ item.value | BytesFilter }}</template>
          <template v-else>{{ item.value | NumberFilter }} {{ unit }}</template>
        </div>
      </div>
    </div>

    <div class="empty-show describe" key='empty' v-if="showEmpty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'

interface SourceItem {
  name: string;
  value: number;
}

type Unit = '%' | 's' | 'B' | string;

@Component
export default class HorizontalBar extends Vue {
  @Prop({ default: () => [] }) private source!: SourceItem[];
  @Prop({ default: false }) private showEmpty!: boolean;  // 数据是否为空
  @Prop({ default: 0 }) private maxValue!: number;        // (选) 最大值，为空时取 source 中的最大值
  @Prop({ default: '' }) private unit!: Unit;             // (选) 单位
  @Prop({ default: 'desc' }) private order!: 'asc' | 'desc' | 'none'; // (选) 排序
  @Prop({ default: 10 }) private itemGap!: number;        // (选) 间距
  @Prop({ default: 'right' }) private valuePosition!: 'right' | 'top'; // (选) 数值位置
  @Prop({ default: () => [] }) private colors!: any[];    // (选) 图表颜色

  get chartSource () {
    let source = (this.source || []).map(t => ({ ...t, value: isNaN(+t.value) ? 0 : +t.value }));
    if (this.order === 'desc') {
      source = source.sort((a, b) => b.value - a.value);
    } else if (this.order === 'asc') {
      source = source.sort((a, b) => a.value - b.value);
    }
    const maxValue = this.maxValue || Math.max(...source.map(t => t.value)) || 1;
    return source.map((item, index) => ({
      ...item,
      progress: Math.min(+(item.value / maxValue * 100).toFixed(2), 100),
      color: this.colors[index % this.colors.length] || '',
    }));
  }
}
</script>

<style lang="scss" scoped>
.horizontal-bar-wrap {
  width: 100%;
  height: 100%;
  .horizontal-bar-item {
    color: var(--color-text-regular);

    &:first-child {
      margin-top: 0 !important;
    }

    .item-title {
      font-size: 12px;
      line-height: 14px;
    }

    .item-cont {
      margin-top: 4px;
      display: flex;
      height: 6px;
    }

    .bar-wrap {
      width: calc(100% - 50px);
      height: 100%;
      background-color: var(--bg-color-base);
      border-radius: 1px;
      overflow: hidden;
      .progress {
        width: 0;
        height: 100%;
        border-radius: 1px;
        background-color: var(--color-primary);
      }
    }

    .count {
      margin-left: 10px;
      width: 40px;
      white-space: nowrap;
      font-size: 12px;
      line-height: 6px;
    }

    &.top-layout .bar-wrap {
      width: 100%;
    }
  }

  .empty-show {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 13px;
  }
}
</style>
