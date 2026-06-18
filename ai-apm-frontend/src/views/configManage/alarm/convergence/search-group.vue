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

@Component({
  components: {
    QueryFilter,
  },
})
export default class SearchGroup extends Vue {
  private queryParams: any = {
    name: '',
    enabled: '',
  }

  private filterListInit: any[] = [
    {
      field: 'name',
      label: i18n.t('modules.views.configManage.alarm.s_4dba924c') as string, labelKey: 'modules.views.configManage.alarm.s_4dba924c',
      type: 'input',
      children: [],
    },
    {
      field: 'enabled',
      label: i18n.t('modules.views.appMonitor.serviceDetail.s_2b82bf9a') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_2b82bf9a',
      type: 'select',
      children: [
        { label: i18n.t('modules.views.configInstall.dataAccess.s_53ace430') as string, labelKey: 'modules.views.configInstall.dataAccess.s_53ace430', value: true, showValue: i18n.t('modules.views.configInstall.dataAccess.s_53ace430') as string },
        { label: i18n.t('modules.views.configInstall.dataAccess.s_69b0f684') as string, labelKey: 'modules.views.configInstall.dataAccess.s_69b0f684', value: false, showValue: i18n.t('modules.views.configInstall.dataAccess.s_69b0f684') as string },
      ],
    },
  ]

  private filterList: any[] = []

  public async init () {
    // 搜索参数回显
    const routerQuery = this.$route.query
    Object.keys(this.queryParams).forEach(k => {
      if (k === 'enabled') {
        if (['true', 'false'].includes(routerQuery[k] as string)) {
          this.queryParams.enabled = routerQuery[k] !== 'false'
        } else {
          this.queryParams.enabled = ''
        }
      } else if (routerQuery[k]) {
        this.queryParams[k] = decodeURIComponent(String(routerQuery[k]))
      } else {
        this.queryParams[k] = ''
      }
    })

    this.filterList = [...this.filterListInit];
    return { ...this.queryParams }
  }

  private handleChange () {
    this.$emit('on-change', { ...this.queryParams })
  }
}
</script>
