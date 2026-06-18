# 技术组件

> 文件: `src/views/infrastructure/tech/index.vue`

## 页面状态

当前仓库中存在技术组件页实现，但路由配置里的 `/infrastructure/tech` 与详情路由都已经注释，因此该页不是当前可访问菜单的一部分。

## 页面职责

从现有实现看，这一页用于按“技术组件名称”筛选服务列表，属于一个轻量的技术组件观察页。

## 页面结构

- `query-filter`: 技术组件名称筛选
- `serviceList`: 服务列表组件

## 主要接口

- `ServiceApi.getServicesIds`

详细接口见:

- [Service API](../../api/service.md)

## 关键参数

- 页面会在初始化时调用 `ServiceApi.getServicesIds`
- 当前实现固定传入 `serviceType: 'db'`
- 时间范围变化会重新刷新服务列表

## 注意事项

- 当前只有页面文件，没有生效路由
- `serviceType: 'db'` 的实现语义更像历史遗留逻辑，后续如果恢复此页，建议先和产品/代码一起确认定位
