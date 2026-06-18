# Data Collect API

> 文件: `src/api/dataColl.ts`

## 概述

`dataColl.ts` 主要负责“请求属性采集”配置，也就是数据采集源的列表、详情、保存、启停和删除。

## 接口说明

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getAttrCollList` | `POST` | `/dataCollectorSource/list` | 请求属性列表 |
| `saveAttrColl` | `POST` | `/dataCollectorSource/add` | 创建或编辑请求属性 |
| `toggleAttrCollEnable` | `POST` | `/dataCollectorSource/state` | 修改请求属性状态 |
| `deleteAttrColl` | `POST` | `/dataCollectorSource/delete` | 删除请求属性 |
| `getAttrCollDetail` | `POST` | `/dataCollectorSource/detail` | 请求属性详情 |

## 使用特点

- `saveAttrColl` 当前代码只暴露一个保存入口，没有拆分新增/编辑接口
- 删除、详情、状态切换都统一使用 `POST`
