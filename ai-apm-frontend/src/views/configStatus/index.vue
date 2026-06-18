<template>
  <div class="status-config-wrapper">
    <div class="status-config flex-v">
      <div class="status-config-header">
        <db-tabnav
          v-model="activeName"
          :tabnavs="tabs"
          :thin="true"
          @on-change="toggleTabHandle" />
      </div>

      <div class="tabs-pane-wrap">
        <component :is="activeName" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import Agent from './agent/index.vue';

@Component({
  components: {
    Agent,
  }
})
export default class AlarmConfig extends Vue {
  private tabs = [
    { label: 'OneAgent', value: 'agent' },
  ]
  private activeName: 'agent' = 'agent'

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
.status-config-wrapper {
  flex: 1;
  overflow: hidden;
  padding: 16px;
  position: relative;

  .status-config {
    height: 100%;
    background-color: var(--bg-color);
  }

  .status-config-header {
    padding: 20px 20px 16px;
  }

  .tabs-pane-wrap {
    flex: 1;
    overflow: hidden;
  }
}
</style>
