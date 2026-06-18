# 业务观测模块

> 模块入口: `src/views/observe`

## 模块概述

`observe` 模块围绕“业务场景”和“业务事件”两类对象展开，当前包含 3 个实际入口：

- 业务场景列表与分析页
- 场景地图编辑页
- 业务事件列表页

其中 `/observe/scene/manage` 是挂在业务场景菜单下的静态编辑页，不直接出现在左侧菜单中。

## 页面矩阵

| 类型 | 路由 | 文件 | 说明 |
|------|------|------|------|
| 页面 | `/observe/scene` | `scene/index.vue` | 业务场景列表、地图/漏斗分析 |
| 静态页 | `/observe/scene/manage` | `scene/manage/index.vue` | 场景地图编辑与 KPI 配置 |
| 页面 | `/observe/event` | `event/index.vue` | 业务事件列表与新增/编辑 |

## 关键参数

- `gid`: 当前选中场景分组，常见格式是 `s-{groupId}`
- `id`: 当前选中场景 ID
- `query`: 场景名或事件名搜索关键字
- `mode`: 场景编辑页模式，取值 `add` 或 `edit`

## 典型导航关系

- 业务场景 -> 场景地图编辑: `/observe/scene/manage?mode=add|edit&id=...&gid=...`
- 业务场景 -> 节点侧边详情: 当前页抽屉
- 业务事件 -> 关联业务场景: 新开页签打开 `/observe/scene?...`

## 主要依赖 API

- `scene.ts`: 场景、场景组、事件、场景地图、KPI、漏斗
- `service.ts`: 业务事件编辑时的服务/请求候选数据

详细接口见:

- [Scene API](../../api/scene.md)
- [Service API](../../api/service.md)

## 已补文档

- [业务场景](scene.md)
- [场景地图编辑](scene-manage.md)
- [业务事件](event.md)
