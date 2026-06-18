<template>
  <el-cascader
    v-model="cValue"
    :options="optionList"
    :props="allProps"
    :size="size"
    :placeholder="placeholder"
    :separator="separator"
    :filterable="filterable"
    :show-all-levels="showAllLevels"
    :disabled="disabled"
    clearable
    @change="changeHandle"
    popper-class="popper-metric-unit-cascader"
  />
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import i18n from '@/i18n';
import MetricApi from '@/api/metric';
import { toAsyncWait } from '@/utils/common';
import deepClone from 'lodash/cloneDeep';

interface OptionItem {
  label: string;
  value: string;
  children: OptionItem[] | null,
}

@Component({
  model: {
    prop: 'value',
    event: 'change',
  },
})
export default class MetricUnitCascader extends Vue {
  @Prop({ default: () => [] }) private value!: string[];
  @Prop({ default: null }) private options!: any[] | null; // 存在时将不再执行 getMetaUnits
  @Prop({ default: 'small' }) private size!: string;
  @Prop({ default: () => i18n.t('modules.components.s_f76a4822') as string }) private placeholder!: string;
  @Prop({ default: true }) private filterable!: boolean;
  @Prop({ default: true }) private showAllLevels!: boolean;
  @Prop({ default: '/' }) private separator!: boolean;
  @Prop({ default: false }) private disabled!: boolean;
  @Prop({ default: () => ({}) }) private props!: any;

  private isLoading = false

  private cValue: string[] = []

  @Watch('value', { immediate: true, deep: true })
  private onValueChange (val: string[]) {
    this.cValue = [...val]
  }

  private optionList: OptionItem[] = []
  private inProps: any = {
    // expandTrigger: 'hover',
  }
  get allProps () {
    return {
      ...this.props,
      ...this.inProps,
    }
  }

  @Watch('options', { immediate: true, deep: true })
  private onOptionListChange (val: any[]) {
    if (Array.isArray(val)) {
      this.optionList = deepClone(val)
    }
  }

  private created() {
    if (!Array.isArray(this.options)) {
      this.getMetaUnits()
    }
  }

  // 选择完成后的回调
  private changeHandle (value: string[]) {
    this.$emit('change', [...value])
  }

  private async getMetaUnits () {
    this.isLoading = true;
    const { result, error } = await toAsyncWait(MetricApi.getMetaUnits());
    this.isLoading = false;
    if (!error) {
      this.optionList = (result.data || []).map((item: any) => ({
        label: item.group,
        value: item.group,
        children: (item.values || []).map((t: string) => ({ label: t, value: t }))
      }))
    }
  }
}
</script>

<style lang="scss">

</style>
