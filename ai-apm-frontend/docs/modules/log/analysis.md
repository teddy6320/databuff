# 日志分析

> 页面: `/log`
> 文件: `src/views/log/index.vue`

## 页面职责

日志页用于按时间范围、关键词、主机和服务筛选日志，并支持在结果集内继续做上下文查看和双日志差异对比。

## 页面结构

- 顶部工具区: 关键词搜索、取消选择、日志对比、配置说明
- `choose-collapse`: 左侧快捷筛选，目前提供主机和服务两组条件
- `db-table`: 日志列表，启用勾选、排序、展开行和偏移分页
- `code-view`: 展开行里查看完整日志内容
- `code-diff`: 两条日志对比弹窗
- `more-log`: 日志上下文弹窗

## 主要接口

- `LogApi.getLogList`
- `LogApi.getLogsCondition`

详细接口见:

- [Log API](../../api/log.md)

## 关键参数

- `query`: 关键词搜索，写入 URL 时会做 `encodeURIComponent`
- `hosts`: 主机筛选，多选数组
- `services`: 服务筛选，多选数组
- 页面内部会把全局时间转换成 `fromTimeNs`、`toTimeNs` 后再发给日志查询接口

## 列表行为

- 列表使用 `db-table` 的 `offsetMode`，更接近“滚动加载 / 偏移分页”模式
- 展开行直接展示当前日志的 `message`
- 行勾选最多允许两条，超过后其他行会被禁用
- 排序变化会触发表格刷新

当前展示字段包括：

- `_timestamp`
- `hostname`
- `service`
- `status`
- `message`

其中 `_timestamp` 由接口返回的 `timestamp` 截取前 13 位后转成毫秒时间戳。

## URL 回写规则

- 搜索关键词通过 `query` 回写
- 快捷筛选通过 `hosts`、`services` 回写
- 多选筛选值会逐项 `encodeURIComponent`
- 页面重新进入时会根据 URL 自动回显搜索词和筛选条件

## 日志对比

点击“日志对比”前必须先勾选两条日志。

- 页面会优先尝试把 `message` 解析成 JSON 并格式化
- 如果解析失败，则直接按原始文本对比
- 内容一致时弹窗显示“对比文件无差异”
- 差异渲染使用 `diff2html` 的并排视图

## 日志上下文

点击“查看上下文”会打开上下文弹窗，由 `more-log.vue` 负责加载。

- 选中日志本身固定显示为序号 `0`
- 会分别向前、向后各取日志
- “展开更早” 使用 `sortOrder=desc`
- “展开更新” 使用 `sortOrder=asc`
- 两侧查询都复用 `LogApi.getLogList`

上下文查询有两个实现细节值得记录：

- 上下文时间范围不是整页通用参数直接透传，而是以前端当前全局时间为边界，再用当前日志的 `timestamp` 切成前后两段
- 代码里查询参数字段写的是 `sortFiled`，这里按现状记录，避免和实际请求不一致

## 关联页面

- 日志采集配置说明: `/config/install?type=log`
