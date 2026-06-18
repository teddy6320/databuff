# Workload

> 页面: `/infrastructure/workload`
> 文件: `src/views/infrastructure/workload/index.vue`

## 页面职责

Workload 页用于查看 Deployment、StatefulSet、DaemonSet 等工作负载实体，并提供工作负载详情的统一入口。

## 页面结构

- `query-filter`: 集群、命名空间、工作负载名称
- `db-table`: Workload 列表
- 操作列: Workload 详情

## 主要接口

- `KubernetesApi.getWorkloadList`
- `KubernetesApi.getClusterSelectList`
- `KubernetesApi.getNamespaceSelectList`
- `KubernetesApi.getWorkloadSelectList`

详细接口见:

- [Kubernetes API](../../api/kubernetes.md)

## 关键参数

- 列表页通常通过 `tags` 承接上游的集群或命名空间过滤
- 详情页入口参数:
  - `kid`: Workload ID
  - `kn`: Workload 名称
  - `type`: 详情页签

## 详情页说明

Workload 详情页位于 `src/views/infrastructure/workloadDetail/index.vue`，页签包括:

- `overview`
- `baseinfo`
- `metric`

详情页会通过 `workloadId` 重新查询列表接口定位当前对象，而不是单独的详情接口。

## 关联页面

- Workload 详情: `/infrastructure/workloadDetail?kid=...&kn=...`
