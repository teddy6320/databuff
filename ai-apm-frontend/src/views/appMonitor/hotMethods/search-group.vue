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
import ServiceApi from '@/api/service';
import dayjs from 'dayjs';

@Component({
  components: {
    QueryFilter,
  },
})
export default class SearchGroup extends Vue {
  @Prop({ default: () => ({}) }) private timeParams!: any;

  get serviceId () {
    return decodeURIComponent((this.$route.query.sid as string || ''))
  }

  private queryParams: any = {
    si: '',
    resource: '',
    onOperation: '',
    traceId: '',
  }

  get filterListInit () {
    const list = [
      {
        field: 'si',
        label: i18n.t('modules.views.alarmCenter.alarm.s_71673bab') as string, labelKey: 'modules.utils.filters.s_71673bab',
        type: 'select',
        children: this.serviceInstanceList.map(t => ({ label: t, value: t, showValue: t })),
      },
      {
        field: 'resource',
        label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c',
        type: 'select',
        likeable: true,
        children: this.resourceList.map(t => ({ label: t, value: t, showValue: t })),
      },
      {
        field: 'onOperation',
        label: i18n.t('modules.views.alarmCenter.eventDetail.s_de9cc3dd') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_de9cc3dd',
        type: 'select',
        likeable: true,
        children: this.operationList.map(t => ({ label: t, value: t, showValue: t })),
      },
      {
        field: 'traceId',
        label: 'TraceID',
        type: 'input',
        likeable: true,
        children: [],
      },
    ]
    return list
  }

  private filterList: any[] = []

  private paramsLoaded = false;
  private serviceInstanceList: string[] = [] // 服务的实例
  private resourceList: any[] = [];
  private operationList: any[] = [];

  @Watch('timeParams', { deep: true })
  private watchQueryParams () {
    this.paramsLoaded = false;
    this.serviceInstanceList = []
    this.resourceList = []
    this.operationList = []
  }

  public async init () {
    // 搜索参数回显
    const routerQuery = this.$route.query
    Object.keys(this.queryParams).forEach(k => {
      if (routerQuery[k]) {
        this.queryParams[k] = decodeURIComponent(String(routerQuery[k]))
      }
    })
    if (this.serviceId && !this.paramsLoaded) {
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
      fromTime: dayjs(this.timeParams.fromTime).format('YYYY-MM-DD HH:mm:ss'),
      toTime: dayjs(this.timeParams.toTime).format('YYYY-MM-DD HH:mm:ss'),
      serviceId: this.serviceId,
      // fields: ['serviceInstance', 'resource', 'onOperation'],
    }
    const { result, error } = await toAsyncWait(ServiceApi.getProfilingParams(params))
    if (!error) {
      const data = (result || {}).data || {}
      this.serviceInstanceList = data.serviceInstance || []
      this.resourceList = data.resource || []
      this.operationList = data.onOperation || []
      this.paramsLoaded = true;
      this.$emit('on-types-loaded', data.eventType || [])
    } else {
      this.serviceInstanceList = []
      this.resourceList = []
      this.operationList = []
    }
  }
}
</script>
