<template>
  <div class="cont flex-v">
    <div class="flex-h mb-10">
      <span class="mr-6 vm">{{ $t('modules.views.configManage.entity.s_789525bf') }}</span>
      <el-tooltip placement="top" :content="$t('modules.views.configManage.entity.s_d54d0cf6')" effect="light">
        <span class="el-icon el-icon-info vm describe"></span>
      </el-tooltip>
      <el-switch :value='midEnabled' @change="toggleMidHandle" :disabled="midLoading" class="ml-6 mr-5 vm"></el-switch>
      <i class="el-icon-loading vm" v-show="midLoading"></i>
    </div>
    <template v-if='midEnabled'>
      <div>{{ $t('modules.views.configManage.entity.s_89160a2e') }}</div>
      <div class="flex-1">
        <db-table
          ref="listTable"
          :data="tableList"
          :total="tableList.length"
          :columnConfig="columnConfig"
          v-loading="isLoading"
          class="manage-group-table">
          <el-table-column slot="suffix" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" width="200">
            <template slot-scope="{ row }">
              <span @click.stop="showDialogHandle(row)" class="blue cp">{{ $t('modules.views.configManage.entity.s_ef6b463a') }}</span>
            </template>
          </el-table-column>
        </db-table>
      </div>
    </template>

    <dialog-manage v-model="showDialog"
      :current="currRow"
      :metricList="metricList"
      :metricLoading="metricLoading"
      @on-close="closeDialogHandle" />
  </div>
</template>

<script lang='ts'>
import { Vue, Component } from 'vue-property-decorator';
import i18n from '@/i18n';
import SystemApi from '@/api/system';
import { toAsyncWait } from '@/utils/common';
import DialogManage from './dialog-manage.vue';

@Component({
  components: {
    DialogManage
  }
})
export default class RelationConfig extends Vue {
  private midEnabled = false;
  private midLoading = false;
  private showDialog = false;
  private currRow: any = null;
  private metricLoading = false;
  private metricList: any[] = [];

  private tableList: any[] = [];
  private columnConfig = [
    { field: 'type', label: i18n.t('modules.views.appMonitor.cache.s_0c51881d') as string, labelKey: 'modules.views.appMonitor.cache.s_0c51881d', minWidth: 100 },
    { field: 'metrics', label: i18n.t('modules.views.configManage.entity.s_e204adf7') as string, labelKey: 'modules.views.configManage.entity.s_e204adf7', minWidth: 450 },
  ]

  private async created () {
    this.getSetting();
    this.getMetrics()
  }

  private async toggleMidHandle (val: boolean) {
    this.midLoading = true;
    const apiFunc = val ? SystemApi.setMidEnable : SystemApi.setMidDisabled;
    const { error, result } = await toAsyncWait(apiFunc());
    if (!error) {
      this.midEnabled = val
      this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string)
    }
    this.midLoading = false;
  }

  private async getSetting () {
    this.midLoading = true;
    const { error, result } = await toAsyncWait(SystemApi.getMidStatus());
    if (!error) {
      const { data = {} } = result || {};
      this.midEnabled = Boolean((data || {})?.enabled) || false;
    }
    this.midLoading = false;
  }

  private showDialogHandle (row: any) {
    this.currRow = row;
    this.showDialog = true;
  }

  private closeDialogHandle (payload: any) {
    this.showDialog = false;
    this.currRow = null;
  }

  private async getMetrics () {
    this.metricLoading = true;
    await this.$store.dispatch('Common/GET_METRIC_TYPE_AND_LIST');
    this.metricList = this.$store.getters['Common/metricTypeAndList'] || [];
    this.metricLoading = false;
  }
}
</script>
<style scoped lang='scss'>
.cont {
  height: 100%;
  overflow: hidden;
}
.mh-300 {
  min-height: 300px;
}
</style>
