<template>
  <el-select
    v-model="selected"
    :multiple="multiple"
    :multiple-limit="multipleLimit"
    :size="size"
    :placeholder="placeholder"
    :collapse-tags="collapseTags"
    :clearable="clearable"
    :filterable="allowCreate || filterable"
    :allow-create="allowCreate"
    :default-first-option="allowCreate"
    :disabled="disabled"
    :loading="loading"
    :filter-method="filterHandle"
    @change="changeHandle"
    @remove-tag="removeTagHandle"
    @visible-change="visibleChangeHandle"
    class="scroll-select"
    :popper-class="`popper-scroll-select ${popperClass}`">
    <el-option
      v-for="t in optionList.slice(0, loadedCount)"
      :key="`${t.label}_${t.value}`"
      :title="showTitle ? t.label : null"
      :label="t.label"
      :value="t.value"
      :disabled="typeof t.disabled === 'boolean' ? t.disabled : false"
      @click.prevent.stop.native="optionClickHandle(t.value)"
      class="scroll-select-option ell">
      <slot :label="t.label" :value="t.value"></slot>
    </el-option>
  </el-select>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import i18n from '@/i18n';
import { debounce } from '@/utils/common';
import { v4 as uuidv4 } from 'uuid';
import { StringIsEmpty } from '@/utils/common';

type OptionValue = string | number | boolean

interface OptionItem {
  label: string;
  value: OptionValue;
  custom?: boolean;
}

@Component({
  model: {
    prop: 'value',
    event: 'change',
  },
})
export default class ScrollSelect extends Vue {
  @Prop({ default: '' }) private value!: OptionValue | OptionValue[];
  @Prop({ default: () => [] }) private options!: Array<OptionItem | string>;
  @Prop({ default: false }) private multiple!: boolean;
  @Prop({ default: 'small' }) private size!: string;
  @Prop({ default: () => i18n.t('modules.components.s_708c9d6d') as string }) private placeholder!: string;
  @Prop({ default: true }) private collapseTags!: boolean;
  @Prop({ default: true }) private clearable!: boolean;
  @Prop({ default: true }) private filterable!: boolean;
  @Prop({ default: false }) private disabled!: boolean;
  @Prop({ default: false }) private loading!: boolean;
  @Prop({ default: 0 }) private multipleLimit!: number;
  @Prop({ default: false }) private allowCreate!: boolean;
  @Prop({ default: false }) private showTitle!: boolean; // 是否显示Option的title属性
  @Prop({ default: 50 }) private pageSize!: number;      // 每次加载的数量

  private selected: OptionValue | OptionValue[] = ''

  @Watch('value', { immediate: true, deep: true })
  private onValueChange (val: OptionValue | OptionValue[]) {
    this.selected = Array.isArray(val) ? [...val] : val;
    (Array.isArray(val) ? val : [val]).forEach(v => {
      if (!StringIsEmpty(v) && !this.allOptionList.find(t => t.value === v)) {
        this.allOptionList.push({ label: `${v}`, value: v, custom: true });
      }
    });
    if (this.allOptionList.length) {
      const allOptionList = [...this.allOptionList]
      this.loadedCount = this.pageSize
      // 如果存在value，并且value的位置大于pageSize，需要提前
      if (this.value && allOptionList && allOptionList.length > this.pageSize) {
        const _needSelectedValues = Array.isArray(this.value) ? [...this.value] : [this.value]
        _needSelectedValues.forEach((v) => {
          const valIndex = allOptionList.findIndex(i => i.value === v)
          if (valIndex && valIndex > this.pageSize) {
            const translateItem = { ...allOptionList[valIndex] }
            allOptionList.splice(valIndex, 1)
            allOptionList.unshift(translateItem)
          }
        })
      }
      this.optionList = [...allOptionList];
      this.$nextTick(() => {
        // 绑定滚动监听事件
        this.scrollLoop();
      });
    }
  }

  @Watch('options', { immediate: true, deep: true })
  private onOptionsChange () {
    const list: any[] = [...this.options];
    const isOptionItem = Object.prototype.toString.call(list[0]) === '[object Object]';
    this.allOptionList = isOptionItem ? list : list.map(t => ({ label: t, value: t }));
    (Array.isArray(this.value) ? this.value : [this.value]).forEach(v => {
      if (!StringIsEmpty(v) && !this.allOptionList.find(t => t.value === v)) {
        this.allOptionList.push({ label: `${v}`, value: v, custom: true });
      }
    });

    const allOptionList = [...this.allOptionList]
    this.loadedCount = this.pageSize
    // 如果存在value，并且value的位置大于pageSize，需要提前
    if (this.value && allOptionList && allOptionList.length > this.pageSize) {
      const _needSelectedValues = Array.isArray(this.value) ? [...this.value] : [this.value]
      _needSelectedValues.forEach((v) => {
        const valIndex = allOptionList.findIndex(i => i.value === v)
        if (valIndex && valIndex > this.pageSize) {
          const translateItem = { ...allOptionList[valIndex] }
          allOptionList.splice(valIndex, 1)
          allOptionList.unshift(translateItem)
        }
      })
    }
    this.optionList = [...allOptionList]
    this.$nextTick(() => {
      // 绑定滚动监听事件
      this.scrollLoop();
    });
  }

  private allOptionList: OptionItem[] = []
  private optionList: OptionItem[] = []
  private loadedCount: number = 0

  get noMore () {
    return this.loadedCount >= this.optionList.length
  }

  // 滚动加载相关
  private timer: any = null;
  private scrollContainer: any = null;
  private scrollHandle: any = null;
  private popperClass = `popper-scroll-select-${uuidv4()}`; // option滚动区标识

  private beforeDestroy () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    if (this.scrollContainer) {
      this.scrollContainer.removeEventListener('scroll', this.scrollHandle);
    }
  }

  // 选择完成后的回调
  private changeHandle (value: OptionValue | OptionValue[]) {
    this.$emit('change', Array.isArray(value) ? [...value] : value)
  }

  private removeTagHandle (value: OptionValue) {
    this.$emit('remove-tag', value)
  }

  private optionClickHandle (value: OptionValue) {
    this.$emit('option-click', value)
  }

  private visibleChangeHandle (show: boolean) {
    this.scrollContainer.scrollTop = 0
    this.$nextTick(() => {
      this.allOptionList.forEach(t => {
        const values = Array.isArray(this.selected) ? [...this.selected] : [this.selected]
        if (t.custom && !values.find(v => t.value === v)) {
          this.allOptionList.splice(this.allOptionList.indexOf(t), 1)
        }
      });

      this.loadedCount = this.pageSize
      this.optionList = [...this.allOptionList]
    })
    this.$emit('visible-change', show)
  }

  private filterHandle (query: string) {
    this.scrollContainer.scrollTop = 0
    this.$nextTick(() => {
      this.loadedCount = this.pageSize
      const q = query.toLocaleLowerCase()
      this.optionList = this.allOptionList.filter(t => t.label.toLocaleLowerCase().includes(q))
      this.$emit('filter-change', query, [...this.optionList])
    })
  }

  private scrollLoop () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    this.timer = setTimeout(() => {
      const scrollContainer = document.body.querySelector(`.el-select-dropdown.${this.popperClass} .el-select-dropdown__wrap.el-scrollbar__wrap`);
      if (!scrollContainer) {
        this.scrollLoop();
      } else {
        this.scrollContainer = scrollContainer;
        // 滚动到底加载更多
        this.scrollHandle = debounce(() => {
          const { scrollHeight, scrollTop, clientHeight } = scrollContainer
          if (!this.noMore && scrollHeight - clientHeight - scrollTop < 50) {
            this.loadedCount += this.pageSize
          }
        }, 17)
        scrollContainer.addEventListener('scroll', this.scrollHandle)
      }
    }, 100)
  }
}
</script>

<style lang="scss" scoped>
.scroll-select {
  width: 220px;
}

.scroll-select-option {
  min-width: 100%;
  max-width: 400px;
}
</style>
