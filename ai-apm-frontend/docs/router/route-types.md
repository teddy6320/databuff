# 路由类型定义

## OptionalPropMenu - 可选属性菜单

定义路由/菜单的可选属性：

```typescript
interface OptionalPropMenu {
  hidden?: boolean;      // 是否隐藏，默认 false
  parentId: number;      // 父节点 Id，顶级菜单为 0
  id: number;            // 唯一标识（不可重复）
  name: string;          // 路由名称，用于显示
  path: string;          // 路由路径
  filePath?: string;     // 组件文件路径，默认同 path
  icon?: string;         // 菜单图标类名
  isMenu?: boolean;      // 是否在菜单中显示，默认 false
  leaf?: boolean;        // 是否为叶子节点，默认 false
  order?: number;        // 排序权重
  isStatic?: boolean;    // 是否为静态路由（非菜单页面），默认 false
  menuId?: number;       // 高亮菜单 id，默认同 id
  module?: string;       // 菜单归属的模块标识
  time?: boolean | 'latest' | string;  // 时间控件配置
  refresh?: boolean;     // 是否显示刷新按钮，默认 false
  limitDays?: number;    // 单次最大时间跨度（天），默认 31
  noHeader?: boolean;    // 是否隐藏左侧菜单，默认 false
  noFooter?: boolean;    // 是否隐藏上方 Footer，默认 false
}
```

### 属性详解

#### time - 时间控件配置

| 值 | 含义 |
|----|------|
| `false` | 不显示时间选择器（默认） |
| `true` | 显示自定义时间和最近时间选项 |
| `'latest'` | 只显示最近时间选项 |
| `'step-{n}'` | 以 n 分钟为间隔前后切换 |

#### limitDays - 时间跨度限制

控制单次查询的最大时间范围：
- 默认值：31 天
- 驾驶舱：1 天
- 报告：60 天
- 服务流：1 天

#### isStatic - 静态路由

静态路由表示非菜单页面（如详情页、设置页），这些页面：
- 不在左侧菜单中显示
- 通过其他页面跳转进入
- 使用 `menuId` 指定高亮的父菜单

**注意**：`menuId` 存在但匹配不到时，当前菜单会被删除。

## FullPropMenu - 完整属性菜单

包含所有属性的完整菜单类型：

```typescript
interface FullPropMenu extends OptionalPropMenu {
  hidden: boolean;
  filePath: string;
  icon: string;
  isMenu: boolean;
  leaf: boolean;
  order: number;
  isStatic: boolean;
  menuId: number;
  menuIds?: number[];    // 所有祖先菜单 id 和高亮菜单 id 的数组
  time: boolean | 'latest' | string;
  module: string;
  level?: number;        // 菜单层级
  children?: FullPropMenu[];  // 子节点
}
```

## Menu - 数据库菜单字段

从后端返回的原始菜单数据结构：

```typescript
interface Menu {
  parentId: number;       // 父节点 Id
  id: number;             // 唯一标识
  path: string;           // 路由路径
  hidden?: boolean;       // hidden == false 隐藏
  module_function?: string;  // 【待确认】功能标识
}
```

## 类型转换

`route-data.ts` 中的转换逻辑：

```typescript
const routeData: FullPropMenu[] = [
  // ...原始数据
].map(t => ({
  ...t,
  hidden: !!t.hidden,
  filePath: t.filePath || t.path,      // 默认使用 path
  icon: t.icon || '',                   // 默认空字符串
  isMenu: !!t.isMenu,
  leaf: !!t.leaf,
  order: t.order || 99,                 // 默认排序 99
  isStatic: !!t.isStatic,
  menuId: t.menuId || t.id,             // 默认使用自身 id
  time: t.time || false,
}));
```

## 唯一性校验

路由数据导出前会进行校验：

1. **ID 重复检查**：控制台输出重复 ID 警告
2. **静态菜单检查**：控制台输出 `isMenu && isStatic` 的警告

## 文件位置

`src/router/route.types.ts`
