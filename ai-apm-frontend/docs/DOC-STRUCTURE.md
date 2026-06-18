# 文档目录结构

> 此文件记录文档目录规划，用于后续补充文档时参考。
> 说明: 当前顶层目录已经全部落地；本文件同时保留目录清单、完成状态和后续扩展建议。
> 创建时间: 2026-04-05

```
docs/
├── README.md                          # 文档索引/导航 ✅
├── DOC-STRUCTURE.md                   # 文档目录结构 (本文件) ✅
│
├── architecture/                      # 架构文档
│   ├── overview.md                    # 整体架构概述 ✅
│   ├── tech-stack.md                  # 技术栈说明 ✅
│   └── directory-structure.md         # 目录结构说明 ✅
│
├── modules/                           # 业务模块文档
│   ├── README.md                      # 模块索引
│   │
│   ├── cockpit/                       # 驾驶舱 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── workbench.md               # 工作台
│   │   ├── business.md                # 业务系统
│   │   ├── alarm.md                   # 告警
│   │   └── fault.md                   # 故障排查
│   │
│   ├── data-report/                   # 数据报表 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── dashboard.md               # 仪表盘
│   │   └── report.md                  # 报告
│   │
│   ├── alarm-center/                  # 智能告警 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── alarm-list.md              # 告警列表
│   │   ├── alarm-detail.md            # 告警详情
│   │   ├── notice.md                  # 通知管理
│   │   ├── problem-list.md            # 问题列表
│   │   ├── problem-detail.md          # 问题详情
│   │   ├── problem-analysis.md        # 问题分析
│   │   ├── root-cause-analysis.md     # 根因分析
│   │   └── event-detail.md            # 事件详情
│   │
│   ├── infrastructure/                # 基础设施监控 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── host.md                    # 主机监控
│   │   ├── cluster.md                 # 集群监控
│   │   ├── node.md                    # 节点监控
│   │   ├── pod.md                     # Pod 监控
│   │   ├── namespace.md               # 命名空间
│   │   ├── workload.md                # 工作负载
│   │   ├── docker.md                  # 容器监控
│   │   ├── process.md                 # 进程监控
│   │   ├── service.md                 # 服务监控
│   │   └── tech.md                    # 技术栈
│   │
│   ├── app-monitor/                   # 应用性能监控 (APM)
│   │   ├── README.md                  # 模块索引
│   │   ├── service.md                 # 服务监控
│   │   ├── service-call.md            # 服务调用
│   │   ├── service-flow.md            # 服务流程
│   │   ├── trace.md                   # 链路追踪
│   │   ├── trace-detail.md            # 链路详情
│   │   ├── thread.md                  # 线程分析
│   │   ├── errors.md                  # 错误监控
│   │   ├── error-detail.md            # 错误详情
│   │   ├── database.md                # 数据库监控
│   │   ├── cache.md                   # 缓存监控
│   │   ├── msg-queue.md               # 消息队列
│   │   ├── http-conn-pool.md          # HTTP 连接池
│   │   ├── db-conn-pool.md            # 数据库连接池
│   │   ├── topology.md                # 系统拓扑
│   │   ├── relation-map.md            # 关系图谱
│   │   ├── diagnostic.md              # 诊断分析
│   │   ├── business-system.md         # 业务系统
│   │   └── business-call.md           # 业务调用
│   │
│   ├── rum/                           # 用户体验监控 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── application.md             # 应用管理
│   │   ├── page.md                    # 页面分析
│   │   ├── action.md                  # 行为分析
│   │   ├── js-error.md                # JS 错误
│   │   ├── request.md                 # 请求分析
│   │   ├── trace.md                   # 链路追踪
│   │   ├── session.md                 # 会话分析
│   │   ├── resource.md                # 资源分析
│   │   ├── user.md                    # 用户分析
│   │   └── help.md                    # 接入帮助
│   │
│   ├── log/                           # 日志分析 ✅
│   │   ├── README.md                  # 模块索引
│   │   └── analysis.md                # 日志分析
│   │
│   ├── metrics/                       # 指标体系 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── list.md                    # 指标列表
│   │   └── analysis.md                # 指标分析
│   │
│   ├── npm/                           # 网络性能 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── analysis.md                # 网络分析
│   │   ├── topology.md                # 网络拓扑
│   │   └── dns.md                     # DNS 分析
│   │
│   ├── observe/                       # 业务观测 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── scene.md                   # 业务场景
│   │   ├── scene-manage.md            # 场景地图编辑
│   │   └── event.md                   # 业务事件
│   │
│   ├── config-manage/                 # 配置管理 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── ai.md                      # AI 配置
│   │   ├── alarm.md                   # 告警配置
│   │   ├── entity.md                  # 实体管理
│   │   └── env-tag.md                 # 环境标签
│   │
│   ├── config-install/                # 配置安装 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── agent.md                   # Agent 安装
│   │   ├── apm.md                     # APM 配置
│   │   ├── data-access.md             # 数据接入
│   │   ├── log.md                     # 日志采集
│   │   ├── npm.md                     # NPM 安装
│   │   └── plugin.md                  # 插件配置
│   │
│   ├── config-status/                 # 部署状态 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── agent.md                   # OneAgent 部署状态
│   │   ├── agent-packages.md          # 更新包管理
│   │   └── run-log.md                 # 运行日志
│   │
│   ├── login/                         # 登录 ✅
│   │   └── README.md                  # 登录页
│   │
│   ├── authorization/                 # 授权管理 ✅
│   │   └── README.md                  # License 授权与超管初始化
│   │
│   ├── personal/                      # 个人中心 ✅
│   │   └── README.md                  # 个人资料与通知设置
│   │
│   ├── help/                          # 帮助中心 ✅
│   │   ├── README.md                  # 模块索引
│   │   ├── start-guide.md             # 入门指南
│   │   ├── time-sync.md               # 时间同步
│   │   ├── setup-ntp.md               # NTP 服务
│   │   └── nginx-tracing.md           # 未开放的 Nginx Tracing 页面说明
│   │
│   └── sys-manage/                    # 系统管理 ✅
│       ├── README.md                  # 模块索引
│       ├── account.md                 # 账户管理
│       ├── role.md                    # 角色权限
│       ├── org.md                     # 组织管理
│       ├── group.md                   # 分组管理
│       ├── license.md                 # 许可证管理
│       ├── notice.md                  # 通知配置
│       ├── basic.md                   # 基础设置
│       ├── health.md                  # 健康检查
│       ├── operation-audit.md         # 操作审计
│       ├── system-event.md            # 系统事件
│       └── system-rule.md             # 系统规则
│
├── components/                        # 共享组件文档 ✅
│   ├── README.md                      # 组件索引 ✅
│   │
│   ├── charts/                        # 图表组件 ✅
│   │   └── index.md                   # 图表组件总览 ✅
│   │
│   ├── business/                      # 业务组件 ✅
│   │   ├── db-table.md                # 表格组件 ✅
│   │   ├── db-tabnav.md               # 页签导航 ✅
│   │   ├── db-menu.md                 # 菜单组件 ✅
│   │   ├── db-radio.md                # 分段单选 ✅
│   │   └── dialog-template.md         # 弹窗模板 ✅
│   │
│   ├── form/                          # 表单组件 ✅
│   │   ├── ip-input.md                # IP 输入 ✅
│   │   ├── metric-select.md           # 指标选择 ✅
│   │   ├── metric-cascader.md         # 指标分类 ✅
│   │   ├── metric-unit-cascader.md    # 指标单位 ✅
│   │   └── scroll-select.md           # 选择器 ✅
│   │
│   ├── display/                       # 展示组件 ✅
│   │   ├── code-view.md               # 代码展示 ✅
│   │   ├── marked-view.md             # Markdown 展示 ✅
│   │   ├── statistic.md               # 统计值展示 ✅
│   │   ├── text-expand.md             # 文本折叠 ✅
│   │   ├── metric-info-tooltip.md     # 指标详情提示 ✅
│   │   └── collapse-tags.md           # 标签折叠 ✅
│   │
│   ├── filter/                        # 筛选组件 ✅
│   │   ├── query-filter.md            # 查询筛选 ✅
│   │   └── matching-criteria.md       # 条件编排 ✅
│   │
│   ├── layout/                        # 布局组件 ✅
│   │   ├── cont-wrapper.md            # 内容容器 ✅
│   │   └── router-view-temp.md        # 路由占位容器 ✅
│   │
│   └── special/                       # 特殊组件 ✅
│       ├── chat-ai.md                 # AI 抽屉 ✅
│       ├── flame-chart.md             # 火焰图库说明 ✅
│       └── db-icon-button.md          # 图标按钮 ✅
│
├── store/                             # 状态管理文档
│   ├── README.md                      # Store 索引 ✅
│   ├── common.md                      # 公共状态 ✅
│   ├── global.md                      # 全局状态 ✅
│   ├── service.md                     # 服务状态 ✅
│   └── user.md                        # 用户状态 ✅
│
├── api/                               # API 接口文档 ✅
│   ├── README.md                      # API 索引 ✅
│   ├── service.md                     # 服务相关 ✅
│   ├── alarm.md                       # 告警相关 ✅
│   ├── metric.md                      # 指标相关 ✅
│   ├── infrastructure.md              # 基础设施 ✅
│   ├── kubernetes.md                  # K8s 资源 ✅
│   ├── apm.md                         # APM ✅
│   ├── log.md                         # 日志 ✅
│   ├── user.md                        # 用户管理 ✅
│   ├── system.md                      # 系统配置 ✅
│   ├── monitor.md                     # 监控 ✅
│   ├── notice.md                      # 通知 ✅
│   ├── config.md                      # 配置 ✅
│   ├── agent.md                       # Agent ✅
│   ├── root-cause.md                  # 根因分析 ✅
│   ├── bs.md                          # 业务系统 ✅
│   ├── data-access.md                 # 数据接入 ✅
│   ├── data-coll.md                   # 属性采集 ✅
│   ├── group.md                       # 分组 ✅
│   ├── env-tag.md                     # 环境标签 ✅
│   ├── npm.md                         # 网络性能 ✅
│   ├── process.md                     # 进程配置 ✅
│   ├── report.md                      # 报告 ✅
│   ├── scene.md                       # 业务场景 ✅
│   └── plugin.md                      # 插件 ✅
│
├── router/                            # 路由文档
│   ├── README.md                      # 路由索引 ✅
│   ├── route-types.md                 # 路由类型定义 ✅
│   ├── route-data.md                  # 路由数据配置 ✅
│   ├── breadcrumb.md                  # 面包屑配置 ✅
│   └── time-options.md                # 时间选择器配置 ✅
│
├── utils/                             # 工具函数文档
│   ├── README.md                      # 工具索引 ✅
│   ├── axios.md                       # HTTP 请求 ✅
│   ├── echarts.md                     # ECharts 配置 ✅
│   ├── time-format.md                 # 时间格式化 ✅
│   ├── unit-data.md                   # 单位转换 ✅
│   ├── auth.md                        # 认证相关 ✅
│   ├── common.md                      # 通用工具 ✅
│   ├── regexp.md                      # 正则表达式 ✅
│   ├── cookie.md                      # Cookie 操作 ✅
│   ├── version.md                     # 版本比较 ✅
│   └── browser.md                     # 浏览器检测 ✅
│
└── guides/                            # 开发指南 ✅
    ├── getting-started.md             # 快速开始
    ├── coding-standards.md            # 编码规范
    ├── component-development.md       # 组件开发指南
    ├── api-development.md             # API 开发指南
    ├── documentation-maintenance.md   # 文档维护指南
    └── testing.md                     # 测试指南
```

---

## 进度追踪

| 目录 | 文件数 | 已完成 | 状态 |
|------|--------|--------|------|
| architecture | 3 | 3 | ✅ 完成 |
| modules | 107 | 107 | ✅ 完成 |
| components | 25 | 25 | ✅ 完成 |
| store | 5 | 5 | ✅ 完成 |
| api | 25 | 25 | ✅ 完成 |
| router | 5 | 5 | ✅ 完成 |
| utils | 10 | 10 | ✅ 完成 |
| guides | 6 | 6 | ✅ 完成 |

---

## 推荐输出顺序

当前已经完成的文档基础:

- 架构文档已完成，可作为全局背景
- 路由文档已完成，可快速确认页面入口与跳转关系
- API 文档已完成，可直接支撑后续模块文档编写
- `alarm-center` 已完成，适合作为后续模块文档的写法参考
- `app-monitor` 已完成，可复用页面矩阵与“列表页 + 详情页”写法
- `infrastructure` 已完成，可作为基础设施/K8s 类模块的写法参考
- `config-manage` 已完成，可作为“菜单页 + 多个设置静态页”类型模块的写法参考
- `sys-manage` 已完成，可作为“平台管理 + 跨模块复用页”类型模块的写法参考
- `config-install` 已完成，可作为“安装说明 + 配置页 + 静态编排页”类型模块的写法参考
- `data-report` 已完成，可作为“小模块 + 静态模板页”类型模块的写法参考
- `npm` 已完成，可作为“筛选 + 拓扑下钻 + 抽屉详情”类型模块的写法参考
- `log` 已完成，可作为“单页查询 + 弹窗下钻 + URL 回写”类型模块的写法参考
- `metrics` 已完成，可作为“树形列表 + 配置抽屉 + 分析预览”类型模块的写法参考
- `cockpit` 已完成，可作为“单路由多页签 + 综合下钻”类型模块的写法参考
- `observe` 已完成，可作为“树形主页面 + 静态编辑页 + 事件弹窗”类型模块的写法参考
- `rum` 已完成，可作为“多主页面 + 大量静态详情页”类型模块的写法参考
- `config-status` 已完成，可作为“运维主页面 + 若干静态工具页”类型模块的写法参考
- `login` 已完成，可作为“入口页 + 路由守卫联动”类型模块的写法参考
- `authorization` 已完成，可作为“状态机式初始化/授权页”类型模块的写法参考
- `personal` 已完成，可作为“单页资料维护 + 多弹窗交互”类型模块的写法参考
- `help` 已完成，可作为“静态知识库 + 少量引导跳转”类型模块的写法参考
- `components` 已完成，可作为共享组件复用与新增文档的参考模板
- `store` 已完成，可作为“全局缓存/鉴权/时间状态”文档的写法参考
- `utils` 已完成，可作为“请求/鉴权/格式化/校验工具”文档的写法参考

基于当前代码结构与已完成文档，推荐后续按下面顺序补文档。

### 第一阶段：优先补核心页面模块

第一阶段核心模块已经完成，可进入分析类与报表类模块。

### 第二阶段：补分析与报表类模块

### 第三阶段：补用户与体验类模块

第三阶段模块已经完成，后续可转入通用能力文档。

### 第四阶段：补通用能力文档

第四阶段已经完成，后续如有新增共享组件或通用规范，可继续在 `components` / `guides` 下按现有结构扩展。

## 模块内推荐写作顺序

为了减少返工，后续每个模块建议统一按这个顺序产出:

1. 模块 `README`
2. 列表页 / 首页
3. 详情页
4. 分析页 / 配置页 / 子页面
5. 复用组件页或特殊入口页
6. 模块内 `api.md`

这样可以优先把“入口、导航、主流程”写清楚，再补边缘页面和页面接口映射。

---

## 备注

- 本文件是规划稿，不等同于当前仓库实际落地状态
- 模块目录名优先采用 kebab-case 命名
- 每个 `.md` 文件建议包含：功能说明、入口/依赖、关键流程、接口/参数、注意事项
- 未知信息统一标注 `待确认`
