# DNS 分析

> 页面: `/npm/dns`
> 文件: `src/views/npm/dns/index.vue`

## 页面职责

DNS 分析页用于查看 DNS 请求量、响应时间和错误率，并通过抽屉继续查看单条 DNS 通信的趋势与域名请求列表。

## 页面结构

- `search-group`: 复用网络分析的查询组件，但以 `isDns=true` 方式运行
- `choose-collapse`: 复用快捷筛选组件，切换为 DNS 标签接口
- `chart-group`: DNS 趋势图，展示请求量、错误率和响应时间
- `table-list`: DNS 列表，支持列设置、排序和滚动加载
- `detail/index.vue`: 行点击后打开的抽屉详情

## 主要接口

- `NpmApi.getDnsPerformanceTags`
- `NpmApi.getDnsPerformanceList`
- `NpmApi.getDnsPerformanceVolumeList`
- `NpmApi.getPerformanceMetricsData`

详细接口见:

- [NPM API](../../api/npm.md)

## 关键参数

- `client`: 客户端聚合维度，默认 `srcHostname`
- `server`: DNS 页固定使用 `ip`
- `from`: 标签条件数组，路由中以 JSON 字符串保存
- `conn`: 多段 `from` 条件的连接符
- `filterType`: 左侧快捷筛选当前视角
- `multisearch`: 左侧多选筛选回写到 URL 的编码字符串

## 详情抽屉

列表行点击后会打开右侧抽屉，包含两部分：

- 顶部摘要：客户端、DNS IP、请求量、响应时间、错误率和标签
- 详情内容：趋势图 + 域名请求量列表

详情页的实现有两个关键点：

- 趋势图仍通过 `getPerformanceMetricsData` 查询，只是指标切换为 DNS 指标
- 下方列表固定把 `server` 设置成 `domainName`，并根据当前行 `tags` 重新拼装 `from` 条件

## 其他实现细节

- 主列表列配置保存在本地 `localStorage`，键名是 `TCWs_NPM_Dns`
- 抽屉内部详情表格使用单独的本地键 `TCWs_NPM_Dns_Detail`
- `search-group` 在 DNS 模式下会禁用服务端视角切换，只保留 `ip`

## 关联页面

- 网络分析: `/npm/analysis`
- 网络拓扑: `/npm/topology`
