<template>
  <el-drawer
    direction="rtl"
    :visible.sync='showDialog' :title='title' size="742px"
    :before-close="cancelHandle" destroy-on-close
    class="group-config-drawer">

    <div class="drawer-content">
      <div class="pb-10">
        <div class="flex-h-jc">
          <span>{{ $t('modules.views.sysManage.group.s_c8721c18') }}</span>
          <el-button
            @click="addHandle"
            type="primary" size="small">
            <i class="db-icon-add"></i> {{ $t('modules.views.configManage.alarm.s_26bb8418') }}</el-button>
        </div>
      </div>

      <el-table
        ref='listTable'
        :data="configList"
        :empty-text='listLoading ? $t('modules.views.sysManage.group.s_f013ea9d') : listTotal ? $t('modules.views.sysManage.group.s_f013ea9d') : $t('modules.views.appMonitor.errorDetail.s_21efd88b')'
        highlight-current-row size="small"
        tooltip-effect='light'
      >
        <el-table-column prop='formatted' :label="$t('modules.views.sysManage.group.s_c940270c')" show-overflow-tooltip min-width="120" >
          <div class="ell" slot-scope="{ row }">
            {{ row.formatted }}
          </div>
        </el-table-column>
        <el-table-column prop='enable' :label="$t('modules.views.configManage.alarm.s_2b82bf9a')" show-overflow-tooltip width="80" >
          <div class="ell" slot-scope="{ row }">
            <el-switch @change="toggleRuleHandle(row)" :disabled="row.loading" :value='row.enable' :active-value="1" :inactive-value="0"></el-switch>
            <i v-if='row.loading' class="el-icon-loading"></i>
          </div>
        </el-table-column>
        <el-table-column key='action' :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" width="120" >
          <div class="ell" slot-scope="{ row }">
            <span @click="showConfigHandle(row)" :class='["blue cp mr-15", row.loading ? "cn" : ""]'>{{ $t('modules.views.hide.advancedConfig.s_95b351c8') }}</span>
            <span @click="deleteConfigHandle(row)" :class='["blue cp mr-15", row.loading ? "cn" : ""]'>{{ $t('modules.views.hide.advancedConfig.s_2f4aaddd') }}</span>
          </div>
        </el-table-column>
      </el-table>
    </div>

    <!-- <div class="drawer-footer">
      <el-button size="small" :disabled="postLoading" @click="cancelHandle">{{ $t('modules.views.alarmCenter.alarm.s_b15d9127') }}</el-button>
    </div> -->

    <config-modal :showModel="showConfigModal" :title='currConfigRow ? $t('modules.views.sysManage.group.s_a1d940b0') : $t('modules.views.sysManage.group.s_ba3c802f')'
      :group='curr'
      :curr="currConfigRow"
      @on-close='closeConfigModalHandle' />
  </el-drawer>
</template>

<script lang='ts'>
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import ConfigModal from './config-modal.vue'
import GroupApi from '@/api/group'
import { toAsyncWait } from '@/utils/common';
import i18n from '@/i18n';

@Component({
  components: {
    ConfigModal
  }
})
export default class GroupManageModal extends Vue {
  @Prop() private title!: string;
  @Prop() private showModel!: boolean
  @Prop() private curr!: any

  @Watch('showModel')
  private onShowModelChange (newVal: boolean) {
    this.showDialog = newVal
  }

  @Watch('curr')
  private onCurrChangeHandle (newVal: any) {
    if (newVal) {
      this.fetchList()
    }
  }

  private showDialog = false
  private postLoading = false

  // 配置规则弹窗
  private showConfigModal = false
  private currConfigRow: any = null

  // 规则列表
  private configList: any[] = []
  private listLoading = false
  private listTotal = 0
  

  private async fetchList () {
    this.listLoading = true
    const { result, error } = await toAsyncWait(GroupApi.getRuleList({ groupId: this.curr.id }))
    if (!error) {
      const { data = [] } = result || {}
      this.configList = (data || []).map((i: any) => ({ ...i, loading: false }));
      this.listTotal = (data || []).length
    }
    this.listLoading = false
  }

  private addHandle () {
    // this.$emit('on-create')
    this.showConfigHandle()
  }

  private cancelHandle () {
    this.showDialog = false
    this.postLoading = false
    this.$emit('on-close')
  }

  private async deleteConfigHandle (row: any) {
    if (row && row.loading) {
      return
    }
    const { id } = row
    this.listLoading = true
    if (id) {
      const { result, error } = await toAsyncWait(GroupApi.deleteRule({ id, groupId: this.curr.id }))
      if (!error) {
        this.$message.success(i18n.t('modules.views.aiPlatform.experts.s_0007d170') as string)
        this.fetchList()
      } else {
        this.$message.error(i18n.t('modules.views.sysManage.group.s_acf0664a') as string)
      }
    }
    this.listLoading = false
  }

  private async toggleRuleHandle (row: any) {
    row.loading = true
    const params = {
      id: row.id,
      enable: Number(!!!row.enable),
      groupId: this.curr.id
    }
    const { result, error } = await toAsyncWait(GroupApi.updateRule(params))
    if (!error) {
      row.enable = params.enable
    }
    row.loading = false
  }

  private showConfigHandle (curr?: any) {
    if (curr && curr.loading) {
      return
    }
    if (!curr) {
      this.currConfigRow = null
    } else {
      this.currConfigRow = curr
    }
    this.showConfigModal = true
  }

  private closeConfigModalHandle () {
    this.showConfigModal = false
    this.currConfigRow = null
    this.fetchList()
  }
}
</script>

<style scoped lang='scss'>
.group-config-drawer {
  :deep(.el-drawer__body) {
    display: flex;
    flex-direction: column;
    padding: 0;
    overflow: hidden;
  }

  .drawer-content {
    flex: 1;
    padding: 15px 24px 20px;
    overflow: auto;
  }

  .drawer-footer {
    text-align: right;
    padding: 20px 24px;
    border-top: 1px solid var(--border-color-lighter);
  }
}
</style>
