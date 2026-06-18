import Vue from 'vue';
import Vuex from 'vuex';
import globalState, { State as GlobalState } from './modules/global';
import User, { State as UserState } from '@/store/modules/user/index';
import Common, { CommonState } from '@/store/modules/common';
import Service, { ServiceState } from '@/store/modules/service';
Vue.use(Vuex);

import ThemeVariables from '@/assets/styles/themes/themeVariables';
import type { AppLocale } from '@/i18n';
import { defaultLocale } from '@/i18n';

export interface State {
  globalState: GlobalState;
  User: UserState;
  Common: CommonState;
  Service: ServiceState;
}

export interface BreadcrumbItem {
  path: string;   // 面包屑项路径，必填
  name?: string;  // 面包屑项名称，为空时会显示为router/route-data里的name
  query?: {       // 面包屑项参数
    [key: string]: any
  };
}
export interface BreadcrumbMap {
  [path: string]: BreadcrumbItem
}

interface MainState {
  finalStatus: number|null;
  breadcrumbMap: BreadcrumbMap; // path对应的数据mapping，route-data的补充
  breadcrumbList: BreadcrumbItem[]; // 重设面包屑，会忽略breadcrumb-data的数据
  theme: 'dark' | 'light';
  themeChanged: boolean;
  locale: AppLocale;
}

export default new Vuex.Store<MainState>({
  state: {
    finalStatus: 0,
    breadcrumbMap: {},
    breadcrumbList: [],
    theme: 'dark',
    themeChanged: false,
    locale: defaultLocale,
  },
  mutations: {
    ['UPDATE_AUTH_FINAL_STATUS'] (state, payload) {
      if (typeof payload === 'number' || payload === null) {
        state.finalStatus = payload
      }
    },
    // 更新面包屑，对route-data里的数据补充(name、query)
    // breadcrumb-data找不到相关path时会忽略
    ['UPDATE_BREADCRUMB'] (state, payload: BreadcrumbItem[]) {
      const data: BreadcrumbMap = {};
      payload.forEach((item) => {
        data[item.path] = item;
      });
      state.breadcrumbMap = data;
    },
    // // 重新设置面包屑，设置后会忽略掉breadcrumb-data里的数据
    ['RESET_BREADCRUMB'] (state, payload: BreadcrumbItem[]) {
      state.breadcrumbList = payload;
    },
    // 清空面包屑
    ['CLEAR_BREADCRUMB'] (state) {
      state.breadcrumbMap = {};
      state.breadcrumbList = [];
    },
    ['UPDATE_THEME'] (state, payload: 'dark' | 'light') {
      state.theme = payload
    },
    ['UPDATE_THEME_CHANGED'] (state, payload: boolean) {
      state.themeChanged = payload
    },
    ['UPDATE_LOCALE'] (state, payload: AppLocale) {
      state.locale = payload
    },
  },
  getters: {
    themeVariables: (state) => ThemeVariables[state.theme],
    theme: (state) => state.theme,
  },
  modules: {
    globalState,
    User,
    Common,
    Service,
  },
});
