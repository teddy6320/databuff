import Vue, { VNode } from 'vue';
import { AxiosInstance } from 'axios';
import { EventBus } from '@/event-bus';
import { GlobalTime, GlobalTimeV2, TimeOrRange } from '@/store/modules/global'
import echarts from '@/utils/echarts'


declare global {
  namespace JSX {
    // tslint:disable no-empty-interface
    interface Element extends VNode {}
    // tslint:disable no-empty-interface
    interface ElementClass extends Vue {}
    interface IntrinsicElements {
      // [elem: string]: any;
    }
  }
  interface Window {
    Promise: any;
    moment: any;
    axiosCancel: any;
  }

  namespace Common {
    interface LabelValue<T = string | number | boolean> {
      label: string;
      value: T;
      disabled?: boolean;
      hidden?: boolean;
      desc?: string;
      [key: string]: any;
    }

    interface NestLabelValue extends LabelValue {
      children?: NestLabelValue[];
    }
  }

  namespace APM {
    interface RelationMap {
      relationType: 'business' | 'application' | 'service' | 'process' | 'host';
    }
  }
}

declare module 'vue/types/vue' {
  interface Vue {
    $http: AxiosInstance;
    $eventBus: EventBus;
    $echarts: echarts;
    globalDurationRange: number;
    getGlobalTime: () => GlobalTime;
    getGlobalTimeV2: () => GlobalTimeV2;
    getRouteTimeOrRange: () => TimeOrRange;
    isAdmin: boolean;
    hasEntityManageAuth: boolean;
    hasAlarmManageAuthV1: boolean;
    hasAlarmManageAuth: (payload: any) => boolean;
  }
}
