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
  @Prop({ default: () => ({}) }) private filterData!: any;

  private queryParams: any = {
    problemShowId: '',
    problemDesc: '',
    rootCauseTypes: [],
    rootCauseNodes: [],
  }

  get filterListInit () {
    const list: any[] = [
      {
        field: 'problemShowId',
        label: i18n.t('modules.views.alarmCenter.rootCause.s_82a9a9ef') as string, labelKey: 'modules.views.alarmCenter.problemDetail.s_82a9a9ef',
        type: 'input',
        children: [],
      },
      {
        field: 'problemDesc',
        label: i18n.t('modules.views.alarmCenter.rootCause.s_254dd6d2') as string, labelKey: 'modules.views.alarmCenter.rootCause.s_254dd6d2',
        type: 'input',
        children: [],
      },
      {
        field: 'rootCauseTypes',
        label: i18n.t('modules.views.alarmCenter.rootCause.s_d5a57c1c') as string, labelKey: 'modules.views.alarmCenter.problemAnalysis.s_d5a57c1c',
        type: 'select',
        multiple: true,
        children: (this.filterData.rootCauseType || []).map((v: any) => ({ label: v, value: v })),
      },
      {
        field: 'rootCauseNodes',
        label: i18n.t('modules.views.alarmCenter.rootCause.s_ec46bb5e') as string, labelKey: 'modules.views.alarmCenter.problemAnalysis.s_ec46bb5e',
        type: 'select',
        multiple: true,
        children: (this.filterData.rootCauseNode || []).map((v: any) => ({ label: v, value: v })),
      },
    ]
    return list
  }

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
