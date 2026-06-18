<template>
  <div class="ai-stat-pill">
    <div class="stat-label">{{ label }}</div>
    <div :class="['stat-value', { accent }]">{{ value }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';

@Component
export default class AiStatPill extends Vue {
  @Prop({ required: true }) private label!: string;
  @Prop({ required: true }) private value!: string | number;
  @Prop({ default: false }) private accent!: boolean;
}
</script>

<style lang="scss" scoped>
.ai-stat-pill {
  position: relative;
  min-width: 108px;
  padding: 10px 12px 9px;
  border: 1px solid rgba(222, 229, 242, 0.78);
  border-radius: 10px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 251, 255, 0.9)),
    #fff;
  box-shadow: 0 8px 18px rgba(43, 55, 86, 0.06);
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    right: -18px;
    top: -18px;
    width: 56px;
    height: 56px;
    border-radius: 999px;
    background: rgba(41, 98, 255, 0.1);
  }
}

.stat-label {
  font-size: 12px;
  font-weight: 600;
  color: #74839a;
  line-height: 1.2;
}

.stat-value {
  position: relative;
  margin-top: 5px;
  font-size: 22px;
  font-weight: 800;
  line-height: 1.2;
  color: #121826;

  &.accent {
    color: #1f5eff;
  }
}

@media (max-width: 960px) {
  .ai-stat-pill {
    min-width: 108px;
  }
}
</style>
