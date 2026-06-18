export interface OptionalPropMenu {
  hidden?: boolean;   // 是否隐藏，默认 false
  parentId: number;   // 父节点Id
  id: number;         // 唯一标识
  name: string;       // 路由名称，中英文
  path: string;       // 路由path
  filePath?: string;  // 文件路径，默认 同path
  icon?: string;      // 默认 空
  isMenu?: boolean;   // 是否在菜单中显示，默认 false
  leaf?: boolean;     // 是否为叶子节点，默认 false
  order?: number;     // 排序
  isStatic?: boolean; // 是否为静态路由，默认 false
  menuId?: number;    // 高亮菜单id，默认 同id    **！！menuId存在但匹配不到时，当前菜单会被删除！！**
  module?: string;    // 菜单归属的模块
  // 时间控件，默认 false  false:不显示，true:显示自定义和最近时间，latest:只显示最近时间
  // 以 step- 开头，以相同的间隔（分钟）前后切换
  time?: boolean | 'latest' | string;
  refresh?: boolean;   // 是否显示刷新按钮，默认 false
  limitDays?: number; // 单次最大时间跨度（天），默认 DEFAULT_LIMIT_DAY
  noHeader?: boolean; // 是否隐藏左侧菜单，默认 false
  noFooter?: boolean; // 是否隐藏上方Footer，默认 false
}

export interface FullPropMenu extends OptionalPropMenu {
  hidden: boolean;
  filePath: string;
  icon: string;
  isMenu: boolean;
  leaf: boolean;
  order: number;
  isStatic: boolean;
  menuId: number;
  menuIds?: number[]; // 所有祖先菜单id和高亮菜单id的数组
  time: boolean | 'latest' | string;
  module: string;
  level?: number; // 菜单层级
  children?: FullPropMenu[]; // 子节点
}

// 数据库菜单字段
interface Menu {
  parentId: number;   // 父节点Id
  id: number;         // 唯一标识
  path: string;       // 路由path
  hidden?: boolean;   // hidden == false 隐藏
  module_function?: string;
}
