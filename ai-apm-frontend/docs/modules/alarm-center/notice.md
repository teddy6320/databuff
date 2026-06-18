# 通知记录

## 页面信息

| 属性 | 值 |
|------|-----|
| 文件 | `src/views/alarmCenter/notice/index.vue` |
| 路由 path | `/alarmCenter/notice` |
| 查询 API | `AlarmApi.getNoticeRecordList` |
| 重发 API | `AlarmApi.resendNotice` |

## 页面结构

- `search-group`: 条件筛选
- `db-table`: 通知记录列表
- 行内操作: 失败记录支持“重新发送”

## 查询参数

页面会把全局时间转换为接口参数:

```ts
{
  ...searchParams,
  from: timeParams.fromTime,
  to: timeParams.toTime,
}
```

当全局时间类型为快捷选择 `select` 时，页面会主动把 `to` 修正为“当前时间”。

## 表格列

| 字段 | 说明 |
|------|------|
| `result` | 通知结果 |
| `noticeTime` | 通知时间 |
| `receiver` | 接收者，页面会由 `rcvNames` 和 `rcvUgNames` 组装 |
| `method` | 通知方式 |
| `errMsg` | 失败原因 |
| `alertType` | 告警类型 |
| `alertDesc` | 告警描述 |
| `alertStartTime` | 告警开始时间 |

## 通知方式展示

- `email`: 邮件
- `dingtalk`: 钉钉
- `wechat`: 微信
- 其中 `dingtalk` / `wechat` 会根据 `isSingle` 额外展示“个人 / 机器人”

## 重发逻辑

- 只有 `row.result !== 'success'` 时才显示“重新发送”
- 调用 `AlarmApi.resendNotice({ id: row.id })`
- 成功后直接把当前行状态改为成功，不重新整表刷新

## 刷新机制

- 监听页面级 `GlobalRefresh`
- 监听全局时间 `globalTimeV2`

## 依赖组件

- `search-group`
- `db-table`
