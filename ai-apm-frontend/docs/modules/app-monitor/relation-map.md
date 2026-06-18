# 空间地图

> 页面: `/appMonitor/relationMap`
> 文件: `src/views/appMonitor/relationMapNew/index.vue`

## 页面职责

空间地图页是应用性能模块的全局拓扑视图，支持在 `service / process / host / application` 等层级之间切换，并联动右侧详情与图表。

## 页面结构

- `sidebar`: 拓扑层级切换
- `search`: 搜索、配置和交互动作
- `TopoGraph`: 主拓扑画布
- `aside-chart`: 侧边图表分析
- `aside-detail`: 节点详情

## 主要接口

- `BsApi.getRelationStatus`
- `BsApi.getSpaceMap`
- `BsApi.getSpaceMapEdge`
- `BsApi.getSpaceMapNpmEdge`
- `BsApi.getLayout`
- `BsApi.setLayout`

详细接口见:

- [Business System API](../../api/bs.md)

## 关键参数

- 路由 query `type` 决定初始层级

## 特点

- 支持读取和保存本地拓扑展示配置
- 支持 RUM 关联能力开关
- 右侧详情与图表会跟随当前点击节点联动
