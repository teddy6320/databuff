# Dialog Template

## 组件说明

`dialog-template` 当前在源码里不是完整可直接复用的组件，而是一个弹窗骨架模板，文件在 [src/components/dialog-template/dialog-copy-template.vue](/src/components/dialog-template/dialog-copy-template.vue)。

它体现了项目里大多数弹窗组件的通用写法：

- 用 `value` 作为显隐输入
- 监听 `value` 同步到本地 `showModel`
- 关闭时统一触发 `input(false)` 和 `on-close`
- 提交时用 `dialogPostLoading` 锁定关闭行为

## 典型结构

| 区块 | 说明 |
|------|------|
| `el-dialog` | 承载弹窗主体 |
| `showModel` | 内部显隐状态 |
| `dialogCancelHandle` | 统一关闭逻辑 |
| `postHandle` | 提交逻辑入口 |

## 推荐复用方式

实际开发中通常不是直接 import 这个模板，而是参照它去创建业务弹窗，例如：

- [src/views/personal/pwd-modal.vue](/src/views/personal/pwd-modal.vue)
- [src/views/personal/email-modal.vue](/src/views/personal/email-modal.vue)
- [src/views/configStatus/agent/config-dialog.vue](/src/views/configStatus/agent/config-dialog.vue)

## 注意事项

- 当前模板文件更像“复制模板”，不是正式的抽象组件；如果后面要推广，建议沉淀成真正的基础弹窗封装。
