<template>
  <div ref='radioSegment' :class='[
      "custom-radio-segment",
      `custom-radio-segment-${this.size}`,
      disable ? "is-disabled" : "",
      plain ? "custom-radio-segment-plain" : "",
    ]' >
    <div class="custom-radio-segment-wrapper">
      <div class="custom-radio-segment-active" :style='{ width: translateInfo.width, transform: translateInfo.x, display: "block" }'></div>
      <div v-for="item, index in options" :key='item.value'
        @click="changeHandle(item, index)"
        :class='["custom-radio-segment-item", current === item.value ? "is-selected" : "", item.disabled ? "is-disabled" : ""]'>
        <i v-if="item.icon" :class="[item.icon, item.label ? 'mr-5' : '']"></i>
        {{ item.labelKey ? $t(item.labelKey) : item.label }}
        <el-tooltip :content='item.describeKey ? $t(item.describeKey) : item.describe' v-if='item.describe' placement="top">
          <i class="el-icon el-icon-question ml-5"></i>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script lang='ts'>
import { StringIsEmpty } from '@/utils/common';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';

const NotNull = (value: any) => value !== null && value !== undefined;

@Component
export default class RadioSegment extends Vue {
  @Prop({ default: 'default' }) private size?: 'large'|'default'|'small';
  @Prop() private options!: any[]
  @Prop() private value!: any;
  @Prop() private disabled!: boolean;
  @Prop({ default: true }) private plain!: boolean;

  @Watch('value', { immediate: true })
  private onValueChange (newVal: string) {
    if (!this.isMounted) {
      return;
    }
    this.current = newVal;
    if (NotNull(this.value)) {
      const target = this.options.find(i => i.value === this.value)
      const targetIndex = this.options.findIndex(i => i.value === this.value)
      if (target) {
        this.current = target.value
        this.currentIndex = targetIndex
        this.$nextTick(() => {
          this.calcActiveSlider(targetIndex)
        })
      }
    }
    this.$emit('input', newVal);
  }
  @Watch('options')
  private onOptionsChange (newVal: any[]) {
    if (NotNull(this.value)) {
      const target = newVal.find(i => i.value === this.value)
      const targetIndex = newVal.findIndex(i => i.value === this.value)
      if (target) {
        this.$nextTick(() => {
          this.calcActiveSlider(targetIndex)
        })
      }
    }
  }

  public $refs!: {
    radioSegment: HTMLDivElement
  }

  private current: any = null;
  private currentIndex: number|null = null;
  private isMounted = false;

  private translateInfo = {
    width: '0px',
    x: 'translateX(0px)',
  }

  private created () {
    //
  }

  private mounted () {
    this.isMounted = true;
    if (NotNull(this.value)) {
      this.onValueChange(this.value);
    }
  }

  private changeHandle (option: any, index: number) {
    if (this.currentIndex === index || option?.disabled) {
      return;
    }
    this.currentIndex = index
    this.current = option.value
    this.calcActiveSlider(index)
    this.$nextTick(() => {
      this.$emit('change', { ...option, $index: index })
    })
  }

  private calcActiveSlider (index: number) {
    if (typeof index !== 'number' || !this.$refs.radioSegment) {
      return
    }
    const itemDomList = this.$refs.radioSegment.querySelectorAll('.custom-radio-segment-item')
    const targetDom = itemDomList && itemDomList[index]
    if (targetDom && targetDom.getBoundingClientRect && targetDom instanceof HTMLElement) {
      const { width } = targetDom.getBoundingClientRect()
      this.translateInfo.width = `${width}px`;
      this.translateInfo.x = `translateX(${targetDom.offsetLeft}px)`
      this.$emit('input', this.current)
    }
  }
}
</script>
<style lang='scss' scoped>
.custom-radio-segment {
  --color-bg: #f7f7f7;
  --color-bg-normal: #fff;
  --color-text-active: #2962ff;
  --color-text-normal: #777a7e;

  display: inline-flex;
  align-items: stretch;
  min-height: 32px;
  border-radius: 4px;
  padding: 3px;
  background-color: var(--color-bg);
  position: relative;
  white-space: nowrap;

  &-large {
    min-height: 40px;
    .custom-radio-segment-item {
      font-size: 16px;
    }
  }
  &-small {
    min-height: 24px;
  }

  &-wrapper {
    display: flex;
    align-items: stretch;
    position: relative;
    width: 100%;
  }

  &.is-disabled {
    pointer-events: none;
    cursor: not-allowed;
    opacity: 0.7;
  }

  .custom-radio-segment-active {
    position: absolute;
    top: 0;
    left: 0;
    width: 10px;
    height: 100%;
    background-color: var(--color-text-active);
    transition: all .3s;
    pointer-events: none;
    border-radius: 4px;
    box-shadow: 0px 2px 4px 0px rgba(119, 122, 126, 0.1);
  }

  .custom-radio-segment-item {
    display: flex;
    align-items: center;
    flex: 1;
    padding: 0 10px;
    cursor: pointer;
    color: var(--color-text-normal);
    transition: all .15s ease;
    font-size: 12px;
    font-weight: 400;
    z-index: 1;
    word-break: keep-all;

    &.is-selected {
      color: var(--color-bg-normal);
    }
    &.is-disabled {
      cursor: not-allowed;
      opacity: 0.7;
    }
  }

  &-plain {
    .custom-radio-segment-active {
      background-color: var(--color-bg-normal);
    }
    .custom-radio-segment-item.is-selected {
      color: var(--color-text-active);
    }
  }
}
</style>
