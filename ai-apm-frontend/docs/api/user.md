# User API

> 文件: `src/api/user.ts`

## 概述

`user.ts` 既包含登录、验证码、用户信息等基础认证接口，也包含组织管理、授权语言、客户端下载版本与单点登录能力。

## 导出方式

这个文件同时存在:

- 命名导出: `getVerificationCode`、`loginHandle`、`getLicenseInfo`、`logoutHandle`
- 默认导出对象: 聚合全部用户域接口

## 认证与用户基础信息

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getVerificationCode` | `POST` | `/user/imgcapt` | 获取验证码 |
| `loginHandle` | `POST` | `/user/login` | 登录 |
| `logoutHandle` | `POST` | `/user/loginOut` | 登出 |
| `getMenus` | `POST` | `/user/getMenuByAccount` | 获取菜单权限 |
| `getProductVersion` | `GET` | `/user/product/version` | 获取产品版本 |
| `getUserInfo` | `GET` | `/user/getUserInfo` | 获取当前用户信息 |
| `updatePwdInfo` | `POST` | `/user/updateUserPass` | 修改当前用户密码 |

## License 与安装下载

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `getLicenseInfo` | `POST` | `/user/licenseList` | 获取 license 信息 |
| `getLicenseSerialnum` | `GET` | `/user/getLicenseSerialnum` | 获取 license 序列号 |
| `getDownloadVersion` | `GET` | `/api6972/getVersions` | Agent 下载版本 |
| `getK8sDownloadVersion` | `GET` | `/api6972/getK8sVersions` | K8s Agent 下载版本 |
| `getAuthLangs` | `GET` | `/user/getAuthLangs` | 授权语言列表 |

## 组织管理

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `addOrg` | `POST` | `/organize/add` | 创建组织 |
| `editOrg` | `POST` | `/organize/edit` | 编辑组织 |
| `getOrgList` | `GET` | `/organize/list` | 组织列表 |
| `deleteOrg` | `POST` | `/organize/delete` | 删除组织 |
| `getMemberList` | `POST` | `/organize/listMember` | 组织成员列表 |
| `joinUser` | `POST` | `/organize/joinUser` | 用户加入组织 |
| `removeUser` | `POST` | `/organize/removeUser` | 移除组织成员 |
| `addManager` | `GET` | `/organize/addManager` | 设置组织管理员 |
| `removeManager` | `GET` | `/organize/removeManager` | 取消组织管理员 |
| `getOrgOptions` | `GET` | `/organize/options` | 组织选项 |
| `toggleOrgAuth` | `POST` | `/meta/config/updateEnable` | 开启/关闭组织能力 |
| `getOrgStatus` | `GET` | `/meta/config/getByCode?code=ORG_MANAGE_AUTH_CONFIG` | 获取组织能力开关 |
| `getAllAccountWithoutOrg` | `POST` | `/user/getAllAccounts` | 获取未入组织账号 |

## 单点登录

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `singleAuthLogin` | `POST` | `/singleLogin/imc/authlogin` | IMC 单点登录 |

## 特殊处理

### `logoutHandle`

登出时会在 `transformResponse` 中顺带清理本地存储里的:

- `DATABUFF_ADVANCED_UNLOCKING`

