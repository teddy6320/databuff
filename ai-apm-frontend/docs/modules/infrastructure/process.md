# 进程监控

> 页面: `/infrastructure/process`
> 文件: `src/views/infrastructure/process/index.vue`

## 页面职责

进程页用于查看主机上的进程清单、资源使用情况以及与 APM 服务的关联关系，并支持下钻进程详情。

## 页面结构

- 顶部工具区: 时间范围与列表筛选
- `search-group`: 进程检索条件
- `db-table`: 树形进程列表，支持懒加载子进程
- 操作列: 进程详情、服务详情、服务实例详情

## 主要接口

- `InfraApi.getProcessList`
- `InfraApi.getProcessDetail`

详细接口见:

- [Infrastructure API](../../api/infrastructure.md)

## 关键参数

- 列表页通过 `InfraApi.getProcessList` 同时承担首层列表与子节点懒加载
- 详情页入口参数:
  - `processName`
  - `hostName`
  - `type`: 详情页签
- 页面支持从基础设施侧继续跳到 APM 的服务详情与服务实例详情

## 详情页说明

进程详情页位于 `src/views/infrastructure/processDetail/index.vue`，页签包括:

- `metric`
- `baseinfo`

详情页先调用 `InfraApi.getProcessDetail` 获取进程基础信息，再按页签加载指标或基础信息。

## 关联页面

- 进程详情: `/infrastructure/processDetail?processName=...&hostName=...`
- 服务详情: `/appMonitor/serviceDetail?sid=...`
- 服务实例详情: `/appMonitor/serviceInstance?sid=...&si=...`
