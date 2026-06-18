# Notice API

> 文件: `src/api/notice.ts`
> 类型: `src/api/notice.type.ts`

## 概述

`notice.ts` 负责系统级通知通道配置，覆盖邮件、短信、钉钉、企业微信、Webhook、Socket 等发送方式。大部分能力都围绕三类操作展开:

- 获取配置
- 保存配置
- 发送测试消息

## 接口分组

### 邮件通知

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getEmailConfig` | `GET` | `/notify/getEmailConfig` | 获取邮件配置 |
| `setEmailConfig` | `POST` | `/notify/setEmailConfig` | 保存邮件配置 |
| `testEmail` | `POST` | `/notify/testEmail` | 测试邮件发送 |

### 短信通知

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getSmsConfig` | `GET` | `/notify/getSmsConfig` | 获取短信配置 |
| `setSmsConfig` | `POST` | `/notify/setSmsConfig` | 保存短信配置 |
| `testSms` | `POST` | `/notify/testSms` | 测试短信发送 |

### 钉钉通知

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getDingTalkConfig` | `GET` | `/notify/getDingTalkConfig` | 获取钉钉配置 |
| `setDingTalkConfig` | `POST` | `/notify/setDingTalkConfig` | 保存钉钉配置 |
| `testDingTalk` | `POST` | `/notify/testDingTalk` | 测试钉钉机器人发送 |
| `testDingTalkByPhone` | `POST` | `/notify/testDingTalkByPhone` | 按手机号测试钉钉通知 |

### 企业微信通知

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getWeChatConfig` | `GET` | `/notify/getWeChatConfig` | 获取企业微信配置 |
| `setWeChatConfig` | `POST` | `/notify/setWeChatConfig` | 保存企业微信配置 |
| `testWeChat` | `POST` | `/notify/testWeChat` | 测试企业微信通知 |
| `testWechatByPhone` | `POST` | `/notify/testWeChatByPhone` | 按手机号测试企业微信通知 |

### Webhook 与 Socket

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `testCustomWebhook` | `POST` | `/notify/testCustomWebhook` | 测试自定义 Webhook |
| `getSocketConfig` | `GET` | `/notify/getSocketConfig` | 获取 Socket 配置 |
| `setSocketConfig` | `POST` | `/notify/setSocketConfig` | 保存 Socket 配置 |
| `testSocket` | `POST` | `/notify/testSocket` | 测试 Socket 通知 |

## 相关类型

通知配置类型定义位于 `src/api/notice.type.ts`，当前主要包括:

- `EmailConfig`
- `TestEmail`
- `SmsConfig`
- `TestSms`
- `DingTalkConfig`
- `WeChatConfig`
- `CustomWebhookConfig`

## 使用特点

- 同一种通知方式通常遵循 `getXxxConfig`、`setXxxConfig`、`testXxx` 的命名模式
- 钉钉和企业微信额外提供按手机号测试的方法
- `testCustomWebhook` 只有测试接口，没有对应的查询/保存接口
