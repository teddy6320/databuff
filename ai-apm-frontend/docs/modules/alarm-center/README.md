# 智能告警模块

> 模块入口: `src/views/alarmCenter/index.vue`

## 页面与组件

| 类型 | 路由 / 用途 | 文件 | 说明 |
|------|-------------|------|------|
| 页面 | `/alarmCenter/alarm` | `alarm/index.vue` | 告警列表 |
| 页面 | `/alarmCenter/alarmDetail` | `alarmDetail/index.vue` | 告警详情，入口参数为 `aid` |
| 页面 | `/alarmCenter/notice` | `notice/index.vue` | 通知记录 |
| 页面 | `/alarmCenter/rootCause` | `rootCause/index.vue` | 问题列表 |
| 页面 | `/alarmCenter/problemDetail` | `problemDetail/index.vue` | 问题详情，常见入口参数为 `id` |
| 页面 | `/alarmCenter/problemAnalysis` | `problemAnalysis/index.vue` | 问题分析 |
| 页面 | `/alarmCenter/rootCauseAnalysis` | `rootCauseAnalysis/index.vue` | 手动根因分析 |
| 复用组件 | 告警详情右侧事件明细 | `eventDetail/index.vue` | 在告警详情内嵌使用 |
| 独立页面 | `/sysManage/eventDetail` | `eventDetail/index.vue` | 系统事件详情页复用同一组件 |

> 部署后通常会以 `/databuff` 作为应用 base，因此完整访问路径一般形如 `/databuff/alarmCenter/alarm`。

## 功能概览

### 告警管理

- 告警列表筛选、趋势图联动、处理操作
- 告警详情查看，包括事件列表、根因分析、告警响应、处理日志
- 通知记录查询与失败重发
- DeepSeek 根因分析重试入口

### 问题与根因

- 问题列表与问题趋势查看
- 问题详情查看，包括影响面分析与关联告警
- 问题分析大盘，包括类型分布、节点分布、收敛率、趋势、MTTR/MTTA
- 手动根因分析，并支持从分析结果跳转到问题详情

### 事件查看

- 事件基本信息
- 异常指标
- 链路追踪
- 日志明细

## 状态与级别

### 告警状态

| 值 | 说明 |
|---|------|
| `0` | 待处理 |
| `1` | 已关闭 |
| `2` | 处理中 |
| `3` | 已解决（自动） |

### 告警级别

| 值 | 说明 |
|---|------|
| `1` | 提示 |
| `2` | 警告 |
| `3` | 严重 |

## 主要依赖组件

- `db-table`: 列表展示
- `db-tabnav`: 页内 Tab 导航
- `basic-chart`: 柱状图、趋势图
- `pie-chart-new`: 饼图
- `scroll-select`: 多选/根因切换
- `text-expand`: 标题展开收起
- `collapse-tags`: 标签折叠展示
- `chat-ai`: AI 对话与分析能力
- `cause-tree`: 根因树 / 影响面分析
- `analysis-record`: 手动分析记录
- `convg-chart`: 问题收敛图

## 典型导航关系

- 告警列表 -> 告警详情：`/alarmCenter/alarmDetail?aid=...`
- 告警详情 -> 问题详情：`/alarmCenter/problemDetail?id=...`
- 问题列表 -> 问题详情：`/alarmCenter/problemDetail?id=...`
- 问题分析 -> 问题列表：携带 `rootCauseTypes`、`rootCauseNodes` 或时间范围参数
- 手动根因分析 -> 问题详情：新窗口打开，并传递 `sn/fromTime/toTime/...`

## API 集成

- 告警、通知、事件、AI 相关接口见 [Alarm API](../../api/alarm.md)
- 问题、影响面、根因分析相关接口见 [Root Cause API](../../api/root-cause.md)
