# Config API

> 文件: `src/api/config.ts`

## 概述

`config.ts` 主要覆盖平台高级配置、服务配置、AI 配置以及平台 Logo/名称/版权等展示配置。

## 接口分组

### 高级配置

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getConfigTypes` | `GET` | `/dConfigManage/getChildrenPaths` | 获取配置分类 |
| `getConfigList` | `GET` | `/dConfigManage/getPathData?path=...` | 根据分类获取配置列表 |
| `saveConfig` | `POST` | `/dConfigManage/saveNode` | 保存配置项 |
| `deleteConfig` | `DELETE` | `/dConfigManage/deleteNode?path=...` | 删除配置项 |

### 全局与应用配置

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `saveGlobalConfig` | `POST` | `/serviceConfig/global/saveNode` | 保存全局配置 |
| `resetGlobalConfig` | `DELETE` | `/serviceConfig/global/reset` | 重置全局配置为默认 |
| `saveServiceConfig` | `POST` | `/serviceConfig/service/saveNode` | 保存应用配置 |
| `resetServiceConfig` | `DELETE` | `/serviceConfig/service/reset?path=...` | 重置应用配置为默认 |

### AI 配置

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getAIConfig` | `GET` | `/api/ai/getAIConfig` | 获取 AI 配置 |
| `updateAIConfig` | `POST` | `/api/ai/updateAIConfig` | 更新 AI 配置 |
| `testAIConfig` | `POST` | `/api/ai/testAIConfig` | 测试 AI 配置 |

### 平台展示配置

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getLogoConfig` | `GET` | `/system/logoConfig` | 获取 Logo/平台名称配置 |
| `saveLogoConfig` | `POST` | `/system/saveLogoConfig` | 保存 Logo 配置 |
| `resetLogoConfig` | `POST` | `/system/resetLogoConfig` | 重置 Logo 配置 |

## 使用特点

- 高级配置和服务配置都以“节点”形式保存
- 多个删除/重置接口通过查询参数传递 `path`
- AI 配置路径以 `/api/ai` 开头，最终仍会按通用请求前缀补成 `/webapi/api/ai/...`
