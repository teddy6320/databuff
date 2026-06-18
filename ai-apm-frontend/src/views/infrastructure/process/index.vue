<template>
  <div class="db-process-wrapper">
    <div class="db-process">
      <div class="flex-h-jc">
        <search-group
          ref="searchGroup"
          :timeParams="getQueryParams"
          @on-change="searchChangeHandle" />

        <!-- <span class="describe font-13 lh-20">{{ $t('modules.views.alarmCenter.alarm.s_9ad9830c', { value0: listTotal }) }}</span> -->
        <el-checkbox
          v-model="busProcess"
          @change="tableRefresh"
          class="process-checkbox ml-10">{{ $t('modules.views.infrastructure.dockerDetail.s_6d06a8c5') }}</el-checkbox>
      </div>

      <div class="db-list">
        <db-table
          ref="listTable"
          :queryApi="queryApi"
          :queryParams="getQueryParams"
          :timeMode="false"
          :autoRefresh="false"
          :offsetMode="true"
          :showSetting='true'
          :columnConfig="columnConfig"
          :cell-class-name="({ column }) => column.property === 'pname' ? 'pname-cell' : ''"
          row-key="_id" lazy
          :load="getChildList"
          :formatFunc="formatFunc"
          @sort-change="tableRefresh"
          @on-fetch-end="tableFetchEndHandle"
          @row-click="jumpProcessDetail"
          :row-style="{ cursor: 'pointer' }">
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
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { ProcessOriginalStateFilter } from '@/utils/filters/infra';
import { BytesFilter } from '@/utils/filters/number';
import SearchGroup from './search-group.vue';
import { toAsyncWait } from '@/utils/common';
import InfraApi from '@/api/infrastructure';

@Component({
  components: {
    SearchGroup,
  },
})
export default class Process extends Vue {
  public $refs!: {
    searchGroup: SearchGroup
    listTable: any
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private busProcess = true; // 仅显示业务进程，默认为 true

  private queryParams: any = {}
  private timeParams: any = {}

  get getQueryParams () {
    const params: any = {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      pnameLike: this.queryParams.processName,
      hostNameLike: this.queryParams.hostName,
    }
    if (this.busProcess) {
      params.busProcess = this.busProcess
    }
    return params;
  }

  private queryApi = InfraApi.getProcessList
  private columnConfig = [
    { field: 'pname', label: i18n.t('modules.views.configManage.entity.s_f0b09f88') as string, labelKey: 'modules.views.configManage.entity.s_f0b09f88', minWidth: 400, extraClass: 'pname', handleClick: this.jumpProcessDetail, },
    { field: '_processState', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', slot: 'column-status', minWidth: 80, },
    { field: 'hostName', label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369', prop: 'hostName', sortable: true, minWidth: 120, },
    { field: 'cpuUsage', label: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, labelKey: 'modules.views.appMonitor.relationMap.s_7054bc34', unit: 'percent', type: 'progress', progressMax: 1, prop: 'cpuTotalPct', sortable: true, minWidth: 120, },
    { field: 'usedMemory', label: i18n.t('modules.views.infrastructure.dockerDetail.s_6f60d25d') as string, labelKey: 'modules.views.infrastructure.dockerDetail.s_6f60d25d', unit: 'b', prop: 'memoryRss', type: 'progress', sortable: true, defaultSort: 'desc', minWidth: 100, },
    { field: 'ioRate', label: 'I/O', minWidth: 190, },
    { field: 'service', prop: 'service', label: i18n.t('modules.views.infrastructure.hostDetail.s_59b7df17') as string, labelKey: 'modules.views.infrastructure.hostDetail.s_59b7df17', slot: 'service', minWidth: 120, defaultShow: false, showOverflowTooltip: false },
    { field: 'serviceInstance', prop: 'serviceInstance', label: i18n.t('modules.views.infrastructure.hostDetail.s_caf5dc1e') as string, labelKey: 'modules.views.infrastructure.hostDetail.s_caf5dc1e', slot: 'serviceInstance', minWidth: 120, defaultShow: false, showOverflowTooltip: false },
  ]

  private listTotal = 0
  private tableParams: any = {} // table上一次的请求参数

  private mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
    this.durationChangeHandle()
  }

  private beforeDestroy () {
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh')
  }

  private formatFunc (data: any) {
    return data.map((t: any) => ({
      ...t,
      _id: `${t.spuid}__${Math.random()}`,
      hasChildren: true,
      _processState: ProcessOriginalStateFilter(t.processState),
      cpuUsage: (+t.cpuTotalPct || 0) / 100,
      usedMemory: +t.memoryRss || 0,
      ioRate: `${BytesFilter((+t.writeRate || 0) * 1024, false, '/s')} / ${BytesFilter((+t.readRate || 0) * 1024, false, '/s')}`,
    }));
  }
  private tableFetchEndHandle (list: any[], total: number, params: any) {
    this.listTotal = total
    this.tableParams = params
  }
  private tableRefresh () {
    (this.$refs.listTable as any)?.refresh()
  }

  private durationChangeHandle () {
    this.timeParams = { ...this.getGlobalTimeV2() }
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        this.queryParams = { ...data };
        this.tableRefresh()
      })
    })
  }

  private searchChangeHandle (data: any) {
    this.queryParams = { ...data };
    this.tableRefresh();
  }

  private async getChildList (row: any, treeNode: any, resolve: any) {
    const { sortOrder, sortField } = this.tableParams
    const params: any = {
      ...this.getQueryParams,
      offset: 0,
      size: 50,
      ppid: row.pid,
      hostName: row.hostName,
    }
    delete params.pnameLike
    delete params.hostNameLike
    Object.entries(params).forEach(([key, value]) => {
      if (value === '') {
        delete params[key]
      }
    })
    if (sortOrder) {
      params.sortOrder = sortOrder
      params.sortField = sortField
    }
    const { result, error } = await toAsyncWait(InfraApi.getProcessList(params));
    const list = ((result || {}).data || []).filter((t: any) => t.pname !== row.pname).map((t: any) => ({
      ...t,
      _id: `${t.spuid}__${Math.random()}`,
      hasChildren: true,
      _processState: ProcessOriginalStateFilter(t.processState),
      cpuUsage: (+t.cpuTotalPct || 0) / 100,
      usedMemory: +t.memoryRss || 0,
      ioRate: `${BytesFilter((+t.writeRate || 0) * 1024, false, '/s')} / ${BytesFilter((+t.readRate || 0) * 1024, false, '/s')}`,
    }));
    resolve(list);
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

<style lang="scss" scoped>
.db-process-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .db-process {
    flex: 1;
    min-height: 300px;
    padding: 20px;
    border-radius: 4px;
    background-color: var(--bg-color);
    display: flex;
    flex-direction: column;
  }

  .db-list {
    // margin-top: 8px;
    flex: 1;
    overflow: hidden;
  }

  .process-checkbox {
    font-size: 12px;
    line-height: 18px;
    :deep(.el-checkbox__label) {
      font-size: 12px;
      line-height: 18px;
    }
  }

  :deep(.pname-cell) {
    padding: 0;

    .cell {
      padding: 9px 10px 8px;
      display: flex;
      align-items: center;
      overflow: visible;
    }

    .pname {
      flex: 1;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
    }

    .el-table__expand-icon {
      &::before {
        content: '';
        display: block;
        width: 9px;
        height: 9px;
        background: var(--color-text-placeholder);
        border-radius: 50%;
        position: absolute;
        top: 50%;
        left: 6px;
        transform: translate(0, -50%);
      }
      .el-icon-arrow-right::before {
        visibility: hidden;
      }
    }
    .el-table__expand-icon--expanded {
      transform: none;
    }

    .el-table__indent {
      position: relative;
      &::before {
        content: '';
        width: 100%;
        height: 40px;
        display: block;
        position: absolute;
        top: -40px;
        left: 0;
        background-image: linear-gradient(to right, var(--color-text-placeholder) 0 1px, transparent 1px 15px);
        background-position: 10px;
        background-size: 16px 100%;
        pointer-events: none;
      }
      &::after {
        content: '';
        width: 18px;
        border-bottom: 1px solid var(--color-text-placeholder);
        transform: translate(0, -50%);
        position: absolute;
        top: 50%;
        right: -12px;
        pointer-events: none;
      }
    }
  }
}
</style>
