<template>
  <div>
    <template v-for='item,idx in filterChain'>
      <!-- <ServiceFilterItem :payload='item' :key='item.id' :showFilter='idx !== 0' :serviceInstanceList='serviceInstanceList(item.id)' @on-filter-change='itemFilterChange' /> -->
      <ServiceFilterItem :payload='item' :key='item.uid' :chainIndex='idx' :showFilter='idx !== 0'
        @on-filter-remove='itemRemoveHandle'
        @on-filter-change='itemFilterChange' />
      <i class="el-icon-right describe ml-5 mr-5" :key='item.uid' v-if='idx !== filterChain.length -1'></i>
    </template>
  </div>
</template>

<script lang='ts'>
import { Vue, Component, Prop } from 'vue-property-decorator';
import ServiceFilterItem from './service-filter-item.vue'
import { FormatedSelected } from '@/components/query-filter/types/index.types';

export interface ServiceFilterPayload {
  id: string;
  name: string;
  typeIcon: string;
  filters: FormatedSelected[];
  resources: string[],
  serviceInstances: string[],
  uid: string,
}

@Component({
  components: {
    ServiceFilterItem,
  }
})
export default class ServiceFilter extends Vue {
  @Prop({ default: () => [] }) private filterChain!: any[];
  @Prop({ default: {} }) private allServicesInstanceMapping!: any;

  private itemFilterChange ({ payload, filters, chainIndex }: { payload: ServiceFilterPayload, filters: FormatedSelected[], chainIndex: number }) {
    this.$emit('on-filter-change', {
      payload,
      filters,
      chainIndex,
    });
  }

  private itemRemoveHandle ({ chainIndex }: { chainIndex: number }) {
    this.$emit('on-filter-remove', {
      chainIndex,
    })
  }
}
</script>
<style lang='scss' scoped>
</style>