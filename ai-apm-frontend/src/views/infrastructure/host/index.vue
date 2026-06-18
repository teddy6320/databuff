<template>
  <div class="db-host-wrapper">
    <div class="db-host">
      <div class="flex-h pl-20 pr-20">
        <el-radio-group v-model="displayMode" @change="toggleModeHandle" size="small" class="radio-group mr-10">
          <el-tooltip placement="top" effect="light" :content="$t('modules.views.infrastructure.host.s_421d8e65')">
            <el-radio-button label="table" class="radio-btn">
              <span class="db-icon-list icon-vm"></span>
            </el-radio-button>
          </el-tooltip>
          <el-tooltip placement="top" effect="light" content="Hostmap">
            <el-radio-button label="chart" class="radio-btn">
              <span class="db-icon-infrastructure icon-vm"></span>
            </el-radio-button>
          </el-tooltip>
        </el-radio-group>

        <search-group
          ref="searchGroup"
          :timeParams="getQueryParams"
          @on-change="searchChangeHandle" />

        <el-select
          v-model="groupSelected"
          @change="groupChangeHandle"
          multiple :collapse-tags="true"
          clearable size="small"
          :placeholder="$t('modules.views.infrastructure.docker.s_ec22193e')"
          class="ml-10 group-select">
          <el-option v-for="t in groupList" :key="t" :label="t" :value="t"></el-option>
        </el-select>
      </div>

      <div class="db-host-body pl-20 pr-20">
        <choose-collapse
          ref="chooseCollapse"
          :timeParams="getQueryParams"
          @on-filter-change="filterChangeHandle"
          @on-filter-toggle="filterToggleHandle"
          class="collapse-choose"
        />

        <div class="db-list">
          <db-table
            v-if="displayMode === 'table'"
            ref="listTable"
            :queryApi="queryApi"
            :queryParams="getQueryParams"
            :timeMode="false"
            :autoRefresh="false"
            :offsetMode="true"
            :showSelection="true"
            showSetting
            tableKey="INFRA_HOST_LIST"
            row-key="__id"
            :columnConfig="columnConfig"
            :formatFunc="formatFunc"
            :selectableFunc="canDrillDown"
            @selection-change="selectionChangeHandle"
            @sort-change="tableRefresh"
            @row-click="rowClickHandle"
            :row-style="{ cursor: 'pointer' }">
            <div slot="total" slot-scope="{ total }" class="flex-1 flex-h-jc mr-10">
              <span class="describe">{{ $t('modules.views.infrastructure.docker.s_b7dbd20a', { value0: total, value1: !groupSelected.length ? $t('modules.views.alarmCenter.alarm.s_65227369') : $t('modules.views.appMonitor.traceDetail.s_829abe5a') }) }}</span>
              <el-button
                @click="showAddLabelHandle"
                :disabled="!selection.length || batchLoading"
                :type="selection.length ? 'primary' : ''"
                plain size="small" class="ml-10">
                <i class="db-icon-add"></i>{{ $t('modules.views.infrastructure.host.s_736eaaae') }}
              </el-button>
            </div>

            <template slot="column-hostName" slot-scope="{ row, column }">
              <template v-if="row.isGroup">{{ row.hostName || '-' }}</template>
              <template v-else>
                <span class="db-icon mr-5 vm db-blue">{{ row.hostOs | DbIconFilter('host') }}</span>
                <span @click.stop="column.handleClick(row)"  class="db-blue cphu ell">{{ row.hostName || '-' }}</span>
              </template>
            </template>

            <el-tooltip slot="column-health" slot-scope="{ row }" placement="right" effect="light">
              <span>
                <span :class='["db-icon font-12 icon-vm mr-5", row.alarmPendingCount ? "db-icon-error-pie db-red" : "db-icon-right-pie db-green" ]'></span>
                <span>{{ (row.alarmPendingCount > 0) | HealthStatusFilter }}</span>
              </span>
              <template slot="content">
                <div v-if="row.alarmCount > 0">
                  <div class="mb-5">
                    {{ $t('modules.views.appMonitor.cache.s_24f58594', { value0: row.alarmCount }) }}<template v-if="row.alarmPendingCount">{{ $t('modules.views.appMonitor.cache.s_4fe75b47', { value0: row.alarmPendingCount }) }}</template><template v-else>{{ $t('modules.views.appMonitor.cache.s_24493430') }}</template>
                  </div>
                  <span @click="jumpAlarmList(row)" class="cphu blue">{{ $t('modules.views.appMonitor.relationMap.s_90ef7c48') }}</span>
                </div>
                <template v-else>{{ $t('modules.views.appMonitor.cache.s_ef984d39') }}</template>
              </template>
            </el-tooltip>

            <div slot="column-apps" slot-scope="{ row }">
              <el-popover placement="top-start" width="240" trigger="hover">
                <div class="tag-list ell popover">
                  <span v-for="(t, i) in row.apps" :key="i"
                    @click.stop="jumpHostMetric(t, row)"
                    class="tag-item ell">{{ t }}</span>
                </div>
                <div slot="reference" class="tag-list ell">
                  <span v-for="(t, i) in row.apps" :key="i"
                    @click.stop="jumpHostMetric(t, row)"
                    class="tag-item ell">{{ t }}</span>
                </div>
              </el-popover>
            </div>

          </db-table>

          <host-map
            v-if="displayMode === 'chart'"
            ref="hostMap"
            :params="getQueryParams"
          />
        </div>
      </div>
    </div>

    <!-- 添加标签弹窗 -->
    <tag-dialog
      ref="tagDialog"
      :saveTagApi="saveTagApi"
      :params="{ ids: selection.map(t => t.id) }"
      @on-saved="tableRefresh" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import SearchGroup from './search-group.vue';
import ChooseCollapse from './choose-collapse.vue';
import HostMap from './hostmap/index.vue';
import TagDialog from './tag-dialog.vue';
import { toAsyncWait } from '@/utils/common';
import InfraApi from '@/api/infrastructure';

@Component({
  components: {
    SearchGroup,
    ChooseCollapse,
    HostMap,
    TagDialog,
  },
})
export default class Host extends Vue {
  public $refs!: {
    searchGroup: SearchGroup
    chooseCollapse: ChooseCollapse
    hostMap: HostMap
    listTable: any
    tagDialog: TagDialog
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private isLoading = false;

  private displayMode: 'table' | 'chart' = 'table'

  // 分组
  private groupSelected: string[] = [] // 已选分组
  private groupList: string[] = [] // 所有分组

  private queryParams: any = {}
  private queryFilter: any = {}
  private timeParams: any = {}

  get getQueryParams () {
    const statusType = this.queryFilter.statusType
    const params: any = {
      ...this.queryParams,
      ...this.queryFilter,
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      hostName: (this.queryParams.hostName || '').trim(),
      hostIp: (this.queryFilter.ip || '').trim(),
      statusType: statusType && typeof statusType === 'string' ? +statusType : statusType,
      isFuzzy: 1,
    }
    if (this.groupSelected.length) {
      params.group = [...this.groupSelected]
    }
    return params;
  }

  get queryApi () {
    if (this.groupSelected.length) {
      return InfraApi.getHostGroupList
    } else {
      return InfraApi.getHostList
    }
  }
  private columnConfig = [
    { field: 'hostName', prop: 'df-hostname', label: i18n.t('modules.views.configManage.alarm.s_3d022a63') as string, labelKey: 'modules.views.configManage.alarm.s_3d022a63', slot: 'column-hostName', sortable: true, minWidth: 150, handleClick: this.jumpHostDetail, canClick: this.canDrillDown, },
    { field: 'health', label: i18n.t('modules.views.appMonitor.cache.s_fb844b8b') as string, labelKey: 'modules.views.appMonitor.cache.s_fb844b8b', slot: 'column-health', minWidth: 100, },
    { field: 'alarmCount', prop: 'alarmMetric.total', label: i18n.t('modules.views.appMonitor.external.s_9cb646c9') as string, labelKey: 'modules.views.appMonitor.external.s_9cb646c9', unit: 'count', sortable: true, minWidth: 100, },
    { field: 'hostManageIp', prop: 'network.ipaddress', label: i18n.t('modules.views.configManage.entity.s_2dc9105c') as string, labelKey: 'modules.views.configManage.entity.s_2dc9105c', sortable: true, minWidth: 200, },
    // { field: 'hostOs', prop: 'platform.os', label: i18n.t('modules.views.infrastructure.host.s_30d23ef4') as string, labelKey: 'modules.views.appMonitor.traceDetail.s_30d23ef4', sortable: true, minWidth: 100, },
    { field: 'cpuUsage', prop: 'lastReportCpu', label: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, labelKey: 'modules.views.appMonitor.relationMap.s_7054bc34', unit: 'percent', type: 'progress', progressMax: 1, sortable: true, defaultSort: 'desc', minWidth: 110, },
    { field: 'memoryUsage', prop: 'lastReportMemUsedPercent', label: i18n.t('modules.views.appMonitor.relationMap.s_31cb8d97') as string, labelKey: 'modules.views.appMonitor.relationMap.s_31cb8d97', unit: 'percent', type: 'progress', progressMax: 1, sortable: true, minWidth: 110, },
    { field: 'memoryUsed', prop: 'lastReportMem', label: i18n.t('modules.views.infrastructure.dockerDetail.s_6f60d25d') as string, labelKey: 'modules.views.infrastructure.dockerDetail.s_6f60d25d', unit: 'b', type: 'progress', sortable: true, minWidth: 100, },
    { field: 'oneAgent', label: i18n.t('modules.views.infrastructure.host.s_7714bc11') as string, labelKey: 'modules.views.infrastructure.host.s_7714bc11', minWidth: 200, defaultShow: false, },
    { field: 'apps', label: i18n.t('modules.views.alarmCenter.alarm.s_5b0520a9') as string, labelKey: 'modules.views.alarmCenter.alarm.s_5b0520a9', slot: 'column-apps', showOverflowTooltip: false, minWidth: 200, },
  ]

  private selection: any[] = []
  private batchLoading = false

  private saveTagApi = InfraApi.customHostTag

  private created () {
    const { mode } = this.$route.query;
    if (mode === 'table' || mode === 'chart') {
      this.displayMode = mode
    }
    let group: any = this.$route.query.group;
    group = (Array.isArray(group) ? group : [group]).filter(t => t);
    if (group.length) {
      this.groupSelected = group.map((t: string) => decodeURIComponent(t));
    }
  }

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

  // 切换列表和主机地图
  private toggleModeHandle (mode: any) {
    this.displayMode = mode
    this.getData()
  }

  private getData () {
    this.$nextTick(() => {
      if (this.displayMode === 'table') {
        this.tableRefresh();
      } else {
        this.selection = [];
        this.$refs.hostMap.getHostmapData();
      }
    });
  }

  private formatFunc (data: any) {
    const formatData = (arr: any[]) => arr.map((t: any) => {
      const hostIp = t.network ? t.network.ipaddress : ''
      const manageIp = (t.network || {}).managerIpaddress || ''
      return {
        ...t,
        __id: Math.random(),
        hostName: t['df-hostname'] || t.hostName,
        hostOs: String((t.platform || {}).GOOS || '').toLocaleLowerCase(),
        alarmCount: t.alarmMetric?.total || 0,
        alarmPendingCount: t.alarmMetric?.total || 0,
        cpuUsage: (+t.lastReportCpu || 0) / 100,
        memoryUsage: (+t.lastReportMemUsedPercent || 0) / 100,
        memoryUsed: (+t.lastReportMem) * 1024 * 1024,
        hostManageIp: (manageIp || hostIp || '-') + (manageIp && manageIp !== hostIp ? ` (${hostIp || '-'})` : ''),
        apps: (t.apps || []).filter((a: string) => a && a !== 'kubelet'), // 过滤kubelet
        oneAgent: (t.userAgent || '').split('+git')[0],
      }
    });

    if (!this.groupSelected.length) {
      return formatData(data)
    } else {
      return data.map((item: any) => ({
        ...item,
        __id: Math.random(),
        isGroup: true,
        children: formatData(item.data || []),
        hostName: `${(item.tagGroups || '-')} (${ (item.data || []).length })`,
        health: 'TABLE_CELL_EMPTY',
        alarmCount: 'TABLE_CELL_EMPTY',
        hostManageIp: 'TABLE_CELL_EMPTY',
        hostOs: 'TABLE_CELL_EMPTY',
        cpuUsage: 'TABLE_CELL_EMPTY',
        memoryUsage: 'TABLE_CELL_EMPTY',
        memoryUsed: 'TABLE_CELL_EMPTY',
        apps: 'TABLE_CELL_EMPTY',
        oneAgent: 'TABLE_CELL_EMPTY',
      }))
    }
  }
  private tableRefresh () {
    (this.$refs.listTable as any)?.refresh()
  }
  // 是否可下钻
  private canDrillDown (row: any) {
    return !row.isGroup
  }

  private durationChangeHandle () {
    this.timeParams = { ...this.getGlobalTimeV2() }
    this.$nextTick(() => {
      Promise.allSettled([
        this.$refs.searchGroup.init(),
        this.$refs.chooseCollapse.init(),
        this.getGroupList(),
      ]).then((rstList) => {
        const resultList = rstList.map((t: any) => t.value || [])
        this.queryParams = { ...resultList[0] };
        this.queryFilter = { ...resultList[1] };
        // 如果有选择的分组，不存在的剔除
        this.groupSelected = this.groupSelected.filter((g: string) => this.groupList.indexOf(g) > -1)
        this.isLoading = false
        this.getData()
      })
    })
  }

  // 搜索
  private searchChangeHandle (data: any) {
    this.queryParams = { ...data };
    this.getData()
  }

  // 分组 change
  private groupChangeHandle () {
    const _query: any = {
      ...this.$route.query,
      group: this.groupSelected.map(t => encodeURIComponent(t)),
    }
    if (!_query.group.length) {
      delete _query.group
    }
    this.$router.replace({ query: _query });
    this.getData()
  }

  // 快捷筛选
  private filterChangeHandle (data: any) {
    if (JSON.stringify(data) === JSON.stringify(this.queryFilter)) {
      return
    }
    this.queryFilter = { ...data }
    this.getData()
  }
  // 快捷筛选 展开/收起回调
  private filterToggleHandle () {
    this.$nextTick(() => {
      this.$refs.hostMap && this.$refs.hostMap.resize()
    })
  }

  // 选中 change
  private selectionChangeHandle (selection: any) {
    this.selection = selection
  }

  private rowClickHandle (row: any) {
    if (row.isGroup) {
      this.$refs.listTable.toggleRowExpansion(row)
    } else {
      this.jumpHostDetail(row)
    }
  }

  // 获取分组
  private async getGroupList () {
    const params = {
      startTime: this.timeParams.fromTime,
      endTime: this.timeParams.toTime,
      type: 'host',
    }
    this.isLoading = true
    const { result, error } = await toAsyncWait(InfraApi.getGroupList(params))
    this.isLoading = false
    if (!error) {
      const tagMap = result?.data?.tagMap || {};
      this.groupList = Object.keys(tagMap);
    } else {
      this.groupList = []
    }
  }

  private showAddLabelHandle () {
    this.$refs.tagDialog.showHandle();
  }

  // 跳转到Host详情
  private jumpHostDetail (row: any) {
    this.$router.push({
      path: '/infrastructure/hostDetail',
      query: {
        hostName: encodeURIComponent(row.hostName),
      }
    })
  }

  // 跳转到主机指标页
  private jumpHostMetric (app: string, row: any) {
    this.$router.push({
      path: '/infrastructure/hostDetail',
      query: {
        hostName: encodeURIComponent(row.hostName),
        type: 'metric',
        app: encodeURIComponent(app),
      }
    })
  }

  // 跳转到告警列表
  private jumpAlarmList (row: any) {
    this.$router.push({
      path: '/alarmCenter/alarm',
      query: {
        host: encodeURIComponent(row.hostName),
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.db-host-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .group-select {
    width: 180px;
  }

  .db-host {
    flex: 1;
    min-height: 300px;
    padding: 20px 0;
    border-radius: 4px;
    background-color: var(--bg-color);
    display: flex;
    flex-direction: column;
  }

  .db-host-body {
    flex: 1;
    overflow: hidden;
    display: flex;
    .collapse-choose {
      height: 100%;
      background-color: var(--bg-color);
    }
  }

  .db-list {
    flex: 1;
    overflow: hidden;
  }
}

.radio-group {
  display: block;
  flex: none;
}

.radio-btn {
  width: 32px;
  height: 32px;
  :deep(.el-radio-button__inner) {
    padding: 0;
    width: 100%;
    height: 100%;
    line-height: 30px;
    color: var(--color-text-secondary);
  }
  &:hover :deep(.el-radio-button__inner) {
    color: var(--color-primary);
  }
  :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
    color: var(--color-primary);
    background: var(--bg-color);
    border-color: var(--border-color-base);
    box-shadow: none;
  }
}

.tag-list {
  .tag-item {
    box-sizing: border-box;
    margin-right: 5px;
    max-width: 100%;
    display: inline-block;
    vertical-align: top;
    padding: 0 5px;
    line-height: 22px;
    background-color: var(--background-color-base);
    border-radius: 2px;
    cursor: pointer;
    &:hover {
      color: var(--color-primary);
    }
  }

  &.popover {
    margin: -4px -4px -8px -4px;
    white-space: normal;
    .tag-item {
      margin-bottom: 5px;
    }
  }
}
</style>
