# 业务系统

> 页面: `/appMonitor/businessSystem`
> 文件: `src/views/appMonitor/businessSystem/index.vue`

## 页面职责

业务系统页用于管理业务系统 / 子系统，并查看当前选中业务系统的主视图内容。

## 页面结构

- 左侧 `business-side`: 业务系统树
- 右侧 `business-main`: 业务系统主内容
- `add-bs-dialog`: 新增/编辑业务系统弹窗

## 主要接口

- `BsApi.getBsTree`
- `BsApi.addBs`
- `BsApi.updateBs`
- `BsApi.deleteBs`
- 主内容和侧边组件继续联动 `bs.ts` 中业务统计、空间地图、规则等接口

详细接口见:

- [Business System API](../../api/bs.md)

## 关键参数

- `bsid`: 当前业务系统 ID
- `bsn`: 当前业务系统名称
- `type`: 业务系统类型

## 特点

- 左侧选择会同步更新路由 query
- 支持主系统 / 子系统的新增、编辑和删除
