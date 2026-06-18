# 数据接入

> 页面: `/config/install?type=dataAccess`
> 文件: `src/views/configInstall/dataAccess/index.vue`

## 页面职责

数据接入页用于维护数据管道、收藏数据源和处理器，并提供独立的管道编排配置页。

## 页面结构

- 左侧树:
  - `pipeline`
  - `favorites/source`
  - `favorites/processor`
- 管道列表:
  - 筛选
  - 批量启停
  - 导入 / 导出
  - 复制 / 删除
  - 创建管道
- 收藏列表:
  - 创建数据源 / 处理器
  - 删除
  - 查看被哪些管道引用

## 静态页

- 管道配置页: `/config/pipelineSetting?id=...`
  - 基于 X6 的拖拽式管道编排
  - 支持开始/停止调试
  - 支持收藏数据源与处理器

## 主要接口

- `AccessApi.getPipelineList`
- `AccessApi.getPipelineNameList`
- `AccessApi.getTemplateList`
- `AccessApi.savePipeline`
- `AccessApi.getPipelineDetail`
- `AccessApi.togglePipelineStatus`
- `AccessApi.batchTogglePipelineStatus`
- `AccessApi.batchDeletePipeline`
- `AccessApi.copyPipeline`
- `AccessApi.getFavoritesData`
- `AccessApi.deleteProcessor`
- `AccessApi.batchDeleteProcessor`
- `AccessApi.getProcessorExplain`

详细接口见:

- [Data Access API](../../api/data-access.md)

## 关键参数

- 模块页:
  - `type=dataAccess`
  - `cType`: `pipeline` / `source` / `processor`
- 管道配置页:
  - `id`: 管道 ID

## 典型流程

- 管道列表 -> 管道配置页: `/config/pipelineSetting?id=...`
- 创建管道成功后，如为新建场景会直接跳到管道配置页
- 收藏数据源/处理器可在管道编排页中直接拖入画布

## 注意事项

- 已启用管道不可删除
- 已被管道引用的收藏数据源/处理器不可删除
- `pipelineSetting` 属于数据接入的核心交互页，但它是独立静态路由，不在 tab 内直接渲染
