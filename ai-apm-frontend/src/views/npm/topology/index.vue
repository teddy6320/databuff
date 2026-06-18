<template>
  <div class="topology-wrapper">
    <search-group
      ref="searchGroup"
      :metricList="metricList"
      @on-change="searchChangeHandle" />

    <div class="body">
      <network-chart
        ref="networkChart"
        :metricList="chartMetricList"
        :query="queryParams"
        :queryLoading="queryLoading" />
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Getter } from 'vuex-class';
import dayjs from 'dayjs';
import SearchGroup from './search-group.vue';
import NetworkChart from './network-chart.vue';

@Component({
  components: {
    SearchGroup,
    NetworkChart,
  }
})
export default class Topology extends Vue {
  @Getter('globalTime') private globalTimeFunc!: any;
  @Getter('globalTimeInited') private globalTimeInited!: boolean;

  public $refs!: {
    searchGroup: SearchGroup
    networkChart: NetworkChart
  }

  get globalTime () {
    return this.globalTimeFunc()
  }
  @Watch('globalTime', { deep: true })
  private watchGlobalTime() {
    if (!this.globalTimeInited) {
      return
    }
    this.durationChangeHandle()
  }

  private metricList = [
    {
      label: 'Volume',
      options: [
        { label: i18n.t('modules.views.npm.topology.s_991cdf4f') as string, labelKey: 'modules.views.npm.topology.s_991cdf4f', value: 'npm.volume_sent', unit: 'bytes', agg: 'sum', type: 'Volume & TCP' },
        { label: i18n.t('modules.views.npm.topology.s_093ff2e2') as string, labelKey: 'modules.views.npm.topology.s_093ff2e2', value: 'npm.volume_rcvd', unit: 'bytes', agg: 'sum', type: 'Volume & TCP' },
      ]
    },
    {
      label: 'TCP',
      options: [
        { label: i18n.t('modules.views.npm.topology.s_e875e779') as string, labelKey: 'modules.views.npm.topology.s_e875e779', value: 'npm.tcp_retransmit', unit: 'requests', agg: 'sum', type: 'Volume & TCP' },
        { label: i18n.t('modules.views.npm.topology.s_11cd7047') as string, labelKey: 'modules.views.npm.topology.s_11cd7047', value: 'npm.tcp_latency', unit: 'µs', agg: 'avg', type: 'Volume & TCP' },
        { label: i18n.t('modules.views.npm.topology.s_757763d0') as string, labelKey: 'modules.views.npm.topology.s_757763d0', value: 'npm.tcp_jitter', unit: 'µs', agg: 'avg', type: 'Volume & TCP' },
        { label: i18n.t('modules.views.npm.topology.s_8f0d095d') as string, labelKey: 'modules.views.npm.topology.s_8f0d095d', value: 'npm.tcp.conns_established', unit: 'conns', agg: 'sum', type: 'Volume & TCP' },
        { label: i18n.t('modules.views.npm.topology.s_e39f6e22') as string, labelKey: 'modules.views.npm.topology.s_e39f6e22', value: 'npm.tcp.conns_closed', unit: 'conns', agg: 'sum', type: 'Volume & TCP' },
      ]
    },
    // {
    //   label: 'DNS',
    //   options: [
    //     { label: i18n.t('modules.views.npm.topology.s_678eed99') as string, labelKey: 'modules.views.npm.topology.s_678eed99', value: 'npm.dns.responses_failed', unit: 'requests', agg: 'sum', type: 'DNS' },
    //     { label: i18n.t('modules.views.npm.topology.s_b97eab8c') as string, labelKey: 'modules.views.npm.topology.s_b97eab8c', value: 'npm.dns.responses_succeded', unit: 'requests', agg: 'sum', type: 'DNS' },
    //     { label: i18n.t('modules.views.npm.topology.s_84a5ae7c') as string, labelKey: 'modules.views.npm.topology.s_84a5ae7c', value: 'npm.dns.timeouts', unit: 'requests', agg: 'sum', type: 'DNS' },
    //     { label: i18n.t('modules.views.npm.topology.s_49119337') as string, labelKey: 'modules.views.npm.topology.s_49119337', value: 'npm.dns.failures', unit: 'requests', agg: 'sum', type: 'DNS' },
    //     { label: i18n.t('modules.views.npm.topology.s_a08cda09') as string, labelKey: 'modules.views.npm.topology.s_a08cda09', value: 'npm.dns.requests', unit: 'requests', agg: 'sum', type: 'DNS' },
    //     { label: i18n.t('modules.views.npm.topology.s_ce94bb1f') as string, labelKey: 'modules.views.npm.topology.s_ce94bb1f', value: 'npm.dns.errors.nxdomain', unit: 'requests', agg: 'sum', type: 'DNS' },
    //     { label: i18n.t('modules.views.npm.topology.s_a450d206') as string, labelKey: 'modules.views.npm.topology.s_a450d206', value: 'npm.dns.errors.servfail', unit: 'requests', agg: 'sum', type: 'DNS' },
    //   ]
    // },
  ];
  get chartMetricList () {
    return this.metricList.map(t => t.options).flat()
  }

  private queryParams = {
    fromTime: '',
    toTime: '',
    dimension: '',
    metric: '',
    from: [],
  }
  private queryLoading = true

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
    this.$eventBus.$off('GlobalRefresh');
  }

  private durationChangeHandle () {
    this.regetGlobalTime()
    this.queryLoading = true;
    this.$nextTick(() => {
      this.$refs.searchGroup.init().then((data: any) => {
        this.queryParams = {
          ...this.queryParams,
          ...data,
        }
        this.$nextTick(() => {
          this.$refs.networkChart.getData()
        })
        this.queryLoading = false;
      }).catch(() => this.queryLoading = false)
    })
  }

  private regetGlobalTime () {
    const { fromTime, toTime } = this.globalTimeFunc()
    this.queryParams.fromTime = dayjs(fromTime).format('YYYY-MM-DD HH:mm:ss')
    this.queryParams.toTime = dayjs(toTime).format('YYYY-MM-DD HH:mm:ss')
  }

  private searchChangeHandle (data: any) {
    const params = {
      ...this.queryParams,
      ...data,
    }
    if (JSON.stringify(params) === JSON.stringify(this.queryParams)) {
      return
    }
    this.queryParams = params
    this.$nextTick(() => {
      this.$refs.networkChart.getData()
    })
  }
}
</script>

<style lang="scss" scoped>
.topology-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .body {
    margin-top: 16px;
    flex: 1;
    min-height: 400px;
    display: flex;
    background: var(--bg-color);
  }
}
</style>
