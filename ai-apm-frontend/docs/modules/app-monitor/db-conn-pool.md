# 数据库连接池监控

> 页面: `/appMonitor/dbConnPool`
> 文件: `src/views/appMonitor/dbConnPool/index.vue`

## 页面职责

数据库连接池页用于展示数据库连接池相关内部指标图。

## 页面结构

- `search-group`
- 多张 `basic-chart`

## 主要接口

- `MetricApi.getAllMetricListByQuery`
- `MetricApi.getMetricChart`

详细接口见:

- [Metric API](../../api/metric.md)

## 指标范围

- 固定分类: `应用性能 / 内部指标 / 数据库连接池`
- 池名称标签: `connectionPoolName`

## 说明

- 页面行为与 HTTP 连接池页基本一致，只是指标分类和池名称标签不同
