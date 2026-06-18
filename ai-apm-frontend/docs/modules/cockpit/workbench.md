# 工作台

> 页面: `/cockpit?type=overview`
> 文件: `src/views/cockpit/tab/overview.vue`

## 页面职责

工作台页签用于展示实体健康趋势和告警总览，是驾驶舱里最偏“总览首页”的部分。

## 页面结构

- `CardTrend`: 实体数据卡片，展示总量、异常量和趋势
- 告警统计表: 按处理状态和告警等级汇总数量
- 右侧告警列表: 展示当前选中状态/等级下的最近 4 条告警

## 主要接口

- `Api.getEntityData`
- `Api.getAlarmData`
- `AlarmApi.getAlarmListNew`

详细接口见:

- [Alarm API](../../api/alarm.md)

## 关键行为

- 页面会根据 `User/hasRumMenu` 决定是否展示“前端应用”卡片
- 告警统计表固定展示 `待处理`、`处理中`、`已关闭`、`已解决(自动)` 四种状态
- 点击统计数字会刷新右侧最近告警列表
- 点击“查看更多”会跳到 `/alarmCenter/alarm`，并带上当前 `status` 和 `level`

## 关联页面

- 告警详情: `/alarmCenter/alarmDetail?aid=...`
- 告警列表: `/alarmCenter/alarm?status=...&level=...`
