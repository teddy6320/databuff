<template>
  <div class="alarm-config-wrap">
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ $t('modules.views.configManage.alarm.s_74dfe77b') }}</h2>
        <p class="page-desc">{{ $t('modules.views.configInstall.plugin.s_b4c5a9d9') }}</p>
      </div>
    </div>
    <div class="alarm-tab-body">
      <RuleList />
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import RuleList from './rule/index.vue';

@Component({
  components: {
    RuleList,
  },
})
export default class AlarmConfig extends Vue {}
</script>

<style lang="scss" scoped>
.alarm-config-wrap {
  flex: 1;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page-header {
  flex: none;
}

.page-title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.page-desc {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary, #909399);
}

.alarm-tab-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
