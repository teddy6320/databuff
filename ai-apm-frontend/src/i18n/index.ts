import Vue from 'vue';
import VueI18n from 'vue-i18n';
import elementZh from 'element-ui/lib/locale/lang/zh-CN';
import elementEn from 'element-ui/lib/locale/lang/en';
import zhCN from './locales/zh-CN';
import enUS from './locales/en-US';

export type AppLocale = 'zh-CN' | 'en-US';

export const LOCALE_STORAGE_KEY = 'DATABUFF_LOCALE';

export const LOCALE_OPTIONS: { value: AppLocale; labelKey: string }[] = [
  { value: 'zh-CN', labelKey: 'common.languageZh' },
  { value: 'en-US', labelKey: 'common.languageEn' },
];

export function readStoredLocale(): AppLocale {
  const stored = window.localStorage.getItem(LOCALE_STORAGE_KEY);
  if (stored === 'zh-CN' || stored === 'en-US') {
    return stored;
  }
  return 'zh-CN';
}

export function hasStoredLocale(): boolean {
  const stored = window.localStorage.getItem(LOCALE_STORAGE_KEY);
  return stored === 'zh-CN' || stored === 'en-US';
}

export const defaultLocale: AppLocale = readStoredLocale();

Vue.use(VueI18n);

const i18n = new VueI18n({
  locale: defaultLocale,
  fallbackLocale: 'zh-CN',
  silentTranslationWarn: true,
  messages: {
    'zh-CN': { ...zhCN, ...elementZh },
    'en-US': { ...enUS, ...elementEn },
  } as any,
});

export function setAppLocale(locale: AppLocale) {
  i18n.locale = locale;
  document.documentElement.lang = locale === 'zh-CN' ? 'zh-CN' : 'en';
  window.localStorage.setItem(LOCALE_STORAGE_KEY, locale);
  window.dispatchEvent(new CustomEvent('databuff-locale-change', { detail: locale }));
}

export function menuDisplayName(id: number, fallback: string): string {
  const key = `menu.${id}`;
  if (i18n.te(key)) {
    return i18n.t(key) as string;
  }
  return fallback;
}

export default i18n;
