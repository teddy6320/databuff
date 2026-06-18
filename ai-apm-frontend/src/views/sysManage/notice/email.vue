<template>
  <div class="setting-wrap flex-v" v-loading="isLoading">
    <el-form @submit.native.prevent ref="settingForm" :model="settingForm" :rules="settingRules" class="setting-form flex-1" label-position="top" size="small">
      <div class="setting-title">{{ $t('modules.views.sysManage.notice.s_bdf0097c') }}</div>
      <el-form-item :label="$t('modules.views.sysManage.notice.s_375d3a23')" prop="tenantEnable">
        <el-switch
          v-model="settingForm.tenantEnable"
          :active-value="1"
          :inactive-value="0"
        ></el-switch>
      </el-form-item>

      <template v-if="settingForm.tenantEnable">
        <el-form-item :label="$t('modules.views.sysManage.notice.s_5d57821d')" prop="mailHost">
          <el-input
            v-model="settingForm.mailHost"
            maxlength="100"
            :placeholder="$t('modules.views.sysManage.notice.s_44056c2f')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item :label="$t('modules.views.deployInstall.apm.s_faa1ad5e')" prop="mailProtocol">
          <el-select
            v-model="settingForm.mailProtocol"
            :placeholder="$t('modules.views.sysManage.notice.s_b4f874c7')" disabled
            class="form-select"
          >
            <el-option label="SMTP" value="SMTP"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('modules.views.sysManage.notice.s_e180511c')" prop="mailSender">
          <el-input
            v-model="settingForm.mailSender"
            maxlength="100"
            :placeholder="$t('modules.views.sysManage.notice.s_e779a3a2')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item :label="$t('modules.views.sysManage.notice.s_fcaed9ec')" prop="mailSenderPwd">
          <el-input
            v-model="settingForm.mailSenderPwd"
            type="password"
            show-password
            autocomplete="new-password"
            maxlength="100"
            :placeholder="$t('modules.views.sysManage.notice.s_89bfea77')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item :label="$t('modules.views.sysManage.notice.s_269099e4')" prop="mailSecretCode">
          <el-input
            v-model="settingForm.mailSecretCode"
            maxlength="100"
            :placeholder="$t('modules.views.sysManage.notice.s_04890294')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item :label="$t('modules.views.deployInstall.otelCollector.s_c76cfefe')" prop="mailPort">
          <el-input
            v-model="settingForm.mailPort"
            @change="portChangeHandle"
            maxlength="5"
            :placeholder="$t('modules.views.sysManage.notice.s_bc0eb7d7')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item :label="$t('modules.views.sysManage.notice.s_e62dd74c')" prop="mailSsl">
          <el-radio-group v-model="settingForm.mailSsl">
            <el-radio :label="1">{{ $t('modules.views.help.startGuide.s_7854b52a') }}</el-radio>
            <el-radio :label="0">{{ $t('modules.views.aiPlatform.experts.s_5c56a889') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item :label="$t('modules.views.sysManage.notice.s_e48312f8')" prop="mailSubject">
          <el-input
            v-model="settingForm.mailSubject"
            maxlength="200"
            :placeholder="$t('modules.views.sysManage.notice.s_6168dd7c')"
            class="form-input"
          />
        </el-form-item>
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
        <el-form-item :label="$t('modules.views.sysManage.notice.s_fb5bf1f1')" prop="toEmails">
          <el-input
            v-model="testForm.toEmails"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 5 }"
            :placeholder="$t('modules.views.sysManage.notice.s_59112eb7')"
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

const emailReg = new RegExp(/^(([^*&$%》《【】\^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/)

@Component
export default class EmailSetting extends Vue {
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
    notifyType: 'mail',
    tenantEnable: 0,
    mailHost: '',
    mailProtocol: 'SMTP',
    mailSender: '',
    mailSenderPwd: '',
    mailSecretCode: '',
    mailPort: '',
    mailSsl: 0,
    mailSubject: '',
  }
  private settingRules = {
    tenantEnable: { required: true, message: i18n.t('modules.views.sysManage.notice.s_2f5436ed') as string, messageKey: 'modules.views.sysManage.notice.s_2f5436ed', trigger: 'change' },
    mailHost: { required: true, message: i18n.t('modules.views.sysManage.notice.s_44056c2f') as string, messageKey: 'modules.views.sysManage.notice.s_44056c2f', trigger: 'blur' },
    mailProtocol: { required: true, message: i18n.t('modules.views.sysManage.notice.s_b4f874c7') as string, messageKey: 'modules.views.sysManage.notice.s_b4f874c7', trigger: 'change' },
    mailSender: { required: true, validator : (rule: any, value: any, cb: any) => {
      value = value.trim()
      if (value) {
        if (emailReg.test(value)) {
          cb()
        } else {
          cb(new Error(i18n.t('modules.views.dataReport.report.s_6a5f045c') as string))
        }
      } else {
        cb(new Error(i18n.t('modules.views.sysManage.notice.s_e779a3a2') as string))
      }
    }, trigger: 'blur'},
    mailSenderPwd: { required: true, message: i18n.t('modules.views.sysManage.notice.s_89bfea77') as string, messageKey: 'modules.views.sysManage.notice.s_89bfea77', trigger: 'blur' },
    mailSecretCode: { required: false, message: i18n.t('modules.views.sysManage.notice.s_04890294') as string, messageKey: 'modules.views.sysManage.notice.s_04890294', trigger: 'blur' },
    mailPort: { required: true, message: i18n.t('modules.views.sysManage.notice.s_bc0eb7d7') as string, messageKey: 'modules.views.sysManage.notice.s_bc0eb7d7', trigger: 'blur' },
    mailSsl: { required: true, message: i18n.t('modules.views.sysManage.notice.s_d8a63cef') as string, messageKey: 'modules.views.sysManage.notice.s_d8a63cef', trigger: 'change' },
    mailSubject: { required: true, message: i18n.t('modules.views.sysManage.notice.s_57d45b57') as string, messageKey: 'modules.views.sysManage.notice.s_57d45b57', trigger: 'blur' },
  }
  private postLoading = false

  private showTestModal = false
  private testForm = {
    toEmails: '',
  }
  private testRules = {
    toEmails: { required: true, validator : (rule: any, value: string, cb: any) => {
      value = value.trim()
      if (value) {
        const emails = value.trim().split(',')
        if (emails.slice(-1)[0] === '') {
          emails.splice(-1)
        }
        if (emails.every(t => emailReg.test(t))) {
          cb()
        } else {
          cb(new Error(i18n.t('modules.views.dataReport.report.s_6a5f045c') as string))
        }
      } else {
        cb(new Error(i18n.t('modules.views.sysManage.notice.s_e779a3a2') as string))
      }
    }, trigger: 'blur'},
  }

  private created () {
    this.getConfig()
  }

  // 获取通知配置
  private getConfig () {
    this.isLoading = true
    NoticeApi.getEmailConfig()
      .then((rst: any) => {
        if (rst && rst.status === 200) {
          const data: any = rst.data || {}
          data.notifyType = data.notifyType || 'mail'
          data.tenantEnable = data.tenantEnable || 0
          data.mailSsl = data.mailSsl || 0
          if (!data.id && !data.mailSubject) {
            data.mailSubject = i18n.t('modules.views.sysManage.notice.s_0998a12c') as string
          }
          for (const key in this.settingForm) {
            if (data.hasOwnProperty(key) && key !== 'mailProtocol') {
              this.settingForm[key] = typeof data[key] !== 'number' ? data[key] || '' : data[key]
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
      }
      this.setEmailConfig()
    })
  }
  private setEmailConfig (test?: boolean) {
    return new Promise((resolve, reject) => {
      const params = {
        ...this.settingForm,
        apiKey: this.apiKey,
      }
      delete params.mailProtocol
      if (!params.id) {
        delete params.id
      }
      this.postLoading = true;
      NoticeApi.setEmailConfig(params)
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
      this.setEmailConfig(true).then(() => {
        this.postLoading = true
        const params = {
          toEmails: this.testForm.toEmails
        }
        NoticeApi.testEmail(params)
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

  // 端口处理
  private portChangeHandle (val: string = '') {
    val = val.trim()
    const value = parseInt(val, 10)
    const port = val === '' || isNaN(value) ? '' : `${value > 65535 ? 65535 : value}`
    this.settingForm.mailPort = port
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
</style>
