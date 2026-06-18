<template>
  <el-badge
    :value="alarmCount"
    :hidden="!hasAlarmInfo"
    :max="99"
    :class="['alarm-badge', { max: alarmCount > 99 }]">
    <span
      @click="viewAlarmHandle"
      :class="{ 'no-alarm': !hasAlarmInfo }"
      class="db-icon-news alarm-info-btn cp"></span>
  </el-badge>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Getter } from 'vuex-class';
import dayjs from 'dayjs';
import { toAsyncWait } from '@/utils/common';
import AlarmApi from '@/api/alarm'

@Component
export default class AlarmBadge extends Vue {
  @Getter('globalTime') private globalTimeFunc!: any;

  get hasAlarmInfo () {
    return this.alarmCount > 0
  }
  private alarmCount = 0;

  get globalTime () {
    return this.getGlobalTimeV2()
  }
  @Watch('globalTime', { deep: true })
  private watchGlobalTime() {
    if (this.firstLoadAlarm) {
      this.firstLoadAlarm = false;
      return;
    }
    this.loopAlarmInfo();
  }

  // 定时器
  private loopTimer: any = null;

  private firstLoadAlarm = true;

  private created() {
    this.loopAlarmInfo();
    this.$eventBus.$on('AlarmInfoRefresh', this, () => {
      this.loopAlarmInfo();
    })
  }

  private beforeDestroy() {
    this.$eventBus.$off('AlarmInfoRefresh')
    if (this.loopTimer) {
      window.clearTimeout(this.loopTimer);
      this.loopTimer = null;
    }
  }

  private async getAlarmInfoData () {
    const { fromTime, toTime } = this.globalTimeFunc()
    const params: any = {
      fromTime: dayjs(fromTime).format('YYYY-MM-DD HH:mm:ss'),
      toTime: dayjs(toTime).format('YYYY-MM-DD HH:mm:ss'),
    }
    const { result, error } = await toAsyncWait(AlarmApi.getAlarmCount(params))
    if (!error) {
      this.alarmCount = result.data || 0
    }
  }

  public loopAlarmInfo () {
    this.getAlarmInfoData()
    if (this.loopTimer) {
      window.clearTimeout(this.loopTimer);
      this.loopTimer = null;
    }
    this.loopTimer = setTimeout(() => {
      this.loopAlarmInfo()
    }, 60000)
  }

  private viewAlarmHandle () {
    if (!this.hasAlarmInfo) {
      return
    }
    this.$router.push({
      path: '/alarmCenter/alarm',
    })
    if (this.$route.path === '/alarmCenter/alarm') {
      this.$eventBus.$emit('AlarmInfoStatusChange', '0')
    }
  }
}
</script>

<style lang="scss" scoped>
.alarm-info-btn {
  padding: 0 4px;
  height: 32px;
  line-height: 30px;
  display: flex;
  align-items: center;
  background-color: transparent;
  color: var(--color-text-regular);
  transition: all 0.3s ease;
  position: relative;
  border-radius: 4px;
  font-size: 14px;
  &:not(.no-alarm):hover {
    color: var(--color-primary);
    background: #e8f2fc;
  }
  &.no-alarm {
    cursor: auto;
  }
}

.alarm-badge {
  :deep(.el-badge__content) {
    background-color: #f56c6c;
    transform: scale(0.65) translateY(-50%) translateX(140%);
  }
  &.max :deep(.el-badge__content) {
    right: 15px;
  }
}
</style>
