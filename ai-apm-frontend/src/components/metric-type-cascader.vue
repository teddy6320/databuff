<template>
  <el-cascader
    ref="typeCascader"
    v-model="cValue"
    :options="optionList"
    :props="allProps"
    :size="size"
    :placeholder="placeholder"
    :separator="separator"
    :filterable="filterable"
    :show-all-levels="showAllLevels"
    :disabled="disabled"
    collapse-tags clearable
    @change="changeHandle"
    @visible-change="visibleChangeHandle"
  />
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import i18n from '@/i18n';
import { formatMetricTypeData } from '@/store/modules/common/utils';
import { CascaderOptionItem } from '@/store/modules/common/index.types';

@Component({
  model: {
    prop: 'value',
    event: 'change',
  },
})
export default class MetricTypeCascader extends Vue {
  @Prop({ default: () => [] }) private value!: string[];
  @Prop({ default: null }) private options!: any[] | null; // 存在时将覆盖内部数据
  @Prop({ default: false }) private awaitOption!: boolean; // 等待options加载
  @Prop({ default: true }) private autoHidden!: boolean; // 点击叶子节点是否自动收起面板
  @Prop({ default: 'small' }) private size!: string;
  @Prop({ default: () => i18n.t('modules.components.s_8bb820b8') as string }) private placeholder!: string;
  @Prop({ default: true }) private filterable!: boolean;
  @Prop({ default: true }) private showAllLevels!: boolean;
  @Prop({ default: '/' }) private separator!: boolean;
  @Prop({ default: false }) private disabled!: boolean;
  @Prop({ default: () => ({}) }) private props!: any;

  private cValue: string[] = []

  @Watch('value', { immediate: true, deep: true })
  private onValueChange (val: string[]) {
    this.cValue = [...val]
  }

  private inProps: any = {
    // expandTrigger: 'hover',
    checkStrictly: true,
  }
  get allProps () {
    return {
      ...this.props,
      ...this.inProps,
    }
  }

  get optionList (): CascaderOptionItem[] {
    if (!Array.isArray(this.options)) {
      return this.$store.getters['Common/metricTypeData'] || []
    } else if (!this.options.length) {
      return []
    } else {
      const first = this.options[0]
      if (first.label && first.value) {
        return this.options
      } else {
        return formatMetricTypeData(this.options)
      }
    }
  }

  private async created() {
    if (!this.awaitOption && !Array.isArray(this.options)) {
      await this.$store.dispatch('Common/GET_METRIC_TYPES');
    }
  }

  // 选择完成后的回调
  private changeHandle (value: string[]) {
    if (this.autoHidden) {
      let options = this.optionList;
      let leaf = false;
      value.forEach((val: string) => {
        const item = options.find(t => t.value === val)
        if (item && item.leaf) {
          leaf = true;
        } else {
          options = item && item.children ? item.children : []
        }
      })
      if (leaf) {
        (this.$refs.typeCascader as any).toggleDropDownVisible(false);
      }
    }
    this.$emit('change', [...value])
  }

  private visibleChangeHandle (visible: boolean) {
    this.$emit('visible-change', visible)
  }
}
</script>
