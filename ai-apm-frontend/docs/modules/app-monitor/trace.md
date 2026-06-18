# 链路追踪

> 页面: `/appMonitor/trace`
> 文件: `src/views/appMonitor/trace/index.vue`

## 页面职责

链路追踪页用于查询 Trace 列表、查看请求量/错误量趋势，并从时间点或筛选条件下钻到调用链详情。

## 页面结构

- `search-group`: 服务、实例、TraceID、多条件查询
- `overviewChart`: 请求趋势图 / 错误趋势图
- `choose-collapse`: 补充筛选条件
- `table-list`: Trace 列表

## 主要接口

- `ApmApi.getServicesIds`（实际从 `service.ts` 导入）
- `ApmApi.getSpanParams`
- `ApmApi.getSpanList`
- `ApmApi.getSpanRequestGraph`
- `ApmApi.getSpanErrorGraph`
- `ApmApi.getSpanResponseTimeGraph`

详细接口见:

- [Service API](../../api/service.md)
- [APM API](../../api/apm.md)

## 关键参数

- `multisearch`: 多条件搜索串
- `sf` / `st`: 图表点位对应的开始/结束时间戳
- 图表点击后才展示下方列表

## 关联页面

- 调用链详情: `/appMonitor/traceDetail?tid=...&spid=...`
