<template>
  <query-filter
    v-model="queryParams"
    :filter-list="filterList"
    :updateRoute="true"
    @on-change="handleChange"
    @field-choose="fieldChooseHandle"
    @on-remove-tag="d => handleChange({ row: d }, 'remove')" />
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import QueryFilter from '@/components/query-filter/index.vue';
import { StringIsEmpty } from '@/utils/common'
import { toAsyncWait } from '@/utils/common';
import ServiceApi from '@/api/service';
import MonitorApi from '@/api/monitor';
import dayjs from 'dayjs';
import { orderBy, uniqBy } from 'lodash';

@Component({
  components: {
    QueryFilter,
  },
})
export default class SearchGroup extends Vue {
  private queryParams: any = {
    ruleName: '',
    classification: '',
    enabled: '',
    host: '',
    pname: '',
    serviceId: '',
    serviceInstance: '',
  }

  get filterListInit () {
    const list = [
      {
        field: 'ruleName',
        label: i18n.t('modules.views.appMonitor.serviceDetail.s_87080256') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_87080256',
        type: 'input',
        children: [],
      },
      {
        field: 'classification',
        label: i18n.t('modules.views.appMonitor.serviceDetail.s_986329a3') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_986329a3',
        type: 'select',
        children: [
          { label: i18n.t('modules.views.configManage.alarm.s_150b3f91') as string, labelKey: 'modules.utils.filters.s_150b3f91', value: 'singleMetric' },
        ],
      },
      {
        field: 'enabled',
        label: i18n.t('modules.views.appMonitor.serviceDetail.s_2b82bf9a') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_2b82bf9a',
        type: 'select',
        children: [
          { label: i18n.t('modules.views.configInstall.dataAccess.s_53ace430') as string, labelKey: 'modules.views.configInstall.dataAccess.s_53ace430', value: true },
          { label: i18n.t('modules.views.configInstall.dataAccess.s_69b0f684') as string, labelKey: 'modules.views.configInstall.dataAccess.s_69b0f684', value: false },
        ],
      },
      {
        field: 'host',
        label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369',
        type: 'select',
        children: [...this.hostList],
      },
      {
        field: 'pname',
        label: i18n.t('modules.views.alarmCenter.alarm.s_f88522cf') as string, labelKey: 'modules.views.alarmCenter.alarm.s_f88522cf',
        type: 'select',
        children: [...this.processList],
      },
      {
        field: 'serviceId',
        label: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_47d68cd0',
        type: 'select',
        children: [...this.serviceList],
      },
    ]
    const serviceId = this.queryParams.serviceId
    if (serviceId) {
      const children = (this.serviceInstanceMap[serviceId] || []).map((t: any) => ({ label: t, value: t }))
      list.push({
        field: 'serviceInstance',
        label: i18n.t('modules.views.alarmCenter.alarm.s_71673bab') as string, labelKey: 'modules.utils.filters.s_71673bab',
        type: 'select',
        children,
      })
    }
    return list
  }

  private filterList: any[] = []

  private entityLoaded: any = {
    host: false,
    pname: false,
    serviceId: false,
  }
  private hostList: any[] = [] // 主机
  private processList: any[] = [] // 进程
  private serviceList: any[] = [] // 服务
  private serviceInstanceMap: any = {} // 服务的实例map

  public async init () {
    // 搜索参数回显
    const routerQuery = this.$route.query
    for (const k of Object.keys(this.queryParams)) {
      if (k === 'enabled') {
        if (['true', 'false'].includes(routerQuery[k] as string)) {
          this.queryParams.enabled = routerQuery[k] !== 'false'
        } else {
          this.queryParams.enabled = ''
        }
      } else if (routerQuery[k]) {
        this.queryParams[k] = decodeURIComponent(String(routerQuery[k]))
        if (Object.hasOwn(this.entityLoaded, k) && !this.entityLoaded[k]) {
          const list = await this.getEntityObjects(k)
          if (!list.find(t => t.value === this.queryParams[k])) {
            this.queryParams[k] = ''
          }
        }
      } else {
        this.queryParams[k] = ''
      }
    }
    const serviceId = this.queryParams.serviceId
    if (serviceId) {
      await this.getServiceInstance(serviceId)
    } else {
      this.queryParams.serviceInstance = ''
    }

    this.filterList = [...this.filterListInit];
    return { ...this.queryParams }
  }

  private async handleChange ({ row }: { row: any }, type?: string) {
    if ((row || {}).field === 'serviceId') {
      // 操作服务时，清空服务实例
      this.queryParams.serviceInstance = '';
      const _query = {...this.$route.query}
      delete _query.serviceInstance
      this.$router.replace({ query: { ..._query } })
      if (type !== 'remove') {
        await this.getServiceInstance((row || {})?.value as string)
      }
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

  private async fieldChooseHandle (field: string) {
    if (Object.hasOwn(this.entityLoaded, field) && !this.entityLoaded[field]) {
      const list = await this.getEntityObjects(field)
      this.$nextTick(() => {
        this.filterList = this.filterList.map((item: any) => {
          if (item.field === field) {
            item.checked = true
            item.disabled = true
            item.children = [...list]
          } else {
            item.checked = false
            item.disabled = false
          }
          return item
        })
      })
    }
  }

  private async getEntityObjects (type: string) {
    const params: any = { type: type === 'serviceId' ? 'service' : type }
    const { result, error } = await toAsyncWait(MonitorApi.getEntityObjects(params))
    if (!error) {
      const data: any = (result || {}).data || {}
      let list: any[] = []
      Object.entries(data).forEach(([key, item]: [string, any]) => {
        list = (item || []).map((t: any) => {
          if (key === 'pname') {
            return { label: t.pname, value: t.pname, }
          } else if (key === 'service') {
            return { label: t.name, value: t.id, }
          }
          return { label: t.name, value: t.name, }
        })
        list = orderBy(uniqBy(list, 'value'), [(t: any) => t.label.toLocaleLowerCase()], ['asc'])
        if (key === 'host') {
          this.hostList = list
        } else if (key === 'pname') {
          this.processList = list
        } else if (key === 'service') {
          this.serviceList = list
        }
      })
      this.entityLoaded[type] = true
      return list
    }
    return []
  }

  private async getServiceInstance (sid: string) {
    // 最近1小时
    const params = {
      serviceId: sid,
      fromTime: dayjs(+new Date() - 3600 * 1000).format('YYYY-MM-DD HH:mm') + ':00',
      toTime: dayjs(+new Date()).format('YYYY-MM-DD HH:mm') + ':00',
    }
    const { result, error } = await toAsyncWait(ServiceApi.getBasicServiceInstance(params))
    if (!error) {
      const list = ((result.data || {}).serviceInstances || []).map((t: any) => t.serviceInstance)
      this.$set(this.serviceInstanceMap, sid, list);
    }
  }
}
</script>
