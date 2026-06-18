<template>
  <div class="agent-manage-wrap">
    <chart-group ref="chartGroup" />

    <div ref='manageCont' class="agent-table-list">
      <div class="search-group flex-h">
        <div class="flex-h flex-1">
          <el-input
            v-model="queryParams.query"
            :placeholder="$t('modules.views.configStatus.agent.s_108c0409')"
            @change="handleQuerySearch"
            clearable
            size="small"
            maxlength="100"
            prefix-icon="db-icon-search"
            class="search-group-ipt mr-10"
          >
          </el-input>

          <el-select
            v-model="queryParams.statuses"
            @change="handleQuerySearch"
            filterable multiple clearable size="small"
            :placeholder="$t('modules.views.configStatus.agent.s_82200fdb')"
            class="search-group-select mr-10">
            <el-option :label="$t('modules.utils.filters.s_68905cf3')" value="Online"></el-option>
            <el-option :label="$t('modules.components.db-table.s_c195df63')" value="Anomaly"></el-option>
            <el-option :label="$t('modules.views.configStatus.agent.s_50d4a850')" value="Offline"></el-option>
          </el-select>
        </div>
        <div class="nosel flex-none">
          <el-button
            @click="batchTogglePreloadHandle(true)"
            :disabled="!preloadSelection.length || batchLoading"
            :type="preloadSelection.length ? 'primary' : ''"
            plain size="small">{{ $t('modules.views.configStatus.agent.s_2122e7b6') }}</el-button>
          <el-button
            @click="batchTogglePreloadHandle(false)"
            :disabled="!preloadSelection.length || batchLoading"
            :type="preloadSelection.length ? 'primary' : ''"
            plain size="small">{{ $t('modules.views.configStatus.agent.s_01d2f876') }}</el-button>
          <el-button
            @click="batchToggleStatusHandle(startSelection, 3)"
            :disabled="!startSelection.length || batchLoading"
            :type="startSelection.length ? 'primary' : ''"
            plain size="small">{{ $t('modules.views.configStatus.agent.s_93e89432') }}</el-button>
          <el-button
            @click="batchToggleStatusHandle(stopSelection, 2)"
            :disabled="!stopSelection.length || batchLoading"
            :type="stopSelection.length ? 'primary' : ''"
            plain size="small">{{ $t('modules.views.configStatus.agent.s_9ad2a83b') }}</el-button>
          <el-button
            @click="batchToggleStatusHandle(restartSelection, 1)"
            :disabled="!restartSelection.length || batchLoading"
            :type="restartSelection.length ? 'primary' : ''"
            plain size="small">{{ $t('modules.views.configStatus.agent.s_73651ba2') }}</el-button>
          <el-button
            @click="showUpdateDialogHandle"
            :disabled="!updateSelection.length || batchLoading"
            :type="updateSelection.length ? 'primary' : ''"
            plain size="small">{{ $t('modules.views.configStatus.agent.s_463e2bed') }}</el-button>
          <el-button
            @click="showConfigDialogHandle"
            :disabled="!configSelection.length || batchLoading"
            :type="configSelection.length ? 'primary' : ''"
            plain size="small">{{ $t('modules.views.configStatus.agent.s_47e6385c') }}</el-button>
          <el-button @click="viewPackagesHandle" type="primary" plain size="small">{{ $t('modules.views.configStatus.agent.s_4e402479') }}</el-button>
          <el-button @click="jumpAgentInstallHandle" type="primary" size="small" icon="el-icon-plus">{{ $t('modules.views.configStatus.agent.s_28436e1d') }}</el-button>
        </div>
      </div>

      <db-table
        :queryApi='queryApi'
        :queryParams='queryParams'
        :timeMode="false"
        :autoRefresh="false"
        :columnConfig='columnConfig'
        :showSelection='true'
        :selectableFunc='getSelectableHandle'
        @selection-change="selectChangeHandle"
        @on-table-inited='tableRefresh'
        @on-fetch-end='tableFetchEndHandle'
        @sort-change='tableRefresh'
        :formatFunc='formatFunc'
        showSetting
        tableKey='CONFIG_STATUS_AGENT_LIST'
        ref='listTable'
        class="agent-table">
        <template slot="column.status" slot-scope="{ row }">
          <i class="status-tag"
            :class="{
              'status-tag-success': row.status === '在线',
              'status-tag-error': row.status === '异常',
              'status-tag-default': row.status === '离线',
            }"></i>{{ row.status }}
          <!-- 等待执行 -->
          <template v-if="row.operationStatus === 0">{{ $t('modules.views.configStatus.agent.s_f2fd87fe', { value0: row.operation | operationFilter }) }}</template>
          <!-- 正在执行 -->
          <template v-if="row.operationStatus === 3">{{ $t('modules.views.configStatus.agent.s_71561141', { value0: row.operation | operationFilter }) }}</template>
          <!-- 成功 -->
          <el-popover
            v-if="row.operationStatus === 1"
            trigger="hover" placement="top"
            popper-class="agent-list-popover">
            <span slot="reference" class="font-12 ell">
              <template v-if="row.operation === 2">{{ $t('modules.views.configStatus.agent.s_da0e1a7f', { value0: row.operation | operationFilter }) }}</template>
              <template v-else>{{ $t('modules.views.configStatus.agent.s_d9eec2ab', { value0: row.operation | operationFilter }) }}</template>
            </span>
            <div class="font-12 ell">{{ $t('modules.views.configStatus.agent.s_6f9f29e4', { value0: row.operation | operationFilter, value1: row.operationEndTime || '-' }) }}</div>
          </el-popover>
          <!-- 失败 -->
          <template v-if="row.operationStatus === 2">（
            <el-popover
              @hide="readAnomalyHandle(row)"
              trigger="click" placement="top"
              popper-class="agent-list-popover">
              <a slot="reference" class="blue">{{ $t('modules.views.configStatus.agent.s_ebe0264d', { value0: row.operation | operationFilter }) }}</a>
              <div class="font-12 ell">{{ $t('modules.views.configStatus.agent.s_3530d45c', { value0: row.operationEndTime || '-' }) }}</div>
              <div class="font-12 mt-5 ell-3">{{ $t('modules.views.configStatus.agent.s_a9ea2223', { value0: row.operationMsg || '-' }) }}</div>
            </el-popover>
          ）</template>
        </template>

        <template slot="column.preload" slot-scope="{ row }">
          <el-popover
            :disabled="!(row.status === $t('modules.views.configStatus.agent.s_50d4a850') || !row.preloadAble)"
            trigger="hover" placement="top">
            <el-switch
              slot="reference"
              v-model="row.preload"
              :disabled="row.loading || row.status === $t('modules.views.configStatus.agent.s_50d4a850') || !row.preloadAble"
              @change="togglePreloadHandle(row)">
            </el-switch>
            <div v-if="!row.isSupport || row.goos === 'Windows'" class="font-12">{{ $t('modules.views.configStatus.agent.s_fff7750f', { value0: row.isK8s ? 'Kubernetes' : row.goos }) }}</div>
            <div v-else-if="!row.preloadAble" class="font-12">{{ $t('modules.views.configStatus.agent.s_9807da2a') }}</div>
            <div v-else-if="row.status === '离线'" class="font-12">{{ $t('modules.views.configStatus.agent.s_466688db') }}</div>
          </el-popover>
        </template>

        <template slot="column.agentVersion" slot-scope="{ row }">
          <span>{{ row.agentVersion || '-' }}</span>
          <el-popover
            v-if="!row.isSupport || row.isLatest || row.isTooLow || row.status === '离线'"
            trigger="hover" placement="top">
            <span slot='reference' class='el-icon el-icon-info ml-5 describe' />
            <div v-if="!row.isSupport" class="font-12">{{ $t('modules.views.configStatus.agent.s_f3ab4af5', { value0: row.isK8s ? 'Kubernetes' : row.goos }) }}</div>
            <div v-else-if="row.isLatest" class="font-12">{{ $t('modules.views.configStatus.agent.s_fcad2e0c') }}</div>
            <div v-else-if="row.isTooLow" class="font-12">{{ $t('modules.views.configStatus.agent.s_952f8bc7') }}</div>
            <div v-else-if="row.status === '离线'" class="font-12">{{ $t('modules.views.configStatus.agent.s_82f3a26f') }}</div>
          </el-popover>
          <el-popover
            v-else-if='row.newVersion && row.operationStatus !== 3'
            trigger="hover" placement="top">
            <span class="db-icon-upload ml-5 green font12" slot='reference'></span>
            <div class="font-12">{{ $t('modules.views.configStatus.agent.s_3c8df435') }}<span @click="updateAgentHandle(row)" class="blue cp">{{ $t('modules.views.configStatus.agent.s_6b4a7fd3') }}</span></div>
          </el-popover>
        </template>

        <template slot="column.lastSynAgentTime" slot-scope="{ row }">
          <template v-if="row.status !== '离线'">
            <span :class="[ row.timeDiff && Math.abs(row.timeDiff) > 30 ? 'red' : '' ]">{{ row.lastSynAgentTime || '-' }}</span>
            <el-popover v-if="row.timeDiff && Math.abs(row.timeDiff) > 30"
              trigger='hover' placement="top"
              popper-class="agent-list-popover">
              <span slot='reference' class='el-icon-warning ml-5 red' />
              <div class="font-12">{{ $t('modules.views.configStatus.agent.s_f0a271ba', { value0: productNameEn }) }}</div>
              <div class="font-12 mt-5">{{ $t('modules.views.configStatus.agent.s_0e25dff6', { value0: productNameEn }) }}</div>
              <div class="font-12 mt-5">{{ $t('modules.views.configStatus.agent.s_a0d9b4e9') }}{{ $t('modules.views.configStatus.agent.s_f7baaa4d') }}</div>
            </el-popover>
          </template>
          <template v-else>-</template>
        </template>

        <el-table-column slot="suffix" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" :width="240">
          <template slot-scope="{ row }">
            <div v-if="row.operationStatus === 3" class="font-12 describe cn">{{ $t('modules.views.configStatus.agent.s_f1dd4e9c', { value0: row.operation | operationFilter }) }}</div>

            <template v-else-if="row.status !== '离线'">
              <a v-if='row.isStartOrStop'
                @click="row.loading ? null : toggleAgentStatusHandle(row, 2)"
                :class="{ 'btn-disabled': row.loading }"
                class="blue mr-10">{{ $t('modules.views.configInstall.agent.s_095e938e') }}</a>
              <el-popover v-else trigger="hover" placement="top" :disabled='row.loading' class="mr-10">
                <span slot='reference' class="describe cn">{{ $t('modules.views.configInstall.agent.s_095e938e') }}</span>
                <div v-if="!row.isSupport" class="font-12">{{ $t('modules.views.configStatus.agent.s_ce453127', { value0: row.isK8s ? 'Kubernetes' : row.goos }) }}</div>
                <div v-else class="font-12">{{ $t('modules.views.configStatus.agent.s_aa90a986') }}</div>
              </el-popover>
            
              <a v-if='row.isStartOrStop'
                @click="row.loading ? null : toggleAgentStatusHandle(row, 1)"
                :class="{ 'btn-disabled': row.loading }"
                class="blue mr-10">{{ $t('modules.views.configStatus.agent.s_01b4e06f') }}</a>
              <el-popover v-else trigger="hover" placement="top" :disabled='row.loading' class="mr-10">
                <span slot='reference' class="describe cn">{{ $t('modules.views.configStatus.agent.s_01b4e06f') }}</span>
                <div v-if="!row.isSupport" class="font-12">{{ $t('modules.views.configStatus.agent.s_e635c1bb', { value0: row.isK8s ? 'Kubernetes' : row.goos }) }}</div>
                <div v-else class="font-12">{{ $t('modules.views.configStatus.agent.s_26572b16') }}</div>
              </el-popover>

              <a v-if='row.newVersion'
                @click="row.loading ? null : updateAgentHandle(row)"
                :class="{ 'btn-disabled': row.loading }"
                class="blue mr-10">{{ $t('modules.utils.static.s_32ac152b') }}</a>
              <el-popover v-else trigger="hover" placement="top" :disabled='row.loading' class="mr-10">
                <span slot='reference' class="describe cn">{{ $t('modules.utils.static.s_32ac152b') }}</span>
                <div v-if="!row.isSupport" class="font-12">{{ $t('modules.views.configStatus.agent.s_f3ab4af5', { value0: row.isK8s ? 'Kubernetes' : row.goos }) }}</div>
                <div v-else-if="row.isLatest" class="font-12">{{ $t('modules.views.configStatus.agent.s_fcad2e0c') }}</div>
                <div v-else-if="row.isTooLow" class="font-12">{{ $t('modules.views.configStatus.agent.s_952f8bc7') }}</div>
              </el-popover>

              <a v-if='(row.newVersion || row.isLatest)'
                @click="row.loading ? null : updateConfigHandle(row)"
                :class="{ 'btn-disabled': row.loading }"
                class="blue mr-10">{{ $t('modules.views.configStatus.agent.s_2e4b9b00') }}</a>
              <el-popover v-else trigger="hover" placement="top" :disabled='row.loading' class="mr-10">
                <span slot='reference' class="describe cn">{{ $t('modules.views.configStatus.agent.s_2e4b9b00') }}</span>
                <div v-if="!row.isSupport" class="font-12">{{ $t('modules.views.configStatus.agent.s_8ddca4bc', { value0: row.isK8s ? 'Kubernetes' : row.goos }) }}</div>
                <div v-else-if="row.isTooLow" class="font-12">{{ $t('modules.views.configStatus.agent.s_304b4e5a') }}</div>
              </el-popover>
            </template>

            <template v-else>
              <a v-if='row.isStartOrStop'
                @click="row.loading ? null : toggleAgentStatusHandle(row, 3)"
                :class="{ 'btn-disabled': row.loading }"
                class="blue mr-10">{{ $t('modules.views.configInstall.agent.s_8e54ddfe') }}</a>
              <el-popover v-else trigger="hover" placement="top" :disabled='row.loading' class="mr-10">
                <span slot='reference' class="describe cn">{{ $t('modules.views.configInstall.agent.s_8e54ddfe') }}</span>
                <div v-if="!row.isSupport" class="font-12">{{ $t('modules.views.configStatus.agent.s_3ddbeceb', { value0: row.isK8s ? 'Kubernetes' : row.goos }) }}</div>
                <div v-else class="font-12">{{ $t('modules.views.configStatus.agent.s_3ee6e6fa') }}</div>
              </el-popover>
            </template>

            <template v-if="row.operationStatus !== 3">
              <a v-if='row.isStartOrStop'
                @click="viewRunLogHandle(row)"
                :class="{ 'btn-disabled': row.loading }"
                class="blue">{{ $t('modules.views.configStatus.agent.s_284a98fa') }}</a>
              <el-popover v-else trigger="hover" placement="top" :disabled='row.loading'>
                <span slot='reference' class="describe cn">{{ $t('modules.views.configStatus.agent.s_284a98fa') }}</span>
                <div v-if="!row.isSupport" class="font-12">{{ $t('modules.views.configStatus.agent.s_973a44b7', { value0: row.isK8s ? 'Kubernetes' : row.goos }) }}</div>
                <div v-else class="font-12">{{ $t('modules.views.configStatus.agent.s_e2f13728') }}</div>
              </el-popover>
            </template>
          </template>
        </el-table-column>
      </db-table>
    </div>

    <config-dialog :showModel='showConfigModel'
      :hostList='configSelection'
      @on-close='dialogCancelHandle' @on-post='showConfigModel = false' />

    <update-dialog :showModel="showUpdateDialog"
      :hostList='updateSelection'
      @on-success-some='filterSomeUpdateList'
      @on-close='updateDialogCancelHandle' @on-post='showUpdateDialog = false'></update-dialog>

  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs'
import { compareVersion } from '@/utils/compareVersion'
import { FirstLetterCapital } from '@/utils/filters/common';
import { toAsyncWait } from '@/utils/common';
import SystemApi from '@/api/system'
import AgentApi from '@/api/agent'
import ConfigDialog from './config-dialog.vue';
import UpdateDialog from './update-dialog.vue';
import ChartGroup from './chart-group.vue';

@Component({
  components: {
    ChartGroup,
    ConfigDialog,
    UpdateDialog,
  },
  filters: {
    operationFilter (type: number) {
      const operations = [i18n.t('modules.utils.static.s_32ac152b') as string, i18n.t('modules.views.configStatus.agent.s_01b4e06f') as string, i18n.t('modules.views.configStatus.agent.s_095e938e') as string, i18n.t('modules.views.configStatus.agent.s_8e54ddfe') as string, i18n.t('modules.views.configStatus.agent.s_64dedcdc') as string];
      return operations[type] || type;
    }
  }
})
export default class AgentManage extends Vue {
  public $refs!: {
    chartGroup: ChartGroup
    listTable: any
  }

  get productNameEn () {
    return this.$store.getters['User/getLogoConfig']?.productNameEn || '';
  }

  private columnConfig = [
    { field: 'hostName', label: i18n.t('modules.views.configStatus.agent.s_3117f05e') as string, labelKey: 'modules.views.configStatus.agent.s_3117f05e', minWidth: 150, defaultShow: true, },
    { field: '_hostIp', label: i18n.t('modules.views.configStatus.agent.s_ec96881d') as string, labelKey: 'modules.views.configStatus.agent.s_ec96881d', minWidth: 200, defaultShow: true, },
    { field: 'status', label: i18n.t('modules.views.configStatus.agent.s_6bf1f392') as string, labelKey: 'modules.views.configStatus.agent.s_6bf1f392', slot: 'column.status', showOverflowTooltip: false, minWidth: 180, defaultShow: true, },
    { field: 'preload', label: i18n.t('modules.views.configStatus.agent.s_2e66512d') as string, labelKey: 'modules.views.configStatus.agent.s_2e66512d', slot: 'column.preload', minWidth: 100, defaultShow: true, },
    { field: 'agentVersion', label: i18n.t('modules.views.configStatus.agent.s_a33ea80c') as string, labelKey: 'modules.views.configStatus.agent.s_a33ea80c', slot: 'column.agentVersion', minWidth: 100, defaultShow: true, },
    { field: 'lastSynAgentTime', label: i18n.t('modules.views.configStatus.agent.s_af92c30d') as string, labelKey: 'modules.views.configStatus.agent.s_af92c30d', slot: 'column.lastSynAgentTime', minWidth: 160, defaultShow: true, },
    { field: 'goos', label: i18n.t('modules.views.configStatus.agent.s_215874af') as string, labelKey: 'modules.views.configStatus.agent.s_215874af', minWidth: 100, defaultShow: true, },
    { field: 'cpu', label: 'CPU(%)', unit: 'percent', minWidth: 100, defaultShow: true, },
    { field: 'memory', label: i18n.t('modules.views.appMonitor.hotMethods.s_9932551c') as string, labelKey: 'modules.views.appMonitor.hotMethods.s_9932551c', minWidth: 100, defaultShow: true, },
  ]
  private queryApi = AgentApi.getList;

  private queryParams: any = {
    query: '',
    statuses: [],
  };

  private tableSource: any[] = []; // 列表数据，仅用于更新宿主机时间

  private selection: any[] = [] // 全部已选中

  private startSelection: any[] = [] // 已选中，可批量启动的
  private stopSelection: any[] = [] // 已选中，可批量停止的
  private restartSelection: any[] = [] // 已选中，可批量重启的
  private updateSelection: any[] = [] // 已选中，可批量更新的
  private configSelection: any[] = [] // 已选中，可批量修改配置的
  private preloadSelection: any[] = [] // 已选中，可批量开启/关闭自动注入的

  private batchLoading = false;

  private showConfigModel = false;
  private showUpdateDialog = false;

  private autoIncreaseTimer: any = null;

  private created() {
    const { query = '', statuses = '' } = this.$route.query;
    this.queryParams.query = decodeURIComponent(query as string)
    if (statuses) {
      const _statuses = (Array.isArray(statuses) ? statuses : [statuses]).filter(t => !!t);
      this.queryParams.statuses = _statuses.map(t => decodeURIComponent(t as string));
    }
  }

  private mounted () {
    this.$refs.chartGroup && this.$refs.chartGroup.getData()
  }

  private beforeDestroy () {
    if (this.autoIncreaseTimer) {
      window.clearTimeout(this.autoIncreaseTimer)
      this.autoIncreaseTimer = null
    }
  }

  // 搜索
  private handleQuerySearch() {
    // 设置路由中的参数
    const _query: any = {
      ...this.$route.query,
      query: encodeURIComponent(this.queryParams.query || ''),
      statuses: this.queryParams.statuses.map((t: string) => encodeURIComponent(t)),
    }
    if (!_query.query) {
      delete _query.query
    }
    if (!_query.status) {
      delete _query.status
    }
    this.$router.replace({ query: { ..._query } })
    this.tableRefresh()
  }

  // 表格刷新
  private tableRefresh () {
    (this.$refs.listTable as any)?.refresh()
  }

  // 表格数据格式化
  private async formatFunc (data: any[]) {
    const result: any = await SystemApi.getsysdate()
    const globalServiceTime = (result || {}).data || '';

    data.forEach((item: any) => {
      const _newTime = dayjs(new Date(globalServiceTime || item.lastSynAgentTime).valueOf() - (item.timeDiff * 1000 || 0)).format('YYYY-MM-DD HH:mm:ss')
      const manageIp = item.managerIpaddress;
      item.lastSynAgentTime = _newTime;
      item._hostIp = manageIp && manageIp !== item.hostIp ? `${manageIp} (${item.hostIp || '-'})` : item.hostIp || '-';
      item.cpu = item.cpu ? Number(String(item.cpu).replace('%', '')) / 100 : '-';
      item.preload = item.preload === true;
      item.goos = FirstLetterCapital(item.goos);
      if (item.operationStatus === 1 && +new Date(_newTime) - +new Date(item.operationEndTime) > 12 * 60 * 60 * 1000) {
        // 操作成功且超过12小时，隐藏操作记录
        item.operationStatus = null
      }
      if (typeof item.operation !== 'number') {
        item.operationStatus = null
      }

      // 版本号正则匹配
      item.agentVersion = (item.agentVersion || '').match(/(\d+\.?)*/g).filter((v: string) => !!v)[0]
      item.lastVersion = (item.lastVersion || '').match(/(\d+\.?)*/g).filter((v: string) => !!v)[0]
      const isSupport = (item.goos === 'Linux' || item.goos === 'Windows') && !item.isK8s
      const isTooLow = compareVersion(item.agentVersion, '2.5.5') < 0
      const version = compareVersion(item.lastVersion, item.agentVersion)
      item.isSupport = isSupport // 系统环境是否支持升级
      item.isLatest = isSupport && version <= 0 // 是否已经是最新版本
      item.isTooLow = isSupport && isTooLow // 版本过低
      item.newVersion = isSupport && !isTooLow && version > 0 // 是否有新版本
      item.isStartOrStop = isSupport && compareVersion(item.agentVersion, '2.8.6') >= 0 // 是否支持启动/停止/重启
      // 是否支持开启/关闭自动注入，Windows暂不支持
      item.preloadAble = isSupport && compareVersion(item.agentVersion, '2.9.2') >= 0 && item.goos !== 'Windows';
    })
  }
  // 表格请求完成回调
  private tableFetchEndHandle (list: any[]) {
    this.tableSource = list;
    // 设置/清除 宿主机时间定时器
    if (list && list.length) {
      this.loopIncrease()
    } else if (this.autoIncreaseTimer) {
      window.clearTimeout(this.autoIncreaseTimer)
      this.autoIncreaseTimer = null
    }
  }
  // 宿主机时间定时器
  private loopIncrease () {
    if (this.autoIncreaseTimer) {
      window.clearTimeout(this.autoIncreaseTimer)
      this.autoIncreaseTimer = null
    }
    this.autoIncreaseTimer = setTimeout(() => {
      this.tableSource.forEach(row => {
        row.lastSynAgentTime = dayjs(new Date(row.lastSynAgentTime).valueOf() + 1000).format('YYYY-MM-DD HH:mm:ss')
      })
      this.loopIncrease()
    }, 1000)
  }
  // 表格行是否可选判断
  private getSelectableHandle (row: any) {
    return row.operationStatus !== 3
  }

  // 选中 change
  private selectChangeHandle (selection: any) {
    this.selection = [...selection]
    this.startSelection = this.selection.filter(t => t.status === i18n.t('modules.views.configStatus.agent.s_50d4a850') as string && t.isStartOrStop);
    this.stopSelection = this.selection.filter(t => t.status !== i18n.t('modules.views.configStatus.agent.s_50d4a850') as string && t.isStartOrStop);
    this.restartSelection = [...this.stopSelection];
    this.configSelection = this.selection.filter(t => t.status !== i18n.t('modules.views.configStatus.agent.s_50d4a850') as string && (t.newVersion || t.isLatest));
    this.updateSelection = this.selection.filter(t => t.status !== i18n.t('modules.views.configStatus.agent.s_50d4a850') as string && t.newVersion);
    this.preloadSelection = this.selection.filter(t => t.status !== i18n.t('modules.views.configStatus.agent.s_50d4a850') as string && t.preloadAble);
  }

  // 启动/停止/重启
  private async toggleAgentStatusHandle (row: any, operation: number) {
    const params = {
      hosts: [row.hostName],
      operation,
    }
    this.$set(row, 'loading', true);
    const { error } = await toAsyncWait(AgentApi.submitUpdate(params));
    this.$set(row, 'loading', false);
    if (!error) {
      this.$message.success(i18n.t('modules.views.configManage.entity.s_33130f5c') as string);
      this.$set(row, 'operation', operation);
      this.$set(row, 'operationStatus', 0);
      if (row.status === i18n.t('modules.views.appMonitor.service.s_c195df63') as string) {
        this.$set(row, 'status', i18n.t('modules.views.configStatus.agent.s_68905cf3') as string);
      }
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message);
    }
  }
  // 批量 启动/停止/重启
  private async batchToggleStatusHandle (list: any[], operation: number) {
    const params = {
      hosts: list.map(t => t.hostName),
      operation,
    }
    this.batchLoading = true;
    list.forEach(row => {
      this.$set(row, 'loading', true);
    });
    const { error } = await toAsyncWait(AgentApi.submitUpdate(params));
    this.batchLoading = false;
    list.forEach(row => {
      this.$set(row, 'loading', false);
    });
    if (!error) {
      this.$message.success(i18n.t('modules.views.configManage.entity.s_33130f5c') as string);
      this.handleQuerySearch();
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message);
    }
  }

  // 更新
  private updateAgentHandle (row: any) {
    this.updateSelection = [{ ...row }]
    this.showUpdateDialog = true
  }
  // 批量更新
  private showUpdateDialogHandle () {
    if (!this.selection.length) {
      return
    }
    this.updateSelection = this.selection.filter(t => t.status !== i18n.t('modules.views.configStatus.agent.s_50d4a850') as string && t.newVersion);
    this.showUpdateDialog = true
  }
  // 关闭更新弹框
  private updateDialogCancelHandle (payload: any) {
    this.showUpdateDialog = false
    if (payload && payload.refresh) {
      this.handleQuerySearch()
    } else {
      this.updateSelection = this.selection.filter(t => t.status !== i18n.t('modules.views.configStatus.agent.s_50d4a850') as string && t.newVersion);
    }
  }
  // 部分更新成功回调，重试，需过滤更新成功的
  private filterSomeUpdateList (succ: any[]) {
    const succHost = succ.map((item: any) => item.host)
    this.updateSelection = this.updateSelection.filter((item: any) => succHost.indexOf(item.hostName) === -1)
  }

  // 修改配置
  private updateConfigHandle (row: any) {
    this.configSelection = [{ ...row }]
    this.showConfigModel = true
  }
  // 批量修改配置
  private showConfigDialogHandle () {
    if (!this.selection.length) {
      return
    }
    this.configSelection = this.selection.filter(t => t.status !== i18n.t('modules.views.configStatus.agent.s_50d4a850') as string && (t.newVersion || t.isLatest));
    this.showConfigModel = true
  }
  // 关闭修改配置弹框
  private dialogCancelHandle (payload: any) {
    this.showConfigModel = false
    if (payload && payload.refresh) {
      this.handleQuerySearch()
    } else {
      this.configSelection = this.selection.filter(t => t.status !== i18n.t('modules.views.configStatus.agent.s_50d4a850') as string && (t.newVersion || t.isLatest));
    }
  }

  // 开启/关闭自动注入
  private async togglePreloadHandle (row: any) {
    if (row.loading) {
      return;
    }
    const params = {
      agents: [row.hostName],
      preload: !!row.preload,
    }
    this.$set(row, 'loading', true);
    const { error } = await toAsyncWait(AgentApi.updatePreload(params));
    this.$set(row, 'loading', false);
    if (!error) {
      this.$message.success(i18n.t('modules.views.configManage.entity.s_33130f5c') as string);
      this.$set(row, 'preload', params.preload);
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message);
    }
  }
  // 批量开启/关闭自动注入
  public async batchTogglePreloadHandle (preload: boolean) {
    const params = {
      agents: this.preloadSelection.map(t => t.hostName),
      preload,
    }
    this.batchLoading = true;
    this.preloadSelection.forEach(row => {
      this.$set(row, 'loading', true);
    });
    const { error } = await toAsyncWait(AgentApi.updatePreload(params));
    this.batchLoading = false;
    this.preloadSelection.forEach(row => {
      this.$set(row, 'loading', false);
    });
    if (!error) {
      this.$message.success(i18n.t('modules.views.configManage.entity.s_33130f5c') as string);
      this.preloadSelection.forEach(row => {
        this.$set(row, 'preload', params.preload);
      });
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message);
    }
  }

  // 已读异常信息
  private async readAnomalyHandle (row: any) {
    if (row.isRead) {
      return;
    }
    const { error } = await toAsyncWait(AgentApi.readUpdateMsg({ id: row.id }));
    if (!error) {
      this.$set(row, 'isRead', true);
    }
  }

  // 跳转到 安装Agent
  private jumpAgentInstallHandle() {
    this.$router.push({
      path: '/deploy/access',
      query: { type: 'oneAgent' }
    })
  }
  // 跳转到 更新包管理
  private viewPackagesHandle () {
    this.$router.push({
      path: '/config/agentPackages',
    })
  }
  // 跳转到 运行日志
  private viewRunLogHandle (row: any) {
    this.$router.push({
      path: '/config/runLog',
      query: { host: encodeURIComponent(row.hostName) }
    })
  }
}
</script>

<style lang="scss" scoped>
.agent-manage-wrap {
  flex: 1;
  padding: 0 20px 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-primary);
  overflow: auto;

  .agent-table-list {
    flex: 1;
    min-height: 512px;
    .agent-table {
      height: calc(100% - 32px);
    }
  }

  .search-group {
    justify-content: space-between;
    .search-group-ipt {
      flex: 1;
      max-width: 260px;
    }
    .search-group-select {
      flex: 1;
      max-width: 220px;
    }
  }
}

.btn-disabled {
  color: var(--color-text-secondary);
  cursor: not-allowed;
}
</style>

<style lang="scss">
.agent-list-popover {
  padding: 12px 16px;
  max-width: 400px;
}
</style>
