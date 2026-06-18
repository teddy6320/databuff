<template>
  <div class="detail-baseinfo">
    <div class="font-14 line-height-22 fw-500 mb-16">{{ $t('modules.views.infrastructure.clusterDetail.s_c5ea2ca1') }}</div>

    <div class="baseinfo-section-item">
      <div class="flex-h wba"><div class="label">{{ $t('modules.views.aiPlatform.experts.s_d7ec2d3f') }}</div>{{ queryParams.workloadName || '-' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">UID</div>{{ queryParams.workloadId || '-' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">Cluster</div>{{ detail.clusterName || '-' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">Namespace</div>{{ detail.namespace || '-' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.aiPlatform.experts.s_226b0912') }}</div>{{ detail.type | ClusterTypeFilter }}</div>
      <div class="flex-h mt-20 wba"><div class="label">Labels</div>{{ (detail.labels || []).join(',') || '-' }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';

@Component
export default class DetailBaseinfo extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: () => ({}) }) private detail!: any;
}
</script>

<style lang="scss" scoped>
.baseinfo-section-item {
  width: 100%;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  padding: 20px;
  color: var(--color-text-primary);
  .flex-h {
    align-items: flex-start;
  }
  .label {
    width: 130px;
    flex: none;
  }
}
</style>
