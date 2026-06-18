import {
  DataTypeList,
  SourceTypeList,
  ProcessorTypeList,
} from '@/utils/static/dataAccess'

/**
 * 数据类型
 * @param {type}
 * @return {typeCn}
 */
export const DataTypeFilter = (value: string) => {
  return DataTypeList.find(t => t.value === value)?.label ?? value ?? '-'
}

/**
 * 处理器/数据源类型
 * @param {type}
 * @return {typeCn}
 */
export const ProcessorTypeFilter = (value: string) => {
  const SourceTypeItem = SourceTypeList.find(t => t.value === value)
  if (SourceTypeItem) {
    return SourceTypeItem.label
  }
  return ProcessorTypeList.find(t => t.value === value)?.label ?? value ?? '-'
}
