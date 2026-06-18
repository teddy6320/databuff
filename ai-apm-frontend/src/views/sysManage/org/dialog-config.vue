<template>
  <el-dialog
    :visible.sync="showModel"
    :title="$t('modules.views.sysManage.org.s_90b704f3', { value0: groupMode.name })"
    :close-on-click-modal='!dialogPostLoading'
    :close-on-press-escape='!dialogPostLoading'
    :show-close='!dialogPostLoading'
    :before-close="dialogCancelHandle"
    width='800px'
    append-to-body
  >
    <div>
      <div class="flex-h">
        <el-input v-model='searchQuery' size="small" :placeholder="$t('modules.views.sysManage.org.s_a4de6f0d')" @change="searchChangeHandle" clearable class="flex-1 mr-10"></el-input>
        <div>
          <el-button @click="showRelationDialogHandle" size="small" type="primary" plain>{{ $t('modules.views.sysManage.org.s_24679ded') }}</el-button>
        </div>
      </div>

      <div class="height-300">
        <db-table
          ref="listTable"
          :data="filterTableList"
          :total="filterTableList.length"
          :columnConfig="columnConfig"
          :formatFunc="formatFunc"
          v-loading="isLoading"
          class="manage-group-table">
          <template slot="organizeManager" slot-scope="{ row }">
            <div size="small">{{ row.organizeManager ? $t('modules.components.s_0a60ac8f') : $t('modules.components.s_c9744f45')  }}</div>
          </template>
          <el-table-column slot="suffix" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" width="200">
            <template slot-scope="{ row }">
              <span v-if='row.organizeManager' @click.stop="removeManagerHandle(row)" :class="['blue', 'cp', 'mr-15', (getManagerLength === 1 && row.organizeManager || row.account === 'Admin') ? 'describe cn' : '']">{{ $t('modules.views.sysManage.org.s_9da33acc') }}</span>
              <span v-if='!row.organizeManager' @click.stop="addManagerHandle(row)" :class="['blue', 'cp', 'mr-15', !isMultipleManager ? '' : 'describe cn']">{{ $t('modules.views.sysManage.org.s_6be5f600') }}</span>
              <span @click.stop="deleteHandle(row)" :class="['blue', 'cp', (getManagerLength === 1 && row.organizeManager || row.account === 'Admin') ? 'describe cn' : '']">{{ $t('modules.views.sysManage.org.s_0ba594c9') }}</span>
            </template>
          </el-table-column>
        </db-table>
      </div>
      
      <div class="tr">
        <el-button size="small" :disabled="dialogPostLoading" @click="dialogCancelHandle">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :loading="dialogPostLoading" @click="postHandle">{{ $t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') }}</el-button>
      </div>

      <relation-dialog v-model="showRelationDialog" :current="current"
        :memberList="tableList"
        :accList='accList'
        @on-close="closeRelationDialogHandle" />
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Form } from 'element-ui';
import i18n from '@/i18n';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import RelationDialog from './dialog-realation.vue'
import UserApi from '@/api/user';
import { toAsyncWait } from '@/utils/common';

@Component({
  components: {
    RelationDialog,
  }
})
export default class DialogConfig extends Vue {
  @Prop() private value!: boolean;
  @Prop() private current!: any;
  @Prop() private accList!: any[];
  @Prop() private mode!: 'add' | 'edit';

  @Watch('value')
  private onShowChange (newVal: boolean) {
    this.showModel = newVal
    if (newVal) {
      this.getMemberList();
    }
    if (this.current) {
      this.groupMode = {
        name: this.current?.name,
        description: this.current?.description,
        manager: '',
      }
    } else {
      this.groupMode = {
        name: '',
        description: '',
        manager: ''
      }
    }
  }

  public $refs!: {
    groupForm: Form
  }

  private showModel = false;
  private dialogPostLoading = false;
  private isLoading = false;
  private searchQuery = '';

  private columnConfig = [
    { field: 'account', label: i18n.t('modules.views.sysManage.org.s_819767ad') as string, labelKey: 'modules.views.authorization.s_819767ad', minWidth: 100 },
    { field: 'roleNames', label: i18n.t('modules.views.sysManage.operationAudit.s_464f3d4e') as string, labelKey: 'modules.views.personal.s_464f3d4e', minWidth: 100 },
    { field: 'responsible', label: i18n.t('modules.views.sysManage.org.s_0c5b77a1') as string, labelKey: 'modules.views.personal.s_0c5b77a1', minWidth: 100 },
    { field: 'organizeManager', label: i18n.t('modules.views.sysManage.org.s_fa8c8e92') as string, labelKey: 'modules.views.sysManage.org.s_fa8c8e92', slot: 'organizeManager', minWidth: 150 },
  ]
  private tableList: any[] = [];

  private filterTableList: any[] = [];

  private groupMode: any = {
    name: '',
    description: '',
    manager: '',
  }

  private showRelationDialog = false;

  get getManagerLength () {
    return this.tableList.filter(i => i.organizeManager).length;
  }

  private async getMemberList () {
    this.isLoading = true
    const { result, error } = await toAsyncWait(UserApi.getMemberList({ organizeId: this.current.id, page: 1, pageSize: 9999 }))
    this.isLoading = false
    if (!error && result.status === 200) {
      this.tableList = result.data?.list || [];
      this.searchChangeHandle();
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.sysManage.org.s_b1fb14d6') as string);
    }
  }

  // 关闭弹窗
  private async dialogCancelHandle (payload?: any) {
    this.dialogPostLoading = false;
    this.showModel = false;
    this.searchQuery = '';
    this.filterTableList = [];
    this.tableList = [];
    const { result: userResult, error: userError } = await toAsyncWait(UserApi.getUserInfo());
    if (!userError) {
      this.$store.commit('User/UPDATE_USER_INFO', {
        organizeManagerIds: userResult?.data?.organizeManagerIds || [],
        userOrganizeIds: userResult?.data?.userOrganizeIds || [],
        organizeManager: userResult?.data?.organizeManager || false,
      });
    }
    this.$emit('on-close', payload);
    this.$emit('input', false);
  }

  private async postHandle () {
    this.dialogCancelHandle();
  }

  private searchChangeHandle () {
    this.filterTableList = this.tableList.filter(item => item.account.includes(this.searchQuery));
  }

  private async addManagerHandle (row: any) {
    this.isLoading = true;
    const { result, error } = await toAsyncWait(UserApi.addManager({ organizeId: this.current.id, userId: row.id }))
    this.isLoading = false;
    if (!error) {
      this.$message.success(i18n.t('modules.views.configManage.entity.s_33130f5c') as string)
      row.organizeManager = true
    } else if (error?.message !== 'interrupt') {
      this.$message.error(error?.message || i18n.t('modules.views.configInstall.dataAccess.s_5fa802be') as string);
    }
  }
  private async removeManagerHandle (row: any) {
    if (this.getManagerLength === 1 && row.organizeManager || row.account === 'Admin') {
      return;
    }
    this.isLoading = true;
    const { result, error } = await toAsyncWait(UserApi.removeManager({ organizeId: this.current.id, userId: row.id }))
    this.isLoading = false;
    if (!error) {
      this.$message.success(i18n.t('modules.views.configManage.entity.s_33130f5c') as string)
      row.organizeManager = false;
    } else if (error?.message !== 'interrupt') {
      this.$message.error(error?.message || i18n.t('modules.views.configInstall.dataAccess.s_5fa802be') as string);
    }
  }

  private deleteHandle (row: any) {
    if (this.getManagerLength === 1 && row.organizeManager || row.account === 'Admin') {
      return;
    }
    this.$confirm(i18n.t('modules.views.sysManage.org.s_bafb9cb6') as string, i18n.t('common.hint') as string, { type: 'warning' })
      .then(async () => {
        this.isLoading = true
        this.dialogPostLoading = true
        const { result, error } = await toAsyncWait(UserApi.removeUser({ organizeId: this.current.id, userId: row.id }))
        this.isLoading = false
        this.dialogPostLoading = false
        if (!error) {
          const index = this.filterTableList.findIndex((i: any) => i.id === row.id)
          const index2 = this.tableList.findIndex((i: any) => i.id === row.id)
          this.filterTableList.splice(index, 1);
          this.tableList.splice(index2, 1);
          this.$message.success(i18n.t('modules.views.aiPlatform.experts.s_0007d170') as string)
        } else if (error.message !== 'interrupt') {
          this.$message.error(error.message);
        }
      })
      .catch(() => null)
  }

  private showRelationDialogHandle (row: any) {
    this.showRelationDialog = true;
  }

  private closeRelationDialogHandle (refresh: any) {
    this.showRelationDialog = false;
    if (refresh) {
      this.getMemberList();
    }
  }

  private formatFunc (data: any) {
    return data.map((t: any) => {
      const roleList = t.roleList.filter((r: any) => r && r.id)
      return {
        ...t,
        roleIds: roleList.map((r: any) => r.id).sort(),
        roleNames: [
          ...roleList.map((r: any) => r.name).sort(),
        ].join(','),
      }
    });
  }
  
}
</script>

<style lang="scss" scoped>
.height-300 {
  min-height: 300px;
}
.manage-group-table {
  min-height: 300px;
}
</style>
