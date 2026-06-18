# 技术栈

> 最后更新: 2026-04-05

## 运行环境

| 依赖 | 版本要求 | 说明 |
|------|----------|------|
| Node.js | v18+ | 运行环境 |
| Yarn | v1+ | 包管理器 |

---

## 前端技术栈

### 核心框架

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 2.7.16 | 前端框架 |
| Vue Router | 3.0.3 | 路由管理 |
| Vuex | 3.0.1 | 状态管理 |
| TypeScript | ~5.7.2 | 类型系统 |

### 构建工具

| 技术 | 版本 | 说明 |
|------|------|------|
| Vite | 6.3.1 | 构建工具 (v2.10.1.1 起使用) |
| Sass | 1.27.0 | CSS 预处理器 |
| Sass Embedded | 1.87.0 | Dart Sass 实现 |

### UI 组件库

| 技术 | 版本 | 说明 |
|------|------|------|
| Element UI | 2.15.3 | UI 组件库 |
| element-china-area-data | 6.1.0 | 中国省市区数据 |

### 可视化

| 技术 | 版本 | 说明 |
|------|------|------|
| ECharts | 6.0.0 | 图表库 |
| @antv/g6 | ^4.8.24 | 图可视化 (关系图、拓扑图) |
| @antv/g6 (v5) | ^5.0.50 | G6 v5 版本 (通过 g6-v5 别名) |
| @antv/x6 | ^2.18.1 | 图编辑框架 (流程图、DAG) |
| d3 | ^7.0.1 | 数据可视化 |
| d3-hexbin | ^0.2.2 | 六边形分箱 |
| flame-chart-js | ^1.7.2 | 火焰图 |

### X6 插件

| 插件 | 版本 | 说明 |
|------|------|------|
| @antv/x6-plugin-dnd | ^2.1.1 | 拖拽 |
| @antv/x6-plugin-keyboard | ^2.2.3 | 键盘快捷键 |
| @antv/x6-plugin-selection | ^2.2.2 | 选择 |
| @antv/x6-plugin-snapline | ^2.1.7 | 对齐线 |
| @antv/x6-vue-shape | ^2.1.2 | Vue 节点 |

### 数据处理

| 技术 | 版本 | 说明 |
|------|------|------|
| Axios | ^0.18.0 | HTTP 客户端 |
| Lodash | ^4.17.21 | 工具函数库 |
| Day.js | ^1.11.5 | 日期处理 |
| MD5 | ^2.3.0 | MD5 加密 |
| SparkMD5 | ^3.0.2 | MD5 计算 |
| UUID | ^8.3.2 | UUID 生成 |
| fraction.js | ^5.3.4 | 分数运算 |
| human-format | ^0.11.0 | 数字格式化 |

### 代码编辑 & 展示

| 技术 | 版本 | 说明 |
|------|------|------|
| vue-codemirror | 4.0.6 | 代码编辑器 |
| highlight.js | ^11.6.0 | 代码高亮 |
| marked | ^4.0.18 | Markdown 解析 |
| diff | ^5.1.0 | 文本对比 |
| diff2html | ^3.4.22 | Diff 展示 |

### 工具库

| 技术 | 版本 | 说明 |
|------|------|------|
| clipboard | ^2.0.11 | 剪贴板操作 |
| js-cookie | ^2.2.1 | Cookie 操作 |
| js-base64 | ^3.7.2 | Base64 编解码 |
| sortablejs | ^1.15.6 | 拖拽排序 |
| vuedraggable | ^2.24.3 | Vue 拖拽 |
| splitpanes | ^2.3.8 | 分割面板 |
| simplebar-vue | ^1.6.10 | 自定义滚动条 |
| typed.js | ^2.1.0 | 打字动画 |
| html2pdf.js | 0.9.3 | HTML 转 PDF |

### 布局算法

| 技术 | 版本 | 说明 |
|------|------|------|
| @dagrejs/dagre | ^1.1.8 | 有向图布局 |

---

## 开发依赖

### 类型定义

| 包 | 版本 | 说明 |
|-----|------|------|
| @types/node | ^24.9.1 | Node.js 类型 |
| @types/d3 | ^7.0.0 | D3 类型 |
| @types/d3-hexbin | ^0.2.3 | D3 hexbin 类型 |
| @types/diff | ^5.0.2 | diff 类型 |
| @types/lodash | ^4.14.138 | Lodash 类型 |
| @types/spark-md5 | ^3.0.5 | SparkMD5 类型 |
| @types/uuid | ^8.3.4 | UUID 类型 |

### 构建插件

| 插件 | 版本 | 说明 |
|------|------|------|
| @vitejs/plugin-vue2 | ^2.3.4 | Vue 2 支持 |
| @vitejs/plugin-vue2-jsx | ^1.1.1 | JSX 支持 |
| vite-plugin-compression | 0.5.1 | Gzip 压缩 |
| vite-plugin-svg-icons | ^2.0.1 | SVG 图标 |
| rollup-plugin-visualizer | ^5.10.0 | 包分析可视化 |

### 其他

| 包 | 版本 | 说明 |
|-----|------|------|
| fast-glob | ^3.3.3 | 文件匹配 |
| json5 | ^2.2.3 | JSON5 解析 |
| prettier | ^1.19.1 | 代码格式化 |
| core-js | ^3.36.1 | Polyfill |
| regenerator-runtime | ^0.14.1 | Async/Await Polyfill |
| browserslist | ^4.21.8 | 浏览器兼容性 |
| babel-plugin-transform-remove-console | ^6.9.4 | 移除 console |

---

## Vue 2 生态增强

| 技术 | 版本 | 说明 |
|------|------|------|
| @vue/composition-api | ^1.7.2 | Composition API 支持 |
| vue-class-component | ^7.0.0 | Class 组件 |
| vue-property-decorator | ^9.1.2 | TypeScript 装饰器 |
| vuex-class | ^0.3.1 | Vuex Class 绑定 |
| @vue/compiler-sfc | ^3.5.22 | SFC 编译器 |

---

## 技术选型说明

### 为什么选择 Vue 2.7

> 待确认

### 为什么选择 Vite

项目在 v2.10.1.1 版本从 Vue CLI 迁移到 Vite，主要原因：
- 更快的开发服务器启动
- 更快的热更新
- 更简洁的配置

### 可视化方案

| 场景 | 技术选择 | 原因 |
|------|----------|------|
| 统计图表 | ECharts | 功能全面、生态成熟 |
| 关系图/拓扑图 | AntV G6 | 专门针对关系图的解决方案 |
| 流程图/DAG | AntV X6 | 支持图编辑、节点交互 |
| 火焰图 | flame-chart-js | 专业的性能分析火焰图 |

---

## 待确认事项

- [ ] 后端技术栈 (语言、框架)
- [ ] 数据库选型 (时序数据库、关系型数据库)
- [ ] 消息队列 (如有)
- [ ] 缓存方案
- [ ] 部署技术 (Docker、K8s)
- [ ] CI/CD 工具
- [ ] 监控告警方案 (自监控)

---

## 相关文档

- [架构概述](overview.md)
- [目录结构](directory-structure.md)
