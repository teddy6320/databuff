<template>
  <el-dialog
    :visible.sync="showModal"
    :title="$t('modules.views.configManage.entity.s_ffdc810d', { value0: isEdit ? $t('modules.views.configManage.alarm.s_95b351c8') : $t('modules.views.configManage.entity.s_26bb8418') })"
    width="600px"
    :before-close="() => hide()"
    :close-on-click-modal="false"
    class="rec-config-dialog">
    <el-form
      ref="settingForm" :model="settingForm" :rules="settingRules"
      label-position="top" size="small" label-width="80px"
      @submit.native.prevent
      class="setting-form">
      <div class="pb-5">{{ $t('modules.views.configManage.entity.s_63871b1e') }}</div>
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
    </el-form>

    <div slot="footer">
      <el-button size="small" :disabled="postLoading" @click="hide()">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
      <el-button type="primary" size="small" :loading="postLoading" @click="saveHandle">{{ $t('modules.components.dialog-template.s_38cf16f2') }}</el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Form } from 'element-ui'
import { toAsyncWait } from '@/utils/common';
import ProcessApi from '@/api/process';

const symbols = {
  '=': i18n.t('modules.components.matching-criteria.s_4c35bf2e') as string,
  '!=': i18n.t('modules.components.matching-criteria.s_14a8af58') as string,
  'like': i18n.t('modules.components.matching-criteria.s_e13556bb') as string,
  'notLike': i18n.t('modules.components.matching-criteria.s_da0291f4') as string,
  'startWith': i18n.t('modules.views.configManage.alarm.s_af5c8737') as string,
  'endWith': i18n.t('modules.views.configManage.alarm.s_bd20bafe') as string,
  'regEx': i18n.t('modules.views.configManage.alarm.s_2e576047') as string,
}
const collFields = {
  processName: i18n.t('modules.views.configManage.entity.s_f0b09f88') as string,
}

// 格式化规则描述
export const formatProcessText = (data: any) => {
  const { collField, collSymbol, collValue } = data
  const _field = (collFields as any)[collField] || collField || ''
  const _symbol = (symbols as any)[collSymbol] || collSymbol || ''
  const _value = collValue || ''
  if (collSymbol === 'startWith' || collSymbol === 'endWith') {
    const [prefix, suffix] = _symbol.split('…')
    return i18n.t('modules.views.configManage.entity.s_7ab8a8ef', { value0: prefix, value1: _value, value2: suffix }) as string
  } else if (collSymbol === 'regEx') {
    return i18n.t('modules.views.configManage.entity.s_6520d72d', { value0: _symbol, value1: _value }) as string
  }
  return i18n.t('modules.views.configManage.entity.s_fbdd761e', { value0: _symbol, value1: _value }) as string
}
// 原始数据格式化为表单数据
export const formatDataToForm = (data: any) => {
  const { processName, matchProcess } = data
  return {
    collField: processName ? 'processName' : '',
    collSymbol: matchProcess || '',
    collValue: processName || '',
  }
}
// 表单数据格式化为保存数据
export const formatFormToData = (data: any) => {
  const { collField, collSymbol, collValue } = data
  return {
    processName: collField === 'processName' ? collValue : '',
    matchProcess: collSymbol,
  }
}

@Component
export default class RecConfig extends Vue {
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
    collField: 'processName',
    collSymbol: '',
    collValue: '',
  }

  private settingRules = {
    collField: { required: true, trigger: 'change' },
    collSymbol: { required: true, trigger: 'change' },
    collValue: { required: true, trigger: 'blur' },
  }

  private symbolList = Object.entries(symbols).map(([value, label]) => ({ value, label }))
  private collFieldList = Object.entries(collFields).map(([value, label]) => ({ value, label }))

  private initForm () {
    for (const key in this.settingForm) {
      if (this.detail.hasOwnProperty(key)) {
        this.settingForm[key] = this.detail[key]
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
        const params: any = formatFormToData({ ...this.settingForm })
        if (this.isEdit) {
          params.id = this.detail.id
        }

        const fetchUrl = this.isEdit ? 'updateIdentifyRule' : 'createIdentifyRule'
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

  .input-item {
    flex: 1;
  }
}
</style>
