<template>
  <div class="update-step-3">
    <div class="mb-10">{{ $t('modules.views.configStatus.agent.s_8d6dac9d') }}</div>
    <div class="font-13">
      <i v-show='loading' class="el-icon el-icon-loading"></i>
      {{ $t('modules.views.configStatus.agent.s_a1f4f238', { value0: processInfo || '-' }) }}
    </div>
    <div class="update-step-process-bar mt-10">
      <el-progress :text-inside="false" :stroke-width="20" :percentage="currProcess" :show-text="false"></el-progress>
    </div>
    <div v-show='doingList.length || prepareList.length' class="update-step-preview mt-10">
      <div v-for="doing in doingList" :key='doing.host' class="update-step-host">
        <span class="update-step-host-name">{{ doing.host }}</span>
        <span class="update-step-host-split"></span>
        <span class="update-step-host-status">{{ doing.progress }} {{ doing.percent }}%</span>
      </div>
      <div v-for="prepare in prepareList" :key='prepare.host' class="update-step-host">
        <span class="update-step-host-name">{{ prepare.host }}</span>
        <span class="update-step-host-split"></span>
        <span class="update-step-host-status">{{ prepare.progress }} {{ prepare.percent }}%</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator'
import Simplebar from 'simplebar-vue'
import { toAsyncWait } from '@/utils/common';
import AgentApi from '@/api/agent';

@Component({
  components: {
    Simplebar: Simplebar as any,
  }
})
export default class UpdateStep3 extends Vue {
  @Prop({ default: () => [] }) private agentList!: any[];

  private processInfo = '准备中...';
  private currProcess = 0;
  private doingList: any[] = [];
  private prepareList: any[] = [];

  private timer: any = null;
  private initTimer: any = null;
  private finishTimer: any = null;
  private loading = true

  private updateList: any[] = [];

  private tempCount = 0;

  get isUpdated () {
    return this.updateList.filter((item) => item.status === 0 || item.status === 3).length === 0
  }

  private created () {
    this.initTimer = setTimeout(() => {
      this.loop()
    }, 1000)
  }

  private beforeDestroy () {
    this.cancelTimer()
  }

  private cancelTimer () {
    window.clearTimeout(this.timer);
    window.clearTimeout(this.initTimer);
    window.clearTimeout(this.finishTimer);
    this.timer = null;
    this.initTimer = null;
    this.finishTimer = null;
  }

  private loop () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    if (this.currProcess >= 100) {
      return
    }
    this.timer = setTimeout(() => {
      // this.getUpdateInfo()
      // if (!this.isUpdated) {
      //   this.loop()
      // }
      AgentApi.getUpdateInfo()
        .then((res: any) => {
          if (res.status === 200 && res.message.toLowerCase() === 'success' && res.data) {
            const { agents = [], percent = 0 } = res.data || {}
            const choosedHosts = this.agentList.map((item: any) => item.hostName)
            this.updateList = (agents || []).filter((item: any) => choosedHosts.indexOf(item.host) > -1);
            // console.log(this.updateList)
            const undo = this.updateList.filter((item: any) => item.status === 0)
            const success = this.updateList.filter((item: any) => item.status === 1)
            const failed = this.updateList.filter((item: any) => item.status === 2)
            const doing = this.updateList.filter((item: any) => item.status === 3)
            this.processInfo = `${success.length + failed.length} / ${this.updateList.length}`
            this.currProcess = percent
            this.doingList = doing;
            this.prepareList = undo;
            this.tempCount++;
            // if (this.tempCount > 3 || !doing.length && !undo.length) {
            if (!doing.length && !undo.length) {
              this.loading = false
              this.finishTimer = setTimeout(() => {
                this.$emit('on-updated', { success, failed, updateList: this.updateList })
              }, 1000)
            }
          }
        })
        .finally(() => {
          if (!this.isUpdated) {
            this.loop()
          }
        })
    }, 3000)
  }

  private async getUpdateInfo () {
    this.loading = true
    const { result, error } = await toAsyncWait(AgentApi.getUpdateInfo())
    if (!error) {
      const { data = {} } = result;
      const { agents = [], percent = 0 } = data || {}
      const choosedHosts = this.agentList.map((item: any) => item.hostName)
      this.updateList = (agents || []).filter((item: any) => choosedHosts.indexOf(item.host) > -1);
      // console.log(this.updateList)
      const undo = this.updateList.filter((item: any) => item.status === 0)
      const success = this.updateList.filter((item: any) => item.status === 1)
      const failed = this.updateList.filter((item: any) => item.status === 2)
      const doing = this.updateList.filter((item: any) => item.status === 3)
      this.processInfo = `${success.length + failed.length} / ${this.updateList.length}`
      this.currProcess = percent
      this.doingList = doing;
      if (!doing.length && !undo.length) {
        this.loading = false
        this.finishTimer = setTimeout(() => {
          this.$emit('on-updated', { success, failed, updateList: this.updateList })
        }, 1000)
      }
    }
  }
}
</script>

<style lang='scss' scoped>
.update-step-3 {
  .update-step-process-bar {
    padding: 0 18px;
  }
  .update-step-preview {
    border: 1px solid var(--border-color-base);
    margin: 15px 18px 0;
    padding: 10px;
    max-height: 182px;
    overflow-y: auto;
    font-size: 13px;
  }
  .update-step-host {
    padding: 3px 18px;
    display: flex;
    align-items: center;
    &-split {
      flex: 1;
      height: 1px;
      border-bottom: 1px dashed var(--color-text-secondary);
      margin: 0 20px;
    }
    &-status {
      flex: 1;
    }
  }
}
</style>
