# 线程列表

> 页面: `/appMonitor/thread`
> 文件: `src/views/appMonitor/thread/index.vue`

## 页面职责

线程列表页是“线程剖析”结果的详情页，用于查看死锁线程、线程组和线程堆栈。

## 页面结构

- `search-group`: 线程筛选
- `chart-group`: 状态分布与 CPU 趋势
- `db-radio`: `死锁线程` / `线程组`
- `db-table`: 线程列表
- `el-dialog`: 堆栈详情弹窗

## 主要接口

- `ServiceApi.getThreadAnalysisDetail`
- `ServiceApi.getThreadAnalysisStack`

详细接口见:

- [Service API](../../api/service.md)

## 关键参数

- `id`: 线程剖析任务 ID
- 页面会把 breadcrumb 指回 `/appMonitor/diagnostic?type=thread`

## 特点

- 若没有死锁线程，页面会自动切换到“线程组”
- 若任务详情拉取失败，会回退到诊断分析页
