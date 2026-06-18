# Report API

> 文件: `src/api/report.ts`

## 概述

`report.ts` 负责报告中心能力，包括报告列表、模板管理、封面图片上传/读取以及历史报告数据。

## 接口分组

### 报告与模板

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getReportList` | `POST` | `/report/list` | 报告列表 |
| `downloadReport` | `GET` | `/report/download` | 下载报告 |
| `getTemplateList` | `POST` | `/report/templateList` | 模板列表 |
| `getTemplateDetail` | `GET` | `/report/templateDetail` | 模板详情 |
| `addTemplate` | `POST` | `/report/addTemplate` | 新建模板 |
| `editTemplate` | `POST` | `/report/editTemplate` | 编辑模板 |
| `deleteTemplate` | `DELETE` | `/report/deleteTemplate` | 删除模板 |
| `toggleTemplateStatus` | `POST` | `/report/toggleStatus` | 启停模板 |

### 图片与历史数据

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `uploadImage` | `POST` | `/report/image/upload` | 上传封面图片 |
| `getImage` | `GET` | `/report/image/{imgId}` | 获取图片内容 |
| `getReportHistoryData` | `GET` | `/report/data/{id}` | 获取历史报告数据 |

## 特殊处理

### `getImage`

该接口使用 `responseType: 'blob'`，用于拉取图片二进制内容。

### `getReportHistoryData`

当前代码里先执行真实请求 `http.get(...)`，后面保留了一段不可达的 `Promise.resolve(...)` mock 返回。文档以真实请求为准。
