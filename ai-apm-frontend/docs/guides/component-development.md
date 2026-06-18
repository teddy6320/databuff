# 组件开发指南

## 组件分层

当前项目里的组件大致分成两类：

- `src/components/` 下的全局复用组件
- `src/views/<module>/` 下的页面私有组件

只有跨模块、复用频率高的组件，才建议放进 [src/components](/src/components)。

## 什么时候放到 `src/components`

满足下面任意两条，通常就值得抽成共享组件：

- 在多个业务模块复用
- 交互模式稳定，不依赖单一页面上下文
- 可以通过 props / slots / events 配置
- 后续值得继续维护独立文档

如果组件只服务单个页面，优先保留在页面目录内。

## 当前全局注册组件

根据 [src/components/index.ts](/src/components/index.ts)，当前全局注册了：

- `DbTabnav`
- `ScrollSelect`
- `DbTable`
- `DbRadio`
- `DbIconButton`
- `BasicChart`
- `DbQuery`

新增组件时，只有在“全局高频复用”成立时才建议加进这个入口。

## 推荐目录结构

常见写法有两种：

1. 单文件组件  
   例如 [src/components/scroll-select.vue](/src/components/scroll-select.vue)
2. 组件目录 + `index.vue`  
   例如 [src/components/db-tabnav/index.vue](/src/components/db-tabnav/index.vue)

如果组件存在类型、样式、子组件或说明文档，优先使用目录结构。

## 设计建议

- Props 尽量保持输入清晰，避免“一个对象塞所有配置”
- 对外暴露事件时，名称要贴近业务动作，如 `change`、`confirm`、`close`
- 可复用容器优先支持 `slot`
- 表格、筛选、选择器类组件要考虑异步加载、空状态、禁用态

## 和业务页面的边界

共享组件应该尽量避免：

- 直接依赖具体模块 API
- 直接读写某个业务页面的路由 query
- 耦合单一模块的实体命名

如果确实需要业务特化，优先把“通用壳层”和“业务拼装层”拆开。

## 文档同步

新增或重构共享组件后，建议同步更新：

- [docs/components/README.md](/docs/components/README.md)
- 对应组件文档，例如 [db-table](/docs/components/business/db-table.md)
- 如有必要，更新 [docs/DOC-STRUCTURE.md](/docs/DOC-STRUCTURE.md) 的组件进度

## 优先参考的现有组件

| 组件 | 可参考点 |
|------|----------|
| [DbTable 文档](/docs/components/business/db-table.md) | 表格增强与统一交互 |
| [Query Filter 文档](/docs/components/filter/query-filter.md) | URL 联动型筛选 |
| [Dialog Template 文档](/docs/components/business/dialog-template.md) | 弹窗骨架拆分 |
| [Scroll Select 文档](/docs/components/form/scroll-select.md) | 异步加载型选择器 |

## 开发检查清单

- 这个组件是否真的需要进入 `src/components`
- 是否可以被 props / slot 配置，而不是写死业务逻辑
- 是否需要加入全局注册入口
- 是否补充了最基本的使用文档
