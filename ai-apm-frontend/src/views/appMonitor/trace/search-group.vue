<template>
  <query-filter
    v-model='queryParams'
    :filter-list="filterList"
    :disabled='paramsLoading'
    @on-change="handleChange"
    @on-remove-tag='handleRemove' />
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import QueryFilter from '@/components/query-filter/index.vue';
import { TagItem, FormatedSelected } from '@/components/query-filter/types/index.types';
import { StringIsEmpty } from '@/utils/common'
import { toAsyncWait } from '@/utils/common';
import ServiceApi from '@/api/service';
import dayjs from 'dayjs';

@Component({
  components: {
    QueryFilter,
  },
})
export default class SearchGroup extends Vue {
  @Prop({ default: () => [] }) private serviceList!: any[];
  @Prop({ default: () => ({}) }) private timeParams!: any;

  private queryParams: any = {
    traceId: '',
    fuzzyTraceName: '',
    sid: '',
    si: '',
  }

  private resourceList: any[] = []

  private paramsLoading = false

  get filterListInit (): any[] {
    const list = [
      {
        field: 'traceId',
        label: 'TraceID',
        type: 'input',
        children: [],
      },
      {
        field: 'fuzzyTraceName',
        label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c',
        type: 'select',
        likeable: true,
        children: [...this.resourceList],
      },
      {
        field: 'sid',
        label: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_47d68cd0',
        type: 'select',
        children: [...this.serviceList],
      },
    ]
    if (this.queryParams.sid) {
      list.push({
        field: 'si',
        label: i18n.t('modules.views.alarmCenter.alarm.s_71673bab') as string, labelKey: 'modules.utils.filters.s_71673bab',
        type: 'select',
        children: (this.serviceInstanceMap[this.queryParams.sid] || []).map((t: string) => ({
          label: t,
          value: t,
        })),
      })
    }
    return list
  }

  private filterList: any[] = []

  private serviceInstanceMap: any = {} // 服务的实例map

  @Watch('timeParams', { deep: true })
  private watchQueryParams () {
    this.serviceInstanceMap = {}
  }

  public async init (timeParams: any) {
    let isNeedRemoveTraceId = false;
    const timeIsOverOneDay = new Date(timeParams.toTime).valueOf() - new Date(timeParams.fromTime).valueOf() > 24 * 60 * 60 * 1000;
    if (timeIsOverOneDay && this.queryParams?.traceId) {
      this.$confirm(i18n.t('modules.views.appMonitor.trace.s_b5908b88') as string, i18n.t('modules.views.appMonitor.trace.s_6dc613a6') as string,  {
        type: 'warning',
        showCancelButton: false,
      });
      this.queryParams.traceId = '';
      const _query = { ...this.$route.query }
      delete _query.traceId
      this.$router.replace({ query: { ..._query } });
      isNeedRemoveTraceId = true;
    }
    this.paramsLoading = true;

    // 搜索参数回显
    const routerQuery = { ...this.$route.query }
    if (isNeedRemoveTraceId) {
      delete routerQuery.traceId;
    }
    Object.keys(this.queryParams).forEach(k => {
      if (routerQuery[k]) {
        this.queryParams[k] = decodeURIComponent(String(routerQuery[k]))
      }
    })
    const serviceId = this.queryParams.sid
    if (serviceId) {
      await this.getServiceInstance(serviceId)
    } else {
      this.queryParams.si = ''
    }

    this.getResourceList().finally(() => {
      this.paramsLoading = false;
      this.filterList = [...this.filterListInit]
    });

    const query = { ...this.queryParams }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    })
    if ((query.traceId || '').includes(',')) {
      const ids = query.traceId.split(',').slice(0, 101) // 显示101条，用于判断是否超出
      query.traceIds = ids
      delete query.traceId
    }
    return { filter: { ...query } }
  }

  private async handleChange ({ row, selected, routerQuery }: { row: TagItem, selected: FormatedSelected[], routerQuery: any }) {
    const timeIsOverOneDay = new Date(this.timeParams.toTime).valueOf() - new Date(this.timeParams.fromTime).valueOf() > 24 * 60 * 60 * 1000;
    if (timeIsOverOneDay && selected.find(t => t.field === 'traceId')?.value) {
      this.$confirm(i18n.t('modules.views.appMonitor.trace.s_b5908b88') as string, i18n.t('modules.views.appMonitor.trace.s_6dc613a6') as string,  {
        type: 'warning',
        showCancelButton: false,
      });
      delete this.queryParams.traceId;
      const _query = { ...this.$route.query }
      delete _query.traceId
      this.$router.replace({ query: { ..._query } });
      delete routerQuery.traceId
    }
    if ((row || {}).field === 'sid') {
      // 操作服务时，清空服务实例
      this.queryParams.si = ''
      const _query = {...this.$route.query}
      delete _query.si
      this.$router.replace({ query: { ..._query } })

      // 获取服务下的服务实例
      const serviceId = ((selected.find(t => t.field === 'sid') || {}).value || [])[0] as string
      if (serviceId && !this.serviceInstanceMap[serviceId]) {
        await this.getServiceInstance(serviceId)
      }
      this.filterList = [...this.filterListInit]
    }
    const query = { ...this.queryParams }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    })
    if ((query.traceId || '').includes(',')) {
      const ids = query.traceId.split(',').slice(0, 101) // 显示101条，用于判断是否超出
      query.traceIds = ids
      delete query.traceId
    }
    this.$emit('on-change', { ...query }, routerQuery)
  }

  private handleRemove (payload: { field: string, value: any, selected: any[], routerQuery: any }) {
    if (payload.field === 'sid') {
      // 操作服务时，清空服务实例
      this.queryParams.si = ''
      const _query = {...this.$route.query}
      delete _query.si
      delete _query.sn
      this.$router.replace({ query: { ..._query } })
    }

    this.filterList = [...this.filterListInit]

    const query = { ...this.queryParams }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    });
    this.$emit('on-change', { ...query }, payload.routerQuery)
  }

  private async getServiceInstance (sid: string) {
    const params = {
      serviceId: sid,
      fromTime: dayjs(this.timeParams.fromTime).format('YYYY-MM-DD HH:mm:ss'),
      toTime: dayjs(this.timeParams.toTime).format('YYYY-MM-DD HH:mm:ss'),
    }
    const { result, error } = await toAsyncWait(ServiceApi.getBasicServiceInstance(params))
    if (!error) {
      const list = ((result.data || {}).serviceInstances || []).map((t: any) => t.serviceInstance)
      this.$set(this.serviceInstanceMap, sid, list);
    }
  }

  private async getResourceList () {
    const { fromTime, toTime } = this.getGlobalTimeV2()
    // 请求列表
    const { result: resRst, error: resErr } = await toAsyncWait(ServiceApi.getServiceRequestByCompTypes({
      field: 'resource',
      // isIn: 1,
      componentType: 'service.trace',
      serviceId: this.queryParams.sid || null,
      serviceInstance: this.queryParams.si || null,
      fromTime, toTime
    }))
    if (!resErr) {
      const data: any = (resRst || {}).data || {}
      const resGroup = Object.values(data)
      if (Array.isArray(resGroup) && resGroup.length) {
        const resList = resGroup.flat();
        const _resList = [...new Set(resList)].map((t: any) => ({
          label: t,
          value: t,
        }));
        this.resourceList = _resList
      } else {
        this.resourceList = []
      }
    } else {
      this.resourceList = []
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
