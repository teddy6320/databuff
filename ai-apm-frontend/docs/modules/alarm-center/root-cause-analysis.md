# 根因分析

## 页面信息

| 属性 | 值 |
|------|-----|
| 文件 | `src/views/alarmCenter/rootCauseAnalysis/index.vue` |
| 路由 path | `/alarmCenter/rootCauseAnalysis` |
| 主接口 | `RootCauseApi.getRootCauseAnalysis` |
| 服务来源 | `Service/basicServiceMap` |

## 页面结构

### 顶部区域

- 分析时间段文案
- `analysis-record`: 历史分析记录
- `scroll-select`: 服务多选
- “重新分析”按钮

### 中间与底部

- `root-cause-trend`: 根因趋势图/切换入口
- 根因节点选择器
- 影响面分析按钮
- `cause-tree`: 当前根因的拓扑树

## URL 参数

- `sns`: 预选中的服务名列表，逗号分隔并经过 `encodeURIComponent`
- `disableExpand=true`: 透传给分析接口的控制参数

## 数据流

### 发起分析

页面请求参数实际形态为:

```ts
{
  service: serviceNames.join(','),
  fromTime: +new Date(timeParams.fromTime),
  toTime: +new Date(timeParams.toTime),
  disableExpand?: true,
}
```

### 结果处理

接口返回后，页面会:

- 读取 `rootAnalyse`
- 为每个根因结果生成一个本地 `uuid`
- 构建 `topoRootList`
- 以根因树节点为维度切换下方 `cause-tree`
- 保存 `logs` 到 `analysis-record`

若后端回写的 `startTime/endTime` 与当前全局时间不一致，页面会把路由 query 更新为新的 `fromTime/toTime`。

## 影响面分析跳转

点击“影响面分析”后会新开窗口进入问题详情页:

```ts
/alarmCenter/problemDetail?sn=...&abnormalFirstTime=...&isRoot=1&fromTime=...&toTime=...&abnormalDetail=...&__nw=t
```

这一步不会直接把当前页替换成问题详情，而是通过新窗口联动。

## 刷新机制

- 首次进入自动分析
- 当服务选择或全局时间变化后，按钮进入可重新分析状态

## 依赖组件

- `scroll-select`
- `analysis-record`
- `root-cause-trend`
- `cause-tree`
