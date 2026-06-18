# Metric Cascader

## 组件说明

文档名里的 `metric-cascader` 对应实际源码 [src/components/metric-type-cascader.vue](/src/components/metric-type-cascader.vue)。它用于选择指标分类路径，本质上是一个带多级分类数据和自动加载能力的 `el-cascader`。

## 常用 Props

| Prop | 说明 |
|------|------|
| `value` | 当前选中的分类路径数组 |
| `options` | 外部传入分类树，存在时覆盖内部 Store 数据 |
| `awaitOption` | 是否等待外部异步加载分类数据 |
| `autoHidden` | 选中叶子节点后是否自动收起面板 |
| `filterable` | 是否允许搜索 |
| `showAllLevels` | 是否展示完整层级 |
| `disabled` | 是否禁用 |
| `props` | 透传给 `el-cascader` 的额外配置 |

## 数据来源

- 默认情况下，组件会在 `created` 时触发 `Common/GET_METRIC_TYPES`
- 如果 `options` 已传入，则直接使用外部数据

## 特殊行为

- 内部默认开启 `checkStrictly: true`
- 如果传入的是原始分类结构而不是标准 `label/value` 结构，会自动走 `formatMetricTypeData`
- 选中叶子节点后，可自动关闭下拉面板

## 输出事件

| 事件 | 说明 |
|------|------|
| `change` | 路径变化 |
| `visible-change` | 面板开关变化 |

## 使用建议

- 需要依赖平台统一指标分类树时优先使用它
- 如果页面分类数据完全独立，且不想依赖 `Common` store，可以直接传 `options`
