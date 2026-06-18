<template>
  <label
    :for="!disabled ? 'chunk-upload-file' : ''"
    :class="{ disabled }"
    class="chunk-upload-wrapper" >
    <span class="chunk-upload-button"><i class="db-icon-upload font-12 mr-5"></i>{{ $t('modules.views.configStatus.agentPackages.s_3418f126') }}</span>
    <input
      ref="fileInput"
      id="chunk-upload-file"
      name="chunk-upload-file"
      type="file"
      :accept="accept"
      @change="handleFileChange"
      class="chunk-upload-input" />
  </label>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import SparkMD5 from 'spark-md5';
import { toAsyncWait } from '@/utils/common';
import AgentApi from '@/api/agent';

@Component
export default class ChunkUpload extends Vue {
  @Prop({ default: '' }) private accept!: string;
  @Prop({ default: false }) private disabled!: boolean;
  @Prop({ default: () => true }) private beforeUpload!: (file: File) => boolean;
  @Prop({ default: () => null }) private onSuccess!: () => void;
  @Prop({ default: () => null }) private onError!: (error: any) => void;
  @Prop({ default: () => null }) private onProgress!: (percent: any) => void;

  public $refs!: {
    fileInput: HTMLInputElement
  }

  private file: File | null = null;
  private progress = 0;
  private chunkSize = 100 * 1024 * 1024; // 每个分片大小 100MB
  private fileHash = '';
  private chunks: any[] = [];
  private uploadedChunks: number[] = [];
  private uploadStatus = '';
  private isUploading = false;
  private currentChunk = 0;

  private async handleFileChange(e: any) {
    if (this.disabled) {
      return
    }

    this.file = e.target.files[0]
    if (!this.file) {
      return
    }

    // 上传前的钩子
    if (this.beforeUpload(this.file) === false) {
      return;
    }

    this.resetUploadState()
    this.uploadStatus = i18n.t('modules.views.configStatus.agentPackages.s_ff890a4e') as string

    try {
      await this.startUpload()
    } catch (error: any) {
      // console.error('上传失败:', error)
      this.uploadStatus = i18n.t('modules.views.configStatus.agentPackages.s_b019c47a') as string + error.message

      // 上传失败的钩子
      this.onError(error);
    }
  }

  private resetUploadState() {
    this.progress = 0
    this.chunks = []
    this.fileHash = ''
    this.uploadedChunks = []
    this.uploadStatus = ''
    this.isUploading = false
    this.currentChunk = 0
  }

  public cancelUpload() {
    this.resetUploadState()
    this.$refs.fileInput.value = '' // 清空文件输入
  }

  private async startUpload() {
    this.isUploading = true

    // 计算文件hash
    this.uploadStatus = i18n.t('modules.views.configStatus.agentPackages.s_17da0ab8') as string
    this.fileHash = await this.calculateFileHash(this.file as File) as string

    // 创建分片
    this.uploadStatus = i18n.t('modules.views.configStatus.agentPackages.s_fa8e3eea') as string
    this.chunks = this.createChunks(this.file as File, this.chunkSize)

    // 检查哪些分片已经上传过
    // this.uploadStatus = i18n.t('modules.views.configStatus.agentPackages.s_b6bd8d3d') as string
    // await this.checkUploadedChunks()

    // 顺序上传未完成的分片
    this.uploadStatus = i18n.t('modules.views.configStatus.agentPackages.s_566f34e0') as string
    await this.uploadChunksSequentially()

    // 所有分片上传完成后，通知服务器合并
    // this.uploadStatus = i18n.t('modules.views.configStatus.agentPackages.s_fad71448') as string
    // await this.mergeChunks()

    this.uploadStatus = i18n.t('modules.views.configStatus.agentPackages.s_0dfca700') as string
    this.progress = 100
    this.isUploading = false

    // 上传成功的钩子
    this.onSuccess();
  }

  // 计算文件hash（用于标识文件，可选）
  private async calculateFileHash (file: File) {
    return new Promise((resolve) => {
      const spark = new SparkMD5.ArrayBuffer()
      const reader = new FileReader()
      const size = file.size
      const offset = 2 * 1024 * 1024 // 只读取前2MB和最后2MB计算hash（优化大文件计算）

      // 前2MB
      const head = file.slice(0, offset)
      // 最后2MB
      const tail = file.slice(Math.max(size - offset, 0))

      const chunks = [head, tail]
      let currentChunk = 0

      reader.onload = (e: any) => {
        spark.append(e.target.result)
        currentChunk++

        if (currentChunk < chunks.length) {
          reader.readAsArrayBuffer(chunks[currentChunk])
        } else {
          resolve(spark.end())
        }
      }

      reader.readAsArrayBuffer(chunks[currentChunk])
    })
  }

  // 创建分片
  private createChunks(file: File, chunkSize: number) {
    const chunks = []
    let start = 0
    while (start < file.size) {
      const end = Math.min(start + chunkSize, file.size)
      chunks.push({
        index: chunks.length,
        file: file.slice(start, end)
      })
      start = end
    }
    return chunks
  }

  // private async checkUploadedChunks() {
  //   try {
  //     const { data } = await axios.get('/api/check-upload', {
  //       params: {
  //         fileHash: this.fileHash,
  //         totalChunks: this.chunks.length
  //       },
  //       cancelToken: this.cancelToken.token
  //     })
  //     this.uploadedChunks = data.uploadedChunks || []
  //   } catch (error) {
  //     if (!axios.isCancel(error)) {
  //       // console.error('检查已上传分片失败:', error)
  //       throw error
  //     }
  //   }
  // }

  // 顺序上传分片
  private async uploadChunksSequentially() {
    for (let i = 0; i < this.chunks.length; i++) {
      if (this.uploadedChunks.includes(i)) {
        // 跳过已上传的分片
        this.currentChunk = i
        this.updateProgressForSkippedChunk(i)
        continue
      }

      this.currentChunk = i
      await this.uploadSingleChunk(i)
    }
  }

  private async uploadSingleChunk(chunkIndex: number) {
    const chunk = this.chunks[chunkIndex]
    const formData = new FormData()
    formData.append('file', chunk.file)
    formData.append('fileIdentifier', this.fileHash)
    formData.append('currentChunk', chunk.index + 1)
    formData.append('totalChunks', this.chunks.length as any)

    try {
      const { result, error } = await toAsyncWait(AgentApi.uploadPackage(
        formData,
        {
          onUploadProgress: (progressEvent: any) => {
            const percent = (progressEvent.loaded / progressEvent.total) * 100
            this.updateTotalProgress(chunk.index, percent)
          }
        }
      ));

      if (!error) {
        // console.log('result', result)
      } else {
        throw error
      }

      // 上传成功后添加到已上传分片列表
      this.uploadedChunks.push(chunkIndex)
    } catch (error) {
      // console.error(`分片 ${chunkIndex} 上传失败:`, error)
      throw error
    }
  }

  // 更新跳过分片的进度
  private updateProgressForSkippedChunk(chunkIndex: number) {
    // 假设跳过的分片是100%完成的
    this.updateTotalProgress(chunkIndex, 100)
  }

  private updateTotalProgress(chunkIndex: number, chunkPercent: number) {
    // 计算已上传的总字节数
    let uploadedSize = 0

    // 已完成的完整分片
    for (let i = 0; i < chunkIndex; i++) {
      uploadedSize += this.chunks[i].file.size
    }

    // 当前正在上传的分片
    if (chunkIndex < this.chunks.length) {
      uploadedSize += (this.chunks[chunkIndex].file.size * chunkPercent / 100)
    }

    // 计算总进度
    const totalSize = this.file?.size as number
    this.progress = (uploadedSize / totalSize) * 100

    // 上传时的钩子
    this.onProgress({
      chunkIndex: chunkIndex + 1,
      chunkPercent,
      totalChunks: this.chunks.length,
      percent: this.progress,
    })
  }

  // private async mergeChunks() {
  //   try {
  //     await axios.post('/api/merge-chunks', {
  //       fileHash: this.fileHash,
  //       fileName: this.file.name,
  //       totalChunks: this.chunks.length
  //     }, {
  //       cancelToken: this.cancelToken.token
  //     })
  //   } catch (error) {
  //     if (!axios.isCancel(error)) {
  //       // console.error('合并分片失败:', error)
  //       throw error
  //     }
  //   }
  // }
}
</script>

<style lang="scss" scoped>
.chunk-upload-button {
  display: inline-flex;
  align-items: center;
  padding: 8px;
  line-height: 14px;
  font-size: 13px;
  border: 1px solid var(--color-primary);
  border-radius: 4px;
  color: var(--color-primary);
  transition: all 0.3s;
  cursor: pointer;
  user-select: none;
  &:hover {
    background-color: var(--color-primary);
    color: #fff;
  }
}

.chunk-upload-input {
  display: none;
}

.chunk-upload-wrapper.disabled {
  .chunk-upload-button,
  .chunk-upload-button:hover {
    background: none;
    border-color: var(--border-color-lighter);
    color: var(--color-info);
    cursor: not-allowed;
  }
}
</style>
