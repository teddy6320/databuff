# Scene API

> 文件: `src/api/scene.ts`

## 概述

`scene.ts` 对应业务场景分析域，覆盖场景、场景组、业务事件、场景地图、漏斗/KPI 和事件属性分析。

## 接口分组

### 场景与场景组

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `addScene` | `POST` | `/biz/addBizScenario` | 创建场景 |
| `deleteScene` | `DELETE` | `/biz/delBizScenario?id=...` | 删除场景 |
| `editScene` | `POST` | `/biz/updateBizScenario` | 编辑场景 |
| `getSceneGroup` | `POST` | `/biz/bizGroupScenarios` | 查询场景分组 |
| `addGroup` | `POST` | `/biz/addBizGroup` | 新建场景组 |
| `getGroup` | `GET` | `/biz/bizGroupList` | 场景组列表 |
| `getSceneMap` | `POST` | `/biz/bizScenarioMap` | 场景地图 |
| `getSceneMapInfo` | `POST` | `/biz/bizScenarioMap/v2` | 场景地图 v2/mock 接口 |
| `getSceneNames` | `POST` | `/biz/bizScenarios` | 场景名称列表 |

### 业务事件

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getEventDetail` | `POST` | `/biz/bizEventInfo` | 事件详情 |
| `addEvent` | `POST` | `/biz/addBizEvent` | 新建事件 |
| `getEvents` | `POST` | `/biz/bizEventList` | 事件列表 |
| `editEvent` | `POST` | `/biz/updateBizEvent` | 编辑事件 |
| `deleteEvents` | `DELETE` | `/biz/delBizEvent?id=...` | 删除事件 |
| `getEventRelation` | `POST` | `/biz/bizScenarios` | 查询事件关联场景 |
| `getEventAttrs` | `GET` | `/biz/event/{eventId}/kpi-attributes` | 事件 KPI 属性 |
| `getEventTrend` | `POST` | `/biz/events/trends` | 事件趋势 |
| `getNodeEvents` | `POST` | `/biz/nodes/eventsByService` | 按服务节点查询事件 |
| `getMetaDetail` | `POST` | `//biz/events/metadata` | 事件元数据详情 |

### 分析辅助

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getExampleSpanAttrs` | `POST` | `/trace/sample/span` | 获取示例 Span 属性 |
| `getBizKpis` | `POST` | `/biz/scenarios/kpis` | 业务 KPI |
| `getBizFunnel` | `POST` | `/biz/funnel/detailed-path` | 业务漏斗 |

## 特殊处理

### `getSceneNames`

当前实现复用了 `/biz/bizScenarios` 接口，并以空对象 `{}` 发起请求。

### `getMetaDetail`

当前代码里的路径字面量是 `//biz/events/metadata`，文档按实现原样记录。
