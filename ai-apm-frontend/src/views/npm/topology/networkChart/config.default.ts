import G6 from '@antv/g6';
import { CustomShapeName } from './register'
import { TopoChartTheme } from './theme';

export default function getDefaultCfg () {
  return {
    width: 400,
    height: 400,
    groupByTypes: false,
    fitCenter: true,
    fitViewPadding: 30,
    animate: false,
    animateCfg: {
      duration: 300,
      easing: 'easePolyIn',
    },
    padding: [20, 50],
    defaultNode: {
      type: CustomShapeName.node.name,
      size: 28,
    },
    defaultEdge: {
      type: CustomShapeName.edge.name,
      style: {
        lineWidth: 1,
        opacity: 1,
        stroke: TopoChartTheme.light.edgeStroke,
        endArrow: {
          path: G6.Arrow.triangle(8, 8, 0),
          fill: TopoChartTheme.light.edgeStroke,
          stroke: 'transparent',
          d: 2,
        },
      }
    },
    nodeStateStyles: {
      nodeActive: {
        [CustomShapeName.node.bgShape]: {
          stroke: TopoChartTheme.light.circleStrokeActive,
        },
      },
    },
    layout: {
      type: 'forceAtlas2',
      workerEnabled: true,
      // kr: 5,
      kg: 6,
      ks: 1.5,
      preventOverlap: true,
      nodeSize: 28,
      barnesHut: true,
      prune: false,
      maxIteration: 0,
    },
    modes: {
      default: ['drag-canvas', 'zoom-canvas', 'drag-node'],
      // 安静模式不允许拖动节点
      quiet: ['drag-canvas', 'zoom-canvas'],
      // 自定义大数据量模式
      optimize: [
        {
          type: 'drag-canvas',
          enableOptimize: true
        },
        {
          type: 'zoom-canvas',
          enableOptimize: true
        },
        {
          type: 'drag-node',
          enableOptimize: true
        },
      ]
    },
  }
}
