# browser

## 文件定位

[src/utils/browserVersion.ts](/src/utils/browserVersion.ts) 用于识别当前浏览器类型和主版本号，并判断是否低于要求版本。

## 主要函数

| 函数 | 说明 |
|------|------|
| `isUpgradeBrowser(browserList)` | 判断当前浏览器是否需要升级 |

内部还包含两个辅助函数：

- `getBrowserVersion`
- `getBrowser`

## 支持识别的浏览器

- `Chrome`
- `Edge`
- `Firefox`
- `Opera`
- `Safari`

## 返回结构

`isUpgradeBrowser()` 返回：

- `browser`
- `version`
- `lowestVersion`（仅需要升级时存在）
- `isUpdate`

## 注意事项

- 当前 `Edge` 的判断基于旧的 `edge/` UA 片段，对 Chromium 新版 Edge 的兼容性要按现有实现理解。
- 登录页底部“建议使用 Chrome 浏览器（v84.0及以上版本）”属于页面静态提示，不一定直接调用这里的工具。
