# 告警配置

> 入口路由: `/config/rule`、`/config/convergence`、`/config/response`、`/config/silence`
> 主要文件: `src/views/configManage/alarm/*`

## 页面职责

告警配置负责维护检测规则、收敛策略、响应策略和静默计划，并提供对应的新建、编辑、复制和推荐规则流程。

## 页面结构

- 检测规则: `search-group` + `table-list` + 批量操作区
- 收敛策略: `search-group` + `table-list`
- 响应策略: `search-group` + `table-list`
- 静默计划: `search-group` + `table-list`
- 各设置页: 折叠表单 + 条件编辑器 + 保存/返回

## 页面与静态页

- 检测规则: `/config/rule`
- 推荐规则: `/configManage/alarm/rulePreset`
- 规则设置: `/configManage/alarm/ruleSetting`
- 收敛策略: `/config/convergence`
- 收敛策略设置: `/configManage/alarm/convgSetting`
- 响应策略: `/config/response`
- 响应策略设置: `/configManage/alarm/responseSetting`
- 静默计划: `/config/silence`
- 静默计划设置: `/configManage/alarm/silenceSetting`

## 主要接口

- `MonitorApi.*`: 检测规则、推荐规则、规则详情
- `AlarmApi.*`: 收敛、响应、静默策略及详情
- `MetricApi.*`: 指标选择、条件配置
- `PluginApi.getPresetMonitorByPlugin`
- `NoticeApi.*`: 响应策略通知接收方配置
- `SystemApi.*`: 通知接收方、静默计划时间相关辅助数据
- `ServiceApi.*`: 规则列表筛选中的监控对象选择

详细接口见:

- [Monitor API](../../api/monitor.md)
- [Alarm API](../../api/alarm.md)
- [Metric API](../../api/metric.md)
- [Plugin API](../../api/plugin.md)
- [Notice API](../../api/notice.md)
- [System API](../../api/system.md)
- [Service API](../../api/service.md)

## 关键参数

- 规则设置页:
  - `id`: 规则 ID
  - `mode`: 常见为 `c` 复制、其他值视为编辑/新建
  - `mid`: 预置规则 ID
  - `pn`: 插件监控对象
- 收敛/响应/静默设置页:
  - `id`: 策略 ID
  - `mode`: `c` 时会复制一份去掉原始 ID

## 典型流程

- 检测规则 -> 推荐规则 -> 复制为自定义规则
- 检测规则 / 收敛策略 / 响应策略 / 静默计划 -> 批量启停、删除、导出
- 静默计划设置页会先取当前系统时间，再预览静默时间窗口

## 关联页面

- 推荐规则: `/configManage/alarm/rulePreset`
- 规则设置: `/configManage/alarm/ruleSetting?...`
- 收敛策略设置: `/configManage/alarm/convgSetting?...`
- 响应策略设置: `/configManage/alarm/responseSetting?...`
- 静默计划设置: `/configManage/alarm/silenceSetting?...`

## 注意事项

- `ruleSetting` 也被 `/sysManage/ruleSetting` 复用，系统规则相关说明建议放到后续 `sys-manage` 文档补充
- 检测规则页存在“系统规则”模式分支，但当前配置管理菜单主入口默认是普通规则
