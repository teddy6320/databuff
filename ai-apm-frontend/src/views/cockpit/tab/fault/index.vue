<template>
  <div v-loading="trendLoading && timeLoading" class="fault-wrapper flex-v bg-color p-20 br-4">
    <div class="fault-header flex-none flex-h">
      <div class="fault-tabs flex-1">
        <div
          v-for="tab in tabs"
          :key="tab.value"
          :class="['fault-tab-item cp', { active: styleCfg.type === tab.value }]"
          @click="switchTabHandle(tab)">
          <span class="fault-tab-label">{{ tab.labelKey ? $t(tab.labelKey) : tab.label }}</span>
          <span
            :class="['fault-tab-count', tab.count > 0 ? 'has-value' : 'is-zero']"
            :title="tab.tip">{{ tab.count | NumberFilter }}</span>
        </div>
      </div>

      <div class="toolbar flex-none">
        <el-button
          @click="showConfigHandle"
          plain icon="el-icon el-icon-setting" size="small">{{ $t('modules.views.deployInstall.apm.s_224e2ccd') }}</el-button>
      </div>
    </div>

    <trend-chart
      v-loading="trendLoading && !timeLoading"
      :source="trendSource"
      :sourceKey="trendSourceKey"
      :loading="trendLoading"
      @change="trendZoomChangeHandle"
      class="flex-none mb-20" />

    <div v-loading="!trendLoading && timeLoading" class="fault-content flex-1 flex-h flex-ai-start">
      <div v-for="(vData, key) in timeSource" :key="key" class="apm-alarm-group">
        <div class="apm-alarm-title">
          <span>{{ key | TimesToDateFilter('YYYY-MM-DD HH:mm') }}</span>
        </div>
        <div class="apm-alarm-item">
          <div>{{ $t('modules.views.appMonitor.serviceFlow.s_8f3747c0') }}</div>
          <span>{{ styleCfg.type === 'alarm' ? $t('modules.views.alarmCenter.problemDetail.s_1d4cbadb') : $t('modules.views.cockpit.tab.s_8eaadf75')  }}</span>
        </div>
        <div v-for='(item, idx) in vData.list' :key='item.serviceId || item.name || idx' class="apm-alarm-item">
          <div class="cp">
            <div :title='item.name' @click="viewServiceDetailHandle(item, key)" class="ell">{{ item.name || '-'  }}</div>
          </div>
          <span v-if="item.value" :data-style="item.trafficLight" @click='viewDetailHandle(item, key)' class="cp count">{{ item.value | NumberFilter }}</span>
          <span v-else :data-style="item.trafficLight" class="count">0</span>
        </div>
        <div v-if='!vData.list || !vData.list.length' class="empty-item-show describe">{{ $t('modules.views.cockpit.component.s_3c36425f') }}</div>
      </div>
    </div>

    <!-- 配置 -->
    <config-dialog
      v-model="showConfigDialog"
      :config="styleCfg"
      :loading="configLoading"
      @on-close="closeDialogHandle" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import { toAsyncWait } from '@/utils/common';
import ServiceApi from '@/api/service';
import TrendChart from './trend-chart.vue';
import ConfigDialog from './config-dialog.vue';
import { buildFaultStyleCfg } from './fault-config';

@Component({
  components: {
    TrendChart,
    ConfigDialog,
  },
})
export default class FaultComp extends Vue {
  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private styleCfg: any = buildFaultStyleCfg()

  private alarmCount = 0;
  private exceptionCount = 0;
  get tabs () {
    return [
      {
        label: i18n.t('modules.views.cockpit.tab.s_6d105e23') as string, labelKey: 'modules.views.cockpit.tab.s_6d105e23',
        value: 'alarm',
        count: this.alarmCount,
        tip: i18n.t('modules.views.cockpit.tab.s_babb4f69') as string,
      },
      {
        label: i18n.t('modules.views.cockpit.tab.s_9f8cda78') as string, labelKey: 'modules.views.cockpit.tab.s_9f8cda78',
        value: 'exception',
        count: this.exceptionCount,
        tip: i18n.t('modules.views.cockpit.tab.s_1e7be795') as string,
      },
    ]
  }

  private queryParams: any = {
    fromTime: '',
    toTime: '',
    interval: '',
  };

  private trendLoading = false;
  private trendSource: any[] = [];
  private trendSourceKey = '';

  private timeLoading = true;
  private timeSource: any = {};

  private configLoading = false;
  private showConfigDialog = false;
  private displayWindowEnd: string | number = '';

  private async created () {
  }

  private async mounted () {
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
    await this.getConfig();
    this.durationChangeHandle()
  }

  private beforeDestroy () {
    this.$eventBus.$off('GlobalRefresh')
  }

  private durationChangeHandle () {
    const { fromTime, toTime, interval } = { ...this.getGlobalTimeV2() }
    this.queryParams.fromTime = fromTime;
    this.queryParams.toTime = toTime;
    this.queryParams.interval = interval;
    this.displayWindowEnd = toTime;
    this.getAlarmAndExceptionCount();
    this.fetchTrendSource();
    this.fetchTimeSource(toTime);
  }

  private switchTabHandle(tab: any) {
    if (this.styleCfg.type === tab.value) {
      return
    }
    this.styleCfg.type = tab.value;
    this.toggleTabHandle(tab);
  }

  private toggleTabHandle(tab: any) {
    this.durationChangeHandle();
  }

  private async fetchTimeSource (toTime: string|number) {
    const { fromTime } = this.queryParams
    let _fromTime = +new Date(toTime) - 5 * 60 * 1000;
    _fromTime = Math.max(_fromTime, +new Date(fromTime));
    const params: any = {
      fromTime: dayjs(_fromTime).format('YYYY-MM-DD HH:mm:ss'),
      toTime: dayjs(toTime).format('YYYY-MM-DD HH:mm:ss'),
      orderBy: this.styleCfg.type
    }
    this.timeLoading = true
    const { result, error } = await toAsyncWait(ServiceApi.getServicesHealth(params))
    this.timeLoading = false
    if (!error) {
      const { data = [] } = result
      const { showServiceNumber } = this.styleCfg
      const source: any = {};
      (data || []).forEach((i: any) => {
        const { timestamp, serviceOrders = [] } = i
        if (timestamp === undefined || timestamp === null) {
          return
        }
        const serviceList: any[] = (serviceOrders || []).slice(0, showServiceNumber);
        source[timestamp] = {
          list: serviceList.map((s: any) => ({
            ...s,
            value: s.value,
            name: s.name,
          })),
        }
      })
      this.timeSource = source
    } else {
      this.timeSource = {}
    }
  }

  private async fetchTrendSource () {
    const { fromTime, toTime, interval } = this.queryParams
    const params: any = { fromTime, toTime, interval: 60, orderBy: this.styleCfg.type }
    this.trendLoading = true
    const { result, error } = await toAsyncWait(ServiceApi.getServiceAlarmTrend(params))
    this.trendLoading = false
    if (!error) {
      const { data = {} } = result || {};
      const timeKeyStr = Object.keys(data || {});
      const _source: any[] = timeKeyStr.map(i => +i).sort().map(i => ({
        key: dayjs(i).format('YYYY-MM-DD HH:mm'),
        value: data[`${i}`]?.value || 0,
        trafficLight: data[`${i}`]?.trafficLight || '',
      }))
      this.trendSource = _source
    } else {
      this.trendSource = []
    }
    this.trendSourceKey = new Date().getTime().toString();
  }

  private trendZoomChangeHandle (key: string) {
    const windowEnd = +new Date(key) + 60 * 1000;
    this.displayWindowEnd = windowEnd;
    this.fetchTimeSource(windowEnd);
    this.getAlarmAndExceptionCount();
  }

  // 查看告警/异常详情
  private viewDetailHandle (service: any, time: string) {
    const _query: any = {
      fromTime: `${+time}`,
      toTime: `${+time + 60 * 1000}`,
    }
    if (this.styleCfg.type === 'alarm') {
      this.$router.push({
        path: '/alarmCenter/alarm',
        query: {
          ..._query,
          serviceId: encodeURIComponent(service.serviceId),
        },
      });
    } else {
      this.$router.push({
        path: '/appMonitor/errors',
        query: {
          ..._query,
          sn: encodeURIComponent(service.name),
          sid: encodeURIComponent(service.serviceId),
        },
      });
    }
  }

  // 查看服务详情
  private viewServiceDetailHandle (service: any, time: string) {
    const _query: any = {
      sn: encodeURIComponent(service.service),
      sid: encodeURIComponent(service.serviceId),
    }
    this.$router.push({
      path: '/appMonitor/serviceDetail',
      query: _query,
    });
  }

  private async getConfig (type?: 'alarm'|'exception') {
    this.configLoading = true
    const { result, error } = await toAsyncWait(ServiceApi.getHealthConfig(!type ? {} : { type }));
    this.configLoading = false
    const data = !error ? (result?.data || {}) : {}
    this.styleCfg = buildFaultStyleCfg({
      ...this.styleCfg,
      ...data,
      type: type || data.type || this.styleCfg.type,
    })
  }
  private async showConfigHandle () {
    await this.getConfig(this.styleCfg.type);
    this.showConfigDialog = true;
  }
  private closeDialogHandle (payload?: any) {
    this.showConfigDialog = false;
    if (payload) {
      this.styleCfg = buildFaultStyleCfg({
        ...this.styleCfg,
        ...payload,
        type: payload.type || this.styleCfg.type,
      })
    }
    if (payload) {
      this.durationChangeHandle();
    }
  }

  // 获取展示窗口内告警/异常黄红色出现总次数
  private async getAlarmAndExceptionCount () {
    const { fromTime, toTime, interval } = this.queryParams
    const params: any = {
      fromTime,
      toTime,
      interval,
      windowEnd: this.displayWindowEnd || toTime,
    }
    const { result, error } = await toAsyncWait(ServiceApi.getServiceAlarmTotal(params))
    if (!error) {
      const { data = {} } = result || {};
      this.alarmCount = data.alarmCount || 0;
      this.exceptionCount = data.exceptionCount || 0;
    } else {
      this.alarmCount = 0;
      this.exceptionCount = 0;
    }
  }
}
</script>

<style lang="scss" scoped>
.fault-wrapper {
  min-height: 100%;

  .fault-header {
    margin-bottom: 20px;
    align-items: center;

    .toolbar {
      margin-left: 16px;
    }
  }

  .fault-tabs {
    display: inline-flex;
    padding: 4px;
    background: var(--background-color-base);
    border-radius: 6px;
    gap: 4px;
  }

  .fault-tab-item {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    border-radius: 4px;
    color: var(--color-text-secondary);
    transition: color .2s ease, background-color .2s ease, box-shadow .2s ease;

    &:hover:not(.active) {
      color: var(--color-text-primary);
    }

    &.active {
      color: var(--color-text-primary);
      background: var(--bg-color);
      box-shadow: 0 1px 4px rgba(18, 19, 23, 0.08);

      .fault-tab-label {
        font-weight: 500;
      }
    }
  }

  .fault-tab-label {
    font-size: 14px;
    line-height: 20px;
  }

  .fault-tab-count {
    min-width: 24px;
    padding: 0 8px;
    height: 22px;
    border-radius: 11px;
    font-size: 12px;
    font-weight: 600;
    line-height: 22px;
    text-align: center;
    background: rgba(119, 122, 126, 0.12);
    color: var(--color-text-secondary);

    &.has-value {
      background: rgba(41, 98, 255, 0.1);
      color: #2962ff;
    }

    &.is-zero {
      font-weight: 500;
    }
  }

  .fault-content {
    min-height: 100px;
    height: calc(100% - 164px);
  }
}

.apm-alarm-group {
  flex: 1;
  border: 1px solid var(--border-color-base);
  border-bottom: none;
  border-radius: 2px;
  overflow: hidden;
  & + .apm-alarm-group {
    margin-left: 16px;
  }

  .apm-alarm-title,
  .apm-alarm-item {
    display: flex;
    justify-content: space-between;
    border-bottom: 1px solid var(--border-color-base);
    font-size: 13px;
    line-height: 18px;
  }

  .apm-alarm-title {
    padding: 8px 10px;
    background-color: var(--background-color-base);
  }

  .apm-alarm-item {
    padding: 10px 10px 9px;
    & > div:first-child {
      flex: 1;
      min-width: 0;
      overflow: hidden;
    }
    & > span:last-child {
      flex-shrink: 0;
      margin-left: 8px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      white-space: nowrap;
    }

    .count {
      font-size: 14px;
      font-weight: 500;
      &[data-style="red"] {
        color: #E12828;
      }
      &[data-style="yellow"] {
        color: #F79532;
      }
      &[data-style="green"] {
        color: #08BE7E;
      }
    }
  }
}
</style>
