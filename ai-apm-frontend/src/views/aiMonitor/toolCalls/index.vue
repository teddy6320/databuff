<template>
  <coming-soon :page-name="$t('modules.views.aiMonitor.toolCalls.s_850b4e4d')" />
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import ComingSoon from '@/components/coming-soon/index.vue';

@Component({ components: { ComingSoon } })
export default class AiToolCallsPage extends Vue {}
</script>
