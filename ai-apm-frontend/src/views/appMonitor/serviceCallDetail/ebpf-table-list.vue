<template>
  <div class="table-list">
    <db-table
      ref="listTable"
      :queryApi="queryApi"
      :queryParams="getQueryParams"
      :timeMode="false"
      :autoRefresh="false"
      :offsetMode="true"
      :columnConfig="columnConfig"
      :formatFunc="formatFunc"
      @sort-change="tableRefresh">
      <span slot='total' slot-scope="{ total }" class="describe">
        <span class="default-text font-14 mr-10">{{ $t('modules.views.appMonitor.errorDetail.s_5c7753a2') }}</span>
        <span>{{ $t('modules.views.alarmCenter.alarm.s_9ad9830c', { value0: total }) }}</span>
      </span>
      <el-table-column slot="suffix" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" :min-width="100">
        <template slot-scope="{ row }">
          <a
            @click.stop="viewErrorStackHandle(row)"
            class="blue">{{ $t('modules.views.appMonitor.resourceDetail.s_ab4c88f0') }}</a>
        </template>
      </el-table-column>
    </db-table>

    <!-- 错误堆栈弹框 -->
    <el-dialog
      :visible.sync="showStackDialog"
      :title="$t('modules.views.appMonitor.resourceDetail.s_e5e94591')"
      width="480px">
      <div class="stack-dialog-cont">
        <code-view :code="stackInfo" />
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import CodeView from '@/components/code-view.vue';
import ServiceApi from '@/api/service';

@Component({
  components: {
    CodeView,
  },
})
export default class TableList extends Vue {
  @Prop({ default: () => ({}) }) private query!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;

  get getQueryParams () {
    const params: any = {
      ...this.query,
      fromTime: this.timeParams.fromTime,
      toTime: this.timeParams.toTime,
    }
    Object.entries(params).forEach(([key, value]) => {
      if (value === '') {
        delete params[key]
      }
    })
    return params;
  }

  private queryApi = ServiceApi.getServiceCallLightApmErrSpans
  private columnConfig = [
    { field: 'startTime', label: i18n.t('modules.views.alarmCenter.eventDetail.s_592c5958') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_592c5958', minWidth: 140, sortable: true, defaultSort: 'desc' },
    { field: 'resource', label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c', minWidth: 180 },
    { field: '_client', label: i18n.t('modules.views.appMonitor.resourceDetail.s_26791765') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_26791765', minWidth: 120 },
    { field: 'srcService', label: i18n.t('modules.views.appMonitor.serviceCallDetail.s_69c948ee') as string, labelKey: 'modules.views.appMonitor.serviceCallDetail.s_69c948ee', minWidth: 120 },
    { field: 'srcServiceInstance', label: i18n.t('modules.views.appMonitor.serviceCallDetail.s_d706f6d9') as string, labelKey: 'modules.views.appMonitor.serviceCallDetail.s_d706f6d9', minWidth: 120 },
    { field: 'service', label: i18n.t('modules.views.appMonitor.serviceCallDetail.s_4cedbd07') as string, labelKey: 'modules.views.appMonitor.serviceCallDetail.s_4cedbd07', minWidth: 120 },
    { field: 'serviceInstance', label: i18n.t('modules.views.appMonitor.serviceCallDetail.s_109f5e5f') as string, labelKey: 'modules.views.appMonitor.serviceCallDetail.s_109f5e5f', minWidth: 120 },
    { field: 'protocol', label: i18n.t('modules.views.appMonitor.resourceDetail.s_faa1ad5e') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_faa1ad5e', minWidth: 80 },
    { field: 'allCount', label: i18n.t('modules.views.appMonitor.resourceDetail.s_0f81e359') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_0f81e359', minWidth: 80, unit: 'count', sortable: true },
    { field: 'errorCount', label: i18n.t('modules.views.appMonitor.resourceDetail.s_f9e62864') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_f9e62864', minWidth: 80, unit: 'count', sortable: true },
    { field: 'reqBodyLength', label: i18n.t('modules.views.appMonitor.resourceDetail.s_0951ecd4') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_0951ecd4', minWidth: 120, unit: 'b', sortable: true },
    { field: 'respBodyLength', label: i18n.t('modules.views.appMonitor.resourceDetail.s_878d0b66') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_878d0b66', minWidth: 120, unit: 'b', sortable: true },
  ]

  public async getData () {
    this.tableRefresh()
  }

  private formatFunc (data: any) {
    return data.map((t: any) => ({
      ...t,
      _client: [t.isOut ? i18n.t('modules.views.appMonitor.resourceDetail.s_efc6882b') as string : '', t.isIn ? '服务端' : ''].filter(i => !!i).join(','),
    }));
  }
  private tableRefresh () {
    (this.$refs.listTable as any)?.refresh()
  }

  // 查看错误堆栈
  private showStackDialog = false;
  private stackInfo: string = '';
  private async viewErrorStackHandle (row: any) {
    try {
      const tags = JSON.parse(row.tags || '{}');
      this.stackInfo = tags.errorMsgDetail || '';
    } catch (error) {
      this.stackInfo = ''
    }
    this.showStackDialog = true;
  }
}
</script>

<style lang="scss" scoped>
.table-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--bg-color);
  padding: 10px 20px 20px;
}

.stack-dialog-cont {
  min-height: 100px;
}
</style>
