<template>
  <div class="create-app-monitor">
    <div class="metric-preview"
      v-loading='graphLoading'>
      <h2 class="empty-metric-tip" v-show='chartStatus === "none" && !graphLoading'>{{ $t('modules.views.configManage.alarm.s_1612f75c') }}</h2>
      <h2 class="empty-metric-tip" v-show='chartStatus === "empty" && !graphLoading'>{{ $t('modules.components.charts.s_21efd88b') }}</h2>
      <el-carousel
        v-if="chartSource.length && chartStatus === 'success' && !graphLoading"
        :interval="2000"
        :arrow="chartSource.length > 1 ? 'hover' : 'never'"
        height="198px" trigger="click"
        :class="{ 'no-indicators': chartSource.length <= 1 }"
        class="metric-preview-carousel metric-preview-chart">
        <el-carousel-item v-for="(item, index) in chartSource" :key="`${item.name}__${index}`">
          <threshold-chart
            :source="item"
            :threshold="chartThreshold"
            :yAxisLabels="stateOptions"
            :tooltipEnterable="true"
            :tooltipConfine="true"
            :fromTime="timeParams.fromTime"
            :toTime="timeParams.toTime"
            :interval="timeParams.interval"
            :class="{ 'pb-10': chartLimited || chartSource.length > 1 }" />
          <div v-if="chartLimited" class="metric-preview-limited">{{ $t('modules.views.configManage.alarm.s_d8e78086') }}</div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <el-form ref="monitorForm" :model="monitorForm" :rules="monitorRules" size="small" label-position="left" label-width="80px" class="setting-form">
      <el-collapse class="db-setting-collapse" v-model='collapseData.value'>
        <el-collapse-item :title="$t('modules.views.configManage.alarm.s_9c7c2b06')" name='1'>
          <el-form-item :label="$t('modules.views.configManage.alarm.s_38c2bb4e')" prop="ruleName" label-width="80px">
            <el-input
              v-model="monitorForm.ruleName"
              :placeholder="$t('modules.views.configManage.alarm.s_590af8b0')"
              :minlength="2"
              :maxlength="100"
              class="form-input w550"
            ></el-input>
          </el-form-item>
          <el-form-item :label="$t('modules.views.configManage.alarm.s_bcc863fd')" prop="enabled" label-width="80px">
            <el-switch v-model="monitorForm.enabled"></el-switch>
          </el-form-item>
        </el-collapse-item>

        <el-collapse-item :title="$t('modules.views.configManage.alarm.s_d72c23b7')" name='2'>
          <monitor-metric
            ref="monitorMetric"
            :initConfig="monitorForm.query"
            :metricTypeList="isSystemRule ? metricTypeList : null"
            @on-change='queryChangeHandle'
          />
        </el-collapse-item>
      </el-collapse>
    </el-form>

    <div class="mt-20">
      <el-button :disabled="postLoading" size="small" @click="cancelMonitorHandle">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
      <el-button v-if="_hasAlarmManageAuth || !isEdit" :loading="postLoading" type="primary" size="small" @click="saveMonitorHandle">{{ $t('modules.views.configManage.alarm.s_769d88e4') }}</el-button>
    </div>
  </div>
</template>

<script lang='ts'>
import { Vue, Component, Watch, Prop } from 'vue-property-decorator'
import { Form } from 'element-ui'
import deepClone from 'lodash/cloneDeep';
import i18n from '@/i18n';
import ThresholdChart from './components/threshold-chart.vue';
import { toAsyncWait } from '@/utils/common';
import MonitorApi from '@/api/monitor'
import MetricApi from '@/api/metric';
import MonitorMetric from './components/monitor-metric.vue';
import dayjs from 'dayjs';

const unwrapResponseData = (response: any) => {
  if (response && typeof response === 'object' && Object.prototype.hasOwnProperty.call(response, 'data')) {
    return response.data
  }
  return response
}

const PREVIEW_GROUP_LIMIT = 50

@Component({
  components: {
    MonitorMetric,
    ThresholdChart,
  },
})
export default class MetricRule extends Vue {
  @Prop({ default: () => null }) private detail!: any;

  public $refs!: {
    monitorForm: Form
    metricPreviewChart: HTMLDivElement
    monitorMetric: MonitorMetric
  }

  get isSystemRule () {
    return this.$route.path === '/sysManage/ruleSetting'
  }

  get isEdit () {
    return this.detail && this.detail.id
  }
  get isCopy () {
    return this.detail && !this.detail.id
  }

  private postLoading = false

  private monitorForm: any = {
    classification: 'singleMetric',
    ruleName: '', // 规则名称
    enabled: true, // 启停状态
    query: {}, // 配置规则
  }

  get _hasAlarmManageAuth () {
    return this.hasAlarmManageAuth(this.detail || {})
  }

  get monitorRules(): any {
    const validateInputText = (rule: any, value: string, callback: any) => {
      if (!value.trim()) {
        callback(new Error(rule.message || i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string));
      } else {
        callback();
      }
    };
    return {
      ruleName: [
        { required: true, validator: validateInputText, message: i18n.t('modules.views.configManage.alarm.s_c10385d9') as string, messageKey: 'modules.views.configManage.alarm.s_c10385d9', trigger: 'blur' },
        { required: true, validator: validateInputText, message: i18n.t('modules.views.configManage.alarm.s_c10385d9') as string, messageKey: 'modules.views.configManage.alarm.s_c10385d9', trigger: 'change' },
        { min: 2, max: 100, message: i18n.t('modules.views.configManage.alarm.s_b6082c00') as string, messageKey: 'modules.views.configManage.alarm.s_b6082c00', trigger: 'blur' },
        { min: 2, max: 100, message: i18n.t('modules.views.configManage.alarm.s_b6082c00') as string, messageKey: 'modules.views.configManage.alarm.s_b6082c00', trigger: 'change' },
      ],
      enabled: { required: true, type: 'boolean', trigger: 'change', message: i18n.t('modules.views.configManage.alarm.s_5e90a580') as string, messageKey: 'modules.views.configManage.alarm.s_5e90a580' },
    }
  }

  private collapseData: any = { // 折叠面板展开控制
    value: ['1', '2'],
    1: ['ruleName', 'enabled'],
    2: [],
  }

  get metricKeys () {
    const query = this.monitorForm.query
    const list = Object.keys(query).filter(key => /^[1-5]$/.test(key))
    return list.sort()
  }

  get previewChartParams () {
    const query = deepClone(this.monitorForm.query[1] || {})
    const metric = query.A
    if (!metric) {
      return { query: {} }
    }
    return deepClone({
      query: {
        A: {
          aggs: metric.aggs || '',
          by: metric.by || [],
          metric: metric.metric || '',
          from: metric.from || [],
          types: [],
        },
      },
    })
  }
  @Watch('previewChartParams')
  private onPreviewChartParamsChange (newVal: any, oldVal: any) {
    this.$nextTick(() => {
      const changed = JSON.stringify(newVal) !== JSON.stringify(oldVal)
      const query = newVal.query || {}
      const metric = query.A || {}
      const hasEmpty = !metric.metric

      if (!changed) {
        return
      } else if (hasEmpty) {
        clearTimeout(this.graphTimer)
        this.chartStatus = 'none'
        this.chartSource = []
        this.prevGraphParams = ''
        this.graphKey += 1
        this.graphLoading = false
        return
      }

      // 切换检测方法，图表多次请求的临时处理
      this.graphTimer = setTimeout(() => {
        clearTimeout(this.graphTimer)
        this.getPreviewMetricGraph(newVal)
      }, 100)
    })
  }

  @Watch('chartThreshold.unit')
  private onPreviewChartUnitChange () {
    this.$nextTick(() => {
      this.updatePreviewGraphUnit()
    })
  }

  private created () {
    if (this.isEdit || this.isCopy) {
      this.initCreateForm()
    }
    if (this.isSystemRule) {
      this.getMetricTypeList()
    } else {
      this.$store.dispatch('Common/GET_METRIC_TYPES');
    }
  }

  private initCreateForm () {
    for (const key in this.monitorForm) {
      if (this.detail.hasOwnProperty(key)) {
        if (key === 'query') {
          this.monitorForm.query = this.detail.query || {}
        } else if (key === 'enabled') {
          this.monitorForm.enabled = this.detail.enabled === true
        } else {
          this.monitorForm[key] = this.detail[key]
        }
      }
    }
  }

  // 保存
  private saveMonitorHandle () {
    const queryValid = this.$refs.monitorMetric.validate()
    if (!queryValid || !this.monitorForm.ruleName) {
      // 滚动至上方
      document.querySelector('.setting-wrapper')!.scrollTop = 0
      if (!queryValid) {
        this.collapseData.value = [...new Set([...this.collapseData.value, '2'])]
      }
    }

    this.$refs.monitorForm.validate((valid: boolean, fields: any) => {
      if (valid) {
        if (!queryValid) {
          return;
        }
        const params: any = deepClone(this.monitorForm)
        if (this.isEdit) {
          params.id = this.detail.id
        }
        if (this.isSystemRule) {
          params.system = true
        }

        this.postLoading = true;
        const fetchUrl = this.isSystemRule ? (this.isEdit ? 'editSystemMonitor' : 'addSystemMonitor') :
            (this.isEdit ? 'editMonitor' : 'addMonitor')
        MonitorApi[fetchUrl](params)
          .then((rst: any) => {
            if (rst && rst.status === 200 && rst.message.toLowerCase() === 'success') {
              this.$message.success(this.isEdit ? i18n.t('modules.views.configInstall.dataAccess.s_55aa6366') as string : i18n.t('modules.views.configManage.alarm.s_3fdaeadf') as string);
              this.$emit('on-close');
            } else {
              throw new Error(rst.message);
            }
          })
          .catch((err) => {
            if (err.message !== 'interrupt') {
              this.$message.error(err.message);
            }
          })
          .finally(() => { this.postLoading = false })
      } else {
        const fieldList = Object.keys(fields);
        const collapseValue = [...this.collapseData.value];
        for (const key in this.collapseData) {
          const collapseItem = this.collapseData[key];
          if (key !== 'value' && fieldList.some((t) => collapseItem.includes(t))) {
            collapseValue.push(key);
          }
        }
        this.collapseData.value = [...new Set(collapseValue)];
      }
    })
  }

  // 取消
  private cancelMonitorHandle () {
    if (!this._hasAlarmManageAuth) {
      this.$emit('on-close')
      return
    }
    this.$confirm(i18n.t('modules.views.configInstall.dataAccess.s_6e1ca07e') as string, i18n.t('common.hint') as string, { type: 'warning' })
      .then(() => {
        this.$emit('on-close')
      })
  }

  // 规则配置 query change
  private queryChangeHandle (query: any, stateMetricOptions: any, stateMetric?: string) {
    this.$nextTick(() => {
      this.monitorForm.query = query
      this.stateMetricOptions = stateMetricOptions
      const _query = this.monitorForm.query[1] || {}
      this.stateMetric = stateMetric || ''
      if (stateMetric || _query.way === 'mutation') {
        this.chartThreshold = { comparison: '', unit: _query.unit }
      } else {
        const thresholds = _query.thresholds
        this.chartThreshold = {
          comparison: _query.comparison,
          critical: thresholds.critical,
          warning: thresholds.warning,
          unit: _query.unit,
        }
      }
    })
  }

  // 图表
  private graphKey: number = 0; // 图表key，防止频繁请求导致图表数据错乱
  private graphTimer: any = null
  private prevGraphParams = '' // 上一次请求的参数
  private timeParams: any = {} // 时间参数
  private graphLoading = false
  private chartSource: any[] = [];
  private chartStatus: 'none' | 'error' | 'empty' | 'success' = 'none';
  private chartThreshold: any = {}
  private chartLimited = false // 超过 PREVIEW_GROUP_LIMIT 条
  private stateMetric = ''
  private stateOptions: any[] = []
  private stateMetricOptions: any = {}
  // 获取图表数据
  private async getPreviewMetricGraph (chartParams: any) {
    const isCarousel = !!(chartParams.rule || {}).way
    const { toTime } = this.getGlobalTime()
    const params = {
      ...chartParams,
      start: +toTime.setSeconds(0) - 60 * 60 * 1000,
      end: +toTime,
      interval: 60,
    }
    if (this.isEdit) {
      params.gid = this.detail.gid
    }

    // 请求参数相同，不重复请求
    const paramsStr = JSON.stringify(params)
    if (paramsStr === this.prevGraphParams) {
      return;
    }
    this.prevGraphParams = paramsStr;

    const graphKey = this.graphKey + 1;
    this.graphKey = graphKey;
    this.graphLoading = true
    const { result, error } = await toAsyncWait(MonitorApi.getPreviewMetricGraph(params))
    if (graphKey !== this.graphKey) {
      // graphKey不一致，退出
      return;
    }
    if (error) {
      this.chartStatus = 'error';
      this.chartSource = [];
      this.prevGraphParams = '';
      this.chartLimited = false;
    } else {
      const data = (result.data || []).filter((item: any) => Array.isArray(item.values) && item.values.length);
      this.chartLimited = !isCarousel && data.length > PREVIEW_GROUP_LIMIT;
      const _data = data.slice(0, PREVIEW_GROUP_LIMIT);
      if (data.length === 0) {
        this.chartStatus = 'empty';
        this.chartSource = [];
      } else {
        this.chartStatus = 'success';
        this.timeParams = {
          fromTime: params.start,
          toTime: params.end,
          interval: params.interval,
        };
        this.chartSource = !isCarousel ? this.formatPreviewData(_data) : this.formatCarouselPreviewData(_data);
        if (this.stateMetric) {
          this.stateOptions = this.stateMetricOptions[this.stateMetric] || [];
        } else {
          this.stateOptions = [];
        }
      }
    }
    this.$nextTick(() => {
      this.graphLoading = false
    })
  }
  private updatePreviewGraphUnit () {
    this.chartSource.forEach((item: any) => {
      item.forEach((chart: any) => {
        chart.unit = this.chartThreshold.unit
      })
    })
  }

  private formatPreviewData (data: any[]) {
    const metricName = (this.previewChartParams.query.A || {}).metric || ''
    const chartData: any[] = data.map((item: any) => {
      const tag = Object.entries(item.tags || {}).map(([k, v]) => `${k}=${v || ''}`).join(';')
      return {
        name: tag || metricName,
        unit: this.chartThreshold.unit,
        data: (item.values || []).map(([key, value]: any) => ({
          key: dayjs(Number(key)).format('YYYY-MM-DD HH:mm'),
          value,
        }))
      }
    });
    return [chartData];
  }
  private formatCarouselPreviewData (data: any[]) {
    const metricName = (this.previewChartParams.query.A || {}).metric || ''
    return data.map((item: any) => {
      const tag = Object.entries(item.tags || {}).map(([k, v]) => `${k}=${v || ''}`).join(';') || metricName
      const chartData: any[] = [];
      const values = item.values || [];
      const keys: string[] = values.map(([key]: any) => dayjs(Number(key)).format('YYYY-MM-DD HH:mm'));
      (item.columns || []).forEach((col: string, idx: number) => {
        const isCritical = idx > 1 && col.indexOf('critical') > -1
        const isWarning = idx > 1 && col.indexOf('warning') > -1
        if (idx > 0) {
          chartData.push({
            name: idx === 1 ? tag : col,
            unit: this.chartThreshold.unit,
            thresholdLine: isCritical ? 'critical' : isWarning ? 'warning' : '',
            data: keys.map((key, i) => ({ key, value: (values[i] || [])[idx], })),
          })
        }
      })
      return chartData;
    });
  }

  // 获取指标分类
  private metricTypeList: any[] = [] // 指标分类列表
  private async getMetricTypeList () {
    const params: any = { system: true }
    const { result, error } = await toAsyncWait(MetricApi.getMetricTypes(params));
    if (!error) {
      this.metricTypeList = unwrapResponseData(result) || []
    }
  }
}
</script>

<style lang='scss' scoped>
.create-app-monitor {
  position: relative;

  .metric-preview{
    margin-bottom: 16px;
    min-height: 200px;
    .empty-metric-tip{
      margin: 0;
      height: 200px;
      text-align: center;
      font-size: 32px;
      line-height: 200px;
      font-weight: 300;
      user-select: none;
    }

    .metric-preview-chart{
      height: 200px;
      font-size: 0;
      border: 1px solid var(--border-color-lighter);
      background-color: var(--bg-lighter);
      border-radius: 6px;
      position: relative;
    }

    .metric-preview-limited {
      width: 100%;
      text-align: center;
      color: var(--color-text-secondary);
      font-size: 12px;
      line-height: 12px;
      pointer-events: none;
      position: absolute;
      bottom: 6px;
      left: 0;
    }
  }

  .setting-form {
    :deep(.el-input.is-disabled .el-input__inner) {
      color: inherit;
    }
    :deep(.el-input-number .el-input__inner) {
      text-align: left;
    }

    .w550 {
      width: 550px;
    }
  }
}

.metric-preview-carousel {
  &.no-indicators :deep(.el-carousel__indicators) {
    display: none;
  }
  :deep(.el-carousel__arrow) {
    background: rgba(125, 125, 125, 0.3);
    color: var(--color-text-primary);
    font-size: 20px;
    &:hover {
      background: rgba(125, 125, 125, 0.5);
    }
  }
  :deep(.el-carousel__indicators) {
    line-height: 1;
  }
  :deep(.el-carousel__indicator--horizontal) {
    padding: 6px 4px;
  }
  :deep(.el-carousel__button) {
    background: var(--color-primary);
    background-color: var(--color-text-secondary);
  }
}
</style>
