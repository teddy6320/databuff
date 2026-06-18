# 指标列表

> 页面: `/metrics/list`
> 文件: `src/views/metrics/list/index.vue`

## 页面职责

指标列表页用于按分类查看全部指标、浏览单个指标详情，并对自定义指标与分类进行创建、编辑、删除和预览。

## 页面结构

- `search-group`: 顶部搜索区，提供指标分类和指标名称两个输入项
- 左侧 `el-tree`: 指标分类树，展示分类层级和每层指标数量
- 中间指标列表: 展示当前分类下的指标名，并标记推荐指标
- `metric-detail`: 右侧详情区，展示指标说明、单位、标签和操作入口
- `metric-config`: 新建 / 编辑 / 查看指标抽屉
- `type-dialog`: 分类重命名弹窗

## 主要接口

- `MetricApi.getMetricTypesByQuery`
- `MetricApi.getMetricTypes`
- `MetricApi.getMetricDetail`
- `MetricApi.deleteMetric`
- `MetricApi.deleteMetricsByTypes`
- `MetricApi.updateMetricTypes`
- `MetricApi.getMetricCoreDetail`
- `MetricApi.saveMetricCore`

详细接口见:

- [Metric API](../../api/metric.md)

## 关键参数

- `typeKey`: 按分类关键字搜索
- `metricKey`: 按指标名关键字搜索
- `cTypes`: 当前选中的分类节点，使用 `encodeURIComponent(JSON.stringify(...))` 回写到 URL
- `cMetric`: 当前选中的指标名，回写到 URL

页面重新进入时会优先从 `cTypes`、`cMetric` 恢复当前选中项。

## 分类树与列表行为

- 分类树数据来自 `getMetricTypesByQuery`
- 左侧数量会同时统计 `all`、一级、二级、三级分类下的指标量
- 内建分类会被记录到 `builtinTypesMap`，不可编辑和删除
- 推荐指标来自额外一次 `builtIn + recommend` 查询，并在指标名前显示点赞图标

当前选中逻辑有两个特点：

- 如果 URL 中的分类不存在于当前结果集，页面会自动清空分类选中
- 如果当前分类下找不到已选指标，会自动切到该分类下第一个指标

## 指标详情区

右侧 `metric-detail` 会根据当前指标名调用详情接口，并提供 4 个动作：

- 删除
- 编辑
- 查看
- 指标分析

其中：

- 内建指标视为不可编辑，但如果存在核心配置 ID，仍可走“查看”
- 点击“指标分析”会跳到 `/metrics/analysis?metric=...`

## 配置抽屉

`metric-config` 同时承担三种模式：

- 新建指标：`id` 为空
- 编辑指标：`id` 有值且 `readonly=false`
- 查看指标：`id` 有值且 `readonly=true`

抽屉里主要维护：

- 三级分类
- `measurement`
- `fields`
- `tagKey`
- `tagValue`

保存成功后，列表页会：

- 关闭抽屉
- 记录保存后的分类到 `metricCoreTypeKey`
- 重新拉取分类树和指标列表
- 清理 store 中相关的指标分类和指标详情缓存

## 关联页面

- 指标分析: `/metrics/analysis?metric=...`
