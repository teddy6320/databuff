# 配置安装模块

> 模块入口: `src/views/configInstall/index.vue`

## 模块概述

`config-install` 是“部署配置 -> 安装部署”入口，主要提供 OneAgent、APM、NPM、日志采集、插件和数据接入的安装或配置指引。

根页面以 `db-tabnav` 组织多个安装页，当前代码实际开放了 6 个 tab:

- `agent`
- `plugin`
- `apm`
- `npm`
- `log`
- `dataAccess`

## 页面矩阵

| 类型 | 路由/状态 | 文件 | 说明 |
|------|------|------|------|
| 页面 | `/config/install?type=agent` | `agent/index.vue` | OneAgent 安装 |
| 页面 | `/config/install?type=plugin` | `plugin/index.vue` | 插件启用与配置 |
| 页面 | `/config/install?type=apm` | `apm/index.vue` | APM 接入 |
| 页面 | `/config/install?type=npm` | `npm/index.vue` | NPM 安装命令 |
| 页面 | `/config/install?type=log` | `log/index.vue` | 日志采集配置 |
| 页面 | `/config/install?type=dataAccess` | `dataAccess/index.vue` | 数据接入与管道 |
| 静态页 | `/config/pipelineSetting?id=...` | `dataAccess/pipelineSetting/index.vue` | 管道配置 |

## 典型导航关系

- 安装部署 -> 部署状态 OneAgent 列表: `/config/status?type=agent`
- 数据接入 -> 管道配置: `/config/pipelineSetting?id=...`
- 插件详情 -> 检测规则设置: `/configManage/alarm/ruleSetting?...`
- APM 安装页 -> OneAgent 安装页: `/config/install?type=agent`

## 主要依赖 API

- `user.ts`: 安装包版本、授权语言
- `agent.ts`: 站点 / 机房地址
- `plugin.ts`: 插件列表、安装卸载、指标、预设规则
- `data-access.ts`: 管道、模板、收藏数据源/处理器、调试

## 注意事项

- `config/install` 当前并不是“纯安装步骤页”，而是把“安装、启用、配置、管道编排”都放在同一个入口
- `NPM` 页面虽然不在旧目录规划里，但当前代码已经作为正式 tab 开放，因此本轮文档已纳入

## 已补文档

- [OneAgent 安装](agent.md)
- [APM 配置](apm.md)
- [NPM 安装](npm.md)
- [日志采集](log.md)
- [插件配置](plugin.md)
- [数据接入](data-access.md)
