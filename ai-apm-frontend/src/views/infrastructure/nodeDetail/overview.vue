<template>
  <div class="detail-overview">
    <div class="flex-h-jc">
      <div class="section-item">
        <div class="section-title ">CPU
          <div class="font-12 fw-400 describe">{{ $t('modules.views.infrastructure.clusterDetail.s_09565e82') }}<span class="default-text">{{ detail.cpuCapacity | NumberFilter }} core</span></div>
        </div>
        <div class="section-cont">
          <horizontal-bar
            :source="cpuSource"
            unit="core"
            :colors="['#2962FF', '#678FFF', '#00AFF4']"
            :itemGap="12"
            valuePosition="top"
            order="none" />
          <!-- :maxValue="detail.cpuCapacity" -->
        </div>
      </div>

      <div class="section-item">
        <div class="section-title">{{ $t('modules.views.appMonitor.hotMethods.s_9932551c') }}
          <div class="font-12 fw-400 describe">{{ $t('modules.views.infrastructure.clusterDetail.s_09565e82') }}<span class="default-text">{{ detail.memCapacity | BytesFilter }}</span></div>
        </div>
        <div class="section-cont">
          <horizontal-bar
            :source="memorySource"
            unit="B"
            :colors="['#2962FF', '#678FFF', '#00AFF4']"
            :itemGap="12"
            valuePosition="top"
            order="none" />
          <!-- :maxValue="detail.memCapacity" -->
        </div>
      </div>

      <div class="section-item">
        <div class="section-title">Pods
          <div class="font-12 fw-400 describe">{{ $t('modules.views.infrastructure.clusterDetail.s_09565e82') }}<span class="default-text">{{ detail.podCount | NumberFilter }}</span></div>
        </div>
        <div class="section-cont">
          <horizontal-bar
            :source="podSource"
            :colors="['#2962FF', '#678FFF', '#00AFF4']"
            :itemGap="12"
            valuePosition="top"
            order="none" />
          <!-- :maxValue="detail.podCount" -->
        </div>
      </div>
    </div>

    <db-table
      :queryApi='podQueryApi'
      :queryParams='getQueryParams'
      :timeMode="false"
      :autoRefresh="false"
      :offsetMode='true'
      :columnConfig='podColumnConfig'
      @sort-change='podRefresh'
      :formatFunc='podFormatSourceFunc'
      ref='podTable'
      @row-click="jumpPodDetail"
      :row-style="{ cursor: 'pointer' }"
      class="table-item">
      <div slot='total' slot-scope="{ total }" class="flex-h lh-20 mb-5">
        <span class="fw-500 mr-8">Pods</span>
        {{ total }}
      </div>
      <div slot='status' slot-scope="{ row }" class="flex-h">
        <i v-if='!row.healthStatus' class="db-icon db-icon-error-pie font-12 mr-5 db-red"></i>
        <i v-else class="db-icon db-icon-right-pie font-12 mr-5 db-green"></i>
        {{ row.status }}
      </div>
    </db-table>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { orderBy } from 'lodash';
import HorizontalBar from '@/components/charts/horizontal-bar.vue';
import KubernetesApi from '@/api/kubernetes';

@Component({
  components: {
    HorizontalBar,
  }
})
export default class DetailOverview extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: () => ({}) }) private detail!: any;

  get cpuSource () {
    return [
      { name: 'Usage', value: this.detail.cpuUsage },
      { name: 'Requests', value: this.detail.cpuRequest },
      { name: 'Limits', value: this.detail.cpuLimit },
    ]
  }

  get memorySource () {
    return [
      { name: 'Usage', value: this.detail.memUsage },
      { name: 'Requests', value: this.detail.memRequest },
      { name: 'Limits', value: this.detail.memLimit },
    ]
  }

  get podSource () {
    const podStatusCount = this.detail.podStatusCount || {};
    const source = Object.entries(podStatusCount).map(([key, value]) => ({ name: key, value }))
    return orderBy(source, ['value'], ['desc']).slice(0, 3);
  }

  get getQueryParams () {
    return {
      fromTime: this.queryParams.fromTime,
      toTime: this.queryParams.toTime,
      interval: this.queryParams.interval,
      clusterId: this.queryParams.clusterId || '',
      nodeName: this.queryParams.nodeName || '',
    };
  }

  private podQueryApi = KubernetesApi.getPodList;
  private podColumnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 160, defaultSort: 'asc', handleClick: this.jumpPodDetail, },
    { field: 'type', label: i18n.t('modules.views.alarmCenter.eventDetail.s_226b0912') as string, labelKey: 'modules.views.aiPlatform.experts.s_226b0912', minWidth: 60, unit: 'clusterType', },
    { field: 'status', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', slot: 'status', minWidth: 120, },
    // { field: 'clusterName', label: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, labelKey: 'modules.views.infrastructure.cluster.s_85fe5099', minWidth: 120, },
    { field: 'namespace', label: 'Namespace', minWidth: 120, },
    { field: 'workload', label: 'Workloads', minWidth: 120, },
    // { field: 'nodeName', label: 'Node', minWidth: 120, },
    { field: 'age', label: i18n.t('modules.views.infrastructure.clusterDetail.s_a01a9963') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_a01a9963', unit: 'sDuration', minWidth: 100, },
    { field: 'ip', label: i18n.t('modules.views.configManage.entity.s_2dc9105c') as string, labelKey: 'modules.views.configManage.entity.s_2dc9105c', minWidth: 120, },
    { field: 'container', label: i18n.t('modules.views.appMonitor.serviceDetail.s_22c79904') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_22c79904', unit: 'count', minWidth: 60, },
    { field: 'containerRestart', label: i18n.t('modules.views.infrastructure.clusterDetail.s_ce6b4ac9') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_ce6b4ac9', unit: 'count', minWidth: 100, },
  ]
  private podFormatSourceFunc (data: any) {
    data.forEach((t: any) => {
      t.id = t.uid
      t.workload = t.wlName
      t.ip = t.IP
      t.age = t.creationTimestamp ? +new Date() / 1000 - t.creationTimestamp : '-'
      t.container = (t.containerStatuses || []).length
      t.containerRestart = (t.containerStatuses || []).reduce((n: number, c: any) => n + (c.restartCount || 0), 0)
    });
  }
  private podRefresh () {
    (this.$refs.podTable as any)?.refresh()
  }

  public getData () {
    this.podRefresh()
  }

  // 跳转到Pod详情
  private jumpPodDetail (data: any) {
    this.$router.push({
      path: '/infrastructure/podDetail',
      query: {
        kid: encodeURIComponent(data.id),
        kn: encodeURIComponent(data.name),
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.detail-overview {
  height: 100%;
}

.section-item {
  width: calc((100% - 32px) / 3);
  height: 168px;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;

  .section-title {
    display: flex;
    justify-content: space-between;
    padding: 16px 20px 0;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    color: var(--color-text-primary);
  }

  .section-cont {
    height: calc(100% - 38px);
    padding: 12px 20px 0;
  }
}

.table-item {
  margin-top: 12px;
  height: calc(100% - 168px - 12px);
}
</style>
