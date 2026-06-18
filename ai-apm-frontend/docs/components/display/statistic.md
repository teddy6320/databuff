# Statistic

## 组件说明

`Statistic` 是一个轻量统计值展示组件，源码在 [src/components/statistic/index.vue](/src/components/statistic/index.vue)。它用于在卡片、概览区里统一展示“标题 + 数值 + 前后缀”。

## 常用 Props

| Prop | 说明 |
|------|------|
| `title` | 标题 |
| `describe` | 标题后的 tooltip 文案 |
| `value` | 原始值 |
| `formatType` | `number` / `date` / `ns` / `ms` / `s` |
| `prefix` | 值前缀 |
| `suffix` | 值后缀 |

## 插槽

| 插槽 | 说明 |
|------|------|
| `title` | 自定义标题区 |
| `prefix` | 自定义前缀区 |
| `suffix` | 自定义后缀区 |

## 特殊行为

- 会根据 `formatType` 调用项目内已有 filter 进行格式化
- 标题支持直接带 tooltip 问号提示
- 适合做居中、卡片化的指标值展示

## 使用建议

- 需要统一风格的小型统计卡片时使用它
- 如果页面已经有复杂布局或需要图文混排，通常直接在页面里手写更灵活
