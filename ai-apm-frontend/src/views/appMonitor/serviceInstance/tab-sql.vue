<template>
  <div class="sql-cont">
    <div class="sql-wrapper flex-v">
      
      <div class="chart-group">
        <div v-for='value,key in chartGroup' :key='key' class="chart-item br-4">
          <h3 class="fw-normal font-14 chart-title">{{ key | chartTitleFilter }}</h3>
          <basic-chart
            :showEmpty="!chartGroup[key].loading && !chartGroup[key].source.length"
            :key='key'
            :colors='chartGroup[key].colors'
            :showLegend='true'
            :compactGrid="true"
            :textSmallMode="true"
            :minInterval="1"
            :min="0"
            group='sqlChart'
            :yAxisSplitNum="3"
            :interval="timeParams.interval"
            :source='chartGroup[key].source'></basic-chart>
        </div>
      </div>

      <div class="list-wrapper">
        <db-table
          :queryApi='queryApi'
          :queryParams='tableQueryParams'
          :offsetMode='true'
          :columnConfig='columnConfig'
          @on-table-inited='tableInitedHandle'
          @sort-change='tableInitedHandle'
          @on-fetch-end='onFetchEnd'
          tableKey='APM_SERVICEINSTANCE_DETAIL_SQL'
          ref='listTable'>
          <template slot="column-resource" slot-scope="{ row }">
            <div class="copy-box">
              {{ row.resource || '-' }}
              <i @click.stop="copyNameHandle(row.resource || '-')" class="db-icon-copy copy-btn"></i>
            </div>
          </template>
        </db-table>
      </div>
    </div>
  </div>
</template>
<script lang='ts'>
import { toAsyncWait } from '@/utils/common';
import i18n from '@/i18n';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import EllipseChart from '@/views/appMonitor/serviceDetail/bar-stack.vue';
import dayjs from 'dayjs';
import ApmApi from '@/api/apm';
import { copy } from '@/utils/common';

@Component({
  components: {
    EllipseChart,
  },
  filters: {
    chartTitleFilter (key: string) {
      switch (key) {
        case 'response':
          return i18n.t('modules.views.appMonitor.relationMap.s_207c26c9') as string;
        case 'error':
          return i18n.t('modules.views.appMonitor.cache.s_0c8524d7') as string;
        case 'request':
          return i18n.t('modules.views.appMonitor.cache.s_8bc42b53') as string;
        case 'io':
          return i18n.t('modules.views.appMonitor.serviceDetail.s_fb54ae94') as string;
      }
    }
  }
})
export default class TabSql extends Vue {
  @Prop({ default: {} }) private current!: any;

  @Watch('current', { immediate: true })
  private onCurrentChange (val: any, oldVal: any) {
    if (val && val?.serviceId !== oldVal?.serviceId && this.isMounted) {
      this.fetchAllData();
    }
  }

  public $refs!: {
    listTable: any;
  };

  private isMounted = false;
  private showCharts = false;
  private listLoading = false;

  private timeParams = {
    fromTime: '',
    toTime: '',
    interval: 60,
  }

  private chartGroup: any = {
    response: {
      loading: true,
      source: [],
      colors: ['#2962FF', '#00AFF4'],
    },
    request: {
      loading: true,
      source: [],
      colors: ['#2962FF', '#F37370'],
    },
    error: {
      loading: true,
      source: [],
      colors: ['#8DCFF8'],
    },
  }

  get tableQueryParams () {
    return {
      ...this.timeParams,
      isSlow: 1,
      isIn: 1,
      componentType: 'service.db',
      serviceId: this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid)),
    }
  }

  private queryApi = ApmApi.getSlowSqlTop;

  private columnConfig: any = [
    { field: 'resource', prop: 'resource', label: 'SQL', slot: 'column-resource', minWidth: 400 },
    { field: 'callCnt', prop: 'callCnt', label: i18n.t('modules.views.appMonitor.serviceDetail.s_00d5bdf1') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_00d5bdf1', unit: 'count', minWidth: 100, sortable: true, },
    { field: 'avgLatency', prop: 'avgLatency', label: i18n.t('modules.views.appMonitor.cache.s_96a0c062') as string, labelKey: 'modules.views.appMonitor.cache.s_96a0c062', unit: 'ns', minWidth: 120, sortable: true, defaultSort: 'desc' },
    { field: 'maxDuration', prop: 'maxDuration', label: i18n.t('modules.views.appMonitor.external.s_3bff553d') as string, labelKey: 'modules.views.appMonitor.external.s_3bff553d', unit: 'ns', minWidth: 120, sortable: true, },
    { field: 'minDuration', prop: 'minDuration', label: i18n.t('modules.views.appMonitor.response.s_9360c736') as string, labelKey: 'modules.views.appMonitor.response.s_9360c736', unit: 'ns', minWidth: 120, sortable: true, },
    { field: 'reqRate', prop: 'reqRate', label: i18n.t('modules.views.appMonitor.serviceDetail.s_bff31600') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_bff31600', unit: 'count', lessZeroOneKey: 'callCnt', minWidth: 130, sortable: true, },
    { field: 'srcServiceCnt', prop: 'srcServiceCnt', label: i18n.t('modules.views.appMonitor.serviceDetail.s_ef573ce9') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_ef573ce9', unit: 'count', minWidth: 110, sortable: true, },
  ];

  get isLoading () {
    const chartLoading = !!(Object.values(this.chartGroup).find((i: any) => i.loading));
    return this.listLoading || chartLoading;
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

  private resetTimeParams () {
    const { fromTime, toTime, interval } = this.getGlobalTimeV2();
    this.timeParams = { fromTime, toTime, interval };
  }

  private fetchAllData () {
    this.resetTimeParams();
    this.fetchResponseSource();
    this.fetchErrorSource();
    this.fetchRequestSource();
    this.tableInitedHandle();
  }

  // 响应时间
  private async fetchResponseSource () {
    const serviceId = this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid));
    const _params: any = {
      serviceId,
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      isIn: 1,
      isSlow: 1,
      componentType: 'service.db',
      graphStats: ['avgLatencys', 'maxLatencys', 'minLatencys']
    }
    this.chartGroup.response.loading = true;
    const multipleResult = await toAsyncWait(ApmApi.getServiceGraph({ ..._params }))
    const multipleData = multipleResult?.result?.data || {}
    if (!Object.keys(multipleData).length) {
      this.chartGroup.response.source = []
    } else {
      this.chartGroup.response.source = [{
        name: i18n.t('modules.views.appMonitor.cache.s_96a0c062') as string, nameKey: 'modules.views.appMonitor.cache.s_96a0c062',
        type: 'line',
        unit: 'ns',
        data: Object.entries(multipleData.avgLatencys).map(([timestamp, value]) => ({
          key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
          value,
        }))
      }, {
        name: i18n.t('modules.views.appMonitor.response.s_9360c736') as string, nameKey: 'modules.views.appMonitor.response.s_9360c736',
        type: 'line',
        unit: 'ns',
        data: Object.entries(multipleData.minLatencys).map(([timestamp, value]) => ({
          key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
          value,
        }))
      }, {
        name: i18n.t('modules.views.appMonitor.external.s_3bff553d') as string, nameKey: 'modules.views.appMonitor.external.s_3bff553d',
        type: 'line',
        unit: 'ns',
        data: Object.entries(multipleData.maxLatencys).map(([timestamp, value]) => ({
          key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
          value,
        }))
      }]
    }
    this.chartGroup.response.loading = false;
  }
  // 错误率
  private async fetchErrorSource () {
    const serviceId = this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid));
    const _params: any = {
      serviceId,
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      isIn: 1,
      isSlow: 1,
      componentType: 'service.db',
      graphStats: ['errorCnts', 'errorRates']
    }
    this.chartGroup.error.loading = true;
    const multipleData = await toAsyncWait(ApmApi.getServiceGraph({ ..._params }))
    const multipleResult = multipleData?.result?.data || {}
    if (!Object.keys(multipleResult).length) {
      this.chartGroup.error.source = []
    } else {
      this.chartGroup.error.source = [
        {
          name: i18n.t('modules.views.appMonitor.errors.s_8731f2a8') as string, nameKey: 'modules.views.appMonitor.errors.s_8731f2a8',
          type: 'bar',
          data: Object.entries(multipleResult.errorCnts).map(([timestamp, value]) => ({
            key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
            value,
          }))
        },
        {
          name: i18n.t('modules.views.appMonitor.cache.s_0c8524d7') as string, nameKey: 'modules.views.appMonitor.cache.s_0c8524d7',
          unit: '%',
          type: 'line',
          data: Object.entries(multipleResult.errorRates).map(([timestamp, value]) => ({
            key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
            value,
          }))
        }
      ]
    }
    this.chartGroup.error.loading = false;
  }
  // 请求数
  private async fetchRequestSource () {
    const serviceId = this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid));
    const _params: any = {
      serviceId,
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
      interval: this.timeParams.interval,
      isIn: 1,
      isSlow: 1,
      componentType: 'service.db',
      graphStats: ['callCnts', 'errorRates']
    }
    this.chartGroup.request.loading = true;
    const multipleData = await toAsyncWait(ApmApi.getServiceGraph({ ..._params }))
    const multipleResult = multipleData?.result?.data || {}
    if (!Object.keys(multipleResult).length) {
      this.chartGroup.request.source = []
    } else {
      this.chartGroup.request.source = [{
        name: i18n.t('modules.views.appMonitor.cache.s_8bc42b53') as string, nameKey: 'modules.views.appMonitor.cache.s_8bc42b53',
        type: 'bar',
        data: Object.entries(multipleResult.callCnts).map(([timestamp, value]) => ({
          key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
          value,
        }))
      }, {
        name: i18n.t('modules.views.appMonitor.database.s_77045982') as string, nameKey: 'modules.views.appMonitor.database.s_77045982',
        type: 'line',
        unit: i18n.t('modules.views.appMonitor.serviceDetail.s_a9415ae0') as string,
        data: Object.entries(multipleResult.callCnts).map(([timestamp, value]) => ({
          key: dayjs(Number(timestamp)).format('YYYY-MM-DD HH:mm'),
          value: (value as number || 0) / this.timeParams.interval,
        }))
      }]
    }
    this.chartGroup.request.loading = false;
  }

  // tableInitedHandle
  private tableInitedHandle () {
    this.resetTimeParams();
    this.$refs.listTable.refresh();
  }
  private onFetchEnd () {
    this.listLoading = false
  }

  // 复制
  private copyNameHandle (name: string) {
    copy(name)
  }
}
</script>
<style lang='scss' scoped>
.sql-cont {
  position: relative;
  height: 100%;
}
.sql-wrapper {
  height: 100%;
  display: flex;
}
.chart-group {
  flex: 0 0 auto;
  display: flex;
  flex-wrap: wrap;
  overflow: hidden;

  .chart-item {
    flex: 1 0 auto;
    width: calc( 33.33% - 16px );
    height: 208px;
    overflow: hidden;
    margin: 8px 4px;
    padding: 35px 15px 15px;
    border: 1px solid var(--border-color-base);
    position: relative;

    &:not(:last-child) {
      margin-right: 12px;
    }
  }
  .chart-title {
    position: absolute;
    top: 15px;
    left: 15px;
    line-height: 1;
    margin: 0;
  }
}
.list-wrapper {
  flex: 1 1 auto;
  min-height: 286px;
  :deep(.el-table__cell:hover) {
    .copy-box {
      padding-right: 26px;
    }
    .copy-btn {
      display: block;
    }
  }

  .copy-box {
    display: inline-block;
    vertical-align: top;
    max-width: 100%;
    position: relative;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    line-height: 22px;
    .copy-btn{
      display: none;
      padding: 4px 6px;
      cursor: pointer;
      font-size: 12px;
      line-height: 14px;
      position: absolute;
      top: 0;
      right: 0;
      &:hover {
        color: var(--color-text-link);
      }
    }
  }
}
.tag-group {
  margin-left: 4px;
}
.tag-group-wrapper {
  flex-wrap: wrap;
}
.tag-item {
  padding: 8px 10px;
  line-height: 1;
  margin: 5px 10px 5px 0;
  box-shadow: 0px 2px 8px 0px rgba(139, 142, 147, 0.26);
  position: relative;
  font-size: 0;
  overflow: hidden;
  display: flex;
  flex: 0 1 auto;
  transition: box-shadow .3s ease;
  cursor: pointer;

  .tag-item-label {
    display: inline-block;
    width: 100%;
    font-size: 13px;
    line-height: 16px;
    transition: width .2s ease;
  }
  .tag-item-delete {
    font-size: 14px;
    position: absolute;
    top: 9px;
    right: -15px;
    opacity: 0;
    transition: all .3s ease;
    color: var(--color-text-secondary);
  }

  &:hover, &.is-loading {
    box-shadow: 0px 2px 8px 0px rgba(41, 98, 255, .3);

    .tag-item-label {
      width: calc( 100% - 12px );
    }
    .tag-item-delete {
      right: 8px;
      opacity: 1;

      &:hover {
        color: var(--color-danger);
      }
    }
  }
}
.add-label-cont {
  .add-label-wrapper {
    .add-label-group {
      .add-label-dot {
        margin: 0 7px;
      }
      .add-label-key {
        width: 120px;
      }
      .add-label-value {
        width: 260px;
      }
      .add-label-commit {
        padding: 8px;
        margin-left: 10px;
        background-color: transparent;
      }
      :deep(.el-input__inner) {
        background-color: transparent;
      }
    }

    .add-label-tag {
      background-color: var(--bg-color03);
      border-color: var(--border-color-base);
      font-size: 0;
      color: var(--color-text-primary);
      :deep(.el-icon-close) {
        top: 0;
      }
    }
  }
}

.attribute-group {
  margin-left: 4px;
}
.attribute-group-wrapper {
  border: 1px solid var(--border-color-base);
  border-radius: 4px;
  padding: 16px 20px;
}
.dialog-confirm-btn,
.dialog-cancel-btn {
  letter-spacing: 3px;
  text-indent: 3px;
  font-size: 14px;
  padding: 8px 15px;
  font-weight: normal;
}
.form-item-cont {
  line-height: 1;
  :deep(.el-form-item__content) {
    line-height: 1;
  }
  :deep(.el-form-item__error) {
    padding-top: 9px;
  }
}
.label-tag-span {
  display: inline-block;
  vertical-align: middle;
  font-size: 12px;
  max-width: 200px;
  line-height: 20px;
  position: relative;
  top: -1px;
}
.attribute-item {
  display: flex;

  .attribute-item-label {
    width: 140px;
    margin-bottom: 12px;
  }
}
.system-span {
  display: inline-block;
  line-height: 18px;
  &:not(:last-child) {
    margin-right: 10px;
  }
}
</style>