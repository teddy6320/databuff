# 授权模块

## 模块概览

授权模块对应 `/authorization`，用于处理平台未授权、已过期、需要重新授权，以及“已导入 License 但未完成授权”的场景。页面文件是 [src/views/authorization/index.vue](/src/views/authorization/index.vue)。

## 状态模型

页面核心依赖全局 `finalStatus`：

| 状态值 | 含义 |
|------|------|
| `0` | 无需处理，正常进入系统 |
| `1` | 平台未授权 |
| `2` | License 已过期 |
| `3` | 平台需要重新授权 |
| `4` | 已导入 License，但还未完成超管初始化 |

## 页面结构

| 区域 | 说明 |
|------|------|
| 顶部横幅 | 展示产品英文名背景图 |
| 状态提示区 | 根据 `finalStatus` 展示未授权、过期、重新授权提示 |
| 授权步骤区 | `1/4` 状态下展示“两步式”流程：授权 -> 设置超管 |
| 上传区 | 上传 `.lic` 授权文件 |
| 超管创建区 | 为 `Admin` 设置初始密码 |
| 退出登录 | 已登录且非初始化状态时可退出 |

## 主要接口

| 接口 | 说明 |
|------|------|
| `getLicenseSerialnum` | 获取 License 序列号 |
| `createAdminAduit` | 创建超管账户 |
| `logoutHandle` | 退出登录 |

## 特殊上传流程

- License 导入不走 `src/api` 封装，而是直接通过 `el-upload` 提交到 `/webapi/user/lisupload`。
- 上传前会限制文件大小不超过 `50kb`，且仅允许 `.lic` 文件。

## 页面流转

1. 路由守卫在发现 `finalStatus` 为 `1` 或 `4` 时，会直接跳到 `/authorization`。
2. 未授权时，用户先扫码申请授权文件，再上传 License。
3. `finalStatus === 1` 上传成功后，会进入“设置超管用户”步骤。
4. `finalStatus === 4` 直接进入超管创建步骤。
5. 过期或重新授权场景下，Admin 用户重新上传 License 后，页面会刷新并回到登录链路。

## 权限差异

| 场景 | 行为 |
|------|------|
| 普通用户 + 已过期 | 只能看到过期提示，无法重新授权 |
| 普通用户 + 重新授权 | 只能看到“请登录 Admin 用户重新授权” |
| Admin 用户 | 可上传 License，并在需要时创建超管 |

## 注意事项

- 授权页内部不会主动持久化跳转到 `/login`，很多成功场景直接使用 `window.location.reload()` 重新触发整套鉴权流程。
- 退出登录时会附带 `__redirect=false`，避免重新落回带 redirect 的登录页。
