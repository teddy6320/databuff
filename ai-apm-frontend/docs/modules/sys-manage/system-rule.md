# 系统规则

> 页面: `/sysManage/systemRule`
> 文件: `src/views/sysManage/systemRule/index.vue`

## 页面职责

系统规则页用于维护系统级检测规则，页面本身是对配置管理规则列表的系统模式封装。

## 页面结构

- 外层容器页
- 内部直接复用 `configManage/alarm/rule/index.vue`
- 设置页继续复用 `configManage/alarm/ruleSetting/index.vue`

## 主要接口

- 列表页复用 `MonitorApi`、`AlarmApi`、`SystemApi` 相关规则能力
- 设置页详情使用:
  - `MonitorApi.getSystemMonitorDetail`

详细接口见:

- [Monitor API](../../api/monitor.md)
- [Alarm API](../../api/alarm.md)
- [System API](../../api/system.md)

## 关键参数

- 列表页与普通规则页共用大部分筛选能力
- 设置页入口参数:
  - `id`: 规则 ID
  - `mode`: `c` 表示复制
- 关闭设置页时会返回 `/sysManage/systemRule`

## 注意事项

- 这是“系统规则”模式，不支持普通检测规则页里的静默相关能力
- `/sysManage/ruleSetting` 与 `/configManage/alarm/ruleSetting` 共用同一个实现，只是详情接口和返回路径不同
