<template>
  <div class="detail-baseinfo">
    <div class="flex-h-jc">
      <div class="section-item">
        <div class="section-title ">CPU
          <div class="font-12 fw-400 describe">{{ $t('modules.views.infrastructure.clusterDetail.s_09565e82') }}<span class="default-text">{{ detail.cpuCapacity | NumberFilter }} core</span></div>
        </div>
        <div class="section-cont">
          <horizontal-bar
            :source="cpuSource"
            unit="core"
            :colors="['#2962FF', '#678FFF', '#00AFF4']"
            :itemGap="12"
            valuePosition="top"
            order="none" />
          <!-- :maxValue="detail.cpuCapacity" -->
        </div>
      </div>

      <div class="section-item">
        <div class="section-title">{{ $t('modules.views.appMonitor.hotMethods.s_9932551c') }}
          <div class="font-12 fw-400 describe">{{ $t('modules.views.infrastructure.clusterDetail.s_09565e82') }}<span class="default-text">{{ detail.memCapacity | BytesFilter }}</span></div>
        </div>
        <div class="section-cont">
          <horizontal-bar
            :source="memorySource"
            unit="B"
            :colors="['#2962FF', '#678FFF', '#00AFF4']"
            :itemGap="12"
            valuePosition="top"
            order="none" />
          <!-- :maxValue="detail.memCapacity" -->
        </div>
      </div>

      <div class="section-item">
        <div class="section-title">Node
          <div class="font-12 fw-400 describe">{{ $t('modules.views.infrastructure.clusterDetail.s_09565e82') }}<span class="default-text">{{ detail.nodeCount | NumberFilter }}</span></div>
        </div>
        <div class="section-cont hexmap-section-cont">
          <hexmap-chart
            :total="Math.min(detail.nodeCount || 0, 96)"
            class="hexmap-chart"
          />
        </div>
      </div>

    </div>

    <div class="font-14 line-height-22 fw-500 mt-20 mb-16">{{ $t('modules.views.infrastructure.clusterDetail.s_c5ea2ca1') }}</div>

    <div class="section-item baseinfo-section-item">
      <div class="flex-h wba"><div class="label">{{ $t('modules.views.infrastructure.cluster.s_c3f28b34') }}</div>{{ detail.name || '-' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.clusterDetail.s_c7ed69f8') }}</div>{{ detail.clusterId || '-' }}</div>
      <div class="flex-h mt-20 wba"><div class="label">{{ $t('modules.views.infrastructure.clusterDetail.s_2eb6b3ad') }}</div>{{ k8sVersion || '-' }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import HorizontalBar from '@/components/charts/horizontal-bar.vue';
import HexmapChart from './hexmap.vue';

@Component({
  components: {
    HorizontalBar,
    HexmapChart,
  }
})
export default class DetailBaseinfo extends Vue {
  @Prop({ default: () => ({}) }) private detail!: any;

  get cpuSource () {
    return [
      { name: 'Usage', value: this.detail.cpuUsage },
      { name: 'Requests', value: this.detail.cpuRequest },
      { name: 'Limits', value: this.detail.cpuLimit },
    ]
  }

  get memorySource () {
    return [
      { name: 'Usage', value: this.detail.memUsage },
      { name: 'Requests', value: this.detail.memRequest },
      { name: 'Limits', value: this.detail.memLimit },
    ]
  }

  get k8sVersion () {
    const versions = this.detail.kubeletVersions || {};
    return Object.keys(versions)[0] || '';
  }
}
</script>

<style lang="scss" scoped>
.section-item {
  width: calc((100% - 32px) / 3);
  height: 168px;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;

  .section-title {
    display: flex;
    justify-content: space-between;
    padding: 16px 20px 0;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    color: var(--color-text-primary);
  }

  .section-cont {
    height: calc(100% - 38px);
    padding: 12px 20px 0;

    &.hexmap-section-cont {
      padding-top: 0;
      padding-bottom: 6px;
      .hexmap-chart {
        margin: 0 auto;
        width: 320px;
      }
    }
  }
}

.baseinfo-section-item {
  width: 100%;
  height: auto;
  padding: 20px;
  color: var(--color-text-primary);
  .flex-h {
    align-items: flex-start;
  }
  .label {
    width: 160px;
    flex: none;
  }
}
</style>
