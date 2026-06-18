<template>
  <div class="update-step-1"
    v-loading='versionLoading'>
    <div class="mb-10">{{ $t('modules.views.configStatus.agent.s_5ca8e6fd') }}<span class="font14">{{ $t('modules.views.configStatus.agent.s_cec24a71', { value0: minVersion }) }}</span></div>
    <el-table :data="tableSource"
      highlight-current-row size="small"
      :max-height='205'
      :row-class-name="getRowClassHandle"
      tooltip-effect='light'>
      <el-table-column prop='version' show-overflow-tooltip :label="$t('modules.views.configStatus.agent.s_fe2df04a')" width="120"></el-table-column>
      <el-table-column prop='remark' show-overflow-tooltip :label="$t('modules.views.configStatus.agent.s_d9b141eb')">
        <!-- <pre slot-scope="{row}">{{ row.remark }}</pre> -->
      </el-table-column>
      <el-table-column :label="$t('modules.views.configStatus.agent.s_0589a599')" align="center" header-align="center" width="80">
        <div slot-scope="{ row }">
          <el-radio v-model='radioModels[row.id]' :disabled='canChooseVersion(row.version, minVersion)' @input="changeHandle(row)"></el-radio>
        </div>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import { compareVersion, sortVersionObj } from '@/utils/compareVersion'

@Component({})
export default class UpdateStep1 extends Vue {
  @Prop({ default: () => [] }) private versionList!: any[];
  @Prop({ default: () => [] }) private agentList!: any[];
  @Prop() private versionLoading!: boolean;

  get minVersion () {
    const mins: any[] = sortVersionObj(Array.from(this.agentList), 'agentVersion')
    const [ min = {} ] = mins
    const { agentVersion = '' } = min || {}
    return agentVersion || ''
  }
  
  private tableSource: any[] = [];
  private radioModels: any = {};
  private singleSelection: any = null;

  @Watch('versionList', { immediate: true })
  private onVersionListChange (newVal: any[]) {
    if (newVal && newVal.length) {
      newVal.forEach((item) => {
        this.$set(this.radioModels, item.id, false)
      })
      this.tableSource = newVal
    }
  }

  private getRowClassHandle (payload: any) {
    const { row = {} } = payload
    return compareVersion(row.version, this.minVersion) <= 0 ? 'row-disabled' : ''
  }

  private canChooseVersion (currVersion: string, minVersion: string) {
    return compareVersion(currVersion, minVersion) <= 0
  }

  private changeHandle (row: any) {
    for ( const key in this.radioModels) {
      if (key !== `${row.id}`) {
        this.radioModels[key] =  false
      }
    }
    this.$emit('on-change', row)
  }
}
</script>

<style lang="scss">
.update-step-1 .row-disabled td .cell{
  color: var(--color-text-secondary);
}
</style>
