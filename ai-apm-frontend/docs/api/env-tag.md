# Env Tag API

> 文件: `src/api/envTag.ts`

## 概述

`envTag.ts` 负责环境标签体系，包括标签列表、标签详情、标签值维护、开关状态以及角色绑定标签值。

## 接口分组

### 标签查询与维护

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getFilterOptions` | `GET` | `/env/tag/filterOptions` | 查询环境标签筛选下拉项 |
| `getTagList` | `POST` | `/env/tag/list` | 环境标签列表 |
| `getTagDetail` | `GET` | `/env/tag/info?tagId=...` | 标签详情 |
| `saveTag` | `POST` | `/env/tag/create` 或 `/env/tag/update` | 创建或编辑标签 |

### 标签值

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `saveTagValue` | `POST` | `/env/tag/value/create` 或 `/env/tag/value/update` | 创建或编辑标签值 |
| `deleteTagValue` | `POST` | `/env/tag/value/delete` | 删除标签值 |

### 状态与角色绑定

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getTagStatus` | `POST` | `/env/tag/status` | 获取环境标签开关状态 |
| `updateTagStatus` | `POST` | `/env/tag/updateStatus` | 更新环境标签开关状态 |
| `getTagListByUser` | `POST` | `/env/tag/listByUser` | 按用户角色查询标签列表 |
| `roleBindTag` | `POST` | `/env/tag/roleBind` 或 `/env/tag/roleUnbind` | 角色绑定/清空环境标签 |

## 特殊处理

### `saveTag` / `saveTagValue`

这两个方法都会根据是否存在 `id` 自动切换“创建”或“编辑”接口:

- 有 `id` 时走 `update`
- 无 `id` 时走 `create`

### `roleBindTag`

该方法会根据 `tagValueIds` 内容自动切换请求:

- 若 `tagValueIds` 为空，调用 `/env/tag/roleUnbind` 清空角色绑定
- 若 `tagValueIds` 有值，会把数组转换为 `{ roleId, tagValueId }[]` 后调用 `/env/tag/roleBind`
