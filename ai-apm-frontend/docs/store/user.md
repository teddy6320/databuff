# User Store

## 模块定位

`User` 模块源码在 [src/store/modules/user/index.ts](/src/store/modules/user/index.ts)，负责登录态后的核心上下文，包括用户信息、菜单权限、角色树、管理域选择、AI 开关和 Logo 配置。

## State

| 字段 | 说明 |
|------|------|
| `menus` | 扁平菜单列表 |
| `menusTree` | 左侧导航树 |
| `permitTree` | 角色权限树 |
| `currMenu` | 当前菜单 |
| `userInfo` | 当前用户信息 |
| `roleList` | 当前用户的角色/管理域树 |
| `dsInfo` | 登录后返回的 DS 信息 |
| `expireLimit` | License 过期后是否仍允许部分功能使用 |
| `currGroup` | 当前选中的管理域 |
| `prevGroup` | 隐藏管理域前的上一次选择 |
| `groupList` | 全量管理域列表 |
| `groupEnabled` | 管理域功能是否开启 |
| `aiEnabled` | AI 开关 |
| `logoConfig` | 平台 Logo 与品牌信息 |

## 关键 Actions

| Action | 说明 |
|------|------|
| `getMenus` | 获取用户菜单，过滤隐藏菜单并构建树 |
| `getUserInfo` | 获取当前用户资料 |
| `findRoleGroupByUser` | 获取当前用户角色树、管理域关系和默认选中组 |
| `getGroupEnabled` | 获取管理域开关 |
| `getAIEnabled` | 获取 AI 配置是否开启 |
| `getLogoConfig` | 获取品牌 Logo 配置，失败时回退到默认值 |

## 关键 Mutations

| Mutation | 说明 |
|------|------|
| `SET_USER_INFO` / `UPDATE_USER_INFO` | 设置或局部更新用户信息 |
| `SET_MENUS` / `SET_MENUS_TREE` | 设置菜单平铺数据和树数据 |
| `SET_PERMIT_TREE` | 设置权限树 |
| `SET_CURR_MENU` | 设置当前菜单 |
| `SET_DS_INFO` | 设置 DS 信息 |
| `SET_EXPIRE_LIMIT` | 设置过期限权状态 |
| `SET_CURRENT_GROUP` / `SET_PREV_GROUP` | 设置当前/上一次管理域 |
| `SET_GROUP_LIST` / `SET_GROUP_ENABLED` | 设置管理域列表和开关 |
| `SET_AI_ENABLED` | 设置 AI 开关 |
| `SET_LOGO_CONFIG` | 设置 Logo 配置 |

## 关键 Getters

| Getter | 说明 |
|------|------|
| `getMenus` / `getMenusTree` | 获取菜单 |
| `getUserInfo` | 获取当前用户 |
| `getUserRoleList` | 获取角色列表 |
| `getGroupEnabled` | 获取管理域开关 |
| `getCurrGroup` / `getPrevGroup` | 获取当前/上一次管理域 |
| `getGroupMapping` | 将管理域列表转成 `id -> name` 映射 |
| `getIsAdmin` | 是否 Administrator |
| `getHasAlarmManageAuth` | 当前管理域下是否有告警配置权限 |
| `getAIEnabled` | AI 开关 |
| `getLogoConfig` | 品牌配置 |

## 关键逻辑

### 菜单构建

- 先调用 `UserApi.getMenus()` 获取后端菜单。
- 再与 [src/router/route-data.ts](/src/router/route-data.ts) 做路径匹配。
- 对隐藏菜单、静态菜单和管理域菜单做额外过滤。
- 最后同时生成：
  - `menusTree`：左侧导航树
  - `menus`：扁平列表
  - `permitTree`：权限编辑树

### 管理域选择

- 当前选中的管理域会通过 `agi` cookie 持久化。
- `findRoleGroupByUser` 会综合：
  - 后端返回的角色-管理域关系
  - 本地上次选择的管理域
  - 管理域总表
  来恢复 `currGroup`。

### 品牌配置

- `getLogoConfig` 会先准备一套默认品牌配置。
- 如果接口返回空对象，仍然回退到默认配置，不让登录页/头部缺 logo。

## 注意事项

- 这个模块里有一批帮助函数，例如 `generMenuTree`、`getTreeItemById`、`getParentMenu`，它们决定了菜单树和静态页可见性。
- `expireLimit` 不是单纯的“是否过期”，而是“过期后是否仍有受限可用能力”，路由守卫会依赖它决定是否强制去授权页。
