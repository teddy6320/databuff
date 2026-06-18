# common

## 文件定位

[src/utils/common.ts](/src/utils/common.ts) 是项目的杂项通用工具集合，里面既有浏览器交互小工具，也有 Promise 包装、事件总线和文本测量能力。

## 主要导出

| 导出 | 说明 |
|------|------|
| `copy` | 复制文本并弹 Element Notification |
| `debounce` | 防抖函数 |
| `resetScreenSize` / `removeResetScreen` | 按设计稿缩放页面 |
| `StringIsEmpty` | 判空工具 |
| `MinNumZore` | 负数归零 |
| `toAsyncWait` | Promise 结果包装器 |
| `EventBus` | 全局事件总线 |
| `getTextWidth` | 文本宽度测量 |
| `getA4Size` | 按 A4 比例计算宽高 |
| `waitForSomeSecond` | 延时 Promise |

## 重点函数

### `toAsyncWait`

这是项目里使用频率最高的工具之一，返回结构固定为：

- `{ error: null, result }`
- `{ error, result: null }`

默认会按 Databuff API 规范判断：

- `status === 200`
- `message.toLowerCase() === 'success'`

如果不是标准后端接口，可以传 `dbApi = false`。

### `copy`

- 通过临时 `textarea` 执行复制
- 成功后统一弹“已复制！”通知

### `EventBus`

- 基于 `new Vue()` 实现
- 项目里常用于全局刷新等轻量广播

## 注意事项

- `resetScreenSize` 会直接覆盖 `window.onresize`，属于比较强的全局副作用。
- `MinNumZore` 这个命名保持源码现状，文档里不做重命名。
