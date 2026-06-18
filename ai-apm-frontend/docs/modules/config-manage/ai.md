# AI 配置

> 页面: `/config/ai`
> 文件: `src/views/configManage/ai/index.vue`

## 页面职责

AI 配置页用于维护外接大模型的基础接入信息，包括开关、模型名称、调用地址和 API Key，并支持连通性测试。

## 页面结构

- 表单区: `open`、`model`、`url`、`apiKey`
- 底部操作区: `重置`、`测试`、`保存`

## 主要接口

- `ConfigApi.getAIConfig`
- `ConfigApi.updateAIConfig`
- `ConfigApi.testAIConfig`

详细接口见:

- [Config API](../../api/config.md)

## 关键行为

- 页面初始化时读取当前 AI 配置
- 点击“重置”会重新拉取服务端配置并覆盖本地表单
- 点击“测试”会直接调用测试接口，不先保存
- 保存成功后会同步更新 `User/SET_AI_ENABLED`

## 关键参数

- 当前页面不依赖额外路由 query
- 保存与测试都基于表单字段直接提交

## 注意事项

- 页面按钮受 `hasEntityManageAuth` 权限控制
- 表单校验要求 `model`、`url`、`apiKey` 均为必填
