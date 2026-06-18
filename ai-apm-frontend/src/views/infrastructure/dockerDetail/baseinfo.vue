<template>
  <div class="detail-baseinfo">
    <div class="font-14 line-height-22 fw-500 mb-16">{{ $t('modules.views.infrastructure.clusterDetail.s_c5ea2ca1') }}</div>

    <div class="baseinfo-section-item">
      <div class="flex-h wba"><div class="label">{{ $t('modules.views.infrastructure.dockerDetail.s_a51cd089') }}</div>
        <span>{{ detail.containerName || '-' }}
          <i @click.stop="copyHandle(detail.containerName || '-')" class="db-icon-copy font-12 blue cp"></i>
        </span>
      </div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.appMonitor.serviceFlow.s_3d022a63') }}</div>
        <span
          @click="viewHostDetail"
          :class="{ 'blue hover-active-underline': detail.hostName }">
          <i class="db-icon icon-vm">{{ detail.hostOs | DbIconFilter('host') }}</i>
          {{ detail.hostName || '-' }}</span>
      </div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.docker.s_86cd8dce') }}</div>{{ detail.started | TimesToDateFilter }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.appMonitor.serviceDetail.s_eca37cb0') }}</div>{{ detail.created | TimesToDateFilter }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.dockerDetail.s_332ca50f') }}</div>
        <span>{{ detail.image || '-' }}
          <i @click.stop="copyHandle(detail.image || '-')" class="db-icon-copy font-12 blue cp"></i>
        </span>
      </div>
    </div>

    <div class="font-14 line-height-22 fw-500 mt-20 mb-16">{{ $t('modules.views.metrics.list.s_1f7be0a9') }}</div>

    <div class="baseinfo-tags">
      <div v-for="(item, index) in tags" :key="index" class="tag-item">
        {{ item.labelKey ? $t(item.labelKey) : item.label }}:{{ item.value }}
        <i @click.stop="copyHandle(item.value || '-')" class="db-icon-copy font-12 blue cp copy-icon"></i>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { copy } from '@/utils/common';

@Component
export default class DetailBaseinfo extends Vue {
  @Prop({ default: () => ({}) }) private detail!: any;

  get tags () {
    return (this.detail?.tags || []).sort().map((t: any) => {
      const [label = '', value = ''] = t.split(':')
      return { label, value }
    })
  }

  // 跳转到主机详情
  private viewHostDetail () {
    if (!this.detail.hostName) {
      return
    }
    this.$router.push({
      path: '/infrastructure/hostDetail',
      query: { hostName: encodeURIComponent(this.detail.hostName), }
    })
  }

  private copyHandle (text: string) {
    copy(text)
  }
}
</script>

<style lang="scss" scoped>
.detail-baseinfo {
  height: 100%;
  overflow: auto;
}

.baseinfo-section-item {
  width: 100%;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  padding: 20px;
  color: var(--color-text-primary);
  overflow: auto;
  .flex-h {
    align-items: flex-start;
  }
  .label {
    width: 110px;
    flex: none;
  }
}

.baseinfo-tags {
  display: flex;
  flex-wrap: wrap;
  .tag-item {
    margin: 0 8px 8px 0;
    padding: 5px 8px;
    max-width: calc(100% - 8px);
    height: 32px;
    background: var(--background-color-base);
    border: 1px solid var(--border-color-lighter);
    border-radius: 4px;
    line-height: 20px;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    position: relative;

    .copy-icon {
      display: none;
      width: 20px;
      height: 20px;
      background: var(--background-color-base);
      transform: translate(0, -50%);
      line-height: 20px;
      text-align: center;
      position: absolute;
      top: 50%;
      right: 4px;
    }
    &:hover .copy-icon {
      display: inline-block;
    }
  }
}
</style>
