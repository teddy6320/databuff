# NPM 安装

> 页面: `/config/install?type=npm`
> 文件: `src/views/configInstall/npm/index.vue`

## 页面职责

NPM 页用于生成主机环境和 Kubernetes 环境下的 NPM 安装命令。

## 页面结构

- 主机环境命令
- Kubernetes 环境命令
- 两段命令都通过 `code-view` 展示

## 主要接口

- `UserApi.getK8sDownloadVersion`
- `AgentApi.getDcSite`

详细接口见:

- [User API](../../api/user.md)
- [Agent API](../../api/agent.md)

## 关键行为

- 页面会拼接当前站点、CID、Token、K8s 安装包版本和 `dcSite`
- 主机与 K8s 使用不同的安装脚本地址

## 注意事项

- 页面中的安装命令会直接带当前登录态相关参数
- 该页属于安装部署模块的正式 tab，但旧文档规划里没有单列，本轮已按代码现状补齐
