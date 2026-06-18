# Text Expand

## 组件说明

`TextExpand` 用于展示长文本的折叠/展开，源码在 [src/components/text-expand/index.vue](/src/components/text-expand/index.vue)。常见于告警详情、事件详情、问题详情这类“描述字段可能很长”的场景。

## 常用 Props

| Prop | 说明 |
|------|------|
| `content` | 原始文本内容 |
| `maxLines` | 最大展示行数，默认 3 |
| `lineHeight` | 行高，默认 24 |
| `bgColor` | 折叠态操作区背景色 |

## 关键事件

| 事件 | 说明 |
|------|------|
| `on-toggle` | 展开/收起时触发，参数为当前是否折叠 |

## 特殊行为

- 组件会在内容变化和窗口 resize 后重新计算真实高度。
- 只有内容高度超过 `lineHeight * maxLines` 时才显示“展开/收起”。
- 折叠态操作区会覆盖在文本末尾右上角，并使用主题背景色避免遮挡突兀。

## 使用建议

- 如果页面背景不是标准卡片背景，建议显式传 `bgColor`，避免操作区背景色不一致。
