<template>
  <el-drawer
    :visible.sync="showDialogModel"
    :title="$t('modules.views.configStatus.agent.s_33305652')"
    :before-close="cancelPostHandle"
    :wrapper-closable='!postLoading'
    :close-on-press-escape='!postLoading'
    :show-close='!postLoading'
    destroy-on-close
    size='596px'
    class="agent-config-drawer">
    <div v-loading='loading' class="drawer-content flex-v">
      <div class="df mb-10">
        <div class="mr-10">{{ $t('modules.views.configStatus.agent.s_91a5efad') }}</div>
        <div class="flex-1 ell-3">
          <template v-for='(host, index) in hosts'>
            <template v-if="index > 0">、</template>
            <el-tooltip :key='host.hostName' :content="$t('modules.views.configStatus.agent.s_b6c12b02', { value0: host.hostIp })" effect='light'>
              <div class="dif">{{ host.hostName }}</div>
            </el-tooltip>
          </template>
        </div>
      </div>
      <codemirror ref="myCm"
        v-if='showDialogModel'
        :value="cmCode" 
        :options="cmOptions"
        @input="onCmCodeChange"
        :class="theme"
        class="config-code flex-1" />
    </div>

    <div class="drawer-footer">
      <el-button size="small" :disabled="postLoading" @click="cancelPostHandle">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
      <el-button type="primary" size="small" :loading="postLoading" @click="postHandle">{{ $t('modules.views.configInstall.plugin.s_be5fbbe3') }}</el-button>
    </div>
  </el-drawer>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import { State } from 'vuex-class';
import i18n from '@/i18n';
import Simplebar from 'simplebar-vue';
import { codemirror } from 'vue-codemirror';
// language js
import 'codemirror/mode/yaml/yaml.js'
// require styles
import 'codemirror/lib/codemirror.css';
// import 'codemirror/theme/3024-night.css';
// import 'codemirror/theme/3024-day.css';
import { toAsyncWait } from '@/utils/common';
import AgentApi from '@/api/agent';
import { decode, encode } from 'js-base64'
import { sortVersionObj } from '@/utils/compareVersion'

@Component({
  components: {
    codemirror,
    Simplebar: Simplebar as any,
  }
})
export default class AgentConfigDialog extends Vue {
  @State('theme') private theme!: 'dark' | 'light';

  @Prop() private showModel!: boolean;
  @Prop() private hostList!: any;

  @Watch('showModel')
  private onShowModelChange (newVal: boolean) {
    this.showDialogModel = newVal
    if (newVal && this.hosts.length) {
      // 排序后，取最新的agent
      const newVersion: any = sortVersionObj([...this.hosts], 'agentVersion').slice(-1)[0] || {}
      this.getAgentConfig(newVersion.id)
    } else {
      this.cmCode = ''
    }
  }

  get hosts (): any[] {
    return this.hostList.map((t: any) => ({
      id: t.id,
      hostName: t.hostName,
      hostIp: t.hostIp,
      agentVersion: t.agentVersion,
    }))
  }

  // 修改agent配置弹窗相关
  private showDialogModel = false
  private postLoading = false;
  private loading = false;

  // codemirror
  private cmCode = ``;
  get cmOptions () {
    return {
      tabSize: 4,
      // theme: this.theme === 'light' ? '3024-day' : '3024-night',
      lineNumbers: true,
      line: true,
      mode: 'text/x-yaml'
    }
  }

  private onCmCodeChange (code: string) {
    this.cmCode = code;
  }

  // 关闭弹窗
  private cancelPostHandle (payload?: any) {
    this.showDialogModel = false
    this.$emit('on-close', payload)
  }

  // 关闭弹窗
  private async postHandle () {
    // 判断是否有修改
    this.postLoading = true
    const params: any = {
      hosts: this.hosts.map((item: any) => item.hostName),
      configContent: encode(this.cmCode)
    }
    const { result, error } = await toAsyncWait(AgentApi.configUpdate(params))
    if (!error) {
      this.$message.success(i18n.t('modules.views.configStatus.agent.s_54836d78') as string);
      this.cancelPostHandle({ refresh: true });
    } else {
      this.$message.error(i18n.t('modules.views.appMonitor.serviceAnalysis.s_930442e2') as string)
    }
    this.postLoading = false
  }

  private async getAgentConfig (id: string) {
    const { result, error } = await toAsyncWait(AgentApi.getAgentDetail({ id }));
    if (!error) {
      const configContent = ((result || {}).data || {}).configContent || '';
      this.cmCode = configContent ? decode(configContent) : ''
    } else {
      this.cmCode = ''
    }
  }
}
</script>

<style lang="scss" scoped>
.agent-config-drawer {
  :deep(.el-drawer__body) {
    display: flex;
    flex-direction: column;
    padding: 0;
    overflow: hidden;
  }

  .drawer-content {
    flex: 1;
    padding: 15px 24px 20px;
    overflow: auto;
  }

  .drawer-footer {
    text-align: right;
    padding: 20px 24px;
    border-top: 1px solid var(--border-color-lighter);
  }

  :deep(.CodeMirror-scrollbar-filler) {
    display: none !important;
  }

  .config-code {
    overflow: hidden;
  }
}
</style>

<style lang="scss">
.config-code {
  .CodeMirror {
    height: 100%;
    background: #F7F7F7;
    color: var(--color-text-primary);
    font-size: 12px;
    line-height: 1.65;
  }
  div.CodeMirror-selected,
  .CodeMirror-line::selection, .CodeMirror-line > span::selection, .CodeMirror-line > span > span::selection {
    background: #A2CEF8;
  }
  .CodeMirror-line::-moz-selection, .CodeMirror-line > span::-moz-selection, .CodeMirror-line > span > span::selection {
    background: #A2CEF8;
  }
  .CodeMirror-gutters { background: #F7F7F7; border-right: 0px; }
  .CodeMirror-guttermarker-subtle, .CodeMirror-linenumber {
    color: var(--color-text-secondary);
  }
  .CodeMirror-guttermarker, span.cm-tag { color: #e45649; }
  .CodeMirror pre.CodeMirror-line, span.cm-property, span.cm-attribute, span.cm-string { color: #50a14f; }
  span.cm-comment { color: #a0a1a7; font-style: italic; }
  span.cm-atom, span.cm-number, span.cm-variable, span.cm-variable-2, span.cm-bracket { color: #986801; }
  span.cm-keyword { color: #0184bb; }
  span.cm-def, span.cm-link { color: #4078f2; }
  span.cm-error { background: #e45649; color: var(--color-text-secondary); }
  .CodeMirror-activeline-background { background: #e8f2ff; }
  .CodeMirror-matchingbracket { text-decoration: underline; color: #4078f2 !important; }

  &.dark {
    .CodeMirror {
      background: #282c34;
      color: #abb2bf;
    }
    div.CodeMirror-selected,
    .CodeMirror-line::selection, .CodeMirror-line > span::selection, .CodeMirror-line > span > span::selection {
      background: #4A76A0;
    }
    .CodeMirror-line::-moz-selection, .CodeMirror-line > span::-moz-selection, .CodeMirror-line > span > span::selection {
      background: #4A76A0;
    }
    .CodeMirror-guttermarker, span.cm-tag { color: #e06c75; }
    .CodeMirror pre.CodeMirror-line, span.cm-property, span.cm-attribute, span.cm-string { color: #98c379; }
    span.cm-comment { color: #5c6370; }
    span.cm-atom, span.cm-number, span.cm-variable, span.cm-variable-2, span.cm-bracket { color: #d19a66; }
    span.cm-keyword { color: #56b6c2; }
    span.cm-def, span.cm-link { color: #61aeee; }
    span.cm-error { background: #e06c75; color: #abb2bf; }
    .CodeMirror-activeline-background { background: #2F2F2F; }
    .CodeMirror-matchingbracket { text-decoration: underline; color: #61aeee !important; }
  }
}
</style>
