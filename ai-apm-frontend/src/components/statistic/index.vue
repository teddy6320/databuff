<template>
  <div class="custom-statistic">
    <div v-if="$slots.title || title" class="custom-statistic-header">
      <slot name='title'>
        <span>{{ title }}</span>
        <el-tooltip v-if='describe' :content="describe" placement="top">
          <i class="el-icon el-icon-question ml-5"></i>
        </el-tooltip>
      </slot>
    </div>
    <div class="custom-statistic-content">
      <div v-if="$slots.prefix || prefix" class="custom-statistic-content-prefix">
        <slot name="prefix">
          <span>{{ prefix }}</span>
        </slot>
      </div>
      <span class="custom-statistic-content-value">
        {{ displayValue }}
      </span>
      <div v-if="$slots.suffix || suffix"  class="custom-statistic-content-prefix">
        <slot name="suffix">
          <span>{{ suffix }}</span>
        </slot>
      </div>
    </div>
  </div>
</template>

<script lang='ts'>
import { Vue, Component, Prop } from 'vue-property-decorator';
import { NumberFilter } from '@/utils/filters/number'
import { TimesToDateFilter, NsFilter, MsFilter, SecondFilter } from '@/utils/filters/times'

@Component
export default class StatisticComp extends Vue {
  @Prop({}) private title!: string;
  @Prop({}) private describe!: string;
  @Prop({ default: 0 }) private value!: string|number;
  @Prop({ default: null }) private formatType!: null|'number'|'date'|'ns'|'ms'|'s'
  @Prop({ default: 0 }) private precision!: number;
  @Prop({}) private prefix!: string;
  @Prop({}) private suffix!: string;

  get displayValue () {
    switch (this.formatType) {
      case 's':
        return SecondFilter(+this.value)
      case 'ns':
        return NsFilter(+this.value)
      case 'ms':
        return MsFilter(+this.value)
      case 'date':
        return TimesToDateFilter(this.value)
      case 'number':
        return NumberFilter(this.value)
      default:
        return this.value
    }
  }
}
</script>
<style lang='scss' scoped>
.custom-statistic {
  text-align: center;
  .custom-statistic-header {
    line-height: 20px;
    margin-bottom: 4px;
    font-weight: 400;
    font-size: 12px;
    color: var(--color-text-regular);
  }

  .custom-statistic-content {
    font-weight: 400;
    font-size: 20px;
    color: var(--color-text-primary);

    .custom-statistic-content-value {
      display: inline-block;
    }
    .custom-statistic-content-prefix {
      margin-left: 4px;
      display: inline-block;
    }
    .custom-statistic-content-suffix {
      margin-left: 4px;
      display: inline-block;
    }
  }
}
</style>