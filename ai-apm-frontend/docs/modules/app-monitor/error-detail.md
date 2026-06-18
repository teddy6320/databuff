# 错误详情

> 页面: `/appMonitor/errorDetail`
> 文件: `src/views/appMonitor/errorDetail/index.vue`

## 页面职责

错误详情页与错误分析页结构接近，但定位为单个错误场景的进一步分析视图。

## 页面结构

- `search-group`
- `chart-group`
- `table-list`

## 主要接口

- `ServiceApi.getServicesIds`
- 继续复用 `service.ts` 中错误分析相关接口

详细接口见:

- [Service API](../../api/service.md)

## 参数处理

- `sid` -> `serviceId`
- `si` -> `serviceInstance`

## 说明

- 页面整体是“筛选 + 趋势 + 明细表”结构
- 和错误分析页的区别主要在于进入路径与使用场景更偏“单点下钻”
