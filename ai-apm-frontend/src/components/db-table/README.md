### Scroll Element Table

#### 扩展功能介绍
- `scroll-mode: Boolean` 是否开启滚动加载模式
  - 滚动模式需要组件父级给定一个有高度的容器，以便自动获取table的最大显示高度
  - 基于`scroll-mode`下的必要`props`：`total`, `loading`
  - `total`: 列表数据的总数，用于内部判断是否还需加载
  - `loading`: 列表当前加载状态，用与内部状态处理

#### columnConfig
  