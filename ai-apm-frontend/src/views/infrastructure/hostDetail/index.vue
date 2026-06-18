<template>
  <div class="db-detail-wrapper">
    <div class="db-detail" :class="`db-detail-${activeName}`" v-loading="isLoading">
      <div class="detail-title ml-4 mr-4">
        <span
          :class="detail.alarmPendingCount === 0 ? 'bg-green' : 'bg-red'"
          class="detail-status">{{ (detail.alarmPendingCount > 0) | HealthStatusFilter }}</span>
        <span class="detail-icon db-icon mr-10">{{ detail.hostOs | DbIconFilter('host') }}</span>
        <span class="detail-name">{{ detail.hostName || '-' }}
          <i @click.stop="copyHandle(detail.hostName || '-')" class="copy-icon db-icon-copy"></i>
        </span>

      </div>

      <db-tabnav
        v-model="activeName"
        :tabnavs="tabs.filter(item => !item.hide)"
        @on-change="toggleTabHandle"
        :thin="true"
        class="detail-tabs" />

      <div class="detail-tabs-pane-wrap">
        <component
          :ref="activeName"
          :is="activeName"
          :queryParams="getQueryParams"
          :detail="detail"
          :isK8sNode="isK8sNode"
          @toggle-type="toggleTabHandle" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { copy } from '@/utils/common';
import { toAsyncWait } from '@/utils/common';
import InfraApi from '@/api/infrastructure';
import Construct from './construct.vue';
import Baseinfo from './baseinfo.vue';
import Metric from './metric.vue';
import Alarm from './alarm.vue';
import Process from './process.vue';
import Docker from './docker.vue';
import Pod from './pod.vue';

@Component({
  components: {
    Construct,
    Baseinfo,
    Metric,
    Alarm,
    Process,
    Docker,
    Pod,
  }
})
export default class HostDetail extends Vue {
  public $refs!: {
    construct: Construct
    baseinfo: Baseinfo
    metric: Metric
    alarm: Alarm
    process: Process
    docker: Docker
    pod: Pod
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private detail: any = {}
  private isLoading = false

  private hostName: string = ''

  private timeParams: any = {}

  get getQueryParams () {
    return {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      hostName: this.hostName || '',
    };
  }

  get isK8sNode () {
    const systemTags: string[] = this.detail['host-tags']?.system || [];
    return !!systemTags.find(t => t.startsWith('isk8s:') && t.split('isk8s:')[1] === 'true')
  }

  private tabs = [
    { label: i18n.t('modules.views.infrastructure.hostDetail.s_378562eb') as string, labelKey: 'modules.views.infrastructure.hostDetail.s_378562eb', value: 'construct' },
    { label: i18n.t('modules.views.alarmCenter.eventDetail.s_9e5ffa06') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_9e5ffa06', value: 'baseinfo' },
    { label: i18n.t('modules.utils.static.s_7e687515') as string, labelKey: 'modules.utils.static.s_7e687515', value: 'metric' },
    { label: i18n.t('modules.views.infrastructure.hostDetail.s_550a27f2') as string, labelKey: 'modules.views.infrastructure.hostDetail.s_550a27f2', value: 'alarm' },
    { label: i18n.t('modules.views.alarmCenter.alarm.s_f88522cf') as string, labelKey: 'modules.views.alarmCenter.alarm.s_f88522cf', value: 'process' },
    { label: i18n.t('modules.views.cockpit.tab.s_7cb69f1f') as string, labelKey: 'modules.views.cockpit.tab.s_7cb69f1f', value: 'docker', hide: true },
    { label: 'Pods', value: 'pod', hide: true },
  ]
  private activeName: string = 'construct'

  private created () {
    const { hostName = '', sn = '' } = this.$route.query;
    this.hostName = decodeURIComponent((hostName || sn) as string);

    const type: any = this.$route.query.type
    if (this.tabs.find(t => t.value === type)) {
      this.activeName = type
    }
  }

  private mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
    this.durationChangeHandle();
  }

  private beforeDestroy () {
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh')
  }

  // 时间范围改变
  private async durationChangeHandle () {
    this.timeParams = { ...this.getGlobalTimeV2() }
    if (!this.hostName) {
      return;
    }
    await this.getDetail()
    this.tabs.forEach((item) => {
      if (item.value === 'docker') {
        item.hide = this.isK8sNode
      } else if (item.value === 'pod') {
        item.hide = !this.isK8sNode
      } else {
        item.hide = false
      }
      if (item.hide && item.value === this.activeName) {
        this.activeName = this.tabs[0].value
        this.$router.replace({ query: { ...this.$route.query, type: this.activeName } })
      }
    })
    this.$nextTick(() => {
      const $refComp = (this.$refs as any)[this.activeName]
      if ($refComp && $refComp.getData) {
        $refComp.getData();
      }
    })
  }

  private toggleTabHandle(tab: any) {
    const { value } = tab;
    this.activeName = value
    const type = this.$route.query.type
    if (value !== type) {
      this.$router.replace({ query: { ...this.$route.query, type: value } })
    }
    if (!this.hostName) {
      return;
    }
    this.$nextTick(() => {
      const $refComp = (this.$refs as any)[this.activeName]
      if ($refComp && $refComp.getData) {
        $refComp.getData();
      }
    })
  }

  // 获取详情
  private async getDetail () {
    const params: any = { ...this.getQueryParams }
    delete params.interval;
    this.isLoading = true;
    const { result, error } = await toAsyncWait(InfraApi.getHostInfo(params));
    this.isLoading = false;
    const data = (result || {}).data
    if (!error && data?.id) {
      const hostIp = data.network ? data.network.ipaddress : ''
      const manageIp = (data.network || {}).managerIpaddress || ''
      this.detail = {
        ...data,
        hostName: data['df-hostname'] || data.hostName,
        hostOs: String((data.platform || {}).GOOS || '').toLocaleLowerCase(),
        alarmCount: data.alarmMetric?.total || 0,
        alarmPendingCount: data.alarmMetric?.total || 0,
        cpuUsage: (+data.lastReportCpu || 0) / 100,
        memoryUsage: (+data.lastReportMemUsedPercent || 0) / 100,
        memoryUsed: (+data.lastReportMem) * 1024 * 1024,
        hostIp,
        manageIp,
        apps: (data.apps || []).filter((a: string) => a && a !== 'kubelet'), // 过滤kubelet
        oneAgent: (data.userAgent || '').split('+git')[0],
      }
    } else {
      this.detail = {}
      this.$confirm(i18n.t('modules.views.infrastructure.hostDetail.s_38a639df') as string, i18n.t('common.hint') as string, {
        confirmButtonText: i18n.t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') as string, confirmButtonTextKey: 'modules.views.alarmCenter.alarmDetail.s_38cf16f2',
        closeOnClickModal: false,
        showCancelButton: false,
        showClose: false,
        type: 'warning'
      }).then(() => {
        this.$router.replace({
          path: '/infrastructure/host',
          query: { ...this.getRouteTimeOrRange }
        })
      })
    }
  }

  private copyHandle (text: string) {
    copy(text)
  }
}
</script>

<style lang="scss" scoped>
.db-detail-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  font-size: 13px;
  color: var(--color-text-primary);
  overflow: auto;

  .db-detail {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 600px;
    padding: 20px 16px;
    border-radius: 4px;
    background-color: var(--bg-color);
  }

  .detail-title {
    flex: none;
    padding-right: 148px;
    line-height: 20px;
    font-size: 16px;
    font-weight: 500;
    white-space: nowrap;
    position: relative;
    .detail-icon {
      display: inline-block;
      vertical-align: top;
      font-size: 20px;
    }
    .detail-name {
      box-sizing: border-box;
      max-width: calc(100% - 30px);
      display: inline-block;
      vertical-align: top;
      padding-right: 22px;
      height: 20px;
      position: relative;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      .copy-icon {
        transform: translate(0, -50%);
        position: absolute;
        top: 50%;
        right: 0;
        font-size: 14px;
        color: var(--color-primary);
        cursor: pointer;
      }
    }
    .detail-title-btns {
      position: absolute;
      top: 0;
      right: 0;
    }
    .detail-title-btn {
      width: 64px;
      padding-left: 0;
      padding-right: 0;
      .db-icon-right {
        margin-right: -6px;
      }
    }
  }

  .detail-tabs {
    flex: none;
    margin: 22px 4px 16px;
  }

  .detail-tabs-pane-wrap {
    flex: 1;
    padding: 0 4px;
    overflow: hidden;
  }

  .detail-status {
    display: inline-block;
    vertical-align: top;
    margin-right: 10px;
    padding: 0 6px;
    font-size: 12px;
    line-height: 20px;
    color: #fff;
    border-radius: 2px;
  }
}
</style>
