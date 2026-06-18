# 告警

> 页面: `/cockpit?type=alarm`
> 文件: `src/views/cockpit/tab/alarm.vue`

## 页面职责

告警页签用于按实体类型汇总业务系统和服务的告警等级分布，并展示实体级方块列表。

## 页面结构

- 业务系统区块
- 服务区块
- `square-list`: 每个区块下的实体方块矩阵

每个区块都会展示：

- 实体总数
- 重要 / 次要 / 无数据 / 无告警 数量与环比
- 实体方块列表

## 主要接口

- `Api.getEntityAlarmList`

## 关键行为

- 页面会按 `BUSINESS` 和 `SERVICE` 两种类型分别请求数据
- 方块颜色由 `matterDataCount`、`minorDataCount`、`noDataCount` 计算得到
- 时间说明文案来自当前全局时间范围
- 服务图标会结合 `Service/basicServiceMap` 推断语言 / 类型图标

## 下钻关系

- 点击业务系统方块: `/appMonitor/businessSystem?type=...&bsid=...&bsn=...`
- 点击服务方块: `/appMonitor/serviceDetail?sid=...&sn=...`

## 当前实现说明

- 代码里保留了按组整体点击跳转的注释逻辑，但当前实际开放的是单个方块点击
- 前端应用和技术组件区块目前没有开放

## 关联页面

- 业务系统: `/appMonitor/businessSystem`
- 服务详情: `/appMonitor/serviceDetail`
