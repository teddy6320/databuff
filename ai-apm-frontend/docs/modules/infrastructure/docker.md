# Docker 容器

> 页面: `/infrastructure/docker`
> 文件: `src/views/infrastructure/docker/index.vue`

## 页面职责

Docker 页用于查看容器清单、分组与运行状态，并提供容器详情入口。

## 页面结构

- 顶部工具区: 时间范围、分组开关
- `search-group`: 容器检索与分组条件
- `db-table`: 容器列表

## 主要接口

- `InfraApi.getContainerList`
- `InfraApi.getContainerGroupList`
- `InfraApi.getGroupList`

详细接口见:

- [Infrastructure API](../../api/infrastructure.md)

## 关键参数

- `group`: 是否按分组视图展示
- 分组模式下使用 `InfraApi.getContainerGroupList`
- 普通模式下使用 `InfraApi.getContainerList`
- 详情页入口参数:
  - `containerId`: 容器 ID
  - `type`: 详情页签

## 详情页说明

容器详情页位于 `src/views/infrastructure/dockerDetail/index.vue`，页签包括:

- `metric`
- `baseinfo`
- `process`
- `trace`

详情页会继续通过 `InfraApi.getContainerList` 按容器 ID 查询当前实体，再分别触发对应页签的数据加载。

## 关联页面

- 容器详情: `/infrastructure/dockerDetail?containerId=...`
- `trace` 页签会继续查看容器内关联链路
