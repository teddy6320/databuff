# 网络分析

> 页面: `/npm/analysis`
> 文件: `src/views/npm/analysis/index.vue`

## 页面职责

网络分析页用于从客户端到服务端的连接视角查看流量、TCP 和 RTT 类指标，并支持通过图表与列表联动下钻到单条连接的详情抽屉。

## 页面结构

- `search-group`: 选择客户端视角、服务端视角，并通过 `tag-input` 组织 `from` 条件
- `choose-collapse`: 左侧快捷筛选，支持客户端/服务端两组标签切换
- `chart-group`: 趋势图区域，指标列表先通过接口动态加载
- `table-list`: 网络分析列表，支持列显示配置和排序
- `detail/index.vue`: 行点击后打开的抽屉详情

## 主要接口

- `NpmApi.getPerformanceMetrics`
- `NpmApi.getPerformanceTags`
- `NpmApi.getPerformanceMetricsData`
- `NpmApi.getPerformanceList`
- `NpmApi.getPerformanceVolumeList`

详细接口见:

- [NPM API](../../api/npm.md)

## 关键参数

- `client`: 客户端聚合维度，默认 `srcHostname`
- `server`: 服务端聚合维度，默认 `hostname`
- `from`: 标签条件数组，路由中以 JSON 字符串形式保存，元素格式是 `left:right`
- `conn`: 当 `from` 有多段条件时记录连接符，常见是 `AND` 或 `OR`
- `filterType`: 左侧快捷筛选当前所处视角，值为 `client` 或 `server`
- `multisearch`: 左侧多选筛选回写到 URL 的编码字符串

## 详情抽屉

列表行点击后会打开右侧抽屉，标题显示 `客户端 ⇆ 服务端` 和当前行截止时间。

- 抽屉初始化时间范围来自当前行的 `_fromTime` 与 `_toTime`
- 当前 UI 实际只展示 `performance` 视图
- `traffic.vue` 组件仍然保留在代码中，但页签切换入口已注释，暂未对用户开放

## 其他实现细节

- 图表指标列表不是写死在页面里，而是先调用 `getPerformanceMetrics` 拉取
- 列显示配置保存在本地 `localStorage`，键名是 `TCWs_NPM_Analysis`
- 页面监听全局 `GlobalRefresh` 事件，并跟随全局时间范围重新拉数

## 关联页面

- 网络拓扑: `/npm/topology`
- DNS 分析: `/npm/dns`
- 网络拓扑下钻到本页时会保留时间参数，并自动拼装 `client`、`server`、`from`、`conn`
