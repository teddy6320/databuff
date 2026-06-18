<template>
  <coming-soon :page-name="$t('modules.views.aiMonitor.tokens.s_7e1d4c83')" />
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import ComingSoon from '@/components/coming-soon/index.vue';

@Component({ components: { ComingSoon } })
export default class AiTokensPage extends Vue {}
</script>
