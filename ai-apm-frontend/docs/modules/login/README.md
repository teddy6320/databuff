# 登录模块

## 模块概览

登录模块对应 `/login`，由 [src/views/login/index.vue](/src/views/login/index.vue) 和 [src/views/login/loginForm.vue](/src/views/login/loginForm.vue) 组成。页面负责展示品牌 Logo、产品名、版本信息、浏览器提示，并完成用户名/密码/图片验证码登录。

## 页面结构

| 区域 | 说明 |
|------|------|
| 品牌区 | 显示登录 Logo 与产品中文名，来自 `User/getLogoConfig` |
| 表单区 | 用户名、密码、图片验证码、登录按钮 |
| 扩展区 | 官方域名下显示“免费注册”入口 |
| 页脚区 | 版本号、浏览器建议、版权信息 |

## 关键参数

| 参数 | 说明 |
|------|------|
| `next` | 登录成功后回跳地址，来自路由守卫 |

## 主要接口

| 接口 | 说明 |
|------|------|
| `getVerificationCode` | 获取图片验证码 |
| `loginHandle` | 提交登录 |

## 登录流程

1. 页面创建时调用 `getVerificationCode` 拉取验证码图片和 `img_uuID`。
2. 提交前会先做表单校验，并把密码转成 MD5。
3. 登录成功后保存用户基础信息、DS 信息和 token/cid。
4. 如果路由携带 `next`，登录后跳回该地址；否则跳转 `/`。
5. 如果后端返回 `3003`，说明平台进入授权流程，会直接跳到 `/authorization`。

## 与路由守卫的关系

- 未登录访问业务页时，`src/router/index.ts` 会跳回 `/login`，并带上 `next`。
- 已登录用户再访问 `/login` 时，会被守卫重定向到 `/`。
- `/singleLogin/:type` 是单点登录中转页，不属于登录模块本身，但登录态建立后会走相同的主页跳转逻辑。

## 注意事项

- 登录页会清理 `DATABUFF_ADVANCED_UNLOCKING` 本地状态，避免高级配置解锁状态泄漏到新会话。
- 验证码输入框会在前端做一次本地校验，错误时立即刷新验证码。
- 官方 SaaS 域名下才会显示“免费注册”链接。
