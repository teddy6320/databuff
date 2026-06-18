# 缓存监控

> 页面: `/appMonitor/cache`
> 文件: `src/views/appMonitor/cache/index.vue`

## 页面职责

缓存页用于按缓存名称和业务线查看缓存资源列表，并下钻到统一详情页。

## 页面结构

- `query-filter`: 缓存名称、业务线
- `service-list`: 缓存列表

## 主要接口

- `ServiceApi.getServicesIds`，固定 `serviceType: 'cache'`
- `BSApi.getGroupTree`
- 列表子组件继续调用 `ServiceApi.getCacheList`

详细接口见:

- [Service API](../../api/service.md)
- [Business System API](../../api/bs.md)

## 说明

- 页面结构与数据库、消息队列页基本一致
- 详情页复用 `/appMonitor/serviceDetail`
