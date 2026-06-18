<template>
  <div
    v-loading='chartLoading'
    class="topo-cont-wrapper">
    <div class="topo-cont-bottom">
      <div :class='["topo-wrap",{"topo-wrap-hidden": showEmpty || chartLoading}]'>
        <div id='serviceFlowDiv' class="topo"></div>
        <div v-if='showEmpty' class="empty-show describe">{{ $t('modules.components.charts.s_21efd88b') }}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import { v4 as uuidv4 } from 'uuid';
import ApmApi from '@/api/apm';
import { toAsyncWait } from '@/utils/common'
import { FlowChart } from '@/views/appMonitor/serviceFlow/flowChart'
import deepClone from 'lodash/cloneDeep';
import { debounce } from '@/utils/common'

let flowChartInstance: any = null;

@Component
export default class TopoGraph extends Vue {
  get showEmpty() {
    const { nodes = [], edges = [] } = this.serviceSource || {};
    return !nodes.length && !edges.length && !this.chartLoading;
  }

  get getBasicServiceMap () {
    return this.$store.getters['Service/basicServiceMap']
  }

  private chartLoading = true;

  private serviceSource: any = {};

  private async mounted () {
    this.resetQueryHandle()
    this.resizeHandler = debounce(() => {
      if (flowChartInstance) {
        this.flowChartResize()
      }
    }, 100)
    window.addEventListener('resize', this.resizeHandler)
  }

  private beforeDestroy () {
    // 清空拓扑参数
    flowChartInstance?.destroy();
    flowChartInstance = null;
    window.removeEventListener('resize', this.resizeHandler)
  }

  // 当查询根服务列表为空时，非首次就为空，显示重置按钮
  private async resetQueryHandle () {
    this.chartLoading = true;
    // ft, tt 为时间戳，此页面无时间组件权限，故时间参数自行传递，不走全局方法
    const { tid, ft, tt } = this.$route.query
    let { fromTime, toTime } = this.getGlobalTime()
    if (ft && tt) {
      fromTime = new Date(+ft as number)
      toTime = new Date(+tt as number)
    }
    // 获取服务流详情
    const params: any = {
      fromTime: dayjs(fromTime).format('YYYY-MM-DD HH:mm:ss'),
      toTime: dayjs(toTime).format('YYYY-MM-DD HH:mm:ss'),
      offset: 0,
      size: 1000,
      traceId: tid
    }
    
    const { result, error } = await toAsyncWait(ApmApi.getSpanFlow(params))
    if (!error) {
      let { data = {} } = result;
      data = [data];
      this.formatFlowSource(deepClone(data));
      this.formatOriginSource(data)
    } else {
      if (error.message !== 'interrupt') {
        this.$message.error(error.message || i18n.t('modules.views.appMonitor.serviceFlow.s_e05c1ca3') as string)
      }
    }
    this.chartLoading = false;
  }

  private formatOriginSource (data: any[]) {
    // 解藕tree型数据
    // 子节点中可能存在同节点，需手动设置唯一id，否则会回流
    // 递归组装
    // data中可能存在多个根节点
    data.forEach((root) => {
      this.setUUIDDeep(root)
    })
    const _multiServiceSource: any = {};
    const level = 0;
    data.forEach((root, idx) => {
      const links: any[] = [];
      const nodes: any[] = [];
      root.isRoot = true;
      root.level = level;
      const durationInfo = root.serviceDurationRange || [];
      _multiServiceSource[root.serviceId] = this.deepFormat(root, links, nodes, durationInfo)
    })
    const [first]: any[] = Object.values(_multiServiceSource)
    if (first) {
      this.formatServiceSource(first as any)
    } else {
      this.formatServiceSource({ nodes: [], links: [], origin: {} })
    }
  }

  private setUUIDDeep (node: any) {
    const _nodeKey = uuidv4();
    node.uuid = _nodeKey
    if (node.children) {
      node.children.forEach((child: any) => {
        this.setUUIDDeep(child);
      })
    }
  }

  private deepFormat (node: any, links: any[], nodes: any[], durationInfo: any[]) {
    node.id = node.uuid;
    node.key = node.uuid;
    node.label = node.name || node.service;

    // 递归
    nodes.push({
      ...node,
      key: node.uuid,
      id: node.uuid,
    })
    if (node.children) {
      node.children.forEach((child: any) => {
        child.isRoot = false;
        child.level = node.level + 1
        links.push({
          source: node.uuid,
          target:  child.uuid,
          id: uuidv4(),
          callPct: child.callPct,
          avgSrcCall: child.avgCall,
        });
        this.deepFormat(child, links, nodes, durationInfo)
      })
    }
    return {
      links,
      nodes,
      origin: node
    }
  }

  private formatServiceSource(source: { nodes: any[], links: any[], origin: any }) {
    if (!source || (!source.nodes && !source.links)) {
      return;
    }
    const { nodes, links, origin } = source;
    const graphNodes: any = [];
    nodes.forEach((node) => {
      graphNodes.push({
        ...node,
        key: node.id,
        id: node.id,
        name: node.name,
        ovalue: node.name,
        olabel: node.name,
        level: node.level,
        hostName: node.hostName || '',
        processName: node.processName || '',
        type: node.type || 'default',
        requestCount: node.call || 0,
        responseCount: node.avgDuration || 0,
        resPercent: node.durationCvPct || 0,
        isRoot: node.isRoot || false,
      });
    });

    const graphEdges: any = [];

    links.forEach((link) => {
      const graphLink: any = {
        ...link,
        id: link.id,
        source: link.source,
        target: link.target,
      };
      graphEdges.push({
        ...graphLink
      });
    });

    this.serviceSource = {
      nodes: graphNodes,
      edges: graphEdges,
    };
  }

  private formatFlowSource (source: any[]) {
    // 子节点中可能存在同节点，需手动设置唯一id，否则会回流
    // 递归组装
    // data中可能存在多个根节点
    // try {
      const _multiFlowChartSource: any = {};
      const level = 0;
      source.forEach((root) => {
        root.isRoot = true;
        root.level = level;
        _multiFlowChartSource[root.serviceId] = this.deepFormatFlowSource(root);
      })
      const [first]: any[] = Object.values(_multiFlowChartSource)
      if (!first || !Object.keys(first).length) {
        return
      }
      if (first) {
        const flowSource = first;
        if (!flowChartInstance) {
          // 获取dom宽高
          const { width, height } = document.querySelector('#serviceFlowDiv')!.getBoundingClientRect();
          flowChartInstance = new FlowChart({
            domId: 'serviceFlowDiv',
            source: flowSource,
            width,
            height,
            viewModel: 'top',
          })
        } else {
          flowChartInstance.updateChart(flowSource);
        }
      } else {
        flowChartInstance.updateChart({id: ''});
      }
    // } catch (err) {
    //   this.$message.error(i18n.t('modules.views.appMonitor.serviceFlow.s_f2504503') as string)
    //   this.chartLoading = false
    // }
    
  }

  private deepFormatFlowSource (source: any) {
    const {
      service, serviceId, avgCall = 0, outCall = 0, callPct = 0,  avgDuration = 0, call = 0, error = 0, durationCvPct = 0,
      uid, children, hostId, serviceInstanceMap = {} } = source;
    const { service_type, type, language } = (this.getBasicServiceMap || {})[serviceId] || {}
    const _source: any = {
      id: uuidv4(), name: service, key: uid,
      viewData: {
        reqCnt: call, // 请求数
        response: avgDuration, // 平均响应时间
        contribution: durationCvPct, // 响应贡献度
        failCnt: error, // 失败请求数
        outCnt: outCall, // 外发请求数
        hostIp: '', // 主机IP
        hostName: '', // 主机名称
        avgReq: avgCall, // n次调用/请求
        callPct
      },
      serviceInfo: {
        service, serviceId,
        serviceType: type || language || service_type || 'default',
        uid,
        serviceInstanceMap
      }
    }
    if (children && children.length) {
      // 子元素先根据响应贡献度排序
      _source.children =
        children.sort(
          (a: any, b: any) => (b.durationCvPct || 0) - (a.durationCvPct || 0)
        ).map((child: any) => this.deepFormatFlowSource(child));
    }
    return _source
  }

  private resizeHandler: any = null;
  private flowChartResize () {
    this.$nextTick(() => {
      // 调用图表resize事件
      const container = document.getElementById('serviceFlowDiv');
      if (!flowChartInstance || !flowChartInstance.graph || flowChartInstance.graph.get('destroyed')) {
        return
      };
      if (!container || !container.clientWidth || !container.clientHeight) {
        return
      };
      const { clientWidth, clientHeight } = container
      flowChartInstance.graph.changeSize(clientWidth, clientHeight);
    })
  }
}
</script>

<style lang="scss" scoped>
.topo-cont-wrapper {
  padding-right: 20px;
  width: 100%;
  height: 100%;
  position: relative;
  background-color: var(--bg-color);

  .topo-cont-bottom {
    height: 100%;
    display: flex;
    // background-color: #FAFAFA;
    position: relative;
    overflow: hidden;
  }

  .topo-wrap {
    user-select: none;
    flex: 1;
    overflow: hidden;
    position: relative;

    &.topo-wrap-hidden .topo {
      opacity: 0;
    }
    .topo {
      width: 100%;
      height: 100%;
    }
    .empty-show{
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      position: absolute;
      top: 0;
      left: 0;
      z-index: 2;
    }
  }
}
</style>
<style lang="scss">
.custom-flow-contextmenu {
  background-color: var(--bg-color);
  border-radius: 4px;
  border: solid 1px var(--border-color-base);
  
  .custom-flow-contextmenu-ul {
    padding: 10px 0;
    margin: 0;
    list-style: none;
    li {
      padding: 3px 15px;
      width: 100px;
      text-align: center;
      line-height: 24px;
      color: var(--color-text-primary);
      cursor: pointer;
      transition: color .3s, background-color .3s;
      &:hover {
        color: var(--color-text-link);
        background-color: var(--bg-color03);
      }
    }
  }
}
</style>