# 故障排查

> 页面: `/cockpit?type=fault`
> 文件: `src/views/cockpit/tab/fault/index.vue`

## 页面职责

故障排查页签用于从时间趋势和服务排序两个角度观察告警或异常的集中爆发情况，并把用户继续下钻到告警、错误、服务详情或根因分析。

## 页面结构

- `db-tabnav`: `告警状态` / `异常状态` 两个子视角
- 顶部筛选: 业务系统级联选择 + 配置按钮
- `trend-chart`: 时间趋势图
- 时间切片列表: 每个时间点展示服务、所属业务系统和数量
- `config-dialog`: 显示配置与根因分析配置抽屉

## 主要接口

- `ServiceApi.getHealthConfig`
- `ServiceApi.setHealthConfig`
- `ServiceApi.getServiceAlarmTotal`
- `ServiceApi.getServiceAlarmTrend`
- `ServiceApi.getServicesHealth`

详细接口见:

- [Service API](../../api/service.md)

## 关键参数

- `type`: 驾驶舱主页签参数，这里固定为 `fault`
- 页面内部 `styleCfg.type`: 子视角，值为 `alarm` 或 `exception`
- `busIds`: 业务系统级联筛选

## 关键行为

- 顶部两个子视角会实时显示 `告警数` 与 `异常数`
- 趋势图默认按 1 分钟粒度查询
- 点击趋势图中的时间点后，右侧服务列表会切换到对应时间窗口
- 配置抽屉按当前子视角分别维护 `red/yellow` 阈值

## 下钻关系

- 告警视角点击数量: `/alarmCenter/alarm?serviceId=...&fromTime=...&toTime=...`
- 异常视角点击数量: `/appMonitor/errors?sn=...&sid=...&fromTime=...&toTime=...`
- 点击服务名: `/appMonitor/serviceDetail?sid=...&sn=...`
- 点击 AI 图标: `/alarmCenter/rootCauseAnalysis?fromTime=...&toTime=...&sns=...`

## 配置项说明

配置抽屉当前包含两组配置：

- 显示配置: `showServiceNumber`、`red`、`yellow`
- 根因分析配置: `analyseServiceNumber`、`analyseTimeRage`、`analyseGreenService`

保存成功后会刷新当前页签的数据。

## 关联页面

- 告警列表: `/alarmCenter/alarm`
- 错误分析: `/appMonitor/errors`
- 服务详情: `/appMonitor/serviceDetail`
- 根因分析: `/alarmCenter/rootCauseAnalysis`
