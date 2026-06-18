# 接口调用分析

> 页面: `/appMonitor/serviceCall`
> 文件: `src/views/appMonitor/serviceCall/index.vue`

## 页面职责

接口调用分析页用于查看某一对服务之间的请求调用情况，并根据实际数据动态切换到连接池/对象池分析视图。

## 页面结构

- `db-tabnav`: 视图切换
- `request-view`: 请求调用分析主视图
- `pool-view`: 连接池 / 对象池视图

## 主要接口

- `ServiceApi.getServiceCallPools`
- 子视图继续使用 `ServiceApi.getServiceCallInfo`、`getServiceCallGraphStats`、`getServiceCallEndpoints`
- 池化视图会联动 `metric.ts` 获取指标图

详细接口见:

- [Service API](../../api/service.md)
- [Metric API](../../api/metric.md)

## 关键参数

- `componentType`: 请求类型，如 `service.http`、`service.db`
- `sid`: 接收端服务 ID
- `srcSid`: 发起端服务 ID
- `viewType`: 当前子视图

## 特点

- 页面初始化时会先调用 `getServiceCallPools`
- 若返回了池名称数据，会动态追加“对象池 / HTTP连接池 / 数据库连接池监控”视图
- `viewType` 会同步到路由 query，便于刷新后恢复当前页签
