# Code View

## 组件说明

`CodeView` 是代码块展示组件，源码在 [src/components/code-view.vue](/src/components/code-view.vue)。它主要用于安装指南、配置说明、日志详情等场景下展示可复制代码。

## 常用 Props

| Prop | 说明 |
|------|------|
| `code` | 展示的原始代码文本 |
| `type` | `block` 或 `inline` |
| `lang` | 语言标识 |
| `showCopy` | 是否显示复制按钮 |
| `copyCode` | 复制时使用的替代文本 |

## 核心行为

- 使用 `highlight.js` 做自动高亮
- 支持块级和行内两种视觉模式
- 点击复制按钮时，默认复制 `code`
- 如果传了 `copyCode`，显示内容和复制内容可以不同

## 典型使用

当前常见于：

- 安装接入页面
- 帮助中心
- 日志、请求、堆栈详情

## 使用建议

- 命令行、YAML、配置片段优先使用它
- 如果内容本质是 Markdown 文档而不是纯代码，优先使用 [Marked View](marked-view.md)
