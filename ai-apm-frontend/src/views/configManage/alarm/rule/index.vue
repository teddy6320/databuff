<template>
  <div class="list-wrapper">
    <div class="list-header flex-h">
      <search-group
        ref="searchGroup"
        @on-change="searchChangeHandle"
        class="search-group"
      />

      <div class="ml-10">
        <el-button
          v-if="hasAlarmManageAuthV1"
          @click="batchDeleteHandle"
          :disabled="!selection.length"
          :type="selection.length ? 'primary' : ''"
          plain size="small">{{ $t('modules.views.configInstall.dataAccess.s_7fb62b30') }}</el-button>
        <el-button
          v-if="hasAlarmManageAuthV1"
          @click="batchToggleEnableHandle(true)"
          :disabled="!selection.length"
          :type="selection.length ? 'primary' : ''"
          plain size="small">{{ $t('modules.views.configInstall.dataAccess.s_31bfc7fa') }}</el-button>
        <el-button
          v-if="hasAlarmManageAuthV1"
          @click="batchToggleEnableHandle(false)"
          :disabled="!selection.length"
          :type="selection.length ? 'primary' : ''"
          plain size="small">{{ $t('modules.views.configInstall.dataAccess.s_3b6692ab') }}</el-button>
        <el-button
          v-if="hasAlarmManageAuthV1"
          @click="viewSettingHandle"
          type="primary" size="small" class="ml-10 flex-none">
          <i class="db-icon-add"></i> {{ $t('modules.views.configManage.alarm.s_26bb8418') }}</el-button>
      </div>
    </div>

    <table-list
      ref="tableList"
      :isSystemRule="isSystemRule"
      :query="queryParams"
      v-loading="queryLoading"
      @selection-change="selectionChangeHandle"
      class="list-cont"
    />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import SearchGroup from './search-group.vue';
import TableList from './table-list.vue';
import { ALARM_DETAIL_PATH, buildAlarmDetailLocation } from '../alarm-routes';

@Component({
  components: {
    SearchGroup,
    TableList,
  }
})
export default class RuleList extends Vue {
  @Prop({ default: '' }) private type!: any;

  public $refs!: {
    searchGroup: SearchGroup
    tableList: TableList
  }

  get isSystemRule () {
    return this.type === 'system'
  }

  private queryParams = {}
  private queryLoading = false;

  private async mounted () {
    this.queryLoading = true;
    this.$refs.searchGroup.init().then((data: any) => {
      this.queryParams = { ...data }
      this.$nextTick(() => {
        this.queryLoading = false;
        this.$refs.tableList && this.$refs.tableList.getData()
      })
    }).catch(() => this.queryLoading = false)
  }

  private searchChangeHandle (data: any) {
    if (JSON.stringify(data) === JSON.stringify(this.queryParams)) {
      return
    }
    this.queryParams = { ...data }
    this.$nextTick(() => {
      this.$refs.tableList && this.$refs.tableList.getData()
    })
  }

  // 新建
  private viewSettingHandle () {
    if (!this.hasAlarmManageAuthV1) {
      return
    }
    const path = this.isSystemRule
      ? ALARM_DETAIL_PATH.systemRuleSetting
      : ALARM_DETAIL_PATH.ruleSetting
    this.$router.push(buildAlarmDetailLocation(path, this.$route.query))
  }

  // 批量删除
  private batchDeleteHandle () {
    if (!this.hasAlarmManageAuthV1) {
      return
    }
    if (!this.selection.length) {
      return;
    }
    this.$refs.tableList && this.$refs.tableList.batchDeleteHandle()
  }

  // 批量启用/停用
  private batchToggleEnableHandle (enabled: boolean) {
    if (!this.hasAlarmManageAuthV1) {
      return
    }
    if (!this.selection.length) {
      return;
    }
    this.$refs.tableList && this.$refs.tableList.batchToggleEnableHandle(enabled)
  }

  // 列表选中
  private selection: any[] = []
  private selectionChangeHandle (data: any[]) {
    this.selection = [...data]
  }
}
</script>

<style lang="scss" scoped>
.list-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  .list-header {
    justify-content: space-between;
    .search-group {
      flex: 1;
    }
  }
  
  .list-cont {
    flex: 1;
    overflow: hidden;
  }
}
</style>
