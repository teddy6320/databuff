# DbTabnav

## 组件说明

`DbTabnav` 是项目里的轻量页签导航组件，源码在 [src/components/db-tabnav/index.vue](/src/components/db-tabnav/index.vue)。它主要用于模块内页签切换，比 `el-tabs` 更轻、更适合嵌入页面头部。

## 常用 Props

| Prop | 说明 |
|------|------|
| `tabnavs` | 页签数组，元素包含 `label`、`value`，可选 `disabled`、`tip`、`dot` |
| `value` | 当前激活值，支持 `v-model` |
| `activeName` | 兼容旧写法的激活值 |
| `thin` | 更紧凑的横向间距 |
| `slim` | 更小的页签间距 |

## 关键事件

| 事件 | 说明 |
|------|------|
| `input` | 激活值变化，支持 `v-model` |
| `on-change` | 切换完成后返回当前 `tab` 对象 |

## 特殊能力

| 能力 | 说明 |
|------|------|
| `tip` | 页签标题后展示 tooltip 图标 |
| `dot` | 页签右上角展示红点 |
| `disabled` | 禁止点击切换 |
| 滑块动画 | 根据当前激活项位置计算底部滑块 |

## 使用建议

- 页面如果需要把页签写回 URL，通常在 `on-change` 里手动 `router.replace`。
- 新代码优先使用 `v-model`，不要同时混用 `value` 和 `activeName`。
