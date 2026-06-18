<template>
  <div class="notice-setting-cont">
    <db-tabnav
      v-model="activeName"
      :tabnavs="tabs"
      @on-change="toggleTabHandle"
      class="tab-nav" />

    <component :is="getComp" class="tab-pane" />
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import i18n from '@/i18n';
import EmailPane from './email.vue';
import SmsPane from './sms.vue';
import DingtalkPane from './dingtalk.vue';
import WechatPane from './wechat.vue';
import SocketPane from './socket.vue';

@Component({
  components: {
    EmailPane,
    SmsPane,
    DingtalkPane,
    WechatPane,
    SocketPane,
  }
})
export default class NoticeSetting extends Vue {
  private tabs = [
    { label: i18n.t('modules.views.alarmCenter.notice.s_e9e8054f') as string, labelKey: 'modules.utils.filters.s_e9e8054f', value: 'email' },
    { label: i18n.t('modules.views.alarmCenter.notice.s_485c3abb') as string, labelKey: 'modules.utils.filters.s_485c3abb', value: 'sms' },
    { label: i18n.t('modules.views.alarmCenter.notice.s_4a0e9142') as string, labelKey: 'modules.utils.filters.s_4a0e9142', value: 'dingtalk' },
    { label: i18n.t('modules.views.alarmCenter.notice.s_ff17b9f9') as string, labelKey: 'modules.utils.filters.s_ff17b9f9', value: 'wechat' },
    { label: 'Socket', value: 'socket' },
  ];
  private activeName: 'email' | 'sms' | 'dingtalk' | 'wechat' | 'socket' = 'email';

  get getComp () {
    switch (this.activeName) {
      case 'email':
        return 'EmailPane';
      case 'sms':
        return 'SmsPane';
      case 'dingtalk':
        return 'DingtalkPane';
      case 'wechat':
        return 'WechatPane';
      case 'socket':
        return 'SocketPane';
      default:
        return 'EmailPane';
    }
  }

  private created() {
    const type: any = this.$route.query.type
    const tabnavs = this.tabs.map(t => t.value)
    if (tabnavs.includes(type)) {
      this.activeName = type
    }
  }

  private toggleTabHandle(tab: any) {
    this.activeName = tab.value
    const { type, __ps, __nw } = this.$route.query
    if (tab.value !== type) {
      const query: any = { __ps, __nw, type: tab.value }
      this.$router.replace({ query })
    }
  }
}
</script>

<style lang="scss" scoped>
.notice-setting-cont {
  flex: 1;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  display: flex;
  flex-direction: column;

  .tab-nav {
    margin: 0 0 16px;
  }

  .tab-pane {
    flex: 1;
    overflow: auto;
  }
}
</style>
