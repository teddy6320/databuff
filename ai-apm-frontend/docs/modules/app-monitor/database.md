# 数据库监控

> 页面: `/appMonitor/database`
> 文件: `src/views/appMonitor/database/index.vue`

## 页面职责

数据库页用于按数据库名称和业务线查看数据库资源列表，并从列表下钻到统一的详情页。

## 页面结构

- `query-filter`: 数据库名称、业务线
- `service-list`: 数据库列表

## 主要接口

- `ServiceApi.getServicesIds`，固定 `serviceType: 'db'`
- `BSApi.getGroupTree`
- 列表子组件进一步调用 `ServiceApi.getDatabaseList`

详细接口见:

- [Service API](../../api/service.md)
- [Business System API](../../api/bs.md)

## 关键参数

- 路由 query 会回填筛选项
- 详情页复用 `/appMonitor/serviceDetail`，路由为 `/appMonitor/database/detail`
