<template>
  <db-table
    ref="listTable"
    :queryApi="queryApi"
    :queryParams="getQueryParams"
    :timeMode="false"
    :autoRefresh="false"
    :offsetMode="true"
    :showTotal="true"
    :columnConfig="columnConfig"
    :formatFunc="formatFunc"
    @sort-change="getData"
    @row-click="jumpDockerDetail"
    :row-style="{ cursor: 'pointer' }">
  </db-table>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import InfraApi from '@/api/infrastructure';

@Component
export default class DetailDocker extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;

  private queryApi = InfraApi.getContainerList
  private columnConfig = [
    { field: 'containerName', label: i18n.t('modules.views.infrastructure.hostDetail.s_a51cd089') as string, labelKey: 'modules.views.infrastructure.dockerDetail.s_a51cd089', minWidth: 150, handleClick: this.jumpDockerDetail, },
    { field: 'image', label: i18n.t('modules.views.infrastructure.docker.s_34772285') as string, labelKey: 'modules.views.infrastructure.docker.s_34772285', minWidth: 120, defaultShow: false, },
    // { field: 'id', label: 'ID', minWidth: 120, defaultShow: false, },
    // { field: 'hostName', label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369', sortable: true, minWidth: 120, defaultShow: false, },
    { field: 'cpuUsage', label: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, labelKey: 'modules.views.appMonitor.relationMap.s_7054bc34', unit: 'percent', prop: 'totalPct', type: 'progress', progressMax: 1, sortable: true, minWidth: 110, },
    { field: 'memRss', label: i18n.t('modules.views.infrastructure.docker.s_d2340f93') as string, labelKey: 'modules.views.infrastructure.docker.s_d2340f93', unit: 'b', type: 'progress', sortable: true, defaultSort: 'desc', minWidth: 120, },
    // { field: 'rbps', label: 'r/s', unit: 'b', suffix: '/s', type: 'progress', sortable: true, minWidth: 100, defaultShow: false, },
    // { field: 'wbps', label: 'w/s', unit: 'b', suffix: '/s', type: 'progress', sortable: true, minWidth: 100, defaultShow: false, },
    { field: 'netSentBps', label: 'TX', unit: 'b', suffix: '/s', type: 'progress', sortable: true, minWidth: 100, },
    { field: 'netRcvdBps', label: 'RX', unit: 'b', suffix: '/s', type: 'progress', sortable: true, minWidth: 100, },
    { field: 'state', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', sortable: true, minWidth: 90, },
    { field: 'health', label: i18n.t('modules.views.appMonitor.cache.s_fb844b8b') as string, labelKey: 'modules.views.appMonitor.cache.s_fb844b8b', type: 'healthStatus', sortable: true, minWidth: 90, defaultShow: false, },
    { field: 'started', label: i18n.t('modules.views.infrastructure.docker.s_86cd8dce') as string, labelKey: 'modules.views.infrastructure.docker.s_86cd8dce', unit: 'time', sortable: true, minWidth: 140, },
    // { field: 'created', label: i18n.t('modules.views.appMonitor.serviceDetail.s_eca37cb0') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_eca37cb0', unit: 'time', sortable: true, minWidth: 140, defaultShow: false, },
  ]

  get getQueryParams () {
    const params: any = {
      ...this.queryParams,
    }
    delete params.interval
    return params;
  }

  public getData () {
    (this.$refs.listTable as any)?.refresh()
  }

  private formatFunc (data: any) {
    return data.map((t: any) => {
      const containerNameTag = (t.tags || []).find((g: any) => g.indexOf('container_name:') >= 0)
      const containerName = containerNameTag ? containerNameTag.split('container_name:')[1] : ''
      return {
        ...t,
        __id: Math.random(),
        containerName: containerName || t.name,
        cpuUsage: typeof t.totalPct === 'number' ? t.totalPct / 100 : '-',
        rbps: +t.rbps / 8,
        wbps: +t.wbps / 8,
        health: t.health === 'healthy' || t.health === 'starting' ? 0 : 1,
      }
    });
  }

  // 跳转到Docker详情
  private jumpDockerDetail (data: any) {
    this.$router.push({
      path: '/infrastructure/dockerDetail',
      query: {
        containerId: encodeURIComponent(data.id || ''),
      }
    })
  }
}
</script>
