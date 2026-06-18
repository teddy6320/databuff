<template>
  <div class="org-cont">
    <div class="flex-h flex-none">
      <el-input
        v-model="nameQuery"
        :disabled="isLoading"
        @change="searchChangeHandle"
        clearable size="small"
        maxlength="100"
        prefix-icon="db-icon-search"
        :placeholder="$t('modules.views.sysManage.org.s_428d4208')"
      />

      <div class="flex-none ml-10">

        <el-popover class="mr-10" placement="bottom-end">
          <el-button slot="reference" type="primary" size="small"><i class="db-icon-setting"></i> {{ $t('modules.views.appMonitor.service.s_e366ccf1') }}</el-button>
          <div class="p-10 w-320" v-loading='statusLoading'>
            <div class="setting-header tc border-bottom-1 mb-15 pb-10">{{ $t('modules.views.appMonitor.service.s_e366ccf1') }}</div>
            <div class="mb-10">
              <span class="mr-6 vm">{{ $t('modules.views.sysManage.org.s_45a9d929') }}</span>
              <el-switch :value='orgAuthEnabled' @change="toggleOrgAuthHandle" class="mr-6 vm"></el-switch>
              <el-tooltip placement="top" :content="$t('modules.views.sysManage.org.s_a9291c44')" effect="light">
                <span class="el-icon el-icon-info vm"></span>
              </el-tooltip>
            </div>
          </div>
        </el-popover>
        <el-button
          @click="showManageModalHandle()"
          :disabled="isLoading"
          type="primary" size="small">
          <i class="db-icon-add"></i> {{ $t('modules.views.sysManage.org.s_514998ea') }}</el-button>
      </div>
    </div>

    <db-table
      ref="listTable"
      :data="tableList"
      :total="tableList.length"
      :columnConfig="columnConfig"
      v-loading="isLoading"
      class="manage-group-table flex-1">
      <el-table-column slot="suffix" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" width="200">
        <template slot-scope="{ row }">
          <span @click.stop="showManageModalHandle(row)" :class='["mr-15", (orgAuthEnabled && row.managable || !orgAuthEnabled) ? "blue cp" : "describe cn"]'>{{ $t('modules.views.hide.advancedConfig.s_95b351c8') }}</span>
          <span @click.stop="showConfigHandle(row)" :class='["mr-15", (orgAuthEnabled && row.managable || !orgAuthEnabled) ? "blue cp" : "describe cn"]'>{{ $t('modules.views.sysManage.org.s_88e6a4f1') }}</span>
          <span @click.stop="deleteHandle(row)" :class='["mr-15", (orgAuthEnabled && row.managable || !orgAuthEnabled) ? "blue cp" : "describe cn"]'>{{ $t('modules.views.hide.advancedConfig.s_2f4aaddd') }}</span>
        </template>
      </el-table-column>

    </db-table>

    <org-dialog v-model="showManageModal" :current="currManageRow" :mode="!!currManageRow ? 'edit' : 'add'"
      :accList='allAccountWithoutOrgList'
      @on-close="manageDialogClose" />

    <config-dialog v-model="showConfigDialog" :current="currConfigRow" :accList='allAccountWithoutOrgList'
      @on-close="closeConfigDialogHandle" />
  </div>
</template>
<script lang="ts">
import { toAsyncWait } from '@/utils/common';
import i18n from '@/i18n';
import { Vue, Component } from 'vue-property-decorator';
import UserApi from '@/api/user';
import OrgDialog from './dialog-org.vue';
import ConfigDialog from './dialog-config.vue';
@Component({
  components: {
    OrgDialog,
    ConfigDialog
  },
})
export default class OrgManage extends Vue {

  private nameQuery = '';
  private isLoading = false;
  private tableList: any[] = []
  private allAccountWithoutOrgList: any[] = []
  private optionLoading = false;

  get accountManageOrgIds () {
    return this.$store.getters['User/getUserInfo']?.organizeManagerIds || []
  }
  get accountOrgIds () {
    return this.$store.getters['User/getUserInfo']?.userOrganizeIds || []
  }

  private columnConfig = [
    { field: 'name', label: i18n.t('modules.views.sysManage.org.s_4c12d831') as string, labelKey: 'modules.views.sysManage.org.s_4c12d831', minWidth: 100 },
    { field: 'desc', label: i18n.t('modules.views.sysManage.org.s_3961de74') as string, labelKey: 'modules.views.sysManage.org.s_3961de74', minWidth: 150 },
    { field: 'memberNumber', label: i18n.t('modules.views.sysManage.org.s_ab5dea29') as string, labelKey: 'modules.views.sysManage.org.s_ab5dea29', minWidth: 150 },
  ]

  // 新增/编辑弹窗
  private showManageModal = false
  private currManageRow: any = null

  // 成员管理弹窗
  private showConfigDialog = false
  private currConfigRow: any = null

  private statusLoading = false;
  private orgAuthEnabled = false;

  private created () {
    const { name = '' } = this.$route.query;
    this.nameQuery = decodeURIComponent(name as string);
    this.getOrgList();
    this.getOrgStatus();
    this.getAllAccountWithoutOrg()
  }

  private async getOrgStatus () {
    this.statusLoading = true
    const { result, error } = await toAsyncWait(UserApi.getOrgStatus())
    this.statusLoading = false
    if (!error) {
      this.orgAuthEnabled = result.data?.enabled || false
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.sysManage.org.s_04a203a3') as string);
    }
  }

  private async getOrgList () {
    this.isLoading = true
    const { result, error } = await toAsyncWait(UserApi.getOrgList())
    this.isLoading = false
    if (!error && Array.isArray(result.data)) {
      (result.data || []).forEach((acc: any) => {
        acc.managable = this.accountManageOrgIds.includes(acc.id);
      })
      this.tableList = (result.data || []).filter((i: any) => !this.nameQuery || (i.name && i.name.includes(this.nameQuery)))
    } else if (error?.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.sysManage.org.s_72767191') as string);
    }
  }
  private async getAllAccountWithoutOrg () {
    this.optionLoading = true
    const { result, error } = await toAsyncWait(UserApi.getAllAccountWithoutOrg())
    this.optionLoading = false
    if (!error && Array.isArray(result.data)) {
      this.allAccountWithoutOrgList = (result.data || []).map((acc: any) => ({ label: acc.account, value: acc.userId }))
    } else if (error?.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.sysManage.org.s_0ac45fd5') as string);
    }
  }

  private searchChangeHandle () {
    this.getOrgList()
    const _query = { ...this.$route.query }
    if (this.nameQuery) {
      _query.name = encodeURIComponent(this.nameQuery)
    } else {
      delete _query.name
    }
    this.$router.replace({ query: { ..._query } })
  }

  private showManageModalHandle (row?: any) {
    if (row && row.managable === false && this.orgAuthEnabled) {
      return;
    }
    this.currManageRow = row || null;
    this.showManageModal = true;

  }
  private manageDialogClose (row?: any) {
    this.currManageRow = null;
    this.showManageModal = false;
    this.getOrgList()
  }
  private showConfigHandle (row: any) {
    if (row.managable === false && this.orgAuthEnabled) {
      return;
    }
    this.currConfigRow = row || null;
    this.showConfigDialog = true;
  }
  private closeConfigDialogHandle (row?: any) {
    this.currConfigRow = null;
    this.showConfigDialog = false;
    this.getOrgList()
  }
  private deleteHandle (row: any) {
    if (row.managable === false && this.orgAuthEnabled) {
      return;
    }
    this.$confirm(i18n.t('modules.views.sysManage.org.s_5b692af6') as string, i18n.t('common.hint') as string, { type: 'warning' })
      .then(async () => {
        this.isLoading = true
        const { result, error } = await toAsyncWait(UserApi.deleteOrg(row.id))
        if (!error) {
          const index = this.tableList.findIndex((i: any) => i.id === row.id)
          this.tableList.splice(index, 1)
          this.$message.success(i18n.t('modules.views.aiPlatform.experts.s_0007d170') as string)
        } else if (error.message !== 'interrupt') {
          this.$message.error(error.message);
        }
      })
      .catch(() => null)
      .finally(() => {
        this.isLoading = false
      });
  }

  private async toggleOrgAuthHandle (val: boolean) {
    this.isLoading = true
    const { result, error } = await toAsyncWait(UserApi.toggleOrgAuth({ enabled: val ? 1 : 0 }))
    this.isLoading = false
    if (!error && result.status === 200) {
      this.orgAuthEnabled = val
      this.$message.success(i18n.t('modules.views.configManage.entity.s_33130f5c') as string)
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.configInstall.dataAccess.s_5fa802be') as string);
    }
  }
}
</script>
<style scoped lang='scss'>
.org-cont {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
}
</style>