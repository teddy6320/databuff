# 角色权限

> 页面: `/sysManage/role`
> 文件: `src/views/sysManage/role/index.vue`

## 页面职责

角色页用于维护角色树、功能权限、管理域绑定和环境标签绑定，是平台权限体系的核心入口。

## 页面结构

- `db-query`: 角色名称、定义类型等筛选
- `db-table`: 角色树
- 角色弹窗: 新建 / 编辑
- 权限弹窗: 功能权限树
- 管理域弹窗: 绑定可见与可配置的管理域
- 环境弹窗: 绑定环境标签

## 主要接口

- `SystemApi.getRoleList`
- `SystemApi.getPermis`
- `SystemApi.getAccountListByRole`
- `GroupApi.getGroupStatus`
- `GroupApi.getGroupList`
- `GroupApi.getGroupListByRole`
- `EnvTagApi.*` 由 `env-dialog` 使用

详细接口见:

- [System API](../../api/system.md)
- [Group API](../../api/group.md)
- [Env Tag API](../../api/env-tag.md)

## 关键参数

- 页面会读取 `type=create`，并直接打开“新建角色”弹窗
- 当环境标签功能开启时，列表会额外显示“环境”列

## 注意事项

- 管理域启用状态来自 `GroupApi.getGroupStatus`
- 环境标签绑定对父子角色有依赖，子角色不能超出父角色已绑定的环境范围
