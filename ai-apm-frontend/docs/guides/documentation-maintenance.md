# 文档维护指南

## 目标

这份指南面向后续维护 Databuff 文档的人，解决两个问题：

1. 改代码后，应该同步改哪些文档
2. 新增文档时，应该按什么结构写，才能和现有体系保持一致

## 什么时候必须更新文档

出现下面这些变更时，建议把文档更新视为同一个任务的一部分：

- 新增或删除路由
- 修改页面入口参数、query 参数或下钻链路
- 新增、删除或改名 API 方法
- 调整共享组件的输入输出、注册方式或使用范围
- 修改 Store 的 state / getter / action / mutation
- 调整启动方式、构建命令、代理配置或开发流程

## 代码变更与文档映射

| 代码改动位置 | 至少同步检查的文档 |
|------|------|
| `src/router/*` | [docs/router/README.md](/docs/router/README.md)、[docs/router/route-data.md](/docs/router/route-data.md)、相关模块 `README` / 页面文档 |
| `src/views/<module>/*` | 对应 [docs/modules/](/docs/modules) 下模块文档 |
| `src/api/*.ts` | [docs/api/README.md](/docs/api/README.md) 和对应 API 文档 |
| `src/components/*` | [docs/components/README.md](/docs/components/README.md) 和对应组件文档 |
| `src/store/*` | [docs/store/README.md](/docs/store/README.md) 和对应 module 文档 |
| `src/utils/*` | [docs/utils/README.md](/docs/utils/README.md) 和对应工具文档 |
| `package.json` / `vite.config.ts` / 启动流程 | [docs/guides/getting-started.md](/docs/guides/getting-started.md)、[docs/architecture/tech-stack.md](/docs/architecture/tech-stack.md) |

## 新文档应该放哪里

| 类型 | 目录 |
|------|------|
| 业务模块 | `docs/modules/<module>/` |
| API 文件 | `docs/api/` |
| 共享组件 | `docs/components/<category>/` |
| Store | `docs/store/` |
| 工具函数 | `docs/utils/` |
| 开发规范 / 流程 | `docs/guides/` |

## 推荐写法模板

### 模块页文档

适合 `docs/modules/<module>/<page>.md`：

```md
# 页面名

> 页面: `/route/path`
> 文件: `src/views/.../index.vue`

## 页面职责

一句话说明页面做什么、在模块中的位置是什么。

## 页面结构

- 子组件 1
- 子组件 2
- 子组件 3

## 主要接口

- `SomeApi.methodA`
- `SomeApi.methodB`

详细接口见:

- `对应 API 文档（按实际文件替换）`

## 关键参数

- 路由参数
- query 参数
- 页面内部固定补参

## 关联页面

- 下钻页 A
- 返回链路 B

## 注意事项

- 当前实现中的特殊兼容、未开放入口、复用关系
```

### 模块 README

适合 `docs/modules/<module>/README.md`：

- 模块定位
- 页面矩阵或页签矩阵
- 主要依赖 API
- 已补文档索引
- 当前注意事项

### API 文档

适合 `docs/api/*.md`：

- 文件定位
- 概述
- 按能力分组列接口表
- 记录特殊处理，如 `blob`、`transformResponse`、参数转换
- 只按源码现状记录，不替源码“纠正命名”

### 组件文档

适合 `docs/components/**/*.md`：

- 组件说明
- 常用 Props
- 关键事件 / 插槽
- 特殊行为
- 典型使用
- 使用建议

### Store / Utils 文档

适合 `docs/store/*.md` 和 `docs/utils/*.md`：

- 文件定位
- 核心职责
- 对外暴露能力
- 特殊行为 / 注意事项

## 命名与表述建议

- 模块目录优先使用与源码一致的 kebab-case 名称
- 标题优先写用户能理解的中文名称，英文名作为补充
- 链接到源码时，尽量直接指向真实文件，不写猜测路径
- 如果代码和命名不理想，文档里要“按现状说明”，不要擅自美化成另一套实现

## 写文档时的几个原则

- 优先写“真实入口、真实参数、真实依赖”
- 优先解释页面之间怎么跳，而不是只列子组件
- 复杂实现可以概括，但关键路由、接口名、参数名不要模糊化
- 不确定的信息统一标注“待确认”，不要脑补

## 更新索引的顺序

新增文档后，通常还要同步这几个地方：

1. [docs/README.md](/docs/README.md)
2. [docs/DOC-STRUCTURE.md](/docs/DOC-STRUCTURE.md)
3. 所属目录自己的 `README.md`

如果只补单篇页面文档而没有进索引，后续很容易再次出现“文件存在，但没人知道从哪里进入”的问题。

## 提交前检查清单

- 链接是否都能打开
- 路径、接口名、query 参数是否和代码一致
- `docs/README.md` 和 `docs/DOC-STRUCTURE.md` 是否同步
- 新增文档是否出现在对应目录索引中
- 状态数字是否需要更新

## 当前建议

现在整套文档已经整体闭环，后续更适合做“增量维护”，而不是再起一套并行结构。新增文档时，优先复用现有目录和模板；只有确实出现新的文档类别时，再扩展 `DOC-STRUCTURE`。
