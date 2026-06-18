# 主机监控

> 页面: `/infrastructure/host`
> 文件: `src/views/infrastructure/host/index.vue`

## 页面职责

主机页是基础设施模块的主入口，支持从表格或主机地图视角查看主机状态、健康度、分组与告警情况，并作为主机详情的跳转入口。

## 页面结构

- 顶部工具区: 时间范围、视图模式切换、分组切换
- `choose-collapse`: 多条件筛选与多值搜索
- `db-table`: 主机列表、批量加标签、静默、告警跳转
- `hostMap`: 主机地图模式
- `tag-dialog`: 批量打标签弹窗

## 主要接口

- `InfraApi.getHostList`
- `InfraApi.getHostGroupList`
- `InfraApi.getGroupList`
- `InfraApi.getHostmapList`
- `InfraApi.getHostObjs`
- `InfraApi.getHostTag`
- `InfraApi.getHostInfo`

详细接口见:

- [Infrastructure API](../../api/infrastructure.md)

## 关键参数

- `mode`: 页面展示模式，常见为 `table` 或 `chart`
- `group`: 主机分组条件，可为数组
- `multisearch`: 左侧折叠筛选回写到 URL 的复合查询
- 详情页入口使用 `hostName`，兼容 `sn`
- 详情页页签由 `type` 控制，可取 `construct`、`baseinfo`、`metric`、`alarm`、`process`、`docker`、`pod`
- 从主机页跳主机指标页时会额外带 `app`

## 详情页说明

主机详情页位于 `src/views/infrastructure/hostDetail/index.vue`，先调用 `InfraApi.getHostInfo` 获取主机信息，再按页签加载子组件数据。

- 默认页签是 `construct`
- 当主机被识别为 Kubernetes Node 时显示 `pod` 页签，否则显示 `docker`
- `alarm` 页签会复用告警中心能力

## 关联页面

- 主机详情: `/infrastructure/hostDetail?hostName=...`
- 告警列表: `/alarmCenter/alarm?host=...`
- 进程页签 / Docker 页签 / Pod 页签都从主机详情继续下钻
