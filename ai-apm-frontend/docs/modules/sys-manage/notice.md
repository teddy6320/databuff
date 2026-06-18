# 通知配置

> 页面: `/sysManage/notice`
> 文件: `src/views/sysManage/notice/index.vue`

## 页面职责

通知配置页用于维护系统级邮件、短信、钉钉、企业微信和 Socket 通道配置，并提供测试通知能力。

## 页面结构

- `db-tabnav`: `email` / `sms` / `dingtalk` / `wechat` / `socket`
- 各页签子组件分别负责配置表单、启停开关与测试发送

## 主要接口

- `NoticeApi.getEmailConfig`
- `NoticeApi.getSmsConfig`
- `NoticeApi.getDingTalkConfig`
- `NoticeApi.getWeChatConfig`
- `NoticeApi.getSocketConfig`
- `NoticeApi.testEmail`
- `NoticeApi.testSms`
- `NoticeApi.testDingTalkByPhone`
- `NoticeApi.testWechatByPhone`
- `NoticeApi.testSocket`

详细接口见:

- [Notice API](../../api/notice.md)

## 关键参数

- 页签由 `type` 控制，取值与通道类型一致，如 `email`、`sms`、`dingtalk`

## 注意事项

- 钉钉、企业微信与 Socket 页签都区分“启用配置”和“测试通知”
- 账户管理页里的用户通知绑定依赖这里的系统级通道配置状态
