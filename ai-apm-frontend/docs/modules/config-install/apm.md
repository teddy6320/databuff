# APM 配置

> 页面: `/config/install?type=apm`
> 文件: `src/views/configInstall/apm/index.vue`

## 页面职责

APM 页用于按部署环境和语言给出应用接入指引，既包含主机环境，也包含容器环境。

## 页面结构

- 环境选择:
  - `host`
  - `container`
- 语言/容器选择:
  - 主机环境下可授权的语言
  - 容器环境下当前只有 `kubernetes`
- Java 场景额外支持:
  - `auto`
  - `manual`

## 主要接口

- `UserApi.getDownloadVersion`
- `UserApi.getAuthLangs`

详细接口见:

- [User API](../../api/user.md)

## 关键参数

- `type=apm`
- `env`: `host` / `container`
- `lang`: 语言或容器类型，如 `java`、`python`、`kubernetes`

## 典型流程

- 第一步跳转到 OneAgent 安装页完成基础安装
- 第二步按环境和语言选择对应接入说明
- Java 自动注入模式会引导到:
  - 部署状态页开启自动注入
  - 配置管理里的服务监控高级配置

## 注意事项

- 可选语言会受 `getAuthLangs` 返回的授权语言列表控制
- 代码里保留了 `ruby` 入口位，但当前未授权也没有对应组件
