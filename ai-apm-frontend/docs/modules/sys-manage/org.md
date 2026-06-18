# 组织管理

> 页面: `/sysManage/org`
> 文件: `src/views/sysManage/org/index.vue`

## 页面职责

组织管理页用于维护组织树、成员归属及组织权限控制开关。

## 页面结构

- 顶部搜索框: 按组织名称过滤
- 设置弹层: 组织权限控制开关
- `db-table`: 组织列表
- `dialog-org`: 新建 / 编辑组织
- `dialog-config`: 成员管理

## 主要接口

- `UserApi.getOrgList`
- `UserApi.getOrgStatus`
- `UserApi.getAllAccountWithoutOrg`
- 组织增删改与成员配置由 `user.ts` 相关接口提供

详细接口见:

- [User API](../../api/user.md)

## 关键参数

- 页面读取 `name` 作为组织名称搜索关键词

## 注意事项

- 页面会根据当前用户可管理组织列表标记 `managable`
- 组织权限控制关闭后，不影响组织与用户的关联关系，只影响“用户管理范围”的约束逻辑
