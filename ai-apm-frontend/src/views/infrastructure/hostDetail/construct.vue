<template>
  <div class="detail-construct">
    <div class="panel-box theme-panel-box">
      <el-tooltip placement="top" effect="light" :content="$t('modules.views.infrastructure.hostDetail.s_ad423275')">
        <div :class="['panel-t large panel-t-app', { active: panelActive === 'app' }]">
          <span class="db-icon-aside-application img"></span>
          <div><div class="count">{{ (detail.apps || []).length }}</div>{{ $t('modules.views.infrastructure.hostDetail.s_5b0520a9') }}</div>
        </div>
      </el-tooltip>
      <el-tooltip placement="left" effect="light" :content="$t('modules.views.infrastructure.hostDetail.s_db8dd4d2')">
        <div :class="['panel-t', { active: panelActive === 'cpu' }]" @click="togglePanelActive('cpu')">
          <span class="db-icon-system-cpu img"></span>
          <div><div class="count">{{ detail.cpuUsage | PercentFilter }}</div>CPU</div>
        </div>
      </el-tooltip>
      <el-tooltip placement="right" effect="light" :content="$t('modules.views.infrastructure.hostDetail.s_ca2ba0bc')">
        <div :class="['panel-t', { active: panelActive === 'memory' }]" @click="togglePanelActive('memory')">
          <span class="db-icon-system-memory img"></span>
          <div><div class="count">{{ detail.memoryUsage | PercentFilter }}</div>{{ $t('modules.views.infrastructure.hostDetail.s_9932551c') }}</div>
        </div>
      </el-tooltip>
      <el-tooltip placement="left" effect="light" :content="$t('modules.views.infrastructure.hostDetail.s_ba40861c')">
        <div :class="['panel-t', { active: panelActive === 'networkCard' }]" @click="togglePanelActive('networkCard')">
          <span class="db-icon-networkcard img"></span>
          <div><div v-if="!isK8sNode" class="count">{{ interfaceCount }}</div>{{ $t('modules.views.infrastructure.hostDetail.s_fa5a706d') }}</div>
        </div>
      </el-tooltip>
      <el-tooltip placement="right" effect="light" :content="$t('modules.views.infrastructure.hostDetail.s_58d742ec')">
        <div :class="['panel-t', { active: panelActive === 'disk' }]" @click="togglePanelActive('disk')">
          <span class="db-icon-system-disk img"></span>
          <div><div class="count">{{ diskSize | BytesFilter }}</div>{{ $t('modules.views.infrastructure.hostDetail.s_4f5537dd') }}</div>
        </div>
      </el-tooltip>
    </div>

    <div v-for="(item, index) in panelChartData" :key="item.title"
      v-loading="item.loading"
      :class="{
        'section-half-item': (panelChartData.length % 2 === 1 && index > 0) ||
            (panelChartData.length % 2 === 0 && panelChartData.length > 2),
      }"
      class="section-item">
      <div class="section-title">
        <div class="flex-h">
          {{ item.titleKey ? $t(item.titleKey) : item.title }}
          <div v-if="item.toggle" class="section-title-toggle ml-20">
            <span
              @click="toggleChartHandle(item, index, 'all')"
              :class="['toggle-t', { active: item.toggle === 'all' }]"
              >{{ $t('modules.views.infrastructure.hostDetail.s_330852f6') }}</span>
            <span
              @click="toggleChartHandle(item, index, 'single')"
              :class="['toggle-t', { active: item.toggle === 'single' }]"
              >{{ item.type === 'cpu' ? $t('modules.views.infrastructure.hostDetail.s_cc683f45') : $t('modules.views.infrastructure.hostDetail.s_9b3ad9bd')  }}</span>
          </div>
        </div>
        <span
          @click="viewHostMetric()"
          class="blue cp font-12 fw-400">
          {{ $t('modules.views.appMonitor.relationMap.s_90ef7c48') }}<i class="db-icon-right font-12"></i>
        </span>
      </div>
      <div class="section-cont">
        <basic-chart
          :source="item.source"
          :showLegend="item.source.length > 1"
          :sourceTop="item.toggle === 'single' ? item.sourceTop : 0"
          :showEmpty="!item.loading && !item.source.length"
          :tooltipEnterable="false"
          :tooltipConfine="true"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { toAsyncWait } from '@/utils/common';
import dayjs from 'dayjs';
import InfraApi from '@/api/infrastructure';
import MetricApi from '@/api/metric';

type PanelType = 'app' | 'cpu' | 'memory' | 'networkCard' | 'disk' | '';

@Component
export default class DetailConstruct extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: () => ({}) }) private detail!: any;
  @Prop({ default: false }) private isK8sNode!: boolean;

  get interfaceCount () {
    return (this.detail?.network?.interfaces || []).length
  }

  get diskSize () {
    const { totalDisk } = this.detail
    const filesystem: any[] = (this.detail?.filesystem || []).filter((t: any) => !!t).map((t: any) => ({
      ...t,
      kb_size: !isNaN(+t.kb_size) ? +t.kb_size : 0,
    }));
    if (totalDisk && !isNaN(parseFloat(totalDisk))) {
      return parseFloat(totalDisk) * 1024
    } else if (filesystem.length) {
      const rootFilesystem = filesystem.find(t => t.mounted_on === '/')
      if (rootFilesystem) {
        return rootFilesystem.kb_size * 1024
      } else {
        return filesystem.map((t: any) => t.kb_size).reduce((a: number, b: number) => a + b) * 1024
      }
    }
    return 0
  }

  @Watch('detail.hostOs', { immediate: true })
  private onDetailHostOsChange () {
    if (this.detail.hostOs === 'windows') {
      this.chartData = this.chartData.filter(t => t.system !== '!windows')
    }
  }

  private panelActive: PanelType = ''
  private panelChartData: any[] = []
  private chartData: any[] = [
    {
      type: 'cpu',
      title: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, titleKey: 'modules.views.appMonitor.relationMap.s_7054bc34',
      toggle: 'all', // all | single
      metrics: [{
        name: 'system.cpu.usage',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_7054bc34',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'cpu',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_6f59d143') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_6f59d143',
      system: '!windows',
      metrics: [{
        name: 'system.load.1',
        nameCn: 'System Load 1',
      }, {
        name: 'system.load.5',
        nameCn: 'System Load 5',
      }, {
        name: 'system.load.15',
        nameCn: 'System Load 15',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'memory',
      title: i18n.t('modules.views.appMonitor.relationMap.s_31cb8d97') as string, titleKey: 'modules.views.appMonitor.relationMap.s_31cb8d97',
      metrics: [{
        name: 'system.mem.usage',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_31cb8d97') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_31cb8d97',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'memory',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_06dbe8e8') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_06dbe8e8',
      metrics: [{
        name: 'system.swap.pct_free',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_06dbe8e8') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_06dbe8e8',
      }],
      format: (val: number) => 100 - val,
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'networkCard',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_f4055f69') as string, titleKey: 'modules.views.appMonitor.relationMap.s_f4055f69',
      metrics: [{
        name: 'system.net.bytes_sent',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_97ecc1bb') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_97ecc1bb',
      }, {
        name: 'system.net.bytes_rcvd',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_80c86063') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_80c86063',
      }],
      aggregation: 'sum',
      loading: false,
      source: [],
    },
    {
      type: 'networkCard',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_8e844ba5') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_8e844ba5',
      metrics: [{
        name: 'system.net.packets_out.count',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_7ba26ee3') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_7ba26ee3',
      }, {
        name: 'system.net.packets_in.count',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_d212209d') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_d212209d',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'networkCard',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_dfea4fef') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_dfea4fef',
      metrics: [{
        name: 'system.net.packets_out.error',
        names: ['system.net.packets_out.error', 'system.net.packets_out.count'],
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_b85e0a0d') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_b85e0a0d',
      }, {
        name: 'system.net.packets_in.error',
        names: ['system.net.packets_in.error', 'system.net.packets_in.count'],
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_515d4827') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_515d4827',
      }],
      unit: 'percent',
      format: (a: number, b: number) => b !== 0 ? a / b : 0,
      aggregation: 'avg',
      loading: false,
      source: [],
    },
    {
      type: 'disk',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_4e83182b') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_4e83182b',
      toggle: 'all', // all | single
      metrics: [{
        name: 'system.disk.pct_used',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_4e83182b') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_4e83182b',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
      sourceTop: 10,
    },
    {
      type: 'disk',
      title: i18n.t('modules.views.infrastructure.hostDetail.s_19112692') as string, titleKey: 'modules.views.infrastructure.hostDetail.s_19112692',
      toggle: 'all', // all | single
      metrics: [{
        name: 'system.io.wkb_s',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_ea22704d') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_ea22704d',
      }, {
        name: 'system.io.rkb_s',
        nameCn: i18n.t('modules.views.appMonitor.relationMap.s_085cf12e') as string, nameCnKey: 'modules.views.appMonitor.relationMap.s_085cf12e',
      }],
      aggregation: 'sum',
      loading: false,
      source: [],
      sourceTop: 10,
    },
    {
      type: 'disk',
      title: i18n.t('modules.views.appMonitor.traceDetail.s_2563881c') as string, titleKey: 'modules.views.appMonitor.traceDetail.s_2563881c',
      metrics: [{
        name: 'system.io.w_await',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_dbcd6064') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_dbcd6064',
      }, {
        name: 'system.io.r_await',
        nameCn: i18n.t('modules.views.appMonitor.traceDetail.s_8bafad84') as string, nameCnKey: 'modules.views.appMonitor.traceDetail.s_8bafad84',
      }],
      aggregation: 'avg',
      loading: false,
      source: [],
    },
  ];

  public getData () {
    const { panel } = this.$route.query
    this.panelActive = (panel || 'cpu') as PanelType
    this.togglePanelActive(this.panelActive)
  }

  private togglePanelActive (value: PanelType) {
    this.panelActive = value
    const panel = this.$route.query.panel
    if (value !== panel) {
      this.$router.replace({ query: { ...this.$route.query, panel: value } })
    }

    this.chartData.forEach(t => t.source = [])
    this.panelChartData = this.chartData.filter(t => t.type === this.panelActive)
    this.panelChartData.forEach((item, index) => {
      this.getChartItem(item, index)
    })
  }

  private toggleChartHandle (item: any, index: number, type: 'all' | 'single') {
    if (item.toggle === type) {
      return;
    }
    item.toggle = type;
    item.source = [];
    this.getChartItem(item, index);
  }

  // 查看主机指标
  private viewHostMetric () {
    this.$router.replace({
      query: {
        ...this.$route.query,
        type: 'metric',
        app: encodeURIComponent(this.isK8sNode ? 'kubelet' : 'system'),
      }
    })
    this.$emit('toggle-type', { value: 'metric' });
  }

  // 获取图表数据
  private getChartItem (item: any, index: number) {
    const params: any = {
      tagList: [`host:${this.queryParams.hostName}`],
      startTime: this.queryParams.fromTime,
      endTime: this.queryParams.toTime,
      interval: this.queryParams.interval,
      groups: [],
    }
    if (item.toggle !== 'single') {
      item.metrics.forEach((t: any) => {
        if (!t.names) {
          this.exploreMetricByGroupGraph({
            ...params,
            metric: t.name,
            aggregation: item.aggregation,
          }, index, t, this.panelActive)
        } else {
          this.exploreMultMetricGraph({
            ...params,
            aggregation: item.aggregation,
          }, index, t, this.panelActive)
        }
      })
    } else if (item.title === i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string) {
      this.exploreMetricByGroupGraph({
        ...params,
        metric: 'system.core.usage',
        groups: ['core'],
        aggregation: 'avg',
      }, index, null, this.panelActive)
    } else if (item.title === i18n.t('modules.views.appMonitor.traceDetail.s_4e83182b') as string) {
      this.exploreMetricByGroupGraph({
        ...params,
        metric: 'system.disk.in_use',
        groups: ['device_name'],
        aggregation: 'avg',
      }, index, null, this.panelActive)
    } else if (item.title === i18n.t('modules.views.infrastructure.hostDetail.s_19112692') as string) {
      item.metrics.forEach((t: any) => {
        this.exploreMetricByGroupGraph({
          ...params,
          metric: t.name,
          groups: ['device_name'],
          aggregation: 'avg',
        }, index, { nameCn: t.nameCn }, this.panelActive)
      })
    }
  }
  private async exploreMultMetricGraph (params: any, idx: number, metric: any, panel: string) {
    this.panelChartData[idx].loading = true
    const { unit, format } = this.panelChartData[idx]
    const len = metric.names.length
    const resultDataArr: any[] = [] // 多个指标请求结果数组
    for (const t of metric.names) {
      const metricQuery = {
        types: [],
        metric: t,
        by: params.groups,
        aggs: params.aggregation,
        from: params.tagList.map((tag: any, index: number) => {
          const [key, ...values] = tag.split(':')
          return { left: key, operator: '=', right: values.join(':'), connector: 'AND' }
        }),
      }
      const { result, error } = await toAsyncWait(MetricApi.getMetricChart({
        query: { A: metricQuery, expr: 'A' },
        start: Math.floor(+new Date(params.startTime) / 1000),
        end: Math.floor(+new Date(params.endTime) / 1000),
        interval: params.interval,
      }));
      resultDataArr.push(!error ? (result?.data || [])[0] || {} : {})
    }
    if (panel !== this.panelActive) {
      // 防止频繁切换导致数据混乱
      this.panelChartData[idx].loading = false
      return;
    }
    const data: any = {} // 数据聚合，全量keys
    resultDataArr.forEach((t, i) => {
      const values = t.values || []
      values.forEach(([key, value]: any) => {
        if (!data[key]) {
          data[key] = Array(len).fill(0)
        }
        data[key][i] = value
      })
    })

    const metrics: string[] = this.panelChartData[idx].metrics.map((t: any) => t.nameCn)
    this.panelChartData[idx].source = [
      ...this.panelChartData[idx].source,
      {
        name: metric.nameCn,
        unit,
        area: metrics.length <= 2,
        smooth: true,
        data: Object.entries(data).map(([key, value]: any) => ({
          key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
          value: value.every((t: any) => typeof t === 'number') ? format(...value) : '-',
        }))
      },
    ].sort((a, b) => {
      return metrics.findIndex(t => t === a.name) - metrics.findIndex(t => t === b.name)
    })
    this.panelChartData[idx].loading = false
  }
  private async exploreMetricByGroupGraph (params: any, idx: number, metric: any, panel: string) {
    this.panelChartData[idx].loading = true
    const metricQuery = {
      types: [],
      metric: params.metric,
      by: params.groups,
      aggs: params.aggregation,
      from: params.tagList.map((tag: any, index: number) => {
        const [key, ...values] = tag.split(':')
        return { left: key, operator: '=', right: values.join(':'), connector: 'AND' }
      }),
    }
    const { result, error } = await toAsyncWait(MetricApi.getMetricChart({
      query: { A: metricQuery, expr: 'A' },
      start: Math.floor(+new Date(params.startTime) / 1000),
      end: Math.floor(+new Date(params.endTime) / 1000),
      interval: params.interval,
    }));
    if (panel !== this.panelActive) {
      // 防止频繁切换导致数据混乱
      this.panelChartData[idx].loading = false
      return;
    }
    if (!error) {
      const metrics: string[] = this.panelChartData[idx].metrics.map((t: any) => t.nameCn)
      const data = result.data || []
      const { format } = this.panelChartData[idx]
      const source = [
        ...this.panelChartData[idx].source,
        ...data.map((item: any) => {
          const tag = Object.entries(item.tags || {}).map(([k, v]) => `${k}:${v || ''}`).join(';')
          return {
            name: !params.groups.length ? metric?.nameCn : tag + (metric ? ` ${metric.nameCn}` : ''),
            unit: (item.units || [])[1] || '',
            area: !params.groups.length && metrics.length <= 2,
            smooth: true,
            data: (item.values || []).map(([key, value]: any) => ({
              key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
              value: typeof value === 'number' && format ? format(value) : value,
            }))
          }
        })
      ]
      if (!params.groups.length) {
        this.panelChartData[idx].source = source.sort((a, b) => {
          return metrics.findIndex(t => t === a.name) - metrics.findIndex(t => t === b.name)
        })
      } else {
        this.panelChartData[idx].source = source
      }
    }
    this.panelChartData[idx].loading = false
  }
}
</script>

<style lang="scss" scoped>
.detail-construct {
  height: 100%;
  overflow: auto;
  position: relative;
}

.panel-box {
  margin: 0 auto;
  padding: 10px;
  width: 432px;
  border-radius: 4px;
  opacity: 1;
  box-sizing: border-box;
  border: 1px solid #DEE5FA;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;

  .panel-t {
    width: calc(50% - 4px);
    height: 56px;
    padding-left: 44px;
    background: #E6ECFC;
    border-radius: 6px;
    display: flex;
    align-items: center;
    font-size: 13px;
    color: var(--color-text-primary);
    line-height: 16px;
    cursor: pointer;
    & + .panel-t {
      margin-top: 8px;
    }
    &.large {
      width: 100%;
      padding-left: 166px;
    }
    &.panel-t-app {
      cursor: auto;
    }
    &.active {
      background-color: var(--color-primary);
      color: #fff;
      cursor: auto;
      .img {
        color: #fff;
      }
    }
    .img {
      display: block;
      margin-right: 16px;
      font-size: 36px;
      color: #5582FF;
    }
    .count {
      margin-bottom: 2px;
      font-size: 16px;
      line-height: 18px;
      font-weight: 500;
    }
  }
}

.section-item {
  margin-top: 16px;
  width: 100%;
  height: 238px;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  display: inline-block;
  vertical-align: top;
  color: var(--color-text-primary);
  &.section-half-item {
    width: calc(50% - 8px);
    &:nth-of-type(2n) {
      margin-left: 16px;
    }
  }

  .section-title {
    display: flex;
    justify-content: space-between;
    padding: 16px 20px 0;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    .section-title-toggle {
      margin-top: -3px;
      margin-bottom: -3px;
      border: 1px solid var(--border-color-light);
      border-radius: 4px;
      display: flex;
      .toggle-t {
        padding: 0 10px;
        text-align: center;
        font-size: 12px;
        font-weight: normal;
        line-height: 26px;
        cursor: pointer;
        position: relative;
        &.active {
          color: var(--color-primary);
          cursor: auto;
        }
        &:not(:first-child)::before {
          content: '';
          margin-top: -6px;
          height: 12px;
          border-left: 1px solid var(--border-color-light);
          position: absolute;
          top: 50%;
          left: 0;
        }
      }
    }
  }

  .section-cont {
    height: calc(100% - 38px);
    padding: 0 10px;
    :deep(.empty-show) {
      top: 4px;
    }
  }
}
</style>
