<template>
  <el-button
    @click="toggleThemeHandle"
    size='mini'
    :class="['theme-switch-btn', { 'dark-check': themeName !== 'light' }]">
    <span class="db-icon-sun switch-icon light"></span>
    <span class="db-icon-moon switch-icon dark"></span>
  </el-button>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { State } from 'vuex-class';

@Component
export default class ThemeSwitch extends Vue {
  @State('theme') private theme!: 'dark' | 'light';

  private themeName: 'dark' | 'light' = 'dark';

  private created() {
    this.themeName = this.theme;
  }

  private toggleThemeHandle () {
    this.themeName = this.themeName === 'dark' ? 'light' : 'dark'
    this.$store.commit('UPDATE_THEME', this.themeName);
    this.$store.commit('UPDATE_THEME_CHANGED', true);
    document.documentElement.setAttribute('data-theme', this.themeName)
    window.localStorage.setItem('DATABUFF_THEME', this.themeName)
    // this.loadElementUIThemeLink()
  }

  private loadElementUIThemeLink () {
    const $link = document.querySelector('link#elementUIThemeLink') as HTMLLinkElement
    $link && ($link.href = `/css/element-${this.themeName}.css`);
  }


}
</script>

<style lang="scss" scoped>
.theme-switch-btn {
  width: 36px;
  height: 32px;
  line-height: 30px;
  cursor: pointer;
  position: relative;

  .switch-icon {
    font-size: 14px;
    transition: opacity .25s;
    transform: translate(-50%, -50%);
    position: absolute;
    top: 50%;
    left: 50%;
    &.dark {
      opacity: 0;
    }
    &.light {
      opacity: 1;
    }
  }

  &.dark-check {
    .dark {
      opacity: 1;
    }
    .light {
      opacity: 0;
    }
  }
}
</style>
