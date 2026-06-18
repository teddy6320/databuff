# Nginx Tracing 说明

## 页面状态

代码中存在 [src/views/help/nginxTracing/index.vue](/src/views/help/nginxTracing/index.vue)，但在 [src/router/route-data.ts](/src/router/route-data.ts#L1618) 附近对应菜单与路由已被注释，因此当前版本用户界面不可达。

## 页面内容

如果未来重新开放，该页将用于说明：

| 区块 | 说明 |
|------|------|
| 兼容范围 | 支持的 nginx 版本与 Linux 环境要求 |
| 编译检查 | 检查 `nginx -V` 与 `--with-compat` |
| 插件下载 | 从平台下载不同版本的 nginx tracing 插件 |
| 配置说明 | `df-config.json` 与 `nginx.conf` 示例 |
| 重启命令 | 重新加载 nginx |

## 主要行为

| 行为 | 说明 |
|------|------|
| 插件下载 | 直接请求 `/webapi/configManage/download/nginxPlugin/...` 下载二进制包 |
| 错误处理 | 会先尝试把返回体解析为 JSON，识别失败消息，再决定是否真正执行下载 |

## 注意事项

- 该页目前更适合作为“代码中存在但未开放”的保留说明，不应在总导航里当作已开放功能描述。
