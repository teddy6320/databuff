# 服务流

> 页面: `/appMonitor/serviceFlow`
> 文件: `src/views/appMonitor/serviceFlow/index.vue`

## 页面职责

服务流页用于展示服务之间的流向关系，是服务关系的全局视图。

## 页面结构

- 根页只负责权限与授权判断
- 实际图表由 `components/service-flow.vue` 承载

## 页面限制

- 路由上配置了 `limitDays: 1`
- 页面要求授权有效，否则直接展示“未授权或授权已过期”

## 主要接口

- 服务流相关取数来自 `apm.ts` / `service.ts`

详细接口见:

- [APM API](../../api/apm.md)
- [Service API](../../api/service.md)
