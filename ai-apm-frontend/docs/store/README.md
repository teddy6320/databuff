# Store 文档

## 目录概览

`src/store/` 使用 Vuex 组织全局状态，当前由一个根 store 和 4 个 module 组成：

- `globalState`
- `User`
- `Common`
- `Service`

入口文件是 [src/store/index.ts](/src/store/index.ts)。

## 根 Store 职责

根 store 主要负责：

- `finalStatus` 授权状态
- 面包屑补充数据与重设
- 主题切换和主题变量 getter
- 注册 4 个 namespaced module

## 模块索引

| 文档 | 说明 |
|------|------|
| [user](user.md) | 登录态、菜单、角色、管理域、Logo 配置 |
| [common](common.md) | 指标分类、指标信息、标签映射、环境标签缓存 |
| [global](global.md) | 全局时间范围、刷新、事件栈、图表栈 |
| [service](service.md) | 服务基础信息、业务树与服务-业务映射 |

## 目录结构

| 路径 | 说明 |
|------|------|
| `src/store/index.ts` | 根 store |
| `src/store/mutation-types.ts` | 全局 mutation 常量 |
| `src/store/modules/user/index.ts` | 用户与权限相关状态 |
| `src/store/modules/common/index.ts` | 通用缓存数据 |
| `src/store/modules/global/index.ts` | 时间与刷新相关全局状态 |
| `src/store/modules/service/index.ts` | 服务与业务树缓存 |

## 当前特点

- `User` 模块承载的职责最多，是路由权限、管理域和品牌配置的核心来源。
- `globalState` 不是 namespaced 文档名里的 `global`，但目录规划里按 `global` 来描述更直观。
- `Common` 和 `Service` 都偏“缓存型 store”，会通过“已有状态则不重复请求”的方式避免重复拉数。
