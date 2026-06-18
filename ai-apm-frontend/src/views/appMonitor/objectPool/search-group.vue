<template>
  <query-filter
    v-model="queryParams"
    :filter-list="filterList"
    :updateRoute="true"
    @on-change="handleChange"
    @on-remove-tag="handleRemoveTag" />
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { orderBy } from 'lodash';
import QueryFilter from '@/components/query-filter/index.vue';
import { TagItem, FormatedSelected } from '@/components/query-filter/types/index.types';
import { ServiceTypeFilter } from '@/utils/filters/service'
import { StringIsEmpty } from '@/utils/common'
import { toAsyncWait } from '@/utils/common';
import ServiceApi from '@/api/service';
import MetricApi from '@/api/metric';
import dayjs from 'dayjs';

const PoolMetric = 'service.object.pool.maxSize'
const PoolNameTag = 'objectPoolName'

@Component({
  components: {
    QueryFilter,
  },
})
export default class SearchGroup extends Vue {
  @Prop({ default: () => ({}) }) private timeParams!: any;

  private queryParams: any = {
    // metric: '',
    poolName: '',
    sid: '',
    si: '',
  }

  get filterListInit () {
    const list = [
      // {
      //   field: 'metric',
      //   label: i18n.t('modules.views.appMonitor.dbConnPool.s_b8403584') as string, labelKey: 'modules.views.appMonitor.dbConnPool.s_b8403584',
      //   type: 'input',
      //   children: [],
      // },
      {
        field: 'poolName',
        label: i18n.t('modules.views.appMonitor.objectPool.s_be3f9ead') as string, labelKey: 'modules.views.appMonitor.objectPool.s_be3f9ead',
        type: 'select',
        children: [...this.poolList],
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

  private serviceList: any[] = []
  private serviceInstanceMap: any = {} // 服务的实例map
  private poolList: any[] = []

  @Watch('timeParams', { deep: true })
  private watchQueryParams () {
    this.serviceInstanceMap = {}
    this.poolList = []
  }

  public async init () {
    if (!this.serviceList.length) {
      await this.getServiceList()
    }
    if (!this.poolList.length) {
      await this.getPoolList()
    }
    // 搜索参数回显
    const routerQuery = this.$route.query
    Object.keys(this.queryParams).forEach(k => {
      if (routerQuery[k]) {
        this.queryParams[k] = decodeURIComponent(String(routerQuery[k]))
      }
    })
    const serviceId = this.queryParams.sid
    if (serviceId && !this.serviceInstanceMap[serviceId]) {
      await this.getServiceInstance(serviceId)
    } else {
      this.queryParams.si = ''
    }

    this.filterList = [...this.filterListInit]
    const query = { ...this.queryParams }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    })
    return { ...query }
  }

  private async handleChange ({ row, selected }: { row: TagItem, selected: FormatedSelected[] }) {
    if ((row || {}).field === 'sid') {
      // 操作服务时，清空服务实例
      this.queryParams.si = '';
      const _query = {...this.$route.query}
      delete _query.si
      this.$router.replace({ query: { ..._query } })

      await this.getServiceInstance((row || {})?.value as string)
    }
    this.filterList = [...this.filterListInit]
    const query = { ...this.queryParams }
    Object.entries(query).forEach(([key, value]) => {
      if (StringIsEmpty(value)) {
        delete query[key]
      }
    })
    this.$emit('on-change', { ...query })
  }

  private async handleRemoveTag (payload: { field: string, value: any, selected: any[] }) {
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
    })
    this.$emit('on-change', { ...query })
  }

  // 获取服务列表
  private async getServiceList () {
    const { result, error } = await toAsyncWait(ServiceApi.getServicesIds({ fromTime: '', toTime: '', ignoreTime: 1 }))
    if (!error) {
      const { data = [] } = result || {};
      const serviceNameIdMap: any = {}
      data.forEach((t: any) => {
        serviceNameIdMap[t.name] = { id: t.id, type: t.service_type }
      });
      this.serviceList = orderBy(Object.keys(serviceNameIdMap), [t => t.toLocaleLowerCase()], ['asc'])
          .map(t => ({
            label: t,
            value: serviceNameIdMap[t].id,
            info: {
              type: serviceNameIdMap[t].type,
              texts: ['服务类型：' + ServiceTypeFilter(serviceNameIdMap[t].type)],
            },
          }))
    }
  }

  // 获取服务的实例
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

  // 获取池子列表
  private async getPoolList () {
    const params = {
      start: +new Date(this.timeParams.fromTime),
      end: +new Date(this.timeParams.toTime),
      metrics: [PoolMetric],
      by: [PoolNameTag],
      from: [],
    }
    const { result, error } = await toAsyncWait(MetricApi.getMetricLastTags(params))
    if (!error) {
      const poolNames: string[] = (result.data || {})[PoolNameTag] || [];
      this.poolList = poolNames.filter(t => !!t).map((t: any) => ({ label: t, value: t }))
      const poolName = decodeURIComponent(this.$route.query.poolName as string || '')
      if (this.poolList.find((t: any) => t.value === poolName)) {
        this.queryParams.poolName = poolName
      } else {
        this.queryParams.poolName = ''
      }
      if (poolName !== this.queryParams.poolName) {
        this.$router.replace({ query: { ...this.$route.query, poolName: this.queryParams.poolName } })
      }
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
