<template>
  <div class="detail-alarm">
    <div class="section-item">
      <div class="section-title">{{ $t('modules.views.appMonitor.resourceDetail.s_358b05d0') }}</div>
      <div class="section-cont">
        <basic-chart
          :source="chartSource"
          :barMaxWidth="6"
          :minInterval="1"
          :colors="['#E12828']"
          :showEmpty="!chartLoading && !chartSource.length"
          v-loading="chartLoading"
        />
      </div>
    </div>

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
      class="detail-alarm-list" />
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
    { field: 'domainName', label: i18n.t('modules.views.alarmCenter.alarm.s_f9d4e244') as string, labelKey: 'modules.views.alarmCenter.alarm.s_f9d4e244', minWidth: 120 },
    { field: 'timestamp', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_858ac2d7') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_858ac2d7', unit: 'time', minWidth: 140 },
    { field: 'level', label: i18n.t('modules.views.alarmCenter.alarm.s_ed7094f4') as string, labelKey: 'modules.views.alarmCenter.alarm.s_ed7094f4', type: 'alarmLevel', minWidth: 80 },
    { field: 'description', label: i18n.t('modules.views.alarmCenter.alarm.s_606a249f') as string, labelKey: 'modules.views.alarmCenter.alarm.s_606a249f', minWidth: 300 },
    { field: 'type', label: i18n.t('modules.views.alarmCenter.alarm.s_c62e34c5') as string, labelKey: 'modules.views.alarmCenter.alarm.s_c62e34c5', unit: 'alarmType', minWidth: 100 },
    { field: 'eventCnt', label: i18n.t('modules.views.alarmCenter.alarm.s_31be934f') as string, labelKey: 'modules.views.alarmCenter.alarm.s_31be934f', unit: 'count', minWidth: 80 },
  ]

  get getQueryParams () {
    const params: any = {
      ...this.queryParams,
      needCount: true,
      sortField: 'timestamp',
      sortOrder: 'desc',
      trigger: {
        host: [this.queryParams.hostName],
      },
    }
    delete params.hostName
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

  private async getChartData () {
    const params: any = {
      ...this.queryParams,
      trigger: {
        host: [this.queryParams.hostName]
      }
    }
    delete params.hostName
    this.chartLoading = true
    const { result, error } = await toAsyncWait(AlarmApi.getAlarmTrend(params))
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

  private tableRefresh () {
    (this.$refs.listTable as any)?.refresh();
  }

  private formatFunc (data: any) {
    return data.map((t: any) => ({
      ...t,
      domainName: this.groupMapping[t.gid] || '',
    }));
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

.section-item {
  width: 100%;
  height: 238px;
  color: var(--color-text-primary);

  .section-title {
    font-size: 14px;
    line-height: 22px;
  }

  .section-cont {
    height: calc(100% - 38px);
  }
}

.detail-alarm-list {
  height: calc(100% - 238px);
}
</style>
