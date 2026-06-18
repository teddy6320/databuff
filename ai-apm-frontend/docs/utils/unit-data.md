# unit-data

## 文件定位

[src/utils/getUnitData.ts](/src/utils/getUnitData.ts) 用于把单位字符串转换成带比例、显示名和 human-format scale 的元数据对象。

## 核心能力

| 能力 | 说明 |
|------|------|
| 单位识别 | 识别字节、时间、百分比、网络、系统类单位 |
| 简写转换 | 生成 `short_name` / `original_short_name` |
| 比例因子 | 给出 `scale_factor` |
| scale 选择 | 自动匹配 `SI`、`binary`、`time` 三类 scale |
| 复合单位 | 支持处理 `req/s`、`B/s` 这类带 `/` 的单位 |

## 内置单位族

| family | 示例 |
|------|------|
| `bytes` | `B`、`KiB`、`MiB`、`GiB` |
| `percentage` | `%` |
| `time` | `ns`、`ms`、`s`、`min`、`h`、`d` |
| `network` | `request`、`packet` |
| `system` | `thread`、`garbage collection` |

## 返回结构

调用 `getUnitData(unit)` 后，常见返回字段包括：

- `name`
- `plural`
- `short_name`
- `original_short_name`
- `scale_factor`
- `family`
- `scale`
- `sub_unit`

## 特殊行为

- 未识别的单位不会报错，而是回退成“原样展示”的默认结构。
- 当单位包含 `/` 时，会拆成前后两个单位分别解析，再拼成复合结果。

## 典型用途

- 指标展示
- 图表 tooltip
- 单位换算前的元数据解析
