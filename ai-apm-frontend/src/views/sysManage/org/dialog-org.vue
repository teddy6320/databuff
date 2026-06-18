<template>
  <el-dialog
    :visible.sync="showModel"
    :title="getTitle"
    :before-close="() => dialogCancelHandle(null, true)"
    :close-on-click-modal='!dialogPostLoading'
    :close-on-press-escape='!dialogPostLoading'
    :show-close='!dialogPostLoading'
    width='500px'
    append-to-body
  >
    <div>
      <el-form :model='groupMode' :rules="groupRules" label-width="90px" label-position="left" size="small" inline ref='groupForm'>
        <el-form-item :label="$t('modules.views.sysManage.org.s_4c12d831')" prop="organizeName">
          <el-input v-model="groupMode.organizeName" minlength="2" maxlength="50" clearable :placeholder="$t('modules.views.sysManage.org.s_a5bf36a3')" class="w-320" />
        </el-form-item>
        <el-form-item :label="$t('modules.views.sysManage.org.s_5093975a')" prop='managerIds'>
          <div class="flex-h">
            <el-tag class="mr-10">Admin</el-tag>
            <el-select v-model="groupMode.managerIds" multiple :placeholder="$t('modules.views.sysManage.org.s_5596db9f')" class="w-260" filterable clearable>
              <el-option
                v-for="item in accountList"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item :label="$t('modules.views.configInstall.apm.s_3bdd08ad')" prop="desc">
          <el-input v-model="groupMode.desc" type="textarea"
            :autosize="{ minRows: 2, maxRows: 5 }" :resize="false" maxlength="200"
            :placeholder="$t('modules.views.observe.event.s_11956a43')" class="w-320" />
        </el-form-item>
      </el-form>

      <div slot="footer" class="tr mr-40">
        <el-button size="small" :disabled="dialogPostLoading" @click="dialogCancelHandle">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :loading="dialogPostLoading" @click="postHandle">{{ $t('modules.components.dialog-template.s_38cf16f2') }}</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Form } from 'element-ui';
import i18n from '@/i18n';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import SystemApi from '@/api/system';
import UserApi from '@/api/user'
import { toAsyncWait } from '@/utils/common';
import { cloneDeep } from 'lodash';

@Component({})
export default class DialogComp extends Vue {
  @Prop() private value!: boolean;
  @Prop() private current!: any;
  @Prop() private accList!: any[];
  @Prop() private mode!: 'add' | 'edit';

  @Watch('value')
  private onShowChange (newVal: boolean) {
    this.showModel = newVal
    if (newVal) {
      this.getUserList();
    }
    if (this.current) {
      this.groupMode = {
        organizeName: this.current?.name,
        desc: this.current?.desc,
        managerIds: (this.current?.managerIds || []).filter((i: any) => i !== this.accList.find((j: any) => j.label === 'Admin')?.value),
      }
    } else {
      this.groupMode = {
        organizeName: '',
        desc: '',
        managerIds: []
      }
    }
  }

  public $refs!: {
    groupForm: Form
  }

  private showModel = false;
  private dialogPostLoading = false;

  get getTitle () {
    return this.mode === 'add' ? i18n.t('modules.views.sysManage.org.s_514998ea') as string : i18n.t('modules.views.sysManage.org.s_6da92fb1') as string
  }

  get getCurrentAccountId () {
    return this.$store.getters['User/getUserInfo']?.id || null
  }
  get getCurrentAccountOrgManagerIds () {
    return this.$store.getters['User/getUserInfo']?.organizeManagerIds || null
  }

  private groupMode: any = {
    organizeName: '',
    desc: '',
    managerIds: [],
  }

  private groupRules = {
    organizeName: [
      {
        required: true, 
        message: i18n.t('modules.views.sysManage.org.s_a5bf36a3') as string, messageKey: 'modules.views.sysManage.org.s_a5bf36a3', 
        trigger: 'blur',
        validator: (rule: any, value: string, callback: any) => {
          if (!value || value.trim() === '') {
          callback(new Error(i18n.t('modules.views.sysManage.org.s_a5bf36a3') as string));
          } else {
          callback();
          }
        }
      },
      {
        min: 2,
        max: 50, 
        message: i18n.t('modules.views.sysManage.org.s_3380fb76') as string, messageKey: 'modules.views.sysManage.org.s_3380fb76', 
        trigger: 'blur',
        validator: (rule: any, value: string, callback: any) => {
          if (value && (value.trim().length > 50 || value.trim().length < 2)) {
          callback(new Error(i18n.t('modules.views.sysManage.org.s_3380fb76') as string));
          } else {
          callback();
          }
        }
      },
    ],
    managerIds: [],
    desc: [
      { max: 200, message: i18n.t('modules.views.sysManage.org.s_c1f1d1e8') as string, messageKey: 'modules.views.sysManage.org.s_c1f1d1e8', trigger: 'blur' },
    ],
  }

  private accountList: any[] = []

  // 关闭弹窗
  private dialogCancelHandle (payload?: any) {
    this.$refs.groupForm.resetFields();
    this.dialogPostLoading = false;
    this.showModel = false;
    this.$emit('on-close', payload);
    this.$emit('input', false);
  }

  private async postHandle () {
    this.$refs.groupForm.validate(async (valid) => {
      if (valid) {
        this.dialogPostLoading = true;
        const params: any = {
          ...this.groupMode
        }
        const isEdit = this.mode === 'edit';
        let apiPath: any = UserApi.addOrg;
        if (isEdit) {
          params.id = this.current.id;
          apiPath = UserApi.editOrg;
        }
        params.managerIds = [...new Set([...params.managerIds, this.accList.find(i => i.label === 'Admin')?.value])];
        const { result, error } = await toAsyncWait(apiPath(params));
        if (!error) {
          this.$message.success(isEdit ? i18n.t('modules.views.sysManage.org.s_3bb47b67') as string : i18n.t('modules.views.configInstall.dataAccess.s_04a691b3') as string);
            // await this.$store.dispatch('User/getUserInfo');
          const { result: userResult, error: userError } = await toAsyncWait(UserApi.getUserInfo());
          if (!userError) {
            this.$store.commit('User/UPDATE_USER_INFO', {
              organizeManagerIds: userResult?.data?.organizeManagerIds || [],
              userOrganizeIds: userResult?.data?.userOrganizeIds || [],
              organizeManager: userResult?.data?.organizeManager || false,
            });
          }
        } else {
          this.$message.error(error?.message || (isEdit ? i18n.t('modules.views.sysManage.org.s_9304e8f4') as string : i18n.t('modules.views.sysManage.org.s_a889286a') as string));
        }
        this.dialogCancelHandle();
      }
    });
  }

  private async getUserList () {
    this.accountList = cloneDeep(this.accList || []).filter((i: any) => i.label !== 'Admin');
  }
  
}
</script>

<style lang="scss" scoped>
.mr-40 {
  margin-right: 42px;
}
.w-260 {
  width: 253px;
}
</style>
