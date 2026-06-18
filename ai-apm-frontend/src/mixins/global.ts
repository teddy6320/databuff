import { Component, Vue } from 'vue-property-decorator';
import { State, Getter, namespace } from 'vuex-class';
import { GlobalTime, GlobalTimeV2 } from '@/store/modules/global'

interface TimeParams {
  fromTime: string;
  toTime: string;
}
interface TimeRangeParams {
  durationRange: number;
}

export type TimeOrRange = TimeParams | TimeRangeParams | {};


@Component({})
export default class GlobalTimerMixin extends Vue {
  // 使用 vuex-class 注入状态和 getter
  @Getter('durationRange') public globalDurationRange!: number;
  @Getter('globalTime') public getGlobalTime!: () => GlobalTime;
  @Getter('globalTimeV2') public getGlobalTimeV2!: () => GlobalTimeV2;
  @Getter('User/getIsAdmin') public getIsAdmin!: boolean;
  @Getter('User/getGroupEnabled') public getGroupEnabled!: boolean;
  @Getter('User/getHasAlarmManageAuth') public getHasAlarmManageAuth!: boolean;
  @Getter('User/getCurrGroup') public getCurrGroup!: any[];

  public get globalTimeV2 () {
    return this.getGlobalTime();
  }

  public get getRouteTimeOrRange () {
    const { durationRange, fromTime, toTime } = this.$route.query
    const query: any = fromTime && toTime ? { fromTime, toTime } : durationRange ? { durationRange } : {}
    return {...query}
  }

  public get isAdmin () {
    return this.getIsAdmin;
  }

  public get hasEntityManageAuth () {
    return this.getGroupEnabled ? this.isAdmin : true;
  }

  public get hasAlarmManageAuthV1 () {
    return this.getHasAlarmManageAuth;
  }

  public get hasAlarmManageAuth () {
    return (payload: any) => {
      if (this.isAdmin) {
        return true
      } else {
        if (!this.getGroupEnabled) {
          return true
        } else {
          const hasEdit = this.getCurrGroup.some(g => g.configAuth && String(g.gid) === String(payload.gid));
          return hasEdit;
        }
      }
    }
  }
}
