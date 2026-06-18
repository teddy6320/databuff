<template>
  <div class="apm-config-wrap">
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
import ApmGlobal from './apmGlobal.vue';
import ApmApp from './apmApp.vue';

@Component({
  components: {
    ApmGlobal,
    ApmApp,
  }
})
export default class ApmConfig extends Vue {
  private tabs = [
    { label: i18n.t('modules.views.configManage.entity.s_8e8aaafe') as string, labelKey: 'modules.views.configManage.entity.s_8e8aaafe', value: 'global' },
    { label: i18n.t('modules.views.configManage.entity.s_d4f806d2') as string, labelKey: 'modules.views.configManage.entity.s_d4f806d2', value: 'app' },
  ];
  private activeName: 'global' | 'app' = 'global';

  get getComp () {
    switch (this.activeName) {
      case 'global':
        return 'ApmGlobal';
      case 'app':
        return 'ApmApp';
      default:
        return 'ApmGlobal';
    }
  }

  private async created () {
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
.apm-config-wrap {
  flex: 1;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  display: flex;
  flex-direction: column;

  .tab-nav {
    margin-bottom: 16px;
  }

  .tab-pane {
    flex: 1;
    overflow: auto;
    background-color: var(--bg-color);
  }
}
</style>
