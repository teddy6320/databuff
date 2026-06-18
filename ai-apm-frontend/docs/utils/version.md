# version

## 文件定位

[src/utils/compareVersion.ts](/src/utils/compareVersion.ts) 提供版本号比较和排序工具，常用于 Agent 升级、插件兼容和版本分布处理。

## 主要导出

| 函数 | 说明 |
|------|------|
| `compareVersion(va, vb)` | 比较两个版本号 |
| `sortVersion(versions)` | 对字符串版本号数组升序排序 |
| `sortVersionObj(versionObjs, sortKey?)` | 对对象数组按版本字段升序排序 |

## 规则说明

- 版本号按 `.` 分段逐位比较
- 某一段不相等时，直接用数值差返回结果
- 如果前面都相同，则用数组长度差决定大小

## 常见使用场景

- Agent 页面判断：
  - 是否支持更新
  - 是否版本过低
  - 是否需要展示“有新版本”
- 图表/列表里的版本分布排序

## 注意事项

- 这个实现只适合纯数字段版本号，如 `2.9.2`、`2.7.8.1`。
- 如果未来出现 `beta`、`rc` 等语义版本后缀，当前实现不能直接处理。
