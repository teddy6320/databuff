export interface TimeRange {
  startTime: Date | string;
  endTime: Date | string;
}

export interface MetricChartParams {
  query: any;
  start: number;
  end: number;
  interval: number;
}
export interface MetricChartCoreParams {
  groups: string[];
  tagList: string[];
  aggregation: string;
  interval: number;
  startTime: Date | string;
  endTime: Date | string;
}

export interface MetricInfoConfig {
  metric: string;
  unit: string;
  type: string;
  interval: number;
  describe: string;
}

export interface MutualInfoParams {
  metric: string;
  startTime: string;
}
