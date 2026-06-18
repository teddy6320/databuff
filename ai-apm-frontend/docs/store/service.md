# Service Store

## 模块定位

`Service` 模块源码在 [src/store/modules/service/index.ts](/src/store/modules/service/index.ts)，负责缓存基础服务列表、当前管理域服务、业务树，以及服务到业务/子系统的映射关系。

## State

| 字段 | 说明 |
|------|------|
| `basicServiceMap` | 全量服务基础信息映射 |
| `basicGroupServiceMap` | 当前管理域下的服务映射 |
| `businessTree` | 业务系统树 |
| `serviceBusinessMap` | 服务与业务/子系统映射 |

## 关键 Actions

| Action | 说明 |
|------|------|
| `GET_BASIC_SERVICE` | 获取所有服务基础信息 |
| `GET_BASIC_GROUP_SERVICE` | 获取当前管理域下服务基础信息，已有缓存时不重复请求 |
| `GET_BUSINESS_TREE` | 获取业务系统树 |

## 关键 Mutations

| Mutation | 说明 |
|------|------|
| `SET_BASIC_SERVICE` | 构建全量服务映射，同时按 `id` 和 `service` 双 key 建索引 |
| `SET_BASIC_GROUP_SERVICE` | 构建当前管理域服务映射 |
| `SET_BUSINESS_TREE` | 格式化业务树，并生成 `serviceBusinessMap` |

## 数据结构特点

### 基础服务映射

- `basicServiceMap` 会同时保存：
  - `basicServiceMap[service.id]`
  - `basicServiceMap[service.service]`
- 这样页面既可以按服务 ID，也可以按服务名查到基础信息。

### 业务树

- `SET_BUSINESS_TREE` 会把后端业务树里的：
  - `name -> label`
  - `id -> value`
  - `hasPermission -> disabled`
  统一转成适合树组件和选择器使用的格式。

### 服务到业务映射

- `serviceBusinessMap` 会给每个服务建立：
  - 所属业务系统
  - 所属子系统
  的双层映射。
- 同时按服务 ID 和服务名都建索引。

## 使用建议

- 需要快速拿服务基础信息时，优先走 `basicServiceMap`，不要每次单独请求。
- 需要把服务归属到业务系统时，直接使用 `getServiceBusinessMap` getter。
