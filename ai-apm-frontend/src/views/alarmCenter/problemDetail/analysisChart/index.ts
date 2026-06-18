import G6, { Graph, IEdge, INode, } from '@antv/g6';
import { registerNode, registerEdge, ChartTheme, colors, NodeSize } from './register';
import deepClone from 'lodash/cloneDeep';
import { EventBus } from '@/utils/common';

export interface TopoSource {
  nodes: any[];
  edges: any[];
}

interface TopoChartCfg {
  width: number;
  height: number;
  domId: string;
  source: TopoSource;
  layoutRankdir?: 'TB' | 'BT' | 'LR' | 'RL';
  theme: ChartTheme;
}

export default class TopoChart {
  constructor (cfg: TopoChartCfg) {
    const { source, domId, width, height, layoutRankdir, theme } = cfg;
    if (source && Object.keys(source).length > 0) {
      if (width) {
        this.defaultCfg.width = width;
      }
      if (height) {
        this.defaultCfg.height = height;
      }
      if (layoutRankdir) {
        this.defaultCfg.layout.rankdir = layoutRankdir
      }
      this.theme = theme || this.theme;
      this.formatSource(deepClone(source));
      this.initChart(domId);
    }
  }

  private theme: ChartTheme = 'dark';

  private defaultCfg = {
    width: 400,
    height: 400,
    minZoom: 0.3,
    maxZoom: 3,
    animate: true,
    animateCfg: {
      duration: 300,
      easing: 'easePolyIn',
    },
    fitCenter: true,
    // padding: [20, 50],
    modes: {
      default: ['drag-canvas', 'zoom-canvas'],
    },
    layout: {
      type: 'dagre',
      rankdir: 'TB', // 从上至下布局
      nodesep: 10,
      ranksep: 15,
      controlPoints: true,
    },
    defaultNode: {
      type: 'root-cause-node',
      size: NodeSize,
      anchorPoints: [[0.5, 0], [0.5, 1]]
    },
    defaultEdge: {
      type: 'root-cause-edge',
      style: {
        stroke: '#B5B7BB',
        lineWidth: 1,
        endArrow: {
          path: G6.Arrow.triangle(8, 8, 0),
          fill: '#B5B7BB',
        },
      }
    },
  }

  private graph: Graph|null = null;

  private source: TopoSource = {
    nodes: [],
    edges: []
  };

  private nodeQuery: string = ''

  private initChart (domId: string) {
    if (!domId) {
      return; 
    }
    registerNode(G6);
    registerEdge(G6);
    this.graph = new G6.Graph({
      ...this.defaultCfg,
      container: domId,
    });
    this.graph.data(this.source);
    this.graph.render();
    // 初始化，设置节点是否被选中
    this.graph!.getNodes().forEach((node: INode) => {
      const model: any = node.getModel();
      this.graph!.setItemState(node, 'nodeChoosed', model.choosed);
    });

    // 注册点击事件
    this.graph.on('canvas:click', (evt) => {
      EventBus.$emit('root-cause-node-click', null);
    });
    this.graph.on('node:click', (evt) => {
      const nodeItem = evt.item as INode;
      if (!nodeItem) {
        EventBus.$emit('root-cause-node-click', null);
        return
      }
      // 触发 EventBus root-cause-node-click 事件
      const model = nodeItem.getModel();
      const nodeItemId = nodeItem.get('id');
      const _graph = evt.currentTarget;
      _graph.getNodes().forEach((node: INode) => {
        node.setState('nodeChoosed', node.getModel().id === nodeItemId);
      });
      EventBus.$emit('root-cause-node-click', { ...model });
    });
    // 注册鼠标移入/移出事件
    this.graph.on('node:mouseenter', (evt) => {
      const nodeItem = evt.item
      const _graph = evt.currentTarget
      if (!nodeItem || !_graph) {
        return
      }
      // 查找有连线的节点
      const fullNeighbors = _graph.getNeighbors(nodeItem)
      const activeNodeIds = fullNeighbors.filter((i: any) => i.get('type') === 'node').map((i: any) => i.get('id'))
      activeNodeIds.push(nodeItem.get('id'))
      _graph.getNodes().forEach((node: INode) => {
        const isInclude = activeNodeIds.includes(node.getModel().id)
        node.setState('inactive', !isInclude)
      });
      _graph.getEdges().forEach((edge: IEdge) => {
        if (edge.getSource() === nodeItem || edge.getTarget() === nodeItem) {
          _graph.setItemState(edge, 'inactive', false);
        } else {
          _graph.setItemState(edge, 'inactive', true);
        }
      });
    });
    this.graph.on('node:mouseleave', (evt) => {
      const nodeItem = evt.item
      const _graph = evt.currentTarget
      if (!nodeItem) {
        return
      }
      _graph.getNodes().forEach((node: INode) => {
        node.clearStates(['inactive']);
      });
      _graph.getEdges().forEach((node: INode) => {
        node.clearStates(['inactive']);
      });

      // 还原搜索状态
      if (this.nodeQuery) {
        this.filterNode(this.nodeQuery);
      }
    });
  }

  public updateChart (source: TopoSource, theme?: ChartTheme, width?: number, height?: number) {
    this.graph!.setAutoPaint(false);
    if (theme && theme !== this.theme) {
      this.theme = theme || this.theme;
    }
    this.formatSource(deepClone(source));
    this.graph!.changeData(this.source);
    if (width && height) {
      this.graph!.changeSize(width, height);
    }
    this.graph!.paint();
    this.graph!.setAutoPaint(true);
    // 初始化，设置节点是否被选中
    this.graph!.getNodes().forEach((node: INode) => {
      const model: any = node.getModel();
      this.graph!.setItemState(node, 'nodeChoosed', model.choosed);
    });

    setTimeout(() => {
      this.graph!.fitCenter();
    }, 300)
  }

  public resize (width: number, height: number) {
    if (this.graph) {
      this.graph.changeSize(width, height);
      this.graph!.fitCenter();
    }
  }

  private formatSource (source: TopoSource) {
    const themeColor = colors[this.theme];
    source.nodes.forEach((node) => {
      node.theme = this.theme
    })
    source.edges.forEach((edge) => {
      edge.theme = this.theme
      edge.style = {
        ...edge.style,
        stroke: themeColor.line,
        endArrow: {
          fill: themeColor.line,
        },
      }
    })
    this.source = source
  }

  public filterNode (query: string) {
    const _query = (query || '').toLocaleLowerCase();
    this.nodeQuery = _query;
    this.graph!.setAutoPaint(false);
    this.graph!.getNodes().forEach((node: INode) => {
      if (this.nodeQuery) {
        const model: any = node.getModel();
        const _name = (model.serviceName || '').toLocaleLowerCase();
        this.graph!.setItemState(node, 'inactive', _name.indexOf(_query) === -1);
      } else {
        this.graph!.setItemState(node, 'inactive', false);
      }
    });
    this.graph!.getEdges().forEach((edge: IEdge) => {
      this.graph!.setItemState(edge, 'inactive', !!this.nodeQuery);
    });
    this.graph!.paint();
    this.graph!.setAutoPaint(true);
  }

  public destroy () {
    this.graph?.destroy();
    this.graph = null;
  }
}
