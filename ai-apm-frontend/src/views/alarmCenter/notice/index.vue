<template>
  <div class="notice-wrapper">
    <div class="notice-content">
      <search-group
        ref="searchGroup"
        :timeParams="getQueryParams"
        @on-change="searchChangeHandle" />

      <div class="notice-list">
        <db-table
          ref="listTable"
          :queryApi="queryApi"
          :queryParams="getQueryParams"
          :timeMode="false"
          :autoRefresh="false"
          showSetting
          tableKey="ALARM_NOTICE_LIST"
          :columnConfig="columnConfig"
          :formatFunc="formatFunc"
          @sort-change="tableRefresh">
          <template slot="column-result" slot-scope="{ row }">
            <i v-if="row.result !== 'success'" class="db-icon-close-pie font-12 mr-5 db-red icon-vm"></i>
            <i v-else class="db-icon-right-pie font-12 mr-5 db-green icon-vm"></i>
            <span>{{ row.result | NoticeResultFilter }}</span>
          </template>
          <template slot="column-method" slot-scope="{ row }">
            <i class="db-icon font-14 mr-5 icon-vm">{{ row.method | DbIconFilter }}</i>
            <span v-if="row.method !== 'wechat' || row.isSingle === 1">{{ row.method | NoticeMethodFilter }}</span>
            <span v-else>{{ $t('modules.views.alarmCenter.alarmDetail.s_cfbf6f4c') }}</span>
            <span v-if="['dingtalk', 'wechat'].includes(row.method)">{{ row.isSingle === 1 ? $t('modules.views.alarmCenter.alarmDetail.s_6a0e0419') : $t('modules.views.alarmCenter.alarmDetail.s_1f097591')  }}</span>
          </template>

          <el-table-column slot="suffix" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" :min-width="80">
            <template slot-scope="{ row }">
              <a v-if="row.result !== 'success'"
                @click.stop="row.loading ? null : resendHandle(row)"
                :class="{ 'action-disabled': row.loading }"
                class="blue">{{ $t('modules.views.alarmCenter.notice.s_89b21328') }}</a>
              <span v-else class="describe">--</span>
            </template>
          </el-table-column>
        </db-table>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import SearchGroup from './search-group.vue';
import { toAsyncWait } from '@/utils/common';
import AlarmApi from '@/api/alarm';

@Component({
  components: {
    SearchGroup,
  },
})
export default class NoticeList extends Vue {
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
      from: this.timeParams.fromTime,
      to: this.timeParams.toTime,
    }
    // 最新时间，结束时间改为当前时间
    if (this.timeParams.type === 'select') {
      params.to = dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss')
    }
    return params;
  }

  private queryApi = AlarmApi.getNoticeRecordList
  private columnConfig = [
    { field: 'result', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_895bd22e') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_895bd22e', slot: 'column-result', minWidth: 80, disabled: true },
    { field: 'noticeTime', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_3f6194f8') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_3f6194f8', minWidth: 140 },
    { field: 'receiver', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_88340ead') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_88340ead', minWidth: 150 },
    { field: 'method', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_9fd00f0a') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_9fd00f0a', slot: 'column-method', minWidth: 120 },
    { field: 'errMsg', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_13d5f243') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_13d5f243', minWidth: 250 },
    { field: 'alertType', label: i18n.t('modules.views.alarmCenter.alarm.s_c62e34c5') as string, labelKey: 'modules.views.alarmCenter.alarm.s_c62e34c5', unit: 'alarmType', minWidth: 100 },
    { field: 'alertDesc', label: i18n.t('modules.views.alarmCenter.alarm.s_606a249f') as string, labelKey: 'modules.views.alarmCenter.alarm.s_606a249f', minWidth: 300 },
    { field: 'alertStartTime', label: i18n.t('modules.views.alarmCenter.notice.s_f282d0e2') as string, labelKey: 'modules.views.alarmCenter.notice.s_f282d0e2', minWidth: 140 },
  ]

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
      receiver: [t.rcvNames, t.rcvUgNames].filter(r => !!r).join(','),
    }));
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

  // 重新发送
  private async resendHandle (row: any) {
    this.$set(row, 'loading', true);
    const { result, error } = await toAsyncWait(AlarmApi.resendNotice({ id: row.id }));
    this.$set(row, 'loading', false);
    if (!error) {
      this.$message.success(i18n.t('modules.views.alarmCenter.notice.s_9db9a7e3') as string);
      row.result = 'success'
      row.errMsg = ''
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message);
      row.errMsg = error.message
    }
  }
}
</script>

<style lang="scss" scoped>
.notice-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .notice-content {
    flex: 1;
    min-height: 300px;
    padding: 20px;
    border-radius: 4px;
    background-color: var(--bg-color);
    display: flex;
    flex-direction: column;
  }

  .notice-list {
    flex: 1;
    overflow: hidden;
  }

  .action-disabled {
    color: var(--color-text-secondary);
    pointer-events: none;
  }
}
</style>
