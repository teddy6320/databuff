# API 开发指南

## 基本原则

Databuff 前端的接口封装统一放在 `src/api/*.ts`，页面不应该直接发裸请求。请求底层统一复用 [src/utils/axios.ts](/src/utils/axios.ts)。

## 分层约定

| 层级 | 职责 |
|------|------|
| `src/utils/axios.ts` | 请求前缀、Header 注入、XSS 校验、登录失效处理 |
| `src/api/*.ts` | 按业务域组织接口方法 |
| `src/views/*` | 调用 API、组装页面数据和交互 |

## 新增接口时放在哪里

优先按业务域归类：

- 服务、链路、线程相关放在 `service.ts` / `apm.ts`
- 告警、问题、通知相关放在 `alarm.ts` / `rootCause.ts`
- 用户、组织、账户相关放在 `user.ts` / `system.ts`
- 平台配置、规则、实体配置相关放在 `config.ts` / `monitor.ts`

如果现有文件已经覆盖该业务域，不要再新建一个重复的 API 文件。

## 推荐写法

```ts
import http from '../utils/axios'

export default {
  getExample(params: any) {
    return http.request({
      url: '/example/path',
      method: 'get',
      params,
    })
  },
  saveExample(data: any) {
    return http.request({
      url: '/example/path',
      method: 'post',
      data,
    })
  },
}
```

## GET / POST 约定

- `GET` 请求使用 `params`
- `POST` 请求使用 `data`
- 下载文件时显式声明 `responseType: 'blob'`
- 只有在接口返回结构和现有页面不兼容时，才使用 `transformResponse`

现有例子可参考 [src/api/service.ts](/src/api/service.ts)。

## 请求层已有能力

[src/utils/axios.ts](/src/utils/axios.ts) 已经统一处理了：

- 自动补 `/webapi` 前缀
- token、cid、管理域、环境标签 Header 注入
- POST 体 XSS 校验
- 默认 `cancelToken`
- 登录失效 `status === 3000` 跳转登录

所以在 `src/api/*` 里通常不需要重复做这些事情。

## 什么时候需要额外处理

以下场景可以在 API 层做轻量兼容：

- 后端返回字段名与前端现有消费结构不一致
- 文件下载需要 `blob`
- 个别接口需要取消前一个未完成请求
- 排序、大小写、查询结构需要做统一转换

这类逻辑要尽量收敛在 API 层，不要散落到多个页面里。

## 页面调用建议

- 页面层只 import 对应业务 API 文件
- 不要在多个页面重复拼同样的请求体
- 跨页面复用的查询参数组装逻辑，可以下沉到 API 或工具函数

## 文档同步

新增或修改 API 后，建议同步检查：

- [docs/api/README.md](/docs/api/README.md)
- 对应 API 文档，例如 [docs/api/service.md](/docs/api/service.md)
- 如果某个模块里有页面接口映射文档，也同步更新模块文档

## 自检清单

- 接口是否放在正确的 `src/api/*.ts`
- `GET` 是否用了 `params`，`POST` 是否用了 `data`
- 是否误把页面级兼容逻辑写到了多个页面里
- 是否需要同步更新 API 文档
