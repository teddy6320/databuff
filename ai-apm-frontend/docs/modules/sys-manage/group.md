# 管理域

> 页面: `/sysManage/group`
> 文件: `src/views/sysManage/group/index.vue`

## 页面职责

管理域页用于维护管理域列表、自动业务系统归域状态，以及未分配实体的二次归域。

## 页面结构

- 管理域主页:
  - 顶部统计未分配实体数量
  - `table-list`: 管理域列表、实体管理、新建与删除
- 未分配实体页:
  - `db-radio`: 主机 / 业务系统 / 服务 / Namespace
  - 搜索框
  - `db-table`: 未分配实体列表
  - `bind-dialog` / `create-dialog`: 分配到已有或新建管理域

## 主要接口

- `GroupApi.getGroupList`
- `GroupApi.deleteGroup`
- `GroupApi.setCustomGroup`
- `GroupApi.getUngroupEntity`
- `GroupApi.getAutoBsStatus`
- `GroupApi.getUngroupList`
- 绑定与创建弹窗继续使用 `group.ts` 中的相关接口

详细接口见:

- [Group API](../../api/group.md)

## 关键参数

- 管理域主页读取 `name` 作为管理域名称搜索词
- 未分配实体页:
  - `type`: `host` / `business` / `service` / `namespace`
  - `query`: 关键字搜索

## 典型导航关系

- 管理域主页 -> 未分配实体: `/sysManage/group/entity`
- 未分配实体 -> 服务详情: `/appMonitor/serviceDetail?sid=...`
- 未分配实体 -> 业务系统: `/appMonitor/businessSystem?bsid=...`
- 未分配实体 -> 主机详情: `/infrastructure/hostDetail?hostName=...`
- 未分配实体 -> Namespace 列表: `/infrastructure/namespace?...`

## 注意事项

- 自动同步生成的管理域 `dataSource=1`，默认不可直接编辑，需先转为自定义
- 删除当前正在使用的管理域后，页面会触发重新选择逻辑
