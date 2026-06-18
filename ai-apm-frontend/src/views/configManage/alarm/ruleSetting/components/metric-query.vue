<template>
  <div class="metric-query-cont" :class="{ 'readonly-query': readonly }">
    <div v-for='(value, key) in queryList' :key='key' class="metric-query-item flex-h">
      <div class="metric-query-id">{{ key }}</div>
      <div class="metric-query-subcont line">
        <div class="query-visual-cont flex-h">
          <div class="query-visual-cont-item first">
            <div class="query-visual-cont-item-label">{{ $t('modules.views.appMonitor.serviceDetail.s_7e687515') }}</div>
            <div class="query-visual-cont-item-select">
              <metric-cascader
                v-model="value.types"
                :key="`${key}-types`"
                size='mini'
                :options="metricTypeOptions"
                :disabled='readonly'
                :awaitOption="metricTypeOptions != null"
                :placeholder="$t('modules.views.configManage.alarm.s_4473858e')"
                @change='metricTypesChangeHandle(key)'
                class="metric-custom-select"
              />
            </div>
            <div
              :class="{ 'select-error': (queryEmpty[key] || {}).metric }"
              class="query-visual-cont-item-select">
              <metric-select
                v-model="value.metric"
                :key="`${key}-metric`"
                @change="metricChangeHandle($event, key)"
                @visible-change="metricVisibleChangeHandle($event, key)"
                :options="value.metricList"
                :loading="value.loading"
                :disabled="readonly"
                :clearable="true"
                :placeholder="$t('modules.views.configManage.alarm.s_4837e0e6')"
                size="mini"
                class="metric-custom-select metric-select"
              />
              <span
                v-if="displayMetricUnit(value.metric)"
                class="metric-custom-unit">{{ $t('modules.views.configManage.alarm.s_7b2d5f03', { value0: displayMetricUnit(value.metric) | unitFilter }) }}</span>
            </div>
          </div>

          <div class="query-visual-cont-item">
            <div class="query-visual-cont-item-label">{{ $t('modules.views.configManage.alarm.s_52efbe5e') }}</div>
            <div class="query-visual-cont-item-select">
              <el-select
                v-model='value.aggs'
                :key="`${key}-aggs`"
                :disabled='readonly'
                @change='metricAggChangeHandle'
                filterable size='mini'
                :placeholder="$t('modules.views.configManage.alarm.s_539bd181')"
                class="metric-custom-select aggs-select">
                <el-option v-for='item in aggList'
                  :key='item.value'
                  :value='item.value'
                  :label='item.label'></el-option>
              </el-select>
            </div>
          </div>

          <div class="query-visual-cont-item">
            <div class="query-visual-cont-item-label">{{ $t('modules.views.appMonitor.serviceFlow.s_c2fe6253') }}</div>
            <div class="query-visual-cont-item-select">
              <query-filter
                :key="`${key}-from`"
                :initFilter='value.from'
                :metrics="metrics"
                :tagLabelMap="tagLabelMap"
                :readonly="readonly"
                :metricTagValues="metricTagValues"
                @on-change='($event) => fromChangeHandle(key, $event)'
                @on-tag-loaded="metricTagLoadedHandle"
              />
            </div>
          </div>

          <div class="query-visual-cont-item last">
            <div class="query-visual-cont-item-label">{{ $t('modules.views.infrastructure.host.s_829abe5a') }}</div>
            <div class="query-visual-cont-item-select">
              <el-select v-model='value.by' filterable size='mini'
                :key="`${key}-by`"
                multiple collapse-tags clearable
                :disabled='readonly'
                @change='groupChangeHandle'
                class="metric-custom-select multiple-select" :placeholder="$t('modules.views.configManage.alarm.s_291df01f')">
                <el-option v-for='group in byList'
                  :key='group.value'
                  :value='group.value'
                  :label='group.label'></el-option>
              </el-select>
            </div>
          </div>

          <el-tooltip v-if="!readonly" style="margin-left:2px" :content="$t('modules.views.configManage.alarm.s_88f727e5')" placement="top" effect="light">
            <i class="db-icon-info query-expression-info describe"></i>
          </el-tooltip>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang='ts'>
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import deepClone from 'lodash/cloneDeep';
import MetricApi from '@/api/metric';
import getUnitData from '@/utils/getUnitData';
import { resolveAvgDurationUnit } from '../../metric-unit';
import MetricCascader from '@/components/metric-type-cascader.vue';
import ScrollSelect from '@/components/scroll-select.vue';
import MetricSelect from '@/components/metric-select.vue';
import QueryFilter from './query-filter.vue';

const unwrapResponseData = (response: any) => {
  if (response && typeof response === 'object' && Object.prototype.hasOwnProperty.call(response, 'data')) {
    return response.data
  }
  return response
}

@Component({
  components: {
    MetricCascader,
    ScrollSelect,
    MetricSelect,
    QueryFilter,
  },
  filters: {
    unitFilter (unit: string) {
      return getUnitData(unit).original_short_name || unit;
    }
  }
})
export default class MetricQuery extends Vue {
  @Prop({ default: false }) private readonly!: boolean;
  @Prop({}) private initQuery!: any;
  @Prop({ default: null }) private metricTypeList!: any[] | null; // 指标分类列表，null 时走 store
  @Prop({ default: () => [] }) private groups!: string[]; // 已选分组
  @Prop({ default: () => [] }) private groupList!: any[]; // 分组列表

  get metricTypeOptions () {
    return this.metricTypeList != null ? this.metricTypeList : null
  }

  get isSystemRule () {
    return this.$route.path === '/sysManage/ruleSetting'
  }

  private queryList: any = {
    A: {
      types: [],
      metric: '',
      metricList: [],
      loading: false,
      from: [],
      aggs: 'avg',
      by: [],
    }
  };

  private allAggList: any[] = [
    { label: 'avg by', value: 'avg' },
    { label: 'max by', value: 'max', type: 'state' },
    { label: 'min by', value: 'min', type: 'state' },
    { label: 'sum by', value: 'sum' },
    { label: 'count by', value: 'count' },
    { label: 'last by', value: 'last' },
  ]
  get aggList () {
    if (!this.isStateMetric || this.readonly) {
      return this.allAggList
    }
    // 状态指标的聚合方式
    return this.allAggList.filter(t => t.type === 'state')
  }

  get byList () {
    return this.groupList.filter(t => !t.tagType).map(t => ({ label: t.label, value: t.value }))
  }

  get tagLabelMap () {
    const map: any = {}
    this.byList.forEach((t: any) => {
      map[t.value] = t.label
    })
    return map
  }

  private groupSelected: string[] = [];
  @Watch('groups')
  private groupsChange(val: string[]) {
    if (val.join(',') !== this.groupSelected.join(',')) {
      this.groupSelected = [...val]
      Object.entries(this.queryList).map(([key, value]: any) => {
        this.queryList[key].by = [...val];
      })
      this.$nextTick(() => {
        this.changeHandle()
      })
    }
  }

  get metricUnit () {
    const firstQuery: any = Object.values(this.queryList)[0] || {}
    const metricInfo = this.allMetricInfos[firstQuery.metric] || {}
    return resolveAvgDurationUnit(firstQuery.metric, metricInfo.unit || '')
  }
  @Watch('metricUnit')
  private metricUnitChange(val: string) {
    this.$emit('on-unit-change', val)
  }

  get metrics () {
    const metrics: string[] = Object.values(this.queryList).map((t: any) => t.metric)
    return Array.from(new Set(metrics.filter(t => !!t)))
  }

  get allMetricInfos () {
    const mapping: any = {};
    this.metrics.forEach((metric: string) => {
      const info = this.$store.getters['Common/metricInfoMap'][metric]
      if (info) {
        mapping[metric] = info
      }
    })
    return mapping
  }

  get isStateMetric () {
    return !!Object.keys(this.stateMetricOptions).length
  }
  get stateMetricOptions () {
    const metricOptions: any = {}
    this.metrics.forEach((metric: string) => {
      const item = this.allMetricInfos[metric] || {}
      if (item._isState) {
        metricOptions[metric] = item._options
      }
    })
    return metricOptions
  }
  @Watch('isStateMetric')
  private isStateMetricChange(val: boolean) {
    if (val && !this.readonly) {
      // 状态指标的聚合方式处理
      Object.entries(this.queryList).map(([key, value]: any) => {
        const aggs = value.aggs
        if (aggs && !this.aggList.find(t => t.value === aggs)) {
          this.queryList[key].aggs = this.aggList[0].value;
        }
      })
    }
  }

  private async created () {
    const _initQuery = deepClone(this.initQuery || {});
    const value = _initQuery.A || {}
    const metric = value.metric || ''
    if (metric) {
      if (!this.$store.getters['Common/metricInfoMap'][metric]) {
        await this.$store.dispatch('Common/GET_METRIC_INFOS', [metric]);
      }
      const info = this.$store.getters['Common/metricInfoMap'][metric] || {}
      if (Array.isArray(value.by) && value.by.length) {
        this.groupSelected = [...value.by]
      }
      this.$set(this.queryList, 'A', {
        ...value,
        metricList: [],
        loading: false,
        types: info._types || value.types || [],
      })
    } else {
      this.queryList.A.by = [...this.groups]
    }
    this.changeHandle(true)
  }

  private queryEmpty: any = {}
  public validate () { // 检验通过 true，否则 false
    let hasEmpty = false
    Object.entries(this.queryList).forEach(([key, value]: any) => {
      if (!value.metric) {
        hasEmpty = true
        this.$set(this.queryEmpty, key, {
          metric: true,
        })
      } else {
        this.$set(this.queryEmpty, key, {})
      }
    })
    return !hasEmpty
  }

  private metricAggChangeHandle() {
    this.$nextTick(() => {
      this.changeHandle()
    })
  }

  private groupChangeHandle (selected: string[]) {
    let _selected = selected.slice(selected.length < 3 ? 0 : -3)
    // 分组内有服务，已选服务实例名称，要加上服务名称
    const formatSelected = (list: string[], serviceKey: 'service' | 'srcService') => {
      const _list: string[] = [...list]
      const isSrc = serviceKey.indexOf('src') === 0
      const instanceKey = isSrc ? 'srcServiceInstance' : 'serviceInstance'
      const hasService = !!this.byList.find(t => t.value === serviceKey)
      if (_list.includes(instanceKey) && !_list.includes(serviceKey) && hasService) {
        if (_list.length < 3) {
          _list.push(serviceKey)
        } else {
          if (_list[0] === (isSrc ? 'service' : 'srcService')) { // 清除服务实例
            const _instanceKey = isSrc ? 'serviceInstance' : 'srcServiceInstance'
            const index = _list.indexOf(_instanceKey);
            if (index !== -1) {
              _list.splice(index, 1);
            }
          }
          _list.shift()
          if (_list.includes(instanceKey)) {
            _list.push(serviceKey)
          }
        }
      }
      return _list
    }
    _selected = formatSelected(_selected, 'service')
    _selected = formatSelected(_selected, 'srcService')
    this.groupSelected = _selected
    for (const key in this.queryList) {
      this.queryList[key].by = this.groupSelected;
    }
    this.$nextTick(() => {
      this.changeHandle()
    })
  }

  private changeHandle (init?: boolean) {
    const value: any = { ...(this.queryList.A || {}) }
    delete value.metricList
    delete value.loading
    this.$emit('on-change', deepClone({ A: value }), {
      stateMetric: Object.keys(this.stateMetricOptions)[0],
      stateMetricOptions: deepClone(this.stateMetricOptions),
      init,
    })
  }

  private metricTypesChangeHandle (key: string) {
    if (this.queryList[key].metric) {
      this.$set(this.queryEmpty, key, {
        ...(this.queryEmpty[key] || {}),
        metric: true,
      })
    }
    this.queryList[key].metric = '';
    this.queryList[key].from = [];
    this.$nextTick(() => {
      this.changeHandle()
    })
  }

  private async metricChangeHandle (metric: string, key: string) {
    if (metric && !this.$store.getters['Common/metricInfoMap'][metric]) {
      await this.$store.dispatch('Common/GET_METRIC_INFOS', [metric]);
    }
    const info = this.$store.getters['Common/metricInfoMap'][metric] || {}
    this.queryList[key].types = info._types || []
    const aggs = info.aggregatorType || ''
    if (this.aggList.find(t => t.value === aggs)) {
      this.queryList[key].aggs = aggs
    }
    // 临时处理
    const [type1, type2, type3] = this.queryList[key].types
    if (type3 === 'HTTP接口' || type3 === 'MQ消费' || type3 === 'RPC接口') {
      this.queryList[key].from = [
        { connector: 'AND', left: 'isIn', operator: '=', right: '1' }
      ]
    }
    if (!metric) {
      this.$set(this.queryEmpty, key, {
        ...(this.queryEmpty[key] || {}),
        metric: true,
      })
      this.queryList[key].from = [];
    } else if (!this.isStateMetric) {
      this.$set(this.queryEmpty, key, {
        ...(this.queryEmpty[key] || {}),
        metric: false,
      })
    } else {
      this.queryList = {
        A: {
          ...this.queryList[key],
        }
      }
      this.queryEmpty = {}
    }
    this.$nextTick(() => {
      this.changeHandle()
    })
  }

  private metricVisibleChangeHandle (visible: boolean, key: string) {
    if (visible) {
      this.getKeyMetricList(key)
    }
  }

  private fromChangeHandle (key: string, from: any[]) {
    this.queryList[key].from = from
    this.$nextTick(() => {
      this.changeHandle()
    })
  }

  private metricListMapping: any = {} // 类型对应的指标列表 Mapping
  private getKeyMetricList (key: string) {
    const typeStr = this.queryList[key].types.join(',')
    if (this.metricListMapping[typeStr]) {
      this.queryList[key].metricList = [...this.metricListMapping[typeStr]]
      return
    }
    this.queryList[key].metricList = []
    const types = this.queryList[key].types
    this.queryList[key].loading = true
    const params: any = {
      type1: types[0] || '',
      type2: types[1] || '',
      type3: types[2] || '',
    }
    if (this.isSystemRule) {
      params.system = true
    }
    Object.entries(params).forEach(([k, v]) => {
      if (v === '') {
        delete params[k]
      }
    })
    MetricApi.getMetricList(params).then((res: any) => {
      const data = unwrapResponseData(res)
      if (Array.isArray(data)) {
        this.queryList[key].metricList = data
        this.$set(this.metricListMapping, typeStr, data);
      }
    }).finally(() => {
      this.queryList[key].loading = false
    })
  }

  private metricTagValues: any = {}
  private displayMetricUnit (metric: string) {
    const info = this.allMetricInfos[metric] || {}
    const rawUnit = info.unit || ''
    const unit = resolveAvgDurationUnit(metric, rawUnit)
    if (!unit) return ''
    if (unit !== rawUnit) {
      return unit
    }
    return info.unitCn || unit
  }
  private metricTagLoadedHandle (data: any) {
    this.metricTagValues = { ...this.metricTagValues, ...data }
  }
}
</script>

<style lang="scss" scoped>
.metric-query-cont {
  padding-right: 16px;
  .metric-query-item{
    padding: 4px 0;
    max-width: 1080px;

    .metric-query-id{
      background: var(--border-color-base);
      border-radius: 4px;
      color: var(--color-text-regular);
      display: block;
      font-size: 15px;
      height: 26px;
      line-height: 26px;
      margin-right: 10px;
      position: relative;
      text-align: center;
      width: 40px;
      min-width: 40px;

      &::after{
        border-bottom: 1px solid var(--border-color-base);
        content: "";
        height: 50%;
        position: absolute;
        right: -10px;
        width: 10px;
      }
    }

    .metric-query-subcont{
      display: flex;
      align-items: center;
      flex: 1 1 auto;
      font-size: 12px;
      justify-content: space-between;
      position: relative;

      &.line::before{
        border-bottom: 1px dashed var(--border-color-base);
        bottom: 13px;
        content: "";
        display: block;
        left: 0;
        position: absolute;
        right: 0;
      }

      .query-visual-cont{
        // flex-wrap: wrap;
        position: relative;
      }

      .metric-query-tools{
        display: inline-flex;
        align-items: center;
        height: 28px;
        line-height: 28px;
        padding: 6px 7px;
        background-color: var(--bg-color03);
        color: var(--color-text-primary);
        position: relative;
        border-radius: 3px;
        font-size: 16px;
        cursor: pointer;
        transition: color .2s ease, background-color .2s ease;
        &:hover{
          background-color: var(--color-danger);
          color: #fff;
        }
      }
    }
  }

  .query-visual-cont-item{
    display: inline-flex;
    // flex-flow: row wrap;
    position: relative;
    vertical-align: middle;

    &.first .query-visual-cont-item-label {
      border-left-width: 1px;
      border-radius: 4px 0 0 4px;
    }
    &.last .query-visual-cont-item-select {
      border-radius: 0 4px 4px 0;
    }

    .query-visual-cont-item-label{
      min-width: 40px;
      border: 1px solid var(--border-color-base);
      border-left-width: 0;
      background: var(--bg-color03);
      border-right: none;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .query-visual-cont-item-select{
      background: var(--bg-color03);
      border: 1px solid var(--border-color-base);
      border-radius: 0;
      height: 28px;
      transition: border-color .3s ease;
      overflow: hidden;
      position: relative;
      white-space: nowrap;
      & + .query-visual-cont-item-select {
        margin-left: -1px;
      }

      &:hover{
        border-color: var(--color-text-secondary);
        z-index: 1;
      }
      &.select-error {
        border-color: var(--color-danger) !important;
      }
    }
  }

  .metric-custom-select{
    width: 150px;
    line-height: 26px;
    :deep(.el-input__inner){
      border: none;
      padding: 0 20px 0 10px;
      line-height: 26px;
      height: 26px;
      border-radius: 0;
      text-overflow: ellipsis;
    }
    :deep(.el-input__suffix){
      right: 0;
      z-index: 1;
    }
    :deep(.el-input__suffix .el-icon-arrow-up),
    :deep(.el-input__suffix .el-icon-arrow-down) {
      display: none;
    }
    &.multiple-select {
      width: 190px;
      :deep(.el-select__input) {
        margin-left: 10px;
        margin-right: -25px;
      }
      :deep(.el-select__tags) {
        flex-wrap: nowrap;
      }
    }
    &.aggs-select {
      width: 80px;
      :deep(.el-input__inner){
        padding-right: 10px;
      }
    }
    &.metric-select {
      width: 180px;
    }
  }

  .metric-custom-input {
    width: 150px;
    line-height: 26px;
    :deep(.el-input__inner){
      border: none;
      padding: 0 10px;
      line-height: 26px;
      height: 26px;
      border-radius: 0;
      text-overflow: ellipsis;
    }
  }

  .metric-custom-unit {
    padding: 0 5px;
    line-height: 26px;
    white-space: nowrap;
  }
}

.readonly-query {
  .metric-custom-select {
    width: 160px;
    &.multiple-select {
      width: 180px;
    }
    :deep(.el-select__tags .el-select__input[disabled]){
      display: none;
    }
  }
  .metric-query-subcont.line::before {
    visibility: hidden;
  }
}
.icon-from-missing {
  font-size: 15px;
  color: var(--color-danger);
  cursor: help;
}
.query-missing-tag {
  word-break: break-all;
}
</style>

<style lang="scss">
.query-visual-cont-item-select.el-loading-parent--relative .el-loading-spinner {
  margin-top: -14px;
}
</style>
