<template>
  <coming-soon :page-name="$t('modules.views.deployInstall.status.s_9900dfa2')" />
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import ComingSoon from '@/components/coming-soon/index.vue';

@Component({ components: { ComingSoon } })
export default class DeployStatus extends Vue {}
</script>
