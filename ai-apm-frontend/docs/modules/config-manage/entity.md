# 实体管理

> 入口路由: `/config/service`、`/config/business`、`/config/process`、`/config/request`、`/config/relationest`
> 主要文件: `src/views/configManage/entity/*`

## 页面职责

实体管理负责维护服务、业务系统、进程、请求属性和空间地图相关的配置，是 APM、基础设施与拓扑能力的配置入口。

## 页面结构

- 服务监控: `db-tabnav` 切换 `global` / `app`
- 业务系统监控: 列表页 + 业务系统管理静态页
- 进程监控: `进程采集` / `进程识别` 两个页签
- 请求监控: 当前只有 `请求属性采集`
- 拓扑配置: 中间件类型开关、指标类型选择与配置弹窗

## 页面与静态页

- 服务监控: `/config/service`
  - `type=global|app` 控制页签
  - `global` 使用 `ConfigApi`
  - `app` 使用 `ConfigApi`、`ServiceApi`
- 业务系统监控: `/config/business`
  - 列表页使用 `BsApi.getGroupTree` 等接口
  - 业务系统管理页: `/config/business/manage?bid=...&mode=...`
- 进程监控: `/config/process`
  - 进程采集与识别页分别使用 `ProcessApi`
- 请求监控: `/config/request`
  - 请求属性采集设置页: `/config/request/attrCollSetting?id=...`
- 拓扑配置: `/config/relationest`
  - 使用业务系统、系统设置和指标类型接口维护中间件配置

## 主要接口

- `ConfigApi.*`: 服务监控全局/应用配置
- `ServiceApi.getServicesIds`
- `BsApi.getGroupTree`
- `BsApi.getBsTree`
- `BsApi.getBsRules`
- `BsApi.addBsWithRules`
- `BsApi.updateBsWithRules`
- `BsApi.getRelationStatus`
- `ProcessApi.*`: 进程采集、识别、规则配置
- `CollApi.*`: 请求属性采集与详情
- `SystemApi.getMidTypeList`
- `SystemApi.getMidStatus`
- `MetricApi.getMetricTypesByQuery`

详细接口见:

- [Config API](../../api/config.md)
- [Business System API](../../api/bs.md)
- [Process API](../../api/process.md)
- [Data Collect API](../../api/data-coll.md)
- [System API](../../api/system.md)
- [Metric API](../../api/metric.md)
- [Service API](../../api/service.md)

## 关键参数

- 服务监控页签使用 `type`
- 业务系统管理页:
  - `bid`: 业务系统 ID
  - `mode`: `add` / `e`
- 请求属性采集设置页:
  - `id`: 配置 ID
- 进程监控页签当前实现存在参数不一致:
  - 初始化读取 `ct`
  - 页签切换回写 `type`

## 关联页面

- 业务系统管理: `/config/business/manage?...`
- 请求属性采集设置: `/config/request/attrCollSetting?id=...`

## 注意事项

- `entity/process/index.vue` 的 URL 参数前后不一致，后续如果要做深链修复，建议统一为同一个 query key
- `service` 与 `business` 更偏配置管理，不等同于 `app-monitor` 下的服务/业务系统观察页
