# Kubernetes Service

> 页面: `/infrastructure/service`
> 文件: `src/views/infrastructure/service/index.vue`

## 页面职责

Service 页用于查看 Kubernetes Service 清单，并提供 Service 详情入口。

## 页面结构

- `query-filter`: 集群、命名空间、Service 名称
- `db-table`: Service 列表
- 操作列: Service 详情

## 主要接口

- `KubernetesApi.getServiceList`
- `KubernetesApi.getClusterSelectList`
- `KubernetesApi.getNamespaceSelectList`
- `KubernetesApi.getServiceSelectList`

详细接口见:

- [Kubernetes API](../../api/kubernetes.md)

## 关键参数

- 列表页可通过 `tags` 接收上游页面的集群、命名空间过滤
- 详情页入口参数:
  - `kid`: Service ID
  - `kn`: Service 名称
  - `type`: 详情页签

## 详情页说明

Service 详情页位于 `src/views/infrastructure/serviceDetail/index.vue`，页签包括:

- `overview`
- `baseinfo`

详情页通过 `serviceId` 再查一次 `KubernetesApi.getServiceList` 定位当前对象。

## 关联页面

- Service 详情: `/infrastructure/serviceDetail?kid=...&kn=...`
