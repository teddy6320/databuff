<template>
  <db-table
    ref="listTable"
    :queryApi="queryApi"
    :queryParams="getQueryParams"
    :timeMode="false"
    :autoRefresh="false"
    :offsetMode="true"
    :showTotal="true"
    :showSetting="true"
    :columnConfig="columnConfig"
    :formatFunc="formatFunc"
    @sort-change="getData"
    @row-click="jumpProcessDetail"
    :row-style="{ cursor: 'pointer' }">
    <span slot="total" slot-scope="{ total }" class="describe">
      {{ $t('modules.views.infrastructure.dockerDetail.s_fed55c7b', { value0: total }) }}
    </span>
    <div slot="column-status" slot-scope="{ row }">
      <i :class="['status-tag', {
        'status-tag-success': row._processState === 0,
        'status-tag-default': row._processState === 1,
      }]"></i>
      <span>{{ row._processState | ProcessStateFilter }}</span>
    </div>
    <div slot="service" slot-scope="{ row }" class="ell">
      <template v-if='row.serviceInstances && Array.isArray(row.serviceInstances) && row.serviceInstances.length'>
        <span v-for='item,index in row.serviceInstances' :key='index' @click.stop="showServiceDetailHandle(item)" class="blue cphu mr-6">{{ item.service }}</span>
      </template>
      <span v-else>-</span>
    </div>
    <div slot="serviceInstance" slot-scope="{ row }" class="ell">
      <template v-if='row.serviceInstances && Array.isArray(row.serviceInstances) && row.serviceInstances.length'>
        <span v-for='item,index in row.serviceInstances' :key='index' class="mr-6 blue cphu" @click.stop="showServiceInstanceDetailHandle(item)">{{ item.serviceInstance }}</span>
      </template>
      <span v-else>-</span>
    </div>
  </db-table>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { ProcessOriginalStateFilter } from '@/utils/filters/infra';
import { BytesFilter } from '@/utils/filters/number';
import InfraApi from '@/api/infrastructure';

@Component
export default class DetailProcess extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;

  private queryApi = InfraApi.getProcessList
  private columnConfig = [
    { field: 'pname', label: i18n.t('modules.views.configManage.entity.s_f0b09f88') as string, labelKey: 'modules.views.configManage.entity.s_f0b09f88', minWidth: 300, handleClick: this.jumpProcessDetail, },
    { field: '_processState', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', slot: 'column-status', minWidth: 80, },
    // { field: 'hostName', label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369', prop: 'hostName', sortable: true, minWidth: 120, },
    { field: 'cpuUsage', label: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, labelKey: 'modules.views.appMonitor.relationMap.s_7054bc34', unit: 'percent', type: 'progress', progressMax: 1, prop: 'cpuTotalPct', sortable: true, minWidth: 120, },
    { field: 'usedMemory', label: i18n.t('modules.views.infrastructure.dockerDetail.s_6f60d25d') as string, labelKey: 'modules.views.infrastructure.dockerDetail.s_6f60d25d', unit: 'b', prop: 'memoryRss', type: 'progress', sortable: true, defaultSort: 'desc', minWidth: 100, },
    { field: 'ioRate', label: 'I/O', minWidth: 190, },
    { field: 'service', prop: 'service', label: i18n.t('modules.views.infrastructure.hostDetail.s_59b7df17') as string, labelKey: 'modules.views.infrastructure.hostDetail.s_59b7df17', slot: 'service', minWidth: 120, defaultShow: false, showOverflowTooltip: false },
    { field: 'serviceInstance', prop: 'serviceInstance', label: i18n.t('modules.views.infrastructure.hostDetail.s_caf5dc1e') as string, labelKey: 'modules.views.infrastructure.hostDetail.s_caf5dc1e', slot: 'serviceInstance', minWidth: 120, defaultShow: false, showOverflowTooltip: false },
  ]

  get getQueryParams () {
    const params: any = {
      ...this.queryParams,
      busProcess: true, // 仅显示业务进程
    }
    delete params.interval
    return params;
  }

  public getData () {
    (this.$refs.listTable as any)?.refresh()
  }

  private formatFunc (data: any) {
    return data.map((t: any) => ({
      ...t,
      _processState: ProcessOriginalStateFilter(t.processState),
      cpuUsage: (+t.cpuTotalPct || 0) / 100,
      usedMemory: +t.memoryRss || 0,
      ioRate: `${BytesFilter((+t.writeRate || 0) * 1024, false, '/s')} / ${BytesFilter((+t.readRate || 0) * 1024, false, '/s')}`,
    }));
  }

  // 跳转到进程详情
  private jumpProcessDetail (data: any) {
    this.$router.push({
      path: '/infrastructure/processDetail',
      query: {
        processName: encodeURIComponent(data.pname),
        hostName: encodeURIComponent(data.hostName),
      }
    })
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
