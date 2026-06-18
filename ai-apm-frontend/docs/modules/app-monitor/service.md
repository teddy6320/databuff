# 服务监控

> 页面: `/appMonitor/service`
> 文件: `src/views/appMonitor/service/index.vue`

## 页面职责

服务页是 APM 模块的主入口之一，用于查看服务总体健康、趋势图和服务列表，并作为服务详情、服务流、接口分析等页面的跳转入口。

## 页面结构

- `query-filter`: 服务名称
- `db-radio`: `Web` / `自定义` 服务类型切换
- `chart-group`: 服务趋势与统计图
- `service-list`: 服务表格

## 主要接口

- `ServiceApi.getServicesIds`
- `BSApi.getBusAndSubList`
- `BSApi.getGroupTree`
- 子组件内继续使用 `service.ts` 与 `apm.ts` 查询趋势和列表

详细接口见:

- [Service API](../../api/service.md)
- [Business System API](../../api/bs.md)

## 关键参数

- 路由 query 会回填筛选项
- 页面内部固定追加 `serviceTypes: [serviceTypeModel]`
- 服务类型默认是 `web`

## 关联页面

- 服务详情: `/appMonitor/serviceDetail?sid=...&sn=...`
- 服务流: `/appMonitor/serviceFlow`
- 响应时间分布: `/appMonitor/response?sid=...`
- 接口调用分析: `/appMonitor/serviceCall?...`
