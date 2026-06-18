# Kubernetes 集群

> 页面: `/infrastructure/cluster`
> 文件: `src/views/infrastructure/cluster/index.vue`

## 页面职责

集群页用于查看 Kubernetes 集群清单，并作为 Node、Namespace、Workload、Pod、Service 五类资源分析页的统一入口。

## 页面结构

- `query-filter`: 集群名称、集群 ID 等筛选项
- `db-table`: 集群列表
- 表格操作列: 下钻集群详情、Node、Namespace、Workload、Pod、Service

## 主要接口

- `KubernetesApi.getClusterList`
- `KubernetesApi.getClusterSelectList`

详细接口见:

- [Kubernetes API](../../api/kubernetes.md)

## 关键参数

- 列表页主要使用全局时间范围
- 集群详情入口参数:
  - `kid`: 集群 ID
  - `kn`: 集群名称
  - `type`: 详情页签
- 从集群页跳转到子资源页时，会通过 `kid` 或 `tags` 传递集群上下文

## 详情页说明

集群详情页位于 `src/views/infrastructure/clusterDetail/index.vue`，页签包括:

- `overview`
- `baseinfo`
- `metric`

详情页会再次调用 `KubernetesApi.getClusterList` 结合 `clusterId` 查当前集群，并在 `overview`、`metric` 页签中继续加载图表与明细。

## 关联页面

- 集群详情: `/infrastructure/clusterDetail?kid=...&kn=...`
- Node: `/infrastructure/node?kid=...`
- Namespace / Workload / Pod / Service: 通过 `tags=clusterId:...` 传递过滤条件
