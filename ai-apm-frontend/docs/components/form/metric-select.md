# Metric Select

## 组件说明

`MetricSelect` 是指标选择器组件，源码在 [src/components/metric-select.vue](/src/components/metric-select.vue)。它基于 [Scroll Select](scroll-select.md) 和 [Metric Info Tooltip](../display/metric-info-tooltip.md) 组合而成，除了下拉选择，还能在 hover 时异步展示指标详情。

## 常用 Props

| Prop | 说明 |
|------|------|
| `value` | 当前指标值，支持单选或多选 |
| `options` | 指标选项列表 |
| `multiple` | 是否多选 |
| `placeholder` | 占位文案 |
| `clearable` | 是否可清空 |
| `filterable` | 是否允许搜索 |
| `disabled` | 是否禁用 |
| `loading` | 是否显示整体加载中 |
| `tooltip` | 是否启用 hover 指标详情 |

## 关键事件

| 事件 | 说明 |
|------|------|
| `change` | 选中值变化 |
| `remove-tag` | 多选删除某项 |
| `visible-change` | 下拉开关变化 |
| `metric-info-change` | 指标详情缓存变化 |

## 特殊行为

- 组件使用 `change` 作为 `v-model` 的回写事件
- hover 某个指标超过 300ms 后，会通过 `Common/GET_METRIC_INFOS` 拉指标详情
- 指标 tooltip 的位置会根据视口宽高自动调整
- 如果 `MetricSelect` 外层值更新但内部 `ScrollSelect` 没同步，源码注释建议外层给它加动态 `key`

## 典型使用

当前常见于：

- 指标分析页查询条件
- 健康度配置
- 主机地图指标切换
- 规则配置里的指标选择

## 使用建议

- 需要“选指标 + 看指标定义”时优先使用它
- 如果只是纯下拉选择，不需要 hover 详情，直接使用 [Scroll Select](scroll-select.md) 更轻
