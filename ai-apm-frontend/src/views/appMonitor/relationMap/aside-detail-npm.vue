<template>
  <div class="node-detail-wrap">

    <div class="node-detail">
      <div class="detail-header">
        <div class="title">
          <span class="db-icon mr-5">{{ current.typeIcon | DbIconFilter }}</span>
          <span @click="viewServiceDetailHandle" class="title-link cp">{{ current.name || $t('modules.views.appMonitor.relationMap.s_b88b9847')  }}</span>
        </div>
      </div>

      <div class="detail-content">
        <el-collapse v-model='activeNames'>
          <el-collapse-item  v-if='!isPod' :title="$t('modules.views.alarmCenter.alarm.s_aa0eab9d')" name='1'>
            <div class="detail-alarm">
              <div class="alarm-head flex-h" :class="{ 'w-auto': true }">
                <div class="title flex-h font-12">{{ $t('modules.views.alarmCenter.alarm.s_aa0eab9d') }}<span v-if='eventCount' class="count">{{ eventCount }}</span></div>
                <div class="status-tab">
                  <span
                    @click="eventStatusChangeHandle('all')"
                    :class="{ active: queryParams.status === 'all' }"
                    class="status-t cp font-12">{{ $t('modules.views.alarmCenter.rootCause.s_a8b0c204') }}</span>
                  <span
                    @click="eventStatusChangeHandle(3)"
                    :class="{ active: queryParams.status === 3 }"
                    class="status-t cp font-12">{{ $t('modules.utils.filters.s_fc7e3846') }}</span>
                  <span
                    @click="eventStatusChangeHandle(2)"
                    :class="{ active: queryParams.status === 2 }"
                    class="status-t cp font-12">{{ $t('modules.utils.filters.s_bde77082') }}</span>
                </div>
              </div>

              <div v-loading="loading.event" class="alarm-list">
                <div
                  v-for="(item, index) in eventSource"
                  :key="index"
                  class="alarm-item">
                  <el-tooltip effect="light" :content='item.level | AlarmStatusFilter' placement="top">
                    <span class="alarm-status" :data-status='item.level'></span>
                  </el-tooltip>
                  <div class="flex-h">
                    <div
                      @click="viewEventHandle(item)"
                      class="name">{{ item.descriptionKey ? $t(item.descriptionKey) : item.description }}</div>
                  </div>
                  <div class="time">{{ item.timestamp | TimesToDateFilter }}</div>
                </div>

                <div v-if="eventSource.length" class="more">
                  <span @click="viewMoreEventHandle">
                    {{ $t('modules.views.appMonitor.relationMap.s_90ef7c48') }}
                    <i class="el-icon el-icon-arrow-right"></i>
                  </span>
                </div>

                <div v-if="!loading.event && !eventSource.length" class="empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
              </div>
            </div>
          </el-collapse-item>
          <el-collapse-item :title='isPod ? $t('modules.views.appMonitor.relationMap.s_84ada0f7') : $t('modules.views.appMonitor.relationMap.s_7054bc34')' name='2'>
            <div class="chart-wrap">
              <div class="chart-cont" v-loading="loading.cpu">
                <basic-chart
                  :showEmpty="!loading.cpu && !cpuSource.length"
                  :showLegend="true"
                  :minInterval="1"
                  :min="0"
                  :yAxisSplitNum='2'
                  :textSmallMode="true"
                  :compactGrid='true'
                  :interval="queryParams.interval"
                  :showAxisLabelCount="4"
                  :source='cpuSource' />
              </div>
            </div>
          </el-collapse-item>
          <el-collapse-item :title='isPod ? $t('modules.views.appMonitor.relationMap.s_34b9b267') : $t('modules.views.appMonitor.relationMap.s_31cb8d97')' name='3'>
            <div class="chart-wrap">
              <div class="chart-cont" v-loading="loading.mem">
                <basic-chart
                  :showEmpty="!loading.mem && !memSource.length"
                  :showLegend="true"
                  :minInterval="1"
                  :min="0"
                  :yAxisSplitNum='2'
                  :textSmallMode="true"
                  :compactGrid='true'
                  :interval="queryParams.interval"
                  :showAxisLabelCount="4"
                  :source='memSource' />
              </div>
            </div>
          </el-collapse-item>
          <el-collapse-item v-if='isHost && !isPod' :title="$t('modules.views.appMonitor.relationMap.s_2c8a6d17')" name='4'>
            <div class="chart-wrap">
              <div class="chart-cont" v-loading="loading.disk">
                <basic-chart
                  :showEmpty="!loading.disk && !diskSource.length"
                  :showLegend="true"
                  :minInterval="1"
                  :min="0"
                  :yAxisSplitNum='2'
                  :textSmallMode="true"
                  :compactGrid='true'
                  :interval="queryParams.interval"
                  :showAxisLabelCount="4"
                  :source='diskSource' />
              </div>
            </div>
          </el-collapse-item>
          <el-collapse-item v-if='isHost && !isPod' title='IO' name='5'>
            <div class="chart-wrap">
              <div class="chart-cont" v-loading="loading.io">
                <basic-chart
                  :showEmpty="!loading.io && !ioSource.length"
                  :showLegend="true"
                  :minInterval="1"
                  :min="0"
                  :yAxisSplitNum='2'
                  :textSmallMode="true"
                  :compactGrid='true'
                  :interval="queryParams.interval"
                  :showAxisLabelCount="4"
                  :source='ioSource' />
              </div>
            </div>
          </el-collapse-item>
          <el-collapse-item :title="$t('modules.views.appMonitor.relationMap.s_f4055f69')" name='6'>
            <div class="chart-wrap">
              <div class="chart-cont" v-loading="loading.net">
                <basic-chart
                  :showEmpty="!loading.net && !netSource.length"
                  :showLegend="true"
                  :minInterval="1"
                  :min="0"
                  :yAxisSplitNum='2'
                  :textSmallMode="true"
                  :compactGrid='true'
                  :interval="queryParams.interval"
                  :showAxisLabelCount="4"
                  :source='netSource' />
              </div>
            </div>
          </el-collapse-item>
          <el-collapse-item v-if='isProcess' :title="$t('modules.views.appMonitor.relationMap.s_1d5e0d0c')" name='7'>
            <div class="chart-wrap">
              <div class="chart-cont" v-loading="loading.through">
                <basic-chart
                  :showEmpty="!loading.through && !thorughSource.length"
                  :showLegend="true"
                  :minInterval="1"
                  :min="0"
                  :yAxisSplitNum='2'
                  :textSmallMode="true"
                  :compactGrid='true'
                  :interval="queryParams.interval"
                  :showAxisLabelCount="4"
                  :source='thorughSource' />
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch, Prop } from 'vue-property-decorator'
import { Getter } from 'vuex-class';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import BasicChart from '@/components/charts/basic-chart.vue';
import { toAsyncWait } from '@/utils/common'
import AlarmApi from '@/api/alarm';
import MetricApi from '@/api/metric'

@Component({
  components: {
    BasicChart,
  },
})
export default class NodeDetail extends Vue {
  @Prop({ default: () => ({}) }) private current!: any;
  @Prop({ default: '' }) private detailType!: any;

  @Watch('current', { immediate: true })
  private onCurrentChange (newVal: any, oldVal: any) {
    newVal = newVal || {}
    oldVal = oldVal || {}
    if (!newVal.id) {
      return
    }
    if (newVal.baseType === oldVal.baseType && newVal.id === oldVal.id) {
      return
    }
    this.fetchAllData()
  }

  get isHost () {
    return this.current && this.current.baseType === 'host'
  }

  get isProcess () {
    return this.current && this.current.baseType === 'process'
  }

  get isContainer () {
    return this.current && (this.current.baseType === 'container' || this.current.originType === 'container')
  }

  get isPod () {
    return this.current && (this.current.originType === 'pod')
  }


  private activeNames = ['1', '2', '3', '4', '5', '6', '7']

  private queryParams: any = {
    fromTime: '',
    toTime: '',
    interval: 300,
    status: 'all', // 'all'|3|2
  }

  private loading = {
    event: true,
    cpu: true,
    mem: true,
    disk: true,
    io: true,
    net: true,
    through: true,
  }

  private cpuSource: any = []
  private memSource: any = []
  private diskSource: any = []
  private ioSource: any = []
  private netSource: any = []
  private thorughSource: any = []

  private created () {
    //
  }

  private regetGlobalTime () {
    const { fromTime, toTime, interval } = this.getGlobalTimeV2()
    this.queryParams.fromTime = fromTime
    this.queryParams.toTime = toTime
    this.queryParams.interval = interval
  }

  public fetchAllData () {
    const { baseType } = this.current
    this.queryParams.status = 'all'
    this.regetGlobalTime()
    switch (baseType) {
      case 'process':
        break;
      case 'container':
        break;
      case 'host':
        break;
    }
    if (!this.isPod) {
      this.getEventSource()
    }
    this.getCpuSource()
    this.getMemSource()
    this.getDiskSource()
    this.getIoSource()
    if (this.isHost) {
      this.getHostNetSource()
    } else if (this.isPod) {
      this.getPodNetSource()
      // this.getPodThroughSource()
    } else if (this.isProcess) {
      this.getProcessNetSource()
      this.getProcessThroughSource()
    }
  }

  // 获取服务相关告警
  private eventSource: any[] = [];
  private eventCount = 0;
  private async getEventSource () {
    const { fromTime, toTime, interval, status } = this.queryParams
    const { baseType, name, id, hostName } = this.current
    const _params: any = {
      needCount: true, // 是否需要total、count字段
      fromTime,
      toTime,
      interval,
      offset: 0,
      size: 5,
      sortField: 'timestamp',
      sortOrder: 'desc',
      level: status === 'all' ? null : [status],
      trigger: {},
    }
    if (baseType === 'process') {
      _params.trigger.pname = name ? [name] : null
      _params.trigger.host = hostName ? [hostName] : null
    } else if (baseType === 'container') {
      _params.trigger.containerId = id ? [id] : null
      _params.trigger.containerName = name ? [name] : null
    } else if (baseType === 'host') {
      _params.trigger.host = name ? [name] : null
    } else {
      delete _params.trigger
    }
    this.loading.event = true
    const { result, error } = await toAsyncWait(AlarmApi.getAlarmListNew(_params))
    this.loading.event = false;
    if (!error) {
      const data = result.data || {}
      this.eventSource = data.list || [];
      this.eventCount = data.total || 0;
      if (status === 'all' && !this.eventCount) {
        this.activeNames = this.activeNames.filter(i => i !== '1')
      }
    }
  }
  private eventStatusChangeHandle (status: 'all'|3|2) {
    this.queryParams.status = status
    this.regetGlobalTime()
    this.getEventSource()
  }
  // 跳转至告警详情
  private viewEventHandle (row: any) {
    // v2.7.2版本暂时去掉权限判断
    // if (row && !row.hasLimit) {
    //   this.$message.warning(i18n.t('modules.views.appMonitor.relationMap.s_e89717d4') as string)
    //   return;
    // }
    const query: any = {
      aid: row.id,
    }
    this.$router.push({
      path: '/alarmCenter/alarmDetail',
      query
    })
  }
  // 查看更多事件
  private viewMoreEventHandle () {
    const { fromTime, toTime, durationRange } = this.$route.query
    const { baseType, name, id, hostName } = this.current
    const query: any = {}
    if (fromTime && toTime) {
      query.fromTime = fromTime
      query.toTime = toTime
    } else if (durationRange) {
      query.durationRange = durationRange
    }
    switch (baseType) {
      case 'process':
        query.pname = encodeURIComponent(name)
        if (hostName) {
          query.host = encodeURIComponent(hostName)
        }
        break;
      case 'container':
        query.containerName = encodeURIComponent(name)
        break;
      case 'host':
        query.host = encodeURIComponent(name)
        break;
    }
    this.$router.push({
      path: '/alarmCenter/alarm',
      query,
    });
  }

  // 获取请求数
  private async getCpuSource () {
    const { baseType, name, id } = this.current
    const { fromTime, toTime, interval } = this.queryParams
    this.loading.cpu = true
    if (!this.isPod) {
      const _params: any = {
        start: dayjs(fromTime).valueOf() / 1000,
        end: dayjs(toTime).valueOf() / 1000,
        interval,
        query: {
          A: {
            aggs: 'avg',
            by: [],
            types: [],
            metric: '',
            from: [
              {
                left: '',
                operator: '=',
                right: '',
                connector: 'AND',
              }
            ]
          }
        }
      }
      switch (baseType) {
        case 'process':
          _params.query.A.metric = 'process.cpu.usage.pct'
          _params.query.A.from[0].left = 'pname'
          _params.query.A.from[0].right = name
          break;
        case 'container':
          _params.query.A.metric = 'docker.cpu.usage'
          _params.query.A.from[0].left = 'container_id'
          _params.query.A.from[0].right = id
          break;
        case 'host':
          _params.query.A.metric = 'system.cpu.usage'
          _params.query.A.from[0].left = 'host'
          _params.query.A.from[0].right = name
          break;
      }
      const metricRst = await toAsyncWait(MetricApi.getMetricChart(_params))
      const metricData = (metricRst.result ? metricRst.result.data : {}) || {}
      const item = metricData[0] || {}
      const values: any[] = item.values || []
      if (values.length) {
        this.cpuSource = [{
          name: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, nameKey: 'modules.views.appMonitor.relationMap.s_7054bc34',
          unit: 'percent',
          area: true,
          data: values.map(([key, ...value]: any) => {
            value = value[0]
            return {
              key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
              value,
            }
          }),
        }]
      } else {
        this.cpuSource = []
      }
    } else {
      const _params: any = {
        start: dayjs(fromTime).valueOf() / 1000,
        end: dayjs(toTime).valueOf() / 1000,
        interval,
        query: {
          A: {
            aggs: 'avg',
            by: [],
            types: [],
            metric: 'kubernetes.cpu.usage.pct',
            // metric: 'kubernetes.cpu.requests',
            // metric: 'kubernetes.cpu.limits',
            from: [
              {
                left: 'pod_name',
                operator: '=',
                right: name,
                connector: 'AND',
              }
            ]
          },
          expr: 'A'
        }
      }
      const usageRst = await toAsyncWait(MetricApi.getMetricChart(_params))
      const usageData = (usageRst.result ? usageRst.result.data : {}) || {}
      const usageItem = usageData[0] || {}
      const usageValues: any[] = usageItem.values || []
      _params.query.A.metric = 'kubernetes.cpu.requests';
      const requestRst = await toAsyncWait(MetricApi.getMetricChart(_params))
      const requestData = (requestRst.result ? requestRst.result.data : {}) || {}
      const requestItem = requestData[0] || {}
      const requestValues: any[] = requestItem.values || []
      _params.query.A.metric = 'kubernetes.cpu.limits';
      const limitRst = await toAsyncWait(MetricApi.getMetricChart(_params))
      const limitData = (limitRst.result ? limitRst.result.data : {}) || {}
      const limitItem = limitData[0] || {}
      const limitValues: any[] = limitItem.values || []
      if (usageValues.length || requestValues.length || limitValues.length) {
        this.cpuSource = [{
          name: i18n.t('modules.views.appMonitor.relationMap.s_7054bc34') as string, nameKey: 'modules.views.appMonitor.relationMap.s_7054bc34',
          unit: 'percent',
          area: true,
          data: usageValues.map(([key, ...value]: any) => {
            value = value[0]
            return {
              key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
              value,
            }
          }),
        }, {
          name: i18n.t('modules.views.appMonitor.relationMap.s_deb33f17') as string, nameKey: 'modules.views.appMonitor.relationMap.s_deb33f17',
          unit: '',
          area: true,
          data: requestValues.map(([key, ...value]: any) => {
            value = value[0]
            return {
              key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
              value,
            }
          }),
        }, {
          name: i18n.t('modules.views.appMonitor.relationMap.s_151c4676') as string, nameKey: 'modules.views.appMonitor.relationMap.s_151c4676',
          unit: '',
          area: true,
          data: limitValues.map(([key, ...value]: any) => {
            value = value[0]
            return {
              key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
              value,
            }
          }),
        }]
      } else {
        this.cpuSource = []
      }
    }
    this.loading.cpu = false;
  }

  // 获取内存使用率
  private async getMemSource () {
    const { baseType, name, id } = this.current
    const { fromTime, toTime, interval } = this.queryParams
    if (!this.isPod) {
      const _params: any = {
        start: dayjs(fromTime).valueOf() / 1000,
        end: dayjs(toTime).valueOf() / 1000,
        interval,
        query: {
          A: {
            aggs: 'avg',
            by: [],
            types: [],
            metric: '',
            from: [
              {
                left: '',
                operator: '=',
                right: '',
                connector: 'AND',
              }
            ]
          }
        }
      }
      switch (baseType) {
        case 'process':
          _params.query.A.metric = 'process.mem.usage.pct'
          _params.query.A.from[0].left = 'pname'
          _params.query.A.from[0].right = name
          break;
        case 'container':
          _params.query.A.metric = 'docker.mem.in_use'
          _params.query.A.from[0].left = 'container_id'
          _params.query.A.from[0].right = id
          break;
        case 'host':
          _params.query.A.metric = 'system.mem.usage'
          _params.query.A.from[0].left = 'host'
          _params.query.A.from[0].right = name
          break;
      }
      this.loading.mem = true;
      const metricRst = await toAsyncWait(MetricApi.getMetricChart(_params))
      const metricData = (metricRst.result ? metricRst.result.data : {}) || {}
      const item = metricData[0] || {}
      const values: any[] = item.values || []
      if (values.length) {
        this.memSource = [{
          name: i18n.t('modules.views.appMonitor.relationMap.s_31cb8d97') as string, nameKey: 'modules.views.appMonitor.relationMap.s_31cb8d97',
          unit: 'percent',
          area: true,
          data: values.map(([key, ...value]: any) => {
            value = value[0]
            return {
              key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
              value,
            }
          }),
        }]
      } else {
        this.memSource = []
      }
    } else {
      const _params: any = {
        start: dayjs(fromTime).valueOf() / 1000,
        end: dayjs(toTime).valueOf() / 1000,
        interval,
        query: {
          A: {
            aggs: 'avg',
            by: [],
            types: [],
            metric: 'kubernetes.memory.usage.pct',
            // metric: 'kubernetes.memory.requests',
            // metric: 'kubernetes.memory.limits',
            from: [
              {
                left: 'pod_name',
                operator: '=',
                right: name,
                connector: 'AND',
              }
            ]
          },
          expr: 'A'
        }
      }
      const usageRst = await toAsyncWait(MetricApi.getMetricChart(_params))
      const usageData = (usageRst.result ? usageRst.result.data : {}) || {}
      const usageItem = usageData[0] || {}
      const usageValues: any[] = usageItem.values || []
      _params.query.A.metric = 'kubernetes.memory.requests';
      const requestRst = await toAsyncWait(MetricApi.getMetricChart(_params))
      const requestData = (requestRst.result ? requestRst.result.data : {}) || {}
      const requestItem = requestData[0] || {}
      const requestValues: any[] = requestItem.values || []
      _params.query.A.metric = 'kubernetes.memory.limits';
      const limitRst = await toAsyncWait(MetricApi.getMetricChart(_params))
      const limitData = (limitRst.result ? limitRst.result.data : {}) || {}
      const limitItem = limitData[0] || {}
      const limitValues: any[] = limitItem.values || []
      if (usageValues.length || requestValues.length || limitValues.length) {
        this.memSource = [{
          name: i18n.t('modules.views.appMonitor.relationMap.s_31cb8d97') as string, nameKey: 'modules.views.appMonitor.relationMap.s_31cb8d97',
          unit: 'percent',
          area: true,
          data: usageValues.map(([key, ...value]: any) => {
            value = value[0]
            return {
              key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
              value,
            }
          }),
        }, {
          name: i18n.t('modules.views.appMonitor.relationMap.s_ba7d4781') as string, nameKey: 'modules.views.appMonitor.relationMap.s_ba7d4781',
          unit: '',
          area: true,
          data: requestValues.map(([key, ...value]: any) => {
            value = value[0]
            return {
              key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
              value,
            }
          }),
        }, {
          name: i18n.t('modules.views.appMonitor.relationMap.s_7cdcc72a') as string, nameKey: 'modules.views.appMonitor.relationMap.s_7cdcc72a',
          unit: '',
          area: true,
          data: limitValues.map(([key, ...value]: any) => {
            value = value[0]
            return {
              key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
              value,
            }
          }),
        }]
      } else {
        this.memSource = []
      }
    }
    this.loading.mem = false;
  }

  // 获取请求数
  private async getDiskSource () {
    const { baseType, name } = this.current
    if (baseType !== 'host') {
      return
    }
    const { fromTime, toTime, interval } = this.queryParams
    const _params: any = {
      start: dayjs(fromTime).valueOf() / 1000,
      end: dayjs(toTime).valueOf() / 1000,
      interval,
      query: {
        A: {
          aggs: 'avg',
          by: [],
          types: [],
          metric: 'system.disk.pct_used',
          from: [
            {
              left: 'host',
              operator: '=',
              right: name,
              connector: 'AND',
            }
          ]
        }
      }
    }
    this.loading.disk = true
    const metricRst = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData = (metricRst.result ? metricRst.result.data : {}) || {}
    const item = metricData[0] || {}
    const values: any[] = item.values || []
    if (values.length) {
      this.diskSource = [{
        name: i18n.t('modules.views.appMonitor.relationMap.s_2c8a6d17') as string, nameKey: 'modules.views.appMonitor.relationMap.s_2c8a6d17',
        unit: 'percent',
        area: true,
        data: values.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      }]
    } else {
      this.diskSource = []
    }
    this.loading.disk = false;
  }

  // 获取错误数
  private async getIoSource () {
    const { baseType, name } = this.current
    if (baseType !== 'host') {
      return
    }
    const { fromTime, toTime, interval } = this.queryParams
    const _params: any = {
      start: dayjs(fromTime).valueOf() / 1000,
      end: dayjs(toTime).valueOf() / 1000,
      interval,
      query: {
        A: {
          aggs: 'avg',
          by: [],
          types: [],
          metric: 'system.io.wkb_s',
          from: [
            {
              left: 'host',
              operator: '=',
              right: name,
              connector: 'AND',
            }
          ]
        }
      }
    }
    this.loading.io = true
    const metricRst1 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData1 = (metricRst1.result ? metricRst1.result.data : {}) || {}
    const itemSend = metricData1[0] || {}
    const sendValues: any[] = itemSend.values || []
    _params.query.A.metric = 'system.io.rkb_s'
    const metricRst2 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData2 = (metricRst2.result ? metricRst2.result.data : {}) || {}
    const itemRcvd = metricData2[0] || {}
    const rcvdValues: any[] = itemRcvd.values || []
    if (sendValues.length || rcvdValues.length) {
      this.ioSource = [{
        name: i18n.t('modules.views.appMonitor.relationMap.s_ea22704d') as string, nameKey: 'modules.views.appMonitor.relationMap.s_ea22704d',
        unit: 'kibibytes/second',
        area: true,
        data: rcvdValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      },
      {
        name: i18n.t('modules.views.appMonitor.relationMap.s_085cf12e') as string, nameKey: 'modules.views.appMonitor.relationMap.s_085cf12e',
        unit: 'kibibytes/second',
        area: true,
        data: sendValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      }]
    } else {
      this.ioSource = []
    }
    this.loading.io = false;
  }

  // 获取请求数
  private async getHostNetSource () {
    const { baseType, name, id } = this.current
    const { fromTime, toTime, interval } = this.queryParams
    const _params: any = {
      start: dayjs(fromTime).valueOf() / 1000,
      end: dayjs(toTime).valueOf() / 1000,
      interval,
      query: {
        A: {
          aggs: 'sum',
          by: [],
          types: [],
          metric: 'system.net.bytes_sent',
          from: [
            {
              left: 'host',
              operator: '=',
              right: name,
              connector: 'AND',
            }
          ]
        }
      }
    }
    this.loading.net = true
    const metricRst1 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData1 = (metricRst1.result ? metricRst1.result.data : {}) || {}
    const itemSend = metricData1[0] || {}
    const sendValues: any[] = itemSend.values || []
    _params.query.A.metric = 'system.net.bytes_rcvd';
    const metricRst2 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData2 = (metricRst2.result ? metricRst2.result.data : {}) || {}
    const itemRcvd = metricData2[0] || {}
    const rcvdValues: any[] = itemRcvd.values || []
    if (sendValues.length || rcvdValues.length) {
      this.netSource = [{
        name: i18n.t('modules.views.appMonitor.relationMap.s_80c86063') as string, nameKey: 'modules.views.appMonitor.relationMap.s_80c86063',
        unit: 'bytes',
        area: true,
        data: rcvdValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      },
      {
        name: i18n.t('modules.views.appMonitor.relationMap.s_97ecc1bb') as string, nameKey: 'modules.views.appMonitor.relationMap.s_97ecc1bb',
        unit: 'bytes',
        area: true,
        data: sendValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      }]
    } else {
      this.netSource = []
    }
    this.loading.net = false;
  }
  // 获取请求数
  private async getPodNetSource () {
    const { baseType, name, id } = this.current
    // if (baseType === 'process') {
    //   return
    // }
    const { fromTime, toTime, interval } = this.queryParams
    const _params: any = {
      start: dayjs(fromTime).valueOf() / 1000,
      end: dayjs(toTime).valueOf() / 1000,
      interval,
      query: {
        A: {
          aggs: 'avg',
          by: [],
          types: [],
          metric: 'kubernetes.network.rx_bytes',
          from: [
            {
              left: 'pod_name',
              operator: '=',
              right: name,
              connector: 'AND',
            }
          ]
        }
      }
    }
    this.loading.net = true
    const metricRst1 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData1 = (metricRst1.result ? metricRst1.result.data : {}) || {}
    const itemSend = metricData1[0] || {}
    const sendValues: any[] = itemSend.values || []
    _params.query.A.metric = 'kubernetes.network.tx_bytes';
    const metricRst2 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData2 = (metricRst2.result ? metricRst2.result.data : {}) || {}
    const itemRcvd = metricData2[0] || {}
    const rcvdValues: any[] = itemRcvd.values || []
    if (sendValues.length || rcvdValues.length) {
      this.netSource = [{
        name: i18n.t('modules.views.appMonitor.relationMap.s_80c86063') as string, nameKey: 'modules.views.appMonitor.relationMap.s_80c86063',
        unit: 'bytes',
        area: true,
        data: rcvdValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      },
      {
        name: i18n.t('modules.views.appMonitor.relationMap.s_97ecc1bb') as string, nameKey: 'modules.views.appMonitor.relationMap.s_97ecc1bb',
        unit: 'bytes',
        area: true,
        data: sendValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      }]
    } else {
      this.netSource = []
    }
    this.loading.net = false;
  }
  // 获取请求数
  private async getProcessNetSource () {
    const { baseType, name, id } = this.current
    // if (baseType === 'process') {
    //   return
    // }
    const { fromTime, toTime, interval } = this.queryParams
    const _params: any = {
      start: dayjs(fromTime).valueOf() / 1000,
      end: dayjs(toTime).valueOf() / 1000,
      interval,
      query: {
        A: {
          aggs: 'avg',
          by: [],
          types: [],
          metric: 'process.net.bytes_rcvd',
          from: [
            {
              left: 'pname',
              operator: '=',
              right: name,
              connector: 'AND',
            }
          ]
        }
      }
    }
    this.loading.net = true
    const metricRst1 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData1 = (metricRst1.result ? metricRst1.result.data : {}) || {}
    const itemSend = metricData1[0] || {}
    const sendValues: any[] = itemSend.values || []
    _params.query.A.metric = 'process.net.bytes_sent';
    const metricRst2 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData2 = (metricRst2.result ? metricRst2.result.data : {}) || {}
    const itemRcvd = metricData2[0] || {}
    const rcvdValues: any[] = itemRcvd.values || []
    if (sendValues.length || rcvdValues.length) {
      this.netSource = [{
        name: i18n.t('modules.views.appMonitor.relationMap.s_80c86063') as string, nameKey: 'modules.views.appMonitor.relationMap.s_80c86063',
        unit: 'bytes',
        area: true,
        data: rcvdValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      },
      {
        name: i18n.t('modules.views.appMonitor.relationMap.s_97ecc1bb') as string, nameKey: 'modules.views.appMonitor.relationMap.s_97ecc1bb',
        unit: 'bytes',
        area: true,
        data: sendValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      }]
    } else {
      this.netSource = []
    }
    this.loading.net = false;
  }
  // 获取请求数
  private async getPodThroughSource () {
    const { baseType, name, id } = this.current
    // if (baseType === 'process') {
    //   return
    // }
    const { fromTime, toTime, interval } = this.queryParams
    const _params: any = {
      start: dayjs(fromTime).valueOf() / 1000,
      end: dayjs(toTime).valueOf() / 1000,
      interval,
      query: {
        A: {
          aggs: 'avg',
          by: [],
          types: [],
          metric: 'process.net.bytes_rcvd',
          from: [
            {
              left: 'pname',
              operator: '=',
              right: name,
              connector: 'AND',
            }
          ]
        }
      }
    }
    this.loading.through = true
    const metricRst1 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData1 = (metricRst1.result ? metricRst1.result.data : {}) || {}
    const itemSend = metricData1[0] || {}
    const sendValues: any[] = itemSend.values || []
    _params.query.A.metric = 'process.net.bytes_sent';
    const metricRst2 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData2 = (metricRst2.result ? metricRst2.result.data : {}) || {}
    const itemRcvd = metricData2[0] || {}
    const rcvdValues: any[] = itemRcvd.values || []
    if (sendValues.length || rcvdValues.length) {
      this.thorughSource = [{
        name: i18n.t('modules.views.appMonitor.relationMap.s_80c86063') as string, nameKey: 'modules.views.appMonitor.relationMap.s_80c86063',
        unit: 'bytes',
        area: true,
        data: rcvdValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      },
      {
        name: i18n.t('modules.views.appMonitor.relationMap.s_97ecc1bb') as string, nameKey: 'modules.views.appMonitor.relationMap.s_97ecc1bb',
        unit: 'bytes',
        area: true,
        data: sendValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      }]
    } else {
      this.thorughSource = []
    }
    this.loading.through = false;
  }
  // 获取请求数
  private async getProcessThroughSource () {
    const { baseType, name, id } = this.current
    // if (baseType === 'process') {
    //   return
    // }
    const { fromTime, toTime, interval } = this.queryParams
    const _params: any = {
      start: dayjs(fromTime).valueOf() / 1000,
      end: dayjs(toTime).valueOf() / 1000,
      interval,
      query: {
        A: {
          aggs: 'avg',
          by: [],
          types: [],
          metric: 'process.net.packets_in.count',
          from: [
            {
              left: 'pname',
              operator: '=',
              right: name,
              connector: 'AND',
            }
          ]
        }
      }
    }
    this.loading.through = true
    const metricRst1 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData1 = (metricRst1.result ? metricRst1.result.data : {}) || {}
    const itemSend = metricData1[0] || {}
    const sendValues: any[] = itemSend.values || []
    _params.query.A.metric = 'process.net.packets_out.count';
    const metricRst2 = await toAsyncWait(MetricApi.getMetricChart(_params))
    const metricData2 = (metricRst2.result ? metricRst2.result.data : {}) || {}
    const itemRcvd = metricData2[0] || {}
    const rcvdValues: any[] = itemRcvd.values || []
    if (sendValues.length || rcvdValues.length) {
      this.thorughSource = [{
        name: i18n.t('modules.views.appMonitor.relationMap.s_0afe2797') as string, nameKey: 'modules.views.appMonitor.relationMap.s_0afe2797',
        unit: 'bytes',
        area: true,
        data: rcvdValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      },
      {
        name: i18n.t('modules.views.appMonitor.relationMap.s_cafba939') as string, nameKey: 'modules.views.appMonitor.relationMap.s_cafba939',
        unit: 'bytes',
        area: true,
        data: sendValues.map(([key, ...value]: any) => {
          value = value[0]
          return {
            key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
            value,
          }
        }),
      }]
    } else {
      this.thorughSource = []
    }
    this.loading.through = false;
  }


  // 跳转至服务详情
  private viewServiceDetailHandle () {
    let path = ''
    const query: any = {}
    const { baseType, name, hostName, _clusterId } = this.current
    switch (baseType) {
      case 'pod':
        path = '/infrastructure/podDetail'
        query.pn = encodeURIComponent(name)
        query.cid = encodeURIComponent(_clusterId)
        break;
      case 'process':
        path = '/infrastructure/processDetail'
        query.processName = encodeURIComponent(name)
        query.hostName = encodeURIComponent(hostName)
        break;
      case 'container':
        path = '/infrastructure/dockerDetail'
        query.containerId = encodeURIComponent(this.current.containerId || '')
        break;
      case 'host':
        path = '/infrastructure/hostDetail'
        query.hostName = encodeURIComponent(name)
        break;
    }
    this.$router.push({
      path,
      query
    })
  }
}
</script>

<style lang="scss" scoped>
.node-detail-wrap {
  height: 100%;

  .node-detail {
    width: 310px;
    height: 100%;
    display: flex;
    flex-direction: column;
    background: #1B1B1B;
    box-shadow: -4px 0px 6px 0px rgba(18, 18, 18, 0.51);
    color: #EBEBED;
  }
}

.detail-header {
  padding: 7px 30px 7px 16px;
  border-bottom: 1px solid #272727;

  .title {
    font-size: 14px;
    line-height: 24px;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    .db-icon {
      display: inline-block;
      vertical-align: middle;
    }
    .title-link:hover {
      color: var(--color-text-link);
    }
  }
}

.detail-content {
  padding: 0 15px 20px;
  flex: 1;
  overflow: auto;
}

.detail-alarm {
  .alarm-head {
    margin-bottom: 14px;
    padding: 0 6px;
    line-height: 22px;
    font-size: 12px;
    .title {
      width: 130px;
      padding-right: 15px;
    }
    .count {
      margin-left: 6px;
      padding: 0 4px;
      background: #E12828;
      font-size: 12px;
      line-height: 14px;
      border-radius: 10px;
    }
    .status-tab {
      color: #777A7E;
      font-size: 13px;
    }
    .status-t {
      &.active {
        color: #EBEBED;
      }
      & + .status-t {
        margin-left: 13px;
        position: relative;
        &::before {
          content: '';
          margin-top: -5px;
          width: 1px;
          height: 10px;
          background: #414141;
          position: absolute;
          top: 50%;
          left: -7px;
        }
      }
    }
    &.w-auto {
      justify-content: space-between;
      .title {
        width: auto;
      }
    }
  }
  .alarm-list {
    padding: 0 6px;
    min-height: 90px;
  }
  .alarm-item {
    padding-left: 8px;
    position: relative;
    font-size: 12px;
    line-height: 16px;
    & + .alarm-item {
      margin-top: 14px;
    }
    .alarm-status {
      width: 2px;
      height: 28px;
      border-radius: 5px;
      position: absolute;
      top: 2px;
      left: 0;
      background-color: #B5B7BB;
      &[data-status="3"] {
        background-color: #E12828;
      }
      &[data-status="2"] {
        background-color: #F79532;
      }
    }
    .time {
      transform-origin: center left;
      color: #777A7E;
    }
    .name {
      padding-right: 70px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      cursor: pointer;
      &:hover {
        color: var(--color-text-link);
      }
    }
    .state {
      font-size: 12px;
      color: #777A7E;
      position: absolute;
      top: 0;
      right: 0;
    }
    &.finished-item {
      opacity: 0.5;
    }
  }
  .empty {
    height: 90px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #777A7E;
  }
  .more {
    margin-top: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    line-height: 18px;
    color: #777A7E;
    span {
      cursor: pointer;
      &:hover {
        color: var(--color-text-link);
      }
    }
  }
}

.chart-wrap {
  margin-top: 10px;
  & + .chart-wrap {
    margin-top: 10px;
  }
  .chart-title {
    margin: 0 16px;
    font-size: 13px;
    line-height: 16px;
  }
  .chart-cont {
    height: 140px;
  }
}
.font-12 {
  font-size: 12px;
}
.font-13 {
  font-size: 13px;
}
.detail-content {
  :deep(.el-collapse-item__content) {
    padding-bottom: 16px;
  }
  :deep(.el-collapse),
  :deep(.el-collapse-item__header),
  :deep(.el-collapse-item__wrap) {
    border: none;
  }
}
:root[data-theme=light] .node-detail-wrap {
  .node-detail {
    background: #FFFFFF;
    box-shadow: -4px 0px 6px 0px rgba(119, 122, 126, 0.06);
    color: #121317;
  }
  .detail-header {
    border-bottom-color: #EEEFF1;
  }

  .detail-alarm {
    .alarm-head {
      .count {
        color: #FFFFFF;
      }
      .status-tab {
        color: #8B8E93;
      }
      .status-t {
        &.active {
          color: #121317;
        }
        & + .status-t::before {
          background: #E7E8E8;
        }
      }
    }
    .alarm-item .state,
    .empty {
      color: #A3A5A8;
    }
  }
}
@media screen and (min-width: 1500px) {
  .node-detail-wrap  .node-detail {
    width: 350px;
  }
}
@media screen and (min-width: 1900px) {
  .node-detail-wrap  .node-detail {
    width: 390px;
  }
}
</style>
