# Log API

> 文件: `src/api/log.ts`

## 概述

`log.ts` 目前接口量不多，主要服务于日志列表页，提供日志检索、筛选条件查询，以及一份仅前端使用的 mock 数据方法。

## 接口说明

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getLogList` | `POST` | `/log/search` | 查询日志列表 |
| `getLogsCondition` | `POST` | `/log/conditions` | 获取日志筛选条件 |
| `getLogListMock` | - | - | 返回前端 mock 日志数据 |

## 特殊处理

### `getLogListMock`

该方法不会发起网络请求，而是直接返回 `Promise.resolve(...)` 的本地 mock 数据，主要用于开发调试。

返回结构里包含:

- `data`
- `total`
- `scrollId`
- `offset`

其中每条日志的 `id` 由 `uuid` 动态生成。
