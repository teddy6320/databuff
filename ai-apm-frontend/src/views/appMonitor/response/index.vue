<template>
  <div class="service-response-wrapper">
    <div class="service-response-content">
      <LatencyChartWrapper ref='latencyComp' />

      <div class="mt-20">
        <div class="font-14 fw-500 lh-20 mb-10">{{ $t('modules.views.appMonitor.response.s_a74048dc') }}</div>
        <el-table
          :data="resourceList"
          v-loading="resourceListLoading"
          :empty-text="!resourceListLoading ? $t('modules.views.appMonitor.errorDetail.s_21efd88b') : ' '"
          highlight-current-row size="small"
          tooltip-effect="light">
          <el-table-column :label="$t('modules.views.aiPlatform.experts.s_d7ec2d3f')" :minWidth='300' show-overflow-tooltip>
            <template slot-scope="{ row }">
              <span @click="viewResourceDetailHandle(row)" class="link-text">{{ row.resource }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('modules.views.appMonitor.serviceFlow.s_96a0c062')" :minWidth='100'>
            <template slot-scope="{ row }">
              <span>{{ row.avgLatency | NsFilter }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('modules.views.appMonitor.cache.s_0c8524d7')" :minWidth='100'>
            <template slot-scope="{ row }">
              <span>{{ row.errRate | PercentFilter(!!row.errCnt) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator'
import LatencyChartWrapper from './latency-chart-wrapper.vue';
import i18n from '@/i18n';
import ApmApi from '@/api/apm';
import { toAsyncWait } from '@/utils/common';

@Component({
  components: {
    LatencyChartWrapper
  },
})
export default class ServiceResponse extends Vue {
  public $refs!: {
    latencyComp: LatencyChartWrapper
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  get getServiceId () {
    return decodeURIComponent(this.$route.query.sid as string)
  }

  get serviceInstance () {
    const si = this.$route.query.si as string || ''
    return decodeURIComponent(si)
  }

  get serviceIdNameMapping () {
    const mapping: any = {}
    const basicServiceMap = this.$store.getters['Service/basicServiceMap']
    Object.keys(basicServiceMap).forEach((t: string) => {
      mapping[t] = basicServiceMap[t].name
    });
    return mapping
  }

  // 请求贡献者列表
  private resourceListQuery = {
    offset: 0,
    size: 5,
    serviceId: '',
    fromTime: '',
    toTime: '',
    sortField: 'callCnt',
    sortOrder: 'desc',
  }
  private resourceListLoading = false;
  private resourceList = [];

  private created () {
    const { sid = '', si = '' } = this.$route.query;
    this.$nextTick(() => {
      const _sid = decodeURIComponent(String(sid || ''))
      const _sn = this.serviceIdNameMapping[_sid] || ''
      const _si = decodeURIComponent(String(si || ''))
      this.$store.commit('UPDATE_BREADCRUMB', [{
        name: !_si
          ? (i18n.t('modules.views.appMonitor.response.s_a3990fd1', { value0: _sn }) as string)
          : (i18n.t('modules.views.appMonitor.response.s_52710629', { value0: _si, value1: _sn }) as string),
        path: '/appMonitor/response',
      }]);
    });
  }
  private mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
    this.durationChangeHandle()
  }
  private beforeDestroy() {
    // 清空面包屑
    this.$store.commit('CLEAR_BREADCRUMB');
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh')
  }

  private async getServiceResourceTop () {
    const { fromTime, toTime } = this.getGlobalTimeV2()
    const params: any = {
      ...this.resourceListQuery,
      serviceId: this.getServiceId,
      fromTime,
      toTime,
    }
    if (this.serviceInstance) {
      params.serviceInstance = this.serviceInstance
    }
    this.resourceListLoading = true;
    const { result, error } = await toAsyncWait(ApmApi.getServiceReqTop(params))
    if (!error) {
      const { data = [] } = result
      this.resourceList = data.sort((a: any, b: any) => b.avgLatency - a.avgLatency);
    }
    this.resourceListLoading = false;
  }

  private durationChangeHandle () {
    this.getServiceResourceTop()
    this.$refs.latencyComp.getData()
  }
  // 查看请求详情
  private viewResourceDetailHandle (row: any) {
    const query: any = {
      ...this.$route.query,
      endpoint: encodeURIComponent(row.resource),
      componentType: row.componentType,
    }
    delete query.type
    this.$router.push({
      path: '/appMonitor/resourceDetail',
      query,
    });
  }
}

</script>

<style lang='scss' scoped>
.service-response-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .service-response-content {
    flex: 1;
    padding: 20px;
    border-radius: 4px;
    background-color: var(--bg-color);
    display: flex;
    flex-direction: column;
  }
}
</style>
