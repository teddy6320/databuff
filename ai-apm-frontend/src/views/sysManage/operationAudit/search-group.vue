<template>
  <query-filter
    v-model='queryParams'
    :updateRoute='true'
    :filter-list="filterList"
    @on-change="handleChange"
    @on-remove-tag="handleChange" />
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import QueryFilter from '@/components/query-filter/index.vue';
import { toAsyncWait } from '@/utils/common';
import SystemApi from '@/api/system';

@Component({
  components: {
    QueryFilter,
  },
})
export default class SearchGroup extends Vue {
  private queryParams: any = {
    actor: '',
    entityName: '',
    entityType: '',
  }

  get filterListInit () {
    const list = [
      {
        field: 'actor',
        label: i18n.t('modules.views.sysManage.operationAudit.s_1fd02a90') as string, labelKey: 'modules.views.aiPlatform.chat.s_1fd02a90',
        type: 'select',
        children: this.accountList.map(t => ({ label: t, value: t, showValue: t })),
      },
      {
        field: 'entityName',
        label: i18n.t('modules.views.sysManage.operationAudit.s_965377c8') as string, labelKey: 'modules.views.sysManage.operationAudit.s_965377c8',
        type: 'select',
        likeable: true,
        children: this.entityList.map(t => ({ label: t, value: t, showValue: t })),
      },
      {
        field: 'entityType',
        label: i18n.t('modules.views.sysManage.operationAudit.s_1e767357') as string, labelKey: 'modules.views.sysManage.operationAudit.s_1e767357',
        type: 'select',
        likeable: true,
        children: this.entityTypeList.map(t => ({ label: t, value: t, showValue: t })),
      },
    ]
    return list
  }

  private filterList: any[] = []

  private paramsLoaded = false;
  private accountList: string[] = [];
  private entityList: any[] = [];
  private entityTypeList: any[] = [];

  public async init () {
    // 搜索参数回显
    const routerQuery = this.$route.query
    Object.keys(this.queryParams).forEach(k => {
      if (routerQuery[k]) {
        this.queryParams[k] = decodeURIComponent(String(routerQuery[k]))
      }
    })
    if (!this.paramsLoaded) {
      await this.getProfilingParams();
    }

    this.filterList = this.filterListInit;
    return { ...this.queryParams }
  }

  private async handleChange () {
    this.$emit('on-change', { ...this.queryParams })
  }

  private async getProfilingParams () {
    const params = {
      // fields: ['actor', 'entityName', 'entityType'],
    }
    const { result, error } = await toAsyncWait(SystemApi.getOperateAuditTags(params))
    if (!error) {
      const data: any = (result || {}).data || {}
      const formatList = (list: string[]) => {
        const _list = [...new Set((list || []).filter((t: string) => !!t && !!t.trim()))]
        return _list.sort((a: string, b: string) => a.localeCompare(b, 'zh-CN'));
      }
      this.accountList = formatList(data.actor);
      this.entityList = formatList(data.entityName);
      this.entityTypeList = formatList(data.entityType);
      this.paramsLoaded = true;
    } else {
      this.accountList = []
      this.entityList = []
      this.entityTypeList = []
    }
  }
}
</script>
