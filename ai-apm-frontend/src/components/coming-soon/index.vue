<template>
  <div class="coming-soon-wrapper">
    <div class="coming-soon-content">
      <span class="db-icon db-icon-agent coming-soon-icon"></span>
      <h2 class="coming-soon-title">{{ pageName }}</h2>
      <el-tag type="info" effect="plain" class="coming-soon-tag">{{ $t('modules.components.coming-soon.s_2a903e75') }}</el-tag>
      <p class="coming-soon-desc">{{ $t('modules.components.coming-soon.s_d78c88d3') }}</p>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';

@Component
export default class ComingSoon extends Vue {
  @Prop({ default: '' }) private pageName!: string;
}
</script>

<style lang="scss" scoped>
.coming-soon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 120px);
  padding: 40px 20px;
}

.coming-soon-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.coming-soon-icon {
  font-size: 64px;
  color: var(--color-primary, #409eff);
  opacity: 0.6;
  margin-bottom: 24px;
}

.coming-soon-title {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 500;
  color: var(--color-text-primary, #303133);
}

.coming-soon-tag {
  margin-bottom: 12px;
  font-size: 13px;
}

.coming-soon-desc {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-secondary, #909399);
}
</style>
