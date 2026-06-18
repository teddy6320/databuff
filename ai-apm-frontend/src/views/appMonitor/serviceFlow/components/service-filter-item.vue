<template>
  <el-tag size="small" closable :key="payload.id" disable-transitions effect='plain'
    :type='payload.filters && payload.filters.length ? "" : "info"' class="mb-10 nosel"
    @close='closeHandle'
    v-clickout='cancelHandle'>
    <span class="db-icon font-12 mr-5">{{ (payload.typeIcon || 'default') | DbIconFilter }}</span>
    <span class="font-12">{{ payload.nameKey ? $t(payload.nameKey) : payload.name }}</span>
    <el-popover :value="showPopover" :popper-options="{ boundariesElement: 'body' }" trigger="manual">
      <i v-if='showFilter' @click="togglePopover(true)" slot="reference" class="db-icon-filter font-12 ml-5 cp"></i>
      <div class="flex-h">
        <query-filter :key="payload.id" :filterTitle="$t('modules.views.appMonitor.serviceFlow.s_c2fe6253')" size='small'
          v-model="queryParams"
          :filter-list="filterList"
          @on-change="handleChange"
          @on-remove-tag='handleRemoveTag' />
        <el-button @click="confirmHandle" plain size="small" type="primary" class="ml-10">{{ $t('modules.views.deployInstall.apm.s_e83a256e') }}</el-button>
      </div>
    </el-popover>
  </el-tag>
</template>

<script lang='ts'>
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import QueryFilter from '@/components/query-filter/index.vue';
import { FilterItem, TagItem, FormatedSelected } from '@/components/query-filter/types/index.types';
import { ServiceFilterPayload } from './service-filter.vue'
import { toAsyncWait } from '@/utils/common'
import ServiceApi from '@/api/service'
import { StringIsEmpty } from '@/utils/common'

@Component({
  components: {
    QueryFilter,
  }
})
export default class ServiceFilterItem extends Vue {
  @Prop() private chainIndex!: number;
  @Prop({ default: false }) private showFilter!: boolean;
  @Prop({ default: {} }) private payload!: ServiceFilterPayload;
  @Prop({ default: {} }) private serviceInstanceList!: any[];

  @Watch('payload', { deep: true })
  private onPayload (newVal: ServiceFilterPayload) {
    //
  }

  private filterList: FilterItem[] = [];
  private resourceList: any[] = [];
  private instanceList: any[] = [];

  private showPopover = false;

  private filters: FormatedSelected[] = [];

  get filterListInit (): FilterItem[] {
    const list: FilterItem[] = [
      {
        field: 'serviceInstance',
        label: i18n.t('modules.views.alarmCenter.alarm.s_71673bab') as string, labelKey: 'modules.utils.filters.s_71673bab',
        type: 'select',
        disabled: false,
        children: this.instanceList.map(t => ({
          ...t,
          showValue: t.label,
        })),
      },
      {
        field: 'resource',
        label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c',
        type: 'select',
        disabled: false,
        children: this.resourceList.map(t => ({
          ...t,
          showValue: t.label,
        })),
      },
    ]
    return list
  }

  private queryParams: any = {
    serviceInstance: '',
    resource: ''
  }

  private async created () {
    // await this.getResource(this.payload.id);
    this.instanceList = this.payload.serviceInstances.map((i) => ({
      label: i,
      value: i,
    }))
    this.resourceList = this.payload.resources.map((i) => ({
      label: i,
      value: i,
    }))
    this.filterList = [...this.filterListInit]
  }

  private togglePopover (val: boolean) {
    this.showPopover = val;
  }

  private handleChange ({ row, selected }: { row: TagItem, selected: FormatedSelected[] }) {
    //
    this.filters = [...selected]
  }

  private handleRemoveTag ({ row, selected }: { row: TagItem, selected: FormatedSelected[] }) {
    this.filters = [...selected]
  }

  private confirmHandle () {
    this.togglePopover(false);
    this.$emit('on-filter-change', {
      payload: this.payload,
      filters: this.filters,
      chainIndex: this.chainIndex,
    });
  }
  private cancelHandle () {
    this.togglePopover(false);
  }

  private closeHandle () {
    this.$emit('on-filter-remove', {
      chainIndex: this.chainIndex,
    })
  }
  
}
</script>
<style lang='scss' scoped>
</style>