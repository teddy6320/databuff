<template>
  <div class="metric-select-wrapper">
    <scroll-select
      v-model="selected"
      :options="options"
      :multiple="multiple"
      :size="size"
      :placeholder="placeholder"
      :clearable="clearable"
      :filterable="filterable"
      :disabled="disabled"
      :loading="loading"
      @change="changeHandle"
      @remove-tag="removeTagHandle"
      @visible-change="visibleChangeHandle"
      @filter-change="filterChangeHandle"
      class="metric-select"
    >
      <template v-slot="item">
        <div
          @mousemove="tooltip ? metricOptionMouseMove(item.value, $event) : null"
          @mouseleave="tooltip ? metricOptionMouseLeave() : null"
          :class="{ 'metric-select-option-loading': (metricInfos[item.value] || {}).loading }"
          class="ell">{{ item.labelKey ? $t(item.labelKey) : item.label }}</div>
        <i v-if="(metricInfos[item.value] || {}).loading" class="el-icon-loading"></i>
      </template>
    </scroll-select>

    <metric-info-tooltip
      v-show="currMetricInfo"
      ref="metricTooltip"
      :detail="currMetricInfo || {}"
    />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import ScrollSelect from '@/components/scroll-select.vue';
import MetricInfoTooltip from '@/components/metric-info-tooltip.vue';

interface OptionItem {
  label: string;
  value: string;
}

@Component({
  components: {
    ScrollSelect,
    MetricInfoTooltip,
  },
  model: {
    prop: 'value',
    event: 'change',
  },
})
export default class MetricSelect extends Vue {
  @Prop({ default: '' }) private value!: string | string[];
  @Prop({ default: () => [] }) private options!: Array<OptionItem | string>;
  @Prop({ default: false }) private multiple!: boolean;
  @Prop({ default: 'small' }) private size!: string;
  @Prop({ default: () => i18n.t('modules.components.s_708c9d6d') as string }) private placeholder!: string;
  @Prop({ default: true }) private clearable!: boolean;
  @Prop({ default: true }) private filterable!: boolean;
  @Prop({ default: false }) private disabled!: boolean;
  @Prop({ default: false }) private loading!: boolean;
  @Prop({ default: true }) private tooltip!: boolean; // 是否显示Tooltip

  /*
    注意：
    如果 MetricSelect 值已更新，但 ScrollSelect 值未更新，
    则需要给 MetricSelect 添加动态的 key 值
  */

  private selected: string | string[] = ''

  private selectVisible = false

  @Watch('value', { immediate: true, deep: true })
  private onValueChange (val: string | string[]) {
    this.selected = Array.isArray(val) ? [...val] : val
  }

  // 选择完成后的回调
  private changeHandle (value: string | string[]) {
    this.$emit('change', Array.isArray(value) ? [...value] : value)
    this.metricOptionMouseLeave()
  }

  private removeTagHandle (value: string) {
    this.$emit('remove-tag', value)
    this.metricOptionMouseLeave()
  }

  private visibleChangeHandle (show: boolean) {
    this.selectVisible = show
    this.$emit('visible-change', show)
    this.metricOptionMouseLeave()
  }

  private filterQuery = '';
  private filteredOptions: string[] = []
  private filterChangeHandle (query: string, options: OptionItem[]) {
    this.filterQuery = query;
    this.filteredOptions = options.map(t => t.label)
    const index = this.filteredOptions.findIndex(t => this.currMetric === t)
    if (this.currMetricIndex !== -1 && index !== this.currMetricIndex) {
      this.metricOptionMouseLeave()
    }
  }

  // 指标详细信息
  private metricInfos: any = {}
  // tooltip timer
  private tooltipTimer: any = null;
  // 当前hover展示详情的指标
  private currMetric = ''
  // 当前指标在已加载options中的索引，用于判断是否隐藏tooltip
  private currMetricIndex = -1;
  get currMetricInfo () {
    if (!this.currMetric || !this.metricInfos[this.currMetric] || this.metricInfos[this.currMetric].loading) {
      return null
    }
    return this.metricInfos[this.currMetric]
  }
  private metricOptionMouseMove (metric: any, event: any) {
    if (this.currMetric === metric) {
      return;
    }

    this.currMetric = metric; // 防止接口返回延迟导致和选中指标不一致

    let _options = this.filteredOptions
    if (!this.filterQuery) {
      const isOptionItem = Object.prototype.toString.call(this.options[0]) === '[object Object]';
      _options = isOptionItem ? this.options.map((t: any) => t.label) : this.options;
    }
    this.currMetricIndex = _options.findIndex(t => metric === t);

    if (this.tooltipTimer) {
      clearTimeout(this.tooltipTimer);
      this.tooltipTimer = null;
    }
    this.tooltipTimer = setTimeout(() => {
      const metricData = this.metricInfos[metric]
      // 超过300ms的时候，加载指标详情
      if (metricData) { // 正在加载
        if (!metricData.loading) { // 已加载
          this.showMetricTooltipDetail(event, metricData);
        }
        return;
      }
      this.getMetricInfo(metric, event)
    }, 300)
  }
  private metricOptionMouseLeave () {
    this.currMetric = '';
    this.currMetricIndex = -1;
    if (this.tooltipTimer) {
      clearTimeout(this.tooltipTimer);
      this.tooltipTimer = null;
    }
  }
  private async getMetricInfo (metric: string, event: any) {
    this.$set(this.metricInfos, metric, { metric, loading: true });
    await this.$store.dispatch('Common/GET_METRIC_INFOS', [metric]);
    this.metricInfos[metric].loading = false;
    const metricInfo = this.$store.getters['Common/metricInfoMap'][metric]
    if (metricInfo) {
      this.$set(this.metricInfos, metric, {
        loading: false,
        ...metricInfo,
      });
      this.showMetricTooltipDetail(event, this.metricInfos[metric]);
      this.$emit('metric-info-change', { ...this.metricInfos });
    }
  }
  private showMetricTooltipDetail (event: any, detail: any) {
    this.$nextTick(() => {
      if (!this.selectVisible || !this.currMetric || this.currMetric !== detail.metric) {
        return;
      }
      const targetDom = event.target || event.srcElement || null;
      if (targetDom && targetDom.parentNode && targetDom.parentNode.getBoundingClientRect) {
        const { top, left, right, width, height } = targetDom.parentNode.getBoundingClientRect()
        if (this.$refs.metricTooltip && (this.$refs.metricTooltip as any).$el) {
          const $metricTooltip = (this.$refs.metricTooltip as any).$el as HTMLDivElement;
          if (!width || !height) {
            $metricTooltip.style.display = 'none';
            return;
          }
          const bodyWidth = document.body.clientWidth
          const bodyHeight = document.body.clientHeight
          $metricTooltip.style.display = 'block';
          $metricTooltip.style.left = `${right + 400 <= bodyWidth ? right : left - 400}px`;
          if (top + $metricTooltip.clientHeight <= bodyHeight) {
            $metricTooltip.style.top = `${top}px`;
            $metricTooltip.style.bottom = 'auto';
          } else {
            $metricTooltip.style.top = 'auto';
            $metricTooltip.style.bottom = '0px';
          }
        }
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.metric-select-wrapper {
  display: inline-block;
  line-height: 1;
  .metric-select {
    width: 100%;
  }
}

.metric-select-option-loading + .el-icon-loading {
  margin-top: -7px;
  position: absolute;
  top: 50%;
  right: 6px;
}
</style>
