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
  @Prop({ default: () => [] }) private serviceList!: any[];
  // @Prop({ default: () => [] }) private srcServiceInstanceList!: any[];
  @Prop({ default: () => [] }) private resourceList!: any[];
  @Prop({ default: () => ({}) }) private entryPointsMapping!: any;

  @Watch('serviceList', { immediate: true })
  private onServiceListChange () {
    this.updateFilterList()
  }
  // @Watch('srcServiceInstanceList', { immediate: true })
  // private onSrcServiceInstanceListChange() {
  //   this.updateFilterList()
  // }
  @Watch('resourceList', { immediate: true })
  private onResourceListChange() {
    this.updateFilterList()
  }

  private queryParams: any = {
    sid: '',
    resource: '',
    errorType: '',
    // srcServiceInstance: '',
    error: '',
  }

  private initOver = false

  public setInitOver () {
    this.initOver = true
  }

  get filterListInit () {
    const list = [
      {
        field: 'sid',
        label: i18n.t('modules.views.appMonitor.serviceFlow.s_491bfca0') as string, labelKey: 'modules.views.appMonitor.serviceFlow.s_491bfca0',
        type: 'select',
        deletable: false,
        children: this.serviceList.map(t => ({
          ...t,
          showValue: t.label,
        })),
      },
      // {
      //   field: 'srcServiceInstance',
      //   label: i18n.t('modules.views.appMonitor.serviceFlow.s_b94dd9d2') as string, labelKey: 'modules.views.appMonitor.serviceFlow.s_b94dd9d2',
      //   type: 'select',
      //   likeable: true,
      //   children: this.srcServiceInstanceList.map(t => ({
      //     ...t,
      //     showValue: t.label,
      //   })),
      // },
      {
        field: 'resource',
        label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c',
        type: 'select',
        likeable: true,
        children: this.resourceList.map(t => ({
          ...t,
          showValue: t.label,
        })),
      },
    ]
    return list
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
    const sid = this.queryParams.sid
    if (!sid) {
      this.queryParams.sid = (this.serviceList[0] || {}).value || ''
      if (this.queryParams.sid) {
        const _query = { ...this.$route.query }
        _query.sid = encodeURIComponent(this.queryParams.sid)
        this.$router.replace({ query: { ..._query } })
      }
    }

    return this.getQuery()
  }

  private updateFilterList () {
    this.filterList = this.filterListInit
  }

  private async handleChange ({ row, selected }: { row: TagItem, selected: FormatedSelected[] }) {
    if ((row || {}).field === 'sid') {
      // 操作入口服务时，清空接口名称
      // this.queryParams.srcServiceInstance = ''
      this.queryParams.resource = ''
      const query = {...this.$route.query}
      // delete query.srcServiceInstance
      delete query.resource
      this.$router.replace({ query: { ...query } })
    }
    this.$emit('on-change', { ...this.getQuery() }, this.queryParams.sid)

  }

  private handleRemoveTag () {
    this.$emit('on-change', { ...this.getQuery() }, this.queryParams.sid)
  }


  private getQuery () {
    const query = { ...this.queryParams }
    if (query.sid) {
      query.entrypointPathId = (this.entryPointsMapping[query.sid] || {}).entrypointPathId
      delete query.sid
    }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    })
    return { ...query }
  }
}
</script>
