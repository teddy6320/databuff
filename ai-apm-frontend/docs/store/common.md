# Common Store

## 模块定位

`Common` 模块源码在 [src/store/modules/common/index.ts](/src/store/modules/common/index.ts)，主要负责项目级的通用缓存数据：指标分类、分类下指标列表、指标详情、标签映射、环境标签。

## State

| 字段 | 说明 |
|------|------|
| `metricTypeAndList` | 指标分类及分类下指标列表的原始数据 |
| `metricTypeData` | 指标分类树 |
| `metricInfoMap` | 指标详情映射 |
| `tagLabelMap` | 标签 key 到标签显示名的映射 |
| `envTagEnabled` | 环境标签开关 |
| `envTagData` | 全量环境标签 |
| `envTagDataByUser` | 当前用户可用的环境标签 |

## 关键 Actions

| Action | 说明 |
|------|------|
| `GET_METRIC_TYPE_AND_LIST` | 获取指标分类及分类下指标列表，已有缓存时不重复请求 |
| `GET_METRIC_TYPES` | 获取指标分类树，已有缓存时不重复请求 |
| `GET_METRIC_INFOS` | 获取多个指标的详情，已缓存指标会被跳过 |
| `GET_TAG_LABEL_MAP` | 获取标签 key 的显示名映射 |
| `GET_ENV_TAG_ENABLED` | 获取环境标签开关 |
| `GET_ENV_TAGS` | 获取全量环境标签 |
| `GET_ENV_TAGS_BY_USER` | 获取当前用户可见环境标签 |

## 关键 Mutations

| Mutation | 说明 |
|------|------|
| `SET_METRIC_TYPE_AND_LIST` / `CLEAR_METRIC_TYPE_AND_LIST` | 设置或清空“分类 + 指标列表”原始数据、指标分类树缓存 |
| `SET_METRIC_TYPES` | 设置指标分类树 |
| `SET_METRIC_INFOS` / `DELETE_METRIC_INFO` | 设置或删除指标详情缓存 |
| `SET_TAG_LABEL_MAP` | 设置标签映射 |
| `SET_ENV_TAG_ENABLED` | 设置环境标签开关 |
| `SET_ENV_TAGS` / `SET_ENV_TAGS_BY_USER` | 设置环境标签数据 |
| `CLEAR_ENV_TAGS` | 清空环境标签相关缓存 |

## 关键 Getters

| Getter | 说明 |
|------|------|
| `metricTypeData` | 返回标准指标分类树 |
| `metricInfoMap` | 返回指标详情缓存 |
| `getMetricTypeDataByType` | 按 `type1/type2/type3` 过滤分类树，常用于限制级联可选范围 |
| `getMetricsByType` | 按 `type1/type2/type3` 过滤指标列表，返回去重排序后的指标名数组 |

## 数据转换

### 指标分类

- `SET_METRIC_TYPES` 会调用 [src/store/modules/common/utils.ts](/src/store/modules/common/utils.ts) 中的 `formatMetricTypeData`。
- 这个工具方法会把后端返回的 `type1/type2/type3` 扁平结构转成 Cascader 树。
- 排序时对“主机/进程/其他”做了特殊优先级处理。
- `SET_METRIC_TYPE_AND_LIST` 会额外保留一份带 `metricList` 的原始数据，供业务侧按分类筛指标使用。

### health 相关能力

- `GET_METRIC_TYPE_AND_LIST` 使用 `MetricApi.getMetricTypesByQuery()` 拉取“分类 + 指标列表”数据。
- `getMetricTypeDataByType` 和 `getMetricsByType` 是为健康度配置等场景提供的筛选能力。
- 这部分能力把原先 `health` 对 `dataReport/store` 的依赖收敛到了 `Common` 模块内。

### 标签映射

- `SET_TAG_LABEL_MAP` 会把后端返回的 `name` 解析成：
  - `name`：用于展示的主名称
  - `originName`：原始名称

### 环境标签

- `SET_ENV_TAGS` 和 `SET_ENV_TAGS_BY_USER` 都会把接口结果整理成：
  - `label`
  - `value`
  - `children`
  这样的树结构，方便直接给选择器使用。

## 使用建议

- 这个模块适合承载“跨页面复用、可缓存”的字典/元数据，不适合放强业务状态。
- 获取指标详情时优先通过 `GET_METRIC_INFOS` 批量请求，避免页面侧重复单个查询。
- 如果页面既需要分类树又需要分类下的指标列表，优先使用 `GET_METRIC_TYPE_AND_LIST`，不要自行拼装两份缓存。
