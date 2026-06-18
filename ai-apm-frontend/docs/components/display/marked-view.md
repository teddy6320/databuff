# Marked View

## 组件说明

`MarkedView` 是 Markdown 渲染组件，源码在 [src/components/marked-view.vue](/src/components/marked-view.vue)。它负责把 Markdown 或纯代码字符串渲染成带代码高亮、表格样式和复制按钮的 HTML。

## 常用 Props

| Prop | 说明 |
|------|------|
| `data` | Markdown 原文 |
| `type` | `markdown` 或 `code` |
| `showCopy` | 是否给代码块插入复制按钮 |

## 核心能力

- 基于 `marked` 渲染 Markdown
- 基于 `highlight.js` 高亮代码块
- 支持表格、列表、图片、段落等常见 Markdown 结构
- 可自动给 `pre > code` 区块插入复制按钮

## 特殊行为

- `type === 'code'` 时，会先把文本包装成代码块再渲染
- **禁止**在 `marked.parse` 前对 `data` 做字符串预处理；LLM/对话内容应原样渲染，否则易导致表格等 Markdown 解析失败
- 组件会在全局只初始化一次 `Clipboard` 实例，挂到 `window.markedClipboard`
- 复制成功/失败会通过 `this.$notify` 给出提示
- 当前 `marked` 配置没有开启 HTML 消毒，使用时要确保输入内容可信

## 典型使用

常见于：

- 安装步骤说明
- 插件说明
- RUM 帮助文档
- 告警详情中的富文本内容展示

## 使用建议

- 如果内容主要是说明文档、接入步骤、富文本描述，优先使用它
- 如果只是单段代码展示，优先用 [Code View](code-view.md)
