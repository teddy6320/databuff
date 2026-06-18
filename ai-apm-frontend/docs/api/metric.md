# Metric API

> 文件: `src/api/metric.ts`

## 概述

`metric.ts` 负责指标体系相关的数据查询与配置能力，既覆盖“指标选择器”所需的分类/详情查询，也覆盖“指标分析页”的图表查询，以及指标核心配置管理。

## 结构特点

除了默认导出的 API 对象外，这个文件还包含两层前端格式化逻辑:

- `formatMetricInfo`
- `formatMetricInfos`

它们会把后端返回的指标详情标准化为前端更易消费的结构，比如:

- `_types`
- `_isState`
- `_options`
- `_tagKeyOptions`
- `metric`
- `metricCn`
- `describeCn`

## 查询类接口

### 分类与指标列表

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getMetricTypes` | `GET` | `/metrics/getMetricTypes` | 获取指标分类 |
| `getMetricTypesByQuery` | `POST` | `/metrics/searchMetricTypes` | 模糊搜索分类与指标 |
| `getMetricList` | `GET` | `/metrics/findMetric` | 按分类查指标名 |
| `getAllMetricListByQuery` | `POST` | `/metrics/searchAllMetrics` | 查询分类或主机/应用下的详细指标 |

### 指标详情

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getMetricInfos` | `GET` | `/metrics/query/in?metrics=...` | 批量指标详情 |
| `getMetricDetail` | `GET` | `/metrics/detail` | 单指标详情 |
| `getMetricTags` | `POST` | `/metrics/listTagValues` | 多指标聚合标签 |
| `getMetricLastTags` | `POST` | `/metrics/lastLastTagValues` | 最新标签 |
| `getAllTagKey` | `GET` | `/metrics/query/tagKey/all` | 全量 tagKey |
| `getMetaUnits` | `GET` | `/meta/conf/unit` | 单位列表 |

### 图表查询

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getMetricChart` | `POST` | `/metrics/exploreMetricByGroupGraph` | 指标趋势图 |
| `getMysqlMetricsTrend` | `POST` | `/metrics/searchAttentionMetrics` | MySQL 关注指标趋势 |

## 配置类接口

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `toggleMetricEnable` | `PUT` | `/metrics/core/enable|disable/{metric}` | 启停指标 |
| `deleteMetric` | `DELETE` | `/metrics/core/{metric}` | 删除单指标 |
| `updateMetricTypes` | `PUT` | `/metrics/core/directory/update` | 更新指标分类 |
| `deleteMetricsByTypes` | `DELETE` | `/metrics/core/directory` | 删除分类下指标 |
| `getMetricCoreDetail` | `GET` | `/metrics/core/{id}` | 获取指标核心配置 |
| `saveMetricCore` | `POST` / `PUT` | `/metrics/core/` | 创建或编辑指标核心配置 |
| `getMetricTypeValues` | `GET` | `/metrics/core/typeValues` | 获取分类下 measurement / app / database 等值 |

## 特殊处理

### `getMetricInfos`

会把原始响应中的指标 mapping 统一转成格式化后的前端结构。

### `getMetricChart`

这个方法做了两层兼容:

1. 若 `start/end` 是 10 位秒级时间戳，会先转成毫秒再请求
2. 若 `query.A` 存在，会把其内容提升到请求体顶层

响应返回后还会把 `values` 中的时间戳统一收敛为秒级。

