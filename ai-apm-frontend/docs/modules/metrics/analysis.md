# 指标分析

> 页面: `/metrics/analysis`
> 文件: `src/views/metrics/analysis/index.vue`

## 页面职责

指标分析页用于按“分析单元”组织指标查询，支持多指标表达式、过滤条件、标签分组和图形预览，适合做临时探索式分析。

## 页面结构

- `metric-query`: 每个分析单元的查询配置区
- `preview-item`: 每个分析单元对应的图表预览区
- “添加分析单元”: 继续追加新的查询块

一个页面可包含多个分析单元，每个分析单元独立预览自己的图表。

## 主要接口

- `MetricApi.getMetricTypesByQuery`
- `MetricApi.getMetaUnits`
- `MonitorApi.getPreviewMetricGraph`

详细接口见:

- [Metric API](../../api/metric.md)
- [Monitor API](../../api/monitor.md)

## 关键参数

- `metric`: 可选入口参数，来自指标列表页下钻

当 URL 中带 `metric` 时，页面会自动生成一个默认分析单元：

- `A.metric = 当前指标`
- `A.aggs = avg`
- `expr = A`

## 分析单元结构

每个分析单元由 `metric-query` 维护，核心字段包括：

- `A/B/C...`: 指标槽位
- `types`: 指标分类
- `metric`: 指标名
- `aggs`: 聚合方式
- `from`: 过滤条件
- `by`: 分组标签
- `expr`: 表达式
- `unit`: 自定义展示单位

页面支持继续添加分析单元；单个分析单元内部也支持添加多个指标槽位，并通过表达式组合计算。

## 查询约束与校验

- 表达式必须只引用当前分析单元中存在的字母槽位
- 任一槽位未选择指标时，不会发起图表预览
- 图表最多展示 20 条图例，超出部分不展示

当前实现里，表达式允许使用：

- 大写字母槽位
- 数字
- `+ - * / ()`

## 图表预览

`preview-item` 负责把当前分析单元转成请求并调用 `MonitorApi.getPreviewMetricGraph`。

主要行为包括：

- 图形类型支持折线图、面积图、柱形图切换
- 刷新按钮会按当前全局时间重新查询
- 图例会按最后一个数据点的值从大到小排序
- 只保留前 20 条有值序列

图表状态分为：

- `none`: 未选择指标
- `exprError`: 表达式非法
- `empty`: 无数据
- `success`: 查询成功
- `error`: 请求失败

## 依赖数据

页面初始化时会预加载两类基础数据：

- 指标分类与分类下指标列表
- 单位选项 `meta units`

这些数据会同时写入 `Common` store，供查询组件和其他页面复用。

## 关联页面

- 指标列表: `/metrics/list`
