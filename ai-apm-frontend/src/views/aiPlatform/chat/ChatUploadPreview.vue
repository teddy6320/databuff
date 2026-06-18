<template>
  <div v-if="items.length" class="upload-preview-wrap">
    <div class="upload-preview-list">
      <div
        v-for="item in items"
        :key="item.id"
        :class="['upload-preview-item', item.type === 'image' ? 'is-image' : 'is-file']"
      >
        <el-image
          v-if="item.type === 'image' && item.url"
          class="upload-image-thumb"
          :src="item.url"
          fit="cover"
          :preview-src-list="imageUrls"
        />
        <div v-else class="upload-file-thumb">
          <i class="el-icon-document"></i>
          <span class="upload-file-name ell" :title="item.name">{{ item.name }}</span>
        </div>
        <i v-if="removable" class="el-icon-close upload-item-delete" @click.stop="$emit('remove', item.id)"></i>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';

export interface UploadPreviewItem {
  id: string
  type: 'image' | 'file'
  name: string
  url?: string
}

@Component
export default class ChatUploadPreview extends Vue {
  @Prop({ default: () => [] }) private items!: UploadPreviewItem[]
  @Prop({ default: true }) private removable!: boolean

  private get imageUrls (): string[] {
    return (this.items || [])
      .filter(item => item.type === 'image' && item.url)
      .map(item => item.url as string)
  }
}
</script>

<style lang="scss" scoped>
.upload-preview-wrap {
  padding: 10px 12px 0;
  background: #f8f9fc;
  border-radius: 6px 6px 0 0;
}

.upload-preview-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.upload-preview-item {
  position: relative;
  flex: none;
}

.upload-image-thumb {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  border: 1px solid #eef1f7;
}

.upload-file-thumb {
  width: 160px;
  height: 72px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #eef1f7;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  color: #2962ff;
  box-sizing: border-box;

  .el-icon-document {
    font-size: 18px;
  }
}

.upload-file-name {
  font-size: 12px;
  color: #475a7d;
  line-height: 1.3;
}

.upload-item-delete {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
</style>
