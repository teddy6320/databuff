<template>
  <div class="setting-wrap flex-v" v-loading="isLoading">
    <el-form @submit.native.prevent ref="settingForm" :model="settingForm" :rules="settingRules" class="setting-form flex-1" label-position="top" size="small">
      <div class="setting-title">{{ $t('modules.views.sysManage.notice.s_934444ba') }}</div>
      <el-form-item :label="$t('modules.views.sysManage.notice.s_375d3a23')" prop="tenantEnable">
        <el-switch
          v-model="settingForm.tenantEnable"
          :active-value="1"
          :inactive-value="0"
        ></el-switch>
      </el-form-item>

      <template v-if="settingForm.tenantEnable">
        <el-form-item label="AccessKey ID" prop="smsKeyId">
          <el-input
            v-model="settingForm.smsKeyId"
            maxlength="300"
            :placeholder="$t('modules.views.sysManage.notice.s_4f4cb319')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item label="AccessKey Secret" prop="smsKeySecret">
          <el-input
            v-model="settingForm.smsKeySecret"
            maxlength="300"
            :placeholder="$t('modules.views.sysManage.notice.s_b2ff5499')"
            class="form-input"
          />
        </el-form-item>

        <div class="setting-item-title mb-5">{{ $t('modules.views.sysManage.notice.s_dbe8bad8') }}</div>
        <div class="setting-item-cont">
          <div class="temp-list">
            <div class="temp-head">
              <span>{{ $t('modules.views.sysManage.notice.s_cd82d2bb') }}</span>
              <span>{{ $t('modules.views.sysManage.notice.s_5cb25268') }}</span>
              <span>{{ $t('modules.views.sysManage.notice.s_03ae7940') }}</span>
              <span>{{ $t('modules.views.sysManage.notice.s_f32c04f9') }}</span>
            </div>
            <div v-for="(item, index) in settingForm.templates" :key="index" class="temp-item">
              <span>
                <div class="text">{{ item.smsNotifyType }}</div>
              </span>
              <span>
                <el-input
                  v-model="item.smsTemplateId"
                  size="mini"
                  maxlength="100"
                  :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')"
                  class="form-input-mini"
                />
              </span>
              <span>
                <el-input
                  v-model="item.smsTemplateContent"
                  type="textarea"
                  :autosize="{ minRows: 1, maxRows: 5 }"
                  size="mini"
                  :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')"
                  maxlength="300"
                  class="form-input-mini"
                />
              </span>
              <span>
                <el-input
                  v-model="item.smsSignName"
                  size="mini"
                  maxlength="50"
                  :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')"
                  class="form-input-mini"
                />
              </span>
            </div>
          </div>
        </div>
      </template>
    </el-form>

    <div class="pt-10">
      <el-button :disabled="postLoading || !settingForm.tenantEnable" @click="toggleTestModalHandle(true)" size="small">{{ $t('modules.views.sysManage.account.s_edb13707') }}</el-button>
      <el-button :disabled="postLoading" @click="setConfig()" type="primary" size="small">{{ $t('modules.views.hide.advancedConfig.s_74d9faed') }}</el-button>
    </div>

    <!-- 测试通知弹框 -->
    <el-dialog
      :title="$t('modules.views.sysManage.account.s_edb13707')"
      :visible.sync="showTestModal"
      width="480px"
      :before-close="() => toggleTestModalHandle()"
      :close-on-click-modal="false"
    >
      <el-form
        @submit.native.prevent
        ref="testForm"
        :model="testForm"
        :rules="testRules"
        label-width="70px"
        label-position="left"
        size="small">
        <el-form-item :label="$t('modules.views.sysManage.notice.s_fb5bf1f1')" prop="phones">
          <el-input
            v-model="testForm.phones"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 5 }"
            :placeholder="$t('modules.views.sysManage.notice.s_251ed116')"
            maxlength="300"
            class="form-input"
          />
        </el-form-item>
      </el-form>
      <template slot="footer">
        <el-button size="small" :disabled="postLoading" @click="toggleTestModalHandle()">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :loading="postLoading" @click="testNotice()">{{ $t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang='ts'>import i18n from '@/i18n';

import { Vue, Component } from 'vue-property-decorator'
import { Form } from 'element-ui'
import NoticeApi from '@/api/notice'

const phoneReg = new RegExp(/^(?:(?:\+|00)86)?1[3-9]\d{9}$/)

@Component
export default class SmsSetting extends Vue {
  get apiKey () {
    return this.$store.getters['User/getUserInfo'].cid || 'NzhEMjlBOTk3MzkxRjk5MjQyMzMzQzI4'
  }

  public $refs!: {
    settingForm: Form
    testForm: Form
  }

  private isLoading = false

  private settingForm: any = {
    id: 0,
    notifyType: 'sms',
    tenantEnable: 0,
    smsKeyId: '',
    smsKeySecret: '',
    templates: [{
      id: 0,
      smsNotifyType: i18n.t('modules.views.sysManage.notice.s_5660bcd2') as string,
      smsTemplateId: '',
      smsTemplateContent: '',
      smsSignName: '',
    }],
  }
  private settingRules = {
    tenantEnable: { required: true, message: i18n.t('modules.views.sysManage.notice.s_2f5436ed') as string, messageKey: 'modules.views.sysManage.notice.s_2f5436ed', trigger: 'change' },
    smsKeyId: { required: true, message: i18n.t('modules.views.sysManage.notice.s_4f4cb319') as string, messageKey: 'modules.views.sysManage.notice.s_4f4cb319', trigger: 'blur' },
    smsKeySecret: { required: true, message: i18n.t('modules.views.sysManage.notice.s_f9c2da88') as string, messageKey: 'modules.views.sysManage.notice.s_f9c2da88', trigger: 'blur' },
  }
  private postLoading = false

  private showTestModal = false
  private testForm = {
    phones: '',
  }
  private testRules = {
    phones: { required: true, validator : (rule: any, value: string, cb: any) => {
      value = value.trim()
      if (value) {
        const phones = value.trim().split(',')
        if (phones.slice(-1)[0] === '') {
          phones.splice(-1)
        }
        if (phones.every(t => phoneReg.test(t))) {
          cb()
        } else {
          cb(new Error(i18n.t('modules.views.dataReport.report.s_18d77151') as string))
        }
      } else {
        cb(new Error(i18n.t('modules.views.sysManage.notice.s_ff95a4ee') as string))
      }
    }, trigger: 'blur'},
  }

  private created () {
    this.getConfig()
  }

  // 获取通知配置
  private getConfig () {
    this.isLoading = true
    NoticeApi.getSmsConfig()
      .then((rst: any) => {
        if (rst && rst.status === 200) {
          const data: any = rst.data || {}
          data.notifyType = data.notifyType || 'sms'
          data.tenantEnable = data.tenantEnable || 0
          for (const key in this.settingForm) {
            if (data.hasOwnProperty(key)) {
              if (key !== 'templates') {
                this.settingForm[key] = typeof data[key] !== 'number' ? data[key] || '' : data[key]
              } else if (data.templates && data.templates.length) {
                this.settingForm.templates = data.templates.map((t: any) => ({
                  id: t.id,
                  smsNotifyType: t.smsNotifyType || '通知',
                  smsTemplateId: t.smsTemplateId,
                  smsTemplateContent: t.smsTemplateContent,
                  smsSignName: t.smsSignName,
                }))
              }
            }
          }
        } else {
          throw new Error(rst.message);
        }
      })
      .catch((err) => {
        if (err.message !== 'interrupt') {
          this.$message.error(err.message);
        }
      })
      .finally(() => this.isLoading = false)
  }
  // 设置通知配置
  private setConfig () {
    this.$refs.settingForm.validate((valid: boolean) => {
      if (!valid) {
        return
      } else if (this.settingForm.tenantEnable) {
        for (const item of this.settingForm.templates) {
          const { smsNotifyType, smsTemplateId, smsTemplateContent, smsSignName } = item
          if (!smsNotifyType) {
            this.$message.warning(i18n.t('modules.views.sysManage.notice.s_8fc85a98') as string);
            return
          } else if (!smsTemplateId) {
            this.$message.warning(i18n.t('modules.views.sysManage.notice.s_c07db9b1') as string);
            return
          } else if (!smsTemplateContent) {
            this.$message.warning(i18n.t('modules.views.sysManage.notice.s_52bedb28') as string);
            return
          } else if (!smsSignName) {
            this.$message.warning(i18n.t('modules.views.sysManage.notice.s_85727c74') as string);
            return
          }
        }
      }
      this.setSmsConfig()
    })
  }
  private setSmsConfig (test?: boolean) {
    return new Promise((resolve, reject) => {
      const params = {
        ...this.settingForm,
        apiKey: this.apiKey,
      }
      if (!params.id) {
        delete params.id
      }
      for (const item of params.templates) {
        if (!item.id) {
          delete item.id
        }
        item.apiKey = this.apiKey
        if (params.id) {
          item.notifyConfigId = params.id
        }
      }
      this.postLoading = true;
      NoticeApi.setSmsConfig(params)
        .then((rst: any) => {
          if (rst && rst.status === 200) {
            this.postLoading = false
            if (!test) {
              this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
            }
            if (!params.id) {
              this.getConfig()
            }
            resolve(rst.data);
          } else {
            throw new Error(rst.message);
          }
        })
        .catch((err) => {
          this.postLoading = false
          if (err.message !== 'interrupt') {
            this.$message.error(err.message);
          }
          reject(err.message);
        })
    })
  }

  // 显示/隐藏 测试通知 弹框
  private toggleTestModalHandle (show?: boolean) {
    if (show) {
      this.$refs.settingForm.validate((valid: boolean) => {
        if (!valid) {
          return
        } else if (!this.settingForm.tenantEnable) {
          this.$message.warning(i18n.t('modules.views.sysManage.notice.s_bcc54918') as string);
          return
        }
        for (const item of this.settingForm.templates) {
          const { smsNotifyType, smsTemplateId, smsTemplateContent, smsSignName } = item
          if (!smsNotifyType) {
            this.$message.warning(i18n.t('modules.views.sysManage.notice.s_8fc85a98') as string);
            return
          } else if (!smsTemplateId) {
            this.$message.warning(i18n.t('modules.views.sysManage.notice.s_c07db9b1') as string);
            return
          } else if (!smsTemplateContent) {
            this.$message.warning(i18n.t('modules.views.sysManage.notice.s_52bedb28') as string);
            return
          } else if (!smsSignName) {
            this.$message.warning(i18n.t('modules.views.sysManage.notice.s_85727c74') as string);
            return
          }
        }
        this.showTestModal = true;
      })
    } else {
      this.showTestModal = false;
      this.$refs.testForm.resetFields()
    }
  }
  // 测试通知
  private testNotice () {
    this.$refs.testForm.validate((valid: boolean) => {
      if (!valid) {
        return
      }
      // 测试通知需要先保存
      this.setSmsConfig(true).then(() => {
        this.postLoading = true
        const params = {
          phones: this.testForm.phones
        }
        NoticeApi.testSms(params)
          .then((rst: any) => {
            if (rst && rst.status === 200 && rst.message.toLocaleLowerCase() === 'success') {
              this.postLoading = false
              this.$message.success(i18n.t('modules.views.alarmCenter.notice.s_9db9a7e3') as string);
              this.toggleTestModalHandle()
            } else {
              throw new Error(rst.message);
            }
          })
          .catch((err) => {
            this.postLoading = false
            if (err.message !== 'interrupt') {
              this.$message.error(err.message);
            }
          })
      })
    })
  }
}
</script>

<style lang='scss' scoped>
.setting-wrap {
  .setting-title {
    margin-bottom: 10px;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
  }

  .setting-form {
    overflow: auto;

    :deep(.el-input.is-disabled .el-input__inner) {
      color: inherit;
    }

    .form-select,
    .form-input {
      display: block;
      max-width: 480px;
    }
  }
}

.setting-item-cont {
  max-width: 600px;
}
.temp-list {
  .temp-head,
  .temp-item {
    display: flex;
    box-sizing: content-box;
    font-size: 12px;
    min-height: 34px;
    span {
      width: 20%;
      display: flex;
      word-break: break-all;
      &:nth-child(2) {
        width: 25%;
      }
      &:nth-child(3) {
        width: 35%;
      }
      .text {
        width: 100%;
        display: flex;
        padding: 7px;
        border: 1px solid var(--border-color-base);
        align-items: center;
      }
    }
  }
  .temp-head {
    border-radius: 4px 4px 0 0;
    background-color: var(--border-color-lighter);
    span {
      text-indent: 8px;
      align-items: center;
    }
  }

  .form-input-mini {
    width: 100%;
    height: 100%;
    :deep(.el-input__inner),
    :deep(.el-textarea__inner) {
      max-height: 100px;
      height: 100%;
      padding: 7px;
      border-radius: 0;
      resize: none;
    }
  }
}
</style>
