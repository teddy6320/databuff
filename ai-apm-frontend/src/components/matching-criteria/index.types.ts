export interface OptionItem {
  label: string;
  value: string;
}

export interface BooleanOptionItem {
  label: string;
  value: boolean;
}

export interface SymbolItem {
  label: string;      // 操作符中文名
  value: string;      // 操作符英文名
  type?: string;      // 目前只支持select
  empty?: boolean;    // 是否为空
  multiple?: boolean; // 是否多选
}

// type ValueItem = string | number | boolean;
type ValueItem = string; // 改为支持string
interface FieldOption {
  label: string;
  value: ValueItem;
}

export interface FieldData {
  [prop: string]: {
    label: string;    // 字段中文名
    value: string;    // 字段英文名
    options?: Array<string | FieldOption>; // 字段可选项
    symbols?: string[]; // 可支持的操作符values
    valueReg?: RegExp; // 字段值正则检验，只在输入框中生效
    tagType?: string; // 标签类型，检测规则中使用，会对数据的格式化特殊处理
  };
}

export interface ErrorData {
  [prop: string]: {
    field: boolean;
    symbol: boolean;
    value: boolean;
    caseInsensitive?: boolean;
  };
}

export interface UuidCharMapping {
  [prop: string]: string;
}

export interface FieldItem {
  field: string;      // 字段英文名
  symbol: string;     // 操作符
  caseInsensitive: boolean; // 是否不区分大小写 true->不区分 false->区分
  value: ValueItem | ValueItem[]; // 字段值
  uuid: string;       // 唯一标识
}

export type FieldList = Array<FieldItem | CriteriaData>

export interface CriteriaData {
  level: number;      // 层级
  uuid: string;       // 唯一标识
  andor: string;      // 连接符
  fieldList: FieldList; // 字段列表
}

export interface ConditionItem {
  connector: string;  // 连接符，同andor
  left: string;       // 字段英文名，同field
  right: ValueItem;   // 字段值，同value或value数组转换
  operator: string;   // 操作符，同symbol
  caseInsensitive?: boolean; // 是否不区分大小写 true->不区分 false->区分
  tagType?: string;   // 标签类型，检测规则中使用
}

interface CombinationItem {
  connector: string;  // 连接符，同andor
  left: ConditionItem[];
  right: never[];     // 空数组
}

export type ConditionData = Array<ConditionItem | CombinationItem>
