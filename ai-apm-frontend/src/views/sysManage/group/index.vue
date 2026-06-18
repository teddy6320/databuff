<template>
  <div class="manage-group-cont">
    <el-row v-if='groupEnable' type="flex" justify="space-around" class="mb-20 lh-20 flex-none">
      <el-col :span="8">
        <el-statistic
          group-separator=","
          :value="total.host"
          :title="$t('modules.views.sysManage.group.s_43ade7f0')"
          @click.native="viewEntityHandle('host')"
          class="cp">
        <template slot="prefix">
          <i class="db-icon db-icon-aside-host"></i>
        </template>
        </el-statistic>
      </el-col>
      <el-col :span="8">
        <el-statistic
          group-separator=","
          :value="total.service"
          :title="$t('modules.views.sysManage.group.s_07343df4')"
          @click.native="viewEntityHandle('service')"
          class="cp">
        <template slot="prefix">
          <i class="db-icon db-icon-aside-service"></i>
        </template></el-statistic>
      </el-col>
      <el-col :span="8">
        <el-statistic
          group-separator=","
          :value="total.namespace"
          :title="$t('modules.views.sysManage.group.s_d1968e43')"
          @click.native="viewEntityHandle('namespace')"
          class="cp">
        <template slot="prefix">
          <i class="db-icon db-icon-namespace"></i>
        </template></el-statistic>
      </el-col>
    </el-row>

    <div class="flex-h mb-16 flex-none">
      <el-switch :value='groupEnable' :disabled="enableLoading" :active-text="enableLabel"
        @change="toggleEnableHandle" :active-value="1" :inactive-value="0"></el-switch>
      <i v-if='enableLoading' class="el-icon-loading"></i>
      <el-tooltip :content="$t('modules.views.sysManage.group.s_0619979b')" effect="light">
        <i class="db-icon-info describe ml-10 mr-5"></i>
      </el-tooltip>
      <span class="ml-20 mr-20 information">|</span>
    </div>

    <table-list ref="tableList" @on-refresh='getUngroupEntity' class="flex-1" />
  </div>
</template>

<script lang='ts'>import i18n from '@/i18n';

import { Vue, Component } from 'vue-property-decorator'
import TableList from './table-list.vue'
import GroupApi from '@/api/group'
import { toAsyncWait } from '@/utils/common'

@Component({
  components: {
    TableList
  }
})
export default class GroupManage extends Vue {
  public $refs!: {
    tableList: TableList,
  }

  private groupEnable = 0
  private enableLabel = '管理域已停用'
  private enableLoading = false
  private ungroupLoading = false

  private total = {
    host: 0,
    service: 0,
    namespace: 0,
  }

  private async created () {
    const enabled = await this.$store.dispatch('User/getGroupEnabled');
    this.groupEnable = enabled;
    this.enableLabel = this.groupEnable ? i18n.t('modules.views.sysManage.group.s_643c00c1') as string : i18n.t('modules.views.sysManage.group.s_61c43b38') as string
    if (this.groupEnable) {
      this.getUngroupEntity();
    }
    this.$refs.tableList && this.$refs.tableList.resize()
  }

  private async toggleEnableHandle (val: number) {
    this.enableLoading  = true
    const params = {
      enable: Number(val)
    }
    const { result, error } = await toAsyncWait(GroupApi.updateStatus(params))
    if (!error) {
      this.groupEnable = params.enable
      this.enableLabel = val ? i18n.t('modules.views.sysManage.group.s_643c00c1') as string : i18n.t('modules.views.sysManage.group.s_61c43b38') as string
      this.$eventBus.$emit('ToggleAuthGroupStatus', val);
      this.$store.commit('User/SET_GROUP_ENABLED', Number(val));
    }
    this.enableLoading  = false
    if (this.groupEnable) {
      this.getUngroupEntity();
    }
    this.$refs.tableList && this.$refs.tableList.resize()
  }

  private viewEntityHandle (type: string) {
    this.$router.push({
      path: '/sysManage/group/entity',
      query: {
        type
      }
    })
  }

  private async getUngroupEntity () {
    this.ungroupLoading = true
    const { error, result } = await toAsyncWait(GroupApi.getUngroupEntity());
    if (!error) {
      const { data = {} } = result || {}
      this.total.host = data?.host || 0;
      this.total.service = data?.service || 0;
      this.total.namespace = data?.namespace || 0;
    }
  }
}
</script>

<style scoped lang='scss'>
.manage-group-cont {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
