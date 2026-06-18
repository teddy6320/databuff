# axios

## 文件定位

[src/utils/axios.ts](/src/utils/axios.ts) 是项目统一的 Axios 实例入口，所有 `src/api/*` 基本都基于它发起请求。

## 核心职责

| 能力 | 说明 |
|------|------|
| 请求前缀 | 自动给大多数请求补 `/webapi` |
| Header 注入 | 自动注入 token、cid、管理域、环境标签 |
| XSS 校验 | 对 POST 请求体做简单非法内容拦截 |
| 取消请求 | 默认注入 `cancelToken()`，统一挂到 `window.axiosCancel` |
| 图表查询补参 | 指定图表接口自动向 `from` 条件里插入环境标签 |
| 登录失效处理 | 返回 `3000` 时清登录态并跳到 `/login` |

## 请求拦截器

### 前缀规则

- 黑名单前缀：`/webapi`、`/api6972`、`/localapi`
- 不在黑名单里的请求，会自动拼成 `/webapi${url}`

### Header 注入

如果存在 token，会调用 [cookie 工具](/docs/utils/cookie.md) 的 `getRequestHeaders()` 注入：

- `Authorization`
- `cid`
- `agi`
- 环境标签相关 header

### XSS 校验

- 只对 `POST` 请求做处理
- 调用 [regexp 工具](/docs/utils/regexp.md) 里的 `xssRegTest`
- 校验失败会直接 reject 一个伪响应错误对象

## 环境标签注入

以下图表接口会额外把环境标签转成 `from` 查询条件：

- `/metrics/exploreMetricByGroupGraph`
- `/base/v2/processGroup/graph`
- `/metrics/lastLastTagValues`

处理逻辑：

- 普通环境标签场景使用 `AND`
- `all:all` 场景会从 `Common/envTagDataByUser` 展开所有可选标签，并使用 `OR`

## 响应拦截器

| 场景 | 行为 |
|------|------|
| HTTP 200 + `status === 3000` | 2 秒后清登录态并跳转登录页 |
| `responseType === 'blob'` | 保留原始 `response` |
| `error.message === 'interrupt'` | 返回一个已解决的中断对象 |
| 业务错误 | 尝试把后端 `message/data` 提升到 `error.message` |

## 相关辅助文件

| 文件 | 作用 |
|------|------|
| [src/utils/cancelToken.ts](/src/utils/cancelToken.ts) | 创建取消令牌并挂到全局数组 |
| [src/utils/jsCookie.ts](/src/utils/jsCookie.ts) | 提供请求头构造能力 |

## 使用建议

- 业务接口尽量只在 `src/api/*` 调用 `http`，不要在页面里直接依赖这个实例。
- 如果某个请求不希望带默认 cancel token，需要显式覆盖 `config.cancelToken`。
