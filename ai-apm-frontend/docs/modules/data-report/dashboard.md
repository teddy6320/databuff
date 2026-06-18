# 仪表盘

> 页面: `/dashboard`
> 文件: `src/views/dataReport/dashboard/index.vue`

## 页面职责

仪表盘页用于把外部 Dashboard 通过 `iframe` 嵌入到当前系统中，并根据 URL 参数定位具体看板。

## 页面结构

- 单一 `iframe` 容器
- 页面创建时动态拼接最终 `dashUrl`

## 主要接口

- `PluginApi.getDashboardUID`

详细接口见:

- [Plugin API](../../api/plugin.md)

## 关键参数

- `dbnm`: Dashboard 名称
- `fid`: 文件夹 ID
- `dbRange`: 时间范围毫秒值

## 关键行为

- 页面会从 Cookie 里读取 `dsi`、`oi`、`dba`
- 当同时带 `dbnm` 和 `fid` 时，会先查 Dashboard UID，再生成 `d/{uid}` 形式的目标地址
- 当带 `dbRange` 时，会把时间范围换算成 `from=now-...&to=now`

## 注意事项

- 页面不是本地渲染的图表，而是外部 Dashboard 的嵌入壳层
- 当前代码里菜单权限参数逻辑被注释掉，实际只传 `dbvn`、Cookie 相关信息和时间范围
