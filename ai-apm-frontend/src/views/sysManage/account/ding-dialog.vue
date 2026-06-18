<template>
  <el-dialog
    :visible.sync="showModel"
    :title="$t('modules.views.sysManage.account.s_13028a40')"
    :before-close="() => dialogCancelHandle(null, true)"
    :close-on-click-modal='!dialogPostLoading'
    :close-on-press-escape='!dialogPostLoading'
    :show-close='!dialogPostLoading'
    width='540px'
    append-to-body>
    <template v-if='!webhookModel'>
      <div class="font-14 lh-22 fw-500 mb-10">{{ $t('modules.views.sysManage.account.s_f596f955') }}</div>
      <div class="mb-30 flex-h flex-start">
        <div class="font-13 flex-0 w-100 lh-32">{{ $t('modules.views.sysManage.account.s_6a5e5705') }}</div>
        <el-input
          v-model="accountForm.dingtalkUid"
          maxlength="100"
          :placeholder="getDingtalkPlacehold"
          disabled
          size="small"
          class="form-input flex-1"
        >
          <el-button v-if='!accountForm.dingtalkUid' @click="bindDingHandle" :loading='bingLoading' :disabled='!isDingEnable' slot="append" size="small" class="bind-btn">{{ $t('modules.views.sysManage.account.s_25048992') }}</el-button>
          <el-button v-if='accountForm.dingtalkUid' @click="unbindDingHandle" :loading='bingLoading' :disabled='!accountMobile' slot="append" size="small" class="bind-btn">{{ $t('modules.views.sysManage.account.s_c7b01a47') }}</el-button>
        </el-input>
      </div>
    </template>

    <div class="font-14 lh-22 fw-500 mb-10">{{ $t('modules.views.personal.s_3290b598') }}</div>
    <div class="flex-h flex-start mb-16">
      <div class="font-13 flex-0 w-100 lh-32">{{ $t('modules.views.sysManage.account.s_a89a4918') }}</div>
      <el-input v-model='accountForm.dingWebhook' size="small"
        :disabled='!isRobotEnable'
        type='textarea' :autosize="{ minRows: 3, maxRows: 5 }" clearable
        :placeholder="$t('modules.views.sysManage.account.s_cf775b38')" class="flex-1"></el-input>
    </div>
    <div class="flex-h flex-start mb-16">
      <div class="font-13 flex-0 w-100 lh-32">Secret</div>
      <el-input v-model='accountForm.dingSecret' size="small"
        :disabled='!isRobotEnable'
        type='textarea' :autosize="{ minRows: 1, maxRows: 3 }" clearable
        :placeholder="$t('modules.views.sysManage.account.s_1bc28f03')" class="flex-1"></el-input>
    </div>
    <div v-if="isRobotEnable" class="mt-15 pl-110">
      <el-button @click="testDtalkNotice"
        :disabled='webhookLoading || !accountForm.dingWebhook || !accountForm.dingSecret'
        size="small" plain >{{ $t('modules.views.sysManage.account.s_edb13707') }}</el-button>
      <el-button @click="setDtalkConfig()"
        :disabled='webhookLoading || (!!accountForm.dingWebhook && !accountForm.dingSecret) || (!accountForm.dingWebhook && !!accountForm.dingSecret)'
        size="small" plain>{{ $t('modules.views.hide.advancedConfig.s_74d9faed') }}</el-button>
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
    this.accountForm.dingtalkUid = newVal?.rcvUser?.dingtalkUid
    this.accountForm.dingWebhook = newVal?.rcvUser?.dingWebhook
    this.accountForm.dingSecret = newVal?.rcvUser?.dingSecret
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
    dingtalkUid: '',
    dingWebhook: '',
    dingSecret: '',
  }

  get getDingtalkPlacehold () {
    return this.accountMobile ? i18n.t('modules.views.sysManage.account.s_9c128a15') as string : i18n.t('modules.views.sysManage.account.s_1b9a3e1c') as string
  }

  get apiKey () {
    return this.$store.getters['User/getUserInfo'].cid || 'NzhEMjlBOTk3MzkxRjk5MjQyMzMzQzI4'
  }

  get isDingEnable () {
    return this.notifyStatus.dingEnable === 1 && this.accountMobile
  }
  get isRobotEnable () {
    return this.notifyStatus.dingRobotEnable === 1
  }

  // 关闭弹窗
  private dialogCancelHandle (payload?: any) {
    this.showModel = false
    this.$emit('on-close', payload);
    this.$emit('input', false);
  }

  private async bindDingHandle () {
    if (this.notifyStatus.dingEnable !== 1) {
      this.$message.info(i18n.t('modules.views.personal.s_d5823fee') as string);
      return
    }
    if (!this.current.mobile) {
      this.$message.info(i18n.t('modules.views.personal.s_2e696096') as string);
      return
    }
    this.bingLoading = true
    const params = {
      type: 'dingTalk',
      phone: this.current.mobile,
      name: this.current.account,
      enable: 1
    }
    const { result, error } = await toAsyncWait(SystemApi.saveReceiverByType(params))
    if (!error) {
      this.$message.success(i18n.t('modules.views.personal.s_1974fe53') as string)
      this.accountForm.dingtalkUid = String(Date.now());
      this.$emit('refresh');
    } else {
      this.$message.error(i18n.t('modules.views.personal.s_76be598f') as string)
    }
    this.bingLoading = false
  }

  private async unbindDingHandle () {
    this.bingLoading = true
    const params = {
      type: 'dingTalk',
      name: this.current.rcvUser?.rcvName || this.current.account,
      id: this.current?.rcvUser?.id,
    }
    const { result, error } = await toAsyncWait(SystemApi.unbindReceiverByType(params))
    if (!error) {
      this.$message.success(i18n.t('modules.views.personal.s_1c4385b5') as string)
      this.accountForm.dingtalkUid = '';
      this.$emit('refresh');
    } else {
      this.$message.error(i18n.t('modules.views.personal.s_913643d0') as string)
    }
    this.bingLoading = false
  }

  private testDtalkNotice () {
    // 测试通知需要先保存
    this.setDtalkConfig(true).then(() => {
      if (!this.accountForm.dingSecret || !this.accountForm.dingWebhook) {
        return
      }
      this.webhookLoading = true
      NoticeApi.testDingTalk({ dingWebhook: this.accountForm.dingWebhook, dingSecret: this.accountForm.dingSecret })
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
  private setDtalkConfig (test?: boolean) {
    return new Promise((resolve, reject) => {
      const { dingSecret, dingWebhook } = this.accountForm
      if (test && dingSecret === this.current?.rcvUser?.dingSecret && dingWebhook === this.current?.rcvUser?.dingWebhook) {
        resolve({})
        return
      }
      const params = {
        id: this.current?.rcvUser?.id,
        dingWebhook,
        dingSecret,
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
