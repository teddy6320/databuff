<template>
  <div class="latency-chart-wrapper df" v-loading='chartLoading'>
    <div class="latency-chart-cont flex-1 flex-v">
      <chart-latency
        :source="latencyChartSource"
        :show-empty="showEmpty"
        :markLineSource='latencyLinesSource'
        unit='ns'
        :interval='latencyChartInterval'
        :logModel='logCalcModel'
        class="flex-1" />
      <el-checkbox
        v-if="!showEmpty"
        v-model="logCalcModel"
        class="latency-setting">{{ $t('modules.views.appMonitor.response.s_097963aa') }}</el-checkbox>

      <div class="analysis-tools font-12 mt-5">
        <span>{{ $t('modules.views.appMonitor.response.s_606672f9') }}</span>
        <el-tag
          @click="viewErrorHandle"
          size="small"
          class="mr-10 cp"
        >{{ $t('modules.views.aiMonitor.errors.s_b593af50') }}<i class="el-icon el-icon-arrow-right ml-5"></i></el-tag>

        <el-tag
          @click="viewAnalysisHandle"
          size="small"
          class="mr-10 cp"
        >{{ $t('modules.views.appMonitor.response.s_598ea178') }}<i class="el-icon el-icon-arrow-right ml-5"></i></el-tag>

        <el-tooltip :disabled="!isEbpfSource" effect="light"
          :content="$t('modules.views.appMonitor.response.s_b311b242')"
          placement="top" class="mr-10">
          <el-tag
            @click="viewFlowHandle(isEbpfSource)"
            :type="isEbpfSource ? 'info' : ''"
            size="small"
            :class="['cp', { 'disable-tab-btn': isEbpfSource }]"
          >{{ $t('modules.views.appMonitor.response.s_54c1cb4b') }}<i class="el-icon el-icon-arrow-right ml-5"></i></el-tag>
        </el-tooltip>
      </div>
    </div>

    <div class="latency-info-cont flex-none">
      <div class="latency-info-item latency-info-title">{{ $t('modules.views.appMonitor.response.s_2609f27e') }}</div>
      <div v-for='value, key in latencyInfo' :key='key' class="latency-info-item flex-h">
        <span>
          {{ key | latencyStrFormat }}
          <el-tooltip effect="light" :content='latencyInfoTip[key].content' placement="top">
            <i class="db-icon-info font-13"></i>
          </el-tooltip>
        </span>
        <span>{{ value | NsFilter }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch, Prop } from 'vue-property-decorator';
import i18n from '@/i18n';
import ChartLatency from './chart-latency.vue';
import { bisectCenter } from 'd3';
import ApmApi from '@/api/apm';
import { toAsyncWait } from '@/utils/common';

@Component({
  components: {
    ChartLatency,
  },
  filters: {
    latencyStrFormat (val: string) {
      return val.replace('Latency', '')
    },
  }
})
export default class LatencyChartWrapper extends Vue {
  private logCalcModel = false;

  private chartLoading = false;

  private showEmpty = false;
  private latencyChartInterval = 0;

  private latencyInfo = {
    max: 0,
    p99Latency: 0,
    p95Latency: 0,
    p90Latency: 0,
    p75Latency: 0,
    p50Latency: 0,
    // min: 0,
  }

  private latencyInfoTip = {
    // min: {
    //   title: i18n.t('modules.views.appMonitor.response.s_9360c736') as string, titleKey: 'modules.views.appMonitor.response.s_9360c736',
    //   content: i18n.t('modules.views.appMonitor.response.s_4c3fcaf6') as string, contentKey: 'modules.views.appMonitor.response.s_4c3fcaf6'
    // },
    max: {
      title: i18n.t('modules.views.appMonitor.external.s_3bff553d') as string, titleKey: 'modules.views.appMonitor.external.s_3bff553d',
      content: i18n.t('modules.views.appMonitor.response.s_325e9150') as string, contentKey: 'modules.views.appMonitor.response.s_325e9150'
    },
    p50Latency: {
      title: i18n.t('modules.views.appMonitor.external.s_13a12460') as string, titleKey: 'modules.views.appMonitor.external.s_13a12460',
      content: i18n.t('modules.views.appMonitor.response.s_976a9fe2') as string, contentKey: 'modules.views.appMonitor.response.s_976a9fe2'
    },
    p75Latency: {
      title: i18n.t('modules.views.appMonitor.external.s_4b846a1b') as string, titleKey: 'modules.views.appMonitor.external.s_4b846a1b',
      content: i18n.t('modules.views.appMonitor.response.s_f4744f28') as string, contentKey: 'modules.views.appMonitor.response.s_f4744f28'
    },
    p90Latency: {
      title: i18n.t('modules.views.appMonitor.external.s_28e0109c') as string, titleKey: 'modules.views.appMonitor.external.s_28e0109c',
      content: i18n.t('modules.views.appMonitor.response.s_c324a483') as string, contentKey: 'modules.views.appMonitor.response.s_c324a483'
    },
    p95Latency: {
      title: i18n.t('modules.views.appMonitor.external.s_e8123b2d') as string, titleKey: 'modules.views.appMonitor.external.s_e8123b2d',
      content: i18n.t('modules.views.appMonitor.response.s_2e871e78') as string, contentKey: 'modules.views.appMonitor.response.s_2e871e78'
    },
    p99Latency: {
      title: i18n.t('modules.views.appMonitor.external.s_8c8e1f39') as string, titleKey: 'modules.views.appMonitor.external.s_8c8e1f39',
      content: i18n.t('modules.views.appMonitor.response.s_22110ee7') as string, contentKey: 'modules.views.appMonitor.response.s_22110ee7'
    },
  }

  private latencyLinesSource: any = [
    { name: 'P50', value: '5ms' },
    { name: 'P75', value: '6ms' },
    { name: 'P90', value: '7ms' },
  ];

  private latencyChartSource: any = [];

  get getServiceId () {
    return decodeURIComponent(this.$route.query.sid as string)
  }

  get serviceInstance () {
    const si = this.$route.query.si as string || ''
    return decodeURIComponent(si)
  }

  get isEbpfSource () {
    const datasource = String((this.$store.state.Service.basicServiceMap?.[this.getServiceId] || {})?.datasource || '')
    return datasource.toLowerCase() === 'df-ebpf';
  }

  public getData () {
    this.getLatencyStaticSource()
  }

  private async getLatencyStaticSource () {
    const { fromTime, toTime, interval } = this.getGlobalTimeV2()
    this.chartLoading = true;
    const params: any = {
      fromTime,
      toTime,
      serviceId: this.getServiceId,
      numBuckets: 100,
      interval,
    }
    if (this.serviceInstance) {
      params.serviceInstance = this.serviceInstance
    }
    const { result, error } = await toAsyncWait(ApmApi.getServiceLatencyGraph(params))
    if (error) {
      // this.showEmpty = true
      // this.latencyChartSource = [];
    } else {
      const { data } = result
      if (!data) {
        // console.log('empty data')
        this.showEmpty = true
        this.chartLoading = false;
        return;
      } else {
        this.showEmpty = false
      }

      // latencyInfo
      this.latencyInfo.max = data.p100Latency || 0;
      this.latencyInfo.p99Latency = data.p99Latency || 0;
      this.latencyInfo.p95Latency = data.p95Latency || 0;
      this.latencyInfo.p90Latency = data.p90Latency || 0;
      this.latencyInfo.p75Latency = data.p75Latency || 0;
      this.latencyInfo.p50Latency = data.p50Latency || 0;
      // this.latencyInfo.min = data.p0Latency || 0;

      const { histogram = {} } = data || {};
      let keys: any[] = Object.keys(histogram);
      if (!keys.length) {
        this.showEmpty = true
        this.chartLoading = false;
        return
      } else {
        this.showEmpty = false
        keys = keys.sort((a: number, b: number) => a - b)
        this.latencyChartInterval = keys[1] - keys[0];
      }

      const values = keys.map(k => histogram[k]);
      const dataSource = [];
      const xAxisDatas = [];
      for (let i = 0, len = keys.length; i < len; i++) {
        if (i < keys.length - 1) {
          dataSource.push({
            // key: `${Math.ceil( ( Number(keys[i + 1]) - Number(keys[i]) ) / 2 + Number(keys[i]) )}`,
            key: keys[i + 1],
            value: values[i] || 0
          });
          xAxisDatas.push(keys[i + 1])
        }
      }

      const source = [
        {
          name: '',
          data: dataSource
        }
      ];

      // 计算markline的数据
      const idx1 = bisectCenter(xAxisDatas, this.latencyInfo.p50Latency);
      const idx2 = bisectCenter(xAxisDatas, this.latencyInfo.p75Latency);
      const idx3 = bisectCenter(xAxisDatas, this.latencyInfo.p90Latency);
      const idx4 = bisectCenter(xAxisDatas, this.latencyInfo.p95Latency);
      const idx5 = bisectCenter(xAxisDatas, this.latencyInfo.p99Latency);
      this.latencyLinesSource = [
        { name: 'p50', value: `${xAxisDatas[idx1]}`, limit: 50 },
        { name: 'p75', value: `${xAxisDatas[idx2]}`, limit: 75 },
        { name: 'p90', value: `${xAxisDatas[idx3]}`, limit: 90 },
        { name: 'p95', value: `${xAxisDatas[idx4]}`, limit: 95 },
        { name: 'p99', value: `${xAxisDatas[idx5]}`, limit: 99 },
      ]
      this.latencyChartSource = source
    }
    this.chartLoading = false;
  }

  // 错误率标签点击事件
  private viewErrorHandle () {
    this.$router.push({
      path: '/appMonitor/errors',
      query: {
        ...this.$route.query,
        sid: encodeURIComponent(this.getServiceId),
      }
    });
  }

  // 链路追踪标签点击事件
  private viewAnalysisHandle () {
    this.$router.push({
      path: '/appMonitor/serviceAnalysis',
      query: {
        ...this.$route.query,
        sid: encodeURIComponent(this.getServiceId),
      }
    });
  }

  private viewFlowHandle (disable?: boolean) {
    if (disable) {
      return
    }
    this.$router.push({
      path: '/appMonitor/serviceFlow',
      query: {
        serviceId: encodeURIComponent(this.getServiceId),
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.latency-chart-wrapper {
  position: relative;

  .latency-chart-cont {
    position: relative;
    padding-top: 5px;
  }

  .latency-setting {
    position: absolute;
    right: 0;
    top: 0;
  }

  .latency-info-cont{
    margin-left: 20px;
    width: 230px;
    border: 1px solid var(--border-color-base);
    border-radius: 4px;
    overflow: hidden;

    .latency-info-item{
      padding: 9px 16px;
      font-size: 12px;
      color: var(--color-text-primary);
      line-height: 14px;
      height: 32px;
      justify-content: space-between;

      &.latency-info-title {
        font-size: 13px;
      }

      &:nth-of-type(2n) {
        background-color: var(--bg-color03);
      }
    }
  }

  :deep(.analysis-tools .el-tag) {
    height: 28px;
    font-size: 12px;
    line-height: 1;
    padding: 8px 10px;
    border-radius: 4px;
    border: none;
  }

  .disable-tab-btn {
    cursor: not-allowed;
  }
}
</style>
