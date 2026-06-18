<template>
  <div class="topo-graph-wrap"
    v-loading="networkLoading || queryLoading">
    <div :id="graphId" :class="{ loading: networkLoading }" class="topo-graph"></div>

    <scroll-select
      v-show="nodeOptions.length"
      v-model="nodeQuery"
      @change="nodeQueryChangeHandle"
      :options="nodeOptions"
      :placeholder="$t('modules.views.npm.topology.s_1da87656')"
      class="node-query-input" />

    <div v-show="networkSource.nodes.length" class="legend-wrap">
      <div class="legend-title border">{{ $t('modules.views.npm.topology.s_0d22a29b') }}</div>
      <div class="flex-h border">
        <div class="legend">
          <div class="edge min"></div>
          {{ $t('modules.views.npm.topology.s_0fd71ec4', { value0: networkLegend.minEdge | valueFilter(networkLegend.unit) }) }}
        </div>
        <div class="legend">
          <div class="edge max"></div>
          {{ $t('modules.views.npm.topology.s_0b0597f5', { value0: networkLegend.maxEdge | valueFilter(networkLegend.unit) }) }}
        </div>
      </div>
      <div class="legend-title border">{{ $t('modules.views.npm.topology.s_2c051c3b') }}</div>
      <div class="flex-h">
        <div class="legend">
          <div class="node min"></div>
          {{ $t('modules.views.npm.topology.s_0fd71ec4', { value0: networkLegend.minNode | valueFilter(networkLegend.unit) }) }}
        </div>
        <div class="legend">
          <div class="node max"></div>
          {{ $t('modules.views.npm.topology.s_0b0597f5', { value0: networkLegend.maxNode | valueFilter(networkLegend.unit) }) }}
        </div>
      </div>
    </div>

    <div v-show="!networkLoading && !networkSource.nodes.length" class="empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch, Prop } from 'vue-property-decorator';
import i18n from '@/i18n';
import humanFormat from 'human-format';
import getUnitData from '@/utils/getUnitData';
import { debounce } from '@/utils/common'
import { EventBus } from '@/utils/common';
import { toAsyncWait } from '@/utils/common';
import NpmApi from '@/api/npm';
import NetworkChart from './networkChart';

let topologyGraph: any = null;

// 对节点或边的size进行缩放，使其在合适的范围内
const formatSize = (list: any[], field: string, max: number, min: number, step = 10, len = 9) => {
  const _size = (max - min) / len || 1
  list.forEach(item => {
    const scale = (item[field] - min) / _size + 1
    item.size = +(scale * step).toFixed(2)
  });
}

const valueFilter = (value: number, unit: string, isStr = true) => {
  if (!value && String(value) !== '0' || isNaN(+value) || !isFinite(+value)) {
    return isStr ? '-' : { value: '-', unit: '' }
  }
  const _val = Number(value)
  if (_val === 0) {
    return isStr ? '0' : { value: '0', unit: '' }
  }
  const { scale_factor, scale, sub_unit, family } = getUnitData(unit);
  const vData = humanFormat.raw(Number(value) * scale_factor, {
    ...scale,
    decimals: 2,
  })
  if (!['time', 'bytes'].includes(family)) {
    const _value = _val < 0.1 ? '< 0.1' : `${vData.value}${vData.prefix}`
    return isStr ? `${_value} ${scale.unit}${sub_unit}` :
      { value: _value, unit: `${scale.unit}${sub_unit}` }
  }
  return isStr ? `${vData.value} ${vData.prefix}${scale.unit}${sub_unit}` :
    { value: vData.value, unit: `${vData.prefix}${scale.unit}${sub_unit}` }
}

@Component({
  filters: {
    valueFilter,
  }
})
export default class TopoGraph extends Vue {
  @Prop({ default: () => [] }) private metricList!: any[];
  @Prop({ default: () => ({}) }) private query!: any;
  @Prop({ default: false }) private queryLoading!: boolean;

  get metricMapping () {
    const mapping: any = {}
    this.metricList.forEach(t => {
      mapping[t.value] = t
    });
    return mapping
  }

  private graphId = 'networkGraph'
  private networkLoading = false
  private networkSource: any = { nodes: [], edges: [] }
  private networkLegend: any = {
    maxNode: 0,
    minNode: 0,
    maxEdge: 0,
    minEdge: 0,
    unit: '',
  }

  get nodeOptions () {
    return this.networkSource.nodes.map((t: any) => t.name)
  }

  private nodeQuery = ''

  get timeQuery () {
    const { fromTime, toTime, durationRange } = this.$route.query
    const query: any = {}
    if (fromTime && toTime) {
      query.fromTime = fromTime
      query.toTime = toTime
    } else if (durationRange) {
      query.durationRange = durationRange
    }
    return query
  }

  private mounted () {
    this.resizeHandler = debounce(() => {
      if (topologyGraph) {
        this.chartResize()
      }
    }, 100)
    window.addEventListener('resize', this.resizeHandler)

    EventBus.$on('network-afterlayout', (data: any) => {
      this.networkLoading = false;
    });
    EventBus.$on('network-node-click', (data: any) => {
      if (!data) {
        this.nodeQuery = '';
        return;
      }

      const dimension = this.query.dimension || ''
      const client = `src${dimension[0].toUpperCase()}${dimension.slice(1)}`
      const server = dimension
      const getName = (name: string) => name !== 'N/A' ? name : ''
      this.$router.push({
        path: '/npm/analysis',
        query: {
          ...this.timeQuery,
          client,
          server,
          from: JSON.stringify([`${client}:${getName(data.name)}`, `${server}:${getName(data.name)}`]),
          conn: 'OR',
        },
      })
    });
    EventBus.$on('network-edge-click', (data: any) => {
      const dimension = this.query.dimension || ''
      const client = `src${dimension[0].toUpperCase()}${dimension.slice(1)}`
      const server = dimension
      const getName = (name: string) => name !== 'N/A' ? name : ''
      this.$router.push({
        path: '/npm/analysis',
        query: {
          ...this.timeQuery,
          client,
          server,
          from: JSON.stringify([`${client}:${getName(data.source)}`, `${server}:${getName(data.target)}`]),
          conn: 'AND',
        },
      })
    });
  }

  private beforeDestroy () {
    // 清空拓扑参数
    topologyGraph?.destroy();
    topologyGraph = null;
    window.removeEventListener('resize', this.resizeHandler)
    EventBus.$off('network-afterlayout');
    EventBus.$off('network-node-click');
    EventBus.$off('network-edge-click');
  }

  public getData () {
    this.getChartData()
  }

  private async getChartData () {
    const { dimension, metric } = this.query || {}
    const currMetric = this.metricMapping[metric] || {}
    const otherMetrics = this.metricList.filter(t => t.type === currMetric.type && t.value !== metric).map(t => t.value)
    this.networkLoading = true
    Promise.all([
      this.getNodes('client', otherMetrics),
      this.getNodes('server', otherMetrics),
      this.getEdges(),
    ]).then((result) => {
      const [nodes1, nodes2, edges1] = result as any[]
      // 合并节点
      const nodes: any[] = [...nodes1];
      nodes2.forEach((t: any) => {
        if (!nodes.find(node => node.name === t.name)) {
          nodes.push(t)
        }
      });
      const edges: any[] = edges1.filter((t: any) => {
        return t.source !== t.target && nodes.find(node => node.name === t.source) && nodes.find(node => node.name === t.target)
      })
      const networkLegend: any = {
        maxNode: 0,
        minNode: 0,
        maxEdge: 0,
        minEdge: 0,
        unit: currMetric.unit || '',
      }
      nodes.forEach((node: any, index: number) => {
        node.id = node.name
        node[metric] = node[metric] || 0
        if (index) {
          networkLegend.maxNode = Math.max(networkLegend.maxNode, node[metric])
          networkLegend.minNode = Math.min(networkLegend.minNode, node[metric])
        } else {
          networkLegend.maxNode = node[metric]
          networkLegend.minNode = node[metric]
        }
        const cVal: any = valueFilter(node[metric], currMetric.unit || '', false)
        node.viewData = {
          ...node,
          metrics: [
            {
              name: currMetric.label || metric,
              metric,
              value: node[metric],
              _value: [cVal.value, cVal.unit],
            },
            ...otherMetrics.map(t => {
              const tMetric = this.metricMapping[t] || {}
              const tVal: any = valueFilter(node[t], tMetric.unit || '', false)
              return {
                name: tMetric.label || t,
                metric: t,
                value: node[t],
                _value: [tVal.value, tVal.unit],
              }
            })
          ],
        }
      });
      edges.forEach((edge: any, index: number) => {
        edge[metric] = edge[metric] || 0
        if (index) {
          networkLegend.maxEdge = Math.max(networkLegend.maxEdge, edge[metric])
          networkLegend.minEdge = Math.min(networkLegend.minEdge, edge[metric])
        } else {
          networkLegend.maxEdge = edge[metric]
          networkLegend.minEdge = edge[metric]
        }
        const cVal: any = valueFilter(edge[metric], currMetric.unit || '', false)
        edge.viewData = {
          ...edge,
          metrics: [
            { name: i18n.t('modules.views.npm.analysis.s_d6ef5b2d') as string, nameKey: 'modules.views.npm.analysis.s_d6ef5b2d', _value: [edge.source] },
            { name: i18n.t('modules.views.npm.analysis.s_f4bd4f2a') as string, nameKey: 'modules.views.npm.analysis.s_f4bd4f2a', _value: [edge.target] },
            {
              name: currMetric.label || metric,
              metric,
              value: edge[metric],
              _value: [cVal.value, cVal.unit],
            },
          ]
        }
      })
      const edgeMaxItem = edges.find((t: any) => t[metric] === networkLegend.maxEdge)
      if (edgeMaxItem) {
        edgeMaxItem.isMax = true
      }
      formatSize(nodes, metric, networkLegend.maxNode, networkLegend.minNode, 28, 2)
      formatSize(edges, metric, networkLegend.maxEdge, networkLegend.minEdge, 2, 3)

      const networkSource = { nodes, edges }
      this.networkSource = networkSource
      this.networkLegend = networkLegend

      if (this.nodeQuery && !this.nodeOptions.includes(this.nodeQuery)) {
        this.nodeQuery = ''
      }

      this.chartUpdate()
      if (!topologyGraph) {
        this.networkLoading = false
      }
    }).catch(() => {
      this.networkLoading = false
    })
  }

  private chartUpdate () {
    if (!topologyGraph) {
      const dom = document.getElementById(this.graphId);
      if (!dom) {
        return
      }
      const { width, height } = dom.getBoundingClientRect();
      topologyGraph = new NetworkChart({
        domId: this.graphId,
        source: this.networkSource,
        width,
        height,
      });
      // 获取dom宽高
      if (topologyGraph) {
        topologyGraph.init();
      }
    } else {
      topologyGraph.updateData(this.networkSource);
    }
  }

  private resizeHandler: any = null;
  private chartResize () {
    this.$nextTick(() => {
      // 调用图表resize事件
      const dom = document.getElementById(this.graphId);
      if (!topologyGraph || !dom || !dom.clientWidth || !dom.clientHeight) {
        return
      };
      topologyGraph.resize(dom.clientWidth, dom.clientHeight);
    })
  }

  private nodeQueryChangeHandle () {
    if (topologyGraph) {
      topologyGraph?.focusItem(this.nodeQuery)
    }
  }

  private getNodes (type: 'client' | 'server', otherMetrics: string[]) {
    return new Promise(async (resolve, reject) => {
      const { metric, from, fromTime, toTime } = this.query || {}
      const dimension = this.query.dimension || ''
      const dimensionName = type === 'server' ? dimension : `src${dimension[0].toUpperCase()}${dimension.slice(1)}`
      const params = {
        start: Math.floor(+new Date(fromTime)),
        end: Math.floor(+new Date(toTime)),
        by: [dimensionName],
        name: dimensionName,
        metrics: [
          ...otherMetrics,
          metric,
        ],
        from,
      }
      const { result, error } = await toAsyncWait(NpmApi.getNpmTopoNodes(params))
      const data = ((result || {}).data || []).map((t: any) => ({
        ...t,
        name: t.name || 'N/A',
      }))
      resolve(data)
    })
  }
  private getEdges () {
    return new Promise(async (resolve, reject) => {
      const { metric, from, fromTime, toTime } = this.query || {}
      const dimension = this.query.dimension || ''
      const client = `src${dimension[0].toUpperCase()}${dimension.slice(1)}`
      const server = dimension
      const params = {
        start: Math.floor(+new Date(fromTime)),
        end: Math.floor(+new Date(toTime)),
        by: [client, server],
        source: client,
        target: server,
        metrics: [metric],
        from,
      }
      const { result, error } = await toAsyncWait(NpmApi.getNpmTopoEdges(params))
      const data = ((result || {}).data || []).map((t: any) => ({
        ...t,
        source: t.source || 'N/A',
        target: t.target || 'N/A',
      }))
      resolve(data)
    })
  }
}
</script>

<style lang="scss" scoped>
.topo-graph-wrap {
  width: 100%;
  height: 100%;
  position: relative;

  .topo-graph {
    width: 100%;
    height: 100%;

    &.loading {
      opacity: 0;
      pointer-events: none;
    }
  }

  .node-query-input {
    width: 240px;
    position: absolute;
    top: 16px;
    right: 16px;
    z-index: 2;
  }

  .legend-wrap {
    width: 220px;
    background: var(--bg-color);
    border: 1px solid var(--border-color-base);
    border-radius: 3px;
    font-size: 12px;
    position: absolute;
    bottom: 16px;
    left: 16px;
    pointer-events: none;
    z-index: 2;
    .border {
      border-bottom: 1px solid var(--border-color-base);
    }
    .legend-title {
      padding-left: 8px;
      line-height: 24px;
    }
    .legend {
      width: 50%;
      padding: 50px 0 6px;
      text-align: center;
      position: relative;
      line-height: 18px;
      .node,
      .edge {
        margin-top: -12px;
        position: absolute;
        transform: translate(-50%, -50%);
        top: 50%;
        left: 50%;
      }
      .edge {
        width: 50px;
        border-radius: 20px;
        &.min {
          height: 6px;
          background: var(--color-info);
        }
        &.max {
          height: 20px;
          background: #9F9ADB;
        }
      }
      .node {
        border: 1px solid var(--border-color-base);
        border-radius: 100%;
        background-color: var(--bg-color02);
        &.min {
          width: 16px;
          height: 16px;
        }
        &.max {
          width: 30px;
          height: 30px;
        }
      }
    }
  }

  .empty {
    width: 100%;
    height: 100%;
    background: var(--bg-color);
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 14px;
    color: var(--color-text-regular);
    position: absolute;
    top: 0;
    left: 0;
    z-index: 3;
  }

  :deep(.relation-minimap) {
    position: absolute;
    bottom: 16px;
    right: 16px;
    border: 1px solid var(--border-color-base);
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(40px);
  }
}
</style>

<style lang="scss">
.g6-network-tooltip {
  display: inline-block;
  vertical-align: top;
  min-width: 200px;
  max-width: 400px;
  border-radius: 6px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(40px);
  border: 2px solid #EEEFF1;
  box-shadow: 0px 2px 10px 0px rgba(119, 122, 126, 0.12);
  font-size: 12px;
  line-height: 16px;
  color: var(--color-text-regular);
  word-break: break-all;
  z-index: 3;

  .title {
    padding: 5px 10px;
    background: var(--background-color-base);
    font-size: 14px;
    line-height: 20px;
    color: var(--color-text-primary);
  }
  .content {
    padding: 5px 10px;
  }
  .metric-item {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 70px;
    padding: 6px 0;
    .value {
      margin-top: 3px;
      span {
        font-size: 14px;
        line-height: 20px;
        color: var(--color-text-primary);
      }
    }
  }
  .metrics {
    display: flex;
    justify-content: space-between;
    border-top: 1px solid var(--border-color-base);
    &.border-bottom {
      border: none;
      border-bottom: 1px solid var(--border-color-base);
    }
    .metric-item + .metric-item {
      margin-left: 10px;
    }
  }
}
</style>
