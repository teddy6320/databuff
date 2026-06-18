# Node 监控

> 页面: `/infrastructure/node`
> 文件: `src/views/infrastructure/node/index.vue`

## 页面职责

Node 页用于查看某个 Kubernetes 集群下的节点状态、资源利用率与主机关联关系，并支持下钻 Node 详情或主机详情。

## 页面结构

- 顶部摘要图: CPU 与内存容量/使用率
- `db-table`: Node 列表
- 操作列: Node 详情、主机详情

## 主要接口

- `KubernetesApi.getNodeList`
- `KubernetesApi.getNodeDetail`
- `KubernetesApi.getClusterList`
- `MetricApi.getMetricChart`

详细接口见:

- [Kubernetes API](../../api/kubernetes.md)
- [Metric API](../../api/metric.md)

## 关键参数

- `kid`: 列表页中表示 `clusterId`
- Node 详情入口参数:
  - `kid`: Node ID
  - `cid`: 集群 ID
  - `kn`: Node 名称
  - `type`: 详情页签
- 只有 `status` 不是 `NotReady` 且不为 `-` 时允许继续下钻

## 详情页说明

Node 详情页位于 `src/views/infrastructure/nodeDetail/index.vue`，页签包括:

- `overview`
- `baseinfo`
- `metric`

列表页会先用 `KubernetesApi.getClusterList` 获取集群容量，再用 `MetricApi.getMetricChart` 生成顶部 CPU、内存摘要图。

## 关联页面

- Node 详情: `/infrastructure/nodeDetail?kid=...&cid=...&kn=...`
- 主机详情: `/infrastructure/hostDetail?hostName=...`
