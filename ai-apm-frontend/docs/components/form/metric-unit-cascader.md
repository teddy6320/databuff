# Metric Unit Cascader

## 组件说明

`MetricUnitCascader` 是指标单位选择器，源码在 [src/components/metric-unit-cascader.vue](/src/components/metric-unit-cascader.vue)。它会把单位分组和具体单位组织成两级级联选择。

## 常用 Props

| Prop | 说明 |
|------|------|
| `value` | 当前选中路径 |
| `options` | 外部传入单位树，存在时不再请求接口 |
| `placeholder` | 占位文案 |
| `filterable` | 是否允许搜索 |
| `showAllLevels` | 是否显示完整路径 |
| `separator` | 层级分隔符 |
| `disabled` | 是否禁用 |
| `props` | 透传给 `el-cascader` 的配置 |

## 数据来源

- 默认会调用 `MetricApi.getMetaUnits()`
- 如果显式传了 `options`，则直接使用传入数据

## 输出事件

| 事件 | 说明 |
|------|------|
| `change` | 单位路径变化 |

## 特殊行为

- 接口返回值会被整理成 `group -> values` 的二级结构
- 组件内部维护自己的 `cValue`，通过 `change` 对外同步

## 使用建议

- 涉及指标创建、配置、编辑时，优先复用这个组件而不是自己拼单位下拉
