# Flame Chart

## 组件说明

这里对应的不是 Vue 单文件组件，而是一套独立的火焰图渲染库，源码目录在 [src/components/flame-chart-js](/src/components/flame-chart-js)。

## 主要入口

| 文件 | 说明 |
|------|------|
| [index.js](/src/components/flame-chart-js/index.js) | 总入口，组装插件并导出 `FlameChart` |
| [flame-chart-container.js](/src/components/flame-chart-js/flame-chart-container.js) | 容器类，负责 render / resize / zoom |
| `plugins/*` | 火焰图、时间网格、瀑布图、切换器等插件 |
| `engines/*` | 渲染引擎与交互引擎 |

## 设计方式

这套实现是“容器 + render engine + interactions engine + plugins”的插件化结构：

- 容器负责生命周期
- 引擎负责绘制和交互
- 插件负责具体图层与功能

## 当前能力

- Flame Chart 主视图
- Waterfall 视图
- 时间网格
- 时间范围选择
- 标记点
- 视图切换

## 使用建议

- 这是偏底层的可视化能力，不建议普通页面直接接入
- 如果业务上只需要展示线程/方法热点，优先先找现有页面实现复用
