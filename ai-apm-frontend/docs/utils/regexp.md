# regexp

## 文件定位

[src/utils/regexp.ts](/src/utils/regexp.ts) 存放项目里复用的正则表达式和输入校验函数，重点覆盖 IP、XSS 和命名规则。

## 主要导出

| 名称 | 说明 |
|------|------|
| `ipReg` | IPv4 校验 |
| `ipRangeCheck` | IP / IP 段 / IP 范围输入校验函数 |
| `characterReg` | 标点和空格检查 |
| `htmlTagReg` / `htmlAnnotaionReg` | HTML 标签/注释检查 |
| `xssRegTest` | 简单 XSS 检测 |
| `serviceNameReg` | 服务名规则 |
| `serviceNameNewReg` / `serviceNameNewReg2` | 新版服务名规则 |
| `LensVersionReg` | 版本号格式校验 |

## `ipRangeCheck`

这个函数用于表单校验，支持：

- 单个 IP
- `a.b.c.d-e.f.g.h` 范围
- `a.b.c.d/24` 网段
- 多项逗号分隔

并且会额外检查：

- 中文字符
- 英文字符
- 中文全角逗号
- 空值

## `xssRegTest`

当前实现重点拦截 HTML 注释片段：

- 支持 `Object`
- 支持 `Array`
- 支持 `String`

它不是完整的富文本安全方案，而是一个轻量输入过滤器，主要配合请求拦截器使用。

## 使用位置

- 请求前参数校验
- 表单输入校验
- 登录页版本号格式判断
- 服务/实体命名规则限制
