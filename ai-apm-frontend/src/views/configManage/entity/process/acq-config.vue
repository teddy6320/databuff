<template>
  <el-dialog
    :visible.sync="showModal"
    :title="$t('modules.views.configManage.entity.s_49e60e59', { value0: isEdit ? $t('modules.views.configManage.alarm.s_95b351c8') : $t('modules.views.configManage.entity.s_26bb8418') })"
    width="600px"
    :before-close="() => hide()"
    :close-on-click-modal="false"
    class="acq-config-dialog">
    <el-form
      ref="settingForm" :model="settingForm" :rules="settingRules"
      label-position="top" size="small" label-width="80px"
      @submit.native.prevent
      class="setting-form">
      <el-form-item prop="monitorType" :show-message="false">
        <el-radio-group v-model="settingForm.monitorType">
          <el-radio v-for="t in typeList" :key="t.value"
            :label="t.value" class="radio mr-30">{{ t.labelKey ? $t(t.labelKey) : t.label }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <div class="pb-5">{{ $t('modules.views.configManage.entity.s_df0d323b') }}</div>
      <div class="flex-h">
        <el-form-item prop="collField" class="select-item" :show-message="false">
          <el-select
            v-model="settingForm.collField"
            filterable size="small">
            <el-option v-for="t in collFieldList" :key="t.value"
              :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>

        <el-form-item prop="collSymbol" class="select-item" :show-message="false">
          <el-select
            v-model="settingForm.collSymbol"
            filterable size="small">
            <el-option v-for="t in symbolList" :key="t.value"
              :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>

        <el-form-item prop="collValue" class="input-item" :show-message="false">
          <el-input
            v-model="settingForm.collValue"
            :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')" />
        </el-form-item>
      </div>

      <div class="pb-5">{{ $t('modules.views.configManage.entity.s_aa329fe2') }}</div>
      <div class="flex-h">
        <el-form-item prop="hostRange" class="select-item" :show-message="false">
          <el-select
            v-model="settingForm.hostRange"
            filterable size="small">
            <el-option v-for="t in hostRangeList" :key="t.value" :disabled='!isAdmin && t.value === "all"'
              :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>

        <template v-if="settingForm.hostRange === 'filter'">
          <el-form-item prop="hostField" class="select-item" :show-message="false">
            <el-select
              v-model="settingForm.hostField"
              filterable size="small">
              <el-option v-for="t in hostFieldList" :key="t.value"
                :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>

          <el-form-item prop="hostSymbol" class="select-item" :show-message="false">
            <el-select
              v-if="!isHostTag"
              v-model="settingForm.hostSymbol"
              filterable size="small">
              <el-option v-for="t in symbolList" :key="t.value"
                :label="t.label" :value="t.value" />
            </el-select>

            <el-input
              v-else
              v-model="settingForm.hostSymbol"
              :placeholder="$t('modules.views.configManage.entity.s_07cdb606')"
              class="tag-key-input" />
            <span v-if="isHostTag" class="tag-split">:</span>
          </el-form-item>

          <el-form-item prop="hostValue" class="input-item" :show-message="false">
            <el-input
              v-model="settingForm.hostValue"
              :placeholder="!isHostTag ? $t('modules.views.configInstall.dataAccess.s_02cc4f8f') : $t('modules.views.configManage.entity.s_47b8f029')"
            />
          </el-form-item>
        </template>
      </div>
    </el-form>

    <div slot="footer">
      <el-button size="small" :disabled="postLoading" @click="hide()">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
      <el-button type="primary" size="small" :loading="postLoading" @click="saveHandle">{{ $t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') }}</el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Form } from 'element-ui'
import { toAsyncWait } from '@/utils/common';
import ProcessApi from '@/api/process';

const monitorTypes = {
  monitor: i18n.t('modules.views.configManage.entity.s_9aff6241') as string,
  notMonitor: i18n.t('modules.views.configManage.entity.s_a7b2a175') as string,
}
const symbols = {
  '=': i18n.t('modules.components.matching-criteria.s_4c35bf2e') as string,
  // '!=': i18n.t('modules.components.matching-criteria.s_14a8af58') as string,
  'like': i18n.t('modules.components.matching-criteria.s_e13556bb') as string,
  // 'notLike': i18n.t('modules.components.matching-criteria.s_da0291f4') as string,
  'startWith': i18n.t('modules.views.configManage.alarm.s_af5c8737') as string,
  'endWith': i18n.t('modules.views.configManage.alarm.s_bd20bafe') as string,
  'regEx': i18n.t('modules.views.configManage.alarm.s_2e576047') as string,
}
const collFields = {
  processName: i18n.t('modules.views.configManage.entity.s_f0b09f88') as string,
  appPath: i18n.t('modules.views.configManage.entity.s_77507a66') as string,
}
const hostRanges = {
  all: i18n.t('modules.views.configManage.entity.s_a6cc4b13') as string,
  filter: i18n.t('modules.views.configManage.entity.s_22aeb1d2') as string,
}
const hostFields = {
  hostName: i18n.t('modules.views.configManage.alarm.s_3d022a63') as string,
  hostIp: i18n.t('modules.views.configManage.entity.s_2dc9105c') as string,
  hostTag: i18n.t('modules.views.configManage.entity.s_5e17f4d3') as string,
}

// 格式化规则描述
export const formatProcessText = (data: any) => {
  const { monitorType, collField, collSymbol, collValue } = data
  const _type = (monitorTypes as any)[monitorType] || monitorType || ''
  const _field = (collFields as any)[collField] || collField || ''
  const _symbol = (symbols as any)[collSymbol] || collSymbol || ''
  const _value = collValue || ''
  if (collSymbol === 'startWith' || collSymbol === 'endWith') {
    const [prefix, suffix] = _symbol.split('…')
    return i18n.t('modules.views.configManage.entity.s_370e65ee', { value0: _type, value1: _field, value2: prefix, value3: _value, value4: suffix }) as string
  } else if (collSymbol === 'regEx') {
    return i18n.t('modules.views.configManage.entity.s_5c494b61', { value0: _type, value1: _field, value2: _symbol, value3: _value }) as string
  }
  return i18n.t('modules.views.configManage.entity.s_2ab09fec', { value0: _type, value1: _field, value2: _symbol, value3: _value }) as string
}
// 格式化主机范围
export const formatHostText = (data: any) => {
  const { hostRange, hostField, hostSymbol, hostValue } = data
  if (hostRange === 'all') {
    return hostRanges.all
  } else {
    const _field = (hostFields as any)[hostField] || hostField || ''
    const _symbol = (symbols as any)[hostSymbol] || hostSymbol || ''
    const _value = hostValue || ''
    if (hostSymbol === 'startWith' || hostSymbol === 'endWith') {
      const [prefix, suffix] = _symbol.split('…')
      return i18n.t('modules.views.configManage.entity.s_9fc284ad', { value0: _field, value1: prefix, value2: _value, value3: suffix }) as string
    } else if (hostSymbol === 'regEx') {
      return i18n.t('modules.views.configManage.entity.s_e6782983', { value0: _field, value1: _symbol, value2: _value }) as string
    }
    return i18n.t('modules.views.configManage.entity.s_1a915831', { value0: _field, value1: _symbol, value2: _value }) as string
  }
}
// 原始数据格式化为表单数据
export const formatDataToForm = (data: any) => {
  const { type, processName, processPath, matchProcess, hostname, ip, matchHost, tags } = data
  const tagKeyValue = Object.entries(tags || {})[0]
  const hostFilter = !!(hostname || ip || matchHost || tagKeyValue)
  return {
    monitorType: type === 1 ? 'notMonitor' : 'monitor',
    collField: processName ? 'processName' : processPath ? 'appPath' : '',
    collSymbol: matchProcess || '',
    collValue: processName || processPath || '',
    hostRange: hostFilter ? 'filter' : 'all',
    hostField: !hostFilter ? '' : hostname ? 'hostName' : ip ? 'hostIp' : tagKeyValue ? 'hostTag' : '',
    hostSymbol: hostFilter && !tagKeyValue ? matchHost || '' : tagKeyValue ? tagKeyValue[0] || '' : '',
    hostValue: hostFilter && !tagKeyValue ? hostname || ip || '' : tagKeyValue ? tagKeyValue[1] || '' : '',
  }
}
// 表单数据格式化为保存数据
export const formatFormToData = (data: any) => {
  const { monitorType, collField, collSymbol, collValue, hostRange, hostField, hostSymbol, hostValue } = data
  const hostFilter = hostRange === 'filter'
  return {
    type: monitorType === 'notMonitor' ? 1 : 0,
    processName: collField === 'processName' ? collValue : '',
    processPath: collField === 'appPath' ? collValue : '',
    matchProcess: collSymbol,
    hostname: hostFilter && hostField === 'hostName' ? hostValue : '',
    ip: hostFilter && hostField === 'hostIp' ? hostValue : '',
    matchHost: hostFilter && hostField !== 'hostTag' ? hostSymbol : '',
    tags: hostFilter && hostField === 'hostTag' ? { [hostSymbol]: hostValue } : null,
  }
}

@Component
export default class AcqConfig extends Vue {
  @Prop({ default: () => null }) private detail!: any;

  public $refs!: {
    settingForm: Form
  }

  get isEdit () {
    return this.detail && this.detail.id
  }

  private showModal = false
  private postLoading = false

  private settingForm: any = {
    monitorType: 'monitor',
    collField: '',
    collSymbol: '',
    collValue: '',
    hostRange: 'all',
    hostField: '',
    hostSymbol: '',
    hostValue: '',
  }

  private settingRules = {
    monitorType: { required: true, trigger: 'change' },
    collField: { required: true, trigger: 'change' },
    collSymbol: { required: true, trigger: 'change' },
    collValue: { required: true, trigger: 'blur' },
    hostRange: { required: true, trigger: 'change' },
    hostField: { required: true, trigger: 'change' },
    hostSymbol: { required: true, trigger: 'change' },
    hostValue: { required: true, trigger: 'blur' },
  }

  private typeList = Object.entries(monitorTypes).map(([value, label]) => ({ value, label }))
  private symbolList = Object.entries(symbols).map(([value, label]) => ({ value, label }))
  private collFieldList = Object.entries(collFields).map(([value, label]) => ({ value, label }))
  private hostRangeList = Object.entries(hostRanges).map(([value, label]) => ({ value, label }))
  private hostFieldList = Object.entries(hostFields).map(([value, label]) => ({ value, label }))

  // 受角色影响选择范围

  get isHostTag () {
    return this.settingForm.hostField === 'hostTag'
  }

  private initForm () {
    if (!this.isEdit) {
      if (!this.isAdmin) {
        this.settingForm.hostRange = 'filter'
      }
    } else {
      for (const key in this.settingForm) {
        if (this.detail.hasOwnProperty(key)) {
          if ((!this.isAdmin && key === 'hostRange' && this.detail[key] === 'all')) {
            this.settingForm[key] = 'filter'
          } else {
            this.settingForm[key] = this.detail[key]
          }
        }
      }
    }
  }

  public show () {
    this.showModal = true;
    this.$nextTick(() => {
      this.initForm()
    })
  }

  private hide () {
    this.showModal = false;
    this.$refs.settingForm.resetFields();
  }

  private saveHandle () {
    this.$refs.settingForm.validate(async (valid: boolean, fields: any) => {
      if (valid) {
        let params: any = { ...this.settingForm }
        if (this.settingForm.hostRange === 'all') {
          delete params.hostField
          delete params.hostSymbol
          delete params.hostValue
        }
        params = formatFormToData(params)
        if (this.isEdit) {
          params.id = this.detail.id
        }

        const fetchUrl = this.isEdit ? 'updateCollectRule' : 'createCollectRule'
        this.postLoading = true;
        const { result, error } = await toAsyncWait(ProcessApi[fetchUrl](params))
        this.postLoading = false;
        if (!error) {
          this.$message.success(this.isEdit ? i18n.t('modules.views.configInstall.dataAccess.s_55aa6366') as string : i18n.t('modules.views.configManage.alarm.s_3fdaeadf') as string);
          this.hide();
          this.$emit('on-saved', params);
        } else {
          this.$message.error(error.message || i18n.t('modules.views.cockpit.tab.s_6de920b4') as string);
        }
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.setting-form {
  .mr-30 {
    margin-right: 30px;
  }

  .radio {
    line-height: 24px;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 4px;
    line-height: 24px;
  }
  :deep(.el-form-item__content) {
    line-height: 24px;
    display: flex;
    align-items: center;
  }

  .select-item {
    margin-right: 10px;
    width: 110px;
    :deep(.el-select .el-input__inner) {
      padding-right: 20px;
    }
  }

  .tag-split {
    display: block;
    width: 4px;
    position: absolute;
    right: -6px;
  }

  .input-item {
    flex: 1;
  }
}
</style>
