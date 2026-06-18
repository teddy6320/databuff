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
      tableKey='SERVICE_ANALYSIS_SERVICE_INSTANCE'
      ref='listTable'>
        
      <template slot='suffix'>
        <el-table-column key="actions" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" :width="140">
          <template slot-scope="{ row }">
            <span
              @click.stop="addQueryHandle(row)"
              :class="{ 'action-disabled': queryName && row.serviceInstance === queryName }"
              class="db-blue cphu">{{ $t('modules.views.appMonitor.serviceAnalysis.s_ab1a54fb') }}</span>
          </template>
        </el-table-column>
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
  @Prop({ default: '' }) private componentType!: string;

  public $refs!: {
    listTable: any;
  };

  private columnConfig: any[] = [
    { field: 'serviceInstance', prop: 'serviceInstance', label: i18n.t('modules.views.appMonitor.serviceAnalysis.s_352de256') as string, labelKey: 'modules.views.appMonitor.serviceAnalysis.s_352de256', minWidth: 120, handleClick: this.showDetailHandle },
  ];

  get getCloumns () {
    return this.columnConfig.filter((i) => !i.columnType || i.columnType === this.componentType)
  }

  private queryApi = ServiceApi.getBasicServiceInstanceV2;

  get tableQueryParams () {
    const query: any = {...this.queryParams}
    query.serviceId = query.sid;
    query.serviceInstance = query.si;
    if (query.srcSid) {
      query.srcServiceId = query.srcSid;
    }
    delete query.srcSid;
    delete query.sid;
    delete query.si;
    return {
      ...query
    }
  }

  get queryName () {
    const { si } = this.$route.query
    return si ? decodeURIComponent(String(si)) : ''
  }

  private created () {
    // console.log({...this.queryParams})
  }

  private showDetailHandle (row: any) {
    this.$router.push({
      path: '/appMonitor/serviceInstance',
      query: {
        si: encodeURIComponent(row.serviceInstance),
        sid: encodeURIComponent(this.queryParams.sid),
      }
    })
  }

  private tableInitedHandle () {
    this.$emit('on-table-inited')
  }

  private formatFunc (data: any) {
    //
  }

  public getData () {
    this.$refs.listTable?.refresh()
  }

  // 添加到搜索
  private addQueryHandle (row: any) {
    if (this.queryName && row.serviceInstance === this.queryName) {
      return
    }
    this.$router.replace({
      query: {
        ...this.$route.query,
        si: row.serviceInstance,
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
