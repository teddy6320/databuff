# 指标体系模块

> 模块入口: `src/views/metrics`

## 模块概述

`metrics` 模块当前包含两类页面：

- 指标列表：按分类浏览、搜索、编辑和删除指标核心配置
- 指标分析：按分析单元组合指标、表达式、过滤和分组，并预览趋势图

模块使用 `/metrics` 作为一级菜单，实际可访问页面是 `/metrics/list` 和 `/metrics/analysis`。

## 页面矩阵

| 类型 | 路由 | 文件 | 说明 |
|------|------|------|------|
| 页面 | `/metrics/list` | `list/index.vue` | 指标分类树、指标列表与详情 |
| 页面 | `/metrics/analysis` | `analysis/index.vue` | 指标分析与图表预览 |

## 典型导航关系

- 指标列表 -> 指标分析: `/metrics/analysis?metric=...`
- 指标列表 -> 新建指标: 当前页抽屉
- 指标列表 -> 编辑 / 查看指标: 当前页抽屉
- 指标列表 -> 编辑分类: 当前页弹窗

## 主要依赖 API

- `metric.ts`: 指标分类、指标详情、标签、核心配置
- `monitor.ts`: 指标分析页图表预览

详细接口见:

- [Metric API](../../api/metric.md)
- [Monitor API](../../api/monitor.md)

## 已补文档

- [指标列表](list.md)
- [指标分析](analysis.md)
