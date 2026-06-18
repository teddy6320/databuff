<template>
  <el-dialog
    :visible.sync="showModel"
    :title="$t('modules.views.sysManage.group.s_5acfc339')"
    :before-close="() => dialogCancelHandle(null, true)"
    :close-on-click-modal='!dialogPostLoading'
    :close-on-press-escape='!dialogPostLoading'
    :show-close='!dialogPostLoading'
    width='480px'
    append-to-body
  >
    <div class="mb-15 flex-h">
      <span class="font-13 flex-0 mr-20">{{ $t('modules.views.sysManage.group.s_eab5b13a') }}</span>
      <el-input v-model='params.groupName' size="small" class="flex-1" :placeholder="$t('modules.views.sysManage.group.s_0ee7a803')"></el-input>
    </div>

    <div slot="footer">
      <el-button size="small" :disabled="dialogPostLoading" @click="dialogCancelHandle">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
      <el-button type="primary" size="small" :loading="dialogPostLoading" :disabled="!params.groupName" @click="postHandle">{{ $t('modules.components.dialog-template.s_38cf16f2') }}</el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import deepClone from 'lodash/cloneDeep';
import i18n from '@/i18n';
import GroupApi from '@/api/group';
import { toAsyncWait } from '@/utils/common';

@Component({})
export default class DialogComp extends Vue {
  @Prop() private value!: boolean;
  @Prop() private current!: any;
  @Prop() private type!: string;
  @Prop() private groupList!: any;
  @Prop() private selection!: any;

  @Watch('value')
  private onShowChange (newVal: boolean) {
    this.showModel = newVal;
    if (newVal && this.selection?.length) {
      const _querys = this.selection.map((i: any) => {
        const typeName = this.type === 'host' ? i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string : this.type === 'service' ? i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string : 'Kubernetes NameSpace'
        const typeValue = this.type === 'host' ? i.hostName : this.type === 'service' ? i.name : i.namespaceName
        return {
          enable: 1,
          type: this.type,
          params: [{
            connector: 'AND',
            left: this.type === 'host' ? 'host_name' : 'name',
            operator: '=',
            right: typeValue
          }],
          formatted: i18n.t('modules.views.sysManage.group.entity.s_e93d9d3c', { value0: typeName, value1: typeName, value2: typeValue }) as string
        }
      });
      this.params.querys = [..._querys]
    }
  }

  private showModel = false;
  private dialogPostLoading = false;

  private params: any = {
    groupId: '',
    groupName: '',
    querys: [],
  }

  // 关闭弹窗
  private dialogCancelHandle (payload?: any) {
    this.showModel = false;
    this.params = {
      groupId: '',
      groupName: '',
      querys: [],
    }
    this.$emit('on-close', payload);
    this.$emit('input', false);
  }

  private async postHandle () {
    this.dialogPostLoading = true;
    // 先创建管理域
    const { result, error } = await toAsyncWait(GroupApi.addGroup({ name: this.params.groupName, description: '' }));
    if (error) {
      this.$message.error(error?.message || i18n.t('modules.views.sysManage.group.s_093b7fca') as string);
      this.dialogPostLoading = false;
      return;
    }
    const { data = {} } = result || {};
    const { id } = data || {};
    if (!id) {
      this.$message.error(i18n.t('modules.views.sysManage.group.s_093b7fca') as string);
      this.dialogPostLoading = false;
      return;
    }
    this.params.groupId = id;
    const _params = deepClone(this.params);
    _params.querys.forEach((i: any) => {
      i.groupId = Number(this.params.groupId);
      i.params = JSON.stringify(i.params)
    });
    const res: any = await Promise.allSettled(_params.querys.map((q: any) => GroupApi.addRule(q)));
    const successCnt = res.filter((r: any) => {
      return r.status === 'fulfilled' && r.value?.message?.toLowerCase() === 'success' 
    })?.length;
    const failedCnt = res.length - successCnt

    if (successCnt === res.length) {
      this.$message.success(i18n.t('modules.views.sysManage.group.s_f54845c8') as string);
    } else {
      this.$message.info(i18n.t('modules.views.sysManage.group.s_9df5f623', { value0: successCnt, value1: failedCnt }) as string)
    }

    this.dialogPostLoading = false;
    this.dialogCancelHandle({ refresh: true });
  }
  
}
</script>
