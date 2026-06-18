# Business System API

> 文件: `src/api/bs.ts`
> 辅助 mock: `src/api/bs.mock.ts`

## 概述

`bs.ts` 覆盖业务系统/子系统管理、空间地图/系统拓扑、上下游调用分析、业务规则、RUM 关联、拓扑布局和业务线分组等能力，是“业务系统域”最核心的 API 文件。

## 接口分组

### 业务系统管理

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `addBs` | `POST` | `/business/addSys` | 新建业务系统/子系统 |
| `updateBs` | `POST` | `/business/updateSys` | 更新业务系统/子系统 |
| `deleteBs` | `DELETE` | `/business/delSys?id=...` | 删除业务系统/子系统 |
| `getBusAndSubList` | `POST` | `/business/busAndSubList` | 查询用户可见业务系统与子系统 |
| `getNamespaceList` | `POST` | `/service/k8sNamespaceList` | 查询服务 Namespace 列表 |
| `getBsTree` | `POST` | `/business/businessSystemTree` | 业务系统树 |

### 空间地图与系统拓扑

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getSpaceMap` | `POST` | `/spaceMap/allResource` | 空间地图节点数据 |
| `getSpaceMapEdge` | `POST` | `/spaceMap/allEdge` | 空间地图边数据 |
| `getSystemMap` | `POST` | `/spaceMap/allResource` | 系统拓扑节点数据 |
| `getSystemMapEdge` | `POST` | `/spaceMap/allEdge` | 系统拓扑边数据 |
| `getSpaceMapNpmEdge` | `POST` | `/spaceMap/allNpm` | 空间地图网络边数据 |
| `getSpaceMapServices` | `POST` | `/spaceMap/services` | 空间地图服务数据 |
| `getSpaceMapBusinesses` | `POST` | `/spaceMap/business` | 空间地图业务系统数据 |
| `getVerticalTree` | `POST` | `/spaceMap/verticalTree` | 空间地图堆栈树 |
| `getLayout` | `POST` | `/spaceMap/getSpaceMapLayout` | 获取拓扑布局 |
| `setLayout` | `POST` | `/spaceMap/saveSpaceMapLayout` | 保存拓扑布局 |

### 业务统计与调用分析

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getBusinessStat` | `POST` | `/business/businessSystemStat` | 业务系统上下游统计 |
| `getRelationDetail` | `POST` | `/business/relations/details` | 依赖关系详情 |
| `getMainMetrics` | `POST` | `/business/metrics` | 业务系统/服务黄金指标 |
| `getBusinessCallInfo` | `POST` | `/business/call_info` | 调用关系基础信息 |
| `getBusinessCallGraphStats` | `POST` | `/business/call_graph_stats` | 调用关系图统计 |
| `getBusinessCallEndpoints` | `POST` | `/business/call_endpoints` | 调用关系端点列表 |
| `getNdmMetrics` | `POST` | `/business/resourceUsage` | 资源使用指标 |

### 规则与 RUM 关联

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `addBsWithRules` | `POST` | `/monitor/addSysWithRules` | 新建业务系统并配置规则 |
| `getBsRules` | `GET` | `/monitor/getBusinessSystemRules?sysId=...` | 查询业务系统规则 |
| `updateBsWithRules` | `POST` | `/monitor/updateBusinessSystemRules` | 更新业务系统规则 |
| `deleteBsWithRules` | `DELETE` | `/monitor/deleteBusinessSystemAndRules?sysId=...` | 删除业务系统及规则 |
| `updateRelationStatus` | `POST` | `/spaceMap/editSupportRum` | 更新空间地图关联 RUM 状态 |
| `getRelationStatus` | `GET` | `/spaceMap/supportRum` | 获取空间地图关联 RUM 状态 |

### 业务线分组

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `addBsGroup` | `POST` | `/business/lines/create` | 新建业务线分组 |
| `editBsGroup` | `PUT` | `/business/lines/{id}` | 编辑业务线分组 |
| `deleteBsGroup` | `DELETE` | `/business/lines/{id}` | 删除业务线分组 |
| `getGroupTree` | `POST` | `/business/lines/tree` | 获取业务线树 |

## 特殊处理

### `getLayout`

默认会以 `{ type: 'service' }` 请求布局数据。

### `getGroupTree`

方法内部会自动附带:

```ts
{ skipPermissionCheck: true }
```

### `bs.mock.ts`

`bs.mock.ts` 提供了空间地图节点/边的 mock 数据生成函数，当前在 `bs.ts` 里仅以注释形式保留调试示例，没有实际参与线上请求。
