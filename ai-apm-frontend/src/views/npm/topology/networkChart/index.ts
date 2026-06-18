import G6, { Graph, IEdge, INode, Item } from '@antv/g6';
import { CustomShapeName, registerTopoFull, tooltip } from './register';
import getDefaultCfg from './config.default';
import deepClone from 'lodash/cloneDeep';
import { EventBus } from '@/utils/common';

export interface TopoSource {
  nodes: any[];
  edges: any[];
}

export interface TopoChartCfg {
  width: number;
  height: number;
  domId: string;
  source: TopoSource;
}

export default class TopologyChart {
  constructor (cfg: TopoChartCfg) {
    const { source, domId, width, height } = cfg;
    this.domId = domId;
    this.source = this.formatSource(source);

    this.chartCfg = {
      ...getDefaultCfg(),
      width,
      height
    };
  }

  private graph: Graph | null = null;

  private domId: string = '';

  private source: TopoSource = {
    nodes: [],
    edges: []
  };

  private chartCfg: any = {};

  private choseId: string = ''

  private isInteracting = false; // 标记是否有交互正在进行
  private waitUpdateData: any = null; // 等交互结束后再更新数据

  // 实例化
  public init () {
    if (!this.graph) {
      registerTopoFull(G6);
      this.initChart();
    }
  }

  // 初始化图表
  private async initChart () {
    const dom = document.querySelector(`#${this.domId}`);
    if (!dom) {
      return
    }
    const miniMap = this.initMiniMap();
    const graph = new G6.Graph({
      ...this.chartCfg,
      container: this.domId,
      plugins: [tooltip(), miniMap],
    })
    // TODO: 关闭局部渲染，修复渲染残影的问题（临时方案）
    graph.get('canvas').set('localRefresh', false);
    graph.setMinZoom(0.5);
    graph.setMaxZoom(3);
    this.bindEvents(graph);
    this.graph = graph;
    this.updateLayout();
  }

  // 初始化minimap
  private initMiniMap () {
    return new G6.Minimap({
      size: [150, 100],
      className: 'relation-minimap',
      viewportClassName: 'relation-minimap-wrapper',
      hideEdge: true
    });
  }

  // 更新数据
  public updateData (source: TopoSource) {
    if (this.isInteracting) { // 正在交互中，缓存数据，等交互结束后再更新
      this.waitUpdateData = source;
      return;
    }
    this.source = this.formatSource(source);
    this.updateLayout();
  }

  private bindEvents (graph: Graph) {
    graph.on('afterlayout', () => {
      const fitRatio = graph?.getZoom() || 1.5;
      const zoomRatio = fitRatio > 1.5 ? 1.5 : fitRatio < 0.5 ? 0.5 : fitRatio;
      graph?.zoomTo(zoomRatio, { x: this.chartCfg.width / 2, y: this.chartCfg.height / 2 }, false, {
        duration: 300
      });

      const nodeItem = this.choseId ? graph.findById(this.choseId) as INode : null;
      if (!nodeItem || nodeItem.get('type') !== 'node') {
        this.choseId = '';
        // 在布局完成后执行视图居中，此时能确保所有节点的位置都已计算完毕
        graph?.fitView([20, 20, 20, 20]);
      } else {
        // 重新聚焦
        this.focusItem(this.choseId);
      }

      EventBus.$emit('network-afterlayout');
    });

    graph.on('canvas:click', (e) => {
      const _graph = e.currentTarget
      // 关闭自动重绘，节约性能
      _graph.setAutoPaint(false);
      const lastItem = _graph.findById(this.choseId);
      const itemType = lastItem && lastItem.get('type');
      if (lastItem && itemType === 'node') {
        lastItem.clearStates(['nodeActive'])
        _graph.getNeighbors(lastItem).forEach((node: INode) => {
          node.clearStates(['nodeActive']);
        });
        lastItem.getEdges().forEach((edge: IEdge) => {
          edge.clearStates(['active']);
          edge.toBack();
        });
      }
      this.choseId = '';
      // 手动触发绘制
      _graph.paint()
      // 恢复自动绘制
      _graph.setAutoPaint(true);
      EventBus.$emit('network-node-click', null);
    });

    graph.on('node:click', (e: any) => {
      const nodeItem = e.item as INode;
      if (!nodeItem) {
        return
      }
      // 触发 EventBus network-node-click 事件
      const model: any = nodeItem.getModel();
      EventBus.$emit('network-node-click', { ...model });
    });

    graph.on('edge:click', (e: any) => {
      const edgeItem = e.item as IEdge;
      if (!edgeItem) {
        return
      }
      // 触发 EventBus network-edge-click 事件
      const model: any = edgeItem.getModel();
      EventBus.$emit('network-edge-click', { ...model });
    });

    graph.on('node:mouseenter', (e) => {
      const nodeItem = e.item as INode;
      const _graph = e.currentTarget
      const _target = e.target
      if (!nodeItem) {
        return
      }
      if (_target?.cfg?.name === CustomShapeName.node.title) {
        return
      }
      const relationEdges = nodeItem.getEdges();
      const relationNodes = _graph.getNeighbors(nodeItem);
      this.highLightNodes([...relationNodes, nodeItem]);
      this.highLightEdges(relationEdges);
      nodeItem.toFront();
    });
    graph.on('node:mouseleave', (e) => {
      const nodeItem = e.item as INode;
      const _graph = e.currentTarget;
      if (!nodeItem) {
        return
      }
      // 查找有连线的节点
      const _relationNodes = _graph.getNeighbors(nodeItem);
      const _relationEdges = nodeItem.getEdges();
      this.clearNodesState([..._relationNodes, nodeItem]);
      this.clearEdgesState(_relationEdges);
    });
    graph.on('edge:mouseenter', (e) => {
      const edgeItem = e.item as IEdge;
      if (!edgeItem) {
        return
      }
      this.highLightNodes([edgeItem.getSource(), edgeItem.getTarget()]);
      this.highLightEdges([edgeItem]);
    });
    graph.on('edge:mouseleave', (e) => {
      const edgeItem = e.item as IEdge;
      if (!edgeItem) {
        return
      }
      this.clearNodesState([edgeItem.getSource(), edgeItem.getTarget()]);
      this.clearEdgesState([edgeItem]);
    });

    graph.on('node:dragstart', () => {
      this.isInteracting = true;
    });
    graph.on('node:dragend', () => {
      this.isInteracting = false;
      // 如果之前在拖动过程中有等待的更新，可以在这里触发
      if (this.waitUpdateData) {
        this.updateData(this.waitUpdateData);
        this.waitUpdateData = null;
      }
    });
  }

  public resize (width?: number, height?: number) {
    if (this.graph) {
      this.chartCfg.width = width || this.chartCfg.width;
      this.chartCfg.height = height || this.chartCfg.height;
      this.graph.changeSize(this.chartCfg.width, this.chartCfg.height);
    }
  }

  public async updateLayout () {
    if (this.graph) {
      // 更新时设置为安静模式，不允许拖动节点
      this.graph.setMode('quiet');

      // 关闭自动绘制，防止在有切换布局的情况下，渲染和布局冲突
      this.graph.setAutoPaint(false);

      // 使用 changeData 替代 data 和 render
      // this.graph!.data(this.source);
      // this.graph!.render();
      this.graph.changeData(this.source);

      // 重新打开自动绘制
      this.graph.paint();
      this.graph.setAutoPaint(true);
      // if (this.graph && this.source && this.source.nodes && this.source.nodes.length > 350) {
      //   this.graph.setMode('optimize');
      // }

      let updateTimer: any = setTimeout(() => {
        window.clearTimeout(updateTimer);
        updateTimer = null;
        if (!this.graph) {
          return;
        }
        // 恢复默认模式
        this.graph?.setMode('default');
        this.graph?.getNodes().forEach((node: INode) => {
          node.toFront();
        });
      }, 100);
    }
  }

  private highLightNodes (nodes: INode[]) {
    nodes.forEach((node) => {
      node.setState('nodeActive', true);
    })
  }

  private clearNodesState (nodes: INode[]) {
    const choseItem = this.choseId && this.graph ? this.graph?.findById(this.choseId) : null;
    const choseType = choseItem && choseItem.get('type');
    let ignoreNodeIds: string[] = [];
    if (choseItem && choseType === 'node') {
      const relationNodes = choseItem ? this.graph?.getNeighbors(choseItem as INode) : [];
      ignoreNodeIds = [
        this.choseId,
        ...Array.isArray(relationNodes) ? relationNodes.map((n) => n.get('id')) : [],
      ];
    }
    nodes.forEach((node) => {
      if (ignoreNodeIds.includes(node.get('id'))) {
        return;
      }
      node.clearStates(['nodeActive']);
    })
  }

  private highLightEdges (edges: IEdge[]) {
    edges.forEach((edge: IEdge) => {
      edge.setState('active', true);
      edge.toFront();
    });
  }

  private clearEdgesState (edges: IEdge[]) {
    const choseItem = this.choseId && this.graph ? this.graph?.findById(this.choseId) : null;
    const choseType = choseItem && choseItem.get('type');
    edges.forEach((edge: IEdge) => {
      if (this.choseId && choseType === 'node' && (edge.getSource().get('id') === this.choseId || edge.getTarget().get('id') === this.choseId)) {
        return;
      }
      edge.clearStates(['active']);
      edge.toBack();
    });
  }

  private formatSource (source: any) {
    const _source = deepClone(source);
    const multiLines: any = {}
    _source.edges.forEach((edge: any) => {
      const key = [edge.source, edge.target].sort().join('_|_')
      multiLines[key] = (multiLines[key] || 0) + 1
    })
    _source.edges.forEach((edge: any) => {
      const key = [edge.source, edge.target].sort().join('_|_')
      if (multiLines[key] > 1) {
        edge.type = CustomShapeName.multiEdge.name
      }
    })
    this.source = source
    return _source
  }

  public focusItem (itemId: string) {
    if (!this.graph) {
      return
    }

    this.graph!.setAutoPaint(false);
    this.graph!.getNodes().forEach((node: INode) => {
      this.graph!.clearItemStates(node);
    });
    this.graph!.getEdges().forEach((edge: IEdge) => {
      this.graph!.clearItemStates(edge);
    });
    const nodeItem = this.graph.findById(itemId) as INode;
    if (!nodeItem || nodeItem.get('type') !== 'node') {
      this.graph!.paint();
      this.graph!.setAutoPaint(true);
      this.choseId = '';
      return;
    }

    this.graph.focusItem(nodeItem, true, {
      duration: 500,
      easing: 'easeCubic'
    })
    const relationEdges = nodeItem.getEdges();
    const relationNodes = this.graph.getNeighbors(nodeItem);
    this.highLightNodes([...relationNodes, nodeItem]);
    this.highLightEdges(relationEdges);
    nodeItem.toFront();
    this.graph!.paint();
    this.graph!.setAutoPaint(true);
    this.choseId = itemId;
  }

  public destroy () {
    if (this.graph) {
      this.graph.clear()
      this.graph.destroy()
      this.graph = null
    }
  }
}
