# Root Cause API

> 文件: `src/api/rootCause.ts`

## 概述

`rootCause.ts` 聚焦问题、根因分析与影响面分析，是“问题列表 / 问题详情 / 问题分析 / 手动根因分析”四个页面共同依赖的接口文件。

## 接口分组

### 问题列表与筛选

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getProblemList` | `POST` | `/webapi/api/influence/search` | 问题列表 |
| `getProblemQueryParams` | `POST` | `/webapi/api/influence/filterTags` | 问题筛选项 |

### 详情与分析

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getProblemDetail` | `GET` | `/webapi/api/influence/findById` | 问题详情 |
| `getInfluenceAnalysis` | `POST` | `/webapi/api/influence/analyse` | 影响面分析 |
| `getRootCauseAnalysisDetail` | `GET` | `/webapi/api/issues/{id}` | 根因分析详情 |
| `getRootCauseAnalysis` | `POST` | `/root/analyse` | 手动根因分析 |

### 统计分析

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getRootCauseTypes` | `POST` | `/webapi/api/influence/chart/problemCauseType` | 问题类型分布 |
| `getRootCauseNodes` | `POST` | `/webapi/api/influence/chart/problemCauseNode` | 问题节点分布 |
| `getInfluenceConvergence` | `POST` | `/webapi/api/influence/convergence` | 问题收敛 |
| `getInfluenceMtt` | `POST` | `/webapi/api/influence/mtt` | MTTR / MTTA |
| `getInfluenceTrend` | `POST` | `/webapi/api/influence/metric` | 问题趋势 |

### 反馈

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `influenceFeedback` | `POST` | `/webapi/api/influence/feedback` | 定位准确性反馈 |

## 特殊处理

### `getProblemList`

如果传入的是前端表格常见参数:

```ts
{
  pageNum,
  pageSize,
}
```

方法内部会自动转换成后端需要的:

```ts
{
  page,
  size,
}
```

## 页面对应关系

- 问题列表: `getProblemList`、`getProblemQueryParams`、`getInfluenceTrend`
- 问题详情: `getProblemDetail`、`getInfluenceAnalysis`、`influenceFeedback`
- 问题分析: `getRootCauseTypes`、`getRootCauseNodes`、`getInfluenceConvergence`、`getInfluenceMtt`、`getInfluenceTrend`
- 手动根因分析: `getRootCauseAnalysis`

