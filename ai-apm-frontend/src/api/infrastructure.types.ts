export interface ScrollId {
  scrollId: string;
}

export interface HostmapInterface {
  fromTime: string;
  toTime: string;
  hostName?: string;
  containerName?: string;
  processName?: string;
  group?: string[];
}

export interface ListInterface extends HostmapInterface {
  offset: number;
  size: number;
}

export interface HostnameInterface {
  hostName: string;
}

export interface GroupInterface {
  type: string;
  startTime: string;
  endTime: string;
}

export interface CustomHostTag {
  id: string;
  tags: string[];
}

export interface GetHostTag {
  startTime: string;
  endTime: string;
}

export interface ProcessGroupList {
  offset: number;
  size: number;
  fromTime: string;
  toTime: string;
  hostName?: string;
  processName?: string;
  processType?: string;
  isFuzzy?: number;
}

export interface CustomProcessGroup {
  childs: string[];
  pname: string;
  hostName: string[];
  memory: any;
  cpu: any;
  ioStat: any;
}
