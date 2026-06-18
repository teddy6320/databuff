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
    rcvNames: '',
    alertDesc: '',
    method: '',
    result: '',
  }

  private filterListInit: any[] = [
    {
      field: 'rcvNames',
      label: i18n.t('modules.views.alarmCenter.notice.s_f669e91c') as string, labelKey: 'modules.views.alarmCenter.notice.s_f669e91c',
      type: 'input',
      children: [],
    },
    {
      field: 'alertDesc',
      label: i18n.t('modules.views.alarmCenter.alarm.s_606a249f') as string, labelKey: 'modules.views.alarmCenter.alarm.s_606a249f',
      type: 'input',
      children: [],
    },
    {
      field: 'method',
      label: i18n.t('modules.views.alarmCenter.alarmDetail.s_9fd00f0a') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_9fd00f0a',
      type: 'select',
      children: [
        { label: i18n.t('modules.views.alarmCenter.notice.s_e9e8054f') as string, labelKey: 'modules.utils.filters.s_e9e8054f', value: 'mail' },
        { label: i18n.t('modules.views.alarmCenter.notice.s_485c3abb') as string, labelKey: 'modules.utils.filters.s_485c3abb', value: 'sms' },
        { label: i18n.t('modules.views.alarmCenter.notice.s_4a0e9142') as string, labelKey: 'modules.utils.filters.s_4a0e9142', value: 'dingtalk' },
        { label: i18n.t('modules.views.alarmCenter.notice.s_ff17b9f9') as string, labelKey: 'modules.utils.filters.s_ff17b9f9', value: 'wechat' },
        { label: 'Webhook', value: 'webhook' },
        { label: 'Socket', value: 'socket' },
      ],
    },
    {
      field: 'result',
      label: i18n.t('modules.views.alarmCenter.alarmDetail.s_895bd22e') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_895bd22e',
      type: 'select',
      children: [
        { label: i18n.t('modules.views.alarmCenter.notice.s_330363df') as string, labelKey: 'modules.utils.filters.s_330363df', value: 'success' },
        { label: i18n.t('modules.views.alarmCenter.notice.s_acd5cb84') as string, labelKey: 'modules.utils.filters.s_acd5cb84', value: 'fail' },
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

    this.filterList = this.filterListInit;
    return { ...this.queryParams }
  }

  private handleChange () {
    this.$emit('on-change', { ...this.queryParams })
  }
}
</script>
