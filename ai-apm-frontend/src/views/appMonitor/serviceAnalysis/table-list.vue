<template>
  <div class="table-list-warp flex-v">

    <div>
      <db-radio v-model='currType' :options='getOptions' @change="typeChangeHandle"></db-radio>
    </div>

    <component
      ref="tableList"
      :is="currType"
      :queryParams="queryParams"
      :timeParams="timeParams"
      :componentType="componentType"
      @add-query="$emit('add-query')"
      :class="instanceAble ? 'instance-able' : ''"
      @on-table-inited='tableInitedHandle'
    />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import Request from './table-request-list.vue'
import Service from './table-service-list.vue'
import Instance from './table-instance-list.vue'

@Component({
  components: {
    Request,
    Service,
    Instance,
  }
})
export default class TableList extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;
  @Prop({ default: '' }) private componentType!: string;

  public $refs!: {
    tableList: Request | Service,
  }

  private currType: string = 'request';
  private chartTypes: any[] = [
    { label: i18n.t('modules.views.appMonitor.serviceAnalysis.s_7d3a5003') as string, labelKey: 'modules.views.appMonitor.serviceAnalysis.s_7d3a5003', value: 'request' },
    { label: i18n.t('modules.views.alarmCenter.eventDetail.s_e739425d') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_e739425d', value: 'service' },
    { label: i18n.t('modules.views.alarmCenter.alarm.s_71673bab') as string, labelKey: 'modules.utils.filters.s_71673bab', value: 'instance', disabled: true },
  ]
  private instanceAble = false;
  private tableInited = false;

  get getOptions () {
    return this.chartTypes.filter(t => !t.disabled);
  }

  @Watch('queryParams.sid', { immediate: true })
  private sidChangeHandle (serviceId: string) {
    const instanceTab = this.chartTypes.find(t => t.value === 'instance')
    instanceTab.disabled = !serviceId;
    this.instanceAble = !!serviceId;
    if (!serviceId && this.currType === 'instance') {
      this.currType = 'request';
      this.typeChangeHandle();
    }
  }

  public get tableListCanInit () {
    return this.isMounted && this.tableInited
  }

  public isMounted = false;

  private mounted () {
    this.isMounted = true;
  }

  private typeChangeHandle () {
    this.getData()
  }

  public getData () {
    this.$nextTick(() => {
      this.$refs.tableList && this.$refs.tableList?.getData()
    })
  }

  private tableInitedHandle () {
    this.tableInited = true;
  }
}
</script>

<style lang="scss" scoped>
.table-list-warp {
  flex: 1;
  height: 100%;
  background: var(--bg-color);

  .flex-0 {
    flex: 0 1 auto;
  }
}
</style>
