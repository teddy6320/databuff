# Cont Wrapper

## 组件说明

`ContWrapper` 是页面内容容器，源码在 [src/components/cont-wrapper/index.vue](/src/components/cont-wrapper/index.vue)。它的职责很简单：提供一个统一的内容区域包裹层和可选的纵向滚动。

## 常用 Props

| Prop | 说明 |
|------|------|
| `scrollY` | 是否开启纵向滚动，默认开启 |
| `subClass` | 追加到内部容器的 class |

## 结构特点

- 外层固定 `width/height: 100%`
- 内层默认加 `padding: 16px`
- 开启 `scrollY` 时，内部容器会加 `overflow-y: auto`

## 典型使用

当前在 RUM 详情和追踪页里使用较多，例如：

- 会话详情
- 请求追踪
- 页面追踪
- JS 错误追踪

## 使用建议

- 需要“整页滚动容器 + 标准内边距”时优先使用它
- 如果页面已经有复杂布局容器，不一定需要再套一层
