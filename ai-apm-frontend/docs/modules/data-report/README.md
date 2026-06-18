# 数据报表模块

> 模块入口: `src/views/dataReport/index.vue`

## 模块概述

`data-report` 提供两类能力：

- 仪表盘嵌入展示
- 报告与报告模板管理

模块本身页面数量不多，但包含两个静态页：

- 报告模板列表
- 模板新增/编辑页

## 页面矩阵

| 类型 | 路由 | 文件 | 说明 |
|------|------|------|------|
| 页面 | `/dashboard` | `dashboard/index.vue` | 仪表盘嵌入页 |
| 页面 | `/report` | `report/index.vue` | 报告列表 |
| 静态页 | `/report/template` | `report/template/index.vue` | 模板列表 |
| 静态页 | `/report/setting` | `report/setting/index.vue` | 新建/编辑模板 |

## 典型导航关系

- 报告列表 -> 模板列表: `/report/template`
- 模板列表 -> 新建模板: `/report/setting`
- 模板列表 -> 编辑模板: `/report/setting?id=...`

## 主要依赖 API

- `plugin.ts`: 仪表盘 UID 查询
- `report.ts`: 报告列表、历史数据、模板增删改查、图片上传
- `metric.ts`: 模板设置页里的指标相关配置

## 已补文档

- [仪表盘](dashboard.md)
- [报告](report.md)
