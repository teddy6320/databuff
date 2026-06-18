<template>
  <div class="setting-wrap flex-v" v-loading="isLoading">
    <el-form @submit.native.prevent ref="settingForm" :model="settingForm" class="setting-form flex-1" label-position="top" size="small">
      <div class="setting-title">{{ $t('modules.views.sysManage.notice.s_bdf0097c') }}</div>
      <el-form-item
        :label="$t('modules.views.dataReport.report.s_3290b598')"
        prop="robotEnable"
        :rules="{ required: true, message: $t('modules.views.sysManage.notice.s_2f5436ed'), trigger: 'change' }"
      >
        <el-switch
          v-model="settingForm.robotEnable"
          :active-value="1"
          :inactive-value="0"
        ></el-switch>
        <el-tooltip class="ml-10" :content="$t('modules.views.sysManage.notice.s_bec3dffd')" placement="right" effect="light">
          <i class="db-icon-info describe"></i>
        </el-tooltip>
      </el-form-item>

      <el-form-item
        :label="$t('modules.views.sysManage.notice.s_a7f73b58')"
        prop="tenantEnable"
        :rules="{ required: true, message: $t('modules.views.sysManage.notice.s_2f5436ed'), trigger: 'change' }"
      >
        <el-switch
          v-model="settingForm.tenantEnable"
          :active-value="1"
          :inactive-value="0"
        ></el-switch>
        <el-tooltip class="ml-10" :content="$t('modules.views.sysManage.notice.s_a17d4425')" placement="right" effect="light">
          <i class="db-icon-info describe"></i>
        </el-tooltip>
      </el-form-item>

      <template v-if="settingForm.tenantEnable">
        <el-form-item label="appkey" prop="appkey"
          :rules="{ required: true, message: $t('modules.views.sysManage.notice.s_7d121d7a'), trigger: 'blur' }">
          <el-input
            v-model="settingForm.appkey"
            maxlength="300"
            :placeholder="$t('modules.views.sysManage.notice.s_7d121d7a')"
            class="form-input" />
        </el-form-item>

        <el-form-item label="appsecret" prop="appsecret"
          :rules="{ required: true, message: $t('modules.views.sysManage.notice.s_90dc2f5a'), trigger: 'blur' }">
          <el-input
            v-model="settingForm.appsecret"
            maxlength="300"
            :placeholder="$t('modules.views.sysManage.notice.s_90dc2f5a')"
            class="form-input" />
        </el-form-item>

        <el-form-item label="AgentId" prop="dingAgentId"
          :rules="{ required: true, message: $t('modules.views.sysManage.notice.s_af2f4ae9'), trigger: 'blur' }">
          <el-input
            v-model="settingForm.dingAgentId"
            maxlength="300"
            :placeholder="$t('modules.views.sysManage.notice.s_af2f4ae9')"
            class="form-input" />
        </el-form-item>
      </template>
    </el-form>

    <div class="pt-10">
      <el-button :disabled="postLoading || !settingForm.tenantEnable" @click="showTestNotice" size="small">{{ $t('modules.views.sysManage.account.s_edb13707') }}</el-button>
      <el-button :disabled="postLoading" @click="setConfig()" type="primary" size="small">{{ $t('modules.views.hide.advancedConfig.s_74d9faed') }}</el-button>
    </div>

    <el-dialog :visible.sync="showTestModal"
      :title="$t('modules.views.sysManage.account.s_edb13707')"
      width="480px">
      <el-form @submit.native.prevent ref="testForm" :model='testForm' :rules="testFormRules" label-width="80px" label-position="left" size="small">
        <el-form-item :label="$t('modules.views.sysManage.notice.s_92448a35')" prop='mobile'>
          <el-input v-model='testForm.mobile' :placeholder="$t('modules.views.sysManage.notice.s_e76cbe28')"></el-input>
        </el-form-item>
        <div class="describe ml-20 font-12">{{ $t('modules.views.sysManage.notice.s_b45b33e0') }}</div>
      </el-form>

      <div slot="footer">
        <el-button size="small" :disabled="postLoading" @click="cancelTestModal">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :disabled="postLoading" @click="testNotice">{{ $t('modules.views.sysManage.notice.s_2c8370b5') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script lang='ts'>import i18n from '@/i18n';

import { Vue, Component } from 'vue-property-decorator'
import { Form } from 'element-ui'
import NoticeApi from '@/api/notice'

@Component
export default class DingtalkSetting extends Vue {
  get apiKey () {
    return this.$store.getters['User/getUserInfo'].cid || 'NzhEMjlBOTk3MzkxRjk5MjQyMzMzQzI4'
  }

  public $refs!: {
    settingForm: Form
    testForm: Form
  }

  private settingForm: any = {
    id: 0,
    notifyType: 'dingtalk',
    robotEnable: 0,
    tenantEnable: 0,
    appkey: '',
    appsecret: '',
    dingAgentId: '',
  }

  private testForm = {
    mobile: ''
  }

  get testFormRules () {
    const validateMobile = (rule: any, value: string, callback: any) => {
      const mobileReg = new RegExp(/^(?:(?:\+|00)86)?1[3-9]\d{9}$/);
      if (!value || !mobileReg.test(value)) {
        callback(new Error(i18n.t('modules.views.personal.s_a32ab517') as string));
      } else {
        callback();
      }
    };
    return {
      mobile: { required: true, validator: validateMobile, trigger: 'blur' },
    };
  }

  private isLoading = false
  private postLoading = false

  private showTestModal = false

  private created () {
    this.getConfig()
  }

  // 获取通知配置
  private getConfig () {
    this.isLoading = true
    NoticeApi.getDingTalkConfig()
      .then((rst: any) => {
        if (rst && rst.status === 200) {
          const data: any = rst.data || {}
          data.notifyType = data.notifyType || 'dingtalk'
          data.robotEnable = data.robotEnable || 0
          data.tenantEnable = data.tenantEnable || 0
          for (const key in this.settingForm) {
            if (data.hasOwnProperty(key)) {
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
  private setConfig (test?: boolean) {
    return new Promise((resolve, reject) => {
      this.$refs.settingForm.validate((valid: boolean) => {
        if (!valid) {
          return
        } else if (test && !this.settingForm.tenantEnable) {
          this.$message.warning(i18n.t('modules.views.sysManage.notice.s_bcc54918') as string);
          return
        }
        const params = {
          ...this.settingForm,
          apiKey: this.apiKey,
        }
        if (!params.id) {
          delete params.id
        }
        this.postLoading = true;
        NoticeApi.setDingTalkConfig(params)
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
    })
  }

  // 测试通知
  private testNotice () {
    this.$refs.testForm.validate((valid: boolean) => {
      if (!valid) {
        return
      }
      this.postLoading = true
      NoticeApi.testDingTalkByPhone({ phone: this.testForm.mobile })
        .then((rst: any) => {
          if (rst && rst.status === 200 && rst.message.toLocaleLowerCase() === 'success') {
            this.postLoading = false
            this.$message.success(i18n.t('modules.views.alarmCenter.notice.s_9db9a7e3') as string);
            this.cancelTestModal()
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
  }

  private showTestNotice () {
    // 测试通知需要先保存
    this.setConfig(true).then(() => {
      this.testForm.mobile = ''
      this.showTestModal = true
    })
  }
  private cancelTestModal () {
    this.$refs.testForm.resetFields();
    this.testForm.mobile = ''
    this.showTestModal = false
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
