# NPM API

> 文件: `src/api/npm.ts`

## 概述

`npm.ts` 对应网络性能监控场景，覆盖性能分析、流量详情、拓扑图以及 DNS 网络性能分析。

## 接口分组

### 网络性能分析

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getPerformanceTags` | `POST` | `/metrics/npm/performance/tags` | 网络分析筛选项 |
| `getPerformanceMetrics` | `GET` | `/metrics/npm/performance/metrics` | 趋势指标列表 |
| `getPerformanceMetricsData` | `POST` | `/metrics/npm/performance` | 指标趋势数据 |
| `getPerformanceList` | `POST` | `/metrics/npm/performance/list` | 网络分析列表 |
| `getPerformanceVolumeList` | `POST` | `/metrics/npm/volume/details` | 流量详情列表 |
| `getNpmTopoNodes` | `POST` | `/metrics/npm/topo/nodes` | 拓扑节点 |
| `getNpmTopoEdges` | `POST` | `/metrics/npm/topo/edges` | 拓扑边 |

### DNS 网络分析

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getDnsPerformanceTags` | `POST` | `/metrics/npm/dns/performance/tags` | DNS 分析筛选项 |
| `getDnsPerformanceList` | `POST` | `/metrics/npm/dns/performance/list` | DNS 网络分析列表 |
| `getDnsPerformanceVolumeList` | `POST` | `/metrics/npm/dns/volume/details` | DNS 流量详情 |

## 使用特点

- 大部分网络分析接口都采用 `POST` 提交查询条件
- DNS 和普通网络分析在路径上保持一一对应，便于页面复用查询结构
