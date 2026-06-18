# Global Store

## 模块定位

`globalState` 模块源码在 [src/store/modules/global/index.ts](/src/store/modules/global/index.ts)，负责全局时间范围、自动刷新、事件栈和图表栈，是分析页联动最核心的状态模块。

## State

| 字段 | 说明 |
|------|------|
| `durationRange` | 当前最近时间范围毫秒值 |
| `globalTime` | 全局时间对象 |
| `refresh` | 自动刷新间隔 |
| `refreshPause` | 自动刷新是否暂停 |
| `eventStack` | 时间变化后需要触发的事件栈 |
| `chartStack` | 图表栈 |
| `edit` | 编辑状态 |
| `lock` | 锁定状态 |

## `globalTime` 结构

| 字段 | 说明 |
|------|------|
| `fromTime` / `toTime` | 时间范围 |
| `duration` | 时间跨度 |
| `interval` | 图表建议时间粒度 |
| `inited` | 是否已完成初始化 |
| `type` | `select` 或 `custom` |

## 关键 Getters

| Getter | 说明 |
|------|------|
| `durationRange` | 当前最近时间范围 |
| `globalTime()` | 返回 `Date` 类型时间对象 |
| `globalTimeV2()` | 返回格式化后的字符串时间 |
| `globalTimeInited` | 时间是否初始化完成 |
| `refresh` | 自动刷新间隔 |
| `refreshPause` | 自动刷新暂停状态 |

## 关键 Mutations / Actions

| 名称 | 说明 |
|------|------|
| `SET_DURATION_RANGE` | 更新最近时间范围 |
| `SET_DURATION_DATES` | 更新全局时间区间 |
| `SET_DURATION_DATES_INIT_STATUS` | 设置时间初始化状态 |
| `SET_EVENTS` / `RUN_EVENTS` | 设置并执行事件栈 |
| `SET_CHARTS` | 设置图表栈 |
| `SET_EDIT` | 设置编辑状态 |
| `SET_REFRESH` / `SET_REFRESH_PAUSE` | 设置自动刷新 |

## 关键行为

### 最近时间与自定义时间

- `type === 'select'` 时，getter 会基于“当前时间减一分钟”动态计算最近时间窗口。
- `type === 'custom'` 时，getter 会直接使用 state 中保存的绝对时间。

### 事件栈机制

- 时间变化后，action 会先尝试中断已有请求：遍历 `window.axiosCancel`。
- 然后再触发 `RUN_EVENTS`，延迟执行注册的事件栈，驱动页面刷新。

### 时间粒度

- `SET_DURATION_DATES` 会调用 `calcInterval` 自动计算合适的图表时间粒度。

## 注意事项

- 当前模块定义了 `lock` state 和 `SET_LOCK` action/常量，但源码里没有对应的 mutation 实现；文档应按当前实现如实记录，不应假设它已生效。
- 页面里常见的 `globalTime()` 和 `globalTimeV2()` 都是 getter 返回函数，不是直接值。
