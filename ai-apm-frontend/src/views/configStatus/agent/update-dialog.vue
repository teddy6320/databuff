<template>
  <el-dialog
    :visible.sync="showDialogModel"
    :title="$t('modules.views.configStatus.agent.s_b7b08917')"
    :before-close="cancelPostHandle"
    :close-on-click-modal='!postLoading'
    :close-on-press-escape='!postLoading'
    :show-close='!postLoading'
    width='800px'
    append-to-body
    :destroy-on-close='true'
  >
    <div v-if='showDialogModel'>
      <div class="config-host-info mb-15">
        <el-steps :active="activeStep" finish-status="success" :align-center='true'>
          <el-step :title="$t('modules.views.configStatus.agent.s_9408b770')"></el-step>
          <el-step :title="$t('modules.views.configStatus.agent.s_6d32a431')"></el-step>
          <el-step :title="$t('modules.views.configStatus.agent.s_56fc0b78')" :status="activeStep < 2 ? 'wait' : (activeStep === 2 && isUpdating ? 'process' : 'success')"></el-step>
          <el-step :title="$t('modules.views.configStatus.agent.s_56a85185')" :status="activeStep > 2 ? (hasFailed ? 'error' : 'success') : 'wait'"></el-step>
        </el-steps>
      </div>
      <step-1 v-show='activeStep === 0' :agentList='hostList'
        :versionList='versionList' :versionLoading="versionLoading" @on-change='versionChooseHandle'></step-1>
      <step-2 v-show='activeStep === 1'
        :agentList='hostList' :versionList='versionList' :choose-version='chooseVersion'></step-2>
      <step-3 v-if='activeStep === 2' :agentList='hostList' @on-updated='updatedOverHandle'></step-3>
      <step-4 v-if='activeStep > 2' :updated='updated' @on-finish='updateFinishHandle'></step-4>
    </div>

    <template slot="footer">
      <el-button size="small" v-show='activeStep === 1' @click="prevStepHandle">{{ $t('modules.views.configStatus.agent.s_eeb69088') }}</el-button>
      <el-button type='primary' size="small" v-show='activeStep === 0' @click="nextStepHandle" :disabled='!canStep2'>{{ $t('modules.views.configStatus.agent.s_38ce27d8') }}</el-button>
      <el-button type='primary' size="small" v-show='activeStep === 1' :disabled='postLoading || !canStep3'
        @click="startUpdateHandle" :loading='postLoading'>{{ $t('modules.views.configStatus.agent.s_6f80dbf9') }}</el-button>
      <el-button size="small" v-show='activeStep < 3' :disabled='postLoading || isUpdating' @click="cancelPostHandle">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
      <el-button type="primary" size="small" v-show='activeStep > 3 && !(updated && updated.failed && updated.failed.length)' :disabled='postLoading' @click="cancelPostHandle">
        <span>{{ $t('modules.components.dialog-template.s_38cf16f2') }}</span><span v-if='autoCloseInfo > 0'> ( {{autoCloseInfo}} )</span>
      </el-button>
      <el-button size="small" v-show='activeStep > 3 && updated && updated.failed && updated.failed.length' :disabled='postLoading' @click="repostHandle">{{ $t('modules.views.configStatus.agent.s_132c5cdc') }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import Simplebar from 'simplebar-vue'
import Step1 from './update/step-1.vue';
import i18n from '@/i18n';
import Step2 from './update/step-2.vue';
import Step3 from './update/step-3.vue';
import Step4 from './update/step-4.vue';
import { toAsyncWait } from '@/utils/common';
import AgentApi from '@/api/agent';
import { compareVersion, sortVersionObj } from '@/utils/compareVersion'

@Component({
  components: {
    Simplebar: Simplebar as any,
    Step1,
    Step2,
    Step3,
    Step4,
  }
})
export default class AgentUpdateDialog extends Vue {
  @Prop() private showModel!: boolean;
  @Prop() private hostList!: any[];

  @Watch('showModel')
  private onShowModelChange (newVal: boolean) {
    this.showDialogModel = newVal
  }

  get canStep3 () {
    // 判断所选列表是否有可更新的
    // 遍历当前版本是否小于等于所选版本
    return this.hostList.filter((item) => compareVersion(item.agentVersion, this.chooseVersion.version) <= 0 ).length
  }

  get hasFailed () {
    return this.updated && this.updated.failed && this.updated.failed.length
  }

  // 修改agent配置弹窗相关
  private showDialogModel = false
  private postLoading = false;

  private activeStep = 0;

  private versionLoading = true;
  private versionList: any[] = [];
  private chooseVersion: any = {
    id: '',
    version: '',
  };
  private canStep2 = false;

  private isUpdating = false;
  private updated: any = {
    success: [],
    failed: [],
    updateList: []
  };

  private autoCloseTimer: any = null;
  private autoCloseInfo = 5

  private needRefresh = false;
  @Watch('activeStep')
  private onActiveStepChange () {
    if (this.activeStep > 1) {
      this.needRefresh = true;
    }
  }

  private created () {
    this.fetchVersionList()
  }

  private beforeDestroy () {
    if (this.autoCloseTimer) {
      window.clearTimeout(this.autoCloseTimer);
      this.autoCloseTimer = null;
    }
  }

  private async fetchVersionList () {
    this.versionLoading = true
    const { result, error } = await toAsyncWait(AgentApi.getVersionList())
    if (!error) {
      const { data = [] } = result || {};
      this.versionList = sortVersionObj((data || []).map((item: any) => ({
        ...item,
        remark: item.remark.replaceAll('\\r\\n', '\r\n')
      })), 'version');
    }
    this.versionLoading = false;
  }

  // 仅提交更新失败的宿主机
  private async repostHandle () {
    // this.isUpdating = true;
    // this.postLoading = true;
    // const params = {
    //   hosts: this.updated.failed.map((item: any) => item.host),
    //   packId: this.chooseVersion.id
    // }
    // const { error } = await toAsyncWait(AgentApi.submitUpdate(params))
    // if (!error) {
    //   this.activeStep = 2;
    // } else {
    //   this.isUpdating = false
    //   this.$message.error(i18n.t('modules.views.configStatus.agent.s_0c4ef06d') as string)
    //   this.postLoading = false;
    // }
    this.isUpdating = false;
    this.postLoading = false;
    this.activeStep = 0
    this.$emit('on-success-some', this.updated.success);
  }

  // 关闭弹窗
  private cancelPostHandle () {
    this.showDialogModel = false
    if (this.autoCloseTimer) {
      window.clearTimeout(this.autoCloseTimer);
      this.autoCloseTimer = null;
    }
    this.activeStep = 0
    const needRefresh = this.needRefresh
    this.needRefresh = false
    this.$emit('on-close', { refresh: needRefresh })
  }

  // 关闭弹窗
  private async postHandle () {
    // 判断是否有修改
    this.postLoading = true
    //
    this.activeStep += 1
    // this.postLoading = false
  }

  private versionChooseHandle (version: any) {
    this.chooseVersion = version;
    this.canStep2 = true
  }

  private updatedOverHandle ({ success, failed, updateList }: any) {
    this.activeStep = 5
    this.isUpdating = false;
    this.postLoading = false;
    this.updated.success = success
    this.updated.failed = failed
    this.updated.updateList = updateList
  }

  private prevStepHandle () {
    this.activeStep -= 1
  }
  private nextStepHandle () {
    this.activeStep += 1
  }
  private async startUpdateHandle () {
    this.isUpdating = true;
    this.postLoading = true;
    const params = {
      hosts: this.hostList.map((item) => item.hostName),
      packId: this.chooseVersion.id,
      operation: 0,
    }
    const { error } = await toAsyncWait(AgentApi.submitUpdate(params))
    if (!error) {
      this.activeStep += 1;
    } else {
      this.isUpdating = false
      this.$message.error(i18n.t('modules.views.configStatus.agent.s_0c4ef06d') as string)
      this.postLoading = false;
    }
  }

  private updateFinishHandle () {
    this.autoCloseLoop();
  }

  private autoCloseLoop () {
    if (this.autoCloseTimer) {
      window.clearTimeout(this.autoCloseTimer);
      this.autoCloseTimer = null;
    }
    this.autoCloseTimer = setTimeout(() => {
      this.autoCloseInfo--;
      if (this.autoCloseInfo > 0) {
        this.autoCloseLoop()
      } else {
        this.cancelPostHandle()
      }
    }, 1000)
  }
}
</script>
