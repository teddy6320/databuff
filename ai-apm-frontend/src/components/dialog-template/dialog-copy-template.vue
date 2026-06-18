<template>
  <el-dialog
    :visible.sync="showModel"
    title="-"
    :before-close="() => dialogCancelHandle(null, true)"
    :close-on-click-modal='!dialogPostLoading'
    :close-on-press-escape='!dialogPostLoading'
    :show-close='!dialogPostLoading'
    width='540px'
    append-to-body
  >
    <div>

      <div slot="footer">
        <el-button size="small" :disabled="dialogPostLoading" @click="dialogCancelHandle">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :loading="dialogPostLoading" @click="postHandle">{{ $t('modules.components.dialog-template.s_38cf16f2') }}</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';

@Component({})
export default class DialogComp extends Vue {
  @Prop() private value!: boolean;
  @Prop() private current!: any;

  @Watch('value')
  private onShowChange (newVal: boolean) {
    this.showModel = newVal
  }

  private showModel = false;
  private dialogPostLoading = false;

  // 关闭弹窗
  private dialogCancelHandle (payload?: any) {
    this.showModel = false;
    this.$emit('on-close', payload);
    this.$emit('input', false);
  }

  private postHandle () {
    this.dialogPostLoading = true;
  }
  
}
</script>

<style lang="scss" scoped>

</style>
