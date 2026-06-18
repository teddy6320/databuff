<template>
  <coming-soon :page-name="$t('modules.views.aiMonitor.modelCalls.s_4f681627')" />
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import ComingSoon from '@/components/coming-soon/index.vue';

@Component({ components: { ComingSoon } })
export default class AiModelCallsPage extends Vue {}
</script>
