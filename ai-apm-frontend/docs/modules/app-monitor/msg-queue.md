# 消息队列

> 页面: `/appMonitor/msgQueue`
> 文件: `src/views/appMonitor/msgQueue/index.vue`

## 页面职责

消息队列页用于查看 Topic / MQ 资源列表，并支持按业务线筛选。

## 页面结构

- `query-filter`: Topic 名称、业务线
- `service-list`: MQ 列表

## 主要接口

- `ServiceApi.getServicesIds`，固定 `serviceType: 'mq'`
- `BSApi.getGroupTree`
- 列表子组件继续调用 `ServiceApi.getMqList`

详细接口见:

- [Service API](../../api/service.md)
- [Business System API](../../api/bs.md)

## 说明

- 页面模式与数据库、缓存页一致
- 详情页复用 `/appMonitor/serviceDetail`
