<template>
  <div class="auto-refresh-cont flex-h ml-10">
    <div class="auto-refresh-wrapper flex-h">
      <el-tooltip :content="$t('modules.views.aiPlatform.chat.s_694fc5ef')" effect="light">
        <span class="manual-refresh-btn cp" @click="immediateRefresh">
          <i ref='manualRefreshIcon' class="manual-refresh-icon db-icon-refresh"></i>
        </span>
      </el-tooltip>

      <el-popover placement="bottom-end" popper-class="auto-refresh-popover" :visible-arrow='false' width="51">
        <el-tooltip :content="$t('modules.views.layout.s_891db237')" slot="reference" effect="light">
          <span class="auto-refresh-trigger cp">
            <span v-show='dateShowValue' class="auto-refresh-trigger-value mr-5">{{ dateShowValue }}</span>
            <span class="el-icon-arrow-down font-14"></span>
          </span>
        </el-tooltip>
        <div class="auto-refresh-panel">
          <div class="auto-refresh-selector">
            <div v-for='option in refreshTimes' :key='option.value'
              @click="chooseOptionHandle(option)"
              class="auto-refresh-selector-option">
              {{ option.labelKey ? $t(option.labelKey) : option.label }}
            </div>
          </div>
        </div>
      </el-popover>

      <span class="hidden-span" ref='hiddenSpan'></span>
    </div>
  </div>
</template>

<script lang='ts'>
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Getter, namespace } from 'vuex-class';
import { FullPropMenu } from '@/router/route.types';

const UserModel = namespace('User');

const RefreshTimes = [
  { label: i18n.t('modules.views.dataReport.report.s_b15d9127') as string, labelKey: 'modules.views.alarmCenter.alarm.s_b15d9127', value: 0 },
  { label: '5s', value: 5 },
  { label: '10s', value: 10 },
  { label: '30s', value: 30 },
  { label: '1m', value: 60 },
  { label: '5m', value: 300 },
  { label: '15m', value: 900 },
  { label: '30m', value: 1800 },
  { label: '1h', value: 3600 },
  { label: '2h', value: 7200 },
  { label: '1d', value: 3600 * 24 },
]

// 需要限制刷新时间的路由配置
const RefreshLimits = [
  {
    // 最小刷新时间30s
    minRefresh: 30,
    menu: ['/cockpit']
  },
]

@Component
export default class DbFooterAutoRefresh extends Vue {
  @Getter('globalTime') private globalTimeFunc!: any;
  @Getter('refresh') public globalRefresh!: number;
  @Getter('refreshPause') public globalRefreshPause!: boolean;
  @UserModel.State private currMenu!: FullPropMenu | null;

  @Prop({ default: '' }) private refreshId!: string;

  public $refs!: {
    hiddenSpan: HTMLSpanElement;
    manualRefreshIcon: HTMLSpanElement;
  }

  get isCustomGlobalTime () {
    return this.globalTimeFunc().type === 'custom'
  }

  @Watch('$route', { immediate: true })
  private onRouterChange (to: any, from: any) {
    if (this.refreshId) {
      // 自定义刷新组件，不监听路由
      return;
    }
    const refreshAble = !!(this.currMenu || {}).refresh
    const refreshItem = this.refreshTimes.find(t => t.value === this.interval)
    if ((this.interval && (!refreshAble || !refreshItem)) ||
        !from ||
        (to.path !== from.path && this.isCustomGlobalTime)
    ) {
      this.chooseOptionHandle(this.refreshTimes[0])
    }
  }

  @Watch('globalRefresh')
  private onGlobalRefreshChange (val: number) {
    if (val !== this.interval) {
      const refreshItem = this.refreshTimes.find(t => t.value === val)
      if (refreshItem) {
        this.chooseOptionHandle(refreshItem)
      }
    }
  }

  @Watch('globalRefreshPause')
  private onGlobalRefreshPauseChange (val: boolean) {
    if (this.refreshId) {
      // 自定义刷新组件，不监听暂停
      return;
    }
    if (val && this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    } else if (!val && this.interval > 0) {
      this.loopUpdateTime()
    }
  }

  // 定时器相关
  private timer: any = null;
  private animateTimer: any = null;

  // 限制快捷时间选项
  get refreshTimes () {
    const { path } = this.$route
    const matchLimitRoute = RefreshLimits.find((obj) => obj.menu.indexOf(path) !== -1)
    if (matchLimitRoute) {
      return RefreshTimes.filter((item) => !item.value || item.value >= matchLimitRoute.minRefresh)
    } else {
      return RefreshTimes
    }
  }

  private dateShowValue = '';
  private interval = 0;

  private created () {
    const localInterval = window.localStorage.getItem('DATABUFF_AUTO_INTERVAL')
    if (localInterval) {
      const option = this.refreshTimes.find(item => `${item.value}` === localInterval)
      if (option) {
        this.dateShowValue = option.value ? option.label : ''
        this.interval = option.value
        this.$store.commit('SET_REFRESH', this.interval);
      }
    }
  }

  private mounted () {
    if (this.interval > 0) {
      this.loopUpdateTime()
    }
  }

  private beforeDestroy() {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
  }

  private chooseOptionHandle (option: any) {
    this.dateShowValue = option.value ? option.label : ''
    this.interval = option.value
    this.$store.commit('SET_REFRESH', this.interval);
    if (option.value) {
      this.loopUpdateTime();
    } else {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    window.localStorage.setItem('DATABUFF_AUTO_INTERVAL', `${option.value}`)
    // 本地记录刷新频率
    // 点击隐藏的span按钮，关闭popover
    if (this.$refs.hiddenSpan) {
      this.$refs.hiddenSpan.click();
    }
  }

  // 定时刷新
  private loopUpdateTime () {
    if (!this.interval || (this.globalRefreshPause && !this.refreshId)) {
      return
    }
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    this.timer = setTimeout(() => {
      this.immediateRefresh();
      this.loopUpdateTime();
    }, 1000 * this.interval)
  }

  private immediateRefresh () {
    if (window.axiosCancel.length !== 0) {
      for (const func of window.axiosCancel) {
        setTimeout(func('interrupt'), 0);
      }
      window.axiosCancel = [];
    }
    this.$refs.manualRefreshIcon.className += ' active-once';
    this.animateTimer = setTimeout(() => {
      if (this.$refs.manualRefreshIcon) {
        this.$refs.manualRefreshIcon.className = this.$refs.manualRefreshIcon.className.replace('active-once', '');
      }
    }, 2600)
    this.$eventBus.$emit(this.refreshId || 'GlobalRefresh');
  }
}
</script>

<style lang='scss' scoped>
.auto-refresh-cont {
  height: 32px;
  justify-content: flex-end;
  align-items: center;

  .auto-refresh-wrapper {
    .auto-refresh-trigger,
    .manual-refresh-btn{
      padding: 0 4px;
      height: 32px;
      line-height: 30px;
      display: flex;
      align-items: center;
      background-color: transparent;
      color: var(--color-text-regular);
      transition: all .3s ease;
      position: relative;
      border-radius: 4px;

      .manual-refresh-icon {
        font-size: 14px;
      }

      &:hover {
        background-color: #1c2730;
        border-color: #1b3a56;
        color: #fff;
        z-index: 1;
      }
    }
    .auto-refresh-trigger {
      margin-left: -1px;
    }
    .manual-refresh-icon.active-once {
      animation: rotateCycle 2.5s linear 1 both;
    }
  }
}

.auto-refresh-panel {
  width: 50px;
  height: auto;

  .auto-refresh-selector {
    width: 100%;
  }
  .auto-refresh-selector-option {
    width: 100%;
    overflow: hidden;
    padding: 7px 9px;
    transition: all .3s ease;
    cursor: pointer;

    &.active, &:hover {
      background-color: var(--background-color-base);
    }
  }
  .hidden-span {
    opacity: 0;
    width: 0;
    height: 0;
    font-size: 0;
    user-select: none;
  }
}

:root[data-theme=light] {
  .auto-refresh-cont .auto-refresh-wrapper {
    .auto-refresh-trigger:hover,
    .manual-refresh-btn:hover{
      color: var(--color-primary);
      background: #e8f2fc;
      border-color: #a2cbf1;
    }
  }
}

</style>

<style lang="scss">
.el-popover.el-popper.auto-refresh-popover {
  margin-top: 5px;
  border-radius: 2px;
  padding: 0;
  min-width: 50px;
}
@keyframes rotateCycle {
  from {
    transform: rotateZ(0);
  }
  to {
    transform: rotateZ(360deg);
  }
}
</style>
