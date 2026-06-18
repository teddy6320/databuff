<template>
  <div class="db-detail-wrapper">
    <div class="db-detail" :class="`db-detail-${activeName}`" v-loading="isLoading">
      <div class="detail-title">
        <span class="db-icon-node detail-icon mr-10"></span>
        <span class="detail-name">{{ getQueryParams.nodeName }}
          <i @click.stop="copyHandle(getQueryParams.nodeName || '-')" class="copy-icon db-icon-copy"></i>
        </span>
      </div>
      <div class="detail-info">
        <div class="info-item">Cluster:<span class="ml-8">{{ detail.clusterName || '-' }}</span></div>
        <div class="info-item">Pods:<span class="ml-8">{{ detail.podCount | NumberFilter }}</span></div>
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
import { toAsyncWait } from '@/utils/common';
import { copy } from '@/utils/common';
import KubernetesApi from '@/api/kubernetes';
import Overview from './overview.vue';
import Baseinfo from './baseinfo.vue';
import Metric from './metric.vue';

@Component({
  components: {
    Overview,
    Baseinfo,
    Metric,
  }
})
export default class NodeDetail extends Vue {
  public $refs!: {
    overview: Overview
    baseinfo: Baseinfo
    metric: Metric
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private detail: any = {}
  private isLoading = false

  private clusterId: string = ''
  private nodeId: string = ''
  private nodeName: string = ''

  private timeParams: any = {}

  get getQueryParams () {
    return {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      clusterId: this.clusterId || this.detail.clusterId || '',
      nodeId: this.nodeId || this.detail.uid || '',
      nodeName: this.nodeName || this.detail.name || '',
    };
  }

  private tabs = [
    { label: i18n.t('modules.views.infrastructure.clusterDetail.s_81a71d43') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_81a71d43', value: 'overview' },
    { label: i18n.t('modules.views.alarmCenter.eventDetail.s_9e5ffa06') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_9e5ffa06', value: 'baseinfo' },
    { label: i18n.t('modules.views.infrastructure.clusterDetail.s_48a055d5') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_48a055d5', value: 'metric' },
  ]
  private activeName: 'overview' | 'baseinfo' | 'metric' = 'overview'

  private created () {
    const { kid = '', kn = '', cid = '' } = this.$route.query;
    this.clusterId = decodeURIComponent(cid as string)
    this.nodeId = decodeURIComponent(kid as string)
    this.nodeName = decodeURIComponent(kn as string)

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
    await this.getDetail()
    if (this.activeName === 'overview') {
      this.$refs.overview.getData()
    }
    if (this.activeName === 'metric') {
      this.$refs.metric.getData()
    }
  }

  private toggleTabHandle(tab: any) {
    const { value } = tab;
    this.activeName = value
    const type = this.$route.query.type
    if (value !== type) {
      this.$router.replace({ query: { ...this.$route.query, type: value } })
    }
    if (this.activeName === 'overview') {
      this.$refs.overview.getData()
    }
    if (this.activeName === 'metric') {
      this.$refs.metric.getData()
    }
  }

  // 获取详情
  private async getDetail () {
    const { fromTime, toTime, nodeId, clusterId } = this.getQueryParams
    const params: any = {
      resourceUid: nodeId,
      clusterId,
      fromTime,
      toTime,
    }
    this.isLoading = true;
    const { result, error } = await toAsyncWait(KubernetesApi.getNodeDetail(params));
    this.isLoading = false;
    const data = result?.data
    if (!error && data?.uid) {
      const nodeStatus = data.status || {}
      const capacity = nodeStatus.capacity || {}
      const nodeAddresses = nodeStatus.nodeAddresses || {}
      this.detail = {
        ...data,
        kubeletVersion: nodeStatus.kubeletVersion || '-',
        internalIP: nodeAddresses.InternalIP || '-',
        cpuCapacity: (+capacity.cpu || 0) / 1000,
        cpuRequest: +data.cpuRequest || 0,
        cpuUsage: +data.cpuUsage || 0,
        cpuLimit: +data.cpuLimit || 0,
        memCapacity: +capacity.memory || 0,
        memRequest: +data.memRequest || 0,
        memUsage: +data.memUsage || 0,
        memLimit: +data.memLimit || 0,
        podCount: +data.podCount || Object.values((data.podStatusCount || {})).reduce((n: any, c: any) => n + (c || 0), 0),
      }
    } else {
      this.detail = {}
      this.$confirm(i18n.t('modules.views.infrastructure.nodeDetail.s_1c88d7bf') as string, i18n.t('common.hint') as string, {
        confirmButtonText: i18n.t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') as string, confirmButtonTextKey: 'modules.views.alarmCenter.alarmDetail.s_38cf16f2',
        closeOnClickModal: false,
        showCancelButton: false,
        showClose: false,
        type: 'warning'
      }).then(() => {
        this.$router.replace({
          path: '/infrastructure/node',
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
  color: var(--color-text-regular);
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

  .detail-info {
    flex: none;
    margin-top: 12px;
    display: flex;
    font-size: 12px;
    line-height: 14px;
    color: var(--color-text-secondary);
    .info-item {
      margin-right: 28px;
      span {
        color: var(--color-text-primary);
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
}
</style>
