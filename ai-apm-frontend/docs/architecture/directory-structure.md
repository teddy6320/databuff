# 目录结构

> 最后更新: 2026-04-05
> 说明: 本文以当前仓库实际存在的目录与文件为准，聚焦顶层结构和主要模块，不再罗列明显过时的历史结构。

## 项目根目录

```text
databuff/
├── .browserslistrc
├── .gitignore
├── .gitlab-ci.yml.bac
├── .npmrc
├── .prettierrc
├── .yarn/
├── .yarnrc
├── README.md
├── docs/
├── index.html
├── package.json
├── public/
├── src/
├── tsconfig.json
├── vite.config.ts
└── yarn.lock
```

## `src/` 目录

```text
src/
├── api/              # 接口封装
├── assets/           # 图片、字体、样式
├── components/       # 通用组件
├── mixins/           # 全局 mixin
├── router/           # 路由与菜单配置
├── store/            # Vuex
├── utils/            # 工具函数
├── views/            # 业务页面
├── App.vue
├── event-bus.ts
├── main.ts
├── shims-tsx.d.ts
├── shims-vue.d.ts
├── types.d.ts
└── vite-env.d.ts
```

## `src/api/`

当前 API 层按业务域拆分，核心文件包括:

```text
api/
├── alarm.ts
├── apm.ts
├── bs.ts
├── bs.mock.ts
├── config.ts
├── dataAccess.ts
├── dataColl.ts
├── envTag.ts
├── group.ts
├── infrastructure.ts
├── infrastructure.types.ts
├── kubernetes.ts
├── log.ts
├── metric.ts
├── metric.types.ts
├── monitor.ts
├── notice.ts
├── notice.type.ts
├── npm.ts
├── plugin.ts
├── process.ts
├── report.ts
├── rootCause.ts
├── scene.ts
├── service.ts
├── system.ts
└── user.ts
```

## `src/components/`

通用组件以目录组件与单文件组件混合组织，当前主要包括:

```text
components/
├── DbIconButton/
├── charts/
├── chat-ai/
├── collapse-tags/
├── cont-wrapper/
├── db-menu/
├── db-radio/
├── db-table/
├── db-tabnav/
├── dialog-template/
├── flame-chart-js/
├── matching-criteria/
├── query-filter/
├── router-view-temp/
├── statistic/
├── text-expand/
├── code-view.vue
├── ipInput.vue
├── marked-view.vue
├── metric-info-tooltip.vue
├── metric-select.vue
├── metric-type-cascader.vue
├── metric-unit-cascader.vue
└── scroll-select.vue
```

## `src/router/`

```text
router/
├── index.ts              # Router 实例、守卫、动态路由注入
├── route-data.ts         # 菜单/路由元数据
├── route.types.ts        # 路由类型定义
├── breadcrumb-data.ts    # 面包屑配置
└── time-new.ts           # 时间范围选项
```

## `src/store/`

```text
store/
├── index.ts
├── mutation-types.ts
└── modules/
    ├── common/
    ├── global/
    ├── service/
    └── user/
```

## `src/utils/`

```text
utils/
├── auth.ts
├── axios.ts
├── browserVersion.ts
├── cancelToken.ts
├── clickout.ts
├── common.ts
├── compareVersion.ts
├── echarts.ts
├── formatColor.ts
├── getUnitData.ts
├── jsCookie.ts
├── metric-query-format.ts
├── regexp.ts
├── timeFormat.ts
├── filters/
└── static/
```

## `src/views/`

页面以业务域划分，当前主要一级目录如下:

```text
views/
├── 404/
├── alarmCenter/
├── appMonitor/
├── authorization/
├── cockpit/
├── configInstall/
├── configManage/
├── configStatus/
├── dataReport/
├── help/
├── hide/
├── infrastructure/
├── layout/
├── log/
├── login/
├── metrics/
├── npm/
├── observe/
├── personal/
├── rum/
├── singleLogin/
└── sysManage/
```

其中较大的业务域会继续按二级目录拆分，例如:

- `src/views/alarmCenter/`: `alarm`、`alarmDetail`、`eventDetail`、`notice`、`problemAnalysis`、`problemDetail`、`rootCause`、`rootCauseAnalysis`
- `src/views/appMonitor/`: `service`、`trace`、`errors`、`database`、`serviceFlow`、`relationMapNew` 等
- `src/views/infrastructure/`: `host`、`docker`、`cluster`、`process` 及对应 detail 页面
- `src/views/sysManage/`: `account`、`group`、`health`、`org`、`role`、`systemEvent`、`systemRule` 等

## `public/`

```text
public/
├── css/
│   ├── element-icon.css
│   ├── element-light.css
│   └── fonts/
└── img/
    ├── favicon-empty.ico
    ├── favicon.ico
    ├── logo_text_h.svg
    ├── logo_text_h_white.svg
    └── logo_white.svg
```

## 文件命名习惯

| 类型 | 习惯 | 示例 |
|------|------|------|
| Vue 组件目录 | kebab-case / camelCase 混用，跟随历史代码 | `alarmCenter/`, `configInstall/` |
| 目录组件入口 | `index.vue` | `src/views/alarmCenter/alarm/index.vue` |
| TypeScript 文件 | camelCase | `timeFormat.ts`, `route-data.ts` |
| 类型定义 | `*.types.ts` / `*.d.ts` | `route.types.ts`, `vite-env.d.ts` |

## 说明

- 本仓库存在一定历史包袱，目录命名并非完全统一
- 路由 path 与视图目录并不是一一同名映射，部分页面通过 `filePath` 做了显式指定
- `router-view-temp` 等目录属于布局/占位型组件，用于承载多级菜单路由

## 相关文档

- [架构概述](overview.md)
- [技术栈说明](tech-stack.md)
