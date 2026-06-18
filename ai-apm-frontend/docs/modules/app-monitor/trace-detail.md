# 调用链详情

> 页面: `/appMonitor/traceDetail`
> 文件: `src/views/appMonitor/traceDetail/index.vue`

## 页面职责

调用链详情页用于查看单条 Trace 的完整 Span 链路，支持调用次序、火焰图、耗时统计和服务流四种视图。

## 页面结构

- 顶部基础信息: 资源名、服务名、开始时间、总耗时、TraceID
- `db-tabnav`: `调用次序` / `火焰图` / `耗时统计` / `服务流`
- 左侧主视图: 树形火焰图、火焰图、Span 列表或服务流
- 右侧 `span-aside`: 当前 Span 详情

## 主要接口

- `ApmApi.getSpanList`
- `ApmApi.getTraceSpans`

详细接口见:

- [APM API](../../api/apm.md)

## 关键参数

- `tid`: TraceID
- `spid`: SpanID
- `pid`: 父 SpanID，可用于从子链路继续下钻
- `ft` / `tt`: 指定时间范围的时间戳

## 特点

- Span 数量超过 1000 时页面会提示，只展示前 1000 条
- 若存在 RUM Span，页面会把前端 Span 合并进同一条链路
- 当带 `pid` 进入时，页面会先查询当前 Span，再拉整条链路
