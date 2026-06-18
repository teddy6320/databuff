# Pod

> 页面: `/infrastructure/pod`
> 文件: `src/views/infrastructure/pod/index.vue`

## 页面职责

Pod 页用于查看 Pod 清单、状态与节点归属，并提供 Pod 详情入口。

## 页面结构

- `query-filter`: 集群、命名空间、工作负载、Pod 名称、Node 名称
- `db-table`: Pod 列表
- 操作列: Pod 详情

## 主要接口

- `KubernetesApi.getPodList`
- `KubernetesApi.getClusterSelectList`
- `KubernetesApi.getNamespaceSelectList`
- `KubernetesApi.getWorkloadSelectList`
- `KubernetesApi.getPodSelectList`
- `KubernetesApi.getNodeSelectList`

详细接口见:

- [Kubernetes API](../../api/kubernetes.md)

## 关键参数

- 列表页通常由 `tags` 承接集群、命名空间或工作负载过滤
- 常规详情入口参数:
  - `kid`: Pod UID
  - `kn`: Pod 名称
- 另一种入口兼容参数:
  - `cid`: 集群 ID
  - `pn`: Pod 名称
- 详情页签由 `type` 控制

## 详情页说明

Pod 详情页位于 `src/views/infrastructure/podDetail/index.vue`，页签包括:

- `overview`
- `baseinfo`
- `metric`

详情页优先兼容两种入参模式，最终用 `clusterId + podName` 查询 `KubernetesApi.getPodList` 定位当前对象。

## 关联页面

- Pod 详情: `/infrastructure/podDetail?kid=...&kn=...`
