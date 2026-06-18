# 快速开始

## 适用范围

这份指南面向第一次接手 Databuff 前端仓库的开发者，目标是先把项目跑起来，再建立“入口文件 -> 路由 -> 页面 -> API”的基本认知。

## 环境要求

根据项目根目录 [README.md](/README.md) 与 [package.json](/package.json)，当前推荐环境为：

- Node.js `18+`
- Yarn `1.x`

## 安装与启动

```bash
yarn
yarn dev
```

常用命令：

| 命令 | 说明 |
|------|------|
| `yarn dev` | 本地开发模式 |
| `yarn serve` | 开启 `--host`，便于局域网访问 |
| `yarn build` | TypeScript 检查 + 生产构建 |
| `yarn build:visualizer` | 构建并生成 `dist/stats.html` 包体积报告 |
| `yarn preview` | 本地预览构建产物 |

默认开发地址是 `http://localhost:5173`。

## 本地代理

[vite.config.ts](/vite.config.ts) 里当前配置了：

- `/webapi`
- `/api6972`

这两个代理默认都指向固定的内网地址 `https://192.168.50.193`。如果本地无法访问该环境，需要先调整代理目标，或者在联调环境中直接使用对应网关。

## 入口认知

建议按下面顺序读代码：

1. [src/main.ts](/src/main.ts)
2. [src/router/index.ts](/src/router/index.ts)
3. [src/store/index.ts](/src/store/index.ts)
4. [src/components/index.ts](/src/components/index.ts)
5. `src/views/<module>/`
6. `src/api/*.ts`

对应关系可以简单理解为：

- `main.ts` 挂载 Vue、Element UI、全局组件、全局过滤器和路由/Store
- `router/index.ts` 负责登录态、授权态、时间范围 query 与动态视图加载
- `views` 负责页面结构和交互
- `api` 负责接口封装
- `utils/axios.ts` 负责统一请求拦截

## 推荐阅读顺序

如果是第一次接手项目，建议优先看这些文档：

1. [架构概述](/docs/architecture/overview.md)
2. [技术栈](/docs/architecture/tech-stack.md)
3. [路由概述](/docs/router/README.md)
4. [API 总览](/docs/api/README.md)
5. 你当前要维护的模块文档，例如 [app-monitor](/docs/modules/app-monitor/README.md) 或 [alarm-center](/docs/modules/alarm-center/README.md)

## 日常开发建议

- 新需求先确认路由入口和 query 参数，再看页面 `index.vue`
- 页面不要直接发裸请求，优先复用 `src/api/*`
- 全局状态优先确认是否已经在 `store` 里缓存过
- 修改完成后，至少执行一次 `yarn build`，确认 TypeScript 和构建都能通过

## 常见入口文件

| 路径 | 说明 |
|------|------|
| [src/main.ts](/src/main.ts) | Vue 应用启动入口 |
| [src/router/index.ts](/src/router/index.ts) | 路由守卫、时间参数处理、白名单 |
| [src/store/index.ts](/src/store/index.ts) | 根 Store 与模块注册 |
| [src/utils/axios.ts](/src/utils/axios.ts) | 统一请求拦截器 |
| [src/components/index.ts](/src/components/index.ts) | 全局组件注册入口 |
