<template>
  <div class="detail-baseinfo flex-v">
    <div class="font-14 line-height-22 fw-500 mb-16">{{ $t('modules.views.infrastructure.clusterDetail.s_c5ea2ca1') }}</div>

    <div class="baseinfo-section-item">
      <div class="flex-h wba"><div class="label">{{ $t('modules.views.configManage.entity.s_f0b09f88') }}</div>{{ detail.pname || '-' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.processDetail.s_568ede62') }}</div>{{ detail.pid || '' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.processDetail.s_3922308d') }}</div>{{ detail.ppname || '-' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.processDetail.s_bbb53cca') }}</div>{{ detail.ppid || '' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.appMonitor.serviceFlow.s_3d022a63') }}</div>
        <span
          @click="viewHostDetail"
          :class="{ 'blue hover-active-underline': detail.hostName }">
          <i class="db-icon icon-vm">{{ detail.hostOs | DbIconFilter('host') }}</i>
          {{ detail.hostName || '-' }}</span>
      </div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.dockerDetail.s_a51cd089') }}</div>
        <span
          @click="viewContainerDetail"
          :class="{ 'blue hover-active-underline': detail.containerId && detail.containerName }">{{ detail.containerName || '-' }}</span>
      </div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.processDetail.s_7e06872f') }}</div>{{ detail.imageVersion || '-' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.processDetail.s_bbf27755') }}</div>{{ detail.imageName || '-' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.processDetail.s_2d1c91bc') }}</div>{{ detail.shortImageName || '-' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.processDetail.s_4e7383a0') }}</div>{{ detail.createTime | TimesToDateFilter }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.processDetail.s_3c0142f1') }}</div>{{ detail.timestamp | TimesToDateFilter }}</div>
      <div class="flex-h mt-20 wba">
        <div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_59b7df17') }}</div>
        <!-- <span v-if='detail.service' class="cphu db-blue" @click='showServiceDetailHandle(detail)'>{{ detail.service }}</span> -->
        <template v-if='detail.serviceInstances && Array.isArray(detail.serviceInstances) && detail.serviceInstances.length'>
          <span v-for='item,index in detail.serviceInstances' :key='index' @click.stop="showServiceDetailHandle(item)" class="blue cphu mr-6">{{ item.service }}</span>
        </template>
        <span v-else>-</span>
      </div>
      <div class="flex-h mt-20 wba">
        <div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_caf5dc1e') }}</div>
        <template v-if='detail.serviceInstances && Array.isArray(detail.serviceInstances) && detail.serviceInstances.length'>
          <span v-for='item,index in detail.serviceInstances' :key='index' class="mr-6 blue cphu" @click.stop="showServiceInstanceDetailHandle(item)">{{ item.serviceInstance }}</span>
        </template>
        <span v-else>-</span>
      </div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.processDetail.s_dbb0d3bb') }}</div>{{ commands.join(' ') || '-' }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';

@Component
export default class DetailBaseinfo extends Vue {
  @Prop({ default: () => ({}) }) private detail!: any;

  get commands () {
    return ((this.detail.command || {}).args || []).filter((t: string) => t)
  }

  // 跳转到主机详情
  private viewHostDetail () {
    if (!this.detail.hostName) {
      return
    }
    this.$router.push({
      path: '/infrastructure/hostDetail',
      query: { hostName: encodeURIComponent(this.detail.hostName), }
    })
  }

  // 跳转到容器详情
  private viewContainerDetail () {
    if (!this.detail.containerId || !this.detail.containerName) {
      return
    }
    this.$router.push({
      path: '/infrastructure/dockerDetail',
      query: {
        containerId: encodeURIComponent(this.detail.containerId || ''),
      }
    });
  }

  // 查看服务详情
  private showServiceDetailHandle (row: any) {
    this.$router.push({
      path: '/appMonitor/serviceDetail',
      query: {
        ...this.getRouteTimeOrRange,
        sid: encodeURIComponent(row.serviceId)
      }
    })
  }
  
  // 查看服务实例详情
  private showServiceInstanceDetailHandle (row: any) {
    this.$router.push({
      path: '/appMonitor/serviceInstance',
      query: {
        ...this.getRouteTimeOrRange,
        sid: encodeURIComponent(row?.serviceId),
        si: encodeURIComponent(row.serviceInstance),
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.detail-baseinfo {
  height: 100%;
}

.baseinfo-section-item {
  flex: 1;
  width: 100%;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  padding: 20px;
  color: var(--color-text-primary);
  overflow: auto;
  .flex-h {
    align-items: flex-start;
  }
  .label {
    width: 140px;
    flex: none;
  }
}
</style>
