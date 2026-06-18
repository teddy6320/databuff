# Query Filter

## 组件说明

`QueryFilter` 是项目里最常见的筛选组件，源码在 [src/components/query-filter/index.vue](/src/components/query-filter/index.vue)。它的定位不是简单的表单，而是“筛选项选择器 + 标签回显 + URL 同步”。

## 核心输入

| Prop | 说明 |
|------|------|
| `value` | 当前筛选值对象，通常配合 `v-model` |
| `filterList` | 筛选项定义数组 |
| `updateRoute` | 是否自动把筛选条件写回当前路由 |
| `filterTitle` | 默认输入提示，默认是“搜索” |
| `allClearable` | 是否允许一键清空 |
| `disabled` | 是否整体禁用 |
| `size` | `small` 或 `default` |

## `filterList` 结构

类型定义见 [src/components/query-filter/types/index.types.ts](/src/components/query-filter/types/index.types.ts)。

| 字段 | 说明 |
|------|------|
| `label` | 展示名称 |
| `field` | 查询字段名 |
| `type` | `input` / `select` / `number` / `date` |
| `multiple` | 是否多选 |
| `children` | 静态选项列表 |
| `likeable` | 是否允许输入时创建候选值 |
| `addable` | 筛选项是否可新增/删除 |
| `editable` | 已选 tag 是否可编辑 |
| `deletable` | 已选 tag 是否可删除 |

## 关键事件

| 事件 | 说明 |
|------|------|
| `input` | 输出新的查询对象 |
| `on-change` | 某个筛选项变化 |
| `on-remove-tag` | 删除 tag |
| `field-choose` | 首次选中某个字段 |

## 特殊行为

- 开启 `updateRoute` 后，会把筛选条件写入 query，并自动对值做 `encodeURIComponent`。
- 多选字段会输出数组，并在 URL 中保留多值。
- 对于 `likeable` 或 `input` 类型，如果当前值不在静态选项里，会自动创建一个临时自定义选项。
- 清空操作会把所有筛选恢复到初始 `value`，而不是一律清空。

## 使用建议

- 页面初始化时，通常先从 `$route.query` 回填到 `value`，再把 `updateRoute` 打开。
- 如果某些筛选项不是长期可见的，建议把 `addable` 设为 `false`，选中后再动态加入。
