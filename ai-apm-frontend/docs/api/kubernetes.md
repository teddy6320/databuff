# Kubernetes API

> 文件: `src/api/kubernetes.ts`

## 概述

`kubernetes.ts` 封装 Kubernetes 资源查询能力，覆盖集群、Namespace、Workload、Pod、Node、Service 等资源列表及下拉选择数据。

## 接口分组

### 集群与命名空间

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getClusterList` | `POST` | `/k8s/cluster/list` | 集群列表 |
| `getClusterSelectList` | `POST` | `/k8s/cluster/idNameList` | 集群下拉列表 |
| `getNamespaceList` | `POST` | `/k8s/ns/list` | Namespace 列表 |
| `getNamespaceSelectList` | `POST` | `/k8s/ns/list` | Namespace 下拉映射 |

### Workload / Pod / Node / Service

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getWorkloadList` | `POST` | `/k8s/wl/list` | Workload 列表 |
| `getWorkloadSelectList` | `POST` | `/k8s/wl/list` | Workload 下拉映射 |
| `getPodList` | `POST` | `/k8s/pod/list` | Pod 列表 |
| `getPodSelectList` | `POST` | `/k8s/pod/list` | Pod 下拉映射 |
| `getNodeList` | `POST` | `/k8s/node/list` | Node 列表 |
| `getNodeSelectList` | `POST` | `/k8s/node/list` | Node 下拉映射 |
| `getServiceList` | `POST` | `/k8s/svc/list` | Service 列表 |
| `getServiceSelectList` | `POST` | `/k8s/svc/list` | Service 下拉映射 |
| `getNodeDetail` | `POST` | `/k8s/node/info` | Node 详情 |

## 特殊处理

### Select 系列方法

以下方法都会对后端列表响应做二次格式化，返回 `{ [name]: name }` 的映射结构:

- `getNamespaceSelectList`
- `getWorkloadSelectList`
- `getPodSelectList`
- `getNodeSelectList`
- `getServiceSelectList`

适合直接喂给下拉组件。
