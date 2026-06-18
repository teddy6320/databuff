<template>
  <div class="traffic-detail">
    <div class="flex-none">
      <div class="info-item">{{ $t('modules.views.npm.analysis.s_5439441f') }}<span class="value">{{ detail._client || '-' }}</span></div>
      <div class="info-item">DNS IP：<span class="value">{{ detail._server || '-' }}</span></div>
      <div class="info-item">{{ $t('modules.views.npm.dns.s_4534095f') }}<span class="value">{{ detail['dns.cnt'] | valueFilter('requests') }}</span></div>
      <div class="info-item">{{ $t('modules.views.npm.dns.s_1e52e197') }}<span class="value">{{ detail['dns.response_time'] | valueFilter('ns') }}</span></div>
      <div class="info-item">{{ $t('modules.views.npm.dns.s_8a8cc2a8') }}<span class="value">{{ detail['dns.cnt.rate'] | valueFilter('requests/s') }}</span></div>
      <div class="info-item">{{ $t('modules.views.npm.dns.s_3102816b') }}<span class="value">{{ detail['dns.errors.pct'] | valueFilter('percent') }}</span></div>
      <div class="info-item max">{{ $t('modules.views.npm.dns.s_1655dd5f') }}<span class="value">
        <el-tag
          v-for="(t, i) in tagList"
          :key="i"
          size="small" effect="dark"
          class="info-tag"
        >{{ t }}</el-tag>
      </span></div>
    </div>

    <chart-group
      ref="chartGroup"
      :query="{
        from: queryFrom,
      }"
      :filter="{}"
      :timeParams="timeParams"
      layoutType="detail"
      class="flex-none"
    />

    <table-list
      ref="tableList"
      :query="{
        server: 'domainName',
        from: queryFrom,
      }"
      :filter="{}"
      :timeParams="timeParams"
      layoutType="detail"
      class="flex-1"
    />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import humanFormat from 'human-format';
import getUnitData from '@/utils/getUnitData';
import ChartGroup from '../chart-group.vue'
import TableList from '../table-list.vue'

@Component({
  components: {
    ChartGroup,
    TableList,
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
  @Prop({ default: () => ({}) }) private timeParams!: any;

  public $refs!: {
    chartGroup: ChartGroup
    tableList: TableList
  }

  private queryParams = {
    fromTime: '',
    toTime: '',
    interval: 300,
  }

  get tagList () {
    return Object.entries(this.detail.tags || {}).map(([k, v]) => `${k || ''}:${v || ''}`)
  }

  get queryFrom () {
    const from = this.tagList.map((tag, index) => {
      const [key, ...values] = tag.split(':')
      return { left: key, operator: '=', right: values.join(':'), connector: 'AND' }
    })
    return from
  }

  private mounted () {
    this.$refs.chartGroup && this.$refs.chartGroup.getData()
    this.$refs.tableList && this.$refs.tableList.getData()
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
}
</style>
