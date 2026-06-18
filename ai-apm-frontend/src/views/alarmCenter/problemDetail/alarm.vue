<template>
  <div class="detail-alarm">
    <basic-chart
      :source="chartSource"
      :barMaxWidth="6"
      :minInterval="1"
      :showEmpty="!chartLoading && !chartSource.length"
      v-loading="chartLoading"
      :colors="['#E12828']"
      class="detail-alarm-chart"
    />

    <db-table
      ref="listTable"
      :queryApi="queryApi"
      :queryParams="getQueryParams"
      :timeMode="false"
      :autoRefresh="false"
      :offsetMode="true"
      :showTotal="true"
      :columnConfig="columnConfig"
      :formatFunc="formatFunc"
      @sort-change="tableRefresh"
      @row-click="viewAlarmDetail"
      :row-style="{ cursor: 'pointer' }"
      class="detail-alarm-list">
      <template slot="column-description" slot-scope="{ row }">
        <span
          :class="{
            'bg-red': row.level === 3,
            'bg-yellow': row.level === 2,
            'bg-grey': row.level !== 3 && row.level !== 2,
          }"
          class="alarm-status">{{ row.level | AlarmStatusFilter }}</span>{{ row.descriptionKey ? $t(row.descriptionKey) : row.description }}
      </template>
    </db-table>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { namespace } from 'vuex-class';
import dayjs from 'dayjs';
import { toAsyncWait } from '@/utils/common';
import AlarmApi from '@/api/alarm';

const UserModel = namespace('User');

@Component
export default class DetailAlarm extends Vue {
  @UserModel.Getter('getGroupMapping') private groupMapping!: any;

  @Prop({ default: () => ({}) }) private queryParams!: any;

  private chartLoading = false
  private chartSource: any[] = []

  private queryApi = AlarmApi.getAlarmListNew
  private columnConfig = [
    { field: 'id', label: i18n.t('modules.views.alarmCenter.alarm.s_10b22107') as string, labelKey: 'modules.views.alarmCenter.alarm.s_10b22107', minWidth: 150, handleClick: this.viewAlarmDetail },
    { field: 'description', label: i18n.t('modules.views.alarmCenter.alarm.s_aa0eab9d') as string, labelKey: 'modules.views.alarmCenter.alarm.s_aa0eab9d', slot: 'column-description', minWidth: 300 },
    { field: 'domainName', label: i18n.t('modules.views.alarmCenter.alarm.s_f9d4e244') as string, labelKey: 'modules.views.alarmCenter.alarm.s_f9d4e244', minWidth: 120 },
    { field: 'timestamp', label: i18n.t('modules.views.alarmCenter.alarm.s_d0539543') as string, labelKey: 'modules.views.alarmCenter.alarm.s_d0539543', unit: 'minuteTime', minWidth: 140 },
    { field: 'duration', label: i18n.t('modules.views.alarmCenter.alarm.s_4a6341a8') as string, labelKey: 'modules.views.alarmCenter.alarm.s_4a6341a8', unit: 'sDuration', zeroIgnore: true, minWidth: 100 },
    { field: 'eventCnt', label: i18n.t('modules.views.alarmCenter.alarm.s_31be934f') as string, labelKey: 'modules.views.alarmCenter.alarm.s_31be934f', unit: 'count', minWidth: 80 },
  ]

  get getQueryParams () {
    const params: any = {
      ...this.queryParams,
      needCount: true,
      sortField: 'timestamp',
      sortOrder: 'desc',
    }
    return params;
  }

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
    this.chartLoading = true
    const { result, error } = await toAsyncWait(AlarmApi.getAlarmTrend(this.queryParams))
    this.chartLoading = false
    if (!error) {
      const { data = {} } = result || {};
      const rstData = data.data || {};
      const timeKeyStr = Object.keys(rstData);
      const _source: any[] = timeKeyStr.map(i => +i).sort().map(i => {
        const item = rstData[`${i}`] || {}
        return {
          key: dayjs(i).format('YYYY-MM-DD HH:mm'),
          value: item.count || 0,
        }
      })
      this.chartSource = [{
        name: i18n.t('modules.views.alarmCenter.problemDetail.s_1d4cbadb') as string, nameKey: 'modules.views.alarmCenter.problemDetail.s_1d4cbadb',
        type: 'bar',
        data: _source,
      }]
    }
  }

  private formatFunc (data: any) {
    return data.map((t: any) => {
      return {
        ...t,
        eventCnt: t.eventCnt || 0,
        domainName: this.groupMapping[t.gid] || '',
      }
    });
  }
  private tableRefresh () {
    (this.$refs.listTable as any)?.refresh();
  }

  private viewAlarmDetail (row: any) {
    this.$router.push({
      path: '/alarmCenter/alarmDetail',
      query: {
        aid: row.id,
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.detail-alarm {
  height: 100%;
}

.detail-alarm-chart {
  height: 150px;
}

.detail-alarm-list {
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
}
</style>
