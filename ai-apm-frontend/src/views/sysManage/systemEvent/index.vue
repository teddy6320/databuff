<template>
  <div class="system-event-wrapper">
    <div class="list-header flex-h">
      <search-group
        ref="searchGroup"
        @on-change="searchChangeHandle"
        class="search-group" />

      <el-button @click="viewRuleHandle" type="primary" plain size="small" class="ml-10">{{ $t('modules.views.alarmCenter.alarm.s_b4c5a9d9') }}</el-button>
    </div>

    <db-table
      ref="listTable"
      :queryApi="queryApi"
      :queryParams="getQueryParams"
      :timeMode="false"
      :autoRefresh="false"
      :columnConfig="columnConfig"
      :row-style="{ cursor: 'pointer' }"
      @sort-change="tableRefresh"
      @row-click="showDetailHandle">
      <template slot="column-message" slot-scope="{ row }">
        <span
          :class="{
            'bg-red': row.level === 3,
            'bg-yellow': row.level === 2,
            'bg-grey': row.level !== 3 && row.level !== 2,
          }"
          class="alarm-status">{{ row.level | AlarmStatusFilter }}</span>{{
            (row.message || '').length > 1500 ? row.message.substring(0, 1500) + '...' : row.message || '-'
          }}
      </template>
    </db-table>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import SearchGroup from './search-group.vue';
import AlarmApi from '@/api/alarm';

@Component({
  components: {
    SearchGroup,
  }
})
export default class SystemEvent extends Vue {
  public $refs!: {
    searchGroup: SearchGroup
    listTable: any
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private queryParams: any = {}
  private timeParams: any = {}

  get getQueryParams () {
    const params: any = {
      ...this.queryParams,
      start: +new Date(this.timeParams.fromTime),
      end: +new Date(this.timeParams.toTime),
    }
    if (typeof this.queryParams.level === 'number') {
      params.levels = [this.queryParams.level]
      delete params.level
    }
    return params;
  }

  private queryApi = AlarmApi.getSystemEventList
  private columnConfig = [
    { field: 'id', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_97f589d9') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_97f589d9', minWidth: 170, handleClick: this.showDetailHandle },
    { field: 'message', label: i18n.t('modules.views.sysManage.systemEvent.s_d76255b2') as string, labelKey: 'modules.views.sysManage.systemEvent.s_d76255b2', slot: 'column-message', minWidth: 500 },
    { field: 'triggerTime', label: i18n.t('modules.views.appMonitor.errorDetail.s_13f7745f') as string, labelKey: 'modules.views.appMonitor.errorDetail.s_13f7745f', unit: 'time', sortable: true, defaultSort: 'desc', minWidth: 150 },
  ]

  private async mounted () {
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

  // 检测规则
  private viewRuleHandle () {
    this.$router.push('/sysManage/systemRule');
  }

  // 查看详情
  public showDetailHandle(row: any) {
    this.$router.push({
      path: '/sysManage/eventDetail',
      query: { eid: row.id }
    });
  }
}
</script>

<style lang="scss" scoped>
.system-event-wrapper {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .list-header {
    justify-content: space-between;
    .search-group {
      flex: 1;
      flex-wrap: nowrap;
    }
  }
  
  .list-cont {
    flex: 1;
    min-height: 400px;
    overflow: hidden;
  }

  .alarm-status {
    display: inline-block;
    margin-right: 6px;
    padding: 0 4px;
    font-size: 12px;
    line-height: 18px;
    border-radius: 2px;
    color: #fff;
  }
}
</style>
