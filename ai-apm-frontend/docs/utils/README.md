# Utils 文档

## 目录概览

`src/utils/` 存放请求封装、鉴权辅助、Cookie 操作、时间处理、单位格式化、通用工具、正则校验和浏览器/版本判断等工具代码。

这一批文档优先覆盖项目里最常被直接 import 的公共工具：

- `axios.ts`
- `auth.ts`
- `jsCookie.ts`
- `timeFormat.ts`
- `getUnitData.ts`
- `common.ts`
- `regexp.ts`
- `browserVersion.ts`
- `compareVersion.ts`
- `echarts.ts`

## 文档索引

| 文档 | 说明 |
|------|------|
| [axios](axios.md) | Axios 实例、请求/响应拦截器、环境标签注入 |
| [auth](auth.md) | 授权状态判断器 `AuthBuilder` |
| [cookie](cookie.md) | 登录态、管理域、环境标签 Cookie/LocalStorage 操作 |
| [time-format](time-format.md) | 时间范围与 interval 计算 |
| [unit-data](unit-data.md) | 单位元数据和格式化比例 |
| [common](common.md) | 复制、节流/防抖、异步包装、EventBus 等 |
| [regexp](regexp.md) | IP、XSS、命名规则等正则与校验函数 |
| [browser](browser.md) | 浏览器识别与最低版本校验 |
| [version](version.md) | 版本号比较与排序 |
| [echarts](echarts.md) | ECharts 按需注册入口 |

## 关联文件

有一些辅助文件没有单独拆页，但会在对应文档里提到：

- `cancelToken.ts`
- `clickout.ts`
- `metric-query-format.ts`
- `formatColor.ts`
- `filters/*`
- `static/*`
