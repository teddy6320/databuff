<template>
  <coming-soon :page-name="$t('modules.views.aiMonitor.topology.s_af401659')" />
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import ComingSoon from '@/components/coming-soon/index.vue';

@Component({ components: { ComingSoon } })
export default class AiTopologyPage extends Vue {}
</script>
