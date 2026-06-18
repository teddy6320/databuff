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
import { toAsyncWait } from '@/utils/common';
import InfraApi from '@/api/infrastructure';

@Component({
  components: {
    QueryFilter,
  },
})
export default class SearchGroup extends Vue {
  @Prop({ default: () => ({}) }) private timeParams!: any;

  private queryParams: any = {
    processName: '',
    hostName: '',
  }

  get filterListInit () {
    const list = [
      {
        field: 'processName',
        label: i18n.t('modules.views.configManage.entity.s_f0b09f88') as string, labelKey: 'modules.views.configManage.entity.s_f0b09f88',
        type: 'select',
        likeable: true,
        children: this.processList.map(t => ({
          ...t,
          showValue: t.label,
        })),
      },
      {
        field: 'hostName',
        label: i18n.t('modules.views.configManage.alarm.s_3d022a63') as string, labelKey: 'modules.views.configManage.alarm.s_3d022a63',
        type: 'select',
        likeable: true,
        children: this.hostList.map(t => ({
          ...t,
          showValue: t.label,
        })),
      },
    ]
    return list
  }

  private filterList: any[] = [];
  private hostList: any[] = [];
  private processList: any[] = [];

  public async init () {
    await this.getHostList()
    await this.getProcessList()

    // 搜索参数回显
    const routerQuery = this.$route.query
    Object.keys(this.queryParams).forEach(k => {
      if (routerQuery[k]) {
        this.queryParams[k] = decodeURIComponent(String(routerQuery[k]))
      }
    })

    if (!this.queryParams.processName) {
      // 旧版参数兼容
      const { query = '', sn = '', container = '' } = this.$route.query;
      if (sn || container) {
        this.queryParams.processName = decodeURIComponent((sn || container || '') as string);
      } else if (query) {
        const _query = decodeURIComponent(query as string);
        const [host, process] = _query.split(':') || [];
        this.queryParams.processName = process || host || '';
        if (!this.queryParams.hostName) {
          this.queryParams.hostName = process ? host : '';
        }
      }
    }

    this.filterList = this.filterListInit;
    return { ...this.queryParams }
  }

  private handleChange () {
    this.$emit('on-change', { ...this.queryParams })

    // 删除旧版参数
    const _query = { ...this.$route.query }
    if (_query.query || _query.sn || _query.container) {
      delete _query.query
      delete _query.sn
      delete _query.container
      this.$router.replace({ query: { ..._query } })
    }
  }

  private async getHostList () {
    const { fromTime, toTime } = this.timeParams
    const { result, error } = await toAsyncWait(InfraApi.getHostObjs({
      fromTime,
      toTime,
      objType: 'host'
    }))
    if (!error) {
      const { data = [] } = result || {};
      this.hostList = (data || []).map((t: any) => ({
        label: t.name,
        value: t.name,
      }))
    } else {
      this.hostList = []
    }
  }
  private async getProcessList () {
    const { fromTime, toTime } = this.timeParams
    const { result, error } = await toAsyncWait(InfraApi.getHostObjs({
      fromTime,
      toTime,
      objType: 'pname',
    }))
    if (!error) {
      const { data = [] } = result || {};
      this.processList = (data || []).map((t: any) => ({
        label: t.name,
        value: t.name,
      }))
    } else {
      this.processList = []
    }
  }
}
</script>
