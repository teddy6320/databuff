<template>
  <query-filter
    v-model='queryParams'
    :filter-list="filterList"
    :updateRoute="true"
    @on-change="handleChange"
    @on-remove-tag="handleRemoveTag" />
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import QueryFilter from '@/components/query-filter/index.vue';
import { TagItem, FormatedSelected } from '@/components/query-filter/types/index.types';
import { StringIsEmpty } from '@/utils/common'

@Component({
  components: {
    QueryFilter,
  },
})
export default class PoolSearchGroup extends Vue {
  @Prop({ default: '' }) private poolType!: string;
  @Prop({ default: () => [] }) private poolList!: string[];

  private queryParams: any = {
    srcServiceInstance: '',
    serviceInstance: '',
    poolName: '',
  }

  get filterListInit () {
    const poolFieldLabel = this.poolType === 'object' ? i18n.t('modules.views.appMonitor.serviceCall.s_dbb6e534') as string : i18n.t('modules.views.appMonitor.serviceCall.s_0370c2d5') as string
    const list = [
      {
        field: 'poolName',
        label: poolFieldLabel,
        type: 'select',
        children: this.poolList.map(t => ({
          label: t,
          value: t,
          showValue: t,
        })),
      },
      {
        field: 'srcServiceInstance',
        label: i18n.t('modules.views.appMonitor.serviceCall.s_082e1305') as string, labelKey: 'modules.views.appMonitor.serviceCall.s_082e1305',
        type: 'input',
        children: [],
      },
      {
        field: 'serviceInstance',
        label: i18n.t('modules.views.appMonitor.serviceCall.s_17c670cd') as string, labelKey: 'modules.views.appMonitor.serviceCall.s_17c670cd',
        type: 'input',
        children: [],
      },
    ]
    return list
  }

  private filterList: any[] = []

  @Watch('poolList', { immediate: true, deep: true })
  private watchPoolList () {
    const _query = { ...this.$route.query }
    if (this.poolList.length && !_query.poolName) {
      this.queryParams.poolName = this.poolList[0]
      this.$router.replace({ query: { ..._query, poolName: this.queryParams.poolName } })
    }
  }

  public async init () {
    // 搜索参数回显
    const routerQuery = this.$route.query
    Object.keys(this.queryParams).forEach(k => {
      if (routerQuery[k]) {
        this.queryParams[k] = decodeURIComponent(String(routerQuery[k]))
      }
    })

    this.filterList = [...this.filterListInit]
    const query = { ...this.queryParams }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    })
    return { ...query }
  }

  private handleChange ({ row, selected }: { row: TagItem, selected: FormatedSelected[] }) {
    
    const query = { ...this.queryParams }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    })
    this.$emit('on-change', { ...query })
  }
  private handleRemoveTag () {
    
    const query = { ...this.queryParams }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    })
    this.$emit('on-change', { ...query })
  }
}
</script>

<style lang="scss" scoped>

</style>
