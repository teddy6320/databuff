<template>
  <div class="detail-overview">
    <div class="flex-h-jc">
      <div class="section-item">
        <div class="section-title ">CPU
          <!-- <div class="font-12 fw-400 describe">{{ $t('modules.views.infrastructure.clusterDetail.s_09565e82') }}<span class="default-text">{{ detail.cpuCapacity | NumberFilter }} core</span></div> -->
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
          <!-- <div class="font-12 fw-400 describe">{{ $t('modules.views.infrastructure.clusterDetail.s_09565e82') }}<span class="default-text">{{ detail.memCapacity | BytesFilter }}</span></div> -->
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
      :queryApi='workloadQueryApi'
      :queryParams='getQueryParams'
      :timeMode="false"
      :autoRefresh="false"
      :offsetMode='true'
      :columnConfig='workloadColumnConfig'
      @sort-change='workloadRefresh'
      :formatFunc='workloadFormatSourceFunc'
      ref='workloadTable'
      @row-click="jumpWorkloadDetail"
      :row-style="{ cursor: 'pointer' }"
      class="table-item">
      <div slot='total' slot-scope="{ total }" class="flex-h lh-20 mb-5">
        <span class="fw-500 mr-8">Workloads</span>
        {{ total }}
      </div>
    </db-table>

    <db-table
      :queryApi='serviceQueryApi'
      :queryParams='getQueryParams'
      :timeMode="false"
      :autoRefresh="false"
      :offsetMode='true'
      :columnConfig='serviceColumnConfig'
      @sort-change='serviceRefresh'
      :formatFunc='serviceFormatSourceFunc'
      ref='serviceTable'
      @row-click="jumpServiceDetail"
      :row-style="{ cursor: 'pointer' }"
      class="table-item">
      <div slot='total' slot-scope="{ total }" class="flex-h lh-20 mb-5">
        <span class="fw-500 mr-8">Kubernetes Services</span>
        {{ total }}
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
      nsName: this.queryParams.namespaceName || '',
    };
  }

  private workloadQueryApi = KubernetesApi.getWorkloadList;
  private workloadColumnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 200, defaultSort: 'asc', handleClick: this.jumpWorkloadDetail, },
    { field: 'type', label: i18n.t('modules.views.alarmCenter.eventDetail.s_226b0912') as string, labelKey: 'modules.views.aiPlatform.experts.s_226b0912', minWidth: 100, unit: 'clusterType', },
    { field: 'pods', label: 'Pods', minWidth: 100, },
    // { field: 'namespace', label: 'Namespace', minWidth: 100, },
    // { field: 'clusterName', label: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, labelKey: 'modules.views.infrastructure.cluster.s_85fe5099', minWidth: 100, },
  ]
  private workloadFormatSourceFunc (data: any) {
    data.forEach((t: any) => {
      t.id = t.uid
      t.pods = `${t.readyReplicas || 0} of ${t.replicasDesired || 0}`
    });
  }
  private workloadRefresh () {
    (this.$refs.workloadTable as any)?.refresh()
  }

  private serviceQueryApi = KubernetesApi.getServiceList;
  private serviceColumnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 200, defaultSort: 'asc', handleClick: this.jumpServiceDetail, },
    { field: 'age', label: i18n.t('modules.views.infrastructure.clusterDetail.s_a01a9963') as string, labelKey: 'modules.views.infrastructure.clusterDetail.s_a01a9963', unit: 'sDuration', minWidth: 100, },
    { field: 'type', label: i18n.t('modules.views.alarmCenter.eventDetail.s_226b0912') as string, labelKey: 'modules.views.aiPlatform.experts.s_226b0912', minWidth: 100, },
    { field: 'clusterIP', label: 'Cluster IP', minWidth: 100, },
    { field: 'externalIPs', label: 'External IP', minWidth: 100, },
    { field: 'ports', label: 'Ports', minWidth: 150, },
    // { field: 'namespace', label: 'Namespace', minWidth: 100, },
  ]
  private serviceFormatSourceFunc (data: any) {
    data.forEach((t: any) => {
      const spec = t.spec || {}
      t.id = t.uid
      t.age = t.creationTimestamp ? +new Date() / 1000 - t.creationTimestamp : '-'
      t.type = spec.type || '-'
      t.clusterIP = spec.clusterIP || '-'
      t.externalIPs = (spec.externalIPs || []).join(',') || '-'
      t.ports = (spec.ports || []).map((p: any) => `${[p.port, p.nodePort].filter(c => !!c).join(':') || '-'}/${p.protocol}`).join(', ') || '-'
    });
  }
  private serviceRefresh () {
    (this.$refs.serviceTable as any)?.refresh()
  }

  public getData () {
    this.workloadRefresh()
    this.serviceRefresh()
  }

  // 跳转到Workload详情
  private jumpWorkloadDetail (data: any) {
    this.$router.push({
      path: '/infrastructure/workloadDetail',
      query: {
        kid: encodeURIComponent(data.id),
        kn: encodeURIComponent(data.name),
      }
    })
  }

  // 跳转到Service详情
  private jumpServiceDetail (data: any) {
    this.$router.push({
      path: '/infrastructure/serviceDetail',
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
  height: calc((100% - 168px - 24px) / 2);
}
</style>
