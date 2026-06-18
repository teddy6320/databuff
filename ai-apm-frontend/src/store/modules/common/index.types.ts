export interface MetricTypeRawItem {
  type1: string
  type2: string
  type3: string
  metricList: string[]
}

export interface CascaderOptionItem {
  label: string
  value: string
  leaf: boolean
  id?: string // 需要传入参数['id'], type的字符串化 '{"type1":"自监控","type2":"dts","type3":"CPU"}'
  level?: number // 需要传入参数 ['level']
  children?: CascaderOptionItem[] | null
}

export interface MetricInfoMap {
  [key: string]: {
    [v: string]: any
  }
}

export interface TagLabelItem {
  enabled: boolean
  name: string
  originName: string
  tagValue?: {
    [v: string]: string
  }
}

export interface TagLabelMap {
  [key: string]: TagLabelItem
}
