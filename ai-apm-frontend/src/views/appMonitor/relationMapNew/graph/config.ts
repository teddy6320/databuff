import { GraphOptions, positionOf } from "g6-v5";
import type { CanvasEvent, EdgeData, IElementDragEvent, IElementEvent, NodeData } from "g6-v5";

export const NODE_SIZE = 46;
export const NODE_BOUND_SIZE = 50;

export enum PluginNames {
  MINIMAP = 'custom-minimap',
  TOOLTIP = 'custom-tooltip',
  GRIDLINE = 'custom-gridline',
  SNAPLINE = 'custom-snapline',
}


export interface LayoutNodePosition {
  id: string;
  name: string;
  x: number;
  y: number;
}

export interface LayoutInfo {
  id: string | number;
  type: APM.RelationMap['relationType'];
  layoutData: LayoutNodePosition[];
  config: {
    lineType: string;
    nodeMaxCount?: {
      [key in Exclude<APM.RelationMap['relationType'], 'business'>]: number;
    }
  },
  version?: string;
}

export const defaultOption: GraphOptions = {
  autoFit: 'center',
  autoResize: true,
  background: '#fff',
  zoomRange: [0.4, 6],
  animation: false,
  node: {
    type: 'circle',
    style: {
      fill: (d) => d?.style?.fill || '#F5F6F7',
      stroke: (d) => d?.style?.stroke || '#B5B7BB',
      lineWidth: 1.2,
      size: NODE_SIZE,
      iconFontFamily: 'db-icon',
      label: (d) => d?.style?.label || false,
      labelFill: '#121317',
      labelTextAlign: 'center',
      labelFontSize: 10,
      labelWordWrap: true,
      labelMaxWidth: NODE_SIZE * 2,
      labelTextOverflow: 'ellipsis',
      iconFill: (d) => d?.style?.iconFill || '#121317',
      ports: (d) => d?.style?.ports || []
    },
    state: {
      active: {
        stroke: '#2962FF',
        lineWidth: 1.2,
        haloLineWidth: 10,
        label: true,
      },
      selected: {
        stroke: '#2962FF',
        fill: (d) => d?.data?.selectedfill as string || '#2962FF',
        lineWidth: 1.2,
        iconFill: '#fff',
        haloLineWidth: 10,
        label: true,
        labelFontSize: 10,
        labelWordWrap: false,
      },
      neighborActive: {
        label: true,
        stroke: '#2962FF',
        halo: true,
        haloLineWidth: 10,
        haloStroke: '#2962FF',
      }
    },
  },
  edge: {
    type: (d) => d?.type || 'quadratic',
    // type: 'cubic-horizontal',
    style: {
      stroke: (d) => d?.style?.stroke || '#B5B7BB',
      curveOffset: 10,

      // label样式
      labelAutoRotate: false,
      labelFill: 'transparent',
      labelFontSize: 8,
      labelTextAlign: 'center',
      labelTextBaseline: 'middle',

      // 箭头样式
      endArrow: (d) =>  d?.style?.endArrow ?? true,
      endArrowType: 'triangle',
      endArrowSize: (d) => d?.style?.endArrowSize ?? 5,

      // 交互样式
      halo: false,
      cursor: 'pointer',
      increasedLineWidthForHitTesting: 6, // 交互区域增大
    },
    state: {
      active: {
        stroke: '#2962FF',
        halo: false,
        labelFill: '#45474A',
      },
      selected: {
        stroke: '#2962FF',
        lineWidth: (d) => Number(d?.style?.lineWidth) || 1,
        halo: false,
        labelFontSize: 8,
        labelFontWeight: 'normal',
        labelFill: '#45474A',
      },
      neighborActive: {
        stroke: '#2962FF',
        lineWidth: (d) => Number(d?.style?.lineWidth) || 1,
        labelFill: '#45474A',
      },
    }
  },
  behaviors: [
    'drag-canvas',
    'zoom-canvas',
    {
      type: 'drag-element', // 自定义配置拖拽元素
      key: 'drag-node-only',
      enable: (event: IElementDragEvent) => event.targetType === 'node', // 只允许拖拽节点
    },
    {
      key: 'hover-activate',
      type: 'hover-activate',
      direction: 'both',
      degree: 1,
      state: 'active',
    },
    {
      type: 'click-select',
      key: 'click-select-1',
      degree: 1, // 选中扩散范围
      state: 'selected', // 选中的状态
      neighborState: 'neighborActive', // 相邻节点附着状态
    },
  ],
  plugins: [
    {
      type: 'tooltip',
      key: PluginNames.TOOLTIP,
      position: 'top-right',
      offset: [30, 0],
      enterable: false,
      style: {
        '.tooltip': {
          // background: '#2962ff',
          borderRadius: '6px',
          padding: '0',
          fontSize: '14px',
          fontFamily: 'Arial, sans-serif',
          boxShadow: '0 4px 20px rgba(0,0,0,0.3)',
          overflow: 'hidden',
        },
        '.tooltip-header': {
          background: '#2962ff',
          padding: '8px 12px',
          // 'border-radius': '6px',
          'font-weight': '500',
          'font-size': '15px',
          color: '#fff',
        }
      },
      getContent: (e: IElementEvent, items: NodeData[] | EdgeData[]) => {
        const item = items[0];
        if (e.targetType === 'node') {
          return `<div>
            <div class="tooltip-header">${item?.data?.label}</div>
          </div>`;
        } else if (e.targetType === 'edge') {
          return `<div>
            <div class="tooltip-header">${item?.data?.sourceName}  →  ${item?.data?.targetName}</div>
            <div class="tooltip-body" style="padding:8px 12px; white-space: pre-wrap;">${item?.data?.fullInfo || ''}</div>
          </div>`;
        }
      },
    },
    {
      type: 'minimap',
      key: PluginNames.MINIMAP,
      size: [200, 140],
      className: 'custom-minimap',
      containerStyle: {
        position: 'fixed',
        right: 0,
        bottom: 0,
        left: 'auto',
        top: 'auto',
        border: '1px solid rgb(221, 221, 221)',
        backgroundColor: '#fff',
      }
    },
  ],
}

export const offscreenGraphOptions: GraphOptions = {
  animation: false,
  node: {
    type: 'circle',
    style: {
      size: NODE_SIZE,
    },
  },
  width: 1440,
  height: 1440,
  layout: {
    type: 'force-atlas2',
    preventOverlap: true,
    nodeSize: NODE_SIZE,
    maxIteration: 100,
    center: [0, 0],
    kr: 10,
    kg: 8,
    ks: 1.5,
  },
}

export const editModeGraphOptions: GraphOptions = {
  autoFit: undefined,
  autoResize: true,
  zoomRange: [0.4, 6],
  background: 'transparent',
  animation: {
    duration: 300,
  },
  node: {
    type: 'circle',
    style: {
      fill: (d) => d?.style?.fill || '#F5F6F7',
      stroke: (d) => d?.style?.stroke || '#B5B7BB',
      lineWidth: 1.2,
      size: NODE_SIZE,
      iconFontFamily: 'db-icon',
      iconFill: (d) => d?.style?.iconFill || '#121317',
      // label: (d) => !!(d?.data?.showLabel),
      labelFill: '#121317',
      labelTextAlign: 'center',
      labelFontSize: 10,
      labelWordWrap: true,
      labelMaxWidth: NODE_SIZE * 2,
      labelTextOverflow: 'ellipsis',
      ports: (d) => d?.style?.ports || [
        // { key: 'port-left', placement: 'left' },
        // { key: 'port-right', placement: 'right' },
      ]
    },
    state: {
      active: {
        stroke: '#2962FF',
        lineWidth: 1.2,
        haloLineWidth: 10,
        label: true,
      },
      selected: {
        stroke: '#2962FF',
        fill: '#2962FF',
        lineWidth: 1.2,
        iconFill: '#fff',
        haloLineWidth: 10,
        label: true,
        labelFontSize: 10,
        labelWordWrap: false,
        
      },
    },
  },
  edge: {
    type: (d) => d?.type || 'quadratic',
    // type: 'cubic-horizontal',
    style: {
      stroke: (d) => d?.style?.stroke || '#B5B7BB',
      curveOffset: 10,
      // label样式
      labelAutoRotate: false,
      labelFill: 'transparent',
      labelFontSize: 8,
      labelTextAlign: 'center',
      labelTextBaseline: 'middle',

      // 箭头样式
      endArrow: true,
      endArrowType: 'triangle',
      endArrowSize: 5,

      // 交互样式
      halo: false,
    },
    state: {
      active: {
        halo: false,
        lineWidth: 1,
      },
      selected: {
        halo: false,
        lineWidth: 1,
      },
    }
  },
  behaviors: [
    'drag-canvas',
    'zoom-canvas',
    {
      type: 'drag-element', // 自定义配置拖拽元素
      key: 'drag-node-only',
      enable: (event: IElementDragEvent) => event.targetType === 'node', // 只允许拖拽节点
    },
    {
      key: 'hover-activate',
      type: 'hover-activate',
      direction: 'both',
      degree: 0,
      state: 'active',
    },
    {
      type: 'click-select',
      key: 'click-select-1',
      degree: 0, // 选中扩散范围
      state: 'selected', // 选中的状态
    },
  ],
  plugins: [
    {
      type: 'grid-line',
      key: PluginNames.GRIDLINE, // 指定唯一标识符
      stroke: '#1890ff33', // 蓝色半透明网格线
      lineWidth: 1,
      size: 50, // 更大的网格
      // borderStroke: '#1890ff', // 蓝色边框
      borderLineWidth: 1,
      follow: true
    },
    {
      type: 'snapline',
      key: PluginNames.SNAPLINE, // 指定唯一标识符
      tolerance: 10, // 对齐吸附阈值
      offset: 30, // 对齐线延伸距离
      autoSnap: true, // 启用自动吸附
    },
    {
      type: 'minimap',
      key: PluginNames.MINIMAP,
      size: [200, 140],
    },
  ]
}
