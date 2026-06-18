declare module '*.vue' {
  import Vue from 'vue';
  export default Vue;
}

declare module 'vue-i18n';

declare module '@/i18n/generated/manifest.json' {
  const value: Record<string, string>;
  export default value;
}

declare module 'element-ui/lib/locale';
declare module 'element-ui/lib/locale/lang/zh-CN';
declare module 'element-ui/lib/locale/lang/en';

declare module 'js-cookie';
declare module 'md5';
declare module 'human-format';
declare module '@/components/flame-chart-js';
declare module 'splitpanes';
declare module 'simplebar-vue';
declare module 'vue-codemirror';
