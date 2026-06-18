# OneAgent 部署状态

## 页面说明

OneAgent 部署状态页对应 `/config/status?type=agent`，是部署状态模块的主页面。页面先展示 Agent 整体概况，再展示宿主机列表，并提供批量启动、停止、重启、升级、修改配置和自动注入开关等运维操作。

## 页面结构

| 区域 | 说明 |
|------|------|
| 统计图区 | Agent 概况、版本分布、上传数据量 Top、CPU 使用率 Top、宿主机时间偏离 Top |
| 查询区 | `query` 关键字、`statuses` 多选状态筛选 |
| 列表区 | 宿主机名称、Host IP、状态、自动注入、版本、宿主机时间、系统环境、CPU、内存 |
| 批量操作区 | 自动注入开关、启动/停止/重启、更新、修改配置、更新包管理、安装 Agent |
| 弹窗/抽屉 | 更新 Agent 四步弹窗、修改配置 YAML 抽屉 |

## 关键参数

| 参数 | 说明 |
|------|------|
| `type` | 页签值，当前为 `agent` |
| `query` | 宿主机名称或 IP 模糊搜索 |
| `statuses` | 状态筛选，值来自 `Online` / `Anomaly` / `Offline` |

## 主要接口

| 接口 | 说明 |
|------|------|
| `getOnline` | 获取在线/异常/离线统计 |
| `versionSpread` | 获取版本分布 |
| `getUploadTop` / `getCpuTop` / `timeDiffTop` | 获取 Top 图表数据 |
| `getList` | 获取 Agent 列表 |
| `submitUpdate` | 执行更新、重启、停止、启动、上传日志操作 |
| `updatePreload` | 开关自动注入 |
| `readUpdateMsg` | 清理已读异常操作记录 |
| `getAgentDetail` / `configUpdate` | 查看并更新 Agent 配置 |
| `getVersionList` | 更新弹窗中获取可选版本列表 |
| `getsysdate` | 获取平台当前时间，用于格式化宿主机时间 |

## 页面流转

1. 页面初始先加载概览图，再按查询条件加载 Agent 列表。
2. 点击“安装Agent”跳转到 `/config/install?type=agent`。
3. 点击“更新包管理”跳转到 `/config/agentPackages`。
4. 单行或批量更新会打开四步式更新弹窗，版本选择后调用 `submitUpdate`。
5. 单行或批量“修改配置”会打开 YAML 抽屉，保存时调用 `configUpdate`。
6. 点击“运行日志”跳转到 `/config/runLog?host=...`。
7. 宿主机时间偏差过大时，可跳转到 `/help/timeSync` 查看处理建议。

## 注意事项

- 页面会根据 `goos`、`isK8s`、版本号判断某些能力是否可用，比如启动/停止/重启、更新和自动注入。
- 宿主机时间会在前端每秒自增展示，避免表格停留时看起来“冻结”。
- Agent 状态成功记录超过 12 小时后会在列表中自动隐藏对应操作提示。
