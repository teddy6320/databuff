# 网络性能模块

> 模块入口: `src/views/npm/index.vue`

## 模块概述

`npm` 模块用于从网络连接视角观察客户端与服务端之间的通信情况，当前包含三类页面：

- 网络分析：查看流量、RTT、抖动、重传等指标趋势和连接列表
- 网络拓扑：按维度聚合连接关系，展示节点与边的拓扑图
- DNS 分析：查看 DNS 请求量、错误率和响应时间

三个页面都挂在 `/npm` 一级菜单下，均支持全局时间范围和手动刷新。

## 页面矩阵

| 类型 | 路由 | 文件 | 说明 |
|------|------|------|------|
| 页面 | `/npm/analysis` | `analysis/index.vue` | 网络分析主页面 |
| 页面 | `/npm/topology` | `topology/index.vue` | 网络拓扑页 |
| 页面 | `/npm/dns` | `dns/index.vue` | DNS 分析页 |

## 典型导航关系

- 网络拓扑节点点击 -> 网络分析: `/npm/analysis?client=...&server=...&from=...&conn=OR`
- 网络拓扑连线点击 -> 网络分析: `/npm/analysis?client=...&server=...&from=...&conn=AND`
- 网络分析列表行点击 -> 当前页抽屉详情
- DNS 分析列表行点击 -> 当前页抽屉详情

## 主要依赖 API

- `npm.ts`: 网络分析筛选、趋势、列表、拓扑、DNS 分析

详细接口见:

- [NPM API](../../api/npm.md)

## 已补文档

- [网络分析](analysis.md)
- [网络拓扑](topology.md)
- [DNS 分析](dns.md)
