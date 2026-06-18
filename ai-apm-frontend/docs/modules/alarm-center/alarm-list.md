# 告警列表

## 页面信息

| 属性 | 值 |
|------|-----|
| 文件 | `src/views/alarmCenter/alarm/index.vue` |
| 路由 path | `/alarmCenter/alarm` |
| 列表 API | `AlarmApi.getAlarmListNew` |
| 趋势 API | `AlarmApi.getAlarmTrend` |

## 页面结构

### 顶部区域

- `search-group`: 关键字与基础筛选
- 设置按钮: 跳转 `/config/rule`
- 图表开关按钮: 控制顶部趋势图显示

设置按钮与图表按钮都依赖 `/config/rule` 菜单权限；无权限时按钮不可用。

### 中间区域

- `choose-collapse`: 快捷筛选折叠面板
- `basic-chart`: 告警趋势柱状图
- `db-table`: 告警列表

### 底部弹窗

- `deal-dialog`: 告警处理弹窗
- `ChatAI`: DeepSeek 根因分析对话入口

## 表格列

当前页面实际配置的主要列如下:

| 字段 | 说明 |
|------|------|
| `id` | 告警 ID |
| `description` | 告警描述，前置展示级别标识 |
| `domainName` | 所属管理域，仅分组开关开启时显示 |
| `startTriggerTime` | 首次触发时间 |
| `timestamp` | 最新触发时间 |
| `endTriggerTime` | 结束时间 |
| `duration` | 持续时间 |
| `eventCnt` | 事件数量 |
| `status` | 处理状态 |
| `appList` / `busNameList` / `serviceList` / `serviceInstanceList` / `processList` / `hostList` / `deviceNameList` | 触发对象维度 |
| `type` | 告警类型 |
| `rootCause` | 根本原因，点击可进入告警详情的根因 Tab |
| `influenceServiceCount` | 影响面，点击新窗口打开问题详情 |

## 数据与格式化

- 列表接口返回后会在 API 层对 `description` 做截断处理；超过 `1500` 字符时保留 `_description` 并展示省略文本
- 页面内会把 `trigger`、`tags`、直接字段中的对象信息整理为多个触发对象列
- `domainName` 通过 `User/getGroupMapping` 转换

## 趋势图

- 图表展示两组堆叠柱状数据: `活跃` 与 `不活跃`
- `活跃 = 待处理 + 处理中`
- `不活跃 = 已关闭 + 已解决（自动）`
- 点击柱子后会生成局部 `chartTimeParams`，用该时间片刷新下方表格

## 跳转行为

- 行点击: `/alarmCenter/alarmDetail?aid=...`
- 根本原因列点击: `/alarmCenter/alarmDetail?aid=...&type=tabCauseTree`
- 影响面列点击: 新窗口打开 `/alarmCenter/problemDetail?id=...`

## 时间与刷新

- 页面依赖全局时间 `globalTimeV2`
- 监听 `GlobalRefresh` 与 `AlarmInfoStatusChange`
- 当搜索或快捷筛选变化时，会同时刷新趋势图和表格

## 依赖组件

- `search-group`
- `choose-collapse`
- `db-table`
- `basic-chart`
- `deal-dialog`
- `chat-ai`
