# Db Icon Button

## 组件说明

`DbIconButton` 是图标按钮组件，源码在 [src/components/DbIconButton/index.vue](/src/components/DbIconButton/index.vue)。它适合承载表格设置、删除、加减行等轻量图标操作。

## 常用 Props

| Prop | 说明 |
|------|------|
| `icon` | 图标名，支持传完整 `db-icon-*` 或简写 |
| `describe` | `title` 提示文案 |
| `size` | `large` / `default` / `small` / `mini` |
| `border` | 是否显示边框 |

## 特殊行为

- 如果 `icon` 没带 `db-icon-` 前缀，组件会自动补齐
- 样式上支持不同尺寸和无边框模式
- 当前源码里定义了 `clickHandle()`，但模板没有直接绑定 click

## 使用注意

从现有页面看，这个组件更多是通过原生事件监听使用，例如 `@click.native`，而不是依赖组件自己 `$emit('click')`。

## 典型使用

- `DbTable` 列设置按钮
- 配置项的增加/删除按钮
- 小型操作入口
