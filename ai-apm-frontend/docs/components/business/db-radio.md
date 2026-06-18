# DbRadio

## 组件说明

`DbRadio` 对应源码 [src/components/db-radio/index.vue](/src/components/db-radio/index.vue)。它不是传统单选框，而是滑块式分段选择器，适合少量选项切换。

## 常用 Props

| Prop | 说明 |
|------|------|
| `value` | 当前选中值 |
| `options` | 选项列表，支持 `label / value / icon / describe / disabled` |
| `size` | `large` / `default` / `small` |
| `disabled` | 是否整体禁用 |
| `plain` | 是否使用浅色高亮模式 |

## 关键事件

| 事件 | 说明 |
|------|------|
| `input` | 用于 `v-model` 回写 |
| `change` | 点击切换后触发，返回当前 option 和 `$index` |

## 特殊行为

- 组件会根据当前选中项自动计算滑块宽度和位移
- `options` 更新后，会重新计算激活滑块位置
- 单个 option 可以带 `describe`，会显示问号 tooltip
- 单个 option 可以独立 `disabled`

## 适用场景

- 小范围模式切换
- 统计卡片上的时间粒度切换
- 轻量 tab 替代

## 使用建议

- 选项数量较少时使用体验最好
- 如果需要真正的路由切换或大粒度页面导航，优先用 [db-tabnav](db-tabnav.md)
