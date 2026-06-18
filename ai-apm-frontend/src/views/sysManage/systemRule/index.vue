<template>
  <div class="system-rule-wrapper">
    <div class="system-rule-content">
      <rule-list type="system" />
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import RuleList from '@/views/configManage/alarm/rule/index.vue';

@Component({
  components: {
    RuleList,
  }
})
export default class SystemRule extends Vue {}
</script>

<style lang="scss" scoped>
.system-rule-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .system-rule-content {
    flex: 1;
    min-height: 300px;
    padding: 20px;
    border-radius: 4px;
    background-color: var(--bg-color);
    display: flex;
    flex-direction: column;
  }
}
</style>
