import Vue from 'vue';
import 'core-js/stable';
import 'regenerator-runtime/runtime';
import clickout from '@/utils/clickout';
import eventBus from './event-bus';
import App from './App.vue';
import router from './router/index';
import store from './store';
import components from './components';
import GlobalFilters from '@/utils/filters'
import echarts from '@/utils/echarts'

// mixin
import GlobalTimerMixin from '@/mixins/global'

// 引入由 vite-plugin-svg-icons 注册的虚拟模块，挂载 svg sprite
// virtual module provided by vite-plugin-svg-icons; suppress TS error for the virtual import
// @ts-ignore
import 'virtual:svg-icons-register';
import Element from 'element-ui';
import locale from 'element-ui/lib/locale';
import elementZh from 'element-ui/lib/locale/lang/zh-CN';
import elementEn from 'element-ui/lib/locale/lang/en';
import i18n, { defaultLocale, setAppLocale } from '@/i18n';

import './assets/styles/reset.scss';
import './assets/styles/themes/theme-variables.scss';
import './assets/styles/themes/element-cover.scss';
import 'simplebar/dist/simplebar.min.css';
import './assets/fonts/db-iconfont.css';

import './assets/styles/lib.scss';
import './assets/styles/grid.scss';
import './assets/styles/common.scss';

Vue.config.silent = true
setAppLocale(defaultLocale);
locale.use(defaultLocale === 'zh-CN' ? elementZh : elementEn);
Vue.use(Element, {
  i18n: (key: string, value: string) => i18n.t(key, value),
});
Vue.use(eventBus);
Vue.use(components);

Vue.mixin(GlobalTimerMixin);


Vue.directive('clickout', clickout);

Object.entries(GlobalFilters).forEach(([key, func]) => {
  Vue.filter(key, func)
})

Vue.prototype.$echarts = echarts;

if (!window.Promise) {
  window.Promise = Promise;
}

Vue.config.productionTip = false;

export default new Vue({
  router,
  store,
  i18n,
  render: (h) => h(App),
}).$mount('#app');
