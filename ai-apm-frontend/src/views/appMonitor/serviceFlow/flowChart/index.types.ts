import { TreeGraphData } from '@antv/g6';
export type RecordType = Record<string, any>;

export const FLOW_NODE = {
  name: 'flow-card-node',
  rootName: 'flow-card-root-node',
  box: 'flow-card-box',
  header: {
    box: 'flow-card-box-header',
    title: 'flow-card-box-header-title',
    titleIcon: 'flow-card-box-header-title-icon',
    filterIcon: 'flow-card-box-header-filter-icon',
    toggleIcon: 'flow-card-box-header-toggle-icon',
  },
  action: {
    expandIcon: 'flow-card-box-header-expand-icon'
  },
  viewData: {
    conbTitle: 'flow-card-box-conb-title',
    conbValue: 'flow-card-box-conb-value',
    percentBg: 'flow-card-box-percent-bg',
    percentBar: 'flow-card-box-percent-bar',
    responseTitle: 'flow-card-box-response-title',
    responseValue: 'flow-card-box-response-value',
    requestTitle: 'flow-card-box-request-title',
    requestValue: 'flow-card-box-request-value',
  }
}
export const FLOW_EDGE = {
  name: 'flow-card-edge',
  viewData: {
    request: 'flow-card-edge-request',
    avgCall: 'flow-card-edge-avg-call',
  }
}

export interface FlowChartProp {
  domId: string; // 挂载dom
  source: TreeSource;
  width?: number;
  height?: number;
  viewModel?: 'top'|'compact';
}

export interface DataSource extends RecordType {
  service?: string;
  children?: DataSource[];
};

export interface TreeSource extends TreeGraphData {
  id: string;
  key: string;
  name: string;
  customShow: false,
  viewData: {
    reqCnt: number; // 请求数
    response: number; // 平均响应时间
    contribution: number; // 响应贡献度
    failCnt: number; // 失败请求数
    outCnt: number; // 外发请求数
    hostIp: string; // 主机IP
    hostName: string; // 主机名称
    avgReq: number; // n次调用/请求
    callPct: number; // 请求占比
  };
  serviceInfo: {
    service: string; // 服务名称
    serviceId: string; // 服务id
    serviceType: string; // 服务类型
  };
  children?: TreeSource[];
  childrenInfo?: {
    count: number;
    first: string; // first children id
    remain: string[]; // remain children ids
    loaded: boolean; // 子节点是否加载完
  };
  loaded?: boolean; // 是否加载完
  chain?: string[]; // id[]
  [propName: string]: any;
}

export interface FlowNode extends TreeSource {
  [propName: string]: any;
}

export interface FlowEdge {
  source: string;
  target: string;
  [propName: string]: any;
}
