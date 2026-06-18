<template>
  <div class="apm-table-cont">
    <db-table
      :queryApi='queryApi'
      :queryParams='tableQueryParams'
      :offsetMode='true'
      :columnConfig='getCloumns'
      :autoRefresh='false'
      @on-table-inited='tableInitedHandle'
      @sort-change='getData'
      :formatFunc='formatFunc'
      tableKey='SERVICE_ANALYSIS_SERVICE'
      ref='listTable'>

      <template slot='serviceName' slot-scope="{ row }">
        <div class="flex-h-jc">
          <span class="db-icon db-blue mr-5 vm">{{ (row.serviceType || row.service_type || row.type || 'default') | DbIconFilter }}</span>
          <span class="flex-1 db-blue cphl" @click="showDetailHandle(row)">{{ row.serviceName || row.srcService || '-' }}</span>
          <span @click.stop="addQueryHandle(row)" class="db-icon db-icon-filter describe cphl mr-5" :class="{ 'action-disabled': queryName && row.serviceId === queryName }" :title="$t('modules.views.appMonitor.serviceAnalysis.s_ab1a54fb')"></span>
        </div>
      </template>
        
      <!-- <template slot='suffix'>
        <el-table-column key="actions" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" :width="140">
          <template slot-scope="{ row }">
            <span
              @click.stop="addQueryHandle(row)"
              :class="{ 'action-disabled': queryName && row.serviceId === queryName }"
              class="db-blue cphu">{{ $t('modules.views.appMonitor.errors.s_ab1a54fb') }}</span>
          </template>
        </el-table-column>
      </template> -->
    </db-table>
  </div>
</template>

<script lang="ts">import i18n from '@/i18n';

import { Vue, Component, Watch, Prop } from 'vue-property-decorator'
import ApmApi from '@/api/apm'

@Component({})
export default class ServiceTable extends Vue {
  @Prop({ default: {} }) private queryParams!: any;
  @Prop({ default: '' }) private componentType!: string;

  public $refs!: {
    listTable: any;
  };

  private columnConfig: any[] = [
    { field: 'serviceName', prop: 'serviceName', label: i18n.t('modules.views.alarmCenter.alarm.s_8f3747c0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_8f3747c0', slot: 'serviceName', minWidth: 120 },
    { field: 'serviceType', prop: 'serviceType', label: i18n.t('modules.views.appMonitor.service.s_924f67de') as string, labelKey: 'modules.views.appMonitor.service.s_924f67de', minWidth: 120 },
    { field: 'callCnt', prop: 'callCnt', label: i18n.t('modules.views.appMonitor.cache.s_8bc42b53') as string, labelKey: 'modules.views.appMonitor.cache.s_8bc42b53', unit: 'count', minWidth: 100, type: 'progress', sortable: true, defaultSort: 'desc', },
  ];

  get getCloumns () {
    return this.columnConfig.filter((i) => !i.columnType || i.columnType === this.componentType)
  }

  private queryApi = ApmApi.getReqContributorService;

  get tableQueryParams () {
    const query: any = { ...this.queryParams }
    query.serviceId = query.sid
    query.serviceInstance = query.si
    if (query.srcSid) {
      query.srcServiceId = query.srcSid;
    }
    delete query.srcSid;
    delete query.si
    delete query.sid
    return {
      ...query,
      isIn: 1,
      componentType: this.componentType,
    }
  }

  get queryName () {
    const { srcSid } = this.$route.query
    return srcSid ? decodeURIComponent(String(srcSid)) : ''
  }

  get getBasicServiceMap () {
    return this.$store.getters['Service/basicServiceMap']
  }

  private created () {
    //
    console.log(123)
  }

  private showDetailHandle (row: any) {
    const serviceName = row.serviceName || row.srcService || ''
    this.$router.push({
      path: '/appMonitor/serviceDetail',
      query: {
        sn: encodeURIComponent(serviceName),
        sid: encodeURIComponent(row.serviceId || row.srcServiceId),
      }
    })
  }

  private tableInitedHandle () {
    this.$emit('on-table-inited')
  }

  private formatFunc (data: any) {
    data.forEach((i: any) => {
      const { service_type, type, language } = (this.getBasicServiceMap || {})[i?.serviceId] || {}
      i.type = i.serviceType
      i.serviceType = type || language || service_type
    });
  }

  public getData () {
    this.$refs.listTable?.refresh()
  }

  // 添加到搜索
  private addQueryHandle (row: any) {
    if (this.queryName && row.serviceId === this.queryName) {
      return
    }
    this.$router.replace({
      query: {
        ...this.$route.query,
        srcSid: encodeURIComponent(row.serviceId || row.srcServiceId),
        srcSn: encodeURIComponent(row.serviceName || row.srcService || ''),
      }
    });
    this.$emit('add-query');
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
.action-disabled {
  cursor: not-allowed;
  color: var(--color-text-placeholder);
}
</style>
