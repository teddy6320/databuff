<template>
  <div class="service-cont">
    <div class="service-wrapper flex-v">
      <div class="list-header flex-h">
        <query-filter
          v-model='queryParams'
          :updateRoute='true'
          :filter-list="filterList"
          @on-change="handleChange"
          @on-remove-tag="handleRemoveTag" />
      </div>

      <service-list
        :queryParams='getQueryParams'
        @on-table-inited='tableInitedHandle'
        ref="serviceListComp" />
    </div>
 
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import ServiceList from './serviceList.vue';
import { toAsyncWait } from '@/utils/common';
import ServiceApi from '@/api/service'
import { TagItem, FormatedSelected } from '@/components/query-filter/types/index.types';
import QueryFilter from '@/components/query-filter/index.vue';

@Component({
  components: {
    ServiceList,
    QueryFilter
  },
})
export default class ServiceManage extends Vue {

  public $refs!: {
    serviceListComp: ServiceList;
  };


  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.durationChangeHandle()
  }

  private queryParams: any = {
    serviceName: '',
    componentType: 'service.remote'
  }

  get getQueryParams () {
    return {
      ...this.queryParams,
    }
  }

  private timeParams = {
    fromTime: '',
    toTime: '',
  }

  private filterList: any[] = []

  private serviceList: any[] = []

  private listLoading = false;


  get filterListInit () {
    const list = [
      {
        field: 'serviceName',
        label: i18n.t('modules.views.appMonitor.external.s_0d496d78') as string, labelKey: 'modules.views.appMonitor.external.s_0d496d78',
        type: 'select',
        likeable: true,
        children: this.serviceList.map(t => ({
          ...t,
          showValue: t.label,
        })),
      },
    ]
    return list
  }

  private async created () {
    //
  }


  private mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    });
  }

  private beforeDestroy () {
    // 清除监听全局刷新事件
    this.$eventBus.$off('GlobalRefresh');
  }


  private async init () {
    if (!this.serviceList.length) {
      await this.getBaseServiceList()
    }

    // 搜索参数回显
    const routerQuery = this.$route.query
    const query: any = { ...this.queryParams }
    Object.keys(query).forEach(k => {
      if (routerQuery[k]) {
        query[k] = decodeURIComponent(String(routerQuery[k]))
      }
    });
    this.queryParams = query;
    this.filterList = this.filterListInit;
    return { ...this.queryParams }
  }

  private handleRemoveTag ({field}: {field: string}) {
    this.durationChangeHandle()
  }

  private handleChange ({ row, selected }: { row: TagItem, selected: FormatedSelected[] }) {
    this.durationChangeHandle()
  }

  private async getBaseServiceList () {
    const { fromTime, toTime } = this.getGlobalTimeV2()
    const { result, error } = await toAsyncWait(ServiceApi.getServicesIds({
      fromTime,
      toTime,
      ignoreTime: 1,
      serviceType: 'remote',
      virtualService: 1
    }))
    if (!error) {
      const { data = [] } = result || {};
      this.serviceList = (data || []).map((t: any) => ({
        label: t.name,
        value: t.name,
      }))
    }
  }

  // 时间范围改变
  private async durationChangeHandle () {
    this.regetGlobalTime()
    this.init().then((queryParams) => {
      this.$refs.serviceListComp?.refresh();
    })
  }

  private regetGlobalTime () {
    const { fromTime, toTime } = this.getGlobalTimeV2()
    this.timeParams = {
      fromTime, toTime
    }
  }

  private searchChangeHandle (data: any) {
    this.durationChangeHandle()
  }
  private tableInitedHandle () {
    this.durationChangeHandle();
  }

}
</script>

<style lang="scss" scoped>
.service-cont {
  flex: 1;
  height: 100%;
  padding: 16px;
  background-color: var(--bg-color-base);
  position: relative;
  overflow: hidden;

  .service-wrapper {
    position: relative;
    width: 100%;
    height: 100%;
    padding: 20px;
    background-color: var(--bg-color);
    overflow-y: auto;
  }

  .list-header {
    .search-group {
      flex: 1;
      flex-wrap: nowrap;
    }
  }

  .service-type-btn {
    font-size: 14px;
    font-weight: normal;
    color: var(--color-text-primary);
    padding: 8px 15px;
    &.active {
      background: var(--color-primary);
      border-color: var(--color-primary);
      color: #fff;
      z-index: 1;
    }
  }
}
.fn {
  flex: none;
}
</style>
