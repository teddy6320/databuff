# IpInput

## 组件说明

`IpInput` 是一个 IPv4 分段输入组件，源码在 [src/components/ipInput.vue](/src/components/ipInput.vue)。它把一个 IP 地址拆成四段输入，并兼容表单项联动。

## 常用 Props

| Prop | 说明 |
|------|------|
| `value` | IP 字符串 |
| `placeholder` | 每段输入框占位符 |
| `disabled` | 是否禁用 |

## 输出事件

| 事件 | 说明 |
|------|------|
| `input` | 回写完整 IP 字符串 |
| `on-change` | 兼容 iView 风格的变更事件 |

## 核心行为

- 每段最多输入 3 位
- 自动限制在 `0 ~ 255`
- 输入满 3 位时自动跳到下一段
- 支持用 `.` 快速切换到下一段
- 支持整段 IP 粘贴并自动拆分
- 会派发 `FormItem` 的 `on-form-change`，兼容表单校验

## 使用建议

- 只适用于 IPv4
- 如果页面只是普通字符串输入，没有分段联动需求，不必优先使用它
