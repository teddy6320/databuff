# 入门指南

## 页面说明

入门指南对应 `/help/startGuide`，页面通过 `type` 参数切换不同页签，用于帮助新用户快速完成主机接入、仪表盘创建、插件启用和用户管理。

## 页面结构

| 页签 | 说明 |
|------|------|
| `host` | 展示当前主机数量，支持跳转安装 Agent 或查看主机列表 |
| `dashboard` | 引导创建仪表盘 |
| `integration` | 展示已启用和热门插件，支持跳到安装部署模块 |
| `account` | 展示用户数，并跳转到账户或角色创建页 |

## 关键参数

| 参数 | 说明 |
|------|------|
| `type` | 当前页签，支持 `host` / `dashboard` / `integration` / `account` |

## 主要接口

| 接口 | 说明 |
|------|------|
| `getHostList` | `host` 页签获取主机总数与样例列表 |
| `getPluginList` | `integration` 页签获取已启用和热门插件 |
| `getAccountList` | `account` 页签获取当前用户总数 |

## 页面流转

1. 页面根据 `type` 切换当前页签，并在切换时回写到路由。
2. `host` 页签可以跳转到 `/config/install?type=agent` 或 `/infrastructure/host?mode=chart`。
3. `dashboard` 页签跳转到 `/dashboard?type=create`。
4. `integration` 页签跳转到 `/config/install?type=plugin&id=...`。
5. `account` 页签跳转到 `/sysManage/account?type=create` 或 `/sysManage/role?type=create`。

## 注意事项

- `dashboard` 页签会在没有 `/dashboard` 菜单权限时自动隐藏。
- `account` 页签会在没有 `/sysManage/account` 权限时自动隐藏。
- 该页面带有时间权限，主机统计会跟随全局时间变化刷新。
