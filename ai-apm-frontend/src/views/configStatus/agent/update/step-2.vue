<template>
  <div class="update-step-2"
    v-loading='tableLoading'>
    <div class="update-step-body">
      <div class="update-version-list">
        <div class="pb-10">
          <span>{{ $t('modules.views.configStatus.agent.s_4e229d21') }}</span>
          <el-select v-model='targetVersion' size="small" filterable>
            <el-option value='all' :label="$t('modules.views.configStatus.agent.s_a0274258')"></el-option>
            <el-option v-for='version in versionList' :key='version' :label='version' :value='version'></el-option>
          </el-select>
        </div>
        <el-table :data="tableSource"
          highlight-current-row size="small"
          :max-height='205'
          :row-class-name="getRowClassName"
          tooltip-effect='light'>
          <el-table-column prop='hostName' show-overflow-tooltip :label="$t('modules.views.configStatus.agent.s_3117f05e')"></el-table-column>
          <el-table-column prop='hostIp' show-overflow-tooltip :label="$t('modules.views.configStatus.agent.s_c5fa15a9')"></el-table-column>
          <el-table-column prop='agentVersion' :label="$t('modules.views.configStatus.agent.s_a33ea80c')" width="120">
            <div slot-scope="{ row }">
              {{ row.agentVersion }}
              <el-tooltip v-if="compareVersion(row.agentVersion, chooseVersion.version) > 0"
                class="ml-5"
                placement="top" effect="light"
                :content="$t('modules.views.configStatus.agent.s_ff4482de')">
                <i class="el-icon el-icon-warning-outline"></i>
              </el-tooltip>
              <el-tooltip v-else-if="compareVersion(row.agentVersion, chooseVersion.version) == 0"
                class="ml-5"
                placement="top" effect="light"
                :content="$t('modules.views.configStatus.agent.s_e034f6bd')">
                <i class="el-icon el-icon-warning-outline"></i>
              </el-tooltip>
            </div>
          </el-table-column>
        </el-table>
      </div>
      <div class="update-version-info">
        <div class="pb-10 lh-32">{{ $t('modules.views.configStatus.agent.s_989b8bee') }}</div>
        <div class="update-version-info-body">
          <div class="update-version-info-header"><span class="">{{ $t('modules.views.configStatus.agent.s_f94c64ac') }}</span>{{ chooseVersion.version }}</div>
          <div style="padding-left: 10px;"><span class="">{{ $t('modules.views.configStatus.agent.s_efd8921b') }}</span></div>
          <pre class="code-pre">{{ chooseVersion.remark || '-' }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator'
import { compareVersion, sortVersionObj, sortVersion } from '@/utils/compareVersion'

@Component({})
export default class UpdateStep2 extends Vue {
  @Prop({ default: () => [] }) private agentList!: any[];
  @Prop({ default: '' }) private chooseVersion!: any;
  private tableLoading = false;

  private targetVersion = 'all';

  private compareVersion = compareVersion;

  get tableSource () {
    const list = this.targetVersion !== 'all' ? this.agentList.filter((item) => item.agentVersion === this.targetVersion)
      : this.agentList
    return sortVersionObj(list, 'agentVersion')
  }

  get versionList () {
    const list: string[] = [...new Set(this.agentList.map(t => t.agentVersion))];
    return sortVersion(list)
  }

  private created () {
    //
  }

  private getRowClassName ({ row }: { row: any}) {
    return row.agentVersion.replaceAll('.', '') >= this.chooseVersion.version.replaceAll('.', '') ? 'update-step-row-disabled' : ''
  }
}
</script>

<style lang='scss' scoped>
.update-step-2 {
  h5 {
    margin: 0;
    height: 32px;
    line-height: 32px;
    margin-bottom: 10px;
  }
}
.update-step-body {
  display: flex;
  flex-wrap: nowrap;
  overflow: hidden;
  h5 {
    line-height: 32px;
  }
  .update-version-list {
    flex: 1;
  }

  .update-version-info {
    width: 316px;
    padding-left: 16px;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .update-version-info-body {
      max-height: 205px;
      overflow-y: auto;
      border-bottom: 1px solid var(--border-color-lighter);
      flex: 1;
      font-size: 12px;
    }
    .update-version-info-header {
      padding: 10px 10px 9px;
      height: 40px;
      font-size: 13px;
      line-height: 20px;
      background-color: var(--background-color-base);
      border-bottom: 1px solid var(--border-color-lighter);
      margin-bottom: 5px;
    }
  }
  .code-pre {
    padding: 5px 10px 10px 28px;
    margin: 0;
    text-align: left;
    word-wrap: normal;
    word-spacing: normal;
    word-break: break-all;
    white-space: pre-wrap;
    hyphens: none;
    -webkit-box-direction: normal;
    tab-size: 2;
    font-family: Roboto,Helvetica Neue,Arial,sans-serif;
  }

  :deep(.update-step-row-disabled td .cell) {
    color: var(--color-text-secondary) !important;
  }
}
</style>
