# 驾驶舱模块

> 模块入口: `src/views/cockpit/index.vue`

## 模块概述

`cockpit` 是一个单路由、多页签的综合视图模块，用来把实体健康、告警、业务系统和故障排查信息聚合到同一页面中。

当前可访问的页签有：

- 工作台
- 业务系统
- 告警
- 故障排查

## 页面矩阵

| 类型 | 路由 | 文件 | 说明 |
|------|------|------|------|
| 页面 | `/cockpit` | `index.vue` | 驾驶舱容器页，内部按 `type` 切换页签 |
| 页签 | `/cockpit?type=overview` | `tab/overview.vue` | 工作台 |
| 页签 | `/cockpit?type=business` | `tab/business.vue` | 业务系统健康概览 |
| 页签 | `/cockpit?type=alarm` | `tab/alarm.vue` | 业务系统 / 服务告警概览 |
| 页签 | `/cockpit?type=fault` | `tab/fault/index.vue` | 故障排查与根因分析入口 |

## 关键参数

- `type`: 当前页签，允许值为 `overview`、`business`、`alarm`、`fault`
- `__ps`、`__nw`: 路由切换时会原样保留的菜单上下文参数

## 典型导航关系

- 工作台 -> 告警详情: `/alarmCenter/alarmDetail?aid=...`
- 工作台 -> 告警列表: `/alarmCenter/alarm?...`
- 业务系统页签 -> 业务系统页: `/appMonitor/businessSystem`
- 业务系统页签 -> 告警列表: `/alarmCenter/alarm?busName=...`
- 告警页签 -> 业务系统 / 服务详情
- 故障排查 -> 告警列表 / 错误分析 / 服务详情 / 根因分析

## 主要依赖 API

- `src/views/cockpit/api.ts`: 驾驶舱工作台、告警、业务系统概览
- `service.ts`: 故障排查所需的健康配置、趋势、服务排序
- `alarm.ts`: 工作台告警列表

## 当前实现说明

- `tab/application.vue` 仍在代码中，但 `tabnav.vue` 已把 `rum` 页签注释掉，当前用户界面无法切换到该页
- 因此本轮文档只覆盖当前可访问的 4 个页签

## 已补文档

- [工作台](workbench.md)
- [业务系统](business.md)
- [告警](alarm.md)
- [故障排查](fault.md)
