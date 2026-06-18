# 报告

> 页面: `/report`
> 文件: `src/views/dataReport/report/index.vue`

## 页面职责

报告页用于查看历史生成的报告、预览/下载报告内容，并作为模板管理入口。

## 页面结构

- 顶部筛选区:
  - 报告名称搜索
  - 报告类型筛选
  - 时间范围
- 列表区:
  - 无限滚动表格
  - 下载 / 预览等操作
- 预览与导出:
  - 基于 `PdfComponent`
  - 使用历史报告数据回填模板内容

## 主要接口

- `ReportApi.getReportList`
- `ReportApi.getReportHistoryData`
- 列表下载使用 `/webapi/report/download?id=...&format=word`

详细接口见:

- [Report API](../../api/report.md)

## 关键参数

- `rq`: 报告名称关键字
- `rt`: 报告类型
- 时间范围由全局时间选择器驱动，并换算为:
  - `fromTime`
  - `toTime`

## 模板相关页面

### 模板列表

页面: `/report/template`
文件: `src/views/dataReport/report/template/index.vue`

职责:

- 查看模板列表
- 预览模板
- 复制模板
- 删除模板
- 启停模板
- 进入新建/编辑模板页

主要接口:

- `ReportApi.getTemplateList`
- `ReportApi.addTemplate`
- `ReportApi.deleteTemplate`
- `ReportApi.toggleTemplateStatus`

关键参数:

- `rq`: 模板名称关键字
- `rt`: 报告类型

### 模板设置页

页面: `/report/setting`
文件: `src/views/dataReport/report/setting/index.vue`

职责:

- 新建或编辑报告模板
- 维护封面、指标页、内容页、附加配置

页面结构:

- `db-tabnav`:
  - `base`
  - `index`
  - `content`
  - `other`
- 预览对话框

主要接口:

- `ReportApi.getTemplateDetail`
- `ReportApi.addTemplate`
- `ReportApi.editTemplate`
- `ReportApi.uploadImage`
- `MetricApi` 中的指标能力用于模板内容配置

关键参数:

- `id`: 模板 ID；存在时进入编辑态

关键行为:

- 页面会设置面包屑为“新增模版”或“编辑模版”
- 保存前会把 `cycleTime` / `customTime` / `content` 序列化成后端结构
- 若上传了新的封面图，会先调 `uploadImage`

## 注意事项

- 报告列表和模板列表都不是标准分页，而是表格滚动加载
- 模板页里 `preset=1` 的预置模板不能直接编辑或删除
- 报告预览与下载依赖模板结构和历史报告数据的二次格式化
