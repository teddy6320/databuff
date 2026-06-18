# Data Access API

> 文件: `src/api/dataAccess.ts`

## 概述

`dataAccess.ts` 主要服务于 DataHub/数据接入场景，覆盖管道管理、模板、处理器/数据源、收藏、实时数据预览与调试。

## 接口分组

### 管道管理

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getPipelineList` | `POST` | `/datahub/v1/pipeline/list` | 管道列表 |
| `getPipelineNameList` | `GET` | `/datahub/v1/pipeline/names` | 管道名称列表 |
| `savePipeline` | `POST` | `/datahub/v1/pipeline/create` / `/save` / `/createFromTemplate` | 创建、编辑、按模板创建管道 |
| `copyPipeline` | `POST` | `/datahub/v1/pipeline/{id}/copy/{name}` | 复制管道 |
| `togglePipelineStatus` | `PUT` | `/datahub/v1/pipeline/{id}/status/{status}` | 更新单个管道状态 |
| `batchTogglePipelineStatus` | `PUT` | `/datahub/v1/pipeline/batch/status/{status}` | 批量更新状态 |
| `batchDeletePipeline` | `DELETE` | `/datahub/v1/pipeline/batch` | 批量删除管道 |
| `getPipelineDetail` | `GET` | `/datahub/v1/pipeline/{id}` | 管道详情 |
| `batchGetPipelineDetail` | `GET` | `/datahub/v1/pipeline/pipelineListDetail?pipelineIds=...` | 批量获取详情 |
| `exportPipeline` | `GET` | `/datahub/v1/pipeline/export?pipelineIds=...&format=json` | 导出管道配置 |
| `importPipeline` | `POST` | `/datahub/v1/pipeline/import` | 导入管道配置 |

### 模板

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getTemplateList` | `GET` | `/datahub/v1/template/list` | 模板列表 |

### 处理器与数据源

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `saveProcessor` | `POST` / `PUT` | `/datahub/v1/processor/create` 或 `/{processorId}/config` | 创建或编辑处理器/数据源 |
| `deleteProcessor` | `DELETE` | `/datahub/v1/pipeline/processor/{id}` | 删除处理器/数据源 |
| `batchDeleteProcessor` | `DELETE` | `/datahub/v1/processor/batch` | 批量删除处理器/数据源 |
| `getProcessorExplain` | `GET` | `/datahub/v1/processor/readme` | 获取处理器说明 |
| `getFavoritesData` | `GET` | `/datahub/v1/processor/favorites/grouped/{type}?order=` | 获取收藏数据 |
| `toggleFavorite` | `PUT` | `/datahub/v1/processor/{id}/favorite?favorite=...` | 收藏/取消收藏 |

### 预览与调试

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getRemapping` | `GET` | `/datahub/v1/remapping/getRemapping` | 获取标准映射模板 |
| `getPipelineRealtimeData` | `GET` | `/datahub/v1/processor/receiverData?pipelineId=...` | 获取实时数据 |
| `testProcessor` | `POST` | `/datahub/v1/processor/debug` | 调试处理器 |

## 特殊处理

### `getPipelineList`

如果前端传入排序字段:

```ts
{
  sortField,
  sortOrder
}
```

方法内部会自动转换成:

```ts
{
  orderBy,
  order
}
```

### `savePipeline`

该方法会根据入参自动切换三种模式:

- 默认走 `/datahub/v1/pipeline/create`
- 有 `pipelineId` 时走 `/datahub/v1/pipeline/save`
- 有 `templateId` 时走 `/datahub/v1/pipeline/createFromTemplate?...`

其中模板创建时会从请求体里删除 `templateId`。

### `getPipelineRealtimeData` / `testProcessor`

这两个方法支持显式传入 `cancelToken`，适合实时预览和调试场景取消旧请求。
