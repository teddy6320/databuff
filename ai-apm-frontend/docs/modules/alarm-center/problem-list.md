# 问题列表

## 页面信息

| 属性 | 值 |
|------|-----|
| 文件 | `src/views/alarmCenter/rootCause/index.vue` |
| 路由 path | `/alarmCenter/rootCause` |
| 列表 API | `RootCauseApi.getProblemList` |
| 筛选项 API | `RootCauseApi.getProblemQueryParams` |
| 趋势 API | `RootCauseApi.getInfluenceTrend` |

## 页面结构

### 顶部区域

- `search-group`: 文本与筛选项查询
- 图表开关按钮: 控制顶部问题趋势图显示

### 内容区域

- `choose-collapse`: 快捷筛选
- `basic-chart`: 问题趋势图
- `db-table`: 问题列表

## 表格列

| 字段 | 说明 |
|------|------|
| `problemShowId` | 问题 ID |
| `problemDesc` | 问题描述 |
| `problemCauseType` | 问题类型 |
| `problemService` | 问题节点 |
| `problemTimeRange` | 时间范围 |
| `problemStartTime` | 开始时间 |
| `beginToActionTime` | 确认时间 |
| `problemEndTime` | 结束时间 |
| `problemRepair` | 修复时间（MTTR） |
| `problemAction` | 响应时间（MTTA） |

页面会在本地把:

- `problemDesc` 截断到 `1500` 字符
- `problemTimeRange` 格式化为 `开始 ~ 结束`
- `problemRepair` / `problemAction` 计算为分钟数

## 权限判断

页面通过 `noExpireLimit` 判断是否允许访问，逻辑依赖:

- `this.$store.state.finalStatus`
- `this.$store.getters['User/isExpireLimit']`

若授权不可用，则页面只展示“未授权或授权已过期”的提示，不会发起列表查询。

## 筛选数据

筛选项由 `RootCauseApi.getProblemQueryParams` 返回，并分别提供给:

- `search-group`
- `choose-collapse`

## 跳转行为

- 行点击或问题 ID 点击: `/alarmCenter/problemDetail?id=...`

## 刷新机制

- 监听页面 `GlobalRefresh`
- 监听全局时间 `globalTimeV2`
- 搜索与快捷筛选变化时，同时刷新图表与列表

## 依赖组件

- `search-group`
- `choose-collapse`
- `db-table`
- `basic-chart`
