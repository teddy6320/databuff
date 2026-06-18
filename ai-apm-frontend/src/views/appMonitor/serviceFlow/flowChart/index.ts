import G6, { Graph, GraphAnimateConfig, IEdge, INode, NodeConfig } from '@antv/g6';
import { LooseObject } from '@antv/g-base';
import deepClone from 'lodash/cloneDeep';
import { EventBus } from '@/utils/common';
import { registerFlowNodeCard, registerFlowRootNodeCard, registerFlowEdge } from './register';
import getDefaultCfg from './config.default';
import * as types from './index.types'

export class FlowChart {
  constructor (cfg: types.FlowChartProp) {
    const { source, domId, width, height, viewModel } = cfg;
    if (source && Object.keys(source).length > 0) {
      this.chartCfg = { ...getDefaultCfg() };
      if (width) {
        this.chartCfg.width = width;
      }
      if (height) {
        this.chartCfg.height = height;
      }
      if (viewModel) {
        this.viewModel = viewModel;
      }
      // 注册G6自定义节点
      registerFlowNodeCard(G6);
      registerFlowRootNodeCard(G6);
      registerFlowEdge(G6);
      this.originSource = deepClone(source);
      this.formatSource(deepClone(source));
      this.initChart(domId);
    }
  }

  private chartCfg: any = {};

  private graph: Graph|null = null;

  private viewModel: 'top'|'compact' = 'top';
  private optimizeModel = false;

  private topConfig = {
    top: 3,
  }

  // 记录展开节点的子元素数据的映射
  private topRecordMapping: Record<string, types.TreeSource[]> = {};
  // 记录已全部展开子集的节点
  private topFullExpanedRecord: Record<string, boolean> = {};

  private originSource: types.TreeSource = {} as types.TreeSource;
  // 节点数据，嵌套结构，children为子集
  private source: types.TreeSource = {} as types.TreeSource;
  private sourceMapping: Record<string, types.TreeSource> = {};

  // highlight 相关
  private highlightInfo: { nodes: INode[], edges: IEdge[] } = {
    nodes: [],
    edges: [],
  }

  // 初始化数据源
  private formatSource (source: types.TreeSource) {
    // 为每个节点创建链路信息：chain: [id, id]
    // 为每个父级节点创建子元素数量信息：childInfo: { count: number }
    let nodeCount = 0;
    const deepFormatChildren = (parent: types.TreeSource) => {
      const { children } = parent;
      if (children && children.length) {
        children.sort((a, b) => b.durationCvPct - a.durationCvPct);
        const [first, ...remain] = children
        nodeCount += children.length
        parent.childrenInfo = {
          count: children.length,
          first: first.id,
          remain: remain.map((i) => i.id),
          loaded: false
        }
        children.forEach((item) => {
          item.chain = parent.chain ? [...parent.chain, item.id] : [item.id];
          deepFormatChildren(item);
        });
      }
      const cloneWithChildren = deepClone(parent);
      delete cloneWithChildren.children
      this.sourceMapping[cloneWithChildren.id] = cloneWithChildren;
    }

    if (source && source.children) {
      nodeCount += 1;
      deepFormatChildren(source);
    }
    this.optimizeModel = nodeCount > 500;
    source.type = types.FLOW_NODE.rootName;

    // 判断是top模式还是折叠模式
    if (this.viewModel === 'top') {
      this.formatTopSource(source);
    } else if (this.viewModel === 'compact') {
      this.formatCompactSource(source);
    }
  }

  // top模式，保留Top N的全链路展示
  private formatTopSource (source: types.TreeSource) {

    // 遍历获取末端节点，
    const _topEndpoints: types.FlowNode[] = [];
    const deepGetEndpoint = (_source: types.TreeSource, _container: types.FlowNode[]) => {
      if (_source.children && _source.children.length) {
        _source.children.forEach((child) => {
          deepGetEndpoint(child, _container)
        })
      } else {
        _container.push(deepClone(_source))
      }
    }
    deepGetEndpoint(source, _topEndpoints);
    _topEndpoints.sort((a, b) => b.viewData.contribution - a.viewData.contribution);
    // 放在此处做判断，如果是top模式，则截取，如果是compact模式，则不截取
    const topN = _topEndpoints.splice(0, this.topConfig.top);
    // 只按照 item[0] 的贡献度大小进行排序
    topN.sort((a, b) => this.sourceMapping[b!.chain![0]].viewData.contribution - this.sourceMapping[a!.chain![0]].viewData.contribution)
    // console.log(topN.map(i => i.chain))
    // 组装source
    // 根据 topN 中的末端阶段倒推链路 - chain
    // chain 中可能存在一条链路的分支，
    //                  /--- top 1
    //        parent  -<
    //                  \--- top 2
    // 需要处理
    const chainsNodes = topN.filter((i) => i.chain);
    
    const manualSource: types.TreeSource = {
      ...deepClone(source)
    };
    delete manualSource.children;
    
    // 手动组装tree
    const deepManualSource = (parent: types.FlowNode, depth: number, chainInfo: string[]) => {
      const currDepthId = chainInfo[depth];
      let current = { ...this.sourceMapping[currDepthId] };
      if (currDepthId && current && current.id) {
        if (!parent.children) {
          parent.children = [current]
        } else {
          // 需要判断是否已经创建过该节点
          const sameIndex = parent.children.findIndex(i => i.id === current.id);
          const hasSame = sameIndex > -1;
          if (!hasSame) {
            parent.children.push(current)
          } else {
            current = parent.children[sameIndex]
          }
        }
        if (parent.children && parent.childrenInfo && parent.children.length === parent.childrenInfo.count) {
          parent.childrenInfo.loaded = true;
        }
      }
      if (currDepthId && depth < chainInfo.length - 1) {
        deepManualSource(current, depth + 1, chainInfo)
      }
    }
    
    if (chainsNodes.length && source.children) {
      chainsNodes.forEach((chainNode) => {
        deepManualSource(manualSource, 0, chainNode.chain!);
      })
    }
    if (!Object.keys(manualSource).length) {
      manualSource.id = ''
      manualSource.key = ''
    }
    // console.log('manualSource', manualSource)
    this.source = manualSource
  }

  // 聚合模式，每条链路只展示一个子节点
  private formatCompactSource (source: types.TreeSource) {
    // 需要跟top模式一样处理数据
    // 第二层级节点超过5个的话，只展示前四个
    // 从第三层级开始只展示响应贡献度最大的一条链路
    const cloneSource = deepClone(source);
    if (cloneSource.children && cloneSource.children.length > 5) {
      cloneSource.children = cloneSource.children.splice(0, 4);
    } else {
      cloneSource.childrenInfo && (cloneSource.childrenInfo.loaded = true);
    }
    const deepCompactSource = (parent: types.TreeSource) => {
      if (parent.children && parent.children.length) {
        parent.children.forEach((child) => {
          if (child.children && child.children.length) {
            if (child.children.length === 1 && child.childrenInfo) {
              child.childrenInfo.loaded = true;
            }
            child.children = child.children.splice(0, 1);
            deepCompactSource(child)
          }
        })
      }
    }
    deepCompactSource(cloneSource);
    // console.log('compactSource', cloneSource)
    this.source = cloneSource
  }

  // 切换模式
  public toggleViewMode (mode?: 'top'|'compact') {
    if (mode) {
      this.viewModel = mode;
    } else {
      this.viewModel = this.viewModel === 'top' ? 'compact' : 'top';
    }
    this.formatSource(deepClone(this.originSource));
    if (this.graph) {
      this.openOptimizeMode();
      this.graph.changeData(this.source);
      this.fitToFirstItem(false);
    }
  }

  // top模式下展开下一级
  private topModeShowNextOnce = (item: INode, currentTarget: LooseObject) => {
    // 根据当前节点的 childrenInfo
    // fullChildId = [childrenInfo.first, ...childrenInfo.remain]
    // 组装当前节点下的全部子元素
    // 需要过滤已经存在的子元素
    const itemModel = item.getModel() as NodeConfig;
    if (!itemModel || !itemModel.id) {
      return
    }
    const childrenInfo = itemModel.childrenInfo as types.TreeSource['childrenInfo'];
    // 末端节点没有childrenInfo
    if (!childrenInfo || !this.graph) {
      return
    }
    const targets = item.getNeighbors('target');
    const targetsIds = targets.map((n: any) => n.getModel().id);
    const childIds = [childrenInfo.first, ...childrenInfo.remain];
    const unRenderIds = childIds.filter((n) => !targetsIds.includes(n));
    if (unRenderIds && unRenderIds.length) {
      // 判断是否已有 recordMapping
      if (Object.prototype.hasOwnProperty.call(this.topRecordMapping, itemModel.id)) {
        currentTarget.updateItem(item, {
          children: this.topRecordMapping[itemModel.id],
          childrenInfo: {
            ...childrenInfo,
            loaded: true
          }
        })
      } else {
        const unRenderItems = unRenderIds.map((id) => {
          return deepClone(this.sourceMapping[id])
        });
        currentTarget.updateItem(item, {
          children: (itemModel.children as any[] || []).concat(unRenderItems),
          childrenInfo: {
            ...childrenInfo,
            loaded: true
          }
        })
      }
      item.setState('loaded', true);
      currentTarget.layout(false);
    } else {
      // 如果全部展开，则隐藏全部子集
      // this.topModeHideChild(item, currentTarget);
      // 将节点中的childrenInfo.loaded置为true
      
    }
  }

  // 隐藏节点全部子集
  // private topModeHideChild = (item: INode, currentTarget: LooseObject) => {
  //   // TODO 收起子节点
  //   // TODO 子节点需要记录已展开的状态，否则下次展开会丢失子节点的children层级
  //   const itemModel = item.getModel() as NodeConfig;
  //   if (!itemModel || !itemModel.id) {
  //     return
  //   }
  //   let _children: types.TreeSource[] = [];
  //   G6.Util.traverseTree(itemModel, (child: any) => {
  //     if (child.id === itemModel.id) {
  //       if (itemModel && itemModel.children) {
  //         _children = deepClone(itemModel.children) as types.TreeSource[];
  //         this.graph!.updateItem(item, {
  //           children: []
  //         })
  //         currentTarget.layout(false);
  //         this.topRecordMapping[itemModel.id!] = _children;
  //         return false
  //       }
  //     }
  //   });
  // }

  // top模式下切换展开状态
  // private topModeToggleChild = (item: INode, currentTarget: LooseObject, status: boolean) => {
  //   if (status) {
  //     this.topModeShowNextOnce(item, currentTarget);
  //   } else {
  //     this.topModeHideChild(item, currentTarget);
  //   }
  // }

  // top模式下展开全部子集
  private topModeShowNextDeep = (item: INode, currentTarget: LooseObject) => {
    // 通过G6.Util.traverseTree遍历this.originSource
    // 找到item后，更新对应的子元素
    const itemModel = item.getModel() as NodeConfig;
    if (!itemModel || !itemModel.id) {
      return
    }
    // TODO 需要把子集的loaded也置为true
    // 查找当前节点下的子集中含有children的节点
    const nodeIds: string[] = []

    G6.Util.traverseTree(itemModel, (child: types.TreeSource) => {
      if (child && child.childrenInfo && !child.loaded && typeof child.loaded === 'undefined') {
        nodeIds.push(child.id)
      }
    });
    const nodes = nodeIds.map((id) => currentTarget.findById(id))
    nodes.forEach((i) => {
      i.setState('loaded', true);
      this.graph!.updateItem(i, {
        childrenInfo: {
          ...i.getModel().childrenInfo,
          loaded: true
        },
        loaded: true,
      })
    });
    let _children: types.TreeSource[] = [];
    G6.Util.traverseTree(this.originSource, (child: any) => {
      if (child.id === itemModel.id) {
        if (child && child.children) {
          _children = deepClone(child.children) as types.TreeSource[];
          return false
        }
      }
    });
    const _childrenObj = {
      children: _children,
    }
    G6.Util.traverseTree(_childrenObj, (child: any) => {
      if (child.children && child.children.length) {
        const [first, ...remain] = child.children
        child.childrenInfo = {
          loaded: true,
          count: child.children.length,
          first: first.id,
          remain: remain.map((n: any) => n.id)
        }
        child.loaded = true;
      }
    });
    currentTarget.updateItem(item, {
      children: _childrenObj.children,
    });
    
    this.topFullExpanedRecord[itemModel.id!] = true;
    currentTarget.layout(false);
  }
  // 切换成大数据量优化模式
  private openOptimizeMode () {
    if (this.graph && this.optimizeModel) {
      this.graph.setMode('optimize');
    }
  }

  // 从当前节点便利向上查找父节点
  private bubbleUpFindParent = (item: INode, currentTarget: LooseObject, nodes: INode[], edges: IEdge[]) => {
    const itemModel = item.getModel() as NodeConfig;
    if (!itemModel || !itemModel.id) {
      return
    }
    const sourceNodes = item.getNeighbors('source');
    const [parent] = sourceNodes;
    const sourceEdges = item.getInEdges();
    const [sourceEdge] = sourceEdges;

    if (parent) {
      nodes.push(parent);
      edges.push(sourceEdge);
      this.bubbleUpFindParent(parent, currentTarget, nodes, edges);
    }
  }

  private initChart (domId: string) {
    if (!domId) {
      return; 
    }
    const menu = new G6.Menu({
      className: 'custom-flow-contextmenu',
      getContent: (evt) => {
        const { item } = evt || {};
        if (item) {
          const itemModel = item.getModel() as types.TreeSource;
          // 没有子元素信息不展示右键菜单
          if (!itemModel.childrenInfo) {
            return ''
          }
          const { loaded } = itemModel.childrenInfo;
          const outDiv = document.createElement('div');
          outDiv.innerHTML = `<ul class='custom-flow-contextmenu-ul'>
            ${
              loaded ?
                `` :
                '<li class="custom-flow-contextmenu-item" data-key="0">展开下级</li>'
            }
            <li class="custom-flow-contextmenu-item" data-key="2">展开全部</li>
          </ul>`;
          return outDiv;
        } else {
          return ''
        }
      },
      handleMenuClick: (target, item) => {
        this.graph!.emit('canvas:click')
        const key = target.getAttribute('data-key');
        if (key === '0') {
          this.topModeShowNextOnce(item as INode, this.graph!);
        }
        // if (key === '1') {
        //   this.topModeHideChild(item as INode, this.graph!);
        // }
        if (key === '2') {
          this.topModeShowNextDeep(item as INode, this.graph!);
        }
      },
      itemTypes: ['node'],
      shouldBegin (evt) {
        const { item, shape } = evt || {};
        const shapeName = shape && shape.get('name');
        if (item && shapeName && shapeName === types.FLOW_NODE.action.expandIcon) {
          return !!(item.getModel() && item.getModel().childrenInfo);
        }
        return false;
      },
      trigger: 'click'
    });
    this.graph = new G6.TreeGraph({
      container: domId,
      ...this.chartCfg,
      plugins: [menu],
    });
    this.graph.data(this.source);

    // TODO: 关闭局部渲染，修复渲染残影的问题（临时方案）
    this.graph.get('canvas').set('localRefresh', false);
    this.graph.render();
    this.fitToFirstItem(false);
    this.openOptimizeMode();

    // 注册事件
    // this.graph.on('node:dblclick', (ev) => {
    //   // 被点击的节点元素
    //   const item = ev.item as INode;
    //   const currentTarget = ev.currentTarget
    //   // 被点击的图形，可根据该信息作出不同响应，以达到局部响应效果
    //   // const shape = ev.target;
    //   if (!item) {
    //     return
    //   }
    //   // this.topModeShowNextOnce(item, currentTarget);
    // });
    this.graph.on('node:click', (ev) => {
      // 被点击的节点元素
      const item = ev.item as INode;
      // 被点击的图形，可根据该信息作出不同响应，以达到局部响应效果
      const shape = ev.target;
      if (!item || !shape || item.getModel().type === types.FLOW_NODE.rootName) {
        return
      }
      if (shape.get('name') !== types.FLOW_NODE.action.expandIcon) {
        const itemModel = item.getModel();
        const currentTarget = ev.currentTarget
        // 判断是否为当前节点，节点必然是开头元素
        if (this.highlightInfo.nodes[0] && this.highlightInfo.nodes[0].getModel().id === itemModel.id) {
          return
        }
        this.highlightInfo.nodes = [item];
        this.highlightInfo.edges = [];
        this.bubbleUpFindParent(item, currentTarget, this.highlightInfo.nodes, this.highlightInfo.edges);
        this.graph!.setAutoPaint(false);
        const nodeIds = this.highlightInfo.nodes.map((node) => node.getModel().id)
        const edgeIds = this.highlightInfo.edges.map((edge) => edge.getModel().id)
        currentTarget.getNodes().forEach((node: INode) => {
          // 1 高亮当前节点
          // 2 根节点到当前节点之间的节点不变
          // 3 其他节点透明化
          node.setState('nodeActive', node.getModel().id === itemModel.id);
          node.setState('highlight', nodeIds.includes(node.getModel().id));
        });
        currentTarget.getEdges().forEach((edge: IEdge) => {
          // 1 高亮连线，并显示label
          // 2 其他连线透明化
          edge.setState('edgeActive', edgeIds.includes(edge.getModel().id));
          edge.setState('highlight', edgeIds.includes(edge.getModel().id));
        });
        this.graph!.paint();
        this.graph!.setAutoPaint(true);
        // 触发 EventBus 'flow-node-click' 事件
        EventBus.$emit('flow-node-click', Array.from(this.highlightInfo.nodes).map((node) => node.getModel()).reverse());
      // } else if (shape.get('name') === types.FLOW_NODE.header.filterIcon) {
      //   // 添加至筛选
      //   const nodes: INode[] = [item];
      //   const edges: IEdge[] = [];
      //   this.bubbleUpFindParent(item, currentTarget, nodes, edges);
      //   EventBus.$emit('flow-node-filter', Array.from(nodes).map((node) => node.getModel()).reverse());
      }
    });
    this.graph.on('canvas:click', (ev) => {
      this.clearStates(this.graph!);
      this.highlightInfo.nodes = []
      this.highlightInfo.edges = []
      // 触发 EventBus 'flow-node-click' 事件
      EventBus.$emit('flow-node-click', [deepClone(this.source)]);
    });
  }

  private clearStates (currentTarget: LooseObject) {
    currentTarget.getNodes().forEach((node: INode) => {
      node.clearStates(['nodeActive', 'highlight']);
      node.getContainer().attr('opacity', 1);
    });
    currentTarget.getEdges().forEach((edge: IEdge) => {
      edge.clearStates(['edgeActive', 'highlight']);
      edge.getKeyShape().attr('opacity', 1);
    });
  }

  public updateChart (source: types.TreeSource) {
    if (this.graph) {
      this.originSource = deepClone(source);
      this.formatSource(deepClone(source));
      this.openOptimizeMode();
      this.graph.changeData(this.source);
      this.fitToFirstItem(false);
    }
  }

  private fitToFirstItem (animate: boolean, aniCfg?: GraphAnimateConfig) {
    if (!this.graph) {
      return
    }

    // 1. 获取首个元素和图表整体信息
    const firstItem = this.graph.findById(this.source.id);
    if (!firstItem) {
      return;
    }

    // 获取首个元素的大小位置信息
    const bbox = firstItem.getBBox();
    const { x, y } = bbox;

    // 获取画布宽高
    const canvasWidth = this.graph.getWidth();
    const canvasHeight = this.graph.getHeight();

    const nodes = this.graph.getNodes();
    let minX = Infinity;
    let minY = Infinity;
    let maxX = -Infinity;
    let maxY = -Infinity;

    if (nodes.length > 0) {
      nodes.forEach((node) => {
        const nodeBBox = node.getBBox();
        minX = Math.min(minX, nodeBBox.minX);
        minY = Math.min(minY, nodeBBox.minY);
        maxX = Math.max(maxX, nodeBBox.maxX);
        maxY = Math.max(maxY, nodeBBox.maxY);
      });
    } else {
      return;
    }

    const fullGraphBBox = {
        x: minX,
        y: minY,
        width: maxX - minX,
        height: maxY - minY
    };
    const graphWidth = fullGraphBBox.width;

    // 2. 垂直居中计算 (第一个节点上下居中)
    const viewportCenterY = canvasHeight / 2;
    // 计算 Y 轴偏移量：视图中心 - (节点中心Y)
    const offsetY = viewportCenterY - (y + bbox.height / 2);

    // 3. 水平偏移计算 (三元逻辑)
    const PADDING_LEFT = 20;
    const PADDING_RIGHT = 20;
    const maxViewWidth = canvasWidth - PADDING_LEFT - PADDING_RIGHT;
    let offsetX = 0;

    if (graphWidth <= maxViewWidth) {
      // 情况 A: 图表元素总宽度没有超过视图宽度-40px，左右居中

      // 计算图表区域的中心点 X 坐标 (在图模型坐标系下)
      const graphCenterX = fullGraphBBox.x + fullGraphBBox.width / 2;
      // 计算视图的中心点 X 坐标 (在画布坐标系下)
      const viewportCenterX = canvasWidth / 2;

      // 计算 X 轴偏移量：视图中心 - 图表中心X
      offsetX = viewportCenterX - graphCenterX;
    } else {
      // 情况 B: 图表元素总宽度超过视图宽度-40px，第一个节点距离左侧 20px

      // 计算 X 轴偏移量：目标左侧位置 (20) - 节点左侧位置 (x)
      offsetX = PADDING_LEFT - x;
    }

    // 4. 执行平移
    if (animate) {
      this.graph.translate(offsetX, offsetY, animate, aniCfg);
    } else {
      this.graph.translate(offsetX, offsetY);
    }
  }

  public destroy () {
    this.graph?.destroy();
    this.graph = null;
  }
};
