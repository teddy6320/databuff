<template>
  <div class="run-log-wrapper">
    <div class="run-log" v-loading="logLoading">
      <div class="flex-h mb-16">
        <scroll-select
          v-model="host"
          @change="hostChangeHandle"
          :options="hostList"
          :clearable="false"
          :showTitle="true"
          class="flex-1 mr-16" />
        <el-button
          @click="getLatestLogHandle"
          :disabled="latestLogDisabled"
          :type="latestLogDisabled ? '' : 'primary'"
          plain size="small">
          {{ currHostData.status === $t('modules.views.configStatus.agent.s_50d4a850') ? $t('modules.views.configStatus.runLog.s_7b2b0b80') : latestLogLoading ? $t('modules.views.configStatus.runLog.s_cb4107e8') : $t('modules.views.configStatus.runLog.s_fad90e9e')  }}
        </el-button>
      </div>

      <db-table
        ref="listTable"
        :data="logList"
        :columnConfig="columnConfig"
        :showTotal="false"
        class="run-log-list">
        <template slot="column-logName" slot-scope="{ row }">
          <span v-if="row.isNew" class="log-new">NEW</span>
          {{ row.logName || '-' }}
        </template>

        <el-table-column slot="suffix" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" :min-width="80">
          <template slot-scope="{ row }">
            <a
              @click.stop="downloadLogHandle(row)"
              :class="{ 'action-disabled': row.downloading }"
              class="blue">{{ $t('modules.views.configStatus.runLog.s_f26ef914') }}</a>
          </template>
        </el-table-column>
      </db-table>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { compareVersion } from '@/utils/compareVersion'
import { FirstLetterCapital } from '@/utils/filters/common';
import { toAsyncWait } from '@/utils/common';
import AgentApi from '@/api/agent'

const downloadFile = (response: any) => {
  const disposition = response.headers['content-disposition']
  let fileName = disposition.substring(disposition.indexOf('filename=') + 9)
  fileName = encodeURIComponent(fileName) // iso8859-1的字符转换成中文
  fileName = fileName.replace(/\"/g, '') // 去掉双引号
  const blob = new Blob([response.data])
  const createObjectURL = (object: any) => (window.URL) ? window.URL.createObjectURL(object)
    : (window as any).webkitURL.createObjectURL(object)
  if (window.navigator && (window.navigator as any).msSaveOrOpenBlob) {
    (navigator as any).msSaveBlob(blob, fileName)
  } else {
    const a = document.createElement('a')
    const url = createObjectURL(blob)
    a.style.display = 'none'
    a.href = url
    a.download = fileName
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
  }
}

@Component
export default class RunLog extends Vue {
  public $refs!: {
    logPreview: HTMLDivElement
  }

  private host: string = ''
  private hostList: string[] = []
  private hostMapping: any = {}
  get currHostData () {
    return this.hostMapping[this.host] || {}
  }

  private latestLogLoading = false;
  get latestLogDisabled () { // 获取最新日志disabled
    return !this.host || this.latestLogLoading || !this.currHostData.isStartOrStop || this.currHostData.status === i18n.t('modules.views.configStatus.agent.s_50d4a850') as string
  }

  private logLoading = true;
  private logList: any[] = [];
  private columnConfig = [
    { field: 'logName', label: i18n.t('modules.views.configStatus.runLog.s_e23a7aab') as string, labelKey: 'modules.views.configStatus.runLog.s_e23a7aab', slot: 'column-logName', minWidth: 300 },
    { field: 'logFileCreateTime', label: i18n.t('modules.views.configStatus.runLog.s_1cb4599c') as string, labelKey: 'modules.views.configStatus.runLog.s_1cb4599c', minWidth: 150 },
    { field: 'logFileUpdateTime', label: i18n.t('modules.views.configStatus.runLog.s_9fed38ba') as string, labelKey: 'modules.views.configStatus.runLog.s_9fed38ba', minWidth: 150 },
    { field: 'logFileSize', label: i18n.t('modules.views.configStatus.runLog.s_c1531cb3') as string, labelKey: 'modules.views.configStatus.runLog.s_c1531cb3', unit: 'b', minWidth: 100 },
  ]

  private logTimer: any = null;
  private logTimerCount = 0;

  private async created () {
    this.getHostList().then(() => {
      // 宿主机回显
      const host = decodeURIComponent(this.$route.query.host as string || '')
      if (this.hostList.length) {
        if (!host || !this.hostList.find((t: any) => t === host)) {
          this.host = this.hostList[0]
        } else {
          this.host = host
        }
      } else {
        this.host = ''
      }
      if (host !== this.host) {
        this.$router.replace({ query: { ...this.$route.query, host: encodeURIComponent(this.host) } })
      }
      if (this.host) {
        this.getLogList()
      }
    })
  }

  private beforeDestroy () {
    if (this.logTimer) {
      window.clearTimeout(this.logTimer);
      this.logTimer = null;
    }
  }

  // 切换宿主机
  private hostChangeHandle (value: string) {
    const host = decodeURIComponent(this.$route.query.host as string || '')
    if (value !== host) {
      this.$router.replace({ query: { ...this.$route.query, host: encodeURIComponent(this.host) } })
    }
    this.latestLogLoading = false;
    this.logList = [];
    this.getLogList()
    if (this.logTimer) {
      window.clearTimeout(this.logTimer);
      this.logTimer = null;
    }
  }

  // 获取最新日志
  private getLatestLogHandle () {
    this.latestLogLoading = true;
    AgentApi.submitUpdate({ hosts: [this.host], operation: 4 });
    const host = this.host;
    this.logTimer = setTimeout(async() => {
      this.getLogStatus(host);
    }, 30 * 1000);
  }

  // 获取日志列表
  private async getLogList () {
    this.logLoading = true;
    const { result, error } = await toAsyncWait(AgentApi.getAgentLogLost({ host: this.host }));
    this.logLoading = false;
    if (!error) {
      this.logList = (result || {}).data || [];
    }
  }

  private async getLogStatus (host: string) {
    this.logTimerCount++;
    let hostItem: any = null;
    if (this.logTimerCount < 4) {
      const params = {
        pageNum: 1,
        pageSize: 9999,
        query: host,
      }
      const { result, error } = await toAsyncWait(AgentApi.getList(params));
      hostItem = (result?.data?.list || []).find((t: any) => t.hostName === this.host) || null;
    }
    if (this.logTimerCount >= 4 || (hostItem && (hostItem.operation !== 4 || [1, 2].includes(hostItem.operationStatus)))) {
      window.clearTimeout(this.logTimer);
      this.logTimer = null;
      this.logTimerCount = 0;
      this.getLogList();
      this.latestLogLoading = false;
    }
  }

  // 下载日志
  private async downloadLogHandle (row: any) {
    const params = {
      id: row.id,
      type: 2,
    }
    this.$set(row, 'downloading', true);
    try {
      const response = await AgentApi.loadAgentLogContent(params);
      const text = await response.data.text();
      try {
        const data = JSON.parse(text);
        if (data?.status) {
          this.$message.error(data?.message || i18n.t('modules.views.configStatus.runLog.s_65e200d3') as string);
          return; // 提前返回，避免执行后续下载逻辑
        }
      } catch (error) {
        // JSON 解析失败时忽略，继续下载原始内容
      }

      downloadFile(response);
      if (row.isNew) {
        this.updateLogStatus(row);
      }
    } catch (err: any) {
      this.$message.error(err.message);
    } finally {
      this.$set(row, 'downloading', false);
    }
  }

  // 更新日志状态
  private async updateLogStatus (row: any) {
    const { error } = await toAsyncWait(AgentApi.updateAgentLogNew({ id: row.id }));
    if (!error) {
      this.$set(row, 'isNew', false);
    }
  }

  // 获取宿主机列表
  private async getHostList () {
    const params = {
      pageNum: 1,
      pageSize: 9999,
    }
    const { result, error } = await toAsyncWait(AgentApi.getList(params));
    if (!error) {
      const hostList: string[] = [];
      const hostMapping: any = {};
      (result?.data?.list || []).forEach((item: any) => {
        const goos = FirstLetterCapital(item.goos);
        const agentVersion = (item.agentVersion || '').match(/(\d+\.?)*/g).filter((v: string) => !!v)[0]
        const isSupport = (goos === 'Linux' || goos === 'Windows') && !item.isK8s
        const isStartOrStop = isSupport && compareVersion(agentVersion, '2.8.6') >= 0 // 是否支持启动/停止/重启
        if (isStartOrStop) {
          hostList.push(item.hostName);
          hostMapping[item.hostName] = {
            hostName: item.hostName,
            status: item.status,
            isK8s: item.isK8s,
            goos,
            isSupport,
            isStartOrStop,
          }
        }
      });
      this.hostList = hostList;
      this.hostMapping = hostMapping;
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message);
    }
  }
}
</script>

<style lang="scss" scoped>
.run-log-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-primary);
  font-size: 12px;
  overflow: auto;

  .run-log {
    flex: 1;
    height: 100%;
    padding: 20px;
    border-radius: 4px;
    background-color: var(--bg-color);
    overflow: hidden;
  }

  .run-log-list {
    height: calc(100% - 48px);
  }

  .log-new {
    display: block;
    height: 20px;
    padding: 0 5px;
    font-size: 12px;
    line-height: 20px;
    background-color: var(--color-danger);
    border-radius: 4px;
    color: #fff;
    transform: scale(0.5);
    transform-origin: top left;
    position: absolute;
    top: 4px;
    left: 6px;
  }

  .action-disabled {
    color: var(--color-text-secondary);
    pointer-events: none;
  }
}
</style>
