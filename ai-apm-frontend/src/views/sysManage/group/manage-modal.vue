<template>
  <el-dialog
    :visible.sync='showDialog' :title='title' width="480px"
    :before-close="cancelHandle">
    <el-form ref='groupForm' :model='groupForm' :rules="formRules" label-width="90px" label-position="left" size="small">
      <el-form-item :label="$t('modules.views.sysManage.group.s_604a92eb')" prop='name'>
        <el-input v-model='groupForm.name' size="small" :minlength="2" :maxlength="40" :placeholder="$t('modules.views.sysManage.group.s_0ee7a803')"></el-input>
      </el-form-item>
      <el-form-item :label="$t('modules.views.sysManage.group.s_f7294384')" prop="description">
        <el-input type='textarea' v-model='groupForm.description' :maxlength="400" show-word-limit size="small" :placeholder="$t('modules.views.observe.event.s_11956a43')"></el-input>
      </el-form-item>
    </el-form>

    <div slot="footer">
      <el-button size="small" :disabled="postLoading" @click="cancelHandle">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
      <el-button size="small" :loading="postLoading"  type="primary" @click="confirmHandle">{{ $t('modules.components.dialog-template.s_38cf16f2') }}</el-button>
    </div>
  </el-dialog>
</template>

<script lang='ts'>import i18n from '@/i18n';

import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import { Form } from 'element-ui'
import GroupApi from '@/api/group'

@Component({})
export default class GroupManageModal extends Vue {
  @Prop() private title!: string;
  @Prop() private showModel!: boolean
  @Prop() private curr!: any

  @Watch('showModel')
  private onShowModelChange (newVal: boolean) {
    this.showDialog = newVal
    if (this.curr) {
      const { name, description } = this.curr
      this.groupForm.name = name
      this.groupForm.description = description
    }
  }

  public $refs!: {
    groupForm: Form
  }

  private showDialog = false
  private postLoading = false

  private groupForm = {
    name: '',
    description: ''
  }

  get formRules(): any {
    const validateInputText = (rule: any, value: string, callback: any) => {
      if (!value.trim()) {
        callback(new Error(rule.message || i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string));
      } else {
        callback();
      }
    };
    return {
      name: [
        { required: true, validator: validateInputText, message: i18n.t('modules.views.sysManage.group.s_0ee7a803') as string, messageKey: 'modules.views.sysManage.group.s_0ee7a803', trigger: 'blur' },
        { required: true, validator: validateInputText, message: i18n.t('modules.views.sysManage.group.s_0ee7a803') as string, messageKey: 'modules.views.sysManage.group.s_0ee7a803', trigger: 'change' },
        { min: 2, max: 40, message: i18n.t('modules.views.configManage.alarm.s_b6d4b40c') as string, messageKey: 'modules.views.configManage.alarm.s_b6d4b40c', trigger: 'blur' },
        { min: 2, max: 40, message: i18n.t('modules.views.configManage.alarm.s_b6d4b40c') as string, messageKey: 'modules.views.configManage.alarm.s_b6d4b40c', trigger: 'change' },
      ],
      description: [
        { required: false, message: i18n.t('modules.views.sysManage.group.s_11956a43') as string, messageKey: 'modules.views.observe.event.s_11956a43', trigger: 'blur' },
        { max: 400, message: i18n.t('modules.views.sysManage.group.s_0b443bae') as string, messageKey: 'modules.views.sysManage.group.s_0b443bae', trigger: 'blur' }
      ]
    }
  }

  private cancelHandle (refresh?: boolean) {
    this.$refs.groupForm.resetFields()
    this.postLoading = false
    this.groupForm.name = ''
    this.groupForm.description = ''
    this.$nextTick(() => {
      this.showDialog = false
      this.$emit('on-close', { refresh })
    })
  }

  private async confirmHandle () {
    this.$refs.groupForm.validate((valid) => {
      if (valid) {
        this.postLoading = true
        // TODO: post data
        const succMsg = this.curr ? i18n.t('modules.views.configInstall.dataAccess.s_55aa6366') as string : i18n.t('modules.views.sysManage.group.s_a5bfd70d') as string
        const errMsg = this.curr ? i18n.t('modules.views.appMonitor.serviceAnalysis.s_930442e2') as string : i18n.t('modules.views.sysManage.group.s_bac372f6') as string
        const params = this.curr ? { ...this.groupForm, id: this.curr.id } : { ...this.groupForm }
        const fetchApi = this.curr ? GroupApi.updateGroup : GroupApi.addGroup
        fetchApi({ ...params })
          .then((res: any) => {
            if (res.message.toLowerCase() === 'success' && res.status === 200) {
              this.$message.success(succMsg)
              this.cancelHandle(true)
            } else {
              this.$message.error(res.message || errMsg)
            }
          })
          .catch(() => {
            this.$message.error(errMsg)
          })
          .finally(() => {
            this.postLoading = false
          })
      }
    })
  }
}
</script>
