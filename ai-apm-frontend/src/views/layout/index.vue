<template>
  <div v-if="showHeader || showFooter" class="layout-wrapper scroll_bar_style">
    <DbHeader v-if="showHeader" @reloadFooter="reloadFooter" />
    <div class="app-container scroll_bar_style">
      <DbFooter ref="footer" v-if='showFooter' />
      <div v-if='showTimeInfo' class="app-time-info">
        <span class="el-icon-warning warning-icon mr-5"></span>
        <p class="app-time-info-describe">
          {{ timeSyncMessage }}
        </p>
        <span @click="clearTimeInfoHandle" class="ml-5">
          <span class="el-icon-error cp"></span>
        </span>
      </div>
      <el-tag
        v-if="showBrowserMsg"
        @close="hideUpdateBrowser"
        type="danger" closable
        class="browser-msg">{{ $t('layout.browserOutdated') }}</el-tag>
      <router-view></router-view>
    </div>
  </div>

  <router-view v-else></router-view>
</template>

<script lang="ts">import i18n from '@/i18n';

  import { Component, Vue, Watch } from 'vue-property-decorator';
  import DbHeader from './db-header.vue';
  import DbFooter from './db-footer.vue';
  import axiosHttp from '@/utils/axios';
  import { namespace } from 'vuex-class';
  import { FullPropMenu } from '@/router/route.types';
  import { isUpgradeBrowser } from '@/utils/browserVersion';

  // 浏览器最低版本
  const browserList = [
    { browser: 'Chrome', version: 84 },
    { browser: 'Edge', version: 84 },
    { browser: 'Firefox', version: 78 },
    { browser: 'Opera', version: 72 },
    { browser: 'Safari', version: 14 },
  ];

  const UserModel = namespace('User');

  @Component({
    components: {
      DbFooter,
      DbHeader,
    },
  })
  export default class Layout extends Vue {
    @UserModel.State private menus!: FullPropMenu[];
    @UserModel.State private currMenu!: FullPropMenu | null;
    @UserModel.Mutation('SET_CURR_MENU') private setCurrMenu!: (currMenu: FullPropMenu | null) => void;

    get productNameEn () {
      return this.$store.getters['User/getLogoConfig']?.productNameEn || '';
    }

    get timeSyncMessage () {
      return i18n.t('layout.timeSyncWarning', {
        hosts: this.showTimeExtraInfo,
        product: this.productNameEn,
      });
    }

    public reloadFooter(timeArray: Date[]): void {
      const footer: any = this.$refs.footer;
      footer.time = timeArray;
    }

    @Watch('$route', { immediate: true })
    private onRouterChange (newVal: any, oldVal: any) {
      const currMenu = this.menus.find(t => t.path === newVal.path) || null
      if (!oldVal || newVal.path !== oldVal.path) {
        this.setCurrMenu(currMenu);
      }
      if (!this.showTimeInfo) {
        return
      }
      this.getList()
    }

    get showHeader () {
      return this.currMenu && !this.currMenu.noHeader
    }
    get showFooter() {
      return this.currMenu && !this.currMenu.noFooter
    }

    private showTimeInfo = false;
    private showTimeExtraInfo = '';

    private created () {
      // const sessionShow = sessionStorage.getItem('DB_TIMEINFO_SHOW')
      // if (sessionShow === 'false') {
      //   return
      // }
      // TODO 获取agent列表
      // this.getList()
      // TODO 获取服务时间
      this.checkBrowser()
    }

    private clearTimeInfoHandle () {
      this.showTimeInfo = false;
      // 本次登录不再提示
      sessionStorage.setItem('DB_TIMEINFO_SHOW', 'false')
    }

    private getList() {
      const params = {
        timeDiff: 30
      };
      axiosHttp.post('/agent/timeDiffList ', params)
        .then((rst: any) => {
          if (rst.status === 200 && rst.message.toLocaleLowerCase() === 'success' && rst.data) {
            const { data = [], total } = rst;
            const _list = data
            if (_list && _list.length > 0) {
              this.showTimeInfo = true
              const [first] = _list
              const targetStr = ` ( ${first.hostName}${_list.length > 1 ? '...' : ''} ) `
              this.showTimeExtraInfo = targetStr
            } else {
              this.showTimeInfo = false
              this.showTimeExtraInfo = ''
            }
          }
        })
        .catch(error => {
          //
        })
    }

    private showBrowserMsg = false
    private checkBrowser () {
      const isUpdate = isUpgradeBrowser(browserList).isUpdate || !String.prototype.replaceAll
      if (isUpdate) {
        let showBrowserMsg = true
        if (window && window.localStorage) {
          const _browserStr = window.localStorage.getItem('UpdateBrowser')
          try {
            const _browser = JSON.parse(_browserStr || '{}')
            const expiration = +(_browser.expiration || '')
            showBrowserMsg = !(expiration > Date.now())
          } catch (err) {
            console.log(err)
          }
        }
        this.showBrowserMsg = showBrowserMsg
      }
    }
    private hideUpdateBrowser () {
      this.showBrowserMsg = false
      if (window && window.localStorage) {
        window.localStorage.setItem('UpdateBrowser', JSON.stringify({
          isUpdate: true,
          expiration: Date.now() + (24 * 60 * 60 * 1000), // 1 天后过期
        }))
      }
    }
  }
</script>

<style lang="scss" scoped>
  .layout-wrapper {
    width: 100%;
    height: 100%;
    overflow: auto;
    display: flex;
    /* flex-direction: column; */
    justify-content: space-between;
  }

  .app-container {
    flex: 1;
    height: 100%;
    display: flex;
    flex-direction: column;
    background-color: var(--bg-color-base);
    color: var(--color-text-primary);
    overflow: auto;

    & > * {
      min-width: 1200px;
    }

    .app-time-info {
      display: flex;
      padding: 8px 16px;
      align-items: flex-start;
      background: var(--bg-color);
      box-shadow: inset 0 1px 4px #141414;
      z-index: 99;

      .app-time-info-describe {
        margin: 0;
        flex: 1;
      }

      .link-text-plain {
        font-size: 13px !important;
      }

      .warning-icon {
        font-size: 18px;
        color: var(--color-danger);
      }
      
      .info-icon {
        margin-top: -2px;
      }
    }

    .browser-msg {
      min-width: auto;
      font-size: 14px;
      transform: translate(-50%, 0);
      position: absolute;
      left: 50%;
      top: 9px;
      z-index: 100;
    }
  }
</style>
