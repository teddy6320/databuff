# 基础设施模块

> 模块入口: `src/views/infrastructure/index.vue`

## 模块概述

`infrastructure` 模块覆盖主机、Docker、Kubernetes 与进程四类基础设施实体，用于查看资源清单、运行状态、指标趋势以及实体间的下钻关系。

模块根组件会做环境标签拦截:

- 非管理员用户且已开启环境标签时，如果当前用户没有环境标签数据，会显示“未配置环境标签”
- 当前实现没有像 `appMonitor` 那样的页面白名单豁免

## 页面矩阵

| 类型 | 路由 | 文件 | 说明 |
|------|------|------|------|
| 页面 | `/infrastructure/host` | `host/index.vue` | 主机列表与主机地图 |
| 页面 | `/infrastructure/docker` | `docker/index.vue` | Docker 容器列表 |
| 页面 | `/infrastructure/cluster` | `cluster/index.vue` | Kubernetes 集群列表 |
| 页面 | `/infrastructure/process` | `process/index.vue` | 进程列表 |
| 静态页 | `/infrastructure/hostDetail` | `hostDetail/index.vue` | 主机详情 |
| 静态页 | `/infrastructure/dockerDetail` | `dockerDetail/index.vue` | 容器详情 |
| 静态页 | `/infrastructure/clusterDetail` | `clusterDetail/index.vue` | 集群详情 |
| 静态页 | `/infrastructure/node` | `node/index.vue` | Node 列表 |
| 静态页 | `/infrastructure/nodeDetail` | `nodeDetail/index.vue` | Node 详情 |
| 静态页 | `/infrastructure/namespace` | `namespace/index.vue` | Namespace 列表 |
| 静态页 | `/infrastructure/namespaceDetail` | `namespaceDetail/index.vue` | Namespace 详情 |
| 静态页 | `/infrastructure/workload` | `workload/index.vue` | Workload 列表 |
| 静态页 | `/infrastructure/workloadDetail` | `workloadDetail/index.vue` | Workload 详情 |
| 静态页 | `/infrastructure/pod` | `pod/index.vue` | Pod 列表 |
| 静态页 | `/infrastructure/podDetail` | `podDetail/index.vue` | Pod 详情 |
| 静态页 | `/infrastructure/service` | `service/index.vue` | Kubernetes Service 列表 |
| 静态页 | `/infrastructure/serviceDetail` | `serviceDetail/index.vue` | Kubernetes Service 详情 |
| 静态页 | `/infrastructure/processDetail` | `processDetail/index.vue` | 进程详情 |
| 未开放路由 | `tech/index.vue` | `tech/index.vue` | 技术组件页，当前路由配置已注释 |

## 典型导航关系

- 主机列表 -> 主机详情: `/infrastructure/hostDetail?hostName=...`
- 主机列表 -> 主机指标页签: `/infrastructure/hostDetail?hostName=...&type=metric&app=...`
- 主机列表 -> 告警中心: `/alarmCenter/alarm?host=...`
- 集群列表 -> 集群详情: `/infrastructure/clusterDetail?kid=...&kn=...`
- 集群列表 -> Node / Namespace / Workload / Pod / Service: 通过 `kid` 或 `tags` 传递集群上下文
- Namespace 列表 -> Workload / Pod / Service: 通过 `tags` 追加 `clusterId` 与 `namespaceName`
- 进程列表 -> 服务详情 / 服务实例详情: 跳转到 `appMonitor`

## 主要依赖 API

- `infrastructure.ts`: 主机、容器、进程、分组、主机地图
- `kubernetes.ts`: 集群、Node、Namespace、Workload、Pod、Service
- `metric.ts`: Node 列表顶部容量与使用率图表
- `service.ts`: 技术组件页中的基础服务选择

## 已补文档

- [主机监控](host.md)
- [Kubernetes 集群](cluster.md)
- [Node 监控](node.md)
- [Namespace](namespace.md)
- [Workload](workload.md)
- [Pod](pod.md)
- [Docker 容器](docker.md)
- [进程监控](process.md)
- [Kubernetes Service](service.md)
- [技术组件](tech.md)
