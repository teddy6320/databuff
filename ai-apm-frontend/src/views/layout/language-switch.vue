<template>
  <el-dropdown trigger="click" @command="switchLocaleHandle" class="language-switch">
    <el-button size="mini" class="language-switch-btn">
      <span class="db-icon-earth language-icon"></span>
      <span class="language-label">{{ currentLabel }}</span>
    </el-button>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item
        v-for="item in localeOptions"
        :key="item.value"
        :command="item.value"
        :class="{ 'is-active': locale === item.value }">
        {{ $t(item.labelKey) }}
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script lang="ts">
import i18n from '@/i18n';
import { Vue, Component } from 'vue-property-decorator';
import { State } from 'vuex-class';
import {
  AppLocale,
  LOCALE_OPTIONS,
  setAppLocale,
} from '@/i18n';
import locale from 'element-ui/lib/locale';
import elementZh from 'element-ui/lib/locale/lang/zh-CN';
import elementEn from 'element-ui/lib/locale/lang/en';

@Component
export default class LanguageSwitch extends Vue {
  @State('locale') private locale!: AppLocale;

  private localeOptions = LOCALE_OPTIONS;

  get currentLabel() {
    const item = this.localeOptions.find((t) => t.value === this.locale);
    return item ? i18n.t(item.labelKey) : this.locale;
  }

  private switchLocaleHandle(next: AppLocale) {
    if (next === this.locale) {
      return;
    }
    this.$store.commit('UPDATE_LOCALE', next);
    setAppLocale(next);
    locale.use(next === 'zh-CN' ? elementZh : elementEn);
  }
}
</script>

<style lang="scss" scoped>
.language-switch-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 32px;
  padding: 0 10px;
  border-color: var(--border-color-base);
  background: var(--bg-color);
  color: var(--color-text-regular);

  .language-icon {
    font-size: 14px;
  }

  .language-label {
    font-size: 12px;
    max-width: 72px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>

<style lang="scss">
.el-dropdown-menu__item.is-active {
  color: var(--color-primary);
  font-weight: 500;
}
</style>
