# echarts

## 文件定位

[src/utils/echarts.ts](/src/utils/echarts.ts) 是项目统一的 ECharts 注册入口，负责按需引入图表、组件、特性和渲染器，再导出配置完成的 `echarts` 实例。

## 当前注册内容

### 图表类型

- `BarChart`
- `LineChart`
- `PieChart`
- `TreemapChart`
- `CustomChart`
- `RadarChart`

### 组件

- `TitleComponent`
- `TooltipComponent`
- `GridComponent`
- `DatasetComponent`
- `TransformComponent`
- `ToolboxComponent`
- `LegendComponent`
- `DataZoomComponent`
- `MarkAreaComponent`
- `MarkLineComponent`
- `MarkPointComponent`
- `BrushComponent`

### 特性与渲染器

- `LabelLayout`
- `UniversalTransition`
- `LegacyGridContainLabel`
- `SVGRenderer`

## 设计特点

- 使用 `echarts/core` + `echarts.use()` 的按需注册方式，避免整包引入。
- 当前统一使用 `SVGRenderer`，没有启用 Canvas 渲染器。

## 使用建议

- 自定义图表组件应优先从这个文件 import `echarts`，不要各自重复注册组件。
- 如果后续增加新的图表类型或组件，先更新这里，再在图表组件里使用。
