# HTTP 连接池监控

> 页面: `/appMonitor/httpConnPool`
> 文件: `src/views/appMonitor/httpConnPool/index.vue`

## 页面职责

HTTP 连接池页用于展示某服务下连接池相关内部指标图。

## 页面结构

- `search-group`: 服务、实例、池名称筛选
- 指标列表: 每个指标渲染一张 `basic-chart`

## 主要接口

- `MetricApi.getAllMetricListByQuery`
- `MetricApi.getMetricChart`

详细接口见:

- [Metric API](../../api/metric.md)

## 指标范围

- 固定分类: `应用性能 / 内部指标 / Http连接池`
- 池名称标签: `httpConnectionPoolName`

## 说明

- 页面会先拉全量指标定义，再逐个指标请求趋势图
- 通常由服务详情或接口调用分析中的池化视图下钻进入
