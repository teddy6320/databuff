<template>
  <div class="db-docker-wrapper">
    <div class="db-docker">
      <div class="flex-h">
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

      <div class="db-list">
        <db-table
          ref="listTable"
          :queryApi="queryApi"
          :queryParams="getQueryParams"
          :timeMode="false"
          :autoRefresh="false"
          :offsetMode="true"
          showSetting
          tableKey="INFRA_DOCKER_LIST"
          row-key="__id"
          :columnConfig="columnConfig"
          :formatFunc="formatFunc"
          @sort-change="tableRefresh"
          @row-click="rowClickHandle"
          :row-style="{ cursor: 'pointer' }">
          <div slot="total" slot-scope="{ total }" class="describe">{{ $t('modules.views.infrastructure.docker.s_b7dbd20a', { value0: total, value1: !groupSelected.length ? $t('modules.views.cockpit.tab.s_7cb69f1f') : $t('modules.views.appMonitor.traceDetail.s_829abe5a') }) }}</div>
        </db-table>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import SearchGroup from './search-group.vue';
import { toAsyncWait } from '@/utils/common';
import InfraApi from '@/api/infrastructure';

@Component({
  components: {
    SearchGroup,
  },
})
export default class Docker extends Vue {
  public $refs!: {
    searchGroup: SearchGroup
    listTable: any
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private isLoading = false;

  // 分组
  private groupSelected: string[] = [] // 已选分组
  private groupList: string[] = [] // 所有分组

  private queryParams: any = {}
  private timeParams: any = {}

  get getQueryParams () {
    const params: any = {
      ...this.queryParams,
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      isFuzzy: 1,
    }
    if (this.groupSelected.length) {
      params.group = [...this.groupSelected]
    }
    return params;
  }

  get queryApi () {
    if (this.groupSelected.length) {
      return InfraApi.getContainerGroupList
    } else {
      return InfraApi.getContainerList
    }
  }
  private columnConfig = [
    { field: 'containerName', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 150, handleClick: this.jumpDockerDetail, canClick: this.canDrillDown, },
    { field: 'image', label: i18n.t('modules.views.infrastructure.docker.s_34772285') as string, labelKey: 'modules.views.infrastructure.docker.s_34772285', minWidth: 120, defaultShow: false, },
    { field: 'id', label: 'ID', minWidth: 120, defaultShow: false, },
    { field: 'hostName', label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369', sortable: true, minWidth: 120, defaultShow: false, },
    { field: 'cpuUsage', label: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, labelKey: 'modules.views.appMonitor.relationMap.s_7054bc34', unit: 'percent', prop: 'totalPct', type: 'progress', progressMax: 1, sortable: true, minWidth: 110, },
    { field: 'memRss', label: i18n.t('modules.views.infrastructure.docker.s_d2340f93') as string, labelKey: 'modules.views.infrastructure.docker.s_d2340f93', unit: 'b', type: 'progress', sortable: true, defaultSort: 'desc', minWidth: 120, },
    { field: 'rbps', label: 'r/s', unit: 'b', suffix: '/s', type: 'progress', sortable: true, minWidth: 100, defaultShow: false, },
    { field: 'wbps', label: 'w/s', unit: 'b', suffix: '/s', type: 'progress', sortable: true, minWidth: 100, defaultShow: false, },
    { field: 'netSentBps', label: 'TX', unit: 'b', suffix: '/s', type: 'progress', sortable: true, minWidth: 100, },
    { field: 'netRcvdBps', label: 'RX', unit: 'b', suffix: '/s', type: 'progress', sortable: true, minWidth: 100, },
    { field: 'state', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', sortable: true, minWidth: 90, },
    { field: 'health', label: i18n.t('modules.views.appMonitor.cache.s_fb844b8b') as string, labelKey: 'modules.views.appMonitor.cache.s_fb844b8b', type: 'healthStatus', sortable: true, minWidth: 90, defaultShow: false, },
    { field: 'started', label: i18n.t('modules.views.infrastructure.docker.s_86cd8dce') as string, labelKey: 'modules.views.infrastructure.docker.s_86cd8dce', unit: 'time', sortable: true, minWidth: 140, },
    { field: 'created', label: i18n.t('modules.views.appMonitor.serviceDetail.s_eca37cb0') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_eca37cb0', unit: 'time', sortable: true, minWidth: 140, defaultShow: false, },
  ]

  private mounted () {
    let group: any = this.$route.query.group;
    group = (Array.isArray(group) ? group : [group]).filter(t => t);
    if (group.length) {
      this.groupSelected = group.map((t: string) => decodeURIComponent(t));
    }

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
    const formatData = (arr: any[]) => arr.map((t: any) => {
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

    if (!this.groupSelected.length) {
      return formatData(data)
    } else {
      return data.map((item: any) => ({
        ...item,
        __id: Math.random(),
        isGroup: true,
        children: formatData(item.data || []),
        containerName: `${(item.tagGroups || '-')} (${ (item.data || []).length })`,
        image: 'TABLE_CELL_EMPTY',
        id: 'TABLE_CELL_EMPTY',
        hostName: 'TABLE_CELL_EMPTY',
        cpuUsage: 'TABLE_CELL_EMPTY',
        memRss: 'TABLE_CELL_EMPTY',
        rbps: 'TABLE_CELL_EMPTY',
        wbps: 'TABLE_CELL_EMPTY',
        netSentBps: 'TABLE_CELL_EMPTY',
        netRcvdBps: 'TABLE_CELL_EMPTY',
        state: 'TABLE_CELL_EMPTY',
        health: 'TABLE_CELL_EMPTY',
        started: 'TABLE_CELL_EMPTY',
        created: 'TABLE_CELL_EMPTY',
        exited: 'TABLE_CELL_EMPTY',
      }))
    }
  }
  private tableRefresh () {
    (this.$refs.listTable as any)?.refresh()
  }

  private durationChangeHandle () {
    this.timeParams = { ...this.getGlobalTimeV2() }
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        this.queryParams = { ...data };
        this.getGroupList().then(() => {
          // 如果有选择的分组，不存在的剔除
          this.groupSelected = this.groupSelected.filter((g: string) => this.groupList.indexOf(g) > -1)
          this.isLoading = false
          this.tableRefresh()
        })
      })
    })
  }

  private searchChangeHandle (data: any) {
    this.queryParams = { ...data };
    this.tableRefresh();
  }

  private groupChangeHandle () {
    const _query: any = {
      ...this.$route.query,
      group: this.groupSelected.map(t => encodeURIComponent(t)),
    }
    if (!_query.group.length) {
      delete _query.group
    }
    this.$router.replace({ query: _query });
    this.tableRefresh();
  }

  // 获取分组
  private async getGroupList () {
    const params = {
      startTime: this.timeParams.fromTime,
      endTime: this.timeParams.toTime,
      type: 'container',
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

  private rowClickHandle (row: any) {
    if (row.isGroup) {
      this.$refs.listTable.toggleRowExpansion(row)
    } else {
      this.jumpDockerDetail(row)
    }
  }

  private canDrillDown (row: any) {
    return !row.isGroup
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

<style lang="scss" scoped>
.db-docker-wrapper {
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

  .db-docker {
    flex: 1;
    min-height: 300px;
    padding: 20px;
    border-radius: 4px;
    background-color: var(--bg-color);
    display: flex;
    flex-direction: column;
  }

  .db-list {
    flex: 1;
    overflow: hidden;
  }
}
</style>
