<template>
  <div class="db-detail-wrapper">
    <div class="db-detail" :class="`db-detail-${activeName}`" v-loading="isLoading">
      <div class="detail-title">
        <span
          :class="detail.health === 0 ? 'bg-green' : 'bg-red'"
          class="detail-status">{{ detail.health | HealthStatusFilter }}</span>
        <span class="detail-name">{{ detail.containerName || '-' }}
          <i @click.stop="copyHandle(detail.containerName || '-')" class="copy-icon db-icon-copy"></i>
        </span>
      </div>

      <db-tabnav
        v-model="activeName"
        :tabnavs="tabs"
        @on-change="toggleTabHandle"
        :thin="true"
        class="detail-tabs" />

      <div class="detail-tabs-pane-wrap">
        <component :ref="activeName" :is="activeName" :queryParams="getQueryParams" :detail="detail" />
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
import Metric from './metric.vue';
import Baseinfo from './baseinfo.vue';
import Process from './process.vue';
import Trace from './trace.vue';

@Component({
  components: {
    Metric,
    Baseinfo,
    Process,
    Trace,
  }
})
export default class DockerDetail extends Vue {
  public $refs!: {
    metric: Metric
    baseinfo: Baseinfo
    process: Process
    trace: Trace
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private detail: any = {}
  private isLoading = false

  private containerId: string = ''

  private timeParams: any = {}

  get getQueryParams () {
    return {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      containerId: this.containerId || '',
    };
  }

  private tabs = [
    { label: i18n.t('modules.views.infrastructure.clusterDetail.s_48a055d5') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_48a055d5', value: 'metric' },
    { label: i18n.t('modules.views.alarmCenter.eventDetail.s_9e5ffa06') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_9e5ffa06', value: 'baseinfo' },
    { label: i18n.t('modules.views.infrastructure.dockerDetail.s_40b657fc') as string, labelKey: 'modules.views.infrastructure.dockerDetail.s_40b657fc', value: 'process' },
    { label: i18n.t('modules.views.infrastructure.dockerDetail.s_acee2a62') as string, labelKey: 'modules.views.infrastructure.dockerDetail.s_acee2a62', value: 'trace' },
  ]
  private activeName: 'metric' | 'baseinfo' | 'process' | 'trace' = 'metric'

  private created () {
    const { containerId = '' } = this.$route.query;
    this.containerId = decodeURIComponent(containerId as string)

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
    if (!this.containerId) {
      return;
    }
    await this.getDetail()
    if (this.activeName === 'metric') {
      this.$refs.metric.getData()
    }
    if (this.activeName === 'process') {
      this.$refs.process.getData()
    }
    if (this.activeName === 'trace') {
      this.$refs.trace.getData()
    }
  }

  private toggleTabHandle(tab: any) {
    const { value } = tab;
    this.activeName = value
    const type = this.$route.query.type
    if (value !== type) {
      this.$router.replace({ query: { ...this.$route.query, type: value } })
    }
    if (!this.containerId) {
      return;
    }
    if (this.activeName === 'metric') {
      this.$refs.metric.getData()
    }
    if (this.activeName === 'process') {
      this.$refs.process.getData()
    }
    if (this.activeName === 'trace') {
      this.$refs.trace.getData()
    }
  }

  // 获取详情
  private async getDetail () {
    const params: any = {
      ...this.getQueryParams,
      isFuzzy: 0,
      offset: 0,
      size: 1,
    }
    delete params.interval;
    this.isLoading = true;
    const { result, error } = await toAsyncWait(InfraApi.getContainerList(params));
    this.isLoading = false;
    const data = ((result || {}).data || [])[0]
    if (!error && data) {
      const containerNameTag = (data.tags || []).find((g: any) => g.indexOf('container_name:') >= 0)
      const containerName = containerNameTag ? containerNameTag.split('container_name:')[1] : ''
      this.detail = {
        ...data,
        containerName: containerName || data.name,
        cpuUsage: typeof data.totalPct === 'number' ? data.totalPct / 100 : '-',
        // rbps: +data.rbps / 8,
        // wbps: +data.wbps / 8,
        health: data.health === 'healthy' || data.health === 'starting' ? 0 : 1,
      }
    } else {
      this.detail = {}
      this.$confirm(i18n.t('modules.views.infrastructure.dockerDetail.s_0945542a') as string, i18n.t('common.hint') as string, {
        confirmButtonText: i18n.t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') as string, confirmButtonTextKey: 'modules.views.alarmCenter.alarmDetail.s_38cf16f2',
        closeOnClickModal: false,
        showCancelButton: false,
        showClose: false,
        type: 'warning'
      }).then(() => {
        this.$router.replace({
          path: '/infrastructure/docker',
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
    padding: 20px;
    border-radius: 4px;
    background-color: var(--bg-color);
  }

  .detail-title {
    flex: none;
    line-height: 20px;
    font-size: 16px;
    font-weight: 500;
    white-space: nowrap;
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
  }

  .detail-tabs {
    flex: none;
    margin: 22px 0 16px;
  }

  .detail-tabs-pane-wrap {
    flex: 1;
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
