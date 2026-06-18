# System API

> 文件: `src/api/system.ts`

## 概述

`system.ts` 覆盖“系统管理”域的大量接口，主要包含:

- 系统激活与基础设置
- 时间/NTP/页面超时
- 角色管理
- 账户管理
- 通知接收者
- 操作审计
- 拓扑中间件设置

## 系统状态与基础设置

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getActiveStatus` | `POST` | `/user/isActivate` | 获取系统激活状态 |
| `isOuttime` | `POST` | `/saasCustomer/isOutTime` | 判断是否过期 |
| `createAdminAduit` | `POST` | `/user/createAdminAduit` | 创建系统用户/初始化审核 |
| `getSystemBase` | `GET` | `/system/systemBase` | 获取系统基础设置 |
| `getsysdate` | `GET` | `/system/getDate` | 获取服务器时间 |
| `setsysdate` | `POST` | `/system/modifyDate` | 设置服务器时间 |
| `setNtpServer` | `POST` | `/system/ntpServer` | 设置 NTP 服务器 |
| `updatePageTimeOut` | `POST` | `/system/updatePageTimeOut` | 修改页面超时时间 |

## 角色管理

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getRoleList` | `POST` | `/user/findAllRole` | 角色列表 |
| `createRole` | `POST` | `/user/createRole` | 新建角色 |
| `editRole` | `POST` | `/user/editRoleById` | 编辑角色 |
| `deleteRole` | `POST` | `/user/delRoleById` | 删除角色 |
| `getPermis` | `POST` | `/user/getPermisByRoleId` | 获取角色权限 |
| `updatePermis` | `POST` | `/user/updatePermisByRoleId` | 配置角色权限 |

## 账户管理

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getAccountList` | `POST` | `/user/getAllAccountList` | 用户列表 |
| `getAccountListByRole` | `GET` | `/user/findUserByRole` | 查询角色关联用户 |
| `createAccount` | `POST` | `/user/regisAccount` | 新建用户 |
| `updateAccount` | `POST` | `/user/updateUserInfo` | 修改用户信息 |
| `resetPassword` | `POST` | `/user/resetUserPassByUserId` | 重置密码 |
| `deleteAccount` | `POST` | `/user/deleteUserByUserId` | 删除用户 |
| `forceOffline` | `POST` | `/user/forceOffline` | 强制下线 |
| `unLockAccount` | `POST` | `/user/unLockAccount` | 解锁用户 |

### `getAccountList` 特殊处理

该方法会自动:

- 把 `pageNum/pageSize` 转成后端使用的 `pagenum/pagesize`
- 把响应中的 `data.accountList` 摊平成 `data`

比较适合直接供列表页使用。

## 通知接收者

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getAllReceiverList` | `POST` | `/rcvUser/all` | 通知接收者列表 |
| `saveReceiver` | `POST` | `/rcvUser/save` | 创建/编辑接收者 |
| `saveReceiverByType` | `POST` | `/rcvUser/bindByType` | 绑定接收方式 |
| `unbindReceiverByType` | `POST` | `/rcvUser/unbindByType` | 解绑接收方式 |

## 操作审计

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getOperateAuditList` | `POST` | `/audit/search` | 审计列表 |
| `getOperateAuditTags` | `POST` | `/audit/tags` | 审计筛选项 |

## 拓扑中间件设置

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getMidStatus` | `GET` | `/topologySetting/middlewareType/show` | 获取开关状态 |
| `setMidEnable` | `GET` | `/topologySetting/middlewareType/enable` | 启用 |
| `setMidDisabled` | `GET` | `/topologySetting/middlewareType/disable` | 停用 |
| `getMidTypeList` | `GET` | `/topologySetting/middlewareType/list` | 中间件类型列表 |
| `getMidTypeMetricList` | `GET` | `/topologySetting/selectedMetrics/list?middlewareType=...` | 类型对应指标列表 |
| `setMidTypeMetric` | `POST` | `/topologySetting/selectedMetrics/save` | 保存类型指标配置 |

