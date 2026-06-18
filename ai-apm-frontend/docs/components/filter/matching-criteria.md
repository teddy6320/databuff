# Matching Criteria

## 组件说明

`MatchingCriteria` 是条件编排组件，源码在 [src/components/matching-criteria/index.vue](/src/components/matching-criteria/index.vue)。它适合配置“字段 + 运算符 + 值 + AND/OR + 子条件”这类规则表达式。

## 常用 Props

| Prop | 说明 |
|------|------|
| `conditionData` | 初始条件数组 |
| `fieldData` | 可选字段定义 |
| `andors` | 自定义连接符列表 |
| `symbols` | 自定义操作符列表 |
| `maxLevel` | 最大嵌套层级 |
| `showView` | 是否显示预览字符串 |
| `detailView` | 是否显示详细预览 |
| `showOne` | 无数据时是否默认显示一个条件 |
| `showSubConfig` | 是否允许添加子条件 |
| `singleModel` | 是否限制为单条件模式 |
| `atLeastOne` | 是否至少保留一个条件 |
| `showCase` | 是否显示大小写开关 |
| `allowCreateOption` | Select 值是否允许自定义 |
| `allowCreateKey` | 字段 Key 是否允许自定义 |

## 输出事件

| 事件 | 说明 |
|------|------|
| `on-change` | 条件变化后输出标准条件数组、预览字符串等数据 |

## 内部能力

- 支持多层嵌套条件组
- 内置默认连接符 `AND/OR`
- 内置常见操作符，如 `=`、`!=`、`like`、`inList`、`empty`
- 可以把树形条件结构转换成后端可消费的条件数组
- 可以生成人类可读的预览字符串

## 典型使用

当前主要用于：

- 告警静默条件
- 告警收敛条件
- 响应条件
- 健康度配置条件

## 使用建议

- 这是规则配置型页面的首选组件
- 如果条件结构非常简单，只是少量表单项筛选，不必使用它
