# Agent API

> 文件: `src/api/agent.ts`

## 概述

`agent.ts` 负责 Agent 包管理、升级任务、在线状态、日志查看、安装配置等能力，偏向系统运维与 Agent 管理页面。

## 接口分组

### 安装包与升级任务

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getVersionList` | `GET` | `/agent/packList` | 获取安装包版本列表 |
| `getUpdateInfo` | `GET` | `/agent/updateProgress` | 获取升级进度 |
| `submitUpdate` | `POST` | `/agent/submitUpPlan` | 提交升级/重启/停止/启动/上传日志计划 |
| `configUpdate` | `POST` | `/agent/modifyConfig` | 修改升级配置 |
| `deletePackage` | `DELETE` | `/agent/delPack?id=...` | 删除安装包 |
| `uploadPackage` | `POST` | `/agent/upload` | 上传安装包 |
| `updatePreload` | `POST` | `/agent/preload` | 预加载相关操作 |

### 状态与统计

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `versionSpread` | `GET` | `/agent/versionSts` | 版本分布统计 |
| `timeDiffTop` | `GET` | `/agent/timeDiffTop` | 时间差 Top |
| `getOnline` | `POST` | `/agent/online` | 在线状态统计 |
| `getList` | `POST` | `/agent/list` | Agent 列表 |
| `getDcSite` | `GET` | `/agent/getDcSite` | 获取机房/站点信息 |
| `getCpuTop` | `POST` | `/agent/cup_usage_top` | CPU 使用 Top |
| `getUploadTop` | `POST` | `/agent/upload_num_top` | 上传量 Top |
| `getAgentDetail` | `GET` | `/agent/info` | Agent 详情 |

### 日志与安装配置

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `readUpdateMsg` | `DELETE` | `/agent/delAgentUpdata?id=...` | 清除升级消息 |
| `getAgentLogLost` | `POST` | `/agent/agentLogList` | Agent 日志列表 |
| `updateAgentLogNew` | `POST` | `/agent/updateAgentLogNew` | 更新日志状态/内容 |
| `loadAgentLogContent` | `GET` | `/agent/readAgentLog` | 读取日志内容 |
| `getAgentInstallConfig` | `POST` | `/agent/getAgentInstallConfig` | 获取安装配置 |
| `updateAgentInstallConfig` | `POST` | `/agent/updateAgentInstallConfig` | 更新安装配置 |

## 特殊处理

### `submitUpdate`

`operation` 字段按当前代码约定:

- `0`: 更新
- `1`: 重启
- `2`: 停止
- `3`: 启动
- `4`: 上传日志

### `loadAgentLogContent`

会根据 `params.type` 切换响应类型:

- `type === 2` 时返回 `blob`
- 其他情况返回 `text`

### `getAgentInstallConfig`

当前实现使用 `POST`，但参数通过 `params` 传递，而不是请求体。
