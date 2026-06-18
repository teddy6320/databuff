# 日志采集

> 页面: `/config/install?type=log`
> 文件: `src/views/configInstall/log/index.vue`

## 页面职责

日志页提供 OneAgent 日志采集与日志关联 Trace 的静态配置说明，偏“配置手册”而不是交互页面。

## 页面结构

- OneAgent 基础日志采集配置 `databuff.yaml`
- `Java.d` 日志采集配置
- 中间件日志采集配置
- 日志关联 Trace 功能配置
  - `log4j`
  - `log4j2`
  - `logback`

## 主要内容

- `logs_enabled` 基础开关
- 中间件 `conf.d/*.yaml` 配置示例
- MySQL 日志采集示例
- 在日志格式中注入 `trace_id` / `span_id` / `service`

## 关键参数

- 当前页面不依赖额外路由参数
- 页面内部通过 `typeModel` 和 `traceTypeModel` 切换展示不同代码片段

## 注意事项

- 这是静态说明页，没有直接调用后端接口
- 日志关联 Trace 的前置条件是对应 Java 进程已经开启 Trace 和日志采集
