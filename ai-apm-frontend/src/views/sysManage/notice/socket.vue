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
        <el-form-item :label="$t('modules.views.configManage.entity.s_2dc9105c')" prop="host">
          <el-input
            v-model="settingForm.host"
            maxlength="100"
            :placeholder="$t('modules.views.infrastructure.host.s_7dfc3e9b')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item :label="$t('modules.views.deployInstall.otelCollector.s_c76cfefe')" prop="port">
          <el-input
            v-model="settingForm.port"
            @change="updateInputHandle($event, 'port', 65535)"
            :placeholder="$t('modules.views.sysManage.notice.s_bc0eb7d7')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item :label="$t('modules.views.sysManage.notice.s_6143a714')" prop="encoding">
          <el-input
            v-model="settingForm.encoding"
            maxlength="100"
            :placeholder="$t('modules.views.sysManage.notice.s_076dadb5')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item :label="$t('modules.views.sysManage.basic.s_56071a4f')" prop="timeOut">
          <el-input
            v-model="settingForm.timeOut"
            @change="updateInputHandle($event, 'timeOut', 3600000)"
            :placeholder="$t('modules.views.sysManage.notice.s_dcba5bce')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item :label="$t('modules.views.sysManage.operationAudit.s_45235f6c')" prop="identify">
          <el-input
            v-model="settingForm.identify"
            maxlength="100"
            :placeholder="$t('modules.views.configManage.llm.s_befa4827')"
            class="form-input"
          />
        </el-form-item>
      </template>
    </el-form>

    <div class="pt-10">
      <el-button :disabled="postLoading || !settingForm.tenantEnable" @click="testNoticeHandle()" size="small">{{ $t('modules.views.sysManage.account.s_edb13707') }}</el-button>
      <el-button :disabled="postLoading" @click="setConfig()" type="primary" size="small">{{ $t('modules.views.hide.advancedConfig.s_74d9faed') }}</el-button>
    </div>
  </div>
</template>

<script lang='ts'>import i18n from '@/i18n';

import { Vue, Component } from 'vue-property-decorator'
import { Form } from 'element-ui'
import NoticeApi from '@/api/notice'

@Component
export default class SocketSetting extends Vue {
  get apiKey () {
    return this.$store.getters['User/getUserInfo'].cid || 'NzhEMjlBOTk3MzkxRjk5MjQyMzMzQzI4'
  }

  public $refs!: {
    settingForm: Form
  }

  private isLoading = false

  private settingForm: any = {
    id: 0,
    notifyType: 'socket',
    tenantEnable: 0,
    host: '',
    port: '',
    encoding: '', // UTF-8
    timeOut: '',
    identify: '', // identify === sdcsh 时，可下拉选择短信接收者
  }
  private settingRules = {
    tenantEnable: { required: true, message: i18n.t('modules.views.sysManage.notice.s_2f5436ed') as string, messageKey: 'modules.views.sysManage.notice.s_2f5436ed', trigger: 'change' },
    host: { required: true, message: i18n.t('modules.views.sysManage.notice.s_7dfc3e9b') as string, messageKey: 'modules.views.infrastructure.host.s_7dfc3e9b', trigger: 'blur' },
    port: { required: true, message: i18n.t('modules.views.sysManage.notice.s_bc0eb7d7') as string, messageKey: 'modules.views.sysManage.notice.s_bc0eb7d7', trigger: 'blur' },
  }
  private postLoading = false

  private created () {
    this.getConfig()
  }

  // 获取通知配置
  private getConfig () {
    this.isLoading = true
    NoticeApi.getSocketConfig()
      .then((rst: any) => {
        if (rst && rst.status === 200) {
          const data: any = { ...rst.data || {}, ...(rst.data || {}).config }
          data.notifyType = data.notifyType || 'socket'
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
  private setConfig () {
    this.$refs.settingForm.validate((valid: boolean) => {
      if (!valid) {
        return
      }
      this.setSocketConfig()
    })
  }
  private setSocketConfig (test?: boolean) {
    return new Promise((resolve, reject) => {
      const params = {
        apiKey: this.apiKey,
        id: this.settingForm.id,
        notifyType: this.settingForm.notifyType,
        tenantEnable: this.settingForm.tenantEnable,
        config: {
          host: this.settingForm.host,
          port: this.settingForm.port,
          encoding: this.settingForm.encoding,
          timeOut: this.settingForm.timeOut,
          identify: this.settingForm.identify,
        }
      }
      if (!params.id) {
        delete params.id
      }
      this.postLoading = true;
      NoticeApi.setSocketConfig(params)
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

  private testNoticeHandle () {
    this.$refs.settingForm.validate((valid: boolean) => {
      if (!valid) {
        return
      } else if (!this.settingForm.tenantEnable) {
        this.$message.warning(i18n.t('modules.views.sysManage.notice.s_bcc54918') as string);
        return
      }
      this.testNotice()
    })
  }
  // 测试通知
  private testNotice () {
    // 测试通知需要先保存
    this.setSocketConfig(true).then(() => {
      this.postLoading = true
      const params = {
        apiKey: this.apiKey,
        id: this.settingForm.id,
        notifyType: this.settingForm.notifyType,
        tenantEnable: this.settingForm.tenantEnable,
        config: {
          host: this.settingForm.host,
          port: this.settingForm.port,
          encoding: this.settingForm.encoding,
          timeOut: this.settingForm.timeOut,
          identify: this.settingForm.identify,
        }
      }
      if (!params.id) {
        delete params.id
      }
      NoticeApi.testSocket(params)
        .then((rst: any) => {
          if (rst && rst.status === 200 && rst.message.toLocaleLowerCase() === 'success') {
            this.postLoading = false
            this.$message.success(i18n.t('modules.views.alarmCenter.notice.s_9db9a7e3') as string);
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

  private updateInputHandle (val: string = '', key: string, max?: number) {
    val = val.trim()
    let value: any = parseInt(val, 10)
    if (val === '' || isNaN(value)) {
      value = ''
    } else if (max && value > max) {
      value = max
    }
    this.settingForm[key] = value
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
