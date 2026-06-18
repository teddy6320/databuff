<template>
  <div id="global-topology-graph-container" class="cont ovh">
    <div class="wrapper bg-color ovh pos-r flex-h-jc">
      <div v-loading="loading.data" class="flex-v ovh flex-1 h-100p pos-r">
        <toolbar
          @search="handleSearchChange"
          @change="handleConfigChange"
          @inited="handleConfigInited"
          :config="getConfig"
          :search-options="searchOptions"
          class="search-comp pl-10 pr-10" />
        <div id="container" class="h-100p pos-r flex-1 ovh topo-container" ref="topoContainer"></div>
        <div v-show="isEmpty && !loading.data" class="empty-show describe">{{ $t('modules.components.charts.s_21efd88b') }}</div>
      </div>
      <aside-detail :current="asideDetailInfo.currentDetailNode" ref="asideDetail" />
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator'
import Toolbar from './toolbar.vue'
import TopoGraph from '../relationMapNew/graph'
import { CanvasEvent, EdgeEvent, NodeEvent } from 'g6-v5'
import type { GraphData } from 'g6-v5'
import {
  CustomConfig,
  defaultCustomConfig,
  expandLayoutToViewport,
  fitTopologyInitialView,
  formatSource,
  richStyleByConfig,
} from '../relationMapNew/graph/utils'
import AsideDetail from '../relationMap/aside-detail.vue'
import TopologyApi from '@/api/topology'
import { toAsyncWait } from '@/utils/common'
import { cloneDeep } from 'lodash'

const CONFIG_STORAGE_KEY = 'DATABUFF_GLOBAL_TOPOLOGY_CUSTOM_CONFIG'

let graphInstance: TopoGraph | null = null
let globalSourceData: { alarmData: { error: number; total: number }; graphData: GraphData } | null = null
let currentConfig: CustomConfig = {
  ...cloneDeep(defaultCustomConfig()),
}
let originServices: any[] = []
let originServiceEdges: any[] = []

@Component({
  components: {
    Toolbar,
    AsideDetail,
  },
})
export default class GlobalTopology extends Vue {
  public $refs!: {
    topoContainer: HTMLDivElement
    asideDetail: InstanceType<typeof AsideDetail>
  }

  private lastClickEdge = { id: '' }
  private lastClickNode: any = { id: '', data: null }
  @Watch('lastClickNode.id')
  private onLastClickNodeIdChange (val: string, oldVal: string) {
    this.asideDetailActiveChange(!!val)
    if (!val) {
      return
    }
    this.$nextTick(() => {
      this.$refs.asideDetail?.getData()
      // 仅首次打开侧栏时微调视口；切换节点时保持画布位置不变
      if (!oldVal) {
        requestAnimationFrame(() => {
          this.moveNodeToCenter()
        })
      }
    })
  }

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime () {
    this.durationChangeHandle()
  }

  private loading = { data: false }
  private isEmpty = false
  private mountedStatus = { parent: false, children: false }
  private searchOptions: Common.NestLabelValue[] = []

  private asideDetailInfo: any = {
    currentDetailNode: null,
  }

  get getConfig () {
    return cloneDeep(currentConfig)
  }

  get mountedOver () {
    return this.mountedStatus.parent && this.mountedStatus.children
  }

  @Watch('mountedOver')
  private async onMountedOverChange (val: boolean) {
    if (val) {
      await this.updateGraph()
    }
  }

  private created () {
    if (window?.localStorage) {
      const savedConfig = window.localStorage.getItem(CONFIG_STORAGE_KEY)
      if (savedConfig) {
        try {
          currentConfig = {
            ...currentConfig,
            ...JSON.parse(savedConfig),
          }
        } catch (err) {
          //
        }
      }
    }
  }

  private mounted () {
    this.mountedStatus.parent = true
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChangeHandle()
    })
  }

  private beforeDestroy () {
    const _graph = graphInstance?.getInstance?.()
    if (_graph) {
      _graph.destroy()
      _graph.off()
      graphInstance = null
    }
    this.$eventBus.$off('GlobalRefresh')
    this.resetAllData()
  }

  private initGraph (data?: GraphData) {
    const _graph = graphInstance?.getInstance?.()
    if (_graph) {
      _graph.destroy()
      _graph.off()
      graphInstance = null
    }
    const { clientWidth, clientHeight } = this.$refs.topoContainer
    graphInstance = new TopoGraph({
      container: 'container',
      width: clientWidth,
      height: clientHeight,
      padding: [60, 48, 48, 48],
      autoFit: undefined,
      data: cloneDeep(data || { nodes: [], edges: [] }),
    })

    graphInstance.bindEvents({
      [CanvasEvent.CLICK]: (e) => {
        if (e?.targetType && e.targetType !== 'canvas') {
          return
        }
        this.resetEdgeLabel(this.lastClickEdge.id)
        this.lastClickNode = { id: '', data: null }
      },
      [NodeEvent.CLICK]: (e) => {
        this.resetEdgeLabel(this.lastClickEdge.id)
        const _graphInst = graphInstance?.getInstance()
        if (!_graphInst || e?.targetType !== 'node') {
          return
        }
        const nodeId = String(e.target?.id || '')
        if (!nodeId) {
          return
        }
        let _nodeData: any
        try {
          _nodeData = cloneDeep(_graphInst.getNodeData(nodeId))
        } catch (err) {
          _nodeData = null
        }
        this.lastClickNode = (!this.lastClickNode.id || this.lastClickNode.id !== nodeId)
          ? { id: nodeId, data: cloneDeep(_nodeData) }
          : { id: '', data: null }
      },
      [EdgeEvent.CLICK]: (e) => {
        this.resetEdgeLabel(this.lastClickEdge.id)
        const _graphInst = graphInstance?.getInstance()
        if (!_graphInst) {
          return
        }
        const edgeId = e.target.id
        try {
          const edgeData = _graphInst.getEdgeData(edgeId)
          const isSelected = _graphInst.getElementState(edgeId).includes('selected')
          this.lastClickEdge.id = isSelected ? '' : edgeId
          _graphInst.updateEdgeData([{
            id: edgeId,
            style: {
              labelText: isSelected
                ? edgeData?.data?.baseInfo as string || ''
                : edgeData?.data?.fullInfo as string || '',
            },
          }])
        } catch (err) {
          //
        }
      },
    })
  }

  private async updateGraph () {
    let _graphInstance = graphInstance?.getInstance?.()
    if (!_graphInstance) {
      this.initGraph(globalSourceData?.graphData)
      _graphInstance = graphInstance?.getInstance?.()
    }

    await this.fetchSource(!!globalSourceData)

    const _graphData = cloneDeep(globalSourceData?.graphData || { nodes: [], edges: [] })
    richStyleByConfig(_graphData, currentConfig)
    const { clientWidth, clientHeight } = this.$refs.topoContainer
    expandLayoutToViewport(_graphData, clientWidth, clientHeight, 80)
    await _graphInstance!.setData(_graphData)
    await _graphInstance!.render()
    await fitTopologyInitialView(_graphInstance!)
    this.formatSearchOptions()
    this.isEmpty = _graphInstance!.getNodeData().length === 0
  }

  private resetEdgeLabel (edgeId: string) {
    if (!edgeId) {
      return
    }
    const _graph = graphInstance?.getInstance()
    if (!_graph) {
      return
    }
    try {
      const edgeData = _graph.getEdgeData(edgeId)
      _graph.updateEdgeData([{
        id: edgeId,
        style: {
          labelText: edgeData?.data?.baseInfo as string || '',
        },
      }])
    } catch (err) {
      //
    }
    this.lastClickEdge.id = ''
  }

  private async fetchSource (refresh = false) {
    this.loading.data = true
    const { fromTime, toTime } = this.getGlobalTimeV2()
    try {
      if (!refresh) {
        const { error, result } = await toAsyncWait(
          TopologyApi.getGlobalTopology({ fromTime, toTime, limit: currentConfig.maxNode.service || 500 }),
        )
        if (!error) {
          const { data = {} } = result || {}
          originServices = data.services || []
          originServiceEdges = data.serviceEdges || []
        } else {
          originServices = []
          originServiceEdges = []
        }
      }

      const formatted = await formatSource(
        { service: cloneDeep(originServices) },
        { service: cloneDeep(originServiceEdges) },
        currentConfig,
      )
      globalSourceData = formatted.service || { alarmData: { error: 0, total: 0 }, graphData: { nodes: [], edges: [] } }
    } finally {
      this.loading.data = false
    }
  }

  private async handleConfigChange (config: CustomConfig) {
    const isChangeDirection = currentConfig.direction !== config.direction
    currentConfig = config
    localStorage?.setItem(CONFIG_STORAGE_KEY, JSON.stringify(currentConfig))
    await this.updateGraph()
    if (isChangeDirection) {
      const graph = graphInstance?.getInstance?.()
      if (graph) {
        fitTopologyInitialView(graph)
      }
    }
  }

  private handleConfigInited () {
    this.mountedStatus.children = true
  }

  private handleSearchChange (searchModel: { nameQuery: string; serviceIds?: string[] }) {
    const _graphInstance = graphInstance?.getInstance?.()
    if (!_graphInstance) {
      return
    }
    const allNodes = _graphInstance.getNodeData()
    const edges = _graphInstance.getEdgeData()
    const seedIds = (searchModel.serviceIds || []).filter(Boolean)
    const hasQuery = seedIds.length > 0
    const showNodeIds = new Set<string>()
    const elementsVisibility: Record<string, 'visible' | 'hidden'> = {}

    if (hasQuery) {
      seedIds.forEach((id) => showNodeIds.add(id))
      edges.forEach((edge) => {
        const source = String(edge.source)
        const target = String(edge.target)
        if (seedIds.includes(source)) {
          showNodeIds.add(target)
        }
        if (seedIds.includes(target)) {
          showNodeIds.add(source)
        }
      })
    }

    allNodes.forEach((node) => {
      elementsVisibility[node.id] = hasQuery
        ? (showNodeIds.has(node.id) ? 'visible' : 'hidden')
        : 'visible'
    })
    edges.forEach((edge) => {
      if (edge?.id) {
        const canShow = hasQuery
          ? showNodeIds.has(String(edge.source)) && showNodeIds.has(String(edge.target))
          : true
        elementsVisibility[edge.id] = canShow ? 'visible' : 'hidden'
        _graphInstance.updateEdgeData([{
          id: edge.id,
          style: {
            endArrow: canShow,
            endArrowSize: canShow ? 5 : 0,
          },
        }])
      }
    })
    _graphInstance.setElementVisibility(elementsVisibility, false)
    this.isEmpty = hasQuery ? showNodeIds.size === 0 : allNodes.length === 0
    if (hasQuery && showNodeIds.size > 0) {
      this.handleNodeFocus(seedIds[0], false)
    }
    _graphInstance.draw()
    this.formatSearchOptions()
  }

  private handleNodeFocus (nodeId: string, emitSelected = true) {
    const _graphInstance = graphInstance?.getInstance?.()
    if (!_graphInstance || !nodeId) {
      return
    }
    const lastSelectedNodes = _graphInstance.getElementDataByState('node', 'selected')
    lastSelectedNodes?.forEach((node: any) => {
      _graphInstance.setElementState(node.id, [], false)
    })
    const targetItem = _graphInstance.getNodeData(nodeId)
    if (!targetItem) {
      return
    }
    _graphInstance.setElementState(nodeId, emitSelected ? 'selected' : '', true)
    this.panNodeIntoView(nodeId, { fullContainer: true })
  }

  private formatSearchOptions () {
    const _graph = graphInstance?.getInstance()
    if (!_graph || _graph.getNodeData().length === 0) {
      this.searchOptions = []
      return
    }
    this.searchOptions = _graph.getNodeData()
      .filter((n) => n?.style?.visibility !== 'hidden')
      .map((n) => ({
        label: n.data?.label as string || '',
        value: n.id,
      }))
  }

  private resolveServiceNode (nodeId: string, nodeItem?: any) {
    const origin = originServices.find((service) => {
      const serviceId = String(service?.serviceId || service?.id || '')
      return serviceId === String(nodeId)
    })
    const nodeData = nodeItem?.data || {}
    return {
      id: String(origin?.serviceId || origin?.id || nodeId || ''),
      name: origin?.service || origin?.serviceName || origin?.name
        || nodeData.label || nodeItem?.style?.labelText || '',
      typeIcon: nodeData.subType || origin?.type || origin?.language || 'default',
    }
  }

  private asideDetailActiveChange (show: boolean) {
    const { id: nodeId, data: nodeItem } = this.lastClickNode
    if (!show) {
      this.asideDetailInfo.currentDetailNode = null
      return
    }
    const serviceNode = this.resolveServiceNode(nodeId, nodeItem)
    if (!serviceNode.id) {
      this.asideDetailInfo.currentDetailNode = null
      return
    }
    this.asideDetailInfo.currentDetailNode = {
      ...serviceNode,
      baseType: 'service',
    }
  }

  private moveNodeToCenter () {
    if (!this.lastClickNode.id) {
      return
    }
    this.panNodeIntoView(this.lastClickNode.id)
  }

  /** 将节点平移到可视区域中心，保留当前缩放比例 */
  private panNodeIntoView (nodeId: string, options: { fullContainer?: boolean } = {}) {
    const _graph = graphInstance?.getInstance()
    if (!_graph || !nodeId) {
      return
    }
    const { clientX, clientY, left, top } = this.calcAreaCenter(options.fullContainer)
    const [x, y] = _graph.getElementPosition(nodeId)
    if (typeof x !== 'number' || typeof y !== 'number') {
      return
    }
    _graph.translateTo([clientX - left - x, clientY - top - y], {
      duration: 200,
      easing: 'easeCubic',
    })
  }

  private calcAreaCenter (isFullCont = false) {
    const isExpanded = this.$refs.asideDetail?.showDetail
    const { width, left, top } = this.$refs.topoContainer.getBoundingClientRect()
    const { width: adWidth } = this.$refs.asideDetail.$el.getBoundingClientRect()
    const _adWidth = isFullCont ? 0 : (isExpanded ? adWidth : 0)
    const centerX = left + (width - _adWidth) / 2
    const centerY = top + this.$refs.topoContainer.clientHeight / 2
    return { clientX: centerX, clientY: centerY, left, top }
  }

  private resetAllData () {
    globalSourceData = null
    originServices = []
    originServiceEdges = []
    currentConfig = { ...cloneDeep(defaultCustomConfig()) }
  }

  private async durationChangeHandle () {
    this.loading.data = true
    globalSourceData = null
    originServices = []
    originServiceEdges = []
    await this.updateGraph()
    this.$refs.asideDetail?.getData()
    this.loading.data = false
  }
}
</script>

<style lang="scss" scoped>
.cont {
  height: 100%;
}
.wrapper {
  height: 100%;
}
.search-comp {
  height: 50px;
  position: absolute;
  top: 0;
  right: 32px;
  left: 0;
  z-index: 9;
  user-select: none;
}
.empty-show {
  position: absolute;
  z-index: 8;
  top: 0;
  right: 0;
  left: 1px;
  bottom: 0;
  background-color: #ffffff;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
