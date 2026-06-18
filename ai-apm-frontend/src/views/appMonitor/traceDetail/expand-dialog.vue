<template>
  <el-dialog
    :visible.sync="showDialog"
    :title="title"
    width="640px"
    :before-close="cancelHandle"
    class="export-pipeline-dialog">
    <code-view :code="previewText" :showCopy="false" />

    <div slot="footer">
      <el-button size="small" @click="cancelHandle">{{ $t('modules.views.alarmCenter.alarm.s_b15d9127') }}</el-button>
      <el-button type="primary" size="small" @click="copyHandle">{{ $t('modules.views.aiPlatform.chat.s_79d3abe9') }}</el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import CodeView from '@/components/code-view.vue';
import { copy } from '@/utils/common';

@Component({
  components: {
    CodeView,
  },
})
export default class ExpandDialog extends Vue {
  @Prop({ default: () => false }) private showModel!: boolean;
  @Prop({ default: '' }) private previewText!: string;
  @Prop({ default: '' }) private title!: string;

  @Watch('showModel')
  private onShowModelChange (newVal: boolean) {
    this.showDialog = newVal
  }

  private showDialog = false;

  private cancelHandle () {
    this.showDialog = false
    this.$emit('on-close')
  }

  private copyHandle () {
    copy(this.previewText)
  }
}
</script>

<style lang="scss" scoped>
.export-pipeline-dialog {
  :deep(.el-dialog) {
    max-height: 90vh;
    display: inline-flex;
    flex-direction: column;
  }
  :deep(.el-dialog__body) {
    padding-bottom: 10px;
    flex: 1;
    overflow: auto;
  }
  :deep(.code-view-wrapper code) {
    padding: 0 !important;
    background: none !important;
    max-height: none;
  }
}
</style>
