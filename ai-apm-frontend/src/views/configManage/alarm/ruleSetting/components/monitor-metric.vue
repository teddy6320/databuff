<template>
  <div class="monitor-metric-wrap" :class="{ 'readonly-wrap': readonly }">
    <div v-if="!readonly" class="config-title mb-5">
      {{ $t('modules.views.configManage.alarm.s_7e2b94e6') }}
      <el-tooltip placement="top-start" effect="light">
        <i class="db-icon-info describe"></i>
        <template slot="content">
          {{ $t('modules.views.configManage.alarm.s_bd561fed') }}
        </template>
      </el-tooltip>
    </div>
    <div v-else class="config-title mb-5">{{ $t('modules.views.configManage.alarm.s_a5f9e9b0') }}</div>

    <div
      v-for="(config, index) in configList"
      :key="config.uuid"
      class="config-item">
      <metric-query
        ref='metricQuery'
        @on-change='(...args) => queryChangeHandle(config, ...args)'
        @on-unit-change="(...args) => metricUnitChangeHandle(config, ...args)"
        :initQuery='config.query'
        :metricTypeList="metricTypeList"
        :groups="groupSelected"
        :groupList="groupList"
        :readonly="readonly"
        class="mb-10"
      />

      <div class="config-title">{{ !readonly ? $t('modules.views.configManage.alarm.s_c4ffeea6') : $t('modules.views.configManage.alarm.s_ea72c2c3')  }}</div>
      <div class="trigger-condition">
        <div class="item-cont-t">
          <div class="label w80">{{ $t('modules.views.configManage.alarm.s_84dfc0d7') }}</div>
          <el-radio-group
            v-if="!readonly"
            v-model="config.way"
            @change="(...args) => monitorTypeChangedHandle(config, ...args)"
            class="t-checkbox checkbox-group">
            <el-tooltip :content="$t('modules.views.configManage.alarm.s_0b41ba19')" placement="top" effect="light">
              <el-radio label="threshold">{{ $t('modules.views.configManage.alarm.s_f46a02b2') }}</el-radio>
            </el-tooltip>

            <el-tooltip :content="$t('modules.views.configManage.alarm.s_7ca569fc')" placement="top" effect="light">
              <el-radio label="mutation">{{ $t('modules.views.configManage.alarm.s_d64c3683') }}</el-radio>
            </el-tooltip>
          </el-radio-group>
          <span v-else>{{ config.way | MonitorMethodFilter }}</span>
        </div>

        <!-- 阈值检测 / 同环比检测 -->
        <div class="item-cont-t flex-h">
          <div class="label w80">{{ $t('modules.views.configManage.alarm.s_ea72c2c3') }}</div>{{ $t('modules.views.configManage.alarm.s_046c6233') }}
          <el-input-number
            v-model="config.period"
            @change="updateEmptyError(config.uuid, 'period', config.period)"
            :disabled="readonly"
            size="mini" :controls="false" :min="1" :max="9999" :precision="0"
            :class="{ error: (configError[config.uuid] || {}).period }"
            class="input-number w100 ml-5 mr-5"
          ></el-input-number> {{ $t('modules.views.configManage.alarm.s_941749eb') }}
          <el-select
            v-model="config.time_aggregator"
            @change="(...args) => timeAggregatorChangeHandle(config, ...args)"
            :disabled="readonly"
            filterable size="mini" :placeholder="$t('modules.views.metrics.list.s_708c9d6d')"
            class="form-select w110 ml-5">
            <el-option
              v-for="item in aggregatorOptions.filter(t => !t.type || t.type === config.way)"
              :key="item.value"
              :value="item.value" :label="item.label"></el-option>
          </el-select>

          <template v-if="config.way === 'mutation'">
            <span class="ml-5">{{ $t('modules.views.configManage.alarm.s_b10212c6') }}</span>
            <el-input-number
              v-model="config.comparePeriod"
              @change="updateEmptyError(config.uuid, 'comparePeriod', config.comparePeriod)"
              :disabled="readonly"
              size="mini" :controls="false" :min="1" :max="9999" :precision="0"
              :class="{ error: (configError[config.uuid] || {}).comparePeriod }"
              class="input-number w100 ml-5 mr-5"
            ></el-input-number> {{ $t('modules.views.configManage.alarm.s_534bf05b') }}
            <el-select
              v-model="config.fluctuate"
              @change="setMutationYoyHandle(config)"
              :disabled="readonly"
              filterable size="mini" :placeholder="$t('modules.views.metrics.list.s_708c9d6d')"
              class="form-select w110 ml-5">
              <el-option value='valUp' :label="$t('modules.views.configManage.alarm.s_b2e98969')"></el-option>
              <el-option value='valDown' :label="$t('modules.views.configManage.alarm.s_826c5cff')"></el-option>
              <el-option value='yoyUp' :label="$t('modules.views.configManage.alarm.s_c5bb8eba')"></el-option>
              <el-option value='yoyDown' :label="$t('modules.views.configManage.alarm.s_8b55088c')"></el-option>
            </el-select>
          </template>

          <template v-else-if="config.time_aggregator === 'least'">
            <el-select
              v-model="config.continuous"
              :disabled="readonly"
              filterable size="mini" :placeholder="$t('modules.views.metrics.list.s_708c9d6d')"
              class="form-select w100 ml-5 mr-5">
              <el-option :label="$t('modules.views.configManage.alarm.s_50bd9290')" :value="true"></el-option>
              <el-option :label="$t('modules.views.configManage.alarm.s_48a6b1d9')" :value="false"></el-option>
            </el-select> {{ $t('modules.views.configManage.alarm.s_01d7aa49') }}
            <el-input-number
              v-model="config.continuous_n"
              @change="updateEmptyError(config.uuid, 'continuous_n', config.continuous_n)"
              :disabled="readonly"
              size="mini" :controls="false" :precision="0"
              :min="1" :max="config.period || 9999"
              :class="{ error: (configError[config.uuid] || {}).continuous_n }"
              class="input-number w100 ml-5 mr-5"
            ></el-input-number> {{ $t('modules.views.configManage.alarm.s_3a17b735') }}
          </template>

          <el-select
            v-model="config.comparison"
            :disabled="readonly"
            filterable size="mini" :placeholder="$t('modules.views.metrics.list.s_708c9d6d')"
            class="form-select w100 ml-5 mr-5">
            <el-option v-for="item in conditionOptions" :key="item.value"
              :value="item.value" :label="item.label"></el-option>
          </el-select>
          {{ $t('modules.views.configManage.alarm.s_581c941d') }}
        </div>

        <div class="item-cont-t flex-h-start">
          <div class="label w80">{{ $t('modules.views.configManage.alarm.s_d430a15b') }}</div>
          <div>
            <div class="item-cont-t flex-h">
                <div class="item-label">
                  <span class="alert">{{ $t('modules.views.alarmCenter.alarm.s_fc7e3846') }}</span>{{ $t('modules.views.configManage.alarm.s_fd99536b') }}
                  <span class="ml-10 mr-10">{{ config.comparison }}</span>
                </div>
                <el-input
                  v-model="config.critical"
                  @change="updateInputHandle($event, config, 'critical', config.uuid)"
                  :disabled="readonly"
                  :placeholder="$t('modules.views.configManage.alarm.s_9e2a048d')" size='mini' maxlength="50"
                  :class="{ error: (configError[config.uuid] || {}).critical }"
                  class="form-select w135 mr-10"
                ></el-input>
                <el-select
                  v-if="!(configMapping[config.uuid] || {}).isYoy"
                  v-model="config.view_unit"
                  @change="(...args) => unitChangeHandle(config, ...args)"
                  :disabled="readonly || !(configMapping[config.uuid] || {}).unitSelectAble"
                  filterable size="mini" placeholder=""
                  class="form-select w100">
                  <el-option v-for="t in (configMapping[config.uuid] || {}).unitSelectList" :key="t.name" :label="t.name" :value="t.name"></el-option>
                  <el-option v-if="!(configMapping[config.uuid] || {}).unitSelectAble" :label="config.unit" :value="config.unit"></el-option>
                </el-select>
                <div v-else class="static-unit-select">%<i class="el-icon-arrow-down select-icon"></i></div>
              </div>

              <div class="item-cont-t flex-h">
                <div class="item-label">
                  <span class="warn">{{ $t('modules.views.alarmCenter.alarm.s_bde77082') }}</span>{{ $t('modules.views.configManage.alarm.s_fd99536b') }}
                  <span class="ml-10 mr-10">{{ config.comparison }}</span>
                </div>
                <el-input
                  v-model="config.warning"
                  @change="updateInputHandle($event, config, 'warning', config.uuid)"
                  :disabled="readonly"
                  :placeholder="$t('modules.views.configManage.alarm.s_9e2a048d')" size='mini' maxlength="50"
                  :class="{ error: (configError[config.uuid] || {}).warning }"
                  class="form-select w135 mr-10"
                ></el-input>
                <el-select
                  v-if="!(configMapping[config.uuid] || {}).isYoy"
                  v-model="config.view_unit"
                  @change="(...args) => unitChangeHandle(config, ...args)"
                  :disabled="readonly || !(configMapping[config.uuid] || {}).unitSelectAble"
                  filterable size="mini" placeholder=""
                  class="form-select w100">
                  <el-option v-for="t in (configMapping[config.uuid] || {}).unitSelectList" :key="t.name" :label="t.name" :value="t.name"></el-option>
                  <el-option v-if="!(configMapping[config.uuid] || {}).unitSelectAble" :label="config.unit" :value="config.unit"></el-option>
                </el-select>
                <div v-else class="static-unit-select">%<i class="el-icon-arrow-down select-icon"></i></div>
              </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { v4 as uuidv4 } from 'uuid';
import deepClone from 'lodash/cloneDeep';
import { orderBy, uniqBy } from 'lodash';
import { StringIsEmpty } from '@/utils/common';
import getUnitData from '@/utils/getUnitData';
import { resolveAvgDurationUnit } from '../../metric-unit';
import MetricQuery from './metric-query.vue';

// 保存时转换成原始单位数值
const formatThreshold = (num: any, scale: number = 1) => {
  num = num || num === 0 ? Number(num) : null;
  return scale !== 1 && typeof num === 'number' ? +(num * scale).toFixed(4) : num
}
// 回填时转换成显示单位数值
const restoreThreshold = (num: any, scale: number = 1) => {
  return (String(num) === 'null' || (!num && String(num) !== '0')) ? '' : `${+(num / scale).toFixed(4)}`
}

// 配置模版
const ConfigTemp = {
  way: 'threshold',
  query: {},
  critical: '',
  warning: '',
  unit: '',
  view_unit: '',
  _scale: 1,
  period: 5,
  time_aggregator: 'avg',
  continuous: true,
  continuous_n: 3,
  comparison: '>',
  comparePeriod: 60,
  fluctuate: 'valUp',
}

@Component({
  components: {
    MetricQuery,
  },
})
export default class MonitorMetric extends Vue {
  @Prop({ default: () => ({}) }) private initConfig!: any;
  @Prop({ default: false }) private readonly!: boolean;
  @Prop({ default: null }) private metricTypeList!: any[] | null;

  public $refs!: {
    metricQuery: MetricQuery[]
  }

  private aggregatorOptions = [
    { label: i18n.t('modules.utils.static.s_e68632a3') as string, labelKey: 'modules.utils.static.s_e68632a3', value: 'avg' },
    { label: i18n.t('modules.utils.static.s_5da89314') as string, labelKey: 'modules.utils.static.s_5da89314', value: 'max' },
    { label: i18n.t('modules.utils.static.s_c322edb8') as string, labelKey: 'modules.utils.static.s_c322edb8', value: 'min' },
    { label: i18n.t('modules.views.configManage.alarm.s_21454990') as string, labelKey: 'modules.views.configManage.alarm.s_21454990', value: 'last', type: 'threshold' },
    { label: i18n.t('modules.views.configManage.alarm.s_48c5ffed') as string, labelKey: 'modules.views.configManage.alarm.s_48c5ffed', value: 'always', type: 'threshold' },
    { label: i18n.t('modules.views.configManage.alarm.s_28b4eeb3') as string, labelKey: 'modules.views.configManage.alarm.s_28b4eeb3', value: 'sum' },
    { label: i18n.t('modules.views.configManage.alarm.s_582d4327') as string, labelKey: 'modules.views.configManage.alarm.s_582d4327', value: 'least', type: 'threshold' },
  ];
  private conditionOptions = [
    { label: i18n.t('modules.views.configManage.alarm.s_2791dca3') as string, labelKey: 'modules.views.configManage.alarm.s_2791dca3', value: '>' },
    { label: i18n.t('modules.views.configManage.alarm.s_a1d1e582') as string, labelKey: 'modules.views.configManage.alarm.s_a1d1e582', value: '>=' },
    { label: i18n.t('modules.views.configManage.alarm.s_f09dc0d1') as string, labelKey: 'modules.views.configManage.alarm.s_f09dc0d1', value: '<' },
    { label: i18n.t('modules.views.configManage.alarm.s_1ec4aae0') as string, labelKey: 'modules.views.configManage.alarm.s_1ec4aae0', value: '<=' },
  ];

  private thresholdConnectors: Record<string, string> = {
    critical: 'AND',
    warning: 'AND',
    noData: 'AND',
  }

  private configMapping: any = {} // 指标配置其他数据映射
  private configError: any = {} // 指标配置错误数据映射
  private configList: any[] = [] // 指标配置列表

  private groupSelected: string[] = [] // 已选指标分组

  get metricSelected () { // 已选指标
    const metrics: string[] = []
    this.configList.forEach(config => {
      const query = config.query
      const metricKeys = Object.keys(query).filter(key => /^[A-Z]$/.test(key))
      metrics.push(...metricKeys.map(k => query[k].metric))
    })
    return Array.from(new Set([...metrics].filter(t => !!t)))
  }
  get fieldOptions () {
    const optionsMap: any = {}
    this.metricSelected.forEach(m => {
      const tagValue = (this.$store.getters['Common/metricInfoMap'][m] || {}).tagValue || {}
      Object.entries(tagValue).forEach(([key, item]: any) => {
        optionsMap[key] = { ...(optionsMap[key] || {}), ...(item || {}) }
      })
    })
    Object.entries(optionsMap).forEach(([key, item]: any) => {
      optionsMap[key] = Object.entries(item).map(([k, v]: any) => ({ label: v, value: k }))
    })
    return optionsMap
  }
  get groupList () { // 获取指标分组的并集列表
    const lists = this.metricSelected.map(m => (this.$store.getters['Common/metricInfoMap'][m] || {})._tagKeyOptions || [])
    const list = uniqBy(lists.flat(), 'value').map(item => {
      const _item = { ...item }
      if ((this.fieldOptions[_item.value] || []).length) {
        _item.options = [...this.fieldOptions[_item.value]]
      }
      return _item
    })
    const byList = orderBy(list.filter(t => !t.tagType), 'value')
    return [
      ...byList,
      ...orderBy(list.filter(t => t.tagType), 'value'),
    ];
  }
  @Watch('groupList')
  private onGroupListChange (newVal: any[], oldVal: any[]) {
    if (newVal.map(t => t.value).join(',') !== oldVal.map(t => t.value).join(',')) {
      const byList = newVal.filter(t => !t.tagType)
      this.groupSelected = this.groupSelected.filter(t => byList.find(g => g.value === t));
    }
  }

  get queryData () {
    const data: any = {
      critical: this.thresholdConnectors.critical,
      warning: this.thresholdConnectors.warning,
      noData: this.thresholdConnectors.noData,
    }
    this.configList.forEach((config, index) => {
      const copyConfig = deepClone(config)
      let _config = {
        ...copyConfig.query,
        way: copyConfig.way,
        unit: copyConfig.unit,
        period: copyConfig.period * 60,
        no_data_timeframe: 0,
        require_full_window: false,
        evaluation_delay: 0,
      }
      if (copyConfig.way === 'mutation') {
        const isYoy = this.configMapping[copyConfig.uuid].isYoy
        const _scale = isYoy ? 0.01 : copyConfig._scale
        _config = {
          ..._config,
          thresholds: {
            critical: formatThreshold(copyConfig.critical, _scale),
            warning: formatThreshold(copyConfig.warning, _scale),
          },
          view_unit: isYoy ? '%' : copyConfig.view_unit,
          _scale,
          time_aggregator: copyConfig.time_aggregator,
          comparison: copyConfig.comparison,
          comparePeriod: copyConfig.comparePeriod * 60,
          fluctuate: copyConfig.fluctuate,
        }
      } else {
        const isYoy = this.configMapping[copyConfig.uuid]?.isYoy
        const _scale = isYoy ? 0.01 : copyConfig._scale
        _config = {
          ..._config,
          thresholds: {
            critical: formatThreshold(copyConfig.critical, _scale),
            warning: formatThreshold(copyConfig.warning, _scale),
          },
          view_unit: isYoy ? '%' : copyConfig.view_unit,
          _scale,
          time_aggregator: copyConfig.time_aggregator,
          comparison: copyConfig.comparison,
        }
        if (copyConfig.time_aggregator === 'least') {
          _config.continuous = copyConfig.continuous
          _config.continuous_n = copyConfig.continuous_n
        }
      }
      data[index + 1] = _config
    })
    return data
  }
  @Watch('queryData')
  private onQueryDataChange (newVal: any) {
    this.$emit('on-change', newVal, {}, '')
  }

  private async created () {
    const _initConfig = deepClone(this.initConfig || {});
    Object.keys(this.thresholdConnectors).forEach((key) => {
      this.thresholdConnectors[key] = _initConfig[key] || 'AND'
    });
    const formatConfig = (conf: any) => {
      let _config = deepClone(conf);
      const query: any = { unit: _config.unit }
      Object.keys(_config).forEach(key => {
        if (/^[A-Z]$/.test(key) && !_config[key]) {
          delete _config[key]
        } else if (/^[A-Z]$/.test(key)) {
          if (key === 'A') {
            query[key] = _config[key]
            delete _config[key]
            delete query[key].metrics
          } else {
            delete _config[key]
          }
        }
      });
      const thresholds = _config.thresholds || {};
      _config = {
        ..._config,
        query,
        critical: !StringIsEmpty(thresholds.critical) ? thresholds.critical : '',
        warning: !StringIsEmpty(thresholds.warning) ? thresholds.warning : '',
        _scale: _config._scale || _config.scale || 1,
      }
      delete _config.thresholds
      if (['threshold', 'mutation'].includes(_config.way)) {
        _config.critical = restoreThreshold(_config.critical, _config._scale)
        _config.warning = restoreThreshold(_config.warning, _config._scale)
        if (_config.way === 'mutation') {
          _config.comparePeriod = Math.floor(_config.comparePeriod / 60)
          const isYoy = ['yoyUp', 'yoyDown'].includes(_config.fluctuate)
          if (isYoy) {
            const unit = _config.unit || ''
            const { original_short_name } = getUnitData(unit);
            _config.view_unit = original_short_name || unit;
            _config._scale = 1;
          }
        }
      }
      _config.period = Math.floor(_config.period / 60)
      if (!this.readonly && !['threshold', 'mutation'].includes(_config.way)) {
        _config.way = 'threshold'
      }
      return _config
    }
    const configList: any[] = []
    const metrics: string[] = []
    if (_initConfig[1]) {
      const _config = formatConfig(_initConfig[1])
      configList.push({
        uuid: uuidv4(),
        ...deepClone(ConfigTemp),
        ..._config,
      })
      Object.keys(_config.query).forEach(key => {
        if (/^[A-Z]$/.test(key) && _config.query[key].metric) {
          metrics.push(_config.query[key].metric)
        }
      });
    }
    await this.$store.dispatch('Common/GET_METRIC_INFOS', metrics);
    this.configList = configList;

    this.initHandle()
  }

  private initHandle () {
    if (!this.configList.length) {
      this.configList = [{
        uuid: uuidv4(),
        ...deepClone(ConfigTemp),
      }]
    }
    this.groupSelected = []
    this.configMapping = {}
    this.configError = {}
    this.configList.forEach(t => {
      const metric = t.query?.A?.metric || ''
      const normalizedUnit = resolveAvgDurationUnit(metric, t.unit)
      if (normalizedUnit !== t.unit) {
        t.unit = normalizedUnit
        if (!t.view_unit || t.view_unit === 'ns') {
          t.view_unit = 'ms'
        }
        t._scale = 1
      }
      this.$set(this.configMapping, t.uuid, {})
      this.$set(this.configError, t.uuid, {})
      this.setMutationYoyHandle(t)
      this.metricUnitChangeHandle(t, t.unit, true)
    });
  }

  // 选择指标
  private queryChangeHandle (config: any, query: any) {
    this.$set(config, 'query', query)
    const metricKeys = Object.keys(query).filter(key => /^[A-Z]$/.test(key))
    this.groupSelected = (query[metricKeys[0]] || {}).by || []
  }
  private metricUnitChangeHandle (config: any, unit: string, init = false) {
    const metric = config.query?.A?.metric || ''
    unit = resolveAvgDurationUnit(metric, unit)
    // 指标unit和scale
    const { original_short_name, family, scale_factor } = getUnitData(unit);
    const configItem = this.configMapping[config.uuid]
    if (!init && config.unit !== unit) {
      this.$set(config, 'unit', unit)
      if (unit !== 'percent') {
        this.$set(config, 'view_unit', original_short_name || unit)
        this.$set(config, '_scale', 1)
      } else {
        this.$set(config, 'view_unit', '%')
        this.$set(config, '_scale', 1 / scale_factor)
      }
    } else if (init && unit && (!config.view_unit || config.view_unit === 'ns' && unit === 'ms')) {
      this.$set(config, 'view_unit', original_short_name || unit)
    }

    if (unit.includes('/')) {
      this.$set(this.configMapping[config.uuid], 'unitSelectAble', false)
      this.$set(this.configMapping[config.uuid], 'unitSelectList', [])
      return
    }

    const units: any = {
      time: [
        { name: 's', scale: 1 / scale_factor },
        { name: 'ms', scale: 0.001 / scale_factor },
        { name: 'µs', scale: 0.000001 / scale_factor },
        { name: 'ns', scale: 1e-9 / scale_factor },
      ],
      bytes: [
        { name: 'B', scale: 1 / scale_factor },
        { name: 'KiB', scale: 1024 / scale_factor },
        { name: 'MiB', scale: 1024 ** 2 / scale_factor },
        { name: 'GiB', scale: 1024 ** 3 / scale_factor },
        { name: 'TiB', scale: 1024 ** 4 / scale_factor },
      ],
    }
    this.$set(this.configMapping[config.uuid], 'unitSelectAble', ['bytes', 'time'].includes(family))
    this.$set(this.configMapping[config.uuid], 'unitSelectList', units[family] || [])
  }

  // 切换检测方法
  private monitorTypeChangedHandle(config: any, value: string) {
    config.critical = ''
    config.warning = ''
    config.comparison = '>'
    config.time_aggregator = 'avg'
    this.$set(this.configError, config.uuid, {})
    this.setMutationYoyHandle(config)
  }

  // 切换 time_aggregator
  private timeAggregatorChangeHandle (config: any, value: string) {
    if (value !== 'least') {
      config.continuous_n = 3
      this.$set(this.configError[config.uuid], 'continuous_n', false)
    }
  }

  // 切换 fluctuate
  private setMutationYoyHandle (config: any) {
    const { way, fluctuate } = config
    const isYoy = way === 'mutation' && ['yoyUp', 'yoyDown'].includes(fluctuate)
    this.$set(this.configMapping[config.uuid], 'isYoy', isYoy)
    if (isYoy) {
      const { critical, warning } = config
      config.critical = critical ? `${+(+critical).toFixed(2)}` : critical
      config.warning = warning ? `${+(+warning).toFixed(2)}` : warning
    }
  }

  // 输入框输入限制
  private updateInputHandle (val: string, data: any, key: string, uuid: string) {
    val = val.trim()
    let value = val === '' || isNaN(Number(val)) ? '' : `${val}`
    if ((value.split('.')[1] || '').length > 4) {
      value = `${+(+value).toFixed(4)}`
    }
    this.$set(data, key, value)
    this.updateEmptyError(uuid, key, value)
  }

  // 手动改变指标单位
  private unitChangeHandle (config: any, unit: string) {
    const configItem = this.configMapping[config.uuid]
    const unitItem = configItem.unitSelectList.find((t: any) => t.name === unit) || {}
    config._scale = unitItem.scale || 1
  }

  private updateEmptyError (uuid: string, key: string, value: any) {
    const isEmpty = (val: any) => !val && val !== 0
    const empty = isEmpty(value)
    const isThresholds = ['critical', 'warning'].includes(key)
    if (!isThresholds) {
      this.$set(this.configError[uuid], key, empty)
    } else {
      if (!empty) {
        this.$set(this.configError[uuid], 'critical', false)
        this.$set(this.configError[uuid], 'warning', false)
      } else {
        const config: any = this.configList.find(t => t.uuid === uuid) || {}
        if (isEmpty(config.critical) && isEmpty(config.warning)) {
          this.$set(this.configError[uuid], key, true)
        }
      }
    }
  }

  public validate () { // 检验通过 true，否则 false
    const isEmpty = (val: any) => !val && val !== 0
    let hasEmpty = false
    this.configList.forEach((config, index) => {
      const queryValid = this.$refs.metricQuery[index].validate()
      if (!hasEmpty && !queryValid) {
        hasEmpty = true
      }
      const _fields = this.getValidateFields(config)
      _fields.forEach((field) => {
        const value = config[field]
        this.updateEmptyError(config.uuid, field, value)
        if (['critical', 'warning'].includes(field)) {
          if (isEmpty(config.critical) && isEmpty(config.warning)) {
            hasEmpty = true
          }
        } else if (isEmpty(value)) {
          hasEmpty = true
        }
      })
    })
    return !hasEmpty
  }

  // 获取需要检验的字段
  private getValidateFields (config: any) {
    const allFields = [
      'period', 'critical', 'warning',
      'continuous_n', 'comparePeriod',
    ];
    let fields = [
      'period', 'critical', 'warning',
    ];
    if (config.way === 'mutation') {
      fields.push('comparePeriod');
    } else if (config.time_aggregator === 'least') {
      fields.push('continuous_n');
    }
    const ignoreFields = allFields.filter(t => !fields.includes(t)); // 当前配置忽略的字段
    ignoreFields.forEach(t => {
      this.$set(this.configError[config.uuid], t, false);
    });
    return fields;
  }
}
</script>

<style lang="scss" scoped>
.item-cont-t {
  margin-bottom: 6px;
  font-size: 13px;
  line-height: 28px;
  overflow: hidden;
  color: var(--color-text-regular);
  &.flex-h-start {
    display: flex;
    margin-bottom: 0;
  }
  &.lh32 {
    line-height: 32px;
  }
  &.lh24 {
    line-height: 24px;
  }
  &.font14 {
    font-size: 14px;
  }
  .label {
    float: left;
    &.w52 {
      width: 52px;
    }
    &.w80 {
      width: 80px;
      text-align: right;
    }
  }
  .cont {
    display: block;
    word-break: break-all;
    overflow: hidden;
  }
  .t-checkbox {
    vertical-align: top;
    :deep(.el-checkbox__label),
    :deep(.el-radio__label) {
      font-size: 13px;
      line-height: 28px;
    }
    &.checkbox-group {
      padding: 3px 0;
      :deep(.el-radio__label) {
        line-height: 22px;
      }
    }
    &.regular :deep(.el-checkbox__label) {
      color: var(--color-text-regular);
    }
  }
  .item-label-index {
    display: block;
    width: 70px;
    min-width: 70px;
    height: 26px;
    background: var(--border-color-base);
    border-radius: 4px;
    color: var(--color-text-regular);
    font-size: 12px;
    line-height: 26px;
    text-align: center;
  }
}

.flex-start {
  display: flex;
  align-items: flex-start;
}

.input-number {
  :deep(.el-input__inner) {
    text-align: left;
  }
}

.item-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  line-height: 28px;
  &.border {
    width: 175px;
    margin-left: 20px;
    padding-left: 20px;
    border-left: 1px solid var(--border-color-light);
  }
}

.trigger-condition {
  .input {
    width: 100px;
  }
  .select {
    width: 70px;
  }
}

.monitor-metric-wrap {
  padding-right: 16px;
  color: var(--color-text-regular);

  .form-input,
  .form-select {
    width: 320px;
  }
  .w110 {
    width: 110px;
  }
  .w100 {
    width: 100px;
  }
  .w135 {
    width: 135px;
  }
  .alert {
    color: var(--color-danger);
  }
  .warn {
    color: var(--color-warning);
  }

  .lh24 {
    line-height: 24px;
  }

  .ml-24 {
    margin-left: 24px;
  }

  .config-title {
    font-size: 14px;
    position: relative;
    .delete-btn {
      padding: 6px;
      font-size: 16px;
      position: absolute;
      top: -4px;
      right: -8px;
      cursor: pointer;
      border-radius: 3px;
      transition: color .2s ease, background-color .2s ease;
      background-color: var(--color-danger);
      color: #fff;
      &.disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }

  .error {
    :deep(.el-input__inner),
    :deep(.el-checkbox__inner) {
      border-color: var(--color-danger);
    }
  }
  .error-msg {
    display: inline-block;
    margin-left: 10px;
    color: var(--color-danger);
    font-size: 12px;
  }
}

.config-item {
  margin-bottom: 10px;
  min-width: 996px;
}

.static-unit-select {
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10px 0 15px;
  width: 100px;
  height: 28px;
  border: 1px solid var(--border-color-light);
  border-radius: 4px;
  background-color: var(--background-color-base);
  line-height: 26px;
  font-size: 12px;
  color: var(--color-text-regular);
  cursor: not-allowed;

  .select-icon {
    display: block;
    font-size: 14px;
    color: var(--color-text-placeholder);
  }
}

.readonly-wrap {
  color: var(--color-text-primary);
  .item-cont-t {
    color: var(--color-text-primary);
  }
  .t-checkbox {
    &:deep(.is-disabled .el-checkbox__input) {
      display: none;
    }
    &:deep(.is-disabled .el-checkbox__label) {
      padding-left: 0;
    }
  }
}
</style>
