# 业务场景

> 页面: `/observe/scene`
> 文件: `src/views/observe/scene/index.vue`

## 页面职责

业务场景页用于按分组浏览业务场景，并在同一页面内查看业务地图、转化漏斗和 KPI 统计，同时承担场景编辑页的主入口。

## 页面结构

- `search-group`: 业务场景名称搜索
- 左侧 `el-tree`: 分组树 + 场景列表
- 顶部操作区: 管理分组、创建业务场景
- KPI 统计卡片: 自定义 KPI、转化数量、错误数
- `map-chart`: 业务地图
- `funnel-chart`: 转化漏斗
- `AsideDetail`: 场景节点详情抽屉
- `GroupModal`: 分组管理弹窗

## 主要接口

- `SceneApi.getGroup`
- `SceneApi.getSceneGroup`
- `SceneApi.getSceneNames`
- `SceneApi.getBizKpis`
- `SceneApi.deleteScene`
- `SceneApi.editScene`
- 子组件继续使用 `SceneApi.getSceneMapInfo`、`SceneApi.getBizFunnel`

详细接口见:

- [Scene API](../../api/scene.md)

## 关键参数

- `gid`: 当前场景所属分组
- `id`: 当前场景 ID
- `query`: 场景名搜索关键字

页面进入时会优先按下面顺序恢复当前选中场景：

1. 路由里的 `gid + id`
2. 路由里的 `query`
3. 第一组下的第一个场景

## 页面行为

- 左侧树节点切换后，会刷新路由里的 `gid` 和 `id`
- 点击“创建业务场景”或“设置”会跳到场景地图编辑页
- 点击铅笔图标会直接弹出场景名称修改框
- 点击删除会删除当前场景并刷新树数据

## 分析视图

页面内部有两个分析页签：

- `map`: 业务地图
- `funnel`: 转化漏斗

切换和刷新逻辑有两个特点：

- KPI 卡片统一由 `getBizKpis` 提供
- 当前页签如果是 `funnel`，刷新后会主动调用漏斗子组件重新取数；如果是 `map`，则刷新地图组件

## 关联页面

- 场景地图编辑: `/observe/scene/manage?mode=add|edit&id=...&gid=...`
