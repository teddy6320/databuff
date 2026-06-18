# 基础设置

> 页面: `/sysManage/basic`
> 文件: `src/views/sysManage/basic/index.vue`

## 页面职责

基础设置页用于维护系统时间同步方式和页面超时时间。

## 页面结构

- 日期时间设置:
  - 自定义时间
  - 自动与 NTP 服务器同步
- 页面超时设置:
  - 会话超时秒数选择

## 主要接口

- `SystemApi.getsysdate`
- `SystemApi.getSystemBase`
- `SystemApi.setsysdate`
- `SystemApi.setNtpServer`
- `SystemApi.updatePageTimeOut`

详细接口见:

- [System API](../../api/system.md)

## 关键参数

- 当前页面不依赖额外路由参数

## 注意事项

- 自定义时间与 NTP 同步是互斥的
- 页面超时设置会直接影响整个平台的登录会话体验
