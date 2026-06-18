<template>
  <div class="table-list-warp flex-v">
    <div class="mb-16">
      <db-radio v-model='currType' :options='options' @change="typeChangeHandle"></db-radio>
    </div>

    <div class="flex-h-jc">
      <el-input
        v-model="queryParams.query"
        @change="queryChange"
        clearable size="small"
        maxlength="100"
        prefix-icon="db-icon-search"
        :placeholder="$t('modules.views.sysManage.group.s_c17ee3e1')"
      />

      <div class="flex-h ml-10">
        <el-button
          @click="bindCurrGroup"
          :disabled="!selection.length"
          :type="selection.length ? 'primary' : ''"
          plain size="small">{{ $t('modules.views.sysManage.group.s_4af718ba') }}</el-button>
        <el-button
          @click="bindCreateCurrGroup"
          :disabled="!selection.length"
          :type="selection.length ? 'primary' : ''"
          plain size="small">{{ $t('modules.views.sysManage.group.s_5acfc339') }}</el-button>
      </div>
    </div>

    <db-table
      :queryApi='queryApi'
      :queryParams='tableQueryParams'
      :columnConfig='getCloumns'
      :autoRefresh='false'
      @on-table-inited='tableInitedHandle'
      @selection-change="selectChangeHandle"
      row-key='id'
      tableKey='SYSTEM_GROUP_ENTITY'
      ref='listTable'>
    
      <template slot="prefix">
        <el-table-column
          type="selection" align="center" header-align="center" width="50"
          label-class-name="table-selector-col-header" />
      </template>

    </db-table>

    <bind-dialog v-model='showDialog' :groupList='groupList' :type='currType' :selection='selection' @on-close='cancelDialog'></bind-dialog>
    <create-dialog v-model='showCreateDialog' :groupList='groupList' :type='currType' :selection='selection' @on-close='cancelCreateDialog'></create-dialog>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import GroupApi from '@/api/group';
import BindDialog from './bind-dialog.vue'
import CreateDialog from './create-dialog.vue'
import { toAsyncWait } from '@/utils/common';

@Component({
  components: {
    BindDialog, CreateDialog
  }
})
export default class TableList extends Vue {

  public $refs!: {
    listTable: any,
  }

  private currType = 'host'

  private options: any[] = [
    { label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369', value: 'host' },
    { label: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_47d68cd0', value: 'service' },
    { label: 'Namespace', value: 'namespace' },
  ]
  private queryParams = {
    query: ''
  }
  private tableInited = false;
  private selection: any[] = [];

  private showDialog = false;
  private showCreateDialog = false;

  private queryApi = GroupApi.getUngroupList;

  get isHost () {
    return this.currType === 'host'
  }

  get tableQueryParams () {
    // const { fromTime, toTime } = this.getGlobalTimeV2();
    const queryParams: any = {
      // fromTime, toTime,
      ...this.queryParams,
    }
    switch (this.currType) {
      case 'namespace':
        return { ...queryParams, objType: 'namespace' };
      case 'service':
        return { ...queryParams, objType: 'service' };
      case 'host':
      default:
        return { ...queryParams, objType: 'host' };
    }
  }

  get getCloumns () {
    switch (this.currType) {
      case 'service':
        return [
          { field: 'name', prop: 'name', label: i18n.t('modules.views.alarmCenter.alarm.s_8f3747c0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_8f3747c0', minWidth: 120, handleClick: this.viewServiceHandle },
          { field: 'service', prop: 'service', label: i18n.t('modules.views.sysManage.group.s_3a06d4a1') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_3a06d4a1', minWidth: 120 },
          { field: 'customTags', prop: 'customTags', label: i18n.t('modules.views.metrics.list.s_14d34236') as string, labelKey: 'modules.views.infrastructure.hostDetail.s_14d34236', minWidth: 200 },
        ];
      case 'namespace':
        return [
          { field: 'namespaceName', prop: 'namespaceName', label: i18n.t('modules.views.infrastructure.cluster.s_d7ec2d3f') as string, labelKey: 'modules.views.aiPlatform.experts.s_d7ec2d3f', minWidth: 120, handleClick: this.viewNamespaceHandle },
          { field: 'clusterName', prop: 'clusterName', label: i18n.t('modules.views.infrastructure.cluster.s_85fe5099') as string, labelKey: 'modules.views.infrastructure.cluster.s_85fe5099', minWidth: 120 },
        ];
      case 'host':
      default:
        return [
          { field: 'hostName', prop: 'hostName', label: i18n.t('modules.views.configManage.alarm.s_3d022a63') as string, labelKey: 'modules.views.configManage.alarm.s_3d022a63', minWidth: 200, handleClick: this.viewHostDetail },
          { field: 'hostIp', prop: 'hostIp', label: i18n.t('modules.views.configManage.entity.s_2dc9105c') as string, labelKey: 'modules.views.configManage.entity.s_2dc9105c', minWidth: 200 },
          { field: 'tags', prop: 'tags', label: i18n.t('modules.views.metrics.list.s_14d34236') as string, labelKey: 'modules.views.infrastructure.hostDetail.s_14d34236', minWidth: 400 },
        ];
    }
  }

  public get tableListCanInit () {
    return this.isMounted && this.tableInited
  }

  public isMounted = false;

  private groupList: any[] = [];

  private created () {
    const { type, query } = this.$route.query
    if (['service', 'host', 'namespace'].includes(String(type))) {
      this.currType = String(type)
    } else if (String(type) === 'business') {
      this.currType = 'host'
    }
    if (query) {
      this.queryParams.query = String(query);
    }
    this.getGroupList();
  }
  private mounted () {
    this.isMounted = true;
  }

  private typeChangeHandle () {
    this.selection = [];
    this.$router.replace({ query: { ...this.$route.query, type: this.currType } })
    this.getData()
  }

  private queryChange () {
    if (this.queryParams.query) {
      this.$router.replace({ query: { ...this.$route.query, query: this.queryParams.query } })
    } else {
      const query = { ...this.$route.query }
      delete query.query
      this.$router.replace({ query: { ...query } })
    }
    this.getData()
  }

  public getData () {
    this.$nextTick(() => {
      this.$refs.listTable && this.$refs.listTable?.refresh()
    })
  }

  private tableInitedHandle () {
    this.tableInited = true;
    this.$refs.listTable?.refresh();
  }

  private selectChangeHandle (selection: any) {
    this.selection = [...selection];
  }

  private bindCurrGroup () {
    this.showDialog = true;
  }
  private bindCreateCurrGroup () {
    this.showCreateDialog = true;
  }
  private cancelDialog (payload?: { refresh: boolean }) {
    this.showDialog = false;
    if (payload?.refresh) {
      this.$refs.listTable?.refresh();
    }
  }
  private cancelCreateDialog (payload?: { refresh: boolean }) {
    this.showCreateDialog = false;
    if (payload?.refresh) {
      this.$refs.listTable?.refresh();
    }
  }

  private async getGroupList () {
    const { result, error } = await toAsyncWait(GroupApi.getGroupList({}))
    if (!error) {
      const { data = [] } = result || {};
      this.groupList = Array.isArray(data) ? data : [];
    }
  }

  private viewServiceHandle (row: any) {
    this.$router.push({
      path: '/appMonitor/serviceDetail',
      query: {
        sid: encodeURIComponent(row.serviceId),
      }
    })
  }

  private viewHostDetail (row: any) {
    this.$router.push({
      path: '/infrastructure/hostDetail',
      query: { hostName: encodeURIComponent(row.hostName) }
    })
  }

  private viewNamespaceHandle (row: any) {
    const { clusterId, clusterName, namespaceName } = row
    this.$router.push({
      path: '/infrastructure/namespace',
      query: {
        tags: [`clusterId:${clusterId}:${clusterName} (${clusterId})`, `namespaceName:${namespaceName}`]
      }
    })
  }

}
</script>

<style lang="scss" scoped>
.table-list-warp {
  height: 100%;
  overflow: hidden;
}
</style>
