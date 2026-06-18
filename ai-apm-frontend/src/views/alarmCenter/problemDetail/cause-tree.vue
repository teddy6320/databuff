<template>
  <div class="topo-chart-wrap">
    <div class="topo-chart" :id="chartId"></div>

    <el-input
      v-show="chartSource.nodes.length"
      v-model="nodeQuery"
      @change="nodeQueryChangeHandle"
      clearable
      size="small"
      maxlength="100"
      prefix-icon="db-icon-search"
      :placeholder="$t('modules.views.alarmCenter.problemDetail.s_898c3624')"
      class="node-query-input"
    />

    <aside-detail
      :detail="currentNode"
      :suggest="suggest"
      :suggestStatus="suggestStatus"
      :showFeedback="showFeedback"
      :feedbackStatus="feedbackStatus"
      :feedbackMessage="feedbackMessage"
      @retry-suggest="retrySuggest"
      @feedback="feedbackHandle"
      class="aside-detail" />

    <div v-show="!chartSource.nodes.length" class="empty-show describe">
      <img src="/src/assets/img/404/no-root-cause.svg" class="empty-svg" />
      <span class="empty-text">{{ $t('modules.views.alarmCenter.problemDetail.s_8429b524') }}</span>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { State } from 'vuex-class';
import { v4 as uuidv4 } from 'uuid';
import { debounce, EventBus } from '@/utils/common'
import TopoChart, { TopoSource } from './analysisChart/index';
import AsideDetail from './aside-detail.vue';

let chartInstance: TopoChart|null = null;

@Component({
  components: {
    AsideDetail,
  },
})
export default class CauseTreeChart extends Vue {
  @State('theme') private theme!: 'dark' | 'light';
  @State('themeChanged') private themeChanged!: boolean;

  @Prop({ default: () => [] }) private source!: any[]
  @Prop({ default: 'surface' }) private chartType!: 'surface' | 'cause'
  @Prop({ default: false }) private showFeedback!: boolean
  @Prop({ default: '' }) private feedbackStatus!: string
  @Prop({ default: '' }) private feedbackMessage!: string
  @Prop({ default: true }) private showAlarm!: boolean
  @Prop({ default: '' }) private suggest!: string;
  @Prop({ default: -1 }) private suggestStatus!: number;

  get serviceNameIdMapping () {
    const mapping: any = {}
    const basicServiceMap = this.$store.getters['Service/basicServiceMap']
    Object.values(basicServiceMap).forEach((item: any) => {
      mapping[item.name] = item.id
    });
    return mapping
  }

  @Watch('themeChanged')
  private onThemeChanged(value: boolean) {
    if (value) {
      this.chartUpdate()
    }
  }

  private chartId = 'influenceSurfaceChart'
  private chartSource: TopoSource = { nodes: [], edges: [] }

  private nodeQuery = ''

  private resizeHandler: any = null;

  private currentNode: any = {};
  private nodeMapping: any = {}; // 根因对应的详情map

  private mounted () {
    this.resizeHandler = debounce(() => {
      this.resize()
    }, 100)
    window.addEventListener('resize', this.resizeHandler);

    EventBus.$on('root-cause-node-click', (data: any) => {
      if (!data) {
        return;
      }
      this.currentNode = { ...this.nodeMapping[data?.id] };
    });
  }

  private beforeDestroy () {
    // 清空拓扑参数
    chartInstance?.destroy();
    chartInstance = null;
    window.removeEventListener('resize', this.resizeHandler);
    EventBus.$off('root-cause-node-click');
  }

  public getData () {
    this.currentNode = {};
    this.nodeQuery = '';
    const { nodes, edges, nodeMapping, choosedId } = this.formatChartData(this.source || []);
    this.chartSource = { nodes, edges };
    this.nodeMapping = nodeMapping;
    this.currentNode = { ...nodeMapping[choosedId] };
    this.chartUpdate();
  }

  private formatChartData (data: any[]) {
    const nodeMapping: any = {};
    const nodes: any[] = [];
    const edges: any[] = [];
    let choosedId: string = '';
    const formatTreeData = (tree: any[], pid = '') => {
      tree.forEach((item: any) => {
        const nodeId = uuidv4();
        const rootCases: any[] = [];
        const rootCaseList: any[] = [];
        const roots = item.roots || []
        roots.forEach((t: any) => {
          const rId = uuidv4();
          let caseTypeDesc = t.caseTypeDesc;
          if (caseTypeDesc.includes('问题') && !caseTypeDesc.includes('出现')) {
            caseTypeDesc = caseTypeDesc.replace('问题', i18n.t('modules.views.alarmCenter.problemDetail.s_4616bdb5') as string);
          }
          rootCases.push({ id: rId, name: caseTypeDesc });
          rootCaseList.push({
            id: rId,
            name: caseTypeDesc,
            url: t.url || '',
            columns: (t.columns || []).map((c: any, d: number) => ({
              label: (t.columnsDesc || [])[d] || c,
              value: c,
            })),
            list: t.roots || [],
          });
        })
        const { abnormalFirstIndex, abnormalLastIndex } = item.abnormalDetail || {};
        const start = item.startTime + (abnormalFirstIndex || 0) * 60 * 1000;
        let end = item.endTime;
        if (item.abnormalDetail) {
          end = item.startTime + ((abnormalLastIndex || 0) + 1) * 60 * 1000;
        }
        const _node = {
          id: nodeId,
          serviceName: item.service,
          serviceId: item.serviceId || this.serviceNameIdMapping[item.service],
          serviceIcon: item.serviceIconType,
          serviceUrl: item.url || '',
          rootCases: rootCases.slice(0, 3), // 故障树节点只显示前3个根因
          rootCaseText: rootCaseList[0]?.name || '',
          serviceType: item.serviceType,
          affectedApp: item.affectedApp || null,
          start,
          end,
          alarmCount: this.showAlarm ? item.alarm || 0 : 0,
          isRoot: !pid, // 是否为根节点
          choosed: !pid && !choosedId,
        }
        if (_node.choosed) {
          choosedId = nodeId;
        }
        nodeMapping[nodeId] = { ..._node, rootCaseList };
        nodes.push(_node);
        if (pid) {
          if (this.chartType === 'surface') {
            edges.push({ source: pid, target: nodeId, label: i18n.t('modules.views.alarmCenter.problemDetail.s_28b42306') as string, labelKey: 'modules.views.alarmCenter.problemDetail.s_28b42306', })
          } else {
            edges.push({ source: nodeId, target: pid })
          }
        }
        if (Array.isArray(item.children) && item.children.length) {
          formatTreeData(item.children, nodeId);
        }
      })
    }
    formatTreeData(data);

    return { nodes, edges, nodeMapping, choosedId };
  }

  private chartUpdate () {
    const container = document.getElementById(this.chartId);
    if (!container) {
      return
    }
    const { width, height } = container!.getBoundingClientRect();
    if (!chartInstance) {
      chartInstance = new TopoChart({
        domId: this.chartId,
        source: this.chartSource,
        width,
        height,
        layoutRankdir: this.chartType === 'surface' ? 'BT' : 'TB', // TB从上至下布局 BT从下至上布局
        theme: this.theme,
      })
    } else {
      chartInstance.updateChart(this.chartSource, this.theme, width, height);
    }
  }

  public resize () {
    this.$nextTick(() => {
      const container = document.getElementById(this.chartId);
      if (!chartInstance || !container || !container.clientWidth || !container.clientHeight) {
        return
      };
      const { clientWidth, clientHeight } = container
      chartInstance.resize(clientWidth, clientHeight);
    })
  }

  private nodeQueryChangeHandle () {
    if (chartInstance) {
      chartInstance.filterNode(this.nodeQuery)
    }
  }

  // 重试
  private retrySuggest () {
    this.$emit('retry-suggest');
  }

  // 反馈
  private feedbackHandle () {
    this.$emit('feedback');
  }
}
</script>

<style lang="scss" scoped>
.topo-chart-wrap {
  padding: 0 !important;
  position: relative;
  height: 100%;
  display: flex;

  .topo-chart,
  .aside-detail {
    width: 50%;
    height: 100%;
    overflow: hidden;
  }

  .node-query-input {
    width: 200px;
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 2;
  }

  .empty-show {
    position: absolute;
    z-index: 100;
    top: 0;
    right: 0;
    left: 0;
    bottom: 0;
    background-color: var(--bg-color);
    font-size: 13px;
    line-height: 14px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    .empty-svg {
      margin-bottom: 40px;
      width: 214px;
      height: 126px;
    }
  }
}
</style>
