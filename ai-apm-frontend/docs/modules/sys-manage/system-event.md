# 系统事件

> 页面: `/sysManage/systemEvent`
> 文件: `src/views/sysManage/systemEvent/index.vue`

## 页面职责

系统事件页用于检索系统级事件，并作为系统事件详情和系统规则页的入口。

## 页面结构

- `search-group`: 事件条件筛选
- 顶部按钮: 前往系统规则
- `db-table`: 系统事件列表

## 主要接口

- `AlarmApi.getSystemEventList`

详细接口见:

- [Alarm API](../../api/alarm.md)

## 关键参数

- 列表查询会把全局时间范围转换为:
  - `start`
  - `end`
- 当筛选项中 `level` 为单值时，会在请求前改写为 `levels`
- 详情页入口参数:
  - `eid`: 事件 ID

## 关联页面

- 系统事件详情: `/sysManage/eventDetail?eid=...`
- 系统规则: `/sysManage/systemRule`

## 注意事项

- `/sysManage/eventDetail` 复用的是告警中心的事件详情实现，详情参数仍为 `eid`
