# API 模块文档

> 对应目录: `src/api/`
> 最后更新: 2026-04-05

## 概述

`src/api/` 负责封装前端对后端接口的访问，按业务域拆分文件。大部分模块导出一个默认对象，内部以方法名区分具体接口；少数文件还会导出辅助类型或格式化函数。

## 通用约定

### 请求入口

所有 API 最终都通过 `src/utils/axios.ts` 导出的 `http` 实例发起请求。

### URL 前缀

- 默认会自动拼接 `/webapi`
- 以 `/webapi`、`/api6972`、`/localapi` 开头的地址不会重复加前缀

### 认证与请求头

- 登录后会自动注入 token 相关请求头
- 部分图表接口会自动在 `from` 条件中插入环境标签

### 取消请求

- 所有请求默认挂载取消令牌
- 路由切换时，未完成请求可能被统一中断，错误消息表现为 `interrupt`

### 响应处理

- 非 `blob` 响应默认直接返回 `response.data`
- 若响应状态标识为 `3000`，会触发登录失效处理
- 个别 API 会通过 `transformResponse` 对返回结构做二次格式化

## 当前 API 文件

| 文件 | 说明 |
|------|------|
| [agent.md](agent.md) | Agent 包管理、升级、日志、安装配置 |
| [alarm.md](alarm.md) | 告警、通知、策略、系统事件、AI |
| [apm.md](apm.md) | APM 服务概览、调用链、慢接口、智能分析 |
| [bs.md](bs.md) | 业务系统、空间地图、拓扑、规则、业务线 |
| [config.md](config.md) | 高级配置、服务配置、AI 与平台展示配置 |
| [data-access.md](data-access.md) | DataHub 管道、模板、处理器、调试 |
| [data-coll.md](data-coll.md) | 请求属性采集配置 |
| [env-tag.md](env-tag.md) | 环境标签、标签值、角色绑定 |
| [group.md](group.md) | 管理域、规则、绑定与自动分组 |
| [infrastructure.md](infrastructure.md) | 主机、容器、进程等基础设施 |
| [kubernetes.md](kubernetes.md) | K8s 集群、Namespace、Workload、Pod、Node、Service |
| [log.md](log.md) | 日志检索、筛选条件与 mock 数据 |
| [metric.md](metric.md) | 指标分类、详情、图表、核心配置 |
| [monitor.md](monitor.md) | 检测规则、预设规则、系统规则 |
| [notice.md](notice.md) | 邮件、短信、IM、Webhook、Socket 通知配置 |
| [npm.md](npm.md) | 网络性能监控与 DNS 分析 |
| [plugin.md](plugin.md) | 插件安装、插件指标、插件预设规则 |
| [process.md](process.md) | 进程采集/识别规则 |
| [report.md](report.md) | 报告、模板、图片与历史数据 |
| [root-cause.md](root-cause.md) | 问题、根因分析、影响面分析 |
| [scene.md](scene.md) | 业务场景、事件、漏斗、KPI |
| [service.md](service.md) | 服务、调用分析、数据库、Dump、线程分析 |
| [system.md](system.md) | 系统管理、角色、账户、审计、拓扑设置 |
| [user.md](user.md) | 登录、用户信息、组织、单点登录 |

## 辅助文件

| 文件 | 说明 |
|------|------|
| `bs.mock.ts` | 业务系统空间地图 mock 数据 |
| `infrastructure.types.ts` | 基础设施类型定义 |
| `metric.types.ts` | 指标图表参数类型定义 |
| `notice.type.ts` | 通知配置类型定义 |

## 推荐阅读顺序

1. [service.md](service.md)
2. [alarm.md](alarm.md)
3. [apm.md](apm.md)
4. [monitor.md](monitor.md)
5. [notice.md](notice.md)
6. [bs.md](bs.md)
7. [scene.md](scene.md)
8. [config.md](config.md)
9. [group.md](group.md)
10. [env-tag.md](env-tag.md)
11. [data-access.md](data-access.md)
12. [kubernetes.md](kubernetes.md)
13. [infrastructure.md](infrastructure.md)
14. [metric.md](metric.md)
15. [user.md](user.md)
16. [system.md](system.md)
17. [root-cause.md](root-cause.md)

## 相关文档

- [架构概述](../architecture/overview.md)
