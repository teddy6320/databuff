# Scroll Select

## 组件说明

`ScrollSelect` 是对 `el-select` 的增强封装，源码在 [src/components/scroll-select.vue](/src/components/scroll-select.vue)。它主要解决“大量选项下拉性能”和“允许创建临时值”两个问题。

## 常用 Props

| Prop | 说明 |
|------|------|
| `value` | 当前值，支持单选或多选 |
| `options` | 选项列表，支持字符串数组或 `{ label, value }` 对象数组 |
| `multiple` | 是否多选 |
| `multipleLimit` | 多选数量上限 |
| `allowCreate` | 是否允许创建新值 |
| `filterable` | 是否允许搜索 |
| `collapseTags` | 多选时是否折叠 tag |
| `clearable` | 是否可清空 |
| `showTitle` | option 是否带 title |
| `pageSize` | 每次渲染的选项数量，默认 50 |

## 关键事件

| 事件 | 说明 |
|------|------|
| `change` | 值变化 |
| `remove-tag` | 多选删除某个 tag |
| `option-click` | 点击某个选项 |
| `visible-change` | 下拉开关变化 |
| `filter-change` | 搜索词变化，并返回过滤后的选项列表 |

## 特殊行为

- 组件会只渲染前 `loadedCount` 条选项，并在滚动接近底部时继续加载。
- 如果当前 `value` 不在 `options` 里，会自动补成 `custom` 选项，避免回显丢失。
- 每次打开下拉时会把滚动位置重置到顶部，并清理未被选中的临时自定义项。

## 使用建议

- 选项数量较大时，优先使用 `ScrollSelect` 而不是直接使用 `el-select`。
- 如果页面允许后端返回值之外再输入自定义值，可以打开 `allowCreate`。
