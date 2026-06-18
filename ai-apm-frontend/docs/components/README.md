# 共享组件文档

## 目录概览

`src/components/` 存放全局复用组件、基础图表组件和少量业务增强组件。现在已经按目录规划把当前主要共享组件全部补齐，后续如果有新增共享组件，可以继续按同一分类补文档。

## 已完成组件

| 分类 | 文档 | 说明 |
|------|------|------|
| business | [db-table](business/db-table.md) | 基于 Element Table 的增强表格 |
| business | [db-tabnav](business/db-tabnav.md) | 轻量页签导航组件 |
| business | [db-menu](business/db-menu.md) | 平台左侧菜单封装 |
| business | [db-radio](business/db-radio.md) | 滑块式分段单选 |
| business | [dialog-template](business/dialog-template.md) | 弹窗模板骨架 |
| charts | [index](charts/index.md) | 图表组件目录与 `BasicChart` 总览 |
| form | [ip-input](form/ip-input.md) | IPv4 分段输入框 |
| form | [metric-select](form/metric-select.md) | 带指标详情 tooltip 的选择器 |
| form | [metric-cascader](form/metric-cascader.md) | 指标分类级联选择器 |
| form | [metric-unit-cascader](form/metric-unit-cascader.md) | 指标单位级联选择器 |
| display | [code-view](display/code-view.md) | 代码块/行内代码展示 |
| display | [marked-view](display/marked-view.md) | Markdown 渲染与代码复制 |
| display | [statistic](display/statistic.md) | 轻量统计值展示 |
| filter | [query-filter](filter/query-filter.md) | URL 联动型筛选组件 |
| display | [metric-info-tooltip](display/metric-info-tooltip.md) | 指标详情卡片内容 |
| form | [scroll-select](form/scroll-select.md) | 带滚动加载的选择器 |
| display | [text-expand](display/text-expand.md) | 超长文本折叠/展开 |
| display | [collapse-tags](display/collapse-tags.md) | 标签折叠展示 |
| filter | [matching-criteria](filter/matching-criteria.md) | 条件编排组件 |
| layout | [cont-wrapper](layout/cont-wrapper.md) | 标准页面内容容器 |
| layout | [router-view-temp](layout/router-view-temp.md) | 纯占位式路由容器 |
| special | [chat-ai](special/chat-ai.md) | AI 根因分析抽屉 |
| special | [flame-chart](special/flame-chart.md) | 火焰图底层渲染库说明 |
| special | [db-icon-button](special/db-icon-button.md) | 轻量图标按钮 |

## 当前注册到全局的组件

根据 [src/components/index.ts](/src/components/index.ts)，当前全局安装的组件包括：

- `DbTabnav`
- `ScrollSelect`
- `DbTable`
- `DbRadio`
- `DbIconButton`
- `BasicChart`
- `DbQuery`

## 当前说明

- `metric-cascader` 文档对应实际源码 [src/components/metric-type-cascader.vue](/src/components/metric-type-cascader.vue)
- `flame-chart` 文档对应实际目录 [src/components/flame-chart-js](/src/components/flame-chart-js)
- 图表目录目前以 `BasicChart` 为核心入口，其他图表组件仍以源码复用为主
