<template>
  <div class="cont ovy-auto">
    <template v-if='showCondition'>
      <div class="fw-500 mb-10">{{ $t('modules.views.sysManage.health.s_c6761c64', { value0: typeName }) }}</div>
      <div class="mb-20">
        <matching-criteria
          ref="filterCriteria"
          :conditionData="filterConditions"
          :fieldData="fieldData"
          :maxLevel="10"
          :showView="false"
          :showCase="true"
          :atLeastOne="true"
          :showSubConfig="true"
          @on-change="conditionChangeHandle"
          class="mt-10"
        />
      </div>
    </template>
    <div class="fw-500 mb-10">{{ $t('modules.views.sysManage.health.s_15fcad7e') }}</div>
    <div class="mb-20">
      <div class="mb-10">{{ $t('modules.views.sysManage.health.s_c0d7ed96') }}</div>
      <div v-if='metricList.length' class="weight-chart-cont flex-h">
        <div class="chart-cont" ref='chartCont'></div>
      </div>
      <MetricItem v-for="(item, index) in metricList" :key="item.id || item.metric || index" :detail="item" @change='handleItemChange($event, index)' @delete='deleteItemHandle(index)' class="mb-10" />
      <div class="mt-10">
        <el-button v-show='!metricCascader.isActive' @click="handleShowMetricCascader" type="primary" plain size="small" icon='el-icon-plus'>{{ $t('modules.views.sysManage.health.s_4f341b4e') }}</el-button>
        <el-cascader
          ref="metricCascader"
          v-if='metricCascader.isActive'
          :options="metricCascader.options"
          :props="metricCascader.props"
          filterable clearable :collapse-tags='false'
          v-model="metricCascader.model"
          :placeholder="$t('modules.views.metrics.list.s_677cacb6')"
          size="small" class="w-320 mr-10"></el-cascader>
        <scroll-select
          ref="metricSelect"
          v-if='metricCascader.isActive'
          :options="metricOptions"
          filterable clearable multiple :collapse-tags='true'
          v-model="metricCascader.metricModel"
          :placeholder="$t('modules.views.appMonitor.serviceDetail.s_7e687515')"
          size="small" class="w-320 mr-10" />
        <el-button @click="addMetricHandle" v-if="metricCascader.isActive" type="primary" size="small">{{ $t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') }}</el-button>
      </div>
    </div>

    <template v-if='showDescribe'>
      <div class="fw-500 mb-10">{{ $t('modules.views.configInstall.apm.s_3bdd08ad') }}</div>
      <div class="mb-20">
        <el-input v-model='describe' type="textarea" :autosize="{ minRows: 3, maxRows: 5 }"
          :maxlength='300'
          show-word-limit></el-input>
      </div>
    </template>

    <div class="footer">
      <el-button @click="saveHandle" :loading="loading" :disabled="loading" size="small" type="primary">{{ $t('modules.views.configInstall.plugin.s_be5fbbe3') }}</el-button>
      <el-button @click="cancelHandle" :disabled="loading" size="small">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { FiledDataMap, getMetricThreshold } from './health-config';
import MatchingCriteria from '@/components/matching-criteria/index.vue';
import MetricItem from './metric-item.vue';
import { v4 as uuidv4 } from 'uuid'
import { cloneDeep } from 'lodash';
import getUnitData from '@/utils/getUnitData';
import { Cascader } from 'element-ui';
import healthApi from './health.api';
import { toAsyncWait, waitForSomeSecond } from '@/utils/common';

@Component({
  components: {
    MatchingCriteria,
    MetricItem
  }
})
export default class MetricList extends Vue {
  @Prop({}) private detail!: any;
  @Prop({}) private typeName!: string;
  @Prop({}) private cid!: any;
  @Prop({}) private type!: string;
  @Prop({ default: true }) private showCondition!: boolean;
  @Prop({ default: true }) private showDescribe!: boolean;

  @Watch('type', { immediate: true })
  private onTypeChange (val: string) {
    this.fieldData = this.getFieldData
    const types = val === 'HOST'
      ? [i18n.t('modules.views.appMonitor.traceDetail.s_1801d7de') as string, i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, undefined]
      : [i18n.t('modules.views.appMonitor.dbConnPool.s_6b522b81') as string, undefined, undefined];
    this.metricCascader.limitTypes = types;
    const typesObj = {
      type1: types[0],
      type2: types[1],
      type3: types[2],
    }
    this.metricCascader.options = this.$store.getters['Common/getMetricTypeDataByType'](typesObj) || [];
  }

  public refs!: {
    metricCascader: Cascader,
    filterCriteria: typeof MatchingCriteria,
  }

  private metricList: any[] = []

  private describe = '';
  private loading = false;
  private chart: any = null;

  get getFieldData (): any {
    return FiledDataMap?.[this.type as keyof typeof FiledDataMap] ?? {}
  }

  private fieldData: any[] = []
  private filterConditions: any[] = [];

  get metricOptions () {
    const { model, limitTypes } = this.metricCascader;
    return (this.$store.getters['Common/getMetricsByType']({
      type1: model[0] || limitTypes[0],
      type2: model[1] || limitTypes[1],
      type3: model[2] || limitTypes[2],
    }) || []).map((metric: string) => ({
      label: metric,
      value: metric,
      disabled: this.metricList.some(t => t.metric === metric),
    }));
  }

  private metricCascader: any = {
    limitTypes: [i18n.t('modules.views.appMonitor.traceDetail.s_1801d7de') as string, i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, undefined],
    model: [],
    metricModel: [],
    props: {
      checkStrictly: true,
    },
    config: {
      filterable: true,
      clearable: true,
    },
    options: [],
    isActive: false,
  }

  private async created () {
    // 格式化详情
    this.describe = this?.detail?.describe ?? '';
    try {
      const _config = JSON.parse(this.detail?.config || '{}');
      this.filterConditions = _config?.from || [];
      this.metricList = (Array.isArray(_config?.metrics) ? _config.metrics : []).map((item: any) => ({
        id: item.id || uuidv4(),
        metric: item.metric,
        zeroPointFlag: item.zeroPointFlag ?? 1,
        thresholdMin: item.thresholdMin ?? 0,
        thresholdMax: item.thresholdMax ?? 0,
        unit: item.unit || '',
        weight: item.weight ?? 50,
        rate: isNaN(Number(item.rate)) ? 0 : Number(item.rate),
        accMode: item.accMode || 'asc',
        formula: item.formula || 'y=kx+b',
      }));
      const metrics = this.metricList.map((item: any) => item.metric).filter((metric: string) => !!metric);
      if (metrics.length) {
        await this.$store.dispatch('Common/GET_METRIC_INFOS', metrics);
      }
    } catch {
      //
    }
  }
  private mounted () {
    this.drawChart();
  }

  private async conditionChangeHandle (conditions: any) {
    this.filterConditions = cloneDeep(conditions.data);
  }
  private async saveHandle () {
    const payload = {
      config: JSON.stringify({
        from: this.filterConditions,
        metrics: this.metricList,
      }),
      describe: this.describe,
    };
    // console.log('is edit', this.detail?.id, this.cid, payload.config)
    if (this.metricList.length === 0) {
      this.$message.error(i18n.t('modules.views.sysManage.health.s_48713ea5') as string);
      return;
    }
    if (this.describe.trim().length === 0 && this.showDescribe) {
      this.$message.error(i18n.t('modules.views.sysManage.health.s_615ed0e8') as string);
      return;
    }
    // @ts-ignore
    let valid = this.showCondition ? this.$refs.filterCriteria?.validate() : true;
    if (valid) {
      this.loading = true;
      const fetchApi = this.detail?.id ? healthApi.editRule : healthApi.createRule;
      const fetchParams = this.detail?.id ? { ...this.detail, ...payload } : { ...payload, id: this.cid };
      const { error } = await toAsyncWait(fetchApi(fetchParams));
      if (error && error.message !== 'interrupt') {
        this.loading = false;
        this.$message.error(error?.message || i18n.t('modules.views.cockpit.tab.s_6de920b4') as string);
        return;
      }
      this.loading = false;
      this.$emit('on-save', { ...this.detail, ...payload });
    } else {
      this.$message.error(i18n.t('modules.views.sysManage.health.s_616d6160') as string);
    }
  }
  private async cancelHandle () {
    this.$emit('on-cancel');
  }

  private async addMetricHandle () {
    if (!this.metricCascader.metricModel.length) {
      return;
    }
    await this.$store.dispatch('Common/GET_METRIC_INFOS', this.metricCascader.metricModel);
    // weight取已有数据中最小的
    const minWeight = this.metricList.reduce((min, item) => Math.min(min, item.weight || 50), 100);
    const totalWeight = this.metricList.reduce((sum, item) => sum + (item.weight || 50), 0) + (this.metricCascader.metricModel.length * minWeight);
    this.metricList.forEach(item => {
      if (!item.weight) {
        item.weight = minWeight;
      }
      item.rate = totalWeight ? ((item.weight || minWeight) / totalWeight).toFixed(6): 0;
    });
    this.metricList.push(...this.metricCascader.metricModel.map((i: string) => ({
      ...getMetricThreshold(i),
      id: uuidv4(),
      metric: i,
      zeroPointFlag: 1,
      unit: this.getMetricDefaultUnit(i),
      weight: minWeight,
      rate: ((minWeight) / totalWeight).toFixed(6),
      formula: 'y=kx+b',
    })));
    this.$nextTick(() => {
      this.metricCascader.isActive = false;
      this.metricCascader.model = [];
      this.metricCascader.metricModel = [];
      this.$emit('on-change', cloneDeep(this.metricList));
      this.drawChart();
    })
  }
  private deleteItemHandle (index: number) {
    this.metricList.splice(index, 1)
  }

  private getMetricDefaultUnit (metric: string) {
    const info = this.$store.getters['Common/metricInfoMap'][metric] || {};
    const unit = info.unit || info.unitCn || '';
    const unitData = getUnitData(unit);
    return unitData?.original_short_name || unitData?.short_name || unit || '';
  }

  private handleItemChange (payload: any, index: number) {
    if (index !== -1) {
      this.metricList.splice(index, 1, payload);
    }
    // 重新计算权重
    const totalWeight = this.metricList.reduce((sum, item) => sum + (item.weight || 50), 0);
    this.metricList.forEach(item => {
      item.rate = totalWeight ? ((item.weight || 50) / totalWeight).toFixed(6): 0;
    });
    this.$emit('on-change', cloneDeep(this.metricList));
    this.drawChart();
  }

  private resetField () {
    this.metricList = [];
    this.describe = '';
    this.filterConditions = [];
    this.metricCascader.model = [];
  }

  private handleShowMetricCascader () {
    this.metricCascader.isActive = true;
    const inputEl = (this.$refs.metricCascader as any)?.$el.querySelector('.el-input__inner') as HTMLElement;
    if (inputEl) {
      this.$nextTick(() => {
        inputEl.click();
      });
    }
  }

  public getMetricList() {
    return cloneDeep(this.metricList);
  }

  private getChartOptions () {
    const getSeries = () => {
      return this.metricList.map((i) => ({
        name: i.metric,
        type: 'bar',
        stack: 'total',
        label: {
          show: i.rate && Number(i.rate * 100) > 5.4, // 需要根据长度判断
          formatter: '{c}%',
          color: '#fff',
        },
        data: [Number(i.rate * 100).toFixed(2)]
      }))
    }
    return {
      animation: false,
      grid: {
        left: 0,
        right: 0
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
        appendToBody: true,
        // formatter: '{b}: {c}'
        valueFormatter: (value: number) => Number(value).toFixed(2) + '%'
      },
      yAxis: {
        show: false,
        type: 'category',
        data: [i18n.t('modules.views.sysManage.health.s_c0d7ed96') as string]
      },
      xAxis: {
        show: false,
        type: 'value',
      },
      series: getSeries()
    };
  }
  private drawChart () {
    if (!this.$refs.chartCont) {
      return;
    }
    if (!this.chart) {
      this.chart = this.$echarts.init(this.$refs.chartCont, '', { renderer: 'svg' });
      this.chart.setOption(this.getChartOptions(), true); // clear cache
    } else {
      this.chart.clear();
      this.chart.setOption(this.getChartOptions(), true); // clear cache
    }
  }
}
</script>

<style lang="scss" scoped>
.cont{
  height: 100%;
}
.wrapper {
  height: 100%;
}
.weight-chart-cont {
  width: 100%;
  height: 32px;
  margin-bottom: 5px;
}
.chart-cont {
  width: 100%;
  height: 100%;
}
</style>
