<template>
  <coming-soon :page-name="$t('modules.views.aiMonitor.errors.s_b593af50')" />
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import ComingSoon from '@/components/coming-soon/index.vue';

@Component({ components: { ComingSoon } })
export default class AiErrorsPage extends Vue {}
</script>
