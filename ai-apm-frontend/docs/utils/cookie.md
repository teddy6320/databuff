# cookie

## 文件定位

[src/utils/jsCookie.ts](/src/utils/jsCookie.ts) 负责管理登录态、DS 信息、管理域和环境标签等 Cookie/LocalStorage 读写。

## 关键 Key

| Key 常量 | 说明 |
|------|------|
| `DATABUFF-Admin-Token` | 登录 token |
| `DATABUFF-Admin-Cid` | cid |
| `DATABUFF-DSI` | 数据源/租户相关标识 |
| `DATABUFF-OI` | 组织标识 |
| `DATABUFF-DBH` | DBH |
| `DATABUFF-DBA` | API key |
| `DATABUFF-AGI` | 当前管理域 |
| `DATABUFF-ENV-TAG` | 当前环境标签 |

## 常用函数

| 函数 | 说明 |
|------|------|
| `getToken` / `getCid` | 获取登录态 |
| `setTokenAndCid` / `removeTokenAndCid` | 设置/清理登录态 |
| `getDsi` / `setDsi` | 管理 DSI |
| `getOi` / `setOi` | 管理 OI |
| `getDba` / `setDba` | 管理 API key |
| `getAgi` / `setAgi` / `removeAgi` | 管理域选择持久化 |
| `getEnvTag` / `setEnvTag` / `removeEnvTag` | 环境标签持久化 |
| `getRequestHeaders` | 组合请求头 |

## `getRequestHeaders()` 行为

返回头信息时会组合：

- `Authorization`
- `cid`
- 可选 `agi`
- 环境标签相关键值

环境标签逻辑：

- 普通标签选择时，把 `k:v` 转成 header，并设置逻辑为 `and`
- `all:all` 时，从 store 里的 `Common/envTagDataByUser` 展开全部标签，并设置逻辑为 `or`

## 设计特点

- 除 token/cid 外，大多数值都会同时写入 Cookie 和 `localStorage`，以提高回显稳定性。
- `setDba()` 在未传值时会写入一个单机版默认固定 key。

## 使用建议

- 登录、退出登录、切换管理域、切换环境标签时，都应优先调用这里的工具函数，不要手写 Cookie key。
