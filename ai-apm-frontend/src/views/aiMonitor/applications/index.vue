<template>
  <coming-soon :page-name="$t('modules.views.aiMonitor.applications.s_d62637c5')" />
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import ComingSoon from '@/components/coming-soon/index.vue';

@Component({ components: { ComingSoon } })
export default class AiApplicationsPage extends Vue {}
</script>
