# 时间选择器配置

## 概述

时间选择器配置定义了全局时间范围选择器的可选选项，用于数据查询时的时间范围控制。

## 时间范围选项

### TimeRangeMsOptions（毫秒值）

| 标签 | 值（ms） | 缩写 | 默认 |
|------|----------|------|------|
| 最近15分钟 | 900,000 | 15m | |
| 最近30分钟 | 1,800,000 | 30m | |
| 最近1小时 | 3,600,000 | 1h | ✓ |
| 最近2小时 | 7,200,000 | 2h | |
| 最近4小时 | 14,400,000 | 4h | |
| 最近6小时 | 21,600,000 | 6h | |
| 最近12小时 | 43,200,000 | 12h | |
| 最近24小时 | 86,400,000 | 24h | |
| 最近3天 | 259,200,000 | 3d | |
| 最近7天 | 604,800,000 | 7d | |
| 最近15天 | 1,296,000,000 | 15d | |
| 最近30天 | 2,592,000,000 | 30d | |

### TimeRangeSOptions（秒值）

| 标签 | 值（s） | 缩写 | 默认 |
|------|---------|------|------|
| 最近15分钟 | 900 | 15m | |
| 最近30分钟 | 1,800 | 30m | |
| 最近1小时 | 3,600 | 1h | ✓ |
| 最近2小时 | 7,200 | 2h | |
| 最近4小时 | 14,400 | 4h | |
| 最近6小时 | 21,600 | 6h | |
| 最近12小时 | 43,200 | 12h | |
| 最近24小时 | 86,400 | 24h | |
| 最近3天 | 259,200 | 3d | |
| 最近7天 | 604,800 | 7d | |
| 最近15天 | 1,296,000 | 15d | |
| 最近30天 | 2,592,000 | 30d | |

## 时间类型枚举

```typescript
enum TimeChooseType {
  SELECT = 'select',   // 快捷选择（最近X分钟/小时/天）
  CUSTOM = 'custom'    // 自定义时间范围
}
```

## 选项数据结构

```typescript
interface TimeRangeOption {
  label: string;              // 显示标签
  value: number;              // 时间值（毫秒或秒）
  type: TimeChooseType;       // 时间类型
  abbr: string;               // URL 缩写（如 '1h', '3d'）
  default?: boolean;          // 是否为默认选项
}
```

## URL 参数格式

### 快捷选择

```
?durationRange=3600000
```

或使用 from/to 缩写格式：

```
?from=now-1h&to=now
```

### 自定义时间范围

```
?fromTime=1680307200000&toTime=1680393600000
```

## 路由守卫时间处理

在 `router.beforeResolve` 中处理时间参数：

1. **自定义时间验证**
   - `fromTime` 和 `toTime` 必须是有效的时间戳
   - `fromTime <= toTime`
   - 时间范围不超过 `limitDays` 天
   - `fromTime` 不早于可选时间窗口（最近31天）
   - `toTime` 不晚于当前时间

2. **from/to 缩写解析**
   - 支持 `from=now-15m&to=now` 格式
   - 自动匹配 `TimeRangeMsOptions` 中的 `abbr`

3. **看板下钻处理**
   - 只含有 `fromTime` 时，自动计算匹配最近的快捷时间范围

4. **默认值**
   - 无时间参数时，使用 `TimeRangeMsOptions` 中 `default: true` 的选项
   - 默认为「最近1小时」

## 全局时间状态

时间状态存储在 Vuex Store 中：

```typescript
// store/modules/global.ts
state: {
  durationDates: {
    fromTime: Date,
    toTime: Date,
    type: 'select' | 'custom',
    duration: number  // 仅 select 类型
  }
}
```

### Store 方法

| 方法 | 说明 |
|------|------|
| `globalTime()` | 获取当前全局时间状态 |
| `SET_DURATION_DATES` | 更新全局时间状态 |

## 页面级时间限制

不同页面可配置不同的 `limitDays`（单次最大时间跨度）：

| 页面 | limitDays | 说明 |
|------|-----------|------|
| 驾驶舱 | 1 | 24小时 |
| 报告 | 60 | 60天 |
| 服务流 | 1 | 24小时 |
| 其他页面 | 31 | 默认31天 |

## 特殊时间模式

### step 模式

当 `time: 'step-{n}'` 时，页面以固定 n 分钟间隔前后切换：

```typescript
{
  time: 'step-5',  // 5分钟间隔
}
```

此时：
- 只显示固定间隔的自定义时间范围
- 不显示快捷时间选项
- 时间范围固定为 n 分钟

## 文件位置

`src/router/time-new.ts`
