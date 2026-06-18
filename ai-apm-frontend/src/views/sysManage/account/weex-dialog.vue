<template>
  <el-dialog
    :visible.sync="showModel"
    :title="$t('modules.views.sysManage.account.s_9b82d9d4')"
    :before-close="() => dialogCancelHandle(null, true)"
    :close-on-click-modal='!dialogPostLoading'
    :close-on-press-escape='!dialogPostLoading'
    :show-close='!dialogPostLoading'
    width='540px'
    append-to-body>
    <template v-if='!webhookModel'>
      <div class="font-14 lh-22 fw-500 mb-10">{{ $t('modules.views.sysManage.account.s_65d9c7ed') }}</div>
      <div class="mb-30 flex-start flex-h">
        <div class="font-13 flex-0 w-100 lh-32">{{ $t('modules.views.sysManage.account.s_cae1d111') }}</div>
        <el-input
          v-model="accountForm.wechatUid"
          maxlength="100"
          :placeholder="getDingtalkPlacehold"
          disabled
          size="small"
          class="form-input flex-1"
        >
          <el-button v-if='!accountForm.wechatUid' @click="bindWeexHandle" :disabled='!isWeexEnable' :loading='bingLoading' slot="append" size="small" class="bind-btn">{{ $t('modules.views.sysManage.account.s_25048992') }}</el-button>
          <el-button v-if='accountForm.wechatUid' @click="unbindWeexHandle" :disabled='!accountMobile' :loading='bingLoading' slot="append" size="small" class="bind-btn">{{ $t('modules.views.sysManage.account.s_c7b01a47') }}</el-button>
        </el-input>
      </div>
    </template>

    <div class="font-14 lh-22 fw-500 mb-10">{{ $t('modules.views.personal.s_4c6e119c') }}</div>
    <div class="flex-h flex-start mb-16">
      <div class="font-13 flex-0 w-100 lh-32">{{ $t('modules.views.sysManage.account.s_a89a4918') }}</div>
      <el-input v-model='accountForm.wechatWebhook' size="small"
        :disabled='!isRobotEnable'
        type='textarea' :autosize="{ minRows: 3, maxRows: 5 }" clearable
        :placeholder="$t('modules.views.sysManage.account.s_cf775b38')" class="flex-1"></el-input>
    </div>
    <div v-if="isRobotEnable" class="mt-15 pl-110">
      <el-button plain :disabled='webhookLoading || !accountForm.wechatWebhook' @click="testWeexNotice" size="small">{{ $t('modules.views.sysManage.account.s_edb13707') }}</el-button>
      <el-button plain :disabled='webhookLoading' @click="setWeexConfig()" size="small">{{ $t('modules.views.hide.advancedConfig.s_74d9faed') }}</el-button>
    </div>

    <div slot="footer">
      <el-button size="small" :disabled="dialogPostLoading" @click="dialogCancelHandle">{{ $t('modules.views.alarmCenter.problemDetail.s_b15d9127') }}</el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { toAsyncWait } from '@/utils/common';
import i18n from '@/i18n';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import SystemApi from '@/api/system';
import NoticeApi from '@/api/notice'

@Component({})
export default class DingtalkDialog extends Vue {
  @Prop() private value!: boolean;
  @Prop() private current!: any;
  @Prop() private notifyStatus!: any;
  @Prop() private webhookModel!: boolean;

  @Watch('value')
  private onShowChange (newVal: boolean) {
    this.showModel = newVal
  }
  @Watch('current', { immediate: true })
  private onCurrentChange (newVal: any) {
    this.accountForm.wechatUid = newVal?.rcvUser?.wechatUid
    this.accountForm.wechatWebhook = newVal?.rcvUser?.wechatWebhook
  }

  get accountMobile () {
    return this.current?.mobile
  }

  private showModel = false;
  private bingLoading = false;
  private webhookLoading = false;
  get dialogPostLoading () {
    return this.bingLoading || this.webhookLoading
  }

  private accountForm = {
    wechatUid: '',
    wechatWebhook: '',
  }

  get getDingtalkPlacehold () {
    return this.accountMobile ? i18n.t('modules.views.sysManage.account.s_6aace92b') as string : i18n.t('modules.views.sysManage.account.s_a3026a73') as string
  }

  get isWeexEnable () {
    return this.notifyStatus.weexEnable === 1 && this.accountMobile
  }
  get isRobotEnable () {
    return this.notifyStatus.weexRobotEnable === 1
  }

  // 关闭弹窗
  private dialogCancelHandle (payload?: any) {
    this.showModel = false
    this.$emit('on-close', payload);
    this.$emit('input', false);
  }

  private async bindWeexHandle () {
    if (this.notifyStatus.weexEnable !== 1) {
      this.$message.info(i18n.t('modules.views.personal.s_3426b567') as string);
      return
    }
    if (!this.current.mobile) {
      this.$message.info(i18n.t('modules.views.personal.s_2e696096') as string);
      return
    }
    this.bingLoading = true
    const params = {
      type: 'wechat',
      phone: this.current.mobile,
      name: this.current.account,
      enable: 1
    }
    const { result, error } = await toAsyncWait(SystemApi.saveReceiverByType(params))
    if (!error) {
      this.$message.success(i18n.t('modules.views.personal.s_1974fe53') as string)
      this.accountForm.wechatUid = String(Date.now());
      this.$emit('refresh');
    } else {
      this.$message.error(error?.message || i18n.t('modules.views.personal.s_76be598f') as string)
    }
    this.bingLoading = false
  }

  private async unbindWeexHandle () {
    this.bingLoading = true
    const params = {
      type: 'wechat',
      name: this.current.account,
      id: this.current.rcvId,
    }
    const { result, error } = await toAsyncWait(SystemApi.unbindReceiverByType(params))
    if (!error) {
      this.$message.success(i18n.t('modules.views.personal.s_1c4385b5') as string)
      this.accountForm.wechatUid = '';
      this.$emit('refresh');
    } else {
      this.$message.error(i18n.t('modules.views.personal.s_913643d0') as string)
    }
    this.bingLoading = false
  }

  private testWeexNotice () {
    // 测试通知需要先保存
    this.setWeexConfig(true).then(() => {
      if (!this.accountForm.wechatWebhook) {
        return
      }
      this.webhookLoading = true
      NoticeApi.testWeChat({ wechatWebhook: this.accountForm.wechatWebhook })
        .then((rst: any) => {
          if (rst && rst.status === 200 && rst.message.toLocaleLowerCase() === 'success') {
            this.$message.success(i18n.t('modules.views.alarmCenter.notice.s_9db9a7e3') as string);
          } else {
            throw new Error(rst.message);
          }
        })
        .catch((err) => {
          this.$message.error(err.message)
        })
        .finally(() => {
          this.webhookLoading = false;
        })
    })
  };
  private setWeexConfig (test?: boolean) {
    return new Promise((resolve, reject) => {
      const { wechatWebhook } = this.accountForm
      if (test && wechatWebhook === this.current?.rcvUser?.wechatWebhook) {
        resolve({})
        return
      }
      const params = {
        id: this.current?.rcvUser?.id,
        wechatWebhook,
      }
      if (!params.id) {
        delete params.id
      }
      this.webhookLoading = true;
      SystemApi.saveReceiver(params)
        .then((rst: any) => {
          if (rst && rst.status === 200) {
            if (!test) {
              this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
            }
            resolve(rst.data);
            this.$emit('refresh');
          } else {
            throw new Error(rst.message);
          }
        })
        .catch((err) => {
          this.$message.error(err.message)
          reject(err.message);
        })
        .finally(() => {
          this.webhookLoading = false;
        })
    })
  };
  
}
</script>

<style lang="scss" scoped>
.w-100 {
  width: 100px;
}
.pl-110 {
  padding-left: 110px;
}
.flex-start {
  align-items: flex-start;
}
.bind-btn {
  height: 30px;
  border: none;
  &:not(.is-disabled) {
    background-color: #fff;
    color: var(--color-text-regular);
  }
}
</style>
