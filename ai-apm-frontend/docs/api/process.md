# Process API

> 文件: `src/api/process.ts`

## 概述

`process.ts` 聚焦进程规则管理，分成“进程采集规则”和“进程识别规则”两套接口，结构基本对称。

## 接口分组

### 进程采集规则

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getCollectRuleList` | `POST` | `/processCollectRules/list` | 采集规则列表 |
| `createCollectRule` | `POST` | `/processCollectRules/insert` | 新建采集规则 |
| `updateCollectRule` | `POST` | `/processCollectRules/edit` | 编辑采集规则 |
| `toggleCollectRuleEnable` | `POST` | `/processCollectRules/updateStatus` | 启停采集规则 |
| `deleteCollectRule` | `POST` | `/processCollectRules/delete` | 删除采集规则 |
| `setCollectAll` | `POST` | `/processCollectRules/collectAll` | 设置是否采集全部监控 |

### 进程识别规则

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getIdentifyRuleList` | `POST` | `/processIdentifyRules/list` | 识别规则列表 |
| `createIdentifyRule` | `POST` | `/processIdentifyRules/insert` | 新建识别规则 |
| `updateIdentifyRule` | `POST` | `/processIdentifyRules/edit` | 编辑识别规则 |
| `toggleIdentifyRuleEnable` | `POST` | `/processIdentifyRules/updateStatus` | 启停识别规则 |
| `deleteIdentifyRule` | `POST` | `/processIdentifyRules/delete` | 删除识别规则 |

## 使用特点

- 采集规则和识别规则的方法命名、路径结构高度一致
- 删除、启停、编辑均使用 `POST`
