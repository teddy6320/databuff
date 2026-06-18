# 诊断分析

> 页面: `/appMonitor/diagnostic`
> 文件: `src/views/appMonitor/diagnostic/index.vue`

## 页面职责

诊断分析页承载两类诊断能力:

- 内存 Dump
- 线程剖析

## 页面结构

- `db-tabnav`: `内存Dump` / `线程剖析`
- 动态组件切换 `dump` 与 `thread`

## 主要接口

- Dump 相关能力依赖 `service.ts` 中 Dump 接口
- 线程剖析依赖 `service.ts` 中线程分析接口

详细接口见:

- [Service API](../../api/service.md)

## 关键参数

- 路由 query `type` 控制初始页签
- 合法值为 `dump` 或 `thread`

## 关联页面

- 线程结果页: `/appMonitor/thread?id=...`
