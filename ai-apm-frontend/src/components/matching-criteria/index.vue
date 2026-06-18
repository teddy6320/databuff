<template>
  <div v-show="!hidden" class="matching-wrapper">
    <div class="matching-content">
      <matching-criteria-item
        :criteriaData="criteriaData"
        :fieldData="fieldData"
        :andorList="andorList"
        :symbolList="symbolList"
        :caseList="caseList"
        :fieldList="fieldList"
        :criteriaError="criteriaError"
        :maxLevel="maxLevel"
        :addText="addText"
        :addMultipleText="addMultipleText"
        :delText="delText"
        :hidden="hidden"
        :uuidCharMapping="uuidCharMapping"
        :showCase="showCase"
        :showSubConfig="showSubConfig"
        :singleModel="singleModel"
        :atLeastOne="atLeastOne"
        :allowCreateOption="allowCreateOption"
        :allowCreateKey="allowCreateKey"
        :class="{ 'show-case': showCase }"
        class="matching-config"
      />
    </div>
    <div v-if="showView" class="matching-overview">
      {{ viewStr }}
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import i18n from '@/i18n';
import { v4 as uuidv4 } from 'uuid';
import MatchingCriteriaItem from './matchingCriteriaItem.vue'
import { OptionItem, SymbolItem, ErrorData, UuidCharMapping,
  CriteriaData, FieldItem, ConditionItem, BooleanOptionItem, } from './index.types'
import type { ConditionData, FieldData } from './index.types'

let charIndex = 0
// charIndex 转换为以a~z为进制的字符串
const getChar = () => {
  const radix = 26;
  const numToChar = (num: number) => String.fromCharCode(num + 'a'.charCodeAt(0))
  const char = charIndex.toString(radix).split('').map(t => numToChar(parseInt(t, radix))).join('')
  charIndex = charIndex + 1
  return char
}

// 默认连接符列表
const andorList: OptionItem[] = [
  { label: i18n.t('modules.components.matching-criteria.s_3b04598b') as string, labelKey: 'modules.components.matching-criteria.s_3b04598b', value: 'AND' },
  { label: i18n.t('modules.components.matching-criteria.s_a7185263') as string, labelKey: 'modules.components.matching-criteria.s_a7185263', value: 'OR' },
];
// 默认操作符列表
const symbolList: SymbolItem[] = [
  { label: i18n.t('modules.components.matching-criteria.s_4c35bf2e') as string, labelKey: 'modules.components.matching-criteria.s_4c35bf2e', value: '=', type: 'select' },
  { label: i18n.t('modules.components.matching-criteria.s_14a8af58') as string, labelKey: 'modules.components.matching-criteria.s_14a8af58', value: '!=', type: 'select' },
  { label: i18n.t('modules.components.matching-criteria.s_e13556bb') as string, labelKey: 'modules.components.matching-criteria.s_e13556bb', value: 'like' },
  { label: i18n.t('modules.components.matching-criteria.s_da0291f4') as string, labelKey: 'modules.components.matching-criteria.s_da0291f4', value: 'notLike'},
  { label: i18n.t('modules.components.matching-criteria.s_ca861b71') as string, labelKey: 'modules.components.matching-criteria.s_ca861b71', value: 'empty', empty: true },
  { label: i18n.t('modules.components.matching-criteria.s_539adc97') as string, labelKey: 'modules.components.matching-criteria.s_539adc97', value: 'notEmpty', empty: true },
  { label: i18n.t('modules.components.matching-criteria.s_4af4af44') as string, labelKey: 'modules.components.matching-criteria.s_4af4af44', value: 'inList', type: 'select', multiple: true },
  { label: i18n.t('modules.components.matching-criteria.s_33a76148') as string, labelKey: 'modules.components.matching-criteria.s_33a76148', value: 'notInList', type: 'select', multiple: true },
];
// 是否不区分大小写
const caseList: BooleanOptionItem[] = [
  { label: i18n.t('modules.components.matching-criteria.s_9759e356') as string, labelKey: 'modules.components.matching-criteria.s_9759e356', value: true },
  { label: i18n.t('modules.components.matching-criteria.s_63d511b7') as string, labelKey: 'modules.components.matching-criteria.s_63d511b7', value: false },
]

@Component({
  name: 'MatchingCriteria',
  components: {
    MatchingCriteriaItem
  }
})
export default class MatchingCriteria extends Vue {
  @Prop({ default: () => [] }) private conditionData!: ConditionData // 初始化数据
  @Prop({ default: () => ({}) }) private fieldData!: FieldData // 字段数据
  @Prop() private andors!: OptionItem[]                 // 连接符列表
  @Prop() private symbols!: SymbolItem[]                // 操作符列表
  @Prop({ default: 3 }) private maxLevel!: number       // 最大嵌套层级
  @Prop({ default: false }) private showView!: boolean  // 显示预览
  @Prop({ default: false }) private detailView!: boolean  // 详细预览信息
  @Prop({ default: true }) private showOne!: boolean    // 无数据时，是否默认显示一个条件
  @Prop({ default: () => i18n.t('modules.components.matching-criteria.s_69fbb2e5') as string }) private addText!: string
  @Prop({ default: () => i18n.t('modules.components.matching-criteria.s_45fea638') as string }) private addMultipleText!: string
  @Prop({ default: () => i18n.t('modules.components.matching-criteria.s_2f4aaddd') as string }) private delText!: string
  @Prop({ default: false }) private hidden!: boolean    // 是否隐藏组件
  @Prop({ default: true }) private showSubConfig!: boolean // 是否展示添加子条件
  @Prop({ default: false }) private singleModel!: boolean // 是否为单条件模式 - TODO：单条件初始为or还是and
  @Prop({ default: false }) private atLeastOne!: boolean // 条件是否至少有一个，至少一个时，删除按钮不可删除
  @Prop({ default: false }) private showCase!: boolean  // 是否显示大小写选择框
  @Prop({ default: false }) private allowCreateOption!: boolean // Select是否允许创建选项
  @Prop({ default: false }) private allowCreateKey!: boolean // Select是否允许创建前置key选项
  @Prop({ default: () => ({}) }) private fieldLabelMap!: any // 字段Label映射，存在时将不在使用fieldData中的label

  private criteriaData: CriteriaData | {} = {} // 组件内部的数据
  private criteriaError: ErrorData = {} // 需要检验的错误，uuid对应错误数据的映射
  private uuidCharMapping: UuidCharMapping = {} // uuid对应char的映射
  private viewStr: string = '' // 连接关系预览

  get andorList () {
    return [...(this.andors || andorList)]
  }
  get symbolList () {
    return [...(this.symbols || symbolList)]
  }
  get emptySymbols () {
    return this.symbolList.filter(t => t.empty).map(t => t.value)
  }
  get multipleSymbols () {
    return this.symbolList.filter(t => t.multiple).map(t => t.value)
  }
  get caseList () {
    return [...caseList]
  }

  // 默认连接符
  get defaultAndor () {
    return this.andorList.length ? this.andorList[0].value : ''
  }
  // 连接符对应的label映射
  get andorData () {
    const data: { [prop: string]: string } = {};
    this.andorList.forEach(t => data[t.value] = t.label);
    return data;
  }

  get fieldList (): OptionItem[] {
    return Object.entries(this.fieldData).map(([key, item]) => ({
      label: this.fieldLabelMap[key] || item.label,
      value: key,
    }))
  }
  @Watch('fieldList')
  private onFieldListChange (value: OptionItem[]) {
    if (!value || !value.length) {
      this.criteriaData = this.conditionToCriteria()
    }
  }

  get criteriaDataStr () {
    return JSON.stringify(this.criteriaData)
  }
  @Watch('criteriaDataStr')
  private onCriteriaDataStrChange () {
    charIndex = 0
    const { data, viewStr, viewCharStrArr, uuidChar, filterEmpty } = this.criteriaToCondition(this.criteriaData as CriteriaData)
    this.viewStr = viewStr
    this.uuidCharMapping = uuidChar
    this.filterEmpty = filterEmpty
    Object.keys(this.criteriaError).forEach((uuid: string) => {
      if (filterEmpty[uuid]) {
        this.criteriaError[uuid] = filterEmpty[uuid]
      } else { // 无报错删除，后续有报错不提示
        delete this.criteriaError[uuid]
      }
    })
    this.$emit('on-change', { data, viewStr, viewCharStrArr })
  }

  private created () {
    if (Array.isArray(this.conditionData) && this.conditionData.length) {
      this.criteriaData = this.conditionToCriteria(this.conditionData)
    } else {
      this.criteriaData = this.conditionToCriteria()
    }
  }

  // 数据保存， CriteriaData类型 转为 ConditionData类型及其他数据
  public criteriaToCondition (data: CriteriaData) {
    const andor = data.andor
    const _data: ConditionData = []
    let _uuidChar: UuidCharMapping = {}
    let _filterEmpty: ErrorData = {}
    const _viewStrArr: string[] = []
    const _viewCharStrArr: string[] = []
    data.fieldList.forEach((item) => {
      if (!Array.isArray((item as CriteriaData).fieldList)) {
        const { field = '', symbol = '', value, caseInsensitive, uuid } = item as FieldItem
        let _value = value
        const isMulti = this.multipleSymbols.includes(symbol)
        const isEmpty = this.emptySymbols.includes(symbol)
        const fieldItem: any = this.fieldData[field] || {}
        if (isMulti && Array.isArray(value)) {
          // 数组 转为 逗号分隔
          _value = value.join(',')
        }
        const _item: ConditionItem = {
          connector: andor,
          left: field,
          operator: symbol,
          right: (_value ? `${_value}`.trim() : _value) as string,
        }
        if (this.showCase) {
          _item.caseInsensitive = !!caseInsensitive
        }
        if (fieldItem.tagType) {
          // 对left进行替换
          _item.left = _item.left.replace(`[[${fieldItem.tagType}]]`, '')
          _item.tagType = fieldItem.tagType
        }
        _data.push(_item)
        const char = getChar()
        _uuidChar[uuid] = char
        _viewStrArr.push(char)

        const options: any[] = (fieldItem.options || []).map((t: any) => {
          const isOptionItem = Object.prototype.toString.call(t) === '[object Object]'
          return isOptionItem ? t : { label: t, value: t }
        })
        // viewCharStr
        if (this.detailView) {
          const symbolCn = (this.symbolList.find(t => t.value === symbol) || {}).label || symbol
          let valueCn = ''
          if (isMulti && Array.isArray(value)) {
            valueCn = value.map(val => {
              return (options.find(t => t.value === val) || {}).label || val
            }).join(',')
          } else {
            valueCn = (options.find(t => t.value === value) || {}).label || value
          }
          let caseInsensitiveCn = ''
          if (this.showCase) {
            const caseItem = this.caseList.find(t => t.value === !!caseInsensitive)
            caseInsensitiveCn = caseItem ? `${caseItem.label}的` : ''
          }
          if (!['startWith', 'endWith'].includes(symbol)) {
            _viewCharStrArr.push(`${char}: ${this.fieldLabelMap[field] || fieldItem.label || field} ${symbolCn} ${caseInsensitiveCn}${valueCn}`)
          } else {
            // symbol为startWith或endWith时，特殊处理
            const [prefix, ...suffix] = symbolCn.split('…')
            _viewCharStrArr.push(`${char}: ${this.fieldLabelMap[field] || fieldItem.label || field} ${prefix} ${caseInsensitiveCn}${valueCn} ${suffix.join('…')}`)
          }
        }

        const valueEmpty = !_value
        let valueErr = !isEmpty && valueEmpty
        if (!isEmpty && !valueEmpty && !Array.isArray(value)) {
          const inOptions = options.find(t => t.value === value)
          if (!inOptions && fieldItem.valueReg) {
            valueErr = !fieldItem.valueReg.test(value as string)
          }
        }
        const caseInsensitiveErr = this.showCase && !this.caseList.find(t => t.value === caseInsensitive)
        if (!field || !symbol || valueErr || caseInsensitiveErr) {
          _filterEmpty[uuid] = {
            field: !field,
            symbol: !symbol,
            value: valueErr,
          }
          if (caseInsensitiveErr) {
            _filterEmpty[uuid].caseInsensitive = caseInsensitiveErr
          }
        }
      } else if ((item as CriteriaData).fieldList.length) {
        const subData = this.criteriaToCondition(item as CriteriaData)
        _data.push({
          connector: andor,
          left: subData.data as ConditionItem[],
          right: [],
        })
        _viewStrArr.push(`( ${subData.viewStr} )`)
        _viewCharStrArr.push(...subData.viewCharStrArr)
        _uuidChar = { ..._uuidChar, ...subData.uuidChar }
        _filterEmpty = { ..._filterEmpty, ...subData.filterEmpty }
      }
    })
    return {
      data: _data,
      viewStr: _viewStrArr.join(` ${this.andorData[andor] || andor} `),
      viewCharStrArr: [..._viewCharStrArr],
      uuidChar: _uuidChar,
      filterEmpty: _filterEmpty,
    }
  }

  // 数据回填， ConditionData类型 转为 CriteriaData类型
  public conditionToCriteria (data?: ConditionData, level = 1) {
    const criteria: CriteriaData = {
      uuid: uuidv4(),
      level,
      andor: this.defaultAndor,
      fieldList: []
    }
    if (data && data.length) {
      const connector0 = data[0].connector
      const connector1 = (data[1] || {}).connector
      criteria.andor = connector0 || connector1 || this.defaultAndor
      data.forEach((item) => {
        if (!Array.isArray(item.left)) {
          item = item as ConditionItem
          const value = item.right
          const isMulti = this.multipleSymbols.includes(item.operator)
          const _item: FieldItem = {
            uuid: uuidv4(),
            field: item.left,
            symbol: item.operator,
            caseInsensitive: item.caseInsensitive !== false,
            value: isMulti && !Array.isArray(value) ? `${value}`.split(',') : `${value}`,
          }
          if (item.tagType) {
            _item.field = `[[${item.tagType}]]${_item.field}`
          }
          criteria.fieldList.push(_item)
        } else if (item.left.length) {
          const subData = this.conditionToCriteria(item.left, level + 1)
          criteria.fieldList.push(subData)
        }
      })
    } else if (this.showOne) {
      criteria.fieldList.push({
        uuid: uuidv4(),
        field: '',
        symbol: '',
        caseInsensitive: true,
        value: '',
      })
    }
    return criteria
  }

  public getCondition (data?: ConditionData) {
    return new Promise((resolve) => {
      this.$nextTick(() => {
        charIndex = 0
        const criteriaData = this.conditionToCriteria(data)
        resolve(this.criteriaToCondition(criteriaData))
      })
    })
  }

  // 空校验
  private filterEmpty: ErrorData = {} // 所有的错误信息
  public validate () { // 检验通过 true，否则 false
    this.criteriaError = this.filterEmpty
    const emptyList = Object.values(this.filterEmpty)
    return !emptyList.map((t) => Object.values(t)).flat().includes(true)
  }
}
</script>

<style lang="scss" scoped>
.matching-wrapper{
  line-height: 32px;
  font-size: 13px;
  .matching-content {
    max-height: 500px;
    overflow: auto;
  }
  .matching-config {
    max-width: 800px;
    &.show-case {
      max-width: 1000px;
    }
  }
  .matching-overview{
    margin: 16px 96px 0 0;
    color: var(--color-text-regular);
    line-height: 21px;
  }
}
</style>
