# 应用性能监控模块

> 模块入口: `src/views/appMonitor/index.vue`

## 模块概述

`appMonitor` 是当前仓库里页面规模最大的模块之一，覆盖服务、接口、链路、业务系统、拓扑、诊断、数据库/缓存/MQ 等 APM 场景。

模块根组件会对部分页面做环境标签拦截:

- 允许直接进入: 空间地图、系统拓扑、业务系统、服务流、诊断分析、业务系统调用分析
- 其余页面在“非管理员 + 已开启环境标签 + 当前用户无环境标签数据”时会显示“未配置环境标签”

## 页面矩阵

| 类型 | 路由 | 文件 | 说明 |
|------|------|------|------|
| 页面 | `/appMonitor/relationMap` | `relationMapNew/index.vue` | 空间地图 |
| 页面 | `/appMonitor/businessSystem` | `businessSystem/index.vue` | 业务系统 |
| 页面 | `/appMonitor/systemTopology` | `systemTopology/index.vue` | 系统拓扑 |
| 页面 | `/appMonitor/service` | `service/index.vue` | 服务列表 |
| 页面 | `/appMonitor/database` | `database/index.vue` | 数据库列表 |
| 页面 | `/appMonitor/msgQueue` | `msgQueue/index.vue` | 消息队列列表 |
| 页面 | `/appMonitor/cache` | `cache/index.vue` | 缓存列表 |
| 页面 | `/appMonitor/external` | `external/index.vue` | 外部服务列表 |
| 页面 | `/appMonitor/serviceAnalysis` | `serviceAnalysis/index.vue` | 接口分析 |
| 页面 | `/appMonitor/errors` | `errors/index.vue` | 错误分析 |
| 页面 | `/appMonitor/trace` | `trace/index.vue` | 链路追踪 |
| 页面 | `/appMonitor/serviceFlow` | `serviceFlow/index.vue` | 服务流 |
| 页面 | `/appMonitor/diagnostic` | `diagnostic/index.vue` | 诊断分析 |
| 页面 | `/appMonitor/serviceCall` | `serviceCall/index.vue` | 接口调用分析 |
| 页面 | `/appMonitor/businessCall` | `businessCall/index.vue` | 业务系统调用分析 |
| 静态页 | `/appMonitor/serviceDetail` | `serviceDetail/index.vue` | 服务详情 |
| 静态页 | `/appMonitor/serviceInstance` | `serviceInstance/index.vue` | 服务实例详情 |
| 静态页 | `/appMonitor/resourceDetail` | `resourceDetail/index.vue` | 接口详情 |
| 静态页 | `/appMonitor/traceDetail` | `traceDetail/index.vue` | 调用链详情 |
| 静态页 | `/appMonitor/errorDetail` | `errorDetail/index.vue` | 错误详情 |
| 静态页 | `/appMonitor/response` | `response/index.vue` | 响应时间分布 |
| 静态页 | `/appMonitor/hotMethods` | `hotMethods/index.vue` | Profiling |
| 静态页 | `/appMonitor/threadPool` | `threadPool/index.vue` | 线程池监控 |
| 静态页 | `/appMonitor/objectPool` | `objectPool/index.vue` | 对象池监控 |
| 静态页 | `/appMonitor/httpConnPool` | `httpConnPool/index.vue` | HTTP 连接池监控 |
| 静态页 | `/appMonitor/dbConnPool` | `dbConnPool/index.vue` | 数据库连接池监控 |
| 静态页 | `/appMonitor/thread` | `thread/index.vue` | 线程列表 |

## 典型导航关系

- 服务列表 -> 服务详情: `/appMonitor/serviceDetail?sid=...&sn=...`
- 服务详情 -> 接口分析: `/appMonitor/serviceAnalysis?sid=...`
- 服务详情 -> 接口调用分析: `/appMonitor/serviceCall?sid=...&srcSid=...&componentType=...`
- 接口分析 -> 接口详情: `/appMonitor/resourceDetail?sid=...&endpoint=...&componentType=...`
- 链路追踪 -> 调用链详情: `/appMonitor/traceDetail?tid=...&spid=...`
- 服务详情 / 接口详情 -> Profiling: `/appMonitor/hotMethods?sid=...`
- 诊断分析 -> 线程列表: `/appMonitor/thread?id=...`
- 业务系统 -> 业务系统调用分析: `/appMonitor/businessCall?...`

## 主要依赖 API

- `service.ts`: 服务列表、接口分析、错误分析、数据库/MQ/缓存、线程剖析
- `apm.ts`: 服务详情、调用链、服务流、Profiling、调用分析
- `bs.ts`: 业务系统、空间地图、系统拓扑、业务系统调用分析
- `metric.ts`: 线程池/对象池/连接池等内部指标图

## 已补文档

- [服务监控](service.md)
- [接口调用分析](service-call.md)
- [服务流](service-flow.md)
- [链路追踪](trace.md)
- [调用链详情](trace-detail.md)
- [线程列表](thread.md)
- [错误分析](errors.md)
- [错误详情](error-detail.md)
- [数据库监控](database.md)
- [缓存监控](cache.md)
- [消息队列](msg-queue.md)
- [HTTP 连接池](http-conn-pool.md)
- [数据库连接池](db-conn-pool.md)
- [系统拓扑](topology.md)
- [空间地图](relation-map.md)
- [诊断分析](diagnostic.md)
- [业务系统](business-system.md)
- [业务系统调用分析](business-call.md)
