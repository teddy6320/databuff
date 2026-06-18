# auth

## 文件定位

[src/utils/auth.ts](/src/utils/auth.ts) 导出 `AuthBuilder`，它是路由守卫里用来判断“平台授权状态 + 用户是否已登录 + 当前账号是否过期”的核心工具。

## 核心状态

| 字段 | 说明 |
|------|------|
| `hasToken` | 当前是否有登录 token |
| `platStatus` | 平台授权状态缓存 |
| `finalStatus` | 最终状态，写入根 store 的 `finalStatus` |

## 状态码语义

| 值 | 含义 |
|------|------|
| `0` | 无需处理 |
| `1` | 平台未授权 |
| `2` | 平台已过期 |
| `3` | 平台重新授权 |
| `4` | 已导入 License 但未完成授权 |

## 主要方法

| 方法 | 说明 |
|------|------|
| `__getPlatStatus()` | 调 `SystemApi.getActiveStatus()` 获取平台授权状态 |
| `__getAccountStatus()` | 调 `SystemApi.isOuttime()` 获取账号使用期状态 |
| `getStatus()` | 综合平台状态、登录态和账号状态给出最终结果 |

## 工作流程

1. 先从 Cookie 判断是否有 token。
2. 如果还没拿过平台授权状态，先调用 `getActiveStatus()`。
3. 如果平台未授权、重新授权、未完成授权，或者虽然已授权但还没登录，就直接返回当前 `finalStatus`。
4. 只有“平台已授权且已登录”时，才会继续调用 `isOuttime()` 判断账号期状态。
5. 每次更新状态都会 `store.commit('UPDATE_AUTH_FINAL_STATUS', finalStatus)`。

## 使用位置

主要在 [src/router/index.ts](/src/router/index.ts) 的 `beforeEach` 守卫中使用。

## 注意事项

- `platStatus` 有缓存，避免一次路由链路中重复请求平台状态。
- 这个工具只负责“状态判断”，具体跳到 `/authorization` 还是 `/login` 是路由守卫来决定。
