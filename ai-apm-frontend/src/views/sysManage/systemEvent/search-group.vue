<template>
  <query-filter
    v-model="queryParams"
    :updateRoute="true"
    :filter-list="filterList"
    @on-change="handleChange"
    @on-remove-tag="handleChange" />
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
  private queryParams: any = {
    message: '',
    level: '',
  }

  private filterListInit: any[] = [
    {
      field: 'message',
      label: i18n.t('modules.views.sysManage.systemEvent.s_d76255b2') as string, labelKey: 'modules.views.sysManage.systemEvent.s_d76255b2',
      type: 'input',
      children: [],
    },
    {
      field: 'level',
      label: i18n.t('modules.views.configManage.alarm.s_e064de59') as string, labelKey: 'modules.views.configManage.alarm.s_e064de59',
      type: 'select',
      children: [
        { label: i18n.t('modules.views.alarmCenter.alarm.s_fc7e3846') as string, labelKey: 'modules.utils.filters.s_fc7e3846', value: 3 },
        { label: i18n.t('modules.views.alarmCenter.alarm.s_bde77082') as string, labelKey: 'modules.utils.filters.s_bde77082', value: 2 },
        { label: i18n.t('modules.views.alarmCenter.alarm.s_01ceb3ed') as string, labelKey: 'modules.utils.filters.s_01ceb3ed', value: 1 },
      ],
    },
  ]

  private filterList: any[] = []

  public async init () {
    // 搜索参数回显
    const routerQuery = this.$route.query
    Object.keys(this.queryParams).forEach(k => {
      if (routerQuery[k]) {  
        this.queryParams[k] = decodeURIComponent(String(routerQuery[k]))
      }
    })

    this.filterList = this.filterListInit
    const query = { ...this.queryParams }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    })
    return { ...query }
  }

  private handleChange ({ row, selected }: { row: TagItem, selected: FormatedSelected[] }) {
    this.$emit('on-change', { ...this.queryParams })
  }
}
</script>
