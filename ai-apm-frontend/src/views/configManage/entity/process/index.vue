<template>
  <div class="process-config-wrap">
    <db-tabnav
      v-model="activeName"
      :tabnavs="tabs"
      @on-change="toggleTabHandle"
      class="tab-nav" />

    <component :is="getComp" class="tab-pane" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import Acquisition from './acquisition.vue';
import Recognition from './recognition.vue';

@Component({
  components: {
    Acquisition,
    Recognition,
  }
})
export default class Process extends Vue {
  private tabs = [
    { label: i18n.t('modules.views.configManage.entity.s_52ede196') as string, labelKey: 'modules.views.configManage.entity.s_52ede196', value: 'acquisition' },
    { label: i18n.t('modules.views.configManage.entity.s_01e9810a') as string, labelKey: 'modules.views.configManage.entity.s_01e9810a', value: 'recognition' },
  ];
  private activeName: 'acquisition' | 'recognition' = 'acquisition';

  get getComp () {
    switch (this.activeName) {
      case 'recognition':
        return 'Recognition';
      default:
        return 'Acquisition';
    }
  }

  private async created () {
    const type: any = this.$route.query.ct
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
.process-config-wrap {
  flex: 1;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  display: flex;
  flex-direction: column;

  .tab-pane {
    margin-top: 16px;
    flex: 1;
    overflow: auto;
  }
}
</style>
