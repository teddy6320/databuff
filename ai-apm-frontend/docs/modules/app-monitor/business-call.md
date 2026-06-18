# 业务系统调用分析

> 页面: `/appMonitor/businessCall`
> 文件: `src/views/appMonitor/businessCall/index.vue`

## 页面职责

业务系统调用分析页用于查看两端业务系统或服务之间的调用量、耗时、错误率，以及趋势图和明细列表。

## 页面结构

- `search-group`: 条件筛选
- 头部双栏摘要: 发起端 / 接收端指标
- `chart-group`: 趋势图
- `table-list`: 明细列表

## 主要接口

- `BsApi.getBusinessCallInfo`
- 子组件继续调用 `BsApi.getBusinessCallGraphStats`
- 子组件继续调用 `BsApi.getBusinessCallEndpoints`

详细接口见:

- [Business System API](../../api/bs.md)

## 关键参数

- `srcBid` / `dstBid`: 发起端 / 接收端业务系统 ID
- `srcSid` / `dstSid`: 发起端 / 接收端服务 ID

## 特点

- 页面会根据传入的是业务系统还是服务，动态拼装头部名称和图标
- 适合从业务系统页或拓扑图中下钻进入
