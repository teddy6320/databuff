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
    containerName: '',
    hostName: '',
  }

  get filterListInit () {
    const list = [
      {
        field: 'containerName',
        label: i18n.t('modules.views.infrastructure.docker.s_53175efe') as string, labelKey: 'modules.views.infrastructure.docker.s_53175efe',
        type: 'select',
        likeable: true,
        children: this.dockerList.map(t => ({
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

  private filterList: any[] = []

  private hostList: any[] = []
  private dockerList: any[] = []

  public async init () {
    await this.getHostList()
    await this.getDockerList()

    // 搜索参数回显
    const routerQuery = this.$route.query
    Object.keys(this.queryParams).forEach(k => {
      if (routerQuery[k]) {
        this.queryParams[k] = decodeURIComponent(String(routerQuery[k]))
      }
    })

    if (!this.queryParams.containerName) {
      // 旧版参数兼容
      const { query = '', sn = '' } = this.$route.query;
      if (sn) {
        this.queryParams.containerName = decodeURIComponent(sn as string);
      } else if (query) {
        const _query = decodeURIComponent(query as string);
        const [host, container] = _query.split(':') || [];
        this.queryParams.containerName = container || host || '';
        if (!this.queryParams.hostName) {
          this.queryParams.hostName = container ? host : '';
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
    if (_query.query || _query.sn) {
      delete _query.query
      delete _query.sn
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

  private async getDockerList () {
    const { fromTime, toTime } = this.timeParams
    const { result, error } = await toAsyncWait(InfraApi.getHostObjs({
      fromTime,
      toTime,
      objType: 'container'
    }))
    if (!error) {
      const { data = [] } = result || {};
      this.dockerList = (data || []).map((t: any) => ({
        label: t.name,
        value: t.name,
      }))
    } else {
      this.dockerList = []
    }
  }
}
</script>
