# 场景地图编辑

> 页面: `/observe/scene/manage`
> 文件: `src/views/observe/scene/manage/index.vue`

## 页面职责

场景地图编辑页用于新增或编辑业务场景，维护场景名称、所属分组、KPI 配置以及场景图中的节点和连线。

## 页面结构

- `header.vue`: 场景名称、业务分组、KPI 配置和保存/取消
- 中间画布: 基于 X6 的场景图
- `aside.vue`: 当前节点的名称和事件配置侧栏
- `kpi.vue`: KPI 配置抽屉

## 主要接口

- `SceneApi.getGroup`
- `SceneApi.getEventRelation`
- `SceneApi.addScene`
- `SceneApi.editScene`

详细接口见:

- [Scene API](../../api/scene.md)

## 关键参数

- `mode`: `add` 或 `edit`
- `id`: 编辑时的场景 ID
- `gid`: 编辑时的场景分组 ID

## 页面行为

- `mode=add` 时以默认单节点场景初始化
- `mode=edit` 时会先根据 `id` 拉取当前场景详情
- 保存时统一由 `header.vue` 汇总表单、图数据和 KPI 配置后提交
- 保存成功后，如果是新建场景，会把当前路由替换为 `mode=edit&id=...&gid=...`

## 图编辑说明

画布里的核心对象是：

- `nodes`: 场景节点
- `edges`: 节点连线

当前实现里有几个要点：

- 点击节点会打开右侧配置栏
- 只有存在入边的节点才允许删除
- 删除节点时，会把上下游连线尽量前移一层，避免直接断图
- KPI 配置保存在 `kpiConfigObject`

## 返回关系

- 点击取消会直接返回 `/observe/scene`
- 保存成功后仍停留在当前编辑页

## 关联页面

- 业务场景: `/observe/scene`
