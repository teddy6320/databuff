<template>
  <div class="apm-table-cont">
    <db-table
      :queryApi='queryApi'
      :queryParams='queryParams'
      :offsetMode='true'
      :showSetting='true'
      :columnConfig='columnConfig'
      :autoRefresh='false'
      @on-table-inited='tableInitedHandle'
      @sort-change='refresh'
      :formatFunc='formatFunc'
      tableKey='APM_MQ_LIST'
      ref='listTable'>
      <template slot="column-name" slot-scope="{ row }">
        <div class="service-name-item ell blue">
          <span class="db-icon mr-5 vm">{{ (row.type || row.language || row.service_type || 'default') | DbIconFilter }}</span>
          <span @click="showDetailHandle(row)" class="service-name-text cphu ell">{{ row.name || row.service || '-' }}</span>
        </div>
      </template>

      <template slot="column-healthStatus" slot-scope="{ row }">
        <el-tooltip placement="right" effect="light">
          <span>
            <!-- <span class="status-tag" :data-status='row.alarmCount ? 3 : 0'></span> -->
            <span :class='["db-icon font-12 vm", row.alarmPendingCount ? "db-icon-error-pie db-red" : "db-icon-right-pie db-green" ]'></span>
            {{ (row.alarmPendingCount > 0) | HealthStatusFilter }}
          </span>
          <template slot="content">
            <div v-if="row.alarmCount > 0">
              <div class="mb-5">
                {{ $t('modules.views.appMonitor.cache.s_24f58594', { value0: row.alarmCount }) }}<template v-if="row.alarmPendingCount">{{ $t('modules.views.appMonitor.cache.s_4fe75b47', { value0: row.alarmPendingCount }) }}</template><template v-else>{{ $t('modules.views.appMonitor.cache.s_24493430') }}</template>
              </div>
              <span @click="viewMoreEventHandle(row)" class="cphu blue">{{ $t('modules.views.appMonitor.relationMap.s_90ef7c48') }}</span>
            </div>
            <template v-else>{{ $t('modules.views.appMonitor.cache.s_ef984d39') }}</template>
          </template>
        </el-tooltip>
      </template>
    </db-table>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch, Prop } from 'vue-property-decorator'
import ServiceApi from '@/api/service';
import i18n from '@/i18n';

@Component({})
export default class ServiceTable extends Vue {
  @Prop({ default: {} }) private queryParams!: any;

  public $refs!: {
    listTable: any;
  };

  private columnConfig: any = [
    { field: 'name', label: i18n.t('modules.views.appMonitor.msgQueue.s_b12299c1') as string, labelKey: 'modules.views.appMonitor.msgQueue.s_b12299c1', slot: 'column-name', minWidth: 150 },
    { field: 'type', label: i18n.t('modules.views.appMonitor.cache.s_0c51881d') as string, labelKey: 'modules.views.appMonitor.cache.s_0c51881d', defaultShow: false,  minWidth: 150 },
    { field: 'healthStatus', label: i18n.t('modules.views.appMonitor.cache.s_fb844b8b') as string, labelKey: 'modules.views.appMonitor.cache.s_fb844b8b', slot: 'column-healthStatus', minWidth: 120 },
    { field: 'reqInCallCnt', prop: 'reqInCallCnt', label: i18n.t('modules.views.appMonitor.msgQueue.s_38633a2b') as string, labelKey: 'modules.views.appMonitor.msgQueue.s_38633a2b', unit: 'count', minWidth: 120, sortable: true, },
    { field: 'reqInAvgLatency', prop: 'reqInAvgLatency', label: i18n.t('modules.views.appMonitor.msgQueue.s_7e8a3917') as string, labelKey: 'modules.views.appMonitor.msgQueue.s_7e8a3917', unit: 'ns', minWidth: 100, type: 'progress', sortable: true, defaultSort: 'desc', },
    { field: 'reqInErrRate', prop: 'reqInErrRate', label: i18n.t('modules.views.appMonitor.msgQueue.s_f50a7462') as string, labelKey: 'modules.views.appMonitor.msgQueue.s_f50a7462', unit: 'percent', lessZeroOneKey: 'reqInErrCnt', minWidth: 100, type: 'progress', progressDirection: 'horizontal', progressType: 'circle', progressStatus: 'exception', progressBarWidth: 3, progressMax: 1, sortable: true },
    { field: 'reqOutCallCnt', prop: 'reqOutCallCnt', label: i18n.t('modules.views.appMonitor.msgQueue.s_b865adcd') as string, labelKey: 'modules.views.appMonitor.msgQueue.s_b865adcd', unit: 'count', minWidth: 120, sortable: true, },
    { field: 'reqOutAvgLatency', prop: 'reqOutAvgLatency', label: i18n.t('modules.views.appMonitor.msgQueue.s_77e51e36') as string, labelKey: 'modules.views.appMonitor.msgQueue.s_77e51e36', unit: 'ns', minWidth: 100, type: 'progress', sortable: true, defaultSort: 'desc', },
    { field: 'reqOutErrRate', prop: 'reqOutErrRate', label: i18n.t('modules.views.appMonitor.msgQueue.s_65341d11') as string, labelKey: 'modules.views.appMonitor.msgQueue.s_65341d11', unit: 'percent', lessZeroOneKey: 'reqOutErrCnt', minWidth: 100, type: 'progress', progressDirection: 'horizontal', progressType: 'circle', progressStatus: 'exception', progressBarWidth: 3, progressMax: 1, sortable: true },
    { field: 'businessLineName', prop: 'businessLineName', label: i18n.t('modules.views.appMonitor.cache.s_73a1c3b8') as string, labelKey: 'modules.views.appMonitor.cache.s_73a1c3b8', minWidth: 120, defaultShow: false },
  ];

  private queryApi = ServiceApi.getMqList


  private created () {
    //
  }

  private showDetailHandle (row: any) {
    this.$router.push({
      path: '/appMonitor/msgQueue/detail',
      query: {
        sn: encodeURIComponent(row.service),
        sid: encodeURIComponent(row.serviceId),
      }
    })
  }

  // 服务关联事件查看更多，需携带各状态事件数量
  private viewMoreEventHandle (row: any = {}) {
    this.$router.push({
      path: '/alarmCenter/alarm',
      query: {
        serviceId: encodeURIComponent(row.serviceId),
      }
    })
  }

  private tableInitedHandle () {
    this.$emit('on-table-inited')
  }

  private formatFunc (data: any) {
    data.forEach((i: any) => {
      i.errorRate = i.errRate
      i.reqCnt = i.callCnt
      i.alarmCount = i.alarmMetric?.total || 0
      i.alarmPendingCount = i.alarmMetric?.total || 0
    });
  }

  public refresh () {
    this.$refs.listTable?.refresh()
  }
}
</script>

<style lang="scss" scoped>
.apm-table-cont {
  width: 100%;
  flex: 1;
  overflow: auto;
}
.monitor-event-info-tip {
  margin-bottom: 5px;
  padding-left: 6px;
}
</style>
