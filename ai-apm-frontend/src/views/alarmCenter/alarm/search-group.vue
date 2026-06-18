<template>
  <query-filter
    v-model="queryParams"
    :filter-list="filterList"
    :updateRoute="true"
    @on-change="handleChange"
    @on-remove-tag="handleChange" />
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import QueryFilter from '@/components/query-filter/index.vue';
import { StringIsEmpty } from '@/utils/common';

@Component({
  components: {
    QueryFilter,
  },
})
export default class SearchGroup extends Vue {
  private levelList = [
    { label: i18n.t('modules.views.alarmCenter.alarm.s_fc7e3846') as string, labelKey: 'modules.utils.filters.s_fc7e3846', value: 3 },
    { label: i18n.t('modules.views.alarmCenter.alarm.s_bde77082') as string, labelKey: 'modules.utils.filters.s_bde77082', value: 2 },
  ];
  private queryParams: any = {
    description: '',
    idLike: '',
    level: [],
  }

  private filterListInit: any[] = [
    {
      field: 'description',
      label: i18n.t('modules.views.alarmCenter.alarm.s_606a249f') as string, labelKey: 'modules.views.alarmCenter.alarm.s_606a249f',
      type: 'input',
      children: [],
    },
    {
      field: 'idLike',
      label: i18n.t('modules.views.alarmCenter.alarm.s_10b22107') as string, labelKey: 'modules.views.alarmCenter.alarm.s_10b22107',
      type: 'input',
      children: [],
    },
    {
      field: 'level',
      label: i18n.t('modules.views.alarmCenter.alarm.s_ed7094f4') as string, labelKey: 'modules.views.alarmCenter.alarm.s_ed7094f4',
      type: 'select',
      valueType: 'number',
      multiple: true,
      children: [...this.levelList],
    },
  ]

  private filterList: any[] = []

  public async init () {
    // 搜索参数回显
    const routerQuery = this.$route.query
    Object.keys(this.queryParams).forEach(k => {
      const filterItem = this.filterListInit.find(item => item.field === k)
      const multiple = filterItem?.multiple || false
      const isNumber = filterItem?.valueType === 'number'
      if (!StringIsEmpty(routerQuery[k])) {
        const isArrayValue = Array.isArray(routerQuery[k])
        const values = (isArrayValue ? routerQuery[k] as string[] : [routerQuery[k] as string]).filter(t => !StringIsEmpty(t));
        if (multiple) {
          this.queryParams[k] = values.map((t: string) => isNumber ? +t : decodeURIComponent(t));
        } else {
          this.queryParams[k] = isNumber ? +values[0] : decodeURIComponent(values[0])
        }
      } else {
        this.queryParams[k] = multiple ? [] : ''
      }
    })

    this.filterList = this.filterListInit;
    return { ...this.queryParams }
  }

  private handleChange () {
    this.$emit('on-change', { ...this.queryParams })
  }
}
</script>
