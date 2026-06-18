<template>
  <footer class="db-footer trans">
    <div class="db-footer-inner">
      <breadcrumb-list class="flex-1 mr-10" />

      <div class="sm flex-h right-tools">
        <!-- 主题切换 -->
        <!-- <theme-switch class="ml-10" /> -->

        <!-- 告警数 -->
        <alarm-badge v-if="showAlarm" class="ml-10" />

        <!-- 刷新控件 -->
        <auto-refresh v-show="showRefresh" />

        <!-- 时间控件 -->
        <footer-time v-show="showTime" />

        <!-- 管理域 -->
        <role-group v-if="showGroup" class="ml-15" />

        <slot></slot>
      </div>
    </div>
  </footer>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { namespace } from 'vuex-class';
import AutoRefresh from './auto-refresh.vue'
import FooterTime from './db-footer-time.vue';
import BreadcrumbList from './breadcrumb-list.vue';
// import ThemeSwitch from './theme-switch.vue';
import RoleGroup from './role-group.vue';
import AlarmBadge from './alarm-badge.vue';
import { FullPropMenu } from '@/router/route.types';

const UserModel = namespace('User');

@Component({
  components: {
    AutoRefresh,
    FooterTime,
    BreadcrumbList,
    // ThemeSwitch,
    RoleGroup,
    AlarmBadge,
  }
})
export default class Footer extends Vue {
  @Prop({ default: true }) private showAlarm!: boolean;
  @Prop({ default: false }) private showGroup!: boolean;

  @UserModel.State private currMenu!: FullPropMenu | null;

  public $refs!: {
    alarmBadge: AlarmBadge
  }

  get showRefresh () {
    return this.currMenu && !!this.currMenu.refresh
  }

  get showTime () {
    return this.currMenu && !!this.currMenu.time
  }

  private created() {
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.$refs.alarmBadge && this.$refs.alarmBadge.loopAlarmInfo();
    })
  }

  private beforeDestroy() {
    this.$eventBus.$off('GlobalRefresh')
  }
}
</script>

<style lang='scss' scoped>
.db-footer {
  flex-shrink: 0;
  height: 48px;
  background: var(--bg-color);
  position: relative;
  z-index: 99;
  &::after {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 1px;
    background: var(--border-color-lighter);
  }
}

.db-footer-inner {
  height: 100%;
  padding: 0 16px 0 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .right-tools {
    height: 100%;
  }
}
</style>
