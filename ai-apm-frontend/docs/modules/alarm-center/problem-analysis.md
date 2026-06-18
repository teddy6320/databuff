# 问题分析

## 页面信息

| 属性 | 值 |
|------|-----|
| 文件 | `src/views/alarmCenter/problemAnalysis/index.vue` |
| 路由 path | `/alarmCenter/problemAnalysis` |
| 数据来源 | `RootCauseApi.*` 一组统计接口 |

## 页面说明

该页面本身没有独立的搜索表单组件，主要依赖全局时间 `globalTimeV2` 变化来刷新 5 组图表。

## 图表组成

### 1. 问题类型

- 组件: `pie-chart-new`
- 接口: `RootCauseApi.getRootCauseTypes`
- 参数: `fromTime`、`toTime`、`topN: 10`
- 点击后跳转问题列表，并携带 `rootCauseTypes`

### 2. 问题节点

- 组件: `pie-chart-new`
- 接口: `RootCauseApi.getRootCauseNodes`
- 参数: `fromTime`、`toTime`、`topN: 10`
- 点击后跳转问题列表，并携带 `rootCauseNodes`

### 3. 问题收敛

- 组件: `convg-chart`
- 接口: `RootCauseApi.getInfluenceConvergence`
- 展示链路: `事件 -> 告警 -> 问题 -> 问题收敛`

### 4. 问题统计

- 组件: `basic-chart`
- 接口: `RootCauseApi.getInfluenceTrend`
- 图形: 柱状图
- 点击时间柱后跳转问题列表，并携带该时间片 `fromTime/toTime`

### 5. MTTR / MTTA

- 组件: `basic-chart`
- 接口: `RootCauseApi.getInfluenceMtt`
- 图形: 双面积折线
- 页面顶部同时展示 `avg/min/max`

## 数据刷新

页面会在以下时机统一刷新全部图表:

- 首次进入页面
- `globalTimeV2` 变化
- 收到 `GlobalRefresh` 事件

## 依赖组件

- `pie-chart-new`
- `basic-chart`
- `convg-chart`
