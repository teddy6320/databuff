# 系统拓扑

> 页面: `/appMonitor/systemTopology`
> 文件: `src/views/appMonitor/systemTopology/index.vue`

## 页面职责

系统拓扑页从业务系统和服务两个层级展示全局拓扑关系，支持搜索、数据来源过滤和自定义布局。

## 页面结构

- 拓扑画布 `#systemChart`
- 数据来源筛选
- 节点级联搜索
- 名称显隐切换
- 自定义布局入口
- 右侧 `aside-detail`

## 主要接口

- `BsApi.getSystemMap`
- `BsApi.getSystemMapEdge`
- `BsApi.getLayout`
- `BsApi.setLayout`
- `BsApi.getRelationStatus`

详细接口见:

- [Business System API](../../api/bs.md)

## 关键限制

- 受授权状态控制，未授权或已过期时不展示图
- 支持保存并读取拓扑布局

## 关联页面

- 自定义布局页: `/appMonitor/systemTopology/manage`
