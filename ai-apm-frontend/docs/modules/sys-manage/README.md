# 系统管理模块

> 模块入口: 路由层位于 `src/router/route-data.ts` 的 `/sysManage/*`

## 模块概述

`sys-manage` 负责平台级管理能力，包括账号、角色、组织、管理域、License、通知通道、系统基础设置、系统事件、系统规则、操作审计与健康度配置。

需要注意的是，当前路由把这些页面挂在“配置管理”一级菜单下，但页面实际路径与实现目录都归属于 `sysManage`。

## 页面矩阵

| 类型 | 路由 | 文件 | 说明 |
|------|------|------|------|
| 页面 | `/sysManage/account` | `account/index.vue` | 账户管理 |
| 页面 | `/sysManage/role` | `role/index.vue` | 角色权限 |
| 页面 | `/sysManage/org` | `org/index.vue` | 组织管理 |
| 页面 | `/sysManage/group` | `group/index.vue` | 管理域 |
| 页面 | `/sysManage/license` | `license/index.vue` | 许可证管理 |
| 页面 | `/sysManage/notice` | `notice/index.vue` | 通知配置 |
| 页面 | `/sysManage/basic` | `basic/index.vue` | 基础设置 |
| 页面 | `/sysManage/systemEvent` | `systemEvent/index.vue` | 系统事件 |
| 页面 | `/sysManage/operationAudit` | `operationAudit/index.vue` | 操作审计 |
| 页面 | `/sysManage/health` | `health/index.vue` | 健康度配置 |
| 静态页 | `/sysManage/systemRule` | `systemRule/index.vue` | 系统检测规则 |
| 静态页 | `/sysManage/ruleSetting` | `configManage/alarm/ruleSetting/index.vue` | 新建/编辑系统规则 |
| 静态页 | `/sysManage/eventDetail` | `alarmCenter/eventDetail/index.vue` | 系统事件详情 |
| 静态页 | `/sysManage/group/entity` | `group/entity/index.vue` | 未分配管理域的实体 |

## 典型导航关系

- 系统事件 -> 系统事件详情: `/sysManage/eventDetail?eid=...`
- 系统事件 -> 系统规则: `/sysManage/systemRule`
- 系统规则 -> 规则设置: `/sysManage/ruleSetting?...`
- 管理域 -> 未分配管理域实体: `/sysManage/group/entity`
- 操作审计 -> 多个业务/配置页: 根据实体类型跳转到 `sysManage`、`configManage`、`appMonitor`、`infrastructure`、`alarmCenter`

## 主要依赖 API

- `system.ts`: 账户、角色、基础设置、操作审计
- `user.ts`: 组织、License、登出
- `group.ts`: 管理域、未分配实体、角色绑定管理域
- `notice.ts`: 通知通道配置与测试
- `alarm.ts`: 系统事件列表
- `monitor.ts`: 系统规则详情
- `env-tag.ts`: 角色绑定环境

## 复用页面说明

- `/sysManage/eventDetail` 复用 `alarmCenter/eventDetail` 组件，详情参数仍然是 `eid`
- `/sysManage/systemRule` 复用 `configManage/alarm/rule/index.vue`，并以 `type="system"` 切换为系统规则模式
- `/sysManage/ruleSetting` 复用 `configManage/alarm/ruleSetting/index.vue`

## 已补文档

- [账户管理](account.md)
- [角色权限](role.md)
- [组织管理](org.md)
- [管理域](group.md)
- [许可证管理](license.md)
- [通知配置](notice.md)
- [基础设置](basic.md)
- [健康度配置](health.md)
- [操作审计](operation-audit.md)
- [系统事件](system-event.md)
- [系统规则](system-rule.md)
