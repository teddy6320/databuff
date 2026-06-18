# OneAgent 安装

> 页面: `/config/install?type=agent`
> 文件: `src/views/configInstall/agent/index.vue`

## 页面职责

OneAgent 页用于按操作系统展示安装命令和安装包信息，是整套部署配置的基础入口。

## 页面结构

- 顶部系统选择卡片:
  - `centos`
  - `windows`
  - `ubuntu`
  - `kylin`
  - `uos`
  - `kubernetes`
- 下方根据系统切换对应安装说明组件

## 主要接口

- `UserApi.getDownloadVersion`
- `UserApi.getK8sDownloadVersion`
- `AgentApi.getDcSite`

详细接口见:

- [User API](../../api/user.md)
- [Agent API](../../api/agent.md)

## 关键参数

- `type=agent`: 由安装部署模块根页维护
- `sys`: 当前操作系统/环境类型，如 `centos`、`windows`、`kubernetes`

## 关联页面

- 已安装列表: `/config/status?type=agent`
- APM 安装页会引用这里作为“先安装 OneAgent”的前置步骤

## 注意事项

- 页面初始化时会主动拉取普通安装包版本、K8s 安装包版本和 `dcSite`
- 当前页面还会触发 `Common/GET_ENV_TAGS`，说明安装命令可能依赖环境标签上下文
