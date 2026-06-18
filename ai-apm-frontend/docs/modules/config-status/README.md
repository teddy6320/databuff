# 部署状态模块

## 模块概览

`config-status` 对应部署配置下的“部署状态”，入口路由是 `/config/status`。当前模块以 OneAgent 为核心，主页面负责查看 Agent 概况、筛选宿主机并执行批量操作，另外还有两个静态子页用于管理更新包和查看运行日志。

## 页面矩阵

| 页面 | 路由 | 说明 |
|------|------|------|
| 部署状态 | `/config/status` | OneAgent 概况、列表、批量操作 |
| 更新包管理 | `/config/agentPackages` | Agent 更新包上传与删除 |
| 运行日志 | `/config/runLog` | 宿主机运行日志获取与下载 |

## 主页面结构

| 区域 | 说明 |
|------|------|
| 页签区 | 当前只有 `OneAgent` 一个页签，通过 `type=agent` 控制 |
| 概览区 | Agent 在线状态、版本分布、上传量 Top、CPU Top、时间偏移 Top |
| 列表区 | 宿主机列表、状态、自动注入、版本、时间同步情况 |
| 操作区 | 批量启动/停止/重启/更新/修改配置、更新包管理、安装 Agent |

## 关键参数

| 参数 | 说明 |
|------|------|
| `type` | 主页面页签，当前固定为 `agent` |
| `query` | 按宿主机名称或 IP 搜索 |
| `statuses` | Agent 状态筛选，支持 `Online` / `Anomaly` / `Offline` |
| `host` | 运行日志页当前宿主机 |
| `__ps` / `__nw` | 主页面保留的壳层参数，切换页签时会透传 |

## 依赖接口

主要依赖 [Agent API](/docs/api/agent.md) 和少量 [System API](/docs/api/system.md)。

## 文档索引

- [OneAgent 部署状态](agent.md)
- [更新包管理](agent-packages.md)
- [运行日志](run-log.md)
