<template>
  <div class="page-cont flex-v">
    <div class="page-list" v-loading="isLoading">
      <db-table
        ref="listTable"
        :data="tableList"
        :total="tableTotal"
        :columnConfig="columnConfig">
        <div slot="total" slot-scope="{ total }" class="flex-1 flex-h-jc describe">
          {{ $t('modules.views.alarmCenter.alarm.s_9ad9830c', { value0: total }) }}

          <el-tooltip :disabled="!overMaxLimit" effect="light" :content="$t('modules.views.configStatus.agentPackages.s_ca412132')" placement="top">
            <chunk-upload
              ref='chunkUpload'
              accept=".zip"
              :disabled="overMaxLimit"
              :before-upload="uploadBeforeHandle"
              :on-success="uploadSuccessHandle"
              :on-error="uploadErrorHandle"
              :on-progress='uploadProcessHandle'>
            </chunk-upload>
          </el-tooltip>
        </div>

        <el-table-column slot="suffix" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" :min-width="80">
          <template slot-scope="{ row, $index }">
            <a @click.stop="deleteHandle(row, $index)" class="blue">{{ $t('modules.views.hide.advancedConfig.s_2f4aaddd') }}</a>
          </template>
        </el-table-column>
      </db-table>
    </div>

    <el-dialog
      :visible.sync="uploadInfo.showModel"
      :close-on-click-modal="false"
      width="480px"
      class="upload-modal">
      <div class="flex-h-jc mb-10">
        <span>
          <i class="el-icon el-icon-loading"></i>
          {{ uploadInfo.fileName }}
        </span>
        <span v-if='uploadInfo.percent < 100'>
          {{ uploadInfo.percent }}%
        </span>
        <span v-else class="el-icon el-icon-circle-check green"></span>
      </div>

      <el-progress :percentage="uploadInfo.percent" :show-text="false"></el-progress>
    </el-dialog>
  </div>
</template>

<script lang='ts'>
import { Vue, Component } from 'vue-property-decorator'
import AgentApi from '@/api/agent';
import i18n from '@/i18n';
import { toAsyncWait } from '@/utils/common';
import { Upload } from 'element-ui'
import ChunkUpload from './chunk-upload.vue'

@Component({
  components: {
    ChunkUpload,
  }
})
export default class AgentPackages extends Vue {
  public $refs!: {
    chunkUpload: ChunkUpload
    uploadBtn: Upload
    listTable: any
  }

  private isLoading = false;
  private tableTotal = 0;
  private tableList: any[] = [];

  private columnConfig = [
    { field: 'version', label: i18n.t('modules.views.configStatus.agentPackages.s_d0b29584') as string, labelKey: 'modules.views.configStatus.agentPackages.s_d0b29584', minWidth: 100 },
    { field: 'remark', label: i18n.t('modules.views.configStatus.agentPackages.s_d9b141eb') as string, labelKey: 'modules.views.configStatus.agent.s_d9b141eb', minWidth: 300 },
    { field: 'uploadTime', label: i18n.t('modules.views.configStatus.agentPackages.s_cae25527') as string, labelKey: 'modules.views.configStatus.agentPackages.s_cae25527', minWidth: 100 },
  ]

  // 上传更新包数量超过限制
  get overMaxLimit () {
    return this.tableTotal >= 5
  }

  private mounted () {
    this.getTableList()
  }

  private async getTableList () {
    this.isLoading = true;
    const { result, error } = await toAsyncWait(AgentApi.getVersionList())
    this.isLoading = false;
    if (!error) {
      const { data = [] } = result || {};
      this.tableList = (data || []).map((t: any) => ({
        ...t,
        remark: (t.remark || '').replaceAll('\\r\\n', ' '),
      }));
      this.tableTotal = this.tableList.length;
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.appMonitor.serviceFlow.s_e05c1ca3') as string);
    }
  }

  private async deleteHandle (row: any, idx: number) {
    this.$confirm(i18n.t('modules.views.configStatus.agentPackages.s_733062e4') as string, i18n.t('common.hint') as string, { type: 'warning' })
      .then(async () => {
        this.isLoading = true;
        const { result, error } = await toAsyncWait(AgentApi.deletePackage(row.id));
        this.isLoading = false;
        if (!error) {
          this.$message.success(i18n.t('modules.views.aiPlatform.experts.s_0007d170') as string);
          this.tableList.splice(idx, 1);
        } else if (error.message !== 'interrupt') {
          this.$message.error(error.message);
        }
      })
      .catch(() => null)
  }

  // 上传更新包相关
  private uploadInfo: any = {
    fileName: i18n.t('modules.views.configStatus.agentPackages.s_0611cccf') as string,
    percent: 0,
    showModel: false,
  }
  private uploadBeforeHandle (file: any) {
    const { name } = file
    this.uploadInfo.fileName = name
    this.uploadInfo.showModel = true
  }
  private uploadSuccessHandle() {
    this.$refs.chunkUpload.cancelUpload()
    this.$message.success(i18n.t('modules.views.configStatus.agentPackages.s_a7699ba7') as string);
    this.getTableList()
    this.uploadInfo.showModel = false
  }
  private uploadErrorHandle(error: any) {
    // console.error('上传失败:', error)
    this.$refs.chunkUpload.cancelUpload()
    this.$message.error(error?.message || i18n.t('modules.views.configStatus.agentPackages.s_54e5de42') as string);
    this.uploadInfo.showModel = false
  }
  private uploadProcessHandle(data: any) {
    const { chunkIndex, chunkPercent, totalChunks, percent } = data
    // console.log('progress', `${Math.floor(percent)}% (${chunkIndex}/${totalChunks}) --- ${chunkPercent.toFixed(2)}%`)
    this.uploadInfo.percent = Math.floor(percent)
  }
}
</script>

<style lang="scss" scoped>
.page-cont {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);
  overflow: auto;

  .page-list {
    flex: 1;
    min-height: 300px;
    padding: 10px 20px 20px;
    border-radius: 4px;
    background-color: var(--bg-color);
    overflow: hidden;
  }

  .action-btn {
    display: inline-block;
    margin-right: 8px;
    color: var(--color-text-link);
    &:last-child {
      margin-right: 0;
    }
  }
}

.upload-modal {
  :deep(.el-dialog) {
    padding: 0;
  }
  :deep(.el-dialog__header) {
    display: none;
  }
  :deep(.el-dialog__body) {
    padding: 40px;
  }
}
</style>
