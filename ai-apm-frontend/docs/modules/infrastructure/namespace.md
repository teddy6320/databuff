# Namespace

> 页面: `/infrastructure/namespace`
> 文件: `src/views/infrastructure/namespace/index.vue`

## 页面职责

Namespace 页用于查看命名空间清单，并作为 Workload、Pod、Service 三类 Kubernetes 资源的过滤入口。

## 页面结构

- `query-filter`: 集群、命名空间等筛选项
- `db-table`: Namespace 列表
- 操作列: 详情、Workload、Pod、Service

## 主要接口

- `KubernetesApi.getNamespaceList`
- `KubernetesApi.getClusterSelectList`
- `KubernetesApi.getNamespaceSelectList`

详细接口见:

- [Kubernetes API](../../api/kubernetes.md)

## 关键参数

- 列表页常通过 `tags` 接收上游页面传入的 `clusterId`
- 详情页入口参数:
  - `kid`: Namespace UID
  - `kn`: Namespace 名称
  - `cid`: 集群 ID，可选
  - `type`: 详情页签
- 从列表页下钻到 Workload / Pod / Service 时，会在 `tags` 中同时带上 `clusterId` 与 `namespaceName`

## 详情页说明

Namespace 详情页位于 `src/views/infrastructure/namespaceDetail/index.vue`，页签包括:

- `overview`
- `baseinfo`
- `metric`

详情页通过 `clusterId + namespaceName` 重新查询当前对象，再加载页签内容。

## 关联页面

- Namespace 详情: `/infrastructure/namespaceDetail?kid=...&kn=...`
- Workload: `/infrastructure/workload?tags=...`
- Pod: `/infrastructure/pod?tags=...`
- Service: `/infrastructure/service?tags=...`
