<template>
  <div class="infrastructure-hostmap"
    v-loading='isLoading'>
    <div class="hostmap-search">
      <div class="search-item">
        {{ $t('modules.views.infrastructure.host.s_354a155e') }}
        <metric-select
          v-model="searchParams.metric"
          @visible-change="toggleHostmapDisabled($event, 'metric')"
          :options="metricList"
          :clearable="false"
          :placeholder="$t('modules.views.configManage.alarm.s_4837e0e6')"
          size="small"
          class="search-select ml-5"
        />

        <el-select
          v-model="searchParams.agg"
          @visible-change="toggleHostmapDisabled"
          size="small"
          :placeholder="$t('modules.views.metrics.list.s_708c9d6d')"
          class="search-select mini"
        >
          <el-option v-for="t in aggList" :key="t" :label="`${t} by`" :value="t"></el-option>
        </el-select>
      </div>

      <div class="search-item ml-10">
        <el-popover
          @hide="thresholdPopoverHideHandle"
          @show="toggleHostmapDisabled(true)"
          placement="bottom-start"
          width="300"
          trigger="click"
          popper-class="hostmap-color-popover">
          <db-icon-button slot="reference" icon='setting'></db-icon-button>

          <el-radio-group v-model="searchParams.thresholdType" size="mini" class="color-type-group">
            <el-radio-button label="num">{{ $t('modules.views.dataReport.report.s_531aadcd') }}</el-radio-button>
            <el-radio-button label="percent">{{ $t('modules.views.infrastructure.host.s_81522afd') }}</el-radio-button>
          </el-radio-group>
          <div :class="['tips', { 'visib-hidden': searchParams.thresholdType !== 'percent' }]">{{ $t('modules.views.infrastructure.host.s_1654486d') }}</div>

          <div class="threshold-list">
            <div v-for="(item, i) in thresholds" :key="item.key" class="threshold-item">
              <span v-if="i === 0" class="threshold-item-input normal">{{ $t('modules.views.infrastructure.host.s_22b777e6') }}</span>
              <el-input-number
                v-else
                v-model="item.value"
                @blur="thresholdValueChangeHandle"
                size="small" :controls="false"
                class="threshold-item-input input-number"
                :class="{ 'unit-item-input': searchParams.thresholdType === 'percent' && i > 0 }"
              ></el-input-number>

              <el-color-picker
                v-model="item.color"
                :predefine="predefineColors"
                size="small"
                class="color-picker"></el-color-picker>

              <span v-if="searchParams.thresholdType === 'percent' && i > 0" class="unit">%</span>

              <el-button
                v-if="i > 0"
                @click="thresholds.splice(i, 1)"
                icon="el-icon-delete" size="small" type="text"
                class="delete-btn"></el-button>
            </div>

            <el-button
              @click="addThresholdHandle"
              icon="el-icon-plus" size="mini"
              class="add-threshold-btn">{{ $t('modules.views.infrastructure.host.s_a2ce8f18') }}</el-button>
          </div>

        </el-popover>
      </div>
    </div>

    <hexmap-chart
      ref="hostmapChart"
      :hostIds="hostIds"
      :hostmapData="hostmapData"
      :isGroup="isGroup"
      :thresholdType="searchParams.thresholdType"
      :thresholds="thresholds"
      :loading="isLoading"
      @on-click="hostmapClickHandle"
      :class="[{ 'event-none': hostmapDisabled }]"
    />
  </div>
</template>

<script lang='ts'>
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import { Getter } from 'vuex-class';
import i18n from '@/i18n';
import InfraApi from '@/api/infrastructure';
import MetricSelect from '@/components/metric-select.vue';
import HexmapChart from './hexmap.vue';

@Component({
  components: {
    HexmapChart,
    MetricSelect,
  },
  filters: {
    formatThresholdType: (type: string) => {
      switch (type) {
        case 'num':
          return i18n.t('modules.views.dataReport.report.s_531aadcd') as string
        case 'percent':
          return i18n.t('modules.views.infrastructure.host.s_81522afd') as string
        default:
          return ''
      }
    },
  }
})
export default class InfrastructureHostmap extends Vue {
  @Prop({ default: () => ({}) }) private params!: any

  public $refs!: {
    hostmapChart: HexmapChart
  }

  private metricList: string[] = [
    'system.cpu.usage',
    'system.mem.usage',
    'system.mem.pct_usable',
    'system.disk.pct_used',
    'system.disk.in_use',
    'system.net.packets_in.error',
    'system.net.packets_in.count',
    'system.net.packets_out.error',
    'system.net.packets_out.count',
  ]
  private aggList: string[] = ['avg', 'sum', 'max', 'min']

  private colorList: string[] = [
    '#73BF69', '#FADE2A', '#F2495C', '#B877D9', '#8886E6',
    '#5794F2', '#65AAAE', '#B7CF4A', '#FDBB2D', '#FF9830',
  ]
  private predefineColors = [
    '#73BF69', '#FADE2A', '#F2495C', '#B877D9', '#8886E6',
    '#5794F2', '#65AAAE', '#B7CF4A', '#FDBB2D', '#FF9830',
  ]

  private searchParams: any = {
    metric: '',
    agg: 'avg',
    thresholdType: 'num',
  }

  private thresholds: any[] = [
    { key: 1.1, color: '#73BF69' },
    { key: 1.2, color: '#FADE2A', value: 10 },
    { key: 1.3, color: '#F2495C', value: 20 },
  ]

  private hostmapDisabled: boolean = false // Select选择时，hostmap事件禁用，防止事件冲突选择框无法收起

  private isLoading = false
  private hostmapData: any = {}
  private hostIds: any[] = []
  private isGroup = false // 是否分组

  @Watch('searchParams', { deep: true })
  private onSearchParamsChange (val: any) {
    this.getHostmapData();
    window.localStorage.setItem('INFRASTRUCTURE_HOSTMAP', JSON.stringify({
      ...val,
      thresholds: this.thresholds,
    }));
  }

  private created() {
    const params = window.localStorage.getItem('INFRASTRUCTURE_HOSTMAP')
    if (params) {
      const { metric, agg, thresholdType, thresholds } = JSON.parse(params)
      this.searchParams = { metric, agg, thresholdType }
      this.thresholds = thresholds
    }
  }

  // 添加阈值项
  private addThresholdHandle () {
    let value = 0
    if (this.thresholds.length > 1) {
      value = (this.thresholds.slice(-1)[0].value || 0) + 10
    }
    this.thresholds.push({
      key: Math.random(),
      color: this.colorList[this.thresholds.length % this.colorList.length],
      value
    })
  }

  // 根据阈值大小重新排序
  private thresholdValueChangeHandle () {
    this.thresholds.sort((a: any, b: any) => (a.value || 0) - b.value)
  }

  // 弹框隐藏时 保存配置到本地
  private thresholdPopoverHideHandle () {
    const thresholds = JSON.parse(JSON.stringify(this.thresholds))
    thresholds.forEach((t: any, i: number) => {
      if (!t.value && i > 0) {
        t.value = 0
      }
    });
    this.toggleHostmapDisabled(false)
    window.localStorage.setItem('INFRASTRUCTURE_HOSTMAP', JSON.stringify({
      ...this.searchParams,
      thresholds,
    }));
  }

  private metricSelectVisible = false
  private toggleHostmapDisabled (disabled: boolean, type?: string) {
    this.hostmapDisabled = disabled
    if (type === 'metric') {
      this.metricSelectVisible = disabled
    }
  }

  // 点击 hostmap 图表回调
  private hostmapClickHandle (data: any) {
    this.$router.push({
      path: '/infrastructure/hostDetail',
      query: { hostName: encodeURIComponent(data['df-hostname']), }
    })
  }

  // 获取 hostmap 数据
  public getHostmapData () {
    if (this.isLoading) {
      return;
    }
    if (!this.metricList.includes(this.searchParams.metric)) {
      this.searchParams.metric = this.metricList[0]
    }
    const { metric, agg, thresholdType } = this.searchParams
    const params: any = {
      ...this.params,
      hostMapMetric: metric,
      hostMapAgg: agg,
      hostMapThresholdType: thresholdType,
    }
    Object.entries(params).forEach(([key, value]) => {
      if (value === '' || (Array.isArray(value) && !value.length)) {
        delete params[key]
      }
    })
    this.isLoading = true;
    InfraApi.getHostmapList(params)
      .then((rst: any) => {
        if (rst && rst.status === 200 && Array.isArray(rst.data)) {
          const { data = [] } = rst
          this.hostIds = []
          this.hostmapData = {}
          this.isGroup = !!params.group
          const formatData = (arr: any[]) => {
            const ids: string[] = []
            arr.forEach((t: any) => {
              this.hostmapData[t.id] = {
                ...t,
                threshold: t.threshold || 0,
                thresholdUnit: t.thresholdUnit ? t.thresholdUnit : thresholdType === 'percent' ? 'percent' : '',
                tableHostName: t['df-hostname'],
              }
              ids.push(t.id)
            })
            return ids
          }
          if (!params.group) {
            this.hostIds = formatData(data)
          } else {
            this.hostIds = data.map((item: any) => ({
              ...item,
              data: formatData(item.data)
            }))
          }
        } else {
          throw new Error(rst.message);
        }
      })
      .catch((err) => {
        if (err.message !== 'interrupt') {
          this.$message.error(err.message);
        }
      })
      .finally(() => this.isLoading = false);
  }

  // resize
  public resize () {
    this.$refs.hostmapChart.resize()
  }
}
</script>

<style lang='scss' scoped>
.infrastructure-hostmap {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  padding-top: 10px;
  background: var(--bg-color);

  .event-none {
    pointer-events: none;
  }
}
.hostmap-search {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-bottom: 10px;

  .search-item {
    font-size: 13px;
    color: var(--color-text-primary);
  }

  .search-select {
    width: 180px;
    &+.search-select {
      margin-left: 4px;
    }
    &.mini {
      width: 100px;
    }
    :deep(.el-input__inner){
      color: var(--color-text-regular);
    }
  }

  .color-btn {
    width: 100px;
    padding: 5px 30px 5px 15px;
    text-align: left;
    font-size: 13px;
    line-height: 20px;
    color: var(--color-text-regular);
    &:hover {
      border-color: var(--border-color-base);
      color: var(--color-text-regular) !important;
    }
    &:focus {
      border-color: var(--color-primary);
    }
  }
}

.hostmap-color-popover {
  .color-type-group {
    :deep(.el-radio-button__inner) {
      min-width: 67px;
    }
  }
  .tips {
    padding: 2px 4px 6px;
    font-size: 12px;
    line-height: 18px;
    color: var(--color-text-secondary);
    &.visib-hidden {
      visibility: hidden;
    }
  }

  .threshold-list {
    margin-right: -2px;
    padding-right: 2px;
    max-height: 270px;
    overflow: auto;
  }

  .threshold-item {
    margin-bottom: 8px;
    position: relative;
    .threshold-item-input {
      display: block;
      width: 100%;
      &.normal {
        box-sizing: border-box;
        padding-left: 44px;
        height: 32px;
        background-color: var(--bg-color02);
        border: 1px solid var(--border-color-base);
        border-radius: 4px;
        color: var(--color-text-secondary);
        line-height: 30px;
        pointer-events: none;
      }
      :deep(.el-input__inner) {
        padding-left: 44px;
        padding-right: 30px;
        text-align: left;
      }
      &.unit-item-input {
        :deep(.el-input__inner) {
          padding-left: 60px;
        }
      }
    }

    .color-picker {
      display: block;
      width: 18px;
      height: 18px;
      position: absolute;
      top: 7px;
      left: 16px;
      :deep(.el-color-picker__trigger) {
        display: block;
        width: 18px;
        height: 18px;
        padding: 0;
        border-radius: 2px;
        border-color: var(--border-color-base);
      }
      :deep(.el-color-picker__color) {
        border: none;
      }
      :deep(.el-color-picker__color-inner) {
        border-radius: 2px;
      }
      :deep(.el-color-picker__icon) {
        display: none;
      }
    }

    .unit {
      color: var(--color-text-secondary);
      font-size: 12px;
      line-height: 20px;
      position: absolute;
      top: 6px;
      left: 44px;
    }


    .delete-btn {
      padding: 4px 5px;
      color: var(--color-text-regular);
      font-size: 14px;
      line-height: 20px;
      position: absolute;
      top: 1px;
      right: 1px;
    }
  }

  .add-threshold-btn {
    width: 100%;
    background: var(--border-color-light);
    padding: 6px 10px;
    border: none;
    color: var(--color-text-regular) !important;
    opacity: 0.9;
    &:hover {
      opacity: 1;
    }
  }
}
</style>
