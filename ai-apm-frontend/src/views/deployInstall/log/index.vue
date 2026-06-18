<template>
  <div class="deploy-tab-coming-soon">
    <coming-soon :page-name="$t('modules.utils.static.s_456d29ef')" />
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import ComingSoon from '@/components/coming-soon/index.vue';

@Component({
  components: { ComingSoon },
})
export default class DeployLog extends Vue {}
</script>

<style lang="scss" scoped>
.deploy-tab-coming-soon :deep(.coming-soon-wrapper) {
  min-height: 360px;
}
</style>
