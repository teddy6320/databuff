<template>
  <query-filter
    v-model='queryParams'
    :filter-list="filterList"
    :updateRoute='true'
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
  // @Prop({ default: () => [] }) private resourceList!: any[];
  @Prop({ default: () => ({}) }) private timeParams!: any;

  private queryParams: any = {
    exception: '',
    resourceQuery: '',
    sid: '',
    si: '',
    rootResourceQuery: '',
  }

  get filterListInit () {
    const list: any[] = [
      {
        field: 'exception',
        label: i18n.t('modules.views.appMonitor.errorDetail.s_2fc597a3') as string, labelKey: 'modules.views.appMonitor.errorDetail.s_2fc597a3',
        type: 'select',
        likeable: true,
        children: this.errorList.map(t => ({
          ...t,
          showValue: t.label,
        })),
      },
      {
        field: 'resourceQuery',
        label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c',
        type: 'select',
        likeable: true,
        children: this.resourceList.map(t => ({
          ...t,
          showValue: t.label,
        })),
      },
      {
        field: 'sid',
        label: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_47d68cd0',
        type: 'select',
        likeable: true,
        children: this.serviceList.map(t => ({
          ...t,
          showValue: t.label,
        })),
      },
    ]
    if (this.queryParams.sid) {
      list.push({
        field: 'si',
        label: i18n.t('modules.views.alarmCenter.alarm.s_71673bab') as string, labelKey: 'modules.utils.filters.s_71673bab',
        type: 'select',
        likeable: true,
        children: this.siList.map(t => ({
          ...t,
          showValue: t.label,
        })),
      })
      list.push({
        field: 'rootResourceQuery',
        label: i18n.t('modules.views.appMonitor.errorDetail.s_915a9dc7') as string, labelKey: 'modules.views.appMonitor.errorDetail.s_915a9dc7',
        likeable: true,
        type: 'select',
        children: this.srcResource.map(t => ({
          ...t,
          showValue: t.label,
        })),
      })
    }
    return list
  }

  private filterList: any[] = []
  private siList: any[] = []
  private srcResource: any[] = []
  private resourceList: any[] = [];
  private errorList: any[] = [];

  private serviceInstanceMap: any = {} // 服务的实例map

  @Watch('timeParams', { deep: true })
  private watchQueryParams () {
    this.serviceInstanceMap = {}
  }

  public async init () {
    // 搜索参数回显
    const routerQuery = this.$route.query
    Object.keys(this.queryParams).forEach(k => {
      if (routerQuery[k]) {
        this.queryParams[k] = decodeURIComponent(String(routerQuery[k]))
      }
    })
    const serviceId = this.queryParams.sid
    if (serviceId) {
      await this.getServiceInstance(serviceId)
      await this.getSrcResourceList();
    } else {
      this.queryParams.si = ''
      this.queryParams.rootResourceQuery = ''
    }
    await this.getResourceList();
    await this.getResourceList('exceptionName');

    this.filterList = [...this.filterListInit]
    const query = { ...this.queryParams };

    return { ...query }
  }

  private async handleChange ({ row, selected }: { row: TagItem, selected: FormatedSelected[] }) {
    this.$emit('on-search-loading', true);
    if ((row || {}).field === 'sid') {
      // 操作服务时，清空服务实例和上游请求
      this.queryParams.si = ''
      this.queryParams.rootResourceQuery = ''
      this.queryParams.resourceQuery = ''
      const _query = {...this.$route.query}
      delete _query.si
      delete _query.rootResourceQuery
      delete _query.resourceQuery
      this.$router.replace({ query: { ..._query } })
    }
    const query = { ...this.queryParams }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    });
    this.init();
    this.$emit('on-change', { ...query })
  }

  private handleRemove ({field}: {field: string}) {
    if (field === 'sid') {
      // 操作服务时，清空服务实例和上游请求
      this.queryParams.si = ''
      this.queryParams.rootResourceQuery = ''
      this.queryParams.resourceQuery = ''
      const _query = {...this.$route.query}
      delete _query.si
      delete _query.rootResourceQuery
      delete _query.resourceQuery
      this.$router.replace({ query: { ..._query } })
    }
    const query = { ...this.queryParams }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    });
    this.init();
    this.$emit('on-change', { ...query })
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
      this.siList = list.map((si: string) => ({ label: si, value: si }))
    }
  }

  private async getResourceList (type: 'resource'|'exceptionName' = 'resource') {
    const { fromTime, toTime } = this.getGlobalTimeV2()
    // 请求列表
    const { result: resRst, error: resErr } = await toAsyncWait(ServiceApi.getServiceRequestByCompTypes({
      field: type,
      isIn: type === 'resource' ? 1 : null,
      componentType: type === 'resource' ? null : 'service.exception',
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
        if (type === 'resource') {
          this.resourceList = _resList
        } else {
          this.errorList = _resList
        }
      } else {
        if (type === 'resource') {
          this.resourceList = []
        } else {
          this.errorList = []
        }
      }
      
    } else {
      if (type === 'resource') {
        this.resourceList = []
      } else {
        this.errorList = []
      }
    }
  }

  private async getSrcResourceList () {
    const { result, error } = await toAsyncWait(ServiceApi.getSrcServices({
      serviceId: this.queryParams.sid,
      fromTime: dayjs(this.timeParams.fromTime).format('YYYY-MM-DD HH:mm:ss'),
      toTime: dayjs(this.timeParams.toTime).format('YYYY-MM-DD HH:mm:ss'),
      field: 'rootResource'
    }))
    if (!error) {
      const { data = {} } = result || {};
      try {
        let srcResources: any = [];
        const resources = Object.values(data || {})?.flat().flatMap((i: any) => i.rootResource);
        if (Array.isArray(resources)) {
          srcResources = srcResources.concat([...new Set(resources)].map((r: string) => ({
            label: r, value: r,
          })))
        }
        this.srcResource = srcResources
      } catch (err) {
        console.log(err)
      }
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
