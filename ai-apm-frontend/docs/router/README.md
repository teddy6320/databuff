# 路由系统文档

## 概述

Databuff 使用 Vue Router 3.x 作为路由管理器，采用 History 模式，base 路径为 `/databuff`。

## 文件结构

```
src/router/
├── index.ts           # 路由主入口，包含守卫和动态路由生成
├── route-data.ts      # 路由数据定义（菜单配置）
├── route.types.ts     # TypeScript 类型定义
├── breadcrumb-data.ts # 面包屑路径配置
└── time-new.ts        # 时间范围选项配置
```

## 路由模式

| 配置项 | 值 |
|--------|-----|
| mode | history |
| base | /databuff |
| linkActiveClass | active |

## 静态路由

| 路径 | 名称 | 说明 |
|------|------|------|
| `/login` | Login | 登录页 |
| `/authorization` | Authorization | 授权页 |
| `/singleLogin/:type` | SingleLogin | 单点登录 |
| `/404` | 404 | 404 页面 |

## 白名单路由

以下路由无需登录验证：
- `/login`
- `/authorization`
- `/404`

单点登录路由：
- `/singleLogin/imc`

## 动态路由

动态路由通过 `formatMenusSource()` 函数根据用户权限菜单动态生成：

1. 从 Store 获取用户菜单树 `User/getMenusTree`
2. 通过 `generRouter()` 递归生成路由配置
3. 使用 `router.addRoutes()` 添加到路由表

### 动态路由加载机制

使用 `import.meta.glob` 预加载所有 `views/**/index.vue` 模块：

```typescript
const viewModules = import.meta.glob('/src/views/**/index.vue');
```

组件动态加载时，根据 `menu.filePath` 查找对应的视图模块。

## 路由守卫

### beforeResolve - 时间参数处理

主要处理时间范围参数：
- 解析 URL 中的 `fromTime`、`toTime`、`durationRange` 参数
- 验证时间范围有效性（不超过 `limitDays` 天）
- 同步全局时间状态到 Store

### beforeEach - 认证拦截

处理流程：
1. 单点登录路径直接放行
2. 处理 Root Lab 实验室的 token/cid
3. 新窗口打开判断
4. 取消未完成的 axios 请求
5. 获取授权状态 `authBuilder.getStatus()`
6. 未授权跳转 `/authorization`
7. 登录状态检查：
   - 已登录：获取用户信息、角色组、服务信息
   - 未登录：跳转 `/login`

## 路由元信息

详见 [route-types.md](./route-types.md)

## 菜单模块

详见 [route-data.md](./route-data.md)

## 面包屑

详见 [breadcrumb.md](./breadcrumb.md)

## 时间选择器

详见 [time-options.md](./time-options.md)

## 相关文件

- `src/store/modules/user/index.ts` - 用户菜单与权限数据
- `src/utils/auth.ts` - 授权工具类
- `src/utils/jsCookie.ts` - Token 管理
