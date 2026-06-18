<template>
  <div
    v-html="markedData"
    ref="markedWrap"
    :class="['marked-view-wrapper', { nocopy: !showCopy }]"></div>
</template>

<script lang="ts">
/**
 * Markdown 渲染组件。
 *
 * 注意：禁止在 marked.parse 之前对 data 做字符串预处理（正则替换、normalize 等）。
 * LLM 输出应原样交给 marked；预处理极易误伤表格/标题/代码块，导致解析失败。
 * 格式问题应在 prompt、后端结构化输出或换渲染方案上解决，不要在前端 patch 文本。
 */
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import Clipboard from 'clipboard'
import { marked } from 'marked'
import hljs from 'highlight.js/lib/common';
import i18n from '@/i18n';
import 'highlight.js/styles/atom-one-light.css';
marked.setOptions({
  renderer: new marked.Renderer(),
  highlight: (code: string, lang: string = 'bash') => {
    // const language = hljs.getLanguage(lang) ? lang : 'bash'
    return hljs.highlightAuto(code).value
  },
  // langPrefix: 'hljs language-', // 代码高亮 code标签的className前缀
  gfm: true, // 允许 GitHub标准的markdown
  tables: true, // 允许支持表格语法（该选项要求 gfm 为true）
  breaks: true, // 允许回车换行（该选项要求 gfm 为true）
  pedantic: false, // 不纠正原始模型任何的不良行为和错误（默认为false）
  sanitizer: false, // 对输出进行过滤（清理），将忽略任何已经输入的html代码（标签）
  smartLists: false, // 使用比原生markdown更智能的列表
  smartypants: false, // 使用智能标点符号表示引号和破折号
})

@Component
export default class MarkedView extends Vue {
  @Prop({ default: '' }) private data!: string
  @Prop({ default: 'markdown' }) private type!: string // markdown | code
  @Prop({ default: true }) private showCopy!: boolean

  public $refs!: {
    markedWrap: HTMLDivElement
  }

  private markedData: string = ''

  @Watch('data', { immediate: true })
  private watchData () {
    const data = this.type !== 'code' ? this.data : `\n\`\`\`\n${this.data}\n\`\`\`\n`
    // 直接 parse，勿预处理 — 见文件头注释
    this.markedData = marked.parse(data);
    if (this.showCopy) {
      this.initCopyButton();
    }
  }

  private created() {
    if (this.showCopy) {
      this.initMarkedClipboard();
    }
  }

  // 插入复制按钮
  private initCopyButton () {
    this.$nextTick(() => {
      const $preList = this.$refs.markedWrap.querySelectorAll('pre')
      $preList.forEach(($pre: any) => {
        const $code = $pre.querySelector('code')
        if (!$code) {
          return
        }
        const codeText = $code.innerText
        const copyId = `marked-${Math.random().toString(36).substring(2)}`
        const copyBtn = `<button class="marked-copy-btn" :title="$t('modules.views.aiPlatform.chat.s_79d3abe9')" data-clipboard-action="copy" data-clipboard-target="#${copyId}"><span class="copy-icon db-icon-copy"></span></button><textarea id="${copyId}" style="position:absolute;top:-9999px;left:-9999px;z-index:-9999;">${codeText}</textarea>`
        $code.insertAdjacentHTML('afterend', copyBtn)
      })
    })
  }

  // 创建全局的剪切板实例
  private initMarkedClipboard () {
    if (!(window as any).markedClipboard) {
      const clipboard = new Clipboard('.marked-view-wrapper .marked-copy-btn');
      clipboard.on('success', (e: any) => {
        this.$notify({
          title: '',
          message: i18n.t('modules.components.s_a28aa67f') as string, messageKey: 'modules.components.s_a28aa67f',
          duration: 1000,
          showClose: false,
          customClass: 'notification-copy success',
        });
        e.clearSelection()
      });
      clipboard.on('error', (e: any) => {
        this.$notify({
          title: '',
          message: i18n.t('modules.components.s_cd981710') as string, messageKey: 'modules.components.s_cd981710',
          duration: 1000,
          showClose: false,
          customClass: 'notification-copy error',
        });
      });
      (window as any).markedClipboard = clipboard;
    }
  }
}
</script>

<style lang="scss">
.marked-view-wrapper {
  font-size: 13px;
  line-height: 24px;
  color: var(--color-text-primary);

  h1,h2,h3,h4,h5 {
    margin-bottom: 10px;
    font-weight: 500;
  }
  h1 {
    font-size: 22px;
  }
  h2 {
    font-size: 20px;
  }
  h3 {
    font-size: 18px;
  }
  h4 {
    font-size: 16px;
  }
  h5 {
    font-size: 14px;
  }
  h6 {
    font-size: 1em;
  }

  img,pre,table,ul,ol {
    margin: 0 0 10px;
  }

  p {
    margin: 5px 0;
  }

  img {
    display: block;
  }

  p a code,
  a {
    color: var(--color-text-link);
  }

  ul,
  ol {
    padding-left: 16px;
    list-style-type: revert;
  }

  table {
    width: 100%;
    display: table;
    border-collapse: collapse;
    border-spacing: 0;
    border-radius: 4px 4px 0 0;
    overflow: hidden;
    tr:hover {
      background-color: var(--table-hover-color);
    }
    td,
    th {
      border-bottom: 1px solid var(--border-color-lighter);
      padding: 9px 10px 8px;
      font-size: 12px;
      line-height: 22px;
      text-align: left;
      color: var(--color-text-primary);
      font-weight: normal;
    }
    th {
      background-color: var(--background-color-base);
      border-right-color: var(--border-color-light);
      font-size: 13px;
      white-space: nowrap;
    }
  }

  code {
    box-sizing: border-box;
    margin: 0 6px;
    padding: 3px 6px;
    border-radius: 4px;
    background-color: #2a2d32;
    color: #e3e8ec;
    font-size: 12px;
  }

  pre {
    box-sizing: border-box;
    font-size: 12px;
    position: relative;
    code {
      display: block;
      height: 100%;
      min-height: 40px;
      max-height: 400px;
      overflow: auto;
      margin: 0;
      padding: 10px 28px 10px 12px;
      background: #282c34;
      border-radius: 4px;
      color: #abb2bf;
      font-size: 12px;
      line-height: 1.65;
      word-break: break-all;
      white-space: pre-wrap;
    }
  }

  &.nocopy pre code {
    padding-right: 12px;
  }

  .marked-copy-btn {
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
}

:root[data-theme=light] .marked-view-wrapper {
  code {
    background: #F5F6F7;
    color: var(--color-text-primary);
  }
}
</style>
