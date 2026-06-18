<template>
  <div class="alarm-cont">
    <div class="alarm-wrapper flex-v">
      <h3 class="m-0 font-14 fw-normal">{{ $t('modules.views.appMonitor.resourceDetail.s_358b05d0') }}</h3>
      <div class="chart-wrapper mt-10 mb-10">
        <basic-chart
          :showEmpty="!chartGroup.alarm.loading && !chartGroup.alarm.source.length"
          :key='key'
          :colors='chartGroup.alarm.colors'
          :showLegend='true'
          :textSmallMode="true"
          :minInterval="1"
          :min="0"
          :yAxisSplitNum="3"
          :interval="timeParams.interval"
          :source='chartGroup.alarm.source'></basic-chart>
      </div>

      <div class="list-wrapper">
        <db-table
          :queryApi='queryApi'
          :queryParams='tableQueryParams'
          :offsetMode='true'
          :columnConfig='columnConfig'
          @on-table-inited='tableInitedHandle'
          @sort-change='tableInitedHandle'
          :formatFunc='formatFunc'
          @on-fetch-end='onFetchEnd'
          tableKey='APM_SERVICE_DETAIL_ALARM'
          ref='listTable'>
        </db-table>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { namespace } from 'vuex-class';
import AlarmApi from '@/api/alarm';
import dayjs from 'dayjs';
import { toAsyncWait } from '@/utils/common';

const UserModel = namespace('User');

@Component
export default class TabAlarm extends Vue {
  @UserModel.Getter('getGroupMapping') private groupMapping!: any;

  @Prop({ default: {} }) private current!: any;

  @Watch('current', { immediate: true })
  private onCurrentChange (val: any, oldVal: any) {
    if (val && val?.serviceId !== oldVal?.serviceId && this.isMounted) {
      this.fetchAllData();
    }
  }

  // 监听上下游服务点击事件，重新获取服务详情
  @Watch('$route.query.sid')
  private async onServiceRouteQueryChange (newSid: string, oldSid: string) {
    if (!oldSid) {
      return
    }
  }

  public $refs!: {
    listTable: any;
  };

  private isMounted = false;
  private showCharts = false;
  private listLoading = true;

  private timeParams = {
    fromTime: '',
    toTime: '',
    interval: 60,
  }

  private chartGroup: any = {
    alarm: {
      loading: true,
      source: [],
      colors: ['#2962FF'],
    },
  }

  get tableQueryParams () {
    return {
      ...this.timeParams,
      searchType: 'service',
      needCount: true,
      sortField: 'timestamp',
      sortOrder: 'desc',
      trigger: {
        serviceId: [this.current.serviceId || decodeURIComponent(String(this.$route.query.sid))],
      },
    }
  }

  private columnConfig: any[] = [
    { field: 'id', prop: 'id', label: i18n.t('modules.views.alarmCenter.alarm.s_10b22107') as string, labelKey: 'modules.views.alarmCenter.alarm.s_10b22107', minWidth: 120, handleClick: this.viewAlarmDetail },
    { field: 'domainName', label: i18n.t('modules.views.alarmCenter.alarm.s_f9d4e244') as string, labelKey: 'modules.views.alarmCenter.alarm.s_f9d4e244', minWidth: 120 },
    { field: 'timestamp', prop: 'timestamp', label: i18n.t('modules.views.appMonitor.errorDetail.s_13f7745f') as string, labelKey: 'modules.views.appMonitor.errorDetail.s_13f7745f', unit: 'time', minWidth: 120 },
    { field: 'level', prop: 'level', label: i18n.t('modules.views.alarmCenter.alarm.s_ed7094f4') as string, labelKey: 'modules.views.alarmCenter.alarm.s_ed7094f4', minWidth: 120, type: 'alarmLevel' },
    { field: 'description', prop: 'description', label: i18n.t('modules.views.alarmCenter.alarm.s_606a249f') as string, labelKey: 'modules.views.alarmCenter.alarm.s_606a249f', minWidth: 150 },
    { field: 'eventCnt', prop: 'eventCnt', label: i18n.t('modules.views.alarmCenter.alarm.s_31be934f') as string, labelKey: 'modules.views.alarmCenter.alarm.s_31be934f', minWidth: 100 },
  ];

  private queryApi = AlarmApi.getAlarmListNew;

  private tableMounted = false;

  get isLoading () {
    return this.chartGroup.alarm.loading || this.listLoading
  }

  @Watch('isLoading')
  private onIsLoading (newVal: boolean) {
    if (!newVal) {
      this.$emit('on-loaded')
    }
  }

  private created () {
    this.$emit('on-created');
    this.resetTimeParams();
    if (!this.$store.getters['User/getGroupEnabled']) {
      this.columnConfig = this.columnConfig.filter(t => t.field !== 'domainName')
    }
  }
  private mounted () {
    if (this.current?.serviceId ) {
      this.refresh();
    }
    this.isMounted = true;
  }

  public refresh () {
    this.fetchAllData();
  }

  private fetchAllData () {
    this.resetTimeParams();
    this.fetchAlarmSource();
    if (this.tableMounted) {
      this.$refs.listTable.refresh();
    }
  }

  private resetTimeParams () {
    const { fromTime, toTime, interval } = this.getGlobalTimeV2();
    this.timeParams = { fromTime, toTime, interval };
  }

  private formatFunc (data: any) {
    return data.map((t: any) => ({
      ...t,
      domainName: this.groupMapping[t.gid] || '',
    }));
  }

  // 告警趋势
  private async fetchAlarmSource () {
    const serviceId = this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid));
    const _params: any = {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      trigger: {
        serviceId: [serviceId]
      }
    }
    this.chartGroup.alarm.loading = true;
    const { result, error } = await toAsyncWait(AlarmApi.getAlarmTrend({ ..._params }))
    if (!error) {
      const { data = {} } = result || {};
      const rstData = data?.data || {};
      const timeKeyStr = Object.keys(rstData);
      const _source: any[] = timeKeyStr.map(i => +i).sort().map(i => {
        const item = rstData[`${i}`] || {}
        return {
          key: dayjs(i).format('YYYY-MM-DD HH:mm'),
          value: item.count || 0,
        }
      })
      this.chartGroup.alarm.source = [{
        name: i18n.t('modules.views.alarmCenter.problemDetail.s_1d4cbadb') as string, nameKey: 'modules.views.alarmCenter.problemDetail.s_1d4cbadb',
        type: 'line',
        area: true,
        data: _source,
      }]
    }
    this.chartGroup.alarm.loading = false;
  }

  // tableInitedHandle
  private tableInitedHandle () {
    this.resetTimeParams();
    this.tableMounted = true;
    this.$refs.listTable.refresh();
  }

  private onFetchEnd () {
    this.listLoading = false;
  }

  // 查看告警详情
  private viewAlarmDetail (row: any) {
    this.$router.push({
      path: '/alarmCenter/alarmDetail',
      query: { aid: row.id }
    })
  }

}
</script>
<style lang="scss" scoped>
.alarm-cont {
  padding-left: 4px;
  height: 100%;
}
.alarm-wrapper {
  height: 100%;
  display: flex;
}
.chart-wrapper {
  flex: 0 0 auto;
  height: 180px;
}
.list-wrapper {
  flex: 1 1 auto;
  min-height: 286px;
}
</style>