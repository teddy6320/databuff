# Infrastructure API

> 文件: `src/api/infrastructure.ts`

## 概述

`infrastructure.ts` 主要覆盖基础设施监控场景下的主机、容器、进程及相关筛选、标签、详情与趋势接口。

## 接口分组

### 主机

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getHostList` | `POST` | `/base/host/list` | 主机列表 |
| `getHostGroupList` | `POST` | `/base/host/group_list` | 主机分组列表 |
| `getHostmapList` | `POST` | `/base/host/hostmap` | HostMap 数据 |
| `getHostInfo` | `GET` | `/base/host/info` | 主机详情 |
| `customHostTag` | `POST` | `/base/customHostTag` | 自定义主机标签 |
| `getHostTag` | `POST` | `/base/getHostTag` | 获取主机标签 |
| `findHostApps` | `POST` | `/base/findHostApps` | 主机应用筛选项 |
| `findHostOs` | `POST` | `/base/findHostOs` | 主机操作系统筛选项 |
| `setHostManagerIp` | `POST` | `/base/setManagerIp` | 设置主机管理 IP |
| `getHostObjs` | `POST` | `/base/objs` | 主机基础对象信息 |

### 容器

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getContainerList` | `POST` | `/base/container/list` | 容器列表 |
| `getContainerGroupList` | `POST` | `/base/container/group_list` | 容器分组列表 |

### 进程

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getProcessList` | `POST` | `/base/processGroup/v2/list` | 进程列表 |
| `getProcessDetail` | `POST` | `/base/processGroup/info` | 进程详情 |
| `getProcessGraph` | `POST` | `/base/v2/processGroup/graph` | 进程指标趋势 |

### 通用分组

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getGroupList` | `POST` | `/base/findMetrics` | 分组数据/指标分组信息 |

## 参数特点

- 多个列表接口接收的都是统一列表查询结构，如 `ListInterface`
- `getHostList`、`getContainerList` 同时兼容普通列表与滚动定位参数
- 主机、容器、进程列表基本都采用 `POST` 方式提交筛选条件

## 相关类型

该模块的类型定义集中在:

- `src/api/infrastructure.types.ts`

常见类型包括:

- `ListInterface`
- `ScrollId`
- `ProcessGroupList`
- `HostmapInterface`
- `GroupInterface`
- `GetHostTag`
- `HostnameInterface`

