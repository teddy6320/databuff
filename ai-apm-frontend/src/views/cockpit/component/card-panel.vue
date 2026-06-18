<template>
  <div @click='$emit("click")' class="card-cont bg-color br-4 cp">
    <div class="card-header flex-h pl-20 pr-20 ovh" :data-status='detail.status'>
      <div class="ell">
        <i class="db-icon mr-8">{{ detail.type | DbIconFilter }}</i>
        <span>{{ detail.nameKey ? $t(detail.nameKey) : detail.name }}</span>
      </div>
      <div @click.stop="$emit('alarm-click')" class="ml-6 br-4 alarm-status cp" data-status='error'>
        <span class="db-icon db-icon-alarm-fill vm mr-4" data-status='error'></span>
        <span>{{ detail.alarmCount || 0 }}</span>
      </div>
    </div>
    <div class="card-body flex-h p-16">
      <div class="rate-circle br-50p text-white flex-v-cc" :data-status='detail.status'>
        <div class="font-32 lh-1">{{ detail.score }}</div>
        <div class="font-10 lh-14">{{ $t('modules.views.cockpit.component.s_b6324be4') }}</div>
      </div>

      <div class="ml-20 flex-1 ovh">
        <div v-for='(info, index) in detail.infos' :key='index' class="flex-h ell lh-28 ovh">
          <el-tooltip :content="info.label" placement="top" effect="light" :open-delay='500' :enterable='false'>
            <span class=" describe font-12 ell">{{ info.labelKey ? $t(info.labelKey) : info.label }}</span>
          </el-tooltip>
          <span class="flex-none dib ell font-12 fw-500 ml-10 mw-50p">{{ info.value | getFilterByUnit(info.unit) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import FilterMap from '@/utils/filters'

@Component({
  filters: {
    getFilterByUnit (value: any, unit: any, lessZeroOne = false, zeroIgnore = false) {
      switch (unit) {
        case 'ns':
          return FilterMap.NsFilter(value, zeroIgnore)
        case 'ms':
          return FilterMap.MsFilter(value, zeroIgnore)
        case 's':
          return FilterMap.SecondFilter(value, zeroIgnore)
        case 'nsDuration':
          return FilterMap.NsDurationFilter(value, zeroIgnore)
        case 'msDuration':
          return FilterMap.MsDurationFilter(value, zeroIgnore)
        case 'sDuration':
          return FilterMap.DurationFilter(value, zeroIgnore)
        case 'b':
          return FilterMap.BytesFilter(value, lessZeroOne)
        case '%':
        case 'percent':
          return FilterMap.PercentFilter(value, lessZeroOne)
        case 'count':
        case '':
        case undefined:
        case null:
          return FilterMap.NumberFilter(value, lessZeroOne)
        case 'time':
          return FilterMap.TimesToDateFilter(value)
        case 'minuteTime':
          return FilterMap.TimesToDateFilter(value, 'YYYY-MM-DD HH:mm')
        case 'alarmType':
          return FilterMap.AlarmTypeFilter(value)
        case 'alarmLevel':
          return FilterMap.AlarmStatusFilter(value)
        case 'alarmStatus':
          return FilterMap.AlarmDealStatusFilter(value)
        case 'processStatus':
          return FilterMap.ProcessStateFilter(value)
        case 'processOriginStatus':
          return FilterMap.ProcessOriginalStateFilter(value)
        case 'clusterType':
          return FilterMap.ClusterTypeFilter(value)
        case 'infraHealth':
          return FilterMap.HealthStatusFilter(value)
        case 'monitorType':
          return FilterMap.MonitorTypeFilter(value)
        case 'monitorMethod':
          return FilterMap.MonitorMethodFilter(value)
        case 'noticeMethod':
          return FilterMap.NoticeMethodFilter(value)
        case 'noticeResult':
          return FilterMap.NoticeResultFilter(value)
        case 'serviceType':
          return FilterMap.ServiceTypeFilter(value)
        case 'serviceRequestType':
          return FilterMap.RequestTypeFilter(value)
        case 'serviceAnalysisType':
          return FilterMap.ServiceAnalysisFilter(value)
        case 'spanStatus':
          return FilterMap.SpanStatusFilter(value)
        case 'firstLetterCapital':
          return FilterMap.FirstLetterCapital(value)
        default:
          return value
      }
    },
  }
})
export default class CardPanel extends Vue {
  @Prop({}) private detail!: any;
  // 复制
  private async created () {
    //
  }
}
</script>

<style lang="scss" scoped>
.card-cont{
  height: 172px;
  position: relative;
  &:hover {
    box-shadow: 0px 2px 10px 0px rgba(119, 122, 126, 0.12);
  }
}
.card-header {
  height: 48px;
  &[data-status="normal"] {
    background: rgba(8, 190, 126, 0.06);
  }
  &[data-status="warning"] {
    background: rgba(247, 149, 50, 0.06);
  }
  &[data-status="error"] {
    background: rgba(225, 40, 40, 0.06);
  }
}
.alarm-status {
  padding: 2px 4px;
  &[data-status="normal"] {
    background: rgba(8, 190, 126, 0.12);
    color: rgba(8, 190, 126, 1);
  }
  &[data-status="warning"] {
    background: rgba(247, 149, 50, 0.12);
    color: rgba(247, 149, 50, 1);
  }
  &[data-status="error"] {
    background: rgba(225, 40, 40, 0.12);
    color: rgba(225, 40, 40, 1);
  }
}
.rate-circle {
  width: 76px;
  height: 76px;
  &[data-status="normal"] {
    background: rgba(8, 190, 126, 1);
  }
  &[data-status="warning"] {
    background: rgba(247, 149, 50, 1);
  }
  &[data-status="error"] {
    background: rgba(225, 40, 40, 1);
  }
}
.lh-14 {
  line-height: 14px;
}
.mw-80 {
  max-width: 85px;
}
.mw-50p {
  max-width: 50%;
}

</style>