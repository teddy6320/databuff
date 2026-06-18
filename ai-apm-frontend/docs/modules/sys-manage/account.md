# 账户管理

> 页面: `/sysManage/account`
> 文件: `src/views/sysManage/account/index.vue`

## 页面职责

账户管理页用于维护平台用户、重置密码、强制下线、解锁账号，并处理用户级通知绑定。

## 页面结构

- `search-group`: 用户名、角色、组织等筛选
- `db-table`: 账号列表
- 用户弹窗: 新建 / 编辑 / 重置密码
- `ding-dialog` / `weex-dialog`: 绑定钉钉、企业微信

## 主要接口

- `SystemApi.getAccountList`
- `SystemApi.getRoleList`
- `UserApi.getOrgOptions`
- `UserApi.getOrgStatus`
- `NoticeApi.getDingTalkConfig`
- `NoticeApi.getWeChatConfig`
- 账户增删改与强制下线、解锁能力继续由 `system.ts` 提供

详细接口见:

- [System API](../../api/system.md)
- [User API](../../api/user.md)
- [Notice API](../../api/notice.md)

## 关键参数

- 页面会读取 `type`，当值为 `create` 时直接弹出“新建用户”对话框
- 列表主查询由 `search-group` 返回的 query 参数驱动

## 注意事项

- 页面同时受组织权限控制与通知通道配置状态影响
- 钉钉/企业微信的用户绑定依赖对应系统级通知配置已正确启用
