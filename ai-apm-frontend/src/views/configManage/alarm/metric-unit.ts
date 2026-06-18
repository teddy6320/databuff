/** avgDuration 查询结果为毫秒，兼容旧元数据中的 ns 单位 */
export const resolveAvgDurationUnit = (metric: string, unit: string) => {
  if (metric?.endsWith('.avgDuration') && unit === 'ns') {
    return 'ms'
  }
  return unit || ''
}
