# Charts

## 目录说明

[src/components/charts](/src/components/charts) 是项目里的通用图表组件目录，主要基于 ECharts 做一层业务封装。

当前目录下的文件包括：

- [basic-chart.vue](/src/components/charts/basic-chart.vue)
- [basic-chart-top-status.vue](/src/components/charts/basic-chart-top-status.vue)
- [horizontal-bar.vue](/src/components/charts/horizontal-bar.vue)
- [pie-chart.vue](/src/components/charts/pie-chart.vue)
- [pie-chart-new.vue](/src/components/charts/pie-chart-new.vue)
- [piecewise-chart.vue](/src/components/charts/piecewise-chart.vue)
- [radar-chart.vue](/src/components/charts/radar-chart.vue)

## 当前主力组件

从页面使用频率看，真正的核心入口是 [basic-chart.vue](/src/components/charts/basic-chart.vue)，它也被注册成了全局组件 `BasicChart`。

它主要负责：

- 折线图 / 柱状图统一渲染
- 顶部状态条联动
- Tooltip、Legend、Brush、联动 group 配置
- 时间轴格式化和大数据量优化
- 明暗主题联动

## 使用场景

`BasicChart` 在 APM、基础设施、告警、RUM、NPM 等模块里都有大量使用，适合承载：

- 趋势图
- Top N 对比图
- 指标分组图
- 带时间范围的联动图表

## 使用建议

- 通用时间序列图优先复用 `BasicChart`
- 如果只是某个页面的强业务图表，不一定要继续塞回 `src/components/charts`
- 新增图表前先判断能不能通过 `source + props + slot(ts)` 在 `BasicChart` 上扩展
