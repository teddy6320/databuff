# time-format

## 文件定位

[src/utils/timeFormat.ts](/src/utils/timeFormat.ts) 提供全局时间范围计算和图表 interval 计算能力，是 `globalState`、路由时间参数和多个分析页的基础工具。

## 主要函数

| 函数 | 说明 |
|------|------|
| `setDateBySeconds(date, seconds)` | 把一个时间设置到指定秒，并清零毫秒 |
| `calcInterval(start, end)` | 根据时间跨度计算推荐采样间隔 |
| `getTimeRange(start, end, minDuration?)` | 校正开始/结束时间并返回合理范围和 interval |

## `calcInterval` 规则

会根据跨度给出不同粒度，例如：

| 时间跨度 | interval |
|------|------|
| `>= 30天` | `12小时` |
| `>= 15天` | `4小时` |
| `>= 7天` | `2小时` |
| `>= 3天` | `1小时` |
| `>= 1天` | `15分钟` |
| `>= 12小时` | `10分钟` |
| `>= 4小时` | `5分钟` |
| `>= 2小时` | `2分钟` |
| 其他 | `1分钟` |

## `getTimeRange` 校正规则

- `end` 不能超过当前时间
- 可视窗口限制在最近 7 天内
- 默认最小跨度是 15 分钟
- 最终返回值会把 `start/end` 秒数归零

## 使用场景

- `store/modules/global`
- 路由时间参数初始化
- 详情页的样本追踪时间范围纠偏
