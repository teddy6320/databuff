<template>
  <el-dialog
    :visible.sync="showDialog"
    :before-close="cancelHandle"
    :title="viewRemark ? $t('modules.views.alarmCenter.problemDetail.s_bd090e93') : $t('modules.views.alarmCenter.problemDetail.s_842b47e6')"
    width="480px">
    <el-form v-if="!viewRemark" ref="settingForm" :model="settingForm" :rules="settingRules" size="small" label-width="83px" class="setting-form">
      <el-form-item :label="$t('modules.views.alarmCenter.problemDetail.s_3366367d')" prop="status">
        <el-radio-group v-model="settingForm.status">
          <el-radio :label="$t('modules.views.alarmCenter.problemDetail.s_a8e10da4')">{{ $t('modules.views.alarmCenter.problemDetail.s_abdbd1fb') }}</el-radio>
          <el-radio :label="$t('modules.utils.filters.s_7030ff64')">{{ $t('modules.views.alarmCenter.problemDetail.s_fc981d1d') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="$t('modules.views.alarmCenter.problemDetail.s_2432b575')" prop="remark">
        <el-input
          v-model="settingForm.remark"
          type='textarea'
          :autosize="{ minRows: 3, maxRows: 5 }"
          :maxlength="300"
          show-word-limit clearable
          :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')"
          class="form-input" />
      </el-form-item>
    </el-form>

    <div v-else>{{ remark }}</div>

    <div slot="footer">
      <el-button size="small" :disabled="postLoading" @click="cancelHandle">{{ viewRemark ? $t('modules.views.alarmCenter.problemDetail.s_b15d9127') : $t('modules.views.alarmCenter.problemDetail.s_625fb26b')  }}</el-button>
      <el-button v-if="!viewRemark" type="primary" size="small" :loading="postLoading" @click="saveHandle">{{ $t('modules.components.dialog-template.s_38cf16f2') }}</el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Form } from 'element-ui'
import { toAsyncWait } from '@/utils/common';
import RootCauseApi from '@/api/rootCause';

@Component
export default class FeedbackDialog extends Vue {
  @Prop() private showModel!: boolean;
  @Prop({ default: '' }) private id!: string; // 问题ID
  @Prop({ default: '' }) private status!: string; // 准确性
  @Prop({ default: '' }) private remark!: string; // 备注

  public $refs!: {
    settingForm: Form
  }

  @Watch('showModel')
  private onShowModelChange (newVal: boolean) {
    this.showDialog = newVal
  }

  private showDialog = false;
  private postLoading = false;

  get viewRemark () {
    return !!this.status
  }

  private settingForm: any = {
    status: '',
    remark: '',
  }
  private settingRules: any = {
    status: { required: true, trigger: 'change', message: i18n.t('modules.views.alarmCenter.problemDetail.s_5270c47e') as string, messageKey: 'modules.views.alarmCenter.problemDetail.s_5270c47e' },
  }

  private saveHandle () {
    this.$refs.settingForm.validate(async (valid: boolean, fields: any) => {
      if (valid) {
        const params: any = {
          problemId: this.id,
          status: this.settingForm.status,
          message: this.settingForm.remark.trim(),
        }
        this.postLoading = true;
        const { result, error } = await toAsyncWait(RootCauseApi.influenceFeedback(params))
        this.postLoading = false;
        if (!error) {
          this.$message.success(i18n.t('modules.views.alarmCenter.problemDetail.s_0aaa810f') as string);
          this.cancelHandle()
          this.$emit('on-saved', params);
        } else {
          this.$message.error(error.message || i18n.t('modules.views.alarmCenter.problemDetail.s_8ee0efec') as string);
        }
      }
    })
  }

  private cancelHandle () {
    if (!this.viewRemark) {
      this.postLoading = false
      this.settingForm = {
        status: '',
        remark: '',
      }
      this.$refs.settingForm.resetFields()
    }
    this.showDialog = false
    this.$emit('on-close')
  }
}
</script>
<style lang='scss' scoped>
.setting-form {
  :deep(.el-form-item__label) {
    padding-right: 20px;
    white-space: nowrap;
  }
  :deep(.el-input.is-disabled .el-input__inner) {
    color: inherit;
  }

  .form-input,
  .form-select {
    display: block;
    width: 100%;
  }
}
</style>
