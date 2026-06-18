<template>
  <query-filter
    v-model='queryParams'
    :filter-list="filterList"
    :updateRoute="true"
    @on-change="handleChange"
    @on-remove-tag='handleRemoveTag' />
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
export default class SearchGroup extends Vue {
  @Prop({ default: '' }) private componentType!: string;

  private queryParams: any = {
    srcServiceInstance: '',
    serviceInstance: '',
    rootResourceQuery: '',
    resourceQuery: '',
    methodQuery: '',
    topicQuery: '',
    groupQuery: '',
    partitionQuery: '',
    brokerQuery: '',
    sqlOperationQuery: '',
    sqlDatabaseQuery: '',
  }

  get filterListInit () {
    const list: any[] = [
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
      {
        field: 'rootResourceQuery',
        label: i18n.t('modules.views.appMonitor.errorDetail.s_915a9dc7') as string, labelKey: 'modules.views.appMonitor.errorDetail.s_915a9dc7',
        type: 'input',
        children: [],
      },
      {
        field: 'resourceQuery',
        label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c',
        type: 'input',
        children: [],
      },
      // {
      //   field: 'urlQuery',
      //   label: 'Url',
      //   type: 'input',
      //   componentType: 'service.http',
      //   children: [],
      // },
      {
        field: 'methodQuery',
        label: i18n.t('modules.views.alarmCenter.eventDetail.s_ea340b9d') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_ea340b9d',
        type: 'input',
        componentType: 'service.http',
        children: [],
      },
      {
        field: 'topicQuery',
        label: 'Topic',
        type: 'input',
        componentType: 'service.mq',
        children: [],
      },
      {
        field: 'groupQuery',
        label: 'ConsumerGroup',
        type: 'input',
        componentType: 'service.mq',
        children: [],
      },
      {
        field: 'partitionQuery',
        label: 'Partition',
        type: 'input',
        componentType: 'service.mq',
        children: [],
      },
      {
        field: 'brokerQuery',
        label: 'Broker',
        type: 'input',
        componentType: 'service.mq',
        children: [],
      },
      {
        field: 'sqlOperationQuery',
        label: i18n.t('modules.views.alarmCenter.eventDetail.s_de9cc3dd') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_de9cc3dd',
        type: 'input',
        componentType: 'service.db',
        children: [],
      },
      {
        field: 'sqlDatabaseQuery',
        label: i18n.t('modules.views.alarmCenter.eventDetail.s_5ccbbd01') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_5ccbbd01',
        type: 'input',
        componentType: 'service.db',
        children: [],
      },
    ]
    return list.filter(item => !item.componentType || item.componentType === this.componentType)
  }

  private filterList: any[] = []

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
