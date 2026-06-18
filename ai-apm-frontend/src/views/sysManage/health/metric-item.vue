<template>
  <div class="metric-item-cont border-1 br-4 flex-h-jc flex-wrap font-12">
    <div class="pos-r flex-1 p-16">
      <div class="mb-5">
        <span class="vm mr-20">{{ $t('modules.views.sysManage.health.s_829a0b02', { value0: getMetricCn }) }}</span>
        <el-checkbox v-model='params.zeroPointFlag' @change="postHandle(true)" label='zoreMode' class="vm">{{ $t('modules.views.sysManage.health.s_a9603572') }}</el-checkbox>
      </div>

      <div>
        <span class="mr-10">{{ $t('modules.views.sysManage.health.s_bee12175') }}</span>
        <span class="mr-50">{{ getKBFraction }}</span>
        <span class="mr-10">{{ $t('modules.views.sysManage.health.s_109275e1') }}</span>
        <span>{{ params.thresholdMin }} - {{ params.thresholdMax }}<template v-if='thresholdUnitLabel'> {{ thresholdUnitLabel }}</template></span>
      </div>

      <div v-show="!editMode" class="blue cp edit-btn pos-a" @click="editHandle">
        <i class="el-icon el-icon-edit mr-6"></i>
        <span>{{ $t('modules.views.hide.advancedConfig.s_95b351c8') }}</span>
      </div>
      <div v-show="!editMode" class="red cp delete-btn pos-a" @click="deleteHandle">
        <i class="el-icon el-icon-delete mr-6"></i>
        <span>{{ $t('modules.views.hide.advancedConfig.s_2f4aaddd') }}</span>
      </div>
    </div>
    <div class="bg-grey-lighter border-left-1 p-16">
      <div class="mb-5">
        <span class="mr-10">{{ $t('modules.views.sysManage.health.s_46278d1a') }}</span>
        <el-input-number v-model='params.weight' @change="postHandle(true)" size="mini" controls-position="right" :min="0" :max="100" :precision='0' :style="{width: '100px'}"></el-input-number>
      </div>
      <div>
        <span class="mr-10">{{ $t('modules.views.sysManage.health.s_4aac5591') }}</span>
        <span>{{ getRate }}</span>
      </div>
    </div>
    <div v-if='editMode' class="w-100p flex-none border-top-1 p-16 pos-r">
      <div class="mb-10">
        <span class="mr-10 describe">{{ $t('modules.views.sysManage.health.s_82450d6e') }}</span>
        <el-radio-group v-model='params.accMode'>
          <el-radio label='asc'>{{ $t('modules.utils.static.s_0f675701') }}</el-radio>
          <el-radio label='desc'>{{ $t('modules.utils.static.s_8503e74f') }}</el-radio>
        </el-radio-group>
      </div>
      <div class="mb-10">
        <span class="mr-10">{{ $t('modules.views.sysManage.health.s_e210a971') }}</span>
        <el-select value='line' size="small" style="width: 120px; margin-right: 10px;">
          <el-option :label="$t('modules.views.sysManage.health.s_9b6cb33b')" value="line"></el-option>
        </el-select>
        <code>y=kx+b</code>
      </div>
      <div class="mb-10">
        <span class="mr-10">{{ $t('modules.views.sysManage.health.s_a2aafc19') }}</span>
        <el-select value='fixed' size="small" style="width: 120px; margin-right: 10px;">
          <el-option :label="$t('modules.views.sysManage.health.s_657ed9bc')" value="fixed"></el-option>
        </el-select>
        <span class="mr-10">x0=</span>
        <el-input-number v-model='params.thresholdMin' size="mini" controls-position="right" :min="0" :max="100" :precision='0' :style="{width: '100px'}"></el-input-number>
        <span class="ml-10 mr-10">x1=</span>
        <el-input-number v-model='params.thresholdMax' size="mini" controls-position="right" :min="0" :max="200" :precision='0' :style="{width: '100px'}"></el-input-number>
        <span class="error-msg font-12 red ml-10">{{ errValidMsg }}</span>
      </div>
      <div class="mb-10">
        <span class="mr-10">{{ $t('modules.views.sysManage.health.s_33216e3b') }}</span>
        <el-select v-model='params.unit' size="small" :placeholder="$t('modules.components.s_f76a4822')" style="width: 120px;" :disabled='!unitOptions.length' @change="postHandle(true)">
          <el-option v-for="item in unitOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
        </el-select>
        <span v-if='!unitOptions.length' class="font-12 ml-10" style="color: var(--color-text-secondary);">{{ $t('modules.views.sysManage.health.s_39a3bca9') }}</span>
      </div>

      <div class="describe mb-20">
        <span v-if='params.accMode === "desc"'>{{ $t('modules.views.sysManage.health.s_9bf16319', { value0: params.thresholdMin, value1: thresholdUnitSuffix, value2: params.thresholdMax, value3: thresholdUnitSuffix, value4: getKBFraction }) }}</span>
        <span v-if='params.accMode === "asc"'>{{ $t('modules.views.sysManage.health.s_379215ca', { value0: params.thresholdMin, value1: thresholdUnitSuffix, value2: params.thresholdMax, value3: thresholdUnitSuffix, value4: getKBFraction }) }}</span>
      </div>

      <div class="">
        <el-button @click="postHandle(false)" size="small" type="primary">{{ $t('modules.views.configInstall.plugin.s_be5fbbe3') }}</el-button>
        <el-button @click="cancelHandle" size="small">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
      </div>

      <div class="preview-chart border-left-1" :class="[ params.accMode === 'asc' ? 'function-high' : 'function-low' ]"
        :style="{ backgroundImage: params.accMode === 'asc' ? `url(${this.funcHigh})` : `url(${this.funcLow})` }">
        <span class="y-label">{{ $t('modules.views.sysManage.health.s_dfe00302') }}</span>
        <span class="x-label">{{ getMetricCn }}(x)</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { StringIsEmpty } from '@/utils/common';
import i18n from '@/i18n';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import { getKandB } from './health-config';
import { getSimlarUnits } from '@/utils/getUnitData';
import getUnitData from '@/utils/getUnitData';
import Fraction from 'fraction.js';
import FunctionHigh from '@/assets/img/metrics/function_high.png';
import FunctionLow from '@/assets/img/metrics/function_low.png';

@Component
export default class MetricItem extends Vue {
  @Prop({}) private detail!: any;

  public $refs!: {
    previewChart: HTMLDivElement
  }

  private params = {
    zeroPointFlag: false,
    thresholdMin: 0,
    thresholdMax: 0,
    unit: '',
    accMode: 'asc',
    weight: 50,
    rate: 0.5,
  }

  private editMode: boolean = false;

  private funcHigh = FunctionHigh;
  private funcLow = FunctionLow;

  @Watch('detail', { immediate: true, deep: true })
  private onDetailChange (val: any) {
    this.syncParamsFromDetail(val);
  }

  get errValidMsg () {
    if (this.params.thresholdMin >= this.params.thresholdMax) {
      return i18n.t('modules.views.sysManage.health.s_92c1678f') as string;
    } else if (StringIsEmpty(this.params.thresholdMin) || StringIsEmpty(this.params.thresholdMax)) {
      return i18n.t('modules.views.sysManage.health.s_4dd3ffa6') as string;
    } else {
      return '';
    }
  }

  get getKBFraction() {
    const { k, b } = getKandB(this.params.thresholdMin, this.params.thresholdMax, this.params.accMode === 'asc');
    if (!Number.isFinite(k) || isNaN(k) || !Number.isFinite(b) || isNaN(b)) {
      return 'y=kx+b';
    }
    const kFraction = new Fraction(k).toFraction(false);
    const bFraction = new Fraction(b).toFraction(false);
    if (Number(kFraction) === 0 && Number(bFraction) !== 0) {
      return `y=${bFraction}`;
    } else if (Number(kFraction) !== 0 && Number(bFraction) === 0) {
      return `y=${kFraction}x`;
    } else if (Number(kFraction) === 0 && Number(bFraction) === 0) {
      return `y=kx+b`;
    } else {
      return `y=${kFraction}x${bFraction.indexOf('-') === 0 ? '' : '+'}${bFraction}`;
    }
  }

  get getRate () {
    let _rate = ((this.detail?.rate || 0) * 100).toFixed(2);
    _rate = _rate.replace(/\.00$/, '');
    return `${_rate}%`;
  }

  get metricInfo () {
    return this.$store.getters['Common/metricInfoMap'][this.detail?.metric] || {};
  }

  get defaultMetricUnit () {
    const unit = this.metricInfo.unit || this.metricInfo.unitCn || '';
    const unitData = getUnitData(unit);
    return unitData?.original_short_name || unitData?.short_name || unit || '';
  }

  get thresholdUnitLabel () {
    const unit = this.params.unit || this.defaultMetricUnit || '';
    const unitData = getUnitData(unit);
    return unitData?.original_short_name || unitData?.short_name || unit || '';
  }

  get thresholdUnitSuffix () {
    return this.thresholdUnitLabel ? ` ${this.thresholdUnitLabel}` : '';
  }

  get unitOptions () {
    const defaultUnit = this.defaultMetricUnit;
    if (!defaultUnit) {
      return [];
    }
    const defaultOption = {
      label: getUnitData(defaultUnit)?.original_short_name || getUnitData(defaultUnit)?.short_name || defaultUnit,
      value: defaultUnit,
    };
    const similarOptions = getSimlarUnits(defaultUnit).map((item: any) => ({
      label: item.label,
      value: item.value,
    }));
    const optionMap = new Map<string, { label: string; value: string }>();
    [defaultOption, ...similarOptions].forEach((item) => {
      if (item.value && !optionMap.has(item.value)) {
        optionMap.set(item.value, item);
      }
    });
    return Array.from(optionMap.values());
  }

  get getMetricCn () {
    return this.detail?.metric || '';
  }

  @Watch('defaultMetricUnit', { immediate: true })
  private onDefaultMetricUnitChange (val: string) {
    if (val && !this.params.unit && !this.detail?.unit) {
      this.params.unit = val;
      this.postHandle(true);
    }
  }

  private async created () {
    if (this.detail?.metric && !this.$store.getters['Common/metricInfoMap'][this.detail.metric]) {
      await this.$store.dispatch('Common/GET_METRIC_INFOS', [this.detail.metric]);
    }
  }

  private editHandle () {
    this.editMode = true;
  }
  private deleteHandle () {
    this.$emit('delete', this.detail);
  }

  private syncParamsFromDetail (detail: any = {}) {
    this.params.zeroPointFlag = !!detail?.zeroPointFlag || false;
    this.params.weight = detail?.weight || 50;
    this.params.rate = detail?.rate || 0.5;
    this.params.thresholdMin = detail?.thresholdMin || 0;
    this.params.thresholdMax = detail?.thresholdMax || 0;
    this.params.unit = detail?.unit || '';
    this.params.accMode = detail?.accMode || 'asc';
  }

  private postHandle (silence = false) {
    const payload = {
      ...this.detail,
      zeroPointFlag: Number(this.params.zeroPointFlag),
      thresholdMin: this.params.thresholdMin,
      thresholdMax: this.params.thresholdMax,
      unit: this.params.unit || this.defaultMetricUnit || '',
      weight: this.params.weight,
      accMode: this.params.accMode,
      formula: this.getKBFraction,
    };
    this.$emit('change', payload);
    if (!silence) {
      this.editMode = false;
    }
  }
  private cancelHandle () {
    this.editMode = false;
  }

  
}
</script>

<style lang="scss" scoped>
.metric-item-cont{
  overflow: hidden;
  :deep(.el-checkbox__label),
  :deep(.el-input__inner),
  :deep(.el-radio__label) {
    font-size: 12px;
  }
}
.mr-50 {
  margin-right: 50px;
}
.edit-btn {
  right: 20px;
  top: 25px;
  transform: translateY(-50%);
}
.delete-btn {
  right: 20px;
  top: 55px;
  transform: translateY(-50%);
}
.bg-grey-lighter {
  background-color: #f9f9f9;
}
.flex-wrap {
  flex-wrap: wrap;
}
.preview-chart {
  position: absolute;
  right: 1px;
  top: 1px;
  bottom: 1px;
  width: 220px;
  height: calc( 100% - 2px );
  background-size: 170px;
  background-repeat: no-repeat;
  background-position: center;
  color: var(--color-text-secondary);
  font-size: 12px;
  .y-label {
    position: absolute;
    left: 30px;
    top: 7px;
    opacity: 0.8;
  }
  .x-label {
    width: calc(100% - 20px);
    position: absolute;
    right: 10px;
    bottom: 11px;
    opacity: 0.8;
    text-align: right;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
  }
}
</style>
