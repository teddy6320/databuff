<template>
  <div class="detail-event">
    <div class="detail-event-half p20">
      <basic-chart
        :source="chartSource"
        :barMaxWidth="12"
        :minInterval="1"
        :colors="['#E12828']"
        :showEmpty="!chartLoading && !chartSource.length"
        v-loading="chartLoading"
        class="detail-event-chart"
      />

      <db-table
        ref="listTable"
        :queryApi="queryApi"
        :queryParams="getQueryParams"
        :timeMode="false"
        :autoRefresh="false"
        :columnConfig="columnConfig"
        :formatFunc="formatFunc"
        @sort-change="tableRefresh"
        @current-change="currentChangeHandle"
        @on-fetch-end="tableFetchEndHandle"
        class="detail-event-list">
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
        <template slot="column-startTriggerTime" slot-scope="{ row }">
          {{ row.startTriggerTime | TimesToDateFilter }}
          <span class="db-icon-right font-12 current-arrow"></span>
        </template>
      </db-table>
    </div>

    <event-detail
      :eventDetail="currEvent"
      class="detail-event-half half-right" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { namespace } from 'vuex-class';
import dayjs from 'dayjs';
import { calcInterval } from '@/utils/timeFormat';
import { toAsyncWait } from '@/utils/common';
import AlarmApi from '@/api/alarm';
import EventDetail from '../eventDetail/index.vue';

const UserModel = namespace('User');

@Component({
  components: {
    EventDetail,
  }
})
export default class DetailEvent extends Vue {
  @UserModel.Getter('getGroupMapping') private groupMapping!: any;

  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: () => ({}) }) private detail!: any;

  private chartLoading = false
  private chartSource: any[] = []

  private queryApi = AlarmApi.getAlarmListV2
  private columnConfig = [
    { field: 'id', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_97f589d9') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_97f589d9', width: 165 },
    { field: 'message', label: i18n.t('modules.utils.static.s_10b2761d') as string, labelKey: 'modules.utils.static.s_10b2761d', slot: 'column-message', minWidth: 200 },
    { field: 'domainName', label: i18n.t('modules.views.alarmCenter.alarm.s_f9d4e244') as string, labelKey: 'modules.views.alarmCenter.alarm.s_f9d4e244', width: 120 },
    { field: 'startTriggerTime', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_858ac2d7') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_858ac2d7', slot: 'column-startTriggerTime', width: 160 },
  ]

  get getQueryParams () {
    return {
      alarmId: this.queryParams.alarmId,
    };
  }

  private currEvent: any = null

  private created () {
    if (!this.$store.getters['User/getGroupEnabled']) {
      this.columnConfig = this.columnConfig.filter(t => t.field !== 'domainName')
    }
  }

  public getData () {
    this.getChartData();
    this.tableRefresh();
  }

  public resize () {
    (this.$refs.listTable as any)?.getHeightHandle();
  }

  private async getChartData () {
    const from = this.detail.startTriggerTime
    const to = this.detail.timestamp
    const params = {
      alarmId: this.queryParams.alarmId,
      interval: calcInterval(from, to),
    }
    this.chartLoading = true
    const { result, error } = await toAsyncWait(AlarmApi.getEventTrendV2(params));
    this.chartLoading = false
    if (!error) {
      const data = result?.data || [];
      this.chartSource = [{
        name: i18n.t('modules.views.alarmCenter.alarmDetail.s_4dd50b23') as string, nameKey: 'modules.views.alarmCenter.alarmDetail.s_4dd50b23',
        type: 'bar',
        data: data.filter((t: any) => t.time).map((t: any) => ({
          key: dayjs(+t.time).format('YYYY-MM-DD HH:mm'),
          value: t.eventCnt,
        })),
      }]
    }
  }

  private formatFunc (data: any) {
    return data.map((t: any) => ({
      ...t,
      startTriggerTime: t.startTriggerTime || t.triggerTime,
      domainName: this.groupMapping[t.gid] || '',
    }));
  }
  private tableRefresh () {
    (this.$refs.listTable as any)?.refresh();
  }
  private tableFetchEndHandle (data: any[]) {
    if (!this.currEvent && data.length) {
      (this.$refs.listTable as any)?.setCurrentRow(data[0]);
    }
  }
  private currentChangeHandle (row: any) {
    this.currEvent = row
  }
}
</script>

<style lang="scss" scoped>
.detail-event {
  height: 100%;
  padding: 0 !important;
  display: flex;

  .detail-event-half {
    width: 50%;
    height: 100%;
  }

  .half-right {
    box-shadow: 0 0 6px 0 var(--shadow-color02);
  }
}

.detail-event-chart {
  height: 150px;
}

.detail-event-list {
  height: calc(100% - 150px);
  .alarm-status {
    display: inline-block;
    margin-right: 6px;
    padding: 0 4px;
    font-size: 12px;
    line-height: 18px;
    border-radius: 2px;
    color: #fff;
  }
  :deep(.el-table__body tr) {
    cursor: pointer;
    &.current-row {
      cursor: auto;
    }
    .current-arrow {
      display: none;
      margin-top: -6px;
      position: absolute;
      top: 50%;
      right: 11px;
    }
    &.current-row  > td.el-table__cell {
      background: none;
    }
    &.current-row .cell {
      color: var(--color-primary);
      position: relative;
      .current-arrow {
        display: block;
      }
    }
  }
}
</style>
