# Group API

> 文件: `src/api/group.ts`

## 概述

`group.ts` 负责管理域能力，包含管理域本身的增删改查、规则维护、角色绑定、未纳管对象统计，以及自动分组等配置能力。

## 接口分组

### 管理域

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getGroupList` | `POST` | `/group/list` | 管理域列表 |
| `getGroupListByUser` | `GET` | `/user/findRoleGroupByUser` | 查询当前用户可见管理域 |
| `getGroupListByRole` | `GET` | `/group/getRoleGroup/{roleId}` | 查询角色绑定的管理域 |
| `addGroup` | `POST` | `/group/add` | 新建管理域 |
| `updateGroup` | `POST` | `/group/update` | 更新管理域 |
| `deleteGroup` | `POST` | `/group/delete` | 删除管理域 |

### 管理域规则

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getRuleList` | `POST` | `/group/rules` | 规则列表 |
| `addRule` | `POST` | `/group/addRule` | 新建规则 |
| `updateRule` | `POST` | `/group/updateRule` | 编辑规则 |
| `deleteRule` | `POST` | `/group/deleteRule` | 删除规则 |
| `getGroupStatus` | `POST` | `/group/status` | 查询管理域状态 |
| `updateStatus` | `POST` | `/group/updateStatus` | 更新管理域状态 |

### 角色绑定与未纳管对象

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `bindRoleGroup` | `POST` | `/group/roleBind` | 角色绑定管理域 |
| `unbindRoleGroup` | `POST` | `/group/roleUnbind` | 角色解绑管理域 |
| `getUngroupEntity` | `GET` | `/group/unGroupObjsStat` | 未纳管对象统计 |
| `getUngroupList` | `POST` | `/group/unGroupObjsList` | 未纳管对象列表 |

### 自动分组与数据源配置

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `updateAutoBsStatus` | `POST` | `/meta/config/updateEnable` | 更新自动生成分组开关 |
| `getAutoBsStatus` | `GET` | `/meta/config/getByCode?code=AUTO_GENERATED_GROUP` | 查询自动生成分组开关 |
| `setCustomGroup` | `POST` | `/group/dataSource/update` | 更新自定义分组数据源配置 |

## 特殊处理

### `getUngroupList`

该方法会先删除前端列表常见但后端接口不需要的字段:

- `fromTime`
- `toTime`
- `pageSize`
- `pageNum`

然后再提交到 `/group/unGroupObjsList`。

### `updateAutoBsStatus`

这个方法并不是直接请求 `/group` 域接口，而是通过元配置接口写入:

```ts
{ enabled, code: 'AUTO_GENERATED_GROUP' }
```
