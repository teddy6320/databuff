<template>
  <div class="baseinfo-cont">
    <div class="baseinfo-wrapper">
      
      <div class="chart-group">
        <div v-for='value,key in chartGroup' :key='key' class="chart-item chart-item-50 br-4">
          <h3 class="fw-normal font-14 chart-title">
            <span class="mr-10">{{ value.titleKey ? $t(value.titleKey) : value.title }}</span>
            <scroll-select
              v-show='value.poolList.length'
              v-model="value.poolActive"
              @change="typeChangeHandle(key)"
              :options="value.poolList"
              :clearable="false"
              :showTitle="true"
              size="mini"
              class="item-select" />
          </h3>
          <div v-if='value.poolList.length' @click="viewMoreHandle(key)" class="view-more-btn db-blue cphu font-12">
            <span class="font-12">{{ $t('modules.views.appMonitor.relationMap.s_90ef7c48') }}</span>
            <i class="el-icon el-icon-arrow-right"></i>
          </div>
          <basic-chart
            :showEmpty="!value.loading && !value.source.length"
            :key='key'
            :colors='value.colors'
            :showLegend='true'
            :compactGrid="true"
            :textSmallMode="true"
            :minInterval="1"
            :min="0"
            group='threadpool'
            :yAxisSplitNum="3"
            :interval="timeParams.interval"
            :source='value.source'></basic-chart>
        </div>
      </div>

    </div>

  </div>
</template>
<script lang='ts'>
import { toAsyncWait } from '@/utils/common';
import i18n from '@/i18n';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import ServiceApi from '@/api/service';
import dayjs from 'dayjs';
import MetricApi from '@/api/metric';
import deepClone from 'lodash/cloneDeep';

type PanelType = 'thread' | 'object' | 'httpConn' | 'dbConn';


@Component({
  components: {},
})
export default class TabThreadpool extends Vue {
  @Prop({ default: {} }) private current!: any;

  @Watch('current', { immediate: true })
  private onCurrentChange (val: any, oldVal: any) {
    if (val && val?.serviceId !== oldVal?.serviceId && this.isMounted) {
      this.fetchAllData();
    }
  }

  private isMounted = false;
  private showCharts = false;
  private metricLoading = true;
  private typeMetrics: any = {};

  private timeParams = {
    fromTime: Math.floor(+new Date() / 1000),
    toTime: Math.floor(+new Date() / 1000),
    interval: 60,
  }

  private chartGroup: any = {
    thread: {
      title: i18n.t('modules.views.appMonitor.serviceDetail.s_46dc5c2e') as string, titleKey: 'modules.views.appMonitor.serviceDetail.s_46dc5c2e',
      metrics: [
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_38c2dac6') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_38c2dac6', value: 'service.thread.pool.corePoolSize' },
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_0a144ec2') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_0a144ec2', value: 'service.thread.pool.maximumPoolSize' },
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_70500caa') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_70500caa', value: 'service.thread.pool.poolSize' },
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_b4804b35') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_b4804b35', value: 'service.thread.pool.activeCount' },
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_d2da477c') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_d2da477c', value: 'service.thread.pool.queueSize' },
      ],
      source: [],
      loading: true,
      poolList: [],
      poolActive: '',
      poolMetric: 'service.thread.pool.maximumPoolSize',
      poolNameTag: 'threadPoolName',
    },
    object: {
      title: i18n.t('modules.views.appMonitor.objectPool.s_be3f9ead') as string, titleKey: 'modules.views.appMonitor.objectPool.s_be3f9ead',
      metrics: [
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_f9136bfa') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_f9136bfa', value: 'service.object.pool.activeSize' },
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_50c0ed0d') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_50c0ed0d', value: 'service.object.pool.maxSize' },
      ],
      source: [],
      loading: true,
      poolList: [],
      poolActive: '',
      poolMetric: 'service.object.pool.maxSize',
      poolNameTag: 'objectPoolName',
    },
    httpConn: {
      title: i18n.t('modules.views.appMonitor.httpConnPool.s_a7ae49ed') as string, titleKey: 'modules.views.appMonitor.httpConnPool.s_a7ae49ed',
      metrics: [
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_8914ac3b') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_8914ac3b', value: 'service.http.connection.pool.maxSize' },
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_2a934932') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_2a934932', value: 'service.http.connection.pool.activeSize' },
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_837fa33c') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_837fa33c', value: 'service.http.connection.pool.currentSize' },
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_726cbbca') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_726cbbca', value: 'service.http.connection.pool.queueSize' },
      ],
      source: [],
      loading: true,
      poolList: [],
      poolActive: '',
      poolMetric: 'service.http.connection.pool.maxSize',
      poolNameTag: 'httpConnectionPoolName',
    },
    dbConn: {
      title: i18n.t('modules.views.appMonitor.dbConnPool.s_a70c4620') as string, titleKey: 'modules.views.appMonitor.dbConnPool.s_a70c4620',
      metrics: [
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_8914ac3b') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_8914ac3b', value: 'service.db.connection.pool.maxSize' },
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_2a934932') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_2a934932', value: 'service.db.connection.pool.activeSize' },
        { label: i18n.t('modules.views.appMonitor.serviceDetail.s_b666bd9a') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_b666bd9a', value: 'service.db.connection.pool.idleSize' },
      ],
      source: [],
      loading: true,
      poolList: [],
      poolActive: '',
      poolMetric: 'service.db.connection.pool.maxSize',
      poolNameTag: 'connectionPoolName',
    },
  }

  get isLoading () {
    const chartLoading = !!(Object.values(this.chartGroup).find((i: any) => i.loading));
    return chartLoading
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
    this.fetchTopData();
  }

  private resetTimeParams () {
    const { fromTime, toTime, interval } = this.getGlobalTime();
    this.timeParams = {
      fromTime: Math.floor(fromTime.valueOf() / 1000),
      toTime: Math.floor(toTime.valueOf() / 1000),
      interval
    };
  }

  private fetchTopData () {
    this.typeChangeHandle('thread');
    this.typeChangeHandle('object');
    this.typeChangeHandle('httpConn');
    this.typeChangeHandle('dbConn');
  }

  private typeChangeHandle (key: PanelType) {
    const chartItem = this.chartGroup[key];
    if (chartItem.poolList.length) {
      this.getchartGroup(key);
    } else {
      this.getPoolList(key).then((data: any) => {
        if (data.length) {
          this.getchartGroup(key);
        } else {
          chartItem.loading = false;
          chartItem.source = [];
        }
      })
    }
  }

  private getchartGroup (type: PanelType) {
    const chartItem = this.chartGroup[type];
    const params: any = {
      start: this.timeParams.fromTime,
      end: this.timeParams.toTime,
      interval: this.timeParams.interval,
      query: { expr: 'A', A: {
        metric: '', aggs: '', by: [], types: [],
        from: [
          { left: 'serviceId', operator: '=', right: this.current?.serviceId, connector: 'AND' },
          { left: 'serviceInstance', operator: '=', right: this.current?.serviceInstance, connector: 'AND' },
        ],
      }},
    }
    const _query = params.query.A
    
    if (chartItem.poolActive) {
      _query.from.push({ left: chartItem.poolNameTag, operator: '=', right: chartItem.poolActive, connector: 'AND' });
    }
    chartItem.source = [];
    chartItem.loading = true;
    Promise.allSettled(chartItem.metrics.map((t: any) => {
      _query.metric = t.value
      return toAsyncWait(MetricApi.getMetricChart(deepClone(params)))
    })).then((rstList) => {
      const resultList = rstList.map((t: any) => (t.value || {}).result || {}).map((t: any) => (t.data || [])[0] || {})
      resultList.forEach((item: any, idx: number) => {
        if ((item.values || []).length) {
          const metricItem = chartItem.metrics[idx]
          chartItem.source.push({
            name: metricItem.label,
            unit: (item.units || [])[1] || '',
            data: item.values.map(([key, value]: any) => ({
              key: dayjs(Number(key) * 1000).format('YYYY-MM-DD HH:mm'),
              value,
            })),
          })
        }
      })
    }).finally(() => {
      chartItem.loading = false
    })
  }

  // 获取池子列表
  private async getPoolList (type: PanelType) {
    const chartItem = this.chartGroup[type];
    const params = {
      start: this.timeParams.fromTime * 1000,
      end: this.timeParams.toTime * 1000,
      metrics: [chartItem.poolMetric],
      by: [chartItem.poolNameTag],
      from: [{ left: 'serviceId', operator: '=', right: this.current?.serviceId, connector: 'AND' }],
    }
    const { result, error } = await toAsyncWait(MetricApi.getMetricLastTags(params))
    if (!error) {
      const poolNames: string[] = (result.data || {})[chartItem.poolNameTag] || [];
      chartItem.poolList = poolNames.filter(t => !!t).map((t: any) => ({ label: t, value: t })) as any;
      if (chartItem.poolList.length) {
        if (!chartItem.poolList.find((t: any) => t.value === chartItem.poolActive)) {
          chartItem.poolActive = chartItem.poolList[0].value
        }
      } else {
        chartItem.poolActive = ''
      }
    }
    return chartItem.poolList
  }

  private viewMoreHandle (key: PanelType) {
    const chartItem = this.chartGroup[key];
    const { sn, sid, si } = this.$route.query
    const query: any = {
      ...this.getRouteTimeOrRange,
      sn, sid,
    }
    if (si) {
      query.si = si
    }
    if (chartItem.poolActive) {
      query.poolName = chartItem.poolActive
    }
    let path = '';
    switch (key) {
      case 'thread':
        path = '/appMonitor/threadPool'
        break;
      case 'object':
        path = '/appMonitor/objectPool'
        break;
      case 'httpConn':
        path = '/appMonitor/httpConnPool'
        break;
      case 'dbConn':
        path = '/appMonitor/dbConnPool'
        break;
    }
    this.$router.push({ path, query });
  }

}
</script>
<style lang='scss' scoped>
.baseinfo-cont {
  overflow: hidden;
  position: relative;

}
.chart-group {
  display: flex;
  flex-wrap: wrap;
  overflow: hidden;

  .chart-item {
    flex: 0 0 auto;
    height: 208px;
    overflow: hidden;
    margin: 8px 4px;
    padding: 40px 15px 15px;
    border: 1px solid var(--border-color-base);
    position: relative;

    &.chart-item-33 {
      width: calc( 33.33% - 16px );
      &:not(:last-child) {
        margin-right: 12px;
      }
    }
    &.chart-item-50 {
      width: calc( 50% - 16px );
      &:nth-child(odd) {
        margin-right: 12px;
      }
    }

    
  }
  .chart-title {
    height: 30px;
    position: absolute;
    top: 7px;
    left: 15px;
    line-height: 1;
    margin: 0;
    display: flex;
    align-items: center;
  }
  .view-more-btn {
    position: absolute;
    top: 15px;
    right: 15px;
    line-height: 1;
  }
}
</style>