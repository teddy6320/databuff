# Router View Temp

## 组件说明

`RouterViewTemp` 是一个极简的路由包裹组件，源码在 [src/components/router-view-temp/index.vue](/src/components/router-view-temp/index.vue)。

它只做一件事：

- 渲染 `<router-view />`

## 存在意义

它主要用于“需要一个公共占位包裹层，但本身不想加任何业务逻辑”的场景。源码注释也明确说明了：

- 如果只需要纯转发，可以用它
- 如果需要特殊逻辑，应该在业务目录里自己写 `index.vue`

## 使用建议

- 适合做纯占位父路由组件
- 不适合承载任何权限、数据初始化、布局副作用
