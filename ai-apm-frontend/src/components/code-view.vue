<template>
  <div :class="['code-view-wrapper', type, { nocopy: !showCopy }]">
    <code :class="lang ? `language-${lang}` : ''" v-html="codeView"></code>
    <button v-if="showCopy" @click="copyHandle(code)" :title="$t('modules.views.aiPlatform.chat.s_79d3abe9')" class="code-copy-btn">
      <span class="copy-icon db-icon-copy"></span>
    </button>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import { copy } from '@/utils/common'
import hljs from 'highlight.js/lib/common';
import 'highlight.js/styles/atom-one-light.css';

@Component
export default class CodeView extends Vue {
  @Prop({ default: '' }) private code!: string        // 代码
  @Prop({ default: 'block' }) private type!: 'block' | 'inline' // 块状样式风格 | 行内样式风格
  @Prop({ default: '' }) private lang!: string        // 语言
  @Prop({ default: true }) private showCopy!: boolean // 是否显示复制按钮
  @Prop({}) private copyCode!: string

  private codeView = ''

  @Watch('code', { immediate: true })
  private watchCode () {
    this.codeView = hljs.highlightAuto(this.code).value
  }

  // 复制
  private copyHandle (value: string) {
    if (this.copyCode) {
      copy(this.copyCode)
    } else {
      copy(value)
    }
  }
}
</script>

<style lang="scss" scoped>
.code-view-wrapper {
  display: block;
  position: relative;

  code {
    box-sizing: border-box;
    display: block;
    height: 100%;
    min-height: 40px;
    max-height: 400px;
    overflow: auto;
    padding: 10px 28px 10px 12px;
    background: #282c34;
    border-radius: 4px;
    color: #abb2bf;
    font-size: 12px;
    line-height: 1.65;
    word-break: break-all;
    white-space: pre-wrap;
  }

  &.nocopy code {
    padding-right: 12px;
  }

  .code-copy-btn {
    width: 20px;
    height: 20px;
    padding: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: transparent;
    border: none;
    color: var(--color-primary);
    user-select: none;
    outline: none;
    cursor: pointer;
    position: absolute;
    top: 10px;
    right: 6px;
    z-index: 1;
    .copy-icon {
      font-size: 12px;
    }
    &:focus .copy-icon {
      display: none;
    }
    &:focus:before {
      content: "";
      margin-top: -4px;
      display: inline-block;
      width: 10px;
      height: 5px;
      border: 2px solid #1eaa99;
      border-top: none;
      border-right: none;
      transform: rotate(-50deg);
    }
  }

  &.inline {
    display: inline-block;
    vertical-align: middle;
    margin-top: -2px;

    code {
      min-height: 20px;
      padding: 0 22px 0 6px;
      background: #2a2d32;
      color: #E3E8EC;
      line-height: 20px;
      white-space: normal;
      word-break: break-all;
    }

    .code-copy-btn {
      top: 0;
      right: 2px;
    }

    &.nocopy code {
      padding-right: 12px;
    }
  }
}

:root[data-theme=light] .code-view-wrapper {
  code {
    background: #F7F7F7;
    color: var(--color-text-primary);
  }
}
</style>
