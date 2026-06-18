# 业务系统

> 页面: `/cockpit?type=business`
> 文件: `src/views/cockpit/tab/business.vue`

## 页面职责

业务系统页签用于展示业务系统健康统计和业务系统卡片列表，并提供关键指标配置入口。

## 页面结构

- 顶部健康统计区: 总数、严重异常、轻微异常、正常
- `CardPanel`: 业务系统卡片，展示得分、告警数和关键指标
- `MetricList`: 关键指标抽屉

## 主要接口

- `Api.getBusinessOverview`
- `Api.getBusinessMetricSelect`
- `Api.updateBusinessMetricSelect`

## 关键行为

- 页面会根据全局时间和手动刷新重新拉取业务系统概览
- 卡片数据来自 `healthEntityScoreList`
- “评分规则”按钮跳到 `/sysManage/health?active=business`
- “关键指标”按钮打开抽屉，可选择最多 3 个指标

## 下钻关系

- 点击顶部统计或卡片主体: `/appMonitor/businessSystem`
- 点击单个卡片时会额外带 `bsid`、`bsn`
- 点击卡片告警入口: `/alarmCenter/alarm?busName=...`

## 当前实现说明

- 关键指标抽屉内部复用了 `MetricList` 组件
- 指标筛选限定在业务系统相关指标分类

## 关联页面

- 业务系统: `/appMonitor/businessSystem`
- 告警列表: `/alarmCenter/alarm`
- 健康度配置: `/sysManage/health?active=business`
