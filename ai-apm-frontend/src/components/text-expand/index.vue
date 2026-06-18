<template>
  <div :style="getBoxStyles" :class="{ collapsed: collapsed }" class="text-expand-box">
    <div ref="textBox" class="text-expand-real">{{ previewContent }}<div
      v-if="needExpand"
      :style="getToolsStyles"
      class="text-expand-tools">
        <template v-if="collapsed">... </template>
        <span @click="toggleHandle" class="cp blue font-13 dib vt">
          {{ collapsed ? $t('modules.components.text-expand.s_e2edde5a') : $t('modules.components.text-expand.s_def9e98b')  }}
          <span class="db-icon-up icon-vm ml-4 text-expand-arrow"></span>
        </span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import { Getter } from 'vuex-class';

@Component
export default class TextExpand extends Vue {
  @Getter('themeVariables') private themeVars!: any;

  @Prop({ default: 24 }) private lineHeight!: number;
  @Prop({ default: 3 }) private maxLines!: number;
  @Prop({ default: '' }) private content!: string;
  @Prop({ default: '' }) private bgColor!: string;

  public $refs!: {
    textBox: HTMLDivElement
  }

  @Watch('content', { immediate: true })
  private onContentChange () {
    this.previewContent = String(this.content || '').trim()
    this.$nextTick(() => {
      this.getRealHeight()
    })
  }

  get calcHeight () {
    return this.lineHeight * this.maxLines
  };

  get getBoxStyles () {
    return this.needExpand && this.collapsed ? {
      height: `${this.calcHeight}px`,
      lineHeight: `${this.lineHeight}px`,
    } : {
      lineHeight: `${this.lineHeight}px`,
    }
  }

  get getToolsStyles () {
    return this.collapsed ? {
      backgroundColor: this.bgColor || this.themeVars.bgColor,
      position: 'absolute',
      top: '0',
      right: '0',
      textAlign: 'right',
    } : {
      display: 'inline-block',
    }
  }

  private realHeight = 0;
  private collapsed = true;
  private needExpand = true;
  private previewContent = '';

  private mounted () {
    window.addEventListener('resize', this.getRealHeight);
  }

  private beforeDestroy () {
    window.removeEventListener('resize', this.getRealHeight);
  }

  private getRealHeight () {
    this.realHeight = this.$refs.textBox.clientHeight
    this.needExpand = this.realHeight > this.calcHeight
  }

  private toggleHandle () {
    this.collapsed = !this.collapsed
    this.$emit('on-toggle', this.collapsed)
  }
}
</script>

<style lang='scss' scoped>
.text-expand-box {
  width: 100%;
  position: relative;
  overflow: hidden;

  &.collapsed .text-expand-arrow {
    transform: rotateZ(180deg);
  }

  .text-expand-real {
    width: 100%;
    word-break: break-word;
    // white-space: break-spaces;
    white-space: normal;
    overflow: hidden;
    position: relative;
  }

  .text-expand-tools {
    padding-left: 6px;
    font-weight: normal;
    white-space: nowrap;
  }

  .text-expand-arrow {
    transition: transform 0.3 ease;
  }
}
</style>
