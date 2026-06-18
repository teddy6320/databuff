<template>
  <div class="detail-overview">
    <db-table
      :columnConfig='portColumnConfig'
      :data="portList"
      :total="allPorts.length"
      @on-table-scroll="portTableScroll"
      ref='portTable'
      class="table-item">
      <div slot='total' slot-scope="{ total }" class="flex-h lh-20 mb-5">
        <span class="fw-500 mr-8">Ports</span>
        {{ total }}
      </div>
    </db-table>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';

@Component
export default class DetailOverview extends Vue {
  @Prop({ default: () => ({}) }) private detail!: any;

  private portColumnConfig = [
    { field: 'name', prop: 'name', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 160 },
    { field: 'port', label: i18n.t('modules.views.infrastructure.serviceDetail.s_39c76443') as string, labelKey: 'modules.views.infrastructure.serviceDetail.s_39c76443', minWidth: 100, },
    { field: 'protocol', label: i18n.t('modules.views.appMonitor.resourceDetail.s_faa1ad5e') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_faa1ad5e', minWidth: 100, },
    { field: 'nodePort', label: 'Node Port', minWidth: 100, },
    { field: 'targetPort', label: i18n.t('modules.views.infrastructure.serviceDetail.s_91bc694a') as string, labelKey: 'modules.views.infrastructure.serviceDetail.s_91bc694a', minWidth: 100, },
  ]
  private portList: any[] = []

  get allPorts () {
    return ((this.detail.spec || {}).ports || []).map((t: any) => ({
      ...t,
      port: (t.port || t.port === 0) ? `${t.port}` : '-',
      nodePort: (t.nodePort || t.nodePort === 0) ? `${t.nodePort}` : '-',
      targetPort: (t.targetPort || t.targetPort === 0) ? `${t.targetPort}` : '-',
    }));
  }

  public getData () {
    this.portList = this.allPorts.slice(0, 50);
  }

  private portTableScroll () {
    if (this.allPorts.length > this.portList.length) {
      this.portList = this.allPorts.slice(0, this.portList.length + 50);
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
  height: 100%;
}
</style>
