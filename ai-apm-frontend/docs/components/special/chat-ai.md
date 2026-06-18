# Chat AI

## 组件说明

`ChatAI` 是一个 AI 根因分析抽屉组件，源码在 [src/components/chat-ai/index.vue](/src/components/chat-ai/index.vue)。当前它主要服务于告警模块，通过对话流的方式展示大模型分析结果。

## 输入参数

| Prop | 说明 |
|------|------|
| `params` | 告警上下文，至少包含 `service / fromTime / toTime / level / description` |

## 对外调用方式

这个组件不是单纯依赖 props 驱动，当前更常见的使用方式是：

- 页面通过 `ref` 获取实例
- 直接调用 `showHandle(retry?)`

当前典型使用点见 [src/views/alarmCenter/alarm/index.vue](/src/views/alarmCenter/alarm/index.vue)。

## 核心能力

- 使用 `el-drawer` 承载对话内容
- 调用 `AlarmApi.startAiRootAnalyse` 发起分析
- 轮询 `AlarmApi.getAiResult` 获取增量结果
- 用 `Typed.js` 做逐字动画
- 用 [Marked View](../display/marked-view.md) 渲染回答内容

## 特殊行为

- 组件内部维护“已完成对话 / 动画中对话 / 排队中对话”三段状态
- 会根据用户是否主动上滚，决定是否自动滚到底部
- `cancelHandle()` 会清空所有内部状态和定时器
- 当前 UI 上有“思考过程”开关，但源码里是禁用状态

## 使用建议

- 适合封装“异步流式结果 + 对话展示”的场景
- 如果后续要复用到其他模块，建议先把告警专属字段和 API 调用进一步解耦
