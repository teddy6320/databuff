export interface Info {
  [key: string]: any
  texts?: string[]
}

export interface FilterItem {
  label: string
  field: string
  type: 'input'|'select'|'number'|'date'
  multiple?: boolean
  checked?: boolean
  disabled: boolean
  deletable?: boolean
  likeable?: boolean
  addable?: boolean
  editable?: boolean
  children: FilterOptionBase[]
}

export interface FilterItemLocal {
  label: string
  field: string
  type: 'input'|'select'|'number'|'date'
  multiple?: boolean
  checked?: boolean
  disabled: boolean
  deletable: boolean
  likeable?: boolean
  addable?: boolean
  editable?: boolean
  children: FilterOptionLocal[]
  info?: Info
}

export interface FilterOptionBase extends KeyValue {
  showValue: string
  checked?: boolean
  disabled?: boolean
  info?: Info
}

export interface FilterOptionLocal extends FilterOptionBase {
  checked: boolean
  disabled: boolean
  custom: boolean
  kv: 'k'|'v'
}

export interface KeyValue {
  label: string
  value: string|number
}


export interface TagItem {
  key: string
  label: string
  value: string|number|null|Array<string|number|null>
  showValue: string|number|null
  field: string
  type: 'input'|'select'|'number'|'date'
  info?: Info
  deletable: boolean
  addable?: boolean
  editable?: boolean
  multiple?: boolean
}

export interface FormatedSelected {
  field: string
  value: Array<string|number|null>
}
