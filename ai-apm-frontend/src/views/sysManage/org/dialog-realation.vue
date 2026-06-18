<template>
  <el-dialog
    :visible.sync="showModel"
    :title="$t('modules.views.sysManage.org.s_24679ded')"
    :before-close="() => dialogCancelHandle(null, true)"
    :close-on-click-modal='!dialogPostLoading'
    :close-on-press-escape='!dialogPostLoading'
    :show-close='!dialogPostLoading'
    width='700px'
    append-to-body
  >
    <div>
      <div v-loading='isLoading'>
        <el-transfer v-model="userChoosed" :data="userList"
          filterable
          :filter-method="filterMethod"
          :titles="[$t('modules.views.sysManage.org.s_feaa92ca'), $t('modules.views.sysManage.org.s_ce5bd3f5')]"
          class="transfer-comp flex-h-cc"></el-transfer>
      </div>
      <div slot="footer" class="tr mt-20 mr-40">
        <el-button size="small" :disabled="dialogPostLoading" @click="dialogCancelHandle">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :loading="dialogPostLoading" @click="postHandle">{{ $t('modules.views.configInstall.plugin.s_be5fbbe3') }}</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Form } from 'element-ui';
import i18n from '@/i18n';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import UserApi from '@/api/user'
import SystemApi from '@/api/system'
import { toAsyncWait } from '@/utils/common';

@Component({})
export default class DialogRelation extends Vue {
  @Prop() private value!: boolean;
  @Prop() private current!: any;
  @Prop() private mode!: 'add' | 'edit';
  @Prop() private memberList!: any[];
  @Prop() private accList!: any[];

  @Watch('value')
  private onShowChange (newVal: boolean) {
    this.showModel = newVal
    if (newVal) {
      this.getUserList();
      this.userChoosed = (this.memberList || []).map(i => i.id);
    } else {
      this.userChoosed = [];
      this.userList = [];
    }
  }

  private showModel = false;
  private dialogPostLoading = false;
  private isLoading = false;

  private userChoosed: any[] = [];
  private userList: any[] = [];

  private groupMode: any = {
    //
  }


  private managerList: any[] = []

  private created () {
    //
  }


  private async getUserList () {
    this.userList = (this.accList || []).map((i: any) => ({
      key: i.value,
      label: i.label,
      disabled: i.label === 'Admin',
    }));
  }

  // 关闭弹窗
  private async dialogCancelHandle (payload?: any) {
    this.dialogPostLoading = false;
    this.showModel = false;
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
    this.dialogPostLoading = true;
    const params: any = {
      organizeId: this.current?.id,
      userIds: this.userChoosed,
    }
    const { result, error } = await toAsyncWait(UserApi.joinUser(params))
    this.dialogPostLoading = false;
    if (!error) {
      this.$message.success(i18n.t('modules.views.configManage.entity.s_33130f5c') as string)
      const { result: userResult, error: userError } = await toAsyncWait(UserApi.getUserInfo());
      if (!userError) {
        this.$store.commit('User/UPDATE_USER_INFO', {
          organizeManagerIds: userResult?.data?.organizeManagerIds || [],
          userOrganizeIds: userResult?.data?.userOrganizeIds || [],
          organizeManager: userResult?.data?.organizeManager || false,
        });
      }
      this.dialogCancelHandle(true);
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.configInstall.dataAccess.s_5fa802be') as string);
    }
    
  }
  
  private filterMethod (query: string, item: any) {
    return item.label.indexOf(query) > -1;
  }
}
</script>

<style lang="scss">
.mr-40 {
  margin-right: 38px;
}
.transfer-comp .el-transfer-panel__header .el-checkbox__input {
  display: inline-block;
}
.transfer-comp .el-transfer-panel__header .el-checkbox__input .el-checkbox__inner {
  display: inline-block;
}
.transfer-comp .el-transfer-panel__header .el-checkbox__label {
  display: inline-block;
}
</style>
