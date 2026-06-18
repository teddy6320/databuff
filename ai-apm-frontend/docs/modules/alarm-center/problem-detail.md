# 问题详情

## 页面信息

| 属性 | 值 |
|------|-----|
| 文件 | `src/views/alarmCenter/problemDetail/index.vue` |
| 路由 path | `/alarmCenter/problemDetail` |
| 主 API | `RootCauseApi.getProblemDetail` / `RootCauseApi.getInfluenceAnalysis` |

## 入口模式

### 模式 1：通过问题 ID 进入

- URL 参数: `id`
- 请求: `RootCauseApi.getProblemDetail({ id })`

### 模式 2：从手动根因分析进入

常见 URL 参数:

- `sn`
- `fromTime`
- `toTime`
- `abnormalFirstTime`
- `isRoot`
- `abnormalDetail`

此时页面调用:

```ts
RootCauseApi.getInfluenceAnalysis(...)
```

拿到结果后会把当前路由替换为仅保留 `id` 的形式，后续刷新与分享都以问题 ID 为准。

## 页面结构

### 顶部信息

- 问题描述
- 问题 ID
- 影响服务数
- 开始时间
- 确认时间
- 结束时间
- 修复时间（MTTR）
- 响应时间（MTTA）

### Tabs

| Tab | 值 | 说明 |
|-----|----|------|
| 告警列表 | `alarm` | 关联告警趋势与列表 |
| 影响面分析 | `causeTree` | 根因树、AI 建议、反馈入口 |

默认激活 `causeTree`；若 URL 中存在 `type` 参数，则会切到对应 Tab。

## 子模块时间范围

页面会根据问题开始/结束时间，为子组件生成一个扩展时间窗:

- 开始时间: `problemStartTime - 10 分钟`
- 结束时间: `problemEndTime + 10 分钟`
- 时间粒度: `getTimeRange(..., 30 * 60 * 1000)` 计算出的 `interval`

这些参数会透传给:

- 告警列表子页
- 告警趋势图

## 反馈功能

影响面分析 Tab 支持准确性反馈:

- 展示字段: `feedbackStatus`、`feedbackMessage`
- 弹窗组件: `feedback-dialog`
- 保存接口: `RootCauseApi.influenceFeedback`

## AI 重试

页面支持:

```ts
AlarmApi.retrySuggest({ problemId: this.problemId })
```

成功后重新拉取问题详情。

## 依赖组件

- `db-tabnav`
- `text-expand`
- `cause-tree`
- `alarm`
- `feedback-dialog`
