<template>
  <div class="problem-convg">
    <div v-for="item in convgList" :key="item.name" class="cont-t">
      <div :class="['cont-t-left', { 'only-text': item.type === 'text' }]">
        {{ item.nameKey ? $t(item.nameKey) : item.name }}
        <span v-if="item.unit === 'percent'" class="count">{{ item.value | PercentFilter }}</span>
        <span v-else class="count">{{ item.value | NumberFilter(false, item.unit || '') }}</span>
      </div>
      <div v-if="item.type !== 'text'" class="cont-t-right">
        <div class="bar-line" :style="`width:${item.width}%;`">
          <div class="bar-line-t" data-status="alarm"></div>
          <div class="bar-line-t" data-status="warn" :style="`width:${item.warn / (item.value || 1) * 100}%;`"></div>
          <div class="bar-line-t" data-status="nodata" :style="`width:${item.nodata / (item.value || 1) * 100}%;`"></div>
        </div>
        <div v-if="item.clipPath" class="bar-shadow" :style="`clip-path:${item.clipPath};`"></div>
      </div>
    </div>
    <div class="empty-show" v-if="showEmpty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';

interface ConvgItem {
  name: string;
  value: number;
  unit?: string;
  type?: 'text';
  warn?: number;
  nodata?: number;
  width?: number;
  clipPath?: string;
}

@Component
export default class ConvgChart extends Vue {
  @Prop({ default: false }) private showEmpty!: boolean;  // 数据是否为空
  @Prop({ default: () => [] }) private list!: ConvgItem[]

  get convgList (): ConvgItem[] {
    return this.list.map((item, index) => {
      const _item = { ...item };
      if (index > 0 && _item.type !== 'text') {
        const prevItem = this.list[index - 1];
        const tsWidth = (100 - (prevItem.width || 0) ) / 2;
        const bsWidth = (100 - (_item.width || 0)) / 2;
        _item.clipPath = `polygon(${tsWidth}% 0, ${100 - tsWidth}% 0, ${100 - bsWidth}% 100%, ${bsWidth}% 100%)`
      }
      return _item;
    });
  }
}
</script>

<style lang="scss" scoped>
.problem-convg {
  color: #626467;
  position: relative;

  .empty-show{
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 14px;
    color: var(--color-text-regular);
    background-color: var(--bg-color);
    border-radius: 5px;
    z-index: 9;
  }

  .cont-t {
    padding-top: 16px;
    height: 66px;
    display: flex;

    .cont-t-left {
      width: 90px;
      position: relative;
      z-index: 1;
      &.only-text {
        padding-left: 90px;
        width: 100%;
        text-align: center;
      }
    }
    .cont-t-right {
      width: calc(100% - 90px);
      position: relative;
    }

    .count {
      display: block;
      font-weight: 500;
      font-size: 22px;
      color: #121317;
      line-height: 30px;
      white-space: nowrap;
    }
  }

  .bar-line {
    margin: 25px auto 0;
    height: 20px;
    background-color: #E12828;
    display: flex;
    .bar-line-t:first-child {
      flex: 1;
    }
    .bar-line-t[data-status=warn] {
      background: #F7C00B;
    }
    .bar-line-t[data-status=nodata] {
      background: #B5B7BB;
    }
  }
  .bar-shadow {
    width: 100%;
    height: 46px;
    position: absolute;
    top: -21px;
    left: 0;
    background-color: #F7F7F7;
  }
}

// 黑色主题
:root[data-theme=dark] .problem-convg {
  color: #8B8E93;
  .cont-t .count {
    color: #EBEBED;
  }
  .bar-line .bar-line-t[data-status=nodata] {
    background: #45474A;
  }
  .bar-shadow {
    background-color: #242424;
  }
}
</style>
