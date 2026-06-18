# 健康度配置

> 页面: `/sysManage/health`
> 文件: `src/views/sysManage/health/index.vue`

## 页面职责

健康度配置页用于定义不同实体类型的评分规则、默认规则和颜色分级规则。

## 页面结构

- `db-tabnav`: 主机、业务系统、服务、数据库、消息队列、缓存、外部服务、接口
- `Config` 面板:
  - 自定义评分规则列表
  - 默认评分规则编辑
  - 颜色标识规则编辑
  - 抽屉式规则编辑器

## 主要接口

- `Common/GET_METRIC_TYPE_AND_LIST`
- `Common/GET_METRIC_INFOS`
- `HealthApi.gethealthConfig`
- `HealthApi.createRule`
- `HealthApi.editRule`
- `HealthApi.deleteRule`
- `HealthApi.setLevel`
- `HealthApi.sortRules`

## 关键数据来源

- 页面进入时会通过 `Common/GET_METRIC_TYPE_AND_LIST` 预加载指标分类及分类下指标列表。
- 规则编辑器中的指标分类 cascader 通过 `Common/getMetricTypeDataByType` 按当前实体类型裁剪可选范围。
- 规则编辑器中的指标下拉通过 `Common/getMetricsByType` 按已选分类筛选候选指标。
- 单个指标项需要的指标详情和单位信息通过 `Common/GET_METRIC_INFOS` 获取。

## 关键参数

- 页签由 `active` 控制，页面内会自动转成大写后匹配，如 `host -> HOST`

## 注意事项

- `health` 已不再依赖 `src/views/dataReport/store`，相关指标分类和指标详情能力已经迁移到 `Common` store
- `HealthApi` 定义在模块本地文件 [health.api.ts](/src/views/sysManage/health/health.api.ts)，不是 `src/api/` 下的公共 API 文件
- 自定义规则支持拖拽排序，排序结果会直接提交到后端
