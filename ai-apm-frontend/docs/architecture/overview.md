# 架构概述

> 最后更新: 2026-04-05

## 项目定位

Databuff 是一个**可观测性平台**，提供全栈监控能力。

### 核心能力

| 能力域 | 说明 |
|--------|------|
| 基础设施监控 | 主机、容器、Kubernetes 集群监控 |
| 应用性能监控 (APM) | 服务调用链路、性能分析、错误追踪 |
| 用户体验监控 (RUM) | 前端 JS 错误、页面性能、用户行为 |
| 日志分析 | 日志采集、存储、检索、分析 |
| 指标分析 | 自定义指标查询与分析 |
| 智能告警 | 告警规则、通知、根因分析 |

---

## 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         前端应用 (Vue 2.7)                        │
├─────────────────────────────────────────────────────────────────┤
│  Views (业务页面)                                                │
│  ├── 驾驶舱 / 数据报表 / 智能告警                                  │
│  ├── 基础设施 / 应用监控 / 用户体验 / 日志 / 指标                   │
│  └── 配置管理 / 系统管理                                          │
├─────────────────────────────────────────────────────────────────┤
│  Components (共享组件)                                           │
│  ├── 图表组件 (ECharts, 火焰图)                                   │
│  ├── 业务组件 (db-table, db-menu, db-tabnav)                     │
│  └── 表单/展示/筛选组件                                           │
├─────────────────────────────────────────────────────────────────┤
│  State Management (Vuex)                                        │
│  ├── common    公共状态                                          │
│  ├── global    全局状态                                          │
│  ├── service   服务状态                                          │
│  └── user      用户状态                                          │
├─────────────────────────────────────────────────────────────────┤
│  API Layer (Axios)                                              │
│  └── RESTful API 调用                                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      后端服务 (待确认)                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## 前端架构分层

### 1. 视图层 (Views)

按业务模块组织，位于 `src/views/` 目录：

| 模块目录 | 功能 |
|----------|------|
| `cockpit/` | 驾驶舱 - 综合概览 |
| `dataReport/` | 数据报表 - 仪表盘、报告 |
| `alarmCenter/` | 智能告警 - 告警列表、通知、根因分析 |
| `infrastructure/` | 基础设施 - 主机、容器、K8s |
| `appMonitor/` | 应用监控 - 服务、链路、错误 |
| `rum/` | 用户体验 - JS 错误、页面性能 |
| `log/` | 日志分析 |
| `metrics/` | 指标分析 |
| `npm/` | 网络监控 |
| `configManage/` | 配置管理 |
| `configInstall/` | 配置安装 |
| `sysManage/` | 系统管理 |

### 2. 组件层 (Components)

位于 `src/components/` 目录：

| 分类 | 组件 |
|------|------|
| 图表 | ECharts 封装、火焰图 |
| 业务 | db-table、db-menu、db-tabnav、db-radio |
| 表单 | ip-input、metric-select、scroll-select |
| 展示 | code-view、marked-view、statistic |
| 筛选 | query-filter、matching-criteria |
| 特殊 | chat-ai、flame-chart |

### 3. 状态管理层 (Store)

位于 `src/store/` 目录，使用 Vuex 进行状态管理：

```
store/
├── index.ts           # Store 入口
├── mutation-types.ts  # Mutation 类型常量
└── modules/
    ├── common/        # 公共状态
    ├── global/        # 全局状态
    ├── service/       # 服务状态
    └── user/          # 用户状态
```

### 4. API 层

位于 `src/api/` 目录，封装所有后端接口调用：

```
api/
├── service.ts        # 服务相关 API
├── alarm.ts          # 告警 API
├── metric.ts         # 指标 API
├── infrastructure.ts # 基础设施 API
├── kubernetes.ts     # K8s 资源 API
├── apm.ts            # APM API
├── log.ts            # 日志 API
├── user.ts           # 用户管理 API
├── system.ts         # 系统配置 API
├── monitor.ts        # 监控 API
├── notice.ts         # 通知 API
├── config.ts         # 配置 API
├── agent.ts          # Agent API
├── rootCause.ts      # 根因分析 API
└── bs.ts             # 业务系统 API (含 Mock 数据)
```

### 5. 工具层 (Utils)

位于 `src/utils/` 目录：

| 文件 | 功能 |
|------|------|
| `axios.ts` | HTTP 请求封装 |
| `echarts.ts` | ECharts 配置 |
| `common.ts` | 通用工具函数 |
| `timeFormat.ts` | 时间格式化 |
| `getUnitData.ts` | 单位数据转换 |
| `regexp.ts` | 正则表达式 |
| `auth.ts` | 认证相关 |
| `cancelToken.ts` | 请求取消 |

---

## 路由设计

### 路由配置

位于 `src/router/` 目录：

```
router/
├── index.ts           # 路由入口
├── route-data.ts      # 路由数据配置 (主要)
├── route.types.ts     # 路由类型定义
├── breadcrumb-data.ts # 面包屑配置
└── time-new.ts        # 时间相关
```

### 路由结构

主要一级菜单：

| ID | 名称 | 路径 | 说明 |
|----|------|------|------|
| 1 | 驾驶舱 | `/cockpit` | 综合概览 |
| 2 | 数据报表 | `/dataReport` | 仪表盘、报告 |
| 3 | 智能告警 | `/alarmCenter` | 告警管理 |
| 4 | 基础设施 | `/infrastructure` | 待确认 |
| 5 | 应用监控 | `/appMonitor` | 待确认 |
| 6 | 用户体验 | `/rum` | 待确认 |
| 7 | 日志 | `/log` | 待确认 |
| 8 | 指标 | `/metrics` | 待确认 |
| 9 | 网络 | `/npm` | 待确认 |
| 10 | 配置管理 | `/configManage` | 待确认 |
| 11 | 配置安装 | `/configInstall` | 待确认 |
| 12 | 系统管理 | `/sysManage` | 待确认 |

---

## 数据流

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Views   │───▶│  Store   │───▶│   API    │───▶│  Backend │
│ (页面)    │◀───│ (状态)    │◀───│  (接口)   │◀───│  (后端)   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
      │               │
      │               │
      ▼               ▼
┌──────────┐    ┌──────────┐
│Components│    │  Utils   │
│ (组件)    │    │ (工具)    │
└──────────┘    └──────────┘
```

---

## 待确认事项

- [ ] 后端服务架构和技术栈
- [ ] 数据采集层架构 (Agent、数据接入方式)
- [ ] 数据存储方案 (时序数据库、日志存储等)
- [ ] 认证授权机制详情 (JWT/Session 等)
- [ ] 多租户设计
- [ ] 部署架构 (单机/分布式)
- [ ] 完整的路由结构

---

## 相关文档

- [技术栈说明](tech-stack.md)
- [目录结构](directory-structure.md)
