# Collapse Tags

## 组件说明

`CollapseTags` 用于在有限宽度内展示一组标签，源码在 [src/components/collapse-tags/index.vue](/src/components/collapse-tags/index.vue)。它比简单的 `el-tag` 循环更适合“标签数量不确定、宽度受限”的场景。

## 常用 Props

| Prop | 说明 |
|------|------|
| `tags` | 标签字符串数组 |
| `silent` | 静态模式，超出时只展示 `...`，不显示展开/折叠按钮 |
| `copyable` | 是否允许复制单个标签 |
| `clickable` | 标签是否可点击 |
| `parentPadding` | 父容器左右 padding 总和，用于计算可用宽度 |
| `minWidth` | 最小宽度 |
| `maxLine` | 最多展示行数 |

## 关键事件

| 事件 | 说明 |
|------|------|
| `on-click` | 点击某个标签时触发 |

## 特殊行为

- 组件会在挂载后根据父容器宽度、标签文本宽度和最大行数，计算首屏最多展示多少个标签。
- 非静态模式下，超出时显示“展开/折叠”操作标签。
- 静态模式下，超出仅显示 `...`。
- 单个标签复制按钮只在 hover 时显示。

## 注意事项

- 组件当前只在 `mounted` 时计算宽度，父容器宽度变化后不会自动重新计算。
