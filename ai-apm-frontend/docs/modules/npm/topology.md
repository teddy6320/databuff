# 网络拓扑

> 页面: `/npm/topology`
> 文件: `src/views/npm/topology/index.vue`

## 页面职责

网络拓扑页用于把连接数据聚合成节点和边，帮助定位哪些客户端、服务端或服务之间存在高流量或高延迟的网络关系，并作为网络分析页的上游入口。

## 页面结构

- `search-group`: 选择展示维度、展示指标，并通过 `tag-input` 组织筛选条件
- `network-chart`: 拓扑图主体，负责节点/边数据拉取、缩放和点击下钻
- `scroll-select`: 拓扑节点快速定位输入
- 图例区: 展示节点大小和边宽度对应的最小/最大值

## 主要接口

- `NpmApi.getNpmTopoNodes`
- `NpmApi.getNpmTopoEdges`

详细接口见:

- [NPM API](../../api/npm.md)

## 关键参数

- `dimension`: 拓扑聚合维度，默认 `hostname`，可选 `cname`、`hostname`、`ip`、`podName`、`service`
- `metric`: 当前渲染指标，默认 `npm.volume_sent`
- `from`: 标签条件数组，路由中以 JSON 字符串回写
- `conn`: 多段 `from` 条件的连接符
- 页面内部会把全局时间转成 `fromTime`、`toTime` 后传给节点和边查询

## 当前指标范围

当前页面只开放了 `Volume` 与 `TCP` 两组指标。

- `DNS` 指标分组在代码里保留了注释块
- 拓扑页当前并未开放 DNS 视角

## 下钻关系

- 点击节点: 跳转 `/npm/analysis`，客户端和服务端都使用当前维度值，`conn=OR`
- 点击连线: 跳转 `/npm/analysis`，客户端取 `source`，服务端取 `target`，`conn=AND`
- 下钻时会尽量保留原页面时间参数，优先保留 `fromTime/toTime`，否则回传 `durationRange`

## 其他实现细节

- 节点大小来自当前指标的节点聚合值，连线宽度来自当前指标的边聚合值
- `network-chart` 会分别查询客户端节点、服务端节点和边，再在前端合并数据源
- 节点或边为空时会用 `N/A` 占位，避免 G6 渲染异常

## 关联页面

- 网络分析: `/npm/analysis`
- DNS 分析: `/npm/dns`
