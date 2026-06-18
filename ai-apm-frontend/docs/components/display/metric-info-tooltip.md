# Metric Info Tooltip

## 组件说明

`MetricInfoTooltip` 是指标详情浮层内容组件，源码在 [src/components/metric-info-tooltip.vue](/src/components/metric-info-tooltip.vue)。它本身不负责浮层定位，更像是“指标详情卡片内容”。

## 常用 Props

| Prop | 说明 |
|------|------|
| `detail` | 指标详情对象 |
| `tooltip` | 是否启用浮层样式类 |

## 展示字段

组件会按固定结构展示：

- 指标名称
- 指标中文名
- 指标描述
- 分类
- 是否启用
- 单位

## 特殊行为

- 分类优先读取 `detail._types`
- 如果没有 `_types`，会回退拼接 `type1/type2/type3`
- 组件额外定义了 `.el-popover.metric-info-popper` 的样式兼容

## 典型使用

当前主要配合 [Metric Select](../form/metric-select.md) 使用，在指标 hover 时展示详情。

## 使用建议

- 如果需要统一的指标说明卡片，优先复用它
- 如果页面需要真正的交互式 Popover，外层仍然需要自己处理定位和显隐
