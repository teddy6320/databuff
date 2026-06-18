<template>
  <div class="traffic-detail">
    <div>
      <div class="info-item">{{ $t('modules.views.npm.analysis.s_5439441f') }}<span class="value">{{ detail._client || '-' }}</span></div>
      <div class="info-item">{{ $t('modules.views.npm.analysis.s_d9aec692') }}<span class="value">{{ detail._server || '-' }}</span></div>
      <div class="info-item">{{ $t('modules.views.npm.analysis.s_242bb77f') }}<span class="value">{{ detail['npm.volume_sent'] | valueFilter('bytes') }}</span></div>
      <div class="info-item">{{ $t('modules.views.npm.analysis.s_df9341c0') }}<span class="value">{{ detail['npm.throughput_sent'] | valueFilter('bytes/s') }}</span></div>
      <div class="info-item">{{ $t('modules.views.npm.analysis.s_2e24e079') }}<span class="value">{{ detail['npm.volume_rcvd'] | valueFilter('bytes') }}</span></div>
      <div class="info-item">{{ $t('modules.views.npm.analysis.s_021485a5') }}<span class="value">{{ detail['npm.throughput_rcvd'] | valueFilter('bytes/s') }}</span></div>
      <div class="info-item max">{{ $t('modules.views.configInstall.apm.s_1655dd5f') }}<span class="value">
        <el-tag
          v-for="(t, i) in tagList"
          :key="i"
          size="small" effect="dark"
          class="info-tag"
        >{{ t }}</el-tag>
      </span></div>
    </div>

    <div class="flex-h-jc mb-15 mt-5">
      <!-- <db-tabnav
        v-model="activeName"
        :tabnavs="tabs"
        :thin="true"
        @on-change="val => activeName = val"
        class="tabs-nav"
      /> -->

      <span class="pane-title">{{ $t('modules.views.npm.analysis.s_c3318eaa') }}</span>

      <el-date-picker
        v-model="timeRange"
        @change="timeRangeChangeHandle"
        :picker-options="dateOptions"
        type="datetimerange"
        align="right"
        :start-placeholder="$t('modules.views.npm.analysis.s_b44c0f33')"
        :end-placeholder="$t('modules.views.npm.analysis.s_1d468be9')"
        :default-time="['00:00:00', '23:59:59']"
        format="yyyy-MM-dd HH:mm"
        size='mini'
        :clearable='false'
        :editable='false'
        :unlink-panels='true'
        class="time-range"
      />
    </div>

    <div class="tabs-pane-wrap">
      <component
        :is="activeName"
        :timeParamsStr="JSON.stringify(queryParams)"
        :chartTypes="chartTypes"
        :tagList="tagList" />
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs'
import humanFormat from 'human-format';
import getUnitData from '@/utils/getUnitData';
import { calcInterval } from '@/utils/timeFormat'
import Performance from './performance.vue';
import Traffic from './traffic.vue';

@Component({
  components: {
    Performance,
    Traffic,
  },
  filters: {
    valueFilter (value: number, unit: string) {
      if (!value && String(value) !== '0' || isNaN(+value) || !isFinite(+value)) {
        return '-'
      }
      const _val = Number(value)
      if (_val === 0) {
        return '0'
      }
      const { scale_factor, scale, sub_unit, family } = getUnitData(unit);
      const vData = humanFormat.raw(Number(value) * scale_factor, {
        ...scale,
        decimals: 2,
      })
      if (!['time', 'bytes'].includes(family)) {
        const _value = _val < 0.1 ? '< 0.1' : `${vData.value}${vData.prefix}`
        return `${_value} ${scale.unit}${sub_unit}`
      }
      return `${vData.value} ${vData.prefix}${scale.unit}${sub_unit}`
    }
  }
})
export default class TrafficDetail extends Vue {
  @Prop({ default: () => ({}) }) private detail!: any;
  @Prop({ default: () => [] }) private chartTypes!: any[];

  private tabs: any[] = [
    { label: i18n.t('modules.views.npm.analysis.s_c3318eaa') as string, labelKey: 'modules.views.npm.analysis.s_c3318eaa', value: 'performance' },
    { label: i18n.t('modules.views.npm.analysis.s_c6e3373a') as string, labelKey: 'modules.views.npm.analysis.s_c6e3373a', value: 'traffic' },
  ]
  private activeName: string = 'performance';

  private timeRange: any[] = []
  private dateOptions: any = {}
  private queryParams = {
    fromTime: '',
    toTime: '',
    interval: 300,
  }

  get tagList () {
    return Object.entries(this.detail.tags || {}).map(([k, v]) => `${k || ''}:${v || ''}`)
  }

  private created () {
    this.dateOptions = {
      disabledDate: (time: Date) => {
        const fromTime = new Date(this.detail._fromTime)
        const toTime = new Date(this.detail._toTime)
        const times = time.getTime()
        return times < +fromTime || times > +toTime
      }
    }
    this.timeRange = [this.detail._fromTime, this.detail._toTime]
    this.timeRangeChangeHandle(this.timeRange)
  }

  private timeRangeChangeHandle (val: Date[]) {
    this.queryParams.fromTime = dayjs(val[0]).format('YYYY-MM-DD HH:mm') + ':00'
    this.queryParams.toTime = dayjs(val[1]).format('YYYY-MM-DD HH:mm') + ':00'
    this.queryParams.interval = calcInterval(+new Date(val[0]), +new Date(val[1]))
  }
}
</script>

<style lang="scss" scoped>
.traffic-detail {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-width: 500px;
  overflow: hidden;

  .info-item {
    margin-bottom: 4px;
    padding-right: 10px;
    min-width: 50%;
    display: inline-block;
    vertical-align: top;
    word-break: break-all;
    &.max {
      width: 100%;
    }
    .value {
      color: var(--color-text-primary);
    }
  }

  .info-tag {
    margin: 3px 6px 3px 0;
    padding: 0 7px;
    max-width: calc(100% - 6px);
    height: 22px;
    line-height: 20px;
    background: var(--bg-color03);
    border: 1px solid var(--border-color-base);
    border-radius: 2px;
    color: var(--color-text-primary);
    vertical-align: middle;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    position: relative;
  }

  .tabs-nav {
    margin-bottom: 16px;
  }
  .tabs-pane-wrap {
    flex: 1;
    position: relative;
    overflow-x: hidden;
    overflow-y: auto;
  }

  .time-range {
    width: 300px;
    flex: none;
  }

  .pane-title {
    font-size: 14px;
    line-height: 28px;
    color: var(--color-text-primary);
  }
}
</style>
