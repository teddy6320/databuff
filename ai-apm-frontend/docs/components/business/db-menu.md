# DbMenu

## 组件说明

`DbMenu` 是项目左侧菜单的统一封装，源码在 [src/components/db-menu/index.vue](/src/components/db-menu/index.vue)。它基于 `el-menu` 和 `simplebar-vue`，负责把 Vuex 里的菜单树渲染成平台导航。

## 主要输入

| Prop | 说明 |
|------|------|
| `collapse` | 是否折叠菜单 |
| `menuTheme` | 菜单主题，`dark` 或 `light` |
| `parentPath` | 只展示某个父路由下的子菜单 |

## 数据来源

组件不直接接收菜单数据，而是依赖 Vuex：

- `User.menusTree`
- `User.currMenu`
- 根 store 的 `breadcrumbList`
- `globalTime` getter

## 核心行为

- 自动过滤 `isMenu !== true` 的路由项
- 自动根据当前菜单高亮激活项和展开组
- 点击菜单时自动带上时间 query
- 会给菜单跳转追加 `__ps=m`，表示来源于左侧菜单

## 特殊行为

- 如果传入 `parentPath`，组件只渲染该节点下的菜单树
- 一级菜单会保留 icon，非一级菜单会清空 icon
- 菜单带 `time` 权限时，会自动把全局时间写到目标路由 query

## 典型使用

当前主要使用点有：

- [src/views/layout/db-header.vue](/src/views/layout/db-header.vue)
- [src/views/configManage/index.vue](/src/views/configManage/index.vue)

## 使用建议

- 平台级菜单优先复用 `DbMenu`
- 如果只是页面内部的小范围页签/分组切换，优先使用 [db-tabnav](db-tabnav.md)
