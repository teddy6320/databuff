# 业务事件

> 页面: `/observe/event`
> 文件: `src/views/observe/event/index.vue`

## 页面职责

业务事件页用于查看、搜索、启停、编辑和删除业务事件，并管理事件与服务/请求、异常定义之间的关系。

## 页面结构

- `search-group`: 业务事件名称搜索
- `db-table`: 事件列表
- `SceneModal`: 新建 / 编辑业务事件弹窗
- 关联业务场景弹窗: 删除前提示当前事件关联到哪些场景

## 主要接口

- `SceneApi.getEvents`
- `SceneApi.addEvent`
- `SceneApi.editEvent`
- `SceneApi.deleteEvents`
- `SceneApi.getEventRelation`
- `ServiceApi.getServicesIds`
- 子弹窗内部继续使用 `ServiceApi.getServiceRequestByCompTypes`、`SceneApi.getExampleSpanAttrs`

详细接口见:

- [Scene API](../../api/scene.md)
- [Service API](../../api/service.md)

## 关键参数

- `query`: 业务事件名称搜索关键字

## 列表行为

- 列表主查询走 `SceneApi.getEvents`
- `enabled` 列通过开关直接调用 `editEvent` 更新
- 删除前会先查询事件与业务场景的关联关系
- 如果仍有关联场景，不允许直接删除，而是弹窗列出关联场景

## 事件弹窗

`sceneModal.vue` 同时承担新建和编辑两种模式。

表单里最关键的几块是：

- `bizName`
- `bizSubType`
- `bizEventReqs`
- `exceptionRuleObjects`

当前实现特点：

- 业务类型固定为“后端业务”
- 业务粒度支持“服务级”和“接口级”
- 接口级时会继续选择服务、请求类型和请求资源
- 异常定义分为 `system` 和 `business` 两组，并支持拖拽优先级

## 场景关联下钻

删除提示弹窗中，点击关联场景会新开标签页进入：

- `/observe/scene?gid=s-{bizGroupId}&id={sceneId}`

## 关联页面

- 业务场景: `/observe/scene`
