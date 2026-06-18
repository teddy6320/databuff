<template>
  <div
    @click="showPopoverHandle"
    :class="{ 'query-filter-readonly': readonly }"
    class="query-filter-wrap">
    <el-tooltip v-if="filterStr"  placement="top" effect="light">
      <div class="query-filter-input">
        {{ filterStr }}
        <span @click.stop="clearHandle" class="clear el-icon-circle-close"></span>
      </div>
      <div slot="content" class="query-filter-tooltip">
        {{ filterStr }}
        <div v-for="(item, index) in filterStrList" :key="index" class="mt-5 wba">{{ item }}</div>
      </div>
    </el-tooltip>
    <span v-else class="placeholder">{{ placeholder }}</span>

    <el-dialog
      :visible.sync="visible"
      :title="$t('modules.views.configManage.alarm.s_c9cc86e7')"
      append-to-body
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      destroy-on-close
      width="1000px"
      top="30vh"
      custom-class="query-filter-dialog">
      <div v-loading="fieldDataLoading" class="metric-qf-cont">
        <matching-criteria
          v-if="visible && !fieldDataLoading"
          ref="filterCriteria"
          :conditionData="initFilter"
          :fieldData="fieldData"
          :symbols="symbolList"
          :fieldLabelMap="tagLabelMap"
          :allowCreateOption="true"
          :addMultipleText="$t('modules.views.configManage.alarm.s_c9ee9e43')"
          :maxLevel="10"
          :showView="true"
          :detailView="true"
          :showCase="true"
          @on-change="conditionChangeHandle"
        />
      </div>

      <template slot="footer">
        <el-button @click="closeHandle" size="mini">{{ $t('modules.views.alarmCenter.alarm.s_b15d9127') }}</el-button>
        <el-button @click="confirmHandle" type="primary" size="mini">{{ $t('modules.components.dialog-template.s_38cf16f2') }}</el-button>
      </template>
    </el-dialog>

    <!-- 隐藏，用于调用组件内部方法 -->
    <matching-criteria
      ref="hideCriteria"
      :conditionData="initFilter"
      :fieldData="fieldData"
      :symbols="symbolList"
      :fieldLabelMap="tagLabelMap"
      :allowCreateOption="true"
      :maxLevel="10"
      :showView="true"
      :detailView="true"
      :showCase="true"
      :hidden="true"
    />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { orderBy } from 'lodash';
import MatchingCriteria from '@/components/matching-criteria/index.vue'
import { toAsyncWait } from '@/utils/common';
import MetricApi from '@/api/metric';
import { alarmFilterSymbolList } from '../../filter-symbols';

const symbolList = alarmFilterSymbolList

const unwrapResponseData = (response: any) => {
  if (response && typeof response === 'object' && Object.prototype.hasOwnProperty.call(response, 'data')) {
    return response.data
  }
  return response
}

@Component({
  components: {
    MatchingCriteria,
  }
})
export default class QueryFilter extends Vue {
  @Prop({ default: false }) private readonly!: boolean;
  @Prop({ default: () => i18n.t('modules.views.configManage.alarm.s_d2e45189') as string }) private placeholder!: string;
  @Prop({ default: () => [] }) private initFilter!: any[];
  @Prop({ default: () => [] }) private metrics!: string[];
  @Prop({ default: () => ({}) }) private metricTagValues!: any;
  @Prop({ default: () => ({}) }) private tagLabelMap!: any; // 标签名称映射
  @Prop() private symbols!: any[]

  public $refs!: {
    filterCriteria: MatchingCriteria
    hideCriteria: MatchingCriteria
  }

  private fieldData: any = {}
  private fieldDataLoading = false

  get metricList () {
    const metrics: string[] = Array.isArray(this.metrics) ? this.metrics : []
    return Array.from(new Set(metrics.filter(t => !!t))).sort()
  }

  get symbolList () {
    return [...(this.symbols || symbolList)]
  }

  private visible = false;

  private filterList: any[] = [];
  private filterStr: string = '';
  private filterStrList: string[] = []

  @Watch('initFilter.length', { immediate: true })
  private async initFilterChange (val: number) {
    if (!val) {
      this.clear();
    } else {
      // 临时处理
      if (!this.filterList.length) {
        this.init(true)
      }
    }
  }

  @Watch('tagLabelMap', { immediate: true, deep: true })
  private async tagLabelMapChange (val: any) {
    this.init(true)
  }

  private async created () {
    this.init(true)
  }

  // 初始化
  private init (setFilter?: boolean) {
    this.$nextTick(() => {
      if (this.initFilter && this.initFilter.length) {
        const $criteria = this.$refs.hideCriteria
        $criteria.getCondition(this.initFilter).then((conditions) => {
          this.conditionChangeHandle(conditions)
          if (setFilter) {
            this.filterStr = this.filterCriteria.str;
            this.filterStrList = this.filterCriteria.charArr;
          }
        })
      } else {
        this.filterList = [];
        this.filterCriteria.str = '';
        this.filterCriteria.charArr = [];
      }
    })
  }

  // 显示弹框
  private showPopoverHandle () {
    if (this.readonly) {
      return
    }
    if (this.metricList.length) {
      if (this.metricTagValues && this.metricTagValues[this.metricList.join(',')]) {
        this.fieldData = this.formatFieldData(this.metricTagValues[this.metricList.join(',')]);
      } else {
        this.getFieldData()
      }
    }
    this.visible = true;
  }

  private async getFieldData () {
    const params = {
      metrics: [...this.metricList],
      by: Object.keys(this.tagLabelMap || {}),
      from: [],
    }
    this.fieldDataLoading = true
    const { result, error } = await toAsyncWait(MetricApi.getMetricTags(params))
    const data = unwrapResponseData(result) || {}
    this.fieldData = this.formatFieldData(data);
    this.fieldDataLoading = false
    this.$emit('on-tag-loaded', { [params.metrics.join(',')]: data });
  }
  private formatFieldData (data: any) {
    const fieldData: any = {}
    const list = Object.entries(data || {}).map(([key, item]: any) => ({
      value: key,
      label: key,
      options: (Array.isArray(item) ? item : []).map((t: string) => ({ label: t, value: t })),
    }))
    orderBy(list, 'value').forEach(item => {
      fieldData[item.value] = item
    })
    return fieldData;
  }

  // 清空所有条件
  private clear () {
    this.filterList = [];
    this.filterCriteria.str = '';
    this.filterCriteria.charArr = [];
    this.filterStr = '';
    this.filterStrList = [];
  }
  private clearHandle() {
    this.$confirm(i18n.t('modules.views.configManage.alarm.s_f8aae98f') as string, i18n.t('common.hint') as string, { type: 'warning' })
      .then(() => {
        this.clear();
        this.$emit('on-change', [], '');
      })
      .catch(() => null)
  }
  // 关闭弹窗，不保存条件
  private closeHandle() {
    this.$confirm(i18n.t('modules.views.configManage.alarm.s_64b179bf') as string, i18n.t('common.hint') as string, { type: 'warning' })
      .then(() => {
        this.visible = false;
        this.init();
      })
      .catch(() => null)
  }
  // 关闭弹窗，保存条件
  private confirmHandle() {
    const filterValid = this.$refs.filterCriteria.validate()
    if (!filterValid) {
      this.$message.error(i18n.t('modules.views.configManage.alarm.s_7e03cb7f') as string)
      return;
    }
    this.visible = false;
    this.filterStr = this.filterCriteria.str;
    this.filterStrList = this.filterCriteria.charArr;
    this.$emit('on-change', this.filterList, this.filterStr);
  }

  private filterCriteria: any = {
    str: '',
    charArr: [],
  }
  private conditionChangeHandle (conditions: any) {
    this.filterList = conditions.data
    this.filterCriteria.str = conditions.viewStr.replace(/ /g, '')
    this.filterCriteria.charArr = [...conditions.viewCharStrArr]
  }
}
</script>

<style lang="scss" scoped>
.query-filter-wrap {
  width: 150px;
  height: 26px;
  line-height: 26px;
  background-color: var(--bg-color);
  cursor: pointer;

  .query-filter-input {
    padding: 0 10px;
    position: relative;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    .clear {
      width: 26px;
      height: 100%;
      display: none;
      align-items: center;
      justify-content: center;
      transition: all .3s;
      position: absolute;
      right: 0;
      top: 0;
      font-size: 14px;
      color: var(--color-text-placeholder);
      &:hover {
        color: var(--color-text-secondary);
      }
    }
    &:hover {
      padding-right: 20px;
      .clear {
        display: flex;
      }
    }
  }

  .placeholder {
    padding: 0 10px;
    color: var(--color-text-placeholder);
  }
}

.metric-qf-cont {
  min-height: 121px;
  font-size: 14px;
}

.query-filter-readonly {
  background-color: var(--background-color-base);
  cursor: not-allowed;
  .query-filter-input,
  .placeholder {
    color: var(--color-text-primary);
  }
  .query-filter-input:hover .clear {
    display: none;
  }
}

.query-filter-tooltip {
  max-height: 400px;
  overflow: auto;
}
</style>
