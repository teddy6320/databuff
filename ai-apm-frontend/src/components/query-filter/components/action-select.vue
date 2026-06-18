<template>
  <div
    v-infinite-scroll="() => loadListHandle(queryParams.pageNum + 1)"
    :infinite-scroll-disabled="noMore"
    :infinite-scroll-distance="50"
    v-show="!noMatchModel || !isLikeable"
    class="query-filter-select-cont">
    <div v-for='option in showOptions' :key='option.value'
      @click.stop="handleChoose(option)"
      class="query-filter-select-option"
      :class="{
        'query-filter-select-option-multiple': isMultiple,
        'query-filter-select-option-checked': option.checked,
      }"
    >
      <el-checkbox v-if="isMultiple" v-model="option.checked" class="query-filter-select-option-checkbox" />
      <div class="query-filter-select-option-text" :title="option.label">{{ option.labelKey ? $t(option.labelKey) : option.label }}</div>
      <div
        v-for="(text, i) in (option.info.texts || [])"
        :key="i"
        class="query-filter-select-option-info">{{ text }}</div>
    </div>
    <div v-show='noMatchModel' class="empty-text cn tc">{{ noMatchText }}</div>
  </div>
</template>
<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { FilterItemLocal, FilterOptionLocal, TagItem } from '../types/index.types';

@Component
export default class QueryFilterSelectCont extends Vue {
  @Prop({ default: () => [] }) private filterList!: FilterItemLocal[];
  @Prop({ default: () => null }) private currTag!: { index: number, tag: TagItem }|null;
  @Prop({ default: false }) private actionStatus!: boolean;
  @Prop({ default: '' }) private searchModel!: string;

  get calcOptions (): FilterOptionLocal[] {
    // 非操作已有tag时，展示所有选项
    if (!this.actionStatus) {
      return this.filterList.filter((item) => !item.disabled).map((item) => ({
        label: item.label,
        value: item.field,
        showValue: item.field,
        checked: false,
        disabled: item.disabled || false,
        custom: false,
        kv: 'k',
        info: {} as any,
      }))
    } else if (this.currTag) {
      // 操作已有tag时，展示已有tag的选项
      const { tag }  = this.currTag
      const { label, field, value } = tag
      const targetFilterItem = this.filterList.find((item) => item.field === field && item.label === label)
      if (targetFilterItem) {
        return targetFilterItem.children.map((item) => ({
          label: item.label,
          value: item.value,
          showValue: item.showValue,
          checked: !Array.isArray(value) ? value === item.value : value.includes(item.value),
          disabled: item.disabled || false,
          custom: item.custom || false,
          kv: 'v',
          info: item.info || {} as any,
        }))
      } else {
        return []
      }
    }
    return []
  }

  get currFilterItem () {
    if (this.currTag) {
      const { tag }  = this.currTag
      const { label, field, value } = tag
      const targetFilterItem = this.filterList.find((item) => item.field === field && item.label === label)
      return targetFilterItem;
    } else {
      return null;
    }
  }

  get isLikeable () {
    return !!this.currFilterItem && !!this.currFilterItem.likeable
  }

  get isMultiple () {
    return !!this.currFilterItem && !!this.currFilterItem.multiple
  }

  // 搜索后的选项
  get filteredOptions () {
    return (this.calcOptions || []).filter((item) => !item.custom && item.label.toLocaleLowerCase().includes(this.searchModel))
  }

  @Watch('filteredOptions', { deep: true, immediate: true })
  private searchModelChange () {
    this.loadListHandle(1)
  }

  get noMatchModel () {
    return this.filteredOptions.length === 0
  }

  get noMatchText () {
    return this.searchModel && this.noMatchModel ? i18n.t('modules.components.query-filter.components.s_22bc560e') as string : i18n.t('modules.components.query-filter.components.s_1970704a') as string
  }

  // 滚动加载
  private queryParams = {
    pageNum: 1,
    pageSize: 50,
  }
  private showOptions: FilterOptionLocal[] = []
  get noMore () {
    return this.showOptions.length >= this.filteredOptions.length
  }
  private loadListHandle (page = 1) {
    if (page !== 1 && this.noMore) {
      return;
    }
    this.queryParams.pageNum = page
    const { pageSize } = this.queryParams
    this.showOptions = [...this.filteredOptions.slice(0, page * pageSize)];
  }

  private handleChoose (row: FilterOptionLocal) {
    this.$emit('on-choose', row)
  }
}
</script>
<style lang="scss" scoped>
.query-filter-select-cont {
  padding: 8px 0;
  min-width: 100px;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;

  .query-filter-select-option {
    padding: 3px 20px;
    line-height: 24px;
    cursor: pointer;
    position: relative;
    &.query-filter-select-option-multiple {
      padding-left: 35px;
    }

    &.query-filter-select-option-checked,
    &:hover {
      background-color: var(--background-color-base);
    }
    .query-filter-select-option-text {
      max-width: 600px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      font-size: 13px;
    }
    .query-filter-select-option-info {
      font-size: 12px;
      color: var(--color-text-secondary);
      line-height: 16px;
    }
    .query-filter-select-option-checkbox {
      transform: translate(0, -50%);
      position: absolute;
      top: 50%;
      left: 16px;
      pointer-events: none;
      :deep(span) {
        display: block;
      }
    }
  }
}
.empty-text {
  font-size: 12px;
  color: var(--color-text-secondary);
  // margin-left: 8px;
  line-height: 32px;
}
</style>
