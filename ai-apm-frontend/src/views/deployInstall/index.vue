<template>
  <div class="install-config-wrapper">
    <div class="install-config flex-v">
      <div class="install-config-header">
        <db-tabnav
          v-model="activeName"
          :tabnavs="tabs"
          @on-change="toggleTabHandle" />
      </div>

      <component :is="activeName" class="tabs-pane-wrap" />
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import Apm from './apm/index.vue';
import OtelCollector from './otelCollector/index.vue';
import OneAgent from './oneAgent/index.vue';
import Log from './log/index.vue';

@Component({
  components: {
    apm: Apm,
    otelCollector: OtelCollector,
    oneAgent: OneAgent,
    log: Log,
  },
})
export default class DeployInstall extends Vue {
  private tabs = [
    { label: 'APM', value: 'apm' },
    { label: i18n.t('modules.views.deployInstall.s_0d57e617') as string, labelKey: 'modules.views.deployInstall.s_0d57e617', value: 'otelCollector' },
    { label: 'OneAgent', value: 'oneAgent' },
    { label: i18n.t('modules.utils.static.s_456d29ef') as string, labelKey: 'modules.utils.static.s_456d29ef', value: 'log' },
  ]
  private activeName: 'apm' | 'otelCollector' | 'oneAgent' | 'log' = 'apm'

  @Watch('$route', { immediate: true, deep: true })
  private onRouteChange (to: any, from: any) {
    if (!from || to.path === from.path) {
      const type: any = to.query.type
      const tabs = this.tabs.map(t => t.value)
      if (tabs.includes(type)) {
        this.activeName = type
      }
    }
  }

  private async created() {
    const type: any = this.$route.query.type
    if (this.tabs.find(t => t.value === type)) {
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
.install-config-wrapper {
  flex: 1;
  padding: 16px;
  position: relative;
  overflow: auto;

  .install-config {
    height: 100%;
    min-height: 400px;
    background-color: var(--bg-color);
    border-radius: 4px;
  }

  .install-config-header {
    padding: 20px 20px 16px;
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
  }

  .tabs-pane-wrap {
    flex: 1;
    padding: 0 20px 20px;
    overflow: auto;
  }
}
</style>
