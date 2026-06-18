<template>
  <div :class="['db-tabs-nav', { 'db-tabs-nav-thin': !!thin, 'db-tabs-nav-slim': !!slim }]" ref='tabnavsDom'>
    <div
      v-for="tab, idx in tabnavs"
      :key="tab.value"
      :class="['tabs-nav-item cp', current === tab.value ? 'active' : '', tab.disabled ? 'is-disabled' : '', tab.dot? 'is-dot' : '']"
      @click="activeTabChange(tab, idx)"
    >
      <slot v-bind="{ tab }">
        <span>{{ tab.labelKey ? $t(tab.labelKey) : tab.label }}</span>
        <el-tooltip v-if='tab.tip' :content="tab.tip" placement="top" effect="light" :enterable='false'>
          <i class="el-icon el-icon-question tavnav-item-tip ml-5"></i>
        </el-tooltip>
      </slot>
    </div>

    <!-- 滑块 -->
    <div class="db-tabs-nav-slider" :style='{ width: translateInfo.width, transform: translateInfo.x }'></div>
  </div>
</template>

<script lang="ts">
interface Option {
  label: string;
  value: string;
  disabled?: boolean;
  tip?: string;
  dot?: boolean;
}
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
@Component
export default class DBTabnav extends Vue {
  @Prop() private tabnavs!: Option[];
  @Prop() private value!: string;
  @Prop() private activeName!: string;
  @Prop() private thin!: boolean;
  @Prop() private slim!: boolean;

  public $refs!: {
    tabnavsDom: HTMLDivElement
  }

  private current = this.value || this.activeName || '';
  private currentIndex: number|null = null;
  private isMounted = false;

  @Watch('value', { immediate: true })
  private onValueChange (newVal: string) {
    if (!this.isMounted) {
      return;
    }
    this.current = newVal;
    if (this.value) {
      const target = this.tabnavs.find(i => i.value === this.value)
      const targetIndex = this.tabnavs.findIndex(i => i.value === this.value)
      if (target) {
        this.current = target.value
        this.currentIndex = targetIndex
        this.$nextTick(() => {
          this.calcActiveSlider(targetIndex);
          this.translateInfo.width = '24px';
        })
      }
    }
    this.$emit('input', newVal);
  }
  @Watch('activeName')
  private onActiveNameChange (newVal: string) {
    this.current = newVal;
  }
  @Watch('tabnavs')
  private onTabnavsChange (newVal: string) {
    if (this.value) {
      const target = this.tabnavs.find(i => i.value === this.value)
      const targetIndex = this.tabnavs.findIndex(i => i.value === this.value)
      if (target) {
        this.$nextTick(() => {
          this.calcActiveSlider(targetIndex);
          this.translateInfo.width = '24px';
        })
      }
    }
  }

  private translateInfo = {
    width: '0px',
    x: 'translateX(0)',
  }

  private mounted () {
    this.isMounted = true;
    if (this.value) {
      this.onValueChange(this.value);
    }
  }

  private activeTabChange (tab: Option, index: number) {
    if (tab.disabled) {
      return
    }
    const { value } = tab
    if (this.current === value) {
      return
    }
    this.current = value;
    this.calcActiveSlider(index);
    this.$emit('input', value);
    this.$nextTick(() => {
      this.$emit('on-change', tab);
    })
  }

  private calcActiveSlider (index: number) {
    if (typeof index !== 'number') {
      return
    }
    const itemDomList = this.$refs.tabnavsDom.querySelectorAll('.tabs-nav-item')
    const targetDom = itemDomList && itemDomList[index]
    if (targetDom && targetDom.getBoundingClientRect && targetDom instanceof HTMLElement) {
      const { width } = targetDom.getBoundingClientRect();
      this.translateInfo.x = `translateX(${targetDom.offsetLeft + width / 2 - 12}px)`
      this.$emit('input', this.current)
    }
  }
}
</script>

<style lang="scss" scoped>
.db-tabs-nav{
  --color-bg: #f7f7f7;
  --color-bg-normal: #fff;
  --color-text-active: #2962ff;
  --color-text-normal: #777a7e;
  --color-text-title: #121317;

  width: 100%;
  color: var(--color-text-regular);
  position: relative;
  font-size: 0;
  padding-bottom: 2px;

  .db-tabs-nav-slider {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    // width: 24px;
    height: 2px;
    background-color: var(--color-text-active);
    border-radius: 5px 5px 0 0;
    transition: all 0.35s cubic-bezier(.77,0,.75,.95);
  }

  .tabs-nav-item {
    display: inline-block;
    vertical-align: top;
    padding: 0 0 10px;
    line-height: 14px;
    font-size: 13px;
    position: relative;
    color: var(--color-text-title);
    transition: color .3s ease, font-weight .3s ease;

    &:not(:nth-last-of-type(2)) {
      margin-right: 30px;
    }
    
    &:not(.active, .is-disabled):hover {
      color: var(--color-text-primary);
    }
    &.active {
      color: var(--color-text-active);
      // font-weight: 500; // 产生抖动，用-webkit-text-stroke代替
      -webkit-text-stroke: 0.5px currentColor;
    }
    &.is-disabled {
      cursor: not-allowed;
      color: var(--color-text-placeholder);
    }
    &.is-dot {
      &::before {
        content: '';
        position: absolute;
        top: -2px;
        right: -6px;
        width: 6px;
        height: 6px;
        border-radius: 3px;
        background-color: var(--color-danger);
        pointer-events: none;
      }
    }
  }

  &-thin {
    .tabs-nav-item  {
      padding-left: 0;
      padding-right: 0;
    }
  }
  &-slim {
    .tabs-nav-item:not(:nth-last-of-type(2)) {
      margin-right: 18px;
    }
  }
}
</style>
