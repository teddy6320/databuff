<template>
  <div class="container-wrapper">
    <div v-loading="filterLoading" class="container-content pt-20 pb-20">
      <div class="pl-20 pr-20 flex-h">
        <search-group
          ref="searchGroup"
          @on-change="searchChangeHandle" />

        <el-tooltip :disabled="hasRuleMenu" effect="light" :content="$t('modules.views.alarmCenter.alarm.s_e5027a0d')" placement="bottom" class="ml-10">
          <el-button
            @click="viewConfigHandle"
            :disabled="!hasRuleMenu"
            size="small"
            class="small-btn db-icon-setting font-16 flex-none"></el-button>
        </el-tooltip>

        <el-button
          @click="toggleChartHandle"
          :disabled="!hasRuleMenu"
          size="small"
          :class="{ active: showChart }"
          class="small-btn db-icon-chart1 font-16 flex-none"></el-button>
      </div>

      <div class="container-content-body pl-20 pr-20 pt-20">
        <choose-collapse
          ref="chooseCollapse"
          :queryParams="getQueryParams"
          @on-filter-change="filterChangeHandle"
          @on-filter-toggle="filterToggleHandle"
          class="collapse-choose" />

        <div class="container-list flex-v">
          <div v-if="showChart" v-loading="chartLoading" class="section-item flex-none mb-10">
            <div class="section-title">{{ $t('modules.views.alarmCenter.alarm.s_aa0eab9d') }}
              <span v-if="lastAlarmData" class="font-12 lh-18">{{ lastAlarmData.total }}</span>
            </div>
            <div class="section-cont">
              <basic-chart
                ref="alarmChart"
                :source="chartSource"
                :minInterval="1"
                :min="0"
                :barMaxWidth="200"
                :showLegend="false"
                :legend="{ top: 10, right: 6, left: null, }"
                :grid="{ top: 48, left: 10, right: 20, bottom: 10, containLabel: true, }"
                :colors="['#E12828']"
                :axisClickEvent="($event) => chartClickHandle($event)"
                :tooltipFormat="chartTooltipFormat"
                :showEmpty="!chartLoading && !chartSource.some(item => item.data?.length)" />
            </div>
          </div>

          <db-table
            ref="listTable"
            :queryApi="queryApi"
            :queryParams="getTableParams"
            :timeMode="false"
            :autoRefresh="false"
            :offsetMode="true"
            :showTotal="true"
            showSetting
            tableKey="ALARM_LIST"
            :columnConfig="columnConfig"
            :formatFunc="formatFunc"
            @sort-change="tableRefresh"
            @row-click="viewAlarmDetail"
            :row-style="{ cursor: 'pointer' }"
            class="container-table">
            <span slot='total' slot-scope="{ total }" class="describe">
              <template v-if="chartTimeParams">
                <i class="db-icon-date mr-4 font-16 icon-vm"></i>
                <span class="mr-4">{{ chartTimeParams.fromTime.slice(0, 16) }} - {{ chartTimeParams.toTime.slice(0, 16) }}</span>
              </template>
              <span>{{ $t('modules.views.alarmCenter.alarm.s_9ad9830c', { value0: total }) }}</span>
            </span>

            <template slot="column-description" slot-scope="{ row }">
              <div class="alarm-description-cell flex-h">
                <span
                  :class="{
                    'bg-red': row.level === 3,
                    'bg-yellow': row.level === 2,
                    'bg-grey': row.level !== 3 && row.level !== 2,
                  }"
                  class="alarm-status flex-none">{{ row.level | AlarmStatusFilter }}</span>
                <el-tooltip :disabled="!row.description" effect="light" :content="row.description" placement="top">
                  <span class="alarm-description ell">{{ row.descriptionKey ? $t(row.descriptionKey) : row.description }}</span>
                </el-tooltip>
              </div>
            </template>

            <template slot="column-trigger" slot-scope="{ row, column }">
              <el-popover :disabled="!row[column.field].length" placement="top" trigger="hover" class="trigger-popover">
                <div style="max-height: 200px;max-width: 200px;overflow: auto" class="wba">
                  <template v-for="(t, i) in row[column.field]">
                    <a :key="i"
                      @click.stop="t.isLink ? viewTriggerHandle(t, column.field) : null"
                      :class="{ blue: t.isLink, ca: !t.isLink }"
                      class="font-12">{{ t.nameKey ? $t(t.nameKey) : t.name }}</a>
                    <template v-if="i < row[column.field].length - 1">, </template>
                  </template>
                </div>
                <span slot="reference">
                  <template v-if="row[column.field].length">
                    <a v-for="(t, i) in row[column.field].slice(0, 1)" :key="i"
                      @click.stop="t.isLink ? viewTriggerHandle(t, column.field) : null"
                      :class="{ blue: t.isLink, ca: !t.isLink }"
                      class="font-12">{{ t.nameKey ? $t(t.nameKey) : t.name }}</a>
                    <template v-if="row[column.field].length > 1">, ...</template>
                  </template>
                  <template v-else>-</template>
                </span>
              </el-popover>
            </template>
          </db-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Getter, namespace } from 'vuex-class';
import dayjs from 'dayjs';
import { StringIsEmpty } from '@/utils/common';
import SearchGroup from './search-group.vue';
import ChooseCollapse from './choose-collapse.vue';
import BasicChart from '@/components/charts/basic-chart.vue';
import { toAsyncWait } from '@/utils/common';
import AlarmApi from '@/api/alarm';
import { buildAlarmListLocation } from '@/views/configManage/alarm/alarm-routes';

const UserModel = namespace('User');

@Component({
  components: {
    SearchGroup,
    ChooseCollapse,
    BasicChart,
  }
})
export default class Alarm extends Vue {
  @UserModel.Getter('getGroupMapping') private groupMapping!: any;
  @UserModel.State private menus!: any[];

  public $refs!: {
    searchGroup: SearchGroup
    chooseCollapse: ChooseCollapse
    listTable: any
    alarmChart: BasicChart
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  get serviceIdNameMapping () {
    const mapping: any = {}
    const basicServiceMap = this.$store.getters['Service/basicServiceMap']
    Object.keys(basicServiceMap).forEach((t: string) => {
      mapping[t] = basicServiceMap[t].name
    });
    return mapping
  }

  private queryParams: any = {}
  private queryFilter: any = {}
  private filterLoading = true
  private timeParams: any = {}

  get getQueryParams () {
    const params: any = {
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      needCount: true,
      trigger: {},
    }
    const fields = ['description', 'idLike', 'level']
    const query = {
      ...this.queryParams,
      ...this.queryFilter,
    }
    Object.entries(query).forEach(([key, value]) => {
      if ((!Array.isArray(value) && value !== '') || (Array.isArray(value) && value.length)) {
        if (fields.includes(key)) {
          params[key] = value
        } else {
          params.trigger[key] = value
        }
      }
    })
    if (!Object.keys(params.trigger).length) {
      delete params.trigger
    }
    return params;
  }

  get getTableParams () {
    return {
      ...this.getQueryParams,
      ...this.chartTimeParams,
    }
  }

  private queryApi = AlarmApi.getAlarmListNew
  private columnConfig = [
    { field: 'id', label: i18n.t('modules.views.alarmCenter.alarm.s_10b22107') as string, labelKey: 'modules.views.alarmCenter.alarm.s_10b22107', sortable: true, minWidth: 150, fixed: 'left', handleClick: this.viewAlarmDetail, disabled: true },
    { field: 'description', label: i18n.t('modules.views.alarmCenter.alarm.s_aa0eab9d') as string, labelKey: 'modules.views.alarmCenter.alarm.s_aa0eab9d', slot: 'column-description', minWidth: 300, disabled: true, showOverflowTooltip: false },
    { field: 'domainName', label: i18n.t('modules.views.alarmCenter.alarm.s_f9d4e244') as string, labelKey: 'modules.views.alarmCenter.alarm.s_f9d4e244', minWidth: 120 },
    { field: 'serviceName', label: i18n.t('modules.views.alarmCenter.alarm.s_8f3747c0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_8f3747c0', minWidth: 120 },
    { field: 'startTriggerTime', label: i18n.t('modules.views.alarmCenter.alarm.s_f8b90878') as string, labelKey: 'modules.views.alarmCenter.alarm.s_f8b90878', unit: 'minuteTime', sortable: true, minWidth: 140, defaultShow: false },
    { field: 'timestamp', label: i18n.t('modules.views.alarmCenter.alarm.s_d0539543') as string, labelKey: 'modules.views.alarmCenter.alarm.s_d0539543', unit: 'minuteTime', sortable: true, defaultSort: 'desc', minWidth: 140 },
    { field: 'endTriggerTime', label: i18n.t('modules.views.alarmCenter.alarm.s_f782779e') as string, labelKey: 'modules.views.alarmCenter.alarm.s_f782779e', unit: 'minuteTime', sortable: true, minWidth: 140, defaultShow: false },
    { field: 'eventCnt', label: i18n.t('modules.views.alarmCenter.alarm.s_31be934f') as string, labelKey: 'modules.views.alarmCenter.alarm.s_31be934f', unit: 'count', sortable: true, minWidth: 100 },
    { field: 'appList', label: i18n.t('modules.views.alarmCenter.alarm.s_5b0520a9') as string, labelKey: 'modules.views.alarmCenter.alarm.s_5b0520a9', slot: 'column-trigger', showOverflowTooltip: false, minWidth: 100, defaultShow: false },
    { field: 'serviceList', label: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_47d68cd0', slot: 'column-trigger', showOverflowTooltip: false, minWidth: 120, defaultShow: false },
    { field: 'serviceInstanceList', label: i18n.t('modules.views.alarmCenter.alarm.s_71673bab') as string, labelKey: 'modules.utils.filters.s_71673bab', slot: 'column-trigger', showOverflowTooltip: false, minWidth: 120, defaultShow: false },
    { field: 'processList', label: i18n.t('modules.views.alarmCenter.alarm.s_f88522cf') as string, labelKey: 'modules.views.alarmCenter.alarm.s_f88522cf', slot: 'column-trigger', showOverflowTooltip: false, minWidth: 100, defaultShow: false },
    { field: 'hostList', label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369', slot: 'column-trigger', showOverflowTooltip: false, minWidth: 100, defaultShow: false },
    { field: 'deviceNameList', label: i18n.t('modules.views.alarmCenter.alarm.s_63cf5e77') as string, labelKey: 'modules.views.alarmCenter.alarm.s_63cf5e77', slot: 'column-trigger', showOverflowTooltip: false, minWidth: 100, defaultShow: false },
    { field: 'type', label: i18n.t('modules.views.alarmCenter.alarm.s_c62e34c5') as string, labelKey: 'modules.views.alarmCenter.alarm.s_c62e34c5', unit: 'alarmType', sortable: true, minWidth: 100, defaultShow: false },
  ]

  private showChart = true
  private chartLoading = false
  private chartSource: any[] = []
  private chartOriginData: any = {}
  private lastAlarmData: any = null

  // 是否有检测规则的权限
  get hasRuleMenu () {
    return !!this.menus.find(t => t.path === '/config/rule')
  }

  private created () {
    if (!this.$store.getters['User/getGroupEnabled']) {
      this.columnConfig = this.columnConfig.filter(t => t.field !== 'domainName')
    }
  }

  private mounted () {
    this.durationChangeHandle()
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
    this.$eventBus.$on('AlarmInfoStatusChange', this, (val) => {
      if (val === '0') {
        this.durationChangeHandle()
      }
    });
  }

  private beforeDestroy () {
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh');
    this.$eventBus.$off('AlarmInfoStatusChange');
  }

  private formatFunc (data: any) {
    const getArr = (a: any) => {
      if (!StringIsEmpty(a) && !Array.isArray(a)) {
        return [a]
      } else if (Array.isArray(a) && !!a.filter(t => t).length) {
        return a.filter(t => t)
      }
      return null
    }
    const getList = (row: any, key: string, isLink = true) => {
      const _arr = getArr(row[key]) || getArr((row.trigger || {})[key]) || getArr((row.tags || {})[key]) || []
      if (key === 'serviceId') {
        const basicServiceMap = this.$store.getters['Service/basicServiceMap']
        return _arr.map(t => ({
          name: row.serviceName || this.serviceIdNameMapping[t] || basicServiceMap[t]?.name || t,
          id: t,
          isLink,
        })).filter(t => t.name)
      }
      return _arr.map(t => ({ name: t, isLink }))
    }
    const basicServiceMap = this.$store.getters['Service/basicServiceMap']
    return data.map((t: any) => {
      const services = getList(t, 'serviceId', false)
      const serviceName = t.serviceName
        || this.serviceIdNameMapping[t.serviceId]
        || basicServiceMap[t.serviceId]?.name
        || ''
      return {
        ...t,
        eventCnt: t.eventCnt || 0,
        serviceName,
        appList: getList(t, 'appName', false),
        serviceList: services,
        serviceInstanceList: getList(t, 'serviceInstance', false),
        processList: getList(t, 'pname', false),
        hostList: getList(t, 'host', false),
        deviceNameList: getList(t, 'device_name', false),
        domainName: this.groupMapping[t.gid] || '',
      }
    });
  }
  private tableRefresh () {
    (this.$refs.listTable as any)?.refresh()
  }

  // 时间范围改变
  private durationChangeHandle () {
    this.timeParams = { ...this.getGlobalTimeV2() }
    this.filterLoading = true;
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        this.queryParams = { ...data };
        this.$nextTick(() => {
          this.$refs.chooseCollapse.init().then((filter: any) => {
            this.queryFilter = { ...filter }
            this.filterLoading = false;
            this.chartTimeParams = null;
            this.getChartSource()
            this.tableRefresh()
          })
        })
      })
    })
  }

  private searchChangeHandle (data: any) {
    this.queryParams = { ...data };
    this.$nextTick(() => {
      this.$refs.chooseCollapse.init().then((filter: any) => {
        this.queryFilter = { ...filter }
        this.chartTimeParams = null;
        this.getChartSource()
        this.tableRefresh()
      })
    })
  }

  // 快捷筛选
  private filterChangeHandle (filter: any) {
    this.queryFilter = { ...filter }
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        this.queryParams = { ...data };
        this.chartTimeParams = null;
        this.getChartSource()
        this.tableRefresh()
      })
    })
  }
  private filterToggleHandle () {
    this.$nextTick(() => {
      this.$refs.alarmChart && this.$refs.alarmChart.resize();
    })
  }

  private toggleChartHandle () {
    this.showChart = !this.showChart;
    this.$nextTick(() => {
      (this.$refs.listTable as any)?.getHeightHandle();
    });
  }

  private async getChartSource () {
    const params = { ...this.getQueryParams, interval: this.timeParams.interval }
    this.chartLoading = true;
    const { result, error } = await toAsyncWait(AlarmApi.getAlarmTrend(params))
    this.chartLoading = false;
    if (!error) {
      const data = result?.data?.data || {};
      const timeKeyStr = Object.keys(data).map(i => +i).sort();
      const totalSeries: any = {
        name: i18n.t('modules.views.alarmCenter.alarm.s_aa0eab9d') as string, nameKey: 'modules.views.alarmCenter.alarm.s_aa0eab9d',
        type: 'bar',
        barWidth: '90%',
        data: [],
      }
      timeKeyStr.forEach(i => {
        const key = dayjs(i).format('YYYY-MM-DD HH:mm')
        const value = data[`${i}`]
        totalSeries.data.push({
          key,
          value: value.count || 0,
        });
      })
      this.chartSource = [totalSeries]
      this.chartOriginData = data;
      const lastItem = data[timeKeyStr.slice(-1)[0]]
      if (lastItem) {
        this.lastAlarmData = {
          total: lastItem.count || 0,
        }
      } else {
        this.lastAlarmData = null
      }
    } else {
      this.chartSource = []
      this.chartOriginData = {}
      this.lastAlarmData = null
    }
  }

  // 图表点击
  private chartTimeParams: any = null;
  private chartClickHandle (params: { xAxisName: string }) {
    const { xAxisName } = params
    // 正在加载中，不允许点击
    if (this.chartLoading) {
      return
    }
    const { toTime, interval } = this.timeParams
    const _toTime = +new Date(xAxisName) + interval * 1000
    this.chartTimeParams = {
      fromTime: xAxisName + ':00',
      toTime: _toTime <= +new Date(toTime) ? dayjs(_toTime).format('YYYY-MM-DD HH:mm:ss') : toTime,
    }
    this.tableRefresh()
  }
  private chartTooltipFormat (params: any[], strs: string[], valTickFormat: any) {
    let _axisValueLabel = ''
    const tips: any[] = []
    params.forEach((item: any) => {
      const { marker, axisValueLabel } = item;
      _axisValueLabel = axisValueLabel;
      tips.push(`<div style="overflow:hidden;margin-top:4px;line-height:18px;">
            ${marker} ${i18n.t('modules.views.alarmCenter.alarm.s_aa0eab9d') as string}
            <span style="float:right;margin-left:20px;font-weight:bold;">${valTickFormat(item.value)}</span>
          </div>
          <div style="clear:both"></div>`);
    })
    const tipStrs = tips;
    tipStrs.unshift(_axisValueLabel)
    return tipStrs
  }

  // 跳转到告警配置
  private viewConfigHandle () {
    if (!this.hasRuleMenu) {
      return
    }
    this.$router.push(buildAlarmListLocation('rule'))
  }
  // 跳转到告警详情
  private viewAlarmDetail (row: any, type?: string) {
    const query: any = { aid: row.id }
    if (type) {
      query.type = type
    }
    this.$router.push({
      path: '/alarmCenter/alarmDetail',
      query,
    })
  }
  // 触发对象下钻
  private viewTriggerHandle (item: any, field: string) {
    if (field === 'appList') {
      // RUM 模块已移除
    } else if (field === 'serviceList') {
      //
    } else if (field === 'serviceInstanceList') {
      //
    } else if (field === 'processList') {
      //
    } else if (field === 'hostList') {
      //
    }
  }

}
</script>

<style lang="scss" scoped>
.container-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .container-content {
    flex: 1;
    min-height: 600px;
    border-radius: 4px;
    background-color: var(--bg-color);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .container-content-body {
    flex: 1;
    overflow: hidden;
    display: flex;
    .collapse-choose {
      height: 100%;
      background-color: var(--bg-color);
    }
  }

  .small-btn {
    width: 32px;
    height: 32px;
    min-width: auto;
    padding: 5px;
    &:focus {
      background: transparent;
      border-color: var(--border-color-base);
      color: var(--color-text-regular);
    }
    &.active {
      color: var(--color-primary);
    }
  }

  .container-list {
    flex: 1;
    overflow: hidden;

    .container-table {
      margin-top: -10px;
      flex: 1;
    }

    .alarm-description-cell {
      overflow: hidden;
      width: 100%;
    }

    .alarm-description {
      flex: 1;
      min-width: 0;
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
}

.section-item {
  width: 100%;
  height: 200px;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  display: inline-block;
  vertical-align: top;
  color: var(--color-text-primary);
  position: relative;

  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px 0;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    position: absolute;
    top: 0;
    left: 0;
    z-index: 1;
  }

  .section-cont {
    height: 100%;
    padding: 0 10px;
  }
}
</style>
