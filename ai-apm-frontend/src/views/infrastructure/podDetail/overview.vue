<template>
  <div class="detail-overview">
    <div class="flex-h-jc">
      <div class="section-item">
        <div class="section-title ">CPU
          <!-- <div class="font-12 fw-400 describe">{{ $t('modules.views.infrastructure.clusterDetail.s_09565e82') }}<span class="default-text">{{ detail.cpuCapacity | NumberFilter }} core</span></div> -->
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
          <!-- <div class="font-12 fw-400 describe">{{ $t('modules.views.infrastructure.clusterDetail.s_09565e82') }}<span class="default-text">{{ detail.memCapacity | BytesFilter }}</span></div> -->
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
    </div>

    <db-table
      :columnConfig='containerColumnConfig'
      :data="containerList"
      :total="allContainers.length"
      @on-table-scroll="containerTableScroll"
      ref='containerTable'
      class="table-item">
      <div slot='total' slot-scope="{ total }" class="flex-h lh-20 mb-5">
        <span class="fw-500 mr-8">{{ $t('modules.views.appMonitor.serviceDetail.s_22c79904') }}</span>
        {{ total }}
      </div>
    </db-table>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import HorizontalBar from '@/components/charts/horizontal-bar.vue';

@Component({
  components: {
    HorizontalBar,
  }
})
export default class DetailOverview extends Vue {
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

  private containerColumnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 160 },
    { field: 'state', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', minWidth: 160, },
    { field: 'restartCount', label: i18n.t('modules.views.infrastructure.podDetail.s_60963ed1') as string, labelKey: 'modules.views.infrastructure.podDetail.s_60963ed1', unit: 'count', minWidth: 160, },
  ]
  private containerList: any[] = []

  get allContainers () {
    return this.detail.containerStatuses || [];
  }

  public getData () {
    this.containerList = this.allContainers.slice(0, 50);
  }

  private containerTableScroll () {
    if (this.allContainers.length > this.containerList.length) {
      this.containerList = this.allContainers.slice(0, this.containerList.length + 50);
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-overview {
  height: 100%;
}

.section-item {
  width: calc(50% - 8px);
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
  }
}

.table-item {
  margin-top: 12px;
  height: calc(100% - 168px - 12px);
}
</style>
