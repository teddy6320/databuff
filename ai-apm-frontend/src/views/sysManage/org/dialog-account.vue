<template>
  <el-dialog
    :visible.sync="showModel"
    :title="$t('modules.views.sysManage.org.s_49a51f68')"
    :before-close="() => dialogCancelHandle(null, true)"
    :close-on-click-modal='!dialogPostLoading'
    :close-on-press-escape='!dialogPostLoading'
    :show-close='!dialogPostLoading'
    width='540px'
    append-to-body
  >
    <div>
      <el-form
        @submit.native.prevent
        class="form-box"
        ref="accountForm"
        :model="accountForm"
        :rules="accountFormRule"
        label-width="80px"
        label-position="left"
        size="small">
        <el-form-item :label="$t('modules.views.authorization.s_819767ad')" prop="account">
          <el-input
            v-model="accountForm.account"
            :disabled="accountModalType !== 'create'"
            maxlength="16"
            :placeholder="$t('modules.views.configInstall.agent.s_08b1fa13')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item
          :label="$t('modules.views.sysManage.org.s_763ffee1')"
          prop="password"
          :key="`password${modalKey}`"
        >
          <el-input
            v-model="accountForm.password"
            type="password"
            show-password
            maxlength="100"
            autocomplete="new-password"
            :placeholder="$t('modules.views.sysManage.org.s_a3091b05')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item
          :label="$t('modules.views.authorization.s_3fbdde13')"
          prop="password2"
          :key="`password2${modalKey}`"
        >
          <el-input
            v-model="accountForm.password2"
            type="password"
            show-password
            maxlength="100"
            autocomplete="new-password"
            :placeholder="$t('modules.views.sysManage.org.s_a0fcd66a')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item :label="$t('modules.views.sysManage.org.s_1c77cb45')" prop="roleIds">
          <el-cascader
            v-model="accountForm.roleIds"
            :props="{ label: 'name', value: 'id', checkStrictly: true }"
            :options="roleOptions"
            size="small" filterable
            :disabled='accountForm.account === "Admin"'
            :placeholder="$t('modules.views.sysManage.org.s_aabcdcd6')"
            class="role-custom-select form-input" />
        </el-form-item>

        <el-form-item :label="$t('modules.views.personal.s_0c5b77a1')" prop="responsible">
          <el-input v-model="accountForm.responsible" :maxlength="20" :placeholder="$t('modules.views.sysManage.org.s_8f9ffcc6')" class="form-input" />
        </el-form-item>

        <el-form-item :label="$t('modules.views.personal.s_8098e2b4')" prop="mobile">
          <el-input v-model="accountForm.mobile" :maxlength="30" :placeholder="$t('modules.views.dataReport.report.s_6e4f4b33')" class="form-input" />
        </el-form-item>

        <el-form-item :label="$t('modules.views.personal.s_6ab78fa2')" prop="email">
          <el-input
            v-model="accountForm.email"
            maxlength="100"
            :placeholder="$t('modules.views.personal.s_2ba4c815')"
            class="form-input"
          />
        </el-form-item>

        <el-form-item :label="$t('modules.views.configManage.alarm.s_2432b575')" prop="remark">
          <el-input
            v-model="accountForm.remark"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 5 }"
            :placeholder="$t('modules.views.configInstall.dataAccess.s_3cac6342')"
            maxlength="100"
            show-word-limit
            class="form-input"
          />
        </el-form-item>
      </el-form>

      <div slot="footer">
        <el-button size="small" :disabled="dialogPostLoading" @click="dialogCancelHandle">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :loading="dialogPostLoading" @click="postHandle">{{ $t('modules.components.dialog-template.s_38cf16f2') }}</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { toAsyncWait } from '@/utils/common';
import i18n from '@/i18n';
import { Form } from 'element-ui';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import SystemApi from '@/api/system'
import md5 from 'md5';

@Component({})
export default class DialogComp extends Vue {
  @Prop() private value!: boolean;
  @Prop() private current!: any;

  @Watch('value')
  private onShowChange (newVal: boolean) {
    this.showModel = newVal
  }

  public $refs!: {
    accountForm: Form;
  };

  private showModel = false;
  private dialogPostLoading = false;
  private isLoading = false;
  private saving = false;

  private accountForm: any = {
    id: 0,
    account: '',
    password: '',
    password2: '',
    roleIds: [],
    responsible: '',
    mobile: '',
    email: '',
    remark: '',
    ipAddress: '',
  };

  private roleIdPathMap: any = {}

  private roleOptions: any[] = [];
  get roleList () {
    const _listFlat: any[] = [];
    const traverse = (list: any[]) => {
      list.forEach(item => {
        _listFlat.push({ id: item.id, name: item.name })
        if (item.children && item.children.length > 0) {
          traverse(item.children);
        }
      });
    }
    traverse(this.roleOptions);
    return _listFlat;
  }

  get accountFormRule() {
    // TODO 用户名复杂度获取 校验
    const validateAcc = (rule: any, value: string, callback: any) => {
      const accReg = new RegExp(/^[a-zA-Z0-9_]{4,16}$/);
      if (!value) {
        callback(new Error(i18n.t('modules.views.sysManage.org.s_0b62b5ce') as string));
      } else if (!accReg.test(value)) {
        callback(new Error(i18n.t('modules.views.sysManage.org.s_75e729c1') as string));
      } else {
        callback();
      }
    };
    const validatePass = (rule: any, value: string, callback: any) => {
      const pwdReg = new RegExp(/^(?=.*[0-9].*)(?=.*[A-Z].*)(?=.*[a-z].*).{6,20}$/);
      if (!value) {
        callback(new Error(i18n.t('modules.views.sysManage.org.s_89b5d3d5') as string));
      } else if (!pwdReg.test(value) || /[\u4e00-\u9fa5]+/.test(value)) {
        callback(new Error(i18n.t('modules.views.personal.s_217fe7d1') as string));
      } else {
        if (this.accountForm.password2) {
          this.$refs.accountForm.validateField('password2');
        }
        callback();
      }
    };
    const validatePassCheck = (rule: any, value: string, callback: any) => {
      if (!value) {
        callback(new Error(i18n.t('modules.views.sysManage.org.s_a0fcd66a') as string));
      } else if (value !== this.accountForm.password) {
        callback(new Error(i18n.t('modules.views.personal.s_321037b2') as string));
      } else {
        callback();
      }
    };
    const validateMobile = (rule: any, value: string, callback: any) => {
      const mobileReg = new RegExp(/^(?:(?:\+|00)86)?1[3-9]\d{9}$/);
      if (value && !mobileReg.test(value)) {
        callback(new Error(i18n.t('modules.views.personal.s_a32ab517') as string));
      } else {
        callback();
      }
    };
    const validateRoleCheck = (rule: any, value: string[], callback: any) => {
      // console.log('val', value)
      if (!value.length) {
        callback(new Error(i18n.t('modules.views.sysManage.org.s_aabcdcd6') as string));
      } else {
        callback();
      }
    };
    const validateEmail = (rule: any, value: string, callback: any) => {
      const emailReg = new RegExp(/^(([^*&$%》《【】\^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/);
      if (value && !emailReg.test(value)) {
        callback(new Error(i18n.t('modules.views.dataReport.report.s_6a5f045c') as string));
      } else {
        callback();
      }
    };

    return {
      account: { required: true, validator: validateAcc, trigger: 'blur' },
      password: { required: true, validator: validatePass, trigger: 'blur' },
      password2: { required: true, validator: validatePassCheck, trigger: 'blur' },
      responsible: { required: true, message: i18n.t('modules.views.sysManage.org.s_d42fdef0') as string, messageKey: 'modules.views.sysManage.org.s_d42fdef0', trigger: 'blur' },
      roleIds: { required: true, validator: validateRoleCheck, trigger: 'change', type: 'array' },
      mobile: { validator: validateMobile, trigger: 'blur' },
      email: { validator: validateEmail, trigger: 'blur' },
    };
  }

  private created () {
    this.getAllRoleList();
  }

  private async getAllRoleList() {
    this.isLoading = true
    const { result, error } = await toAsyncWait(SystemApi.getRoleList({}))
    this.isLoading = false
    if (!error) {
      const list = result.data || [];
      // 递归设置idPath用于回填
      const formatTree = (item: any, path: number[]) => {
        item._path = [...path, item.id];
        this.roleIdPathMap[String(item.id)] = [...item._path];
        if (Array.isArray(item?.children)) {
          item.children.forEach((child: any) => {
            formatTree(child, [...item._path]);
          });
        }
      }
      list.forEach((roleTree: any) => {
        formatTree(roleTree, []);
      });
      this.roleOptions = list;
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message);
    }
  }

  private saveAccountForm() {
    this.$refs.accountForm.validate((valid: boolean) => {
      if (valid) {
        this.saving = true;
        this.createAccount();
      }
    });
  }

  private createAccount() {
    const { account, password, roleIds, responsible, mobile, email, remark, ipAddress } = this.accountForm;
    const params: any = {
      account,
      password: md5(password),
      // @ts-ignore
      roleIds: [[...roleIds].reverse()[0]],
      responsible,
      mobile,
      email,
      remark,
      ipAddress,
    };
    SystemApi.createAccount(params)
      .then((rst: any) => {
        if (rst && rst.status === 200) {
          // 创建/更新通知者
          this.$message.success(i18n.t('modules.views.dataReport.report.s_e477bf35') as string);
          // TODO
        } else if (rst && rst.status === 405) {
          this.$message.error(i18n.t('modules.views.sysManage.org.s_a633b7b9') as string);
        } else {
          throw new Error(rst.message);
        }
      })
      .catch((err) => {
        if (err.message !== 'interrupt') {
          this.$message.error(err.message);
        }
      })
      .finally(() => this.saving = false);
  }

  // 关闭弹窗
  private dialogCancelHandle (payload?: any) {
    this.showModel = false;
    this.$emit('on-close', payload);
    this.$emit('input', false);
  }

  private postHandle () {
    this.dialogPostLoading = true;
  }
  
}
</script>

<style lang="scss" scoped>

</style>
