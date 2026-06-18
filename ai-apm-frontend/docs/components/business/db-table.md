# DbTable

## 组件说明

`DbTable` 是项目里最核心的通用表格组件，源码在 [src/components/db-table/db-table.vue](/src/components/db-table/db-table.vue)。它基于 `el-table` 做了统一封装，补充了远程查询、全局时间联动、滚动加载、列展示持久化、进度条渲染和拖拽排序等能力。

## 典型用法

| 模式 | 说明 |
|------|------|
| 纯展示模式 | 传 `data`，不传 `queryApi` |
| 远程查询模式 | 传 `queryApi + queryParams`，通过 `refresh()` 拉数 |
| 滚动加载模式 | 打开 `scrollMode`，依赖父容器高度自动计算 |
| 列配置模式 | 打开 `showSetting`，结合 `tableKey` 持久化列显隐 |

## 常用 Props

| Prop | 说明 |
|------|------|
| `data` | 静态表格数据 |
| `queryApi` | 远程查询函数 |
| `queryParams` | 远程查询额外参数 |
| `columnConfig` | 列定义数组 |
| `tableKey` | 本地存储列配置 key |
| `showSetting` | 是否显示列设置按钮 |
| `showTotal` | 是否显示顶部总数 |
| `timeMode` | 是否自动注入全局 `fromTime/toTime` |
| `autoRefresh` | 是否跟随全局时间变化自动刷新 |
| `offsetMode` | 是否使用 `offset/size` 分页 |
| `showSelection` | 是否显示多选列 |
| `selectableFunc` | 控制某一行是否可选 |
| `formatFunc` | 请求后对结果进行二次格式化 |
| `tableSortable` | 是否允许拖拽排序 |

## `columnConfig` 约定

| 字段 | 说明 |
|------|------|
| `field` / `prop` | 数据字段，`prop` 默认回退到 `field` |
| `label` | 列标题 |
| `type` | 自定义渲染类型，如 `default`、`service`、`progress`、`alarmLevel`、`alarmStatus`、`healthBar`、`healthStatus` |
| `unit` | 值格式化单位，如 `ns`、`ms`、`s`、`b`、`percent`、`time` |
| `slot` | 自定义插槽渲染 |
| `handleClick` | 单元格点击事件 |
| `defaultShow` | 列设置里的默认展示状态 |
| `disabled` | 在列设置里禁止隐藏 |
| `defaultSort` | 默认排序方向，支持 `asc` / `desc` |
| `headerDescribe` | 表头 tooltip 提示 |

## 暴露方法

通过 `ref` 可以调用：

| 方法 | 说明 |
|------|------|
| `refresh()` | 重置分页并重新请求 |
| `clear()` | 清空数据和内部分页状态 |
| `clearSelection()` | 清空选中 |
| `toggleRowSelection()` | 切换行选中状态 |
| `doLayout()` | 触发表格重排 |
| `sort()` / `clearSort()` | 外部控制排序 |

## 关键事件

| 事件 | 说明 |
|------|------|
| `on-table-inited` | 内部滚动容器初始化完成 |
| `on-fetch-end` | 一次拉数结束，返回列表、总数和最终请求参数 |
| `sort-change` | 排序变化 |
| `selection-change` | 选中变化 |
| `row-click` | 行点击 |
| `on-columns-inited` | 列初始化完成 |
| `on-columns-change` | 列显示配置变化 |
| `drag` / `drop` | 拖拽排序开始与结束 |

## 特殊行为

- 开启 `timeMode` 后，会自动把全局时间格式化成 `YYYY-MM-DD HH:mm:ss` 注入请求参数。
- 接口返回 `{ data: { list, total } }` 或 `{ data: { data, total } }` 时，组件会自动兼容提取。
- `showSetting + tableKey` 会把列显隐状态写到 `localStorage`。
- 进度条列会根据当前表格数据自动计算百分比并写入 `row.progressValue`。

## 使用建议

- 只要是“远程表格 + 分页/滚动加载”场景，优先复用 `DbTable`。
- 如果页面需要手动控制请求时机，通常把 `autoRefresh` 关掉，再通过 `ref.refresh()` 驱动。
