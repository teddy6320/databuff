# 错误分析

> 页面: `/appMonitor/errors`
> 文件: `src/views/appMonitor/errors/index.vue`

## 页面职责

错误分析页用于汇总展示服务错误分布、趋势和错误列表。

## 页面结构

- `search-group`: 服务、实例等筛选
- `chart-group`: 错误趋势图
- `table-list`: 错误列表

## 主要接口

- `ServiceApi.getServicesIds`
- `ServiceApi.getErrorDistList`
- `ServiceApi.getErrorSpanList`

详细接口见:

- [Service API](../../api/service.md)

## 参数处理

- `sid` 会转换成 `serviceId`
- `si` 会转换成 `serviceInstance`

## 关联页面

- 错误详情: `/appMonitor/errorDetail?...`
