# 事件详情

## 页面信息

| 属性 | 值 |
|------|-----|
| 文件 | `src/views/alarmCenter/eventDetail/index.vue` |
| 复用方式 | 可内嵌，也可作为 `/sysManage/eventDetail` 独立页面使用 |
| 主接口 | `AlarmApi.getEventDetailV2` / `AlarmApi.getSystemEventDetail` |

## 使用方式

### 1. 作为内嵌组件

在告警详情页 `tab-event.vue` 中通过 `eventDetail` prop 传入当前事件对象。

### 2. 作为独立页面

系统管理模块通过:

```text
/sysManage/eventDetail?eid=...
```

打开该组件，并由页面内部自行请求详情。

## 关键 Props / 路由参数

| 名称 | 来源 | 说明 |
|------|------|------|
| `eventDetail` | prop | 已有事件对象时直接使用 |
| `eid` | query | 独立页面场景下的事件 ID |
| `eType` | query | 默认激活的 Tab，值如 `tabMetric` |

## Tabs

| Tab | 值 | 说明 |
|-----|----|------|
| 基本信息 | `tabBaseinfo` | 事件基础字段、异常对象、异常指标、标签 |
| 异常指标 | `tabMetric` | 事件指标图 |
| 链路追踪 | `tabTrace` | 事件链路上下文 |
| 日志 | `tabLog` | 相关日志 |

默认激活 `tabMetric`。

## 数据格式化

页面会在拿到详情后做一轮格式化:

- 统一 `trigger` 字段
- 统一 `tags` 字段为数组结构
- 补充 `classification`
- 计算 `chartTime`
- 计算 `_start/_end`
- 解析 `query` 或 `metrics`，得到 `metricsFormat`

### 时间窗口

- `chartTime`: 以事件时间为中心，按 `15 分钟` 计算指标图时间窗
- `_start/_end`: 为链路与日志查询准备的更大时间窗，向前回溯 `45 分钟`

## 请求逻辑

当页面需要自行取数时:

- 告警中心事件: `AlarmApi.getEventDetailV2({ eventId })`
- 系统事件: `AlarmApi.getSystemEventDetail({ eventId })`

这里实际使用的是 `eventId`，不是 `issueId` 或 `eventSystemId`。

## 依赖组件

- `text-expand`
- `db-tabnav`
- `tab-baseinfo`
- `tab-metric`
- `tab-trace`
- `tab-log`
