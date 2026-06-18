<template>
  <div class="matching-criteria-wrapper" :key="criteriaData.uuid">
    <div v-if="!hidden" class="andor-box flex-h">
      <scroll-select
        v-model="criteriaData.andor"
        :options="andorList"
        :disabled="singleModel"
        :clearable="false"
        :showTitle="true"
        class="criteria-select small" />
      <span
        @click="addCriteriaHandle(criteriaData.fieldList)"
        class="action-btn"><i class="db-icon-add"></i>{{ addText }}</span>
      <span
        v-if="criteriaData.level < maxLevel && showSubConfig"
        @click="addSubCriteriaHandle(criteriaData.fieldList, criteriaData.level)"
        class="action-btn"><i class="db-icon-add"></i>{{ addMultipleText }}</span>
      <span
        v-if="criteriaData.level !== 1"
        @click="deleteCriteriaGroupHandle()"
        class="action-btn">{{ delText }}</span>
    </div>

    <div class="ml-10">
      <template v-for="(item, index) in criteriaData.fieldList">
        <div :key="item.uuid" class="criteria-item flex-h connect-line" v-if="!hidden && !Array.isArray(item.fieldList)">
          <!-- key -->
          <span class="criteria-key">{{ uuidCharMapping[item.uuid] || '' }}</span>

          <!-- 字段 -->
          <scroll-select
            v-model="item.field"
            @change="fieldChangeHandle(item)"
            :allowCreate="allowCreateKey"
            :options="fieldList"
            :clearable="true"
            :showTitle="true"
            :class="{ error: (criteriaError[item.uuid] || {}).field }"
            class="criteria-select" />

          <!-- 运算符 -->
          <scroll-select
            v-model="item.symbol"
            @change="symbolChangeHandle(item)"
            :options="
              !((fieldData[item.field] || {}).symbols || []).length ?
                symbolList :
                symbolList.filter(t => ((fieldData[item.field] || {}).symbols || []).includes(t.value))
            "
            :clearable="true"
            :showTitle="true"
            :class="{ error: (criteriaError[item.uuid] || {}).symbol }"
            class="criteria-select" />

          <!-- 是否不区分大小写 -->
          <el-select
            v-if="showCase && showValue && !emptySymbols.includes(item.symbol)"
            v-model="item.caseInsensitive"
            filterable size="small" clearable
            :class="{ error: !!(criteriaError[item.uuid] || {}).caseInsensitive }"
            class="criteria-select">
            <el-option
              v-for="t in caseList"
              :key="t.value"
              :label="t.label"
              :value="t.value"
            ></el-option>
          </el-select>

          <!-- 取值 -->
          <!-- 有value项，且运算符非emptySymbols -->
          <template v-if="showValue && !emptySymbols.includes(item.symbol)">
            <!-- 运算符支持选择框，并且 options 存在 -->
            <scroll-select
              v-if="selectSymbols.includes(item.symbol) && ((fieldData[item.field] || {}).options || []).length"
              v-model="item.value"
              :key="`value-select-${item.symbol}-${item.uuid}`"
              :options="(fieldData[item.field] || {}).options"
              :multiple="multipleSymbols.includes(item.symbol)"
              :allowCreate="allowCreateOption"
              :showTitle="true"
              :class="{ error: (criteriaError[item.uuid] || {}).value }"
              class="criteria-select big"
            ></scroll-select>

            <!-- 长文本 -->
            <el-input
              v-else-if="multipleSymbols.includes(item.symbol)"
              v-model="item.value"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 5 }"
              :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')" size="small"
              :class="{ error: (criteriaError[item.uuid] || {}).value }"
              class="criteria-area big"
            />

            <!-- 默认 -->
            <el-input
              v-else
              v-model="item.value"
              :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')" size="small"
              :class="{ error: (criteriaError[item.uuid] || {}).value }"
              class="criteria-ipt big"
            />
          </template>

          <span
            v-show='!atLeastOne || (atLeastOne && criteriaData.fieldList.length > 1)'
            @click="deleteCriteriaHandle(criteriaData.fieldList, index)"
            class="delete-btn"><i class="db-icon-minus font-12"></i></span>
        </div>

        <matching-criteria-item
          v-else-if="criteriaData.level < maxLevel && (item.fieldList || []).length"
          :key="item.uuid"
          :criteriaData="item"
          :fieldData="fieldData"
          :andorList="andorList"
          :symbolList="symbolList"
          :caseList="caseList"
          :fieldList="fieldList"
          :criteriaList="criteriaData.fieldList"
          :criteriaIndex="index"
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
          class="connect-line"
        />
      </template>
    </div>
  </div>
</template>

<script lang='ts'>
import { Vue, Component, Prop } from 'vue-property-decorator'
import i18n from '@/i18n';
import { v4 as uuidv4 } from 'uuid';
import type { OptionItem, SymbolItem, FieldData, ErrorData, UuidCharMapping,
  CriteriaData, FieldItem, FieldList } from './index.types'

@Component({
  name: 'MatchingCriteriaItem'
})
export default class MatchingCriteriaItem extends Vue {
  @Prop({ default: () => ({}) }) private criteriaData!: CriteriaData
  @Prop({ default: () => ({}) }) private fieldData!: FieldData
  @Prop({ default: () => [] }) private andorList!: OptionItem[]
  @Prop({ default: () => [] }) private symbolList!: SymbolItem[]
  @Prop({ default: () => [] }) private caseList!: SymbolItem[]
  @Prop({ default: () => [] }) private fieldList!: OptionItem[]
  @Prop({ default: () => ({}) }) private criteriaError!: ErrorData
  @Prop({ default: () => ({}) }) private uuidCharMapping!: UuidCharMapping
  @Prop({ default: 3 }) private maxLevel!: number
  @Prop({ default: () => [] }) private criteriaList!: FieldList
  @Prop({ default: 0 }) private criteriaIndex!: number
  @Prop({ default: () => i18n.t('modules.components.matching-criteria.s_69fbb2e5') as string }) private addText!: string
  @Prop({ default: () => i18n.t('modules.components.matching-criteria.s_45fea638') as string }) private addMultipleText!: string
  @Prop({ default: () => i18n.t('modules.components.matching-criteria.s_2f4aaddd') as string }) private delText!: string
  @Prop({ default: false }) private hidden!: boolean    // 是否隐藏组件
  @Prop({ default: true }) private showSubConfig!: boolean // 是否展示添加子条件
  @Prop({ default: false }) private singleModel!: boolean // 是否为单条件模式 - TODO：单条件初始为or还是and
  @Prop({ default: false }) private atLeastOne!: boolean // 条件是否至少有一个，至少一个时，删除按钮不可删除
  @Prop({ default: false }) private showCase!: boolean  // 是否显示大小写选择框
  @Prop({ default: false }) private allowCreateOption!: boolean // Select是否允许创建选项
  @Prop({ default: false }) private allowCreateKey!: boolean // Select是否允许创建选项

  get emptySymbols () {
    return this.symbolList.filter(t => t.empty).map(t => t.value)
  }
  get multipleSymbols () {
    return this.symbolList.filter(t => t.multiple).map(t => t.value)
  }
  get selectSymbols () {
    return this.symbolList.filter(t => t.type === 'select').map(t => t.value)
  }

  get showValue () {
    return !this.symbolList.length || this.symbolList.length !== this.emptySymbols.length
  }

  private created () {
    //
  }

  // 添加条件
  private addCriteriaHandle (list: FieldList) {
    const uuid = uuidv4()
    list.push({ field: '', symbol: '', value: '', caseInsensitive: true, uuid })
  }

  // 添加子条件
  private addSubCriteriaHandle (list: FieldList, level: number) {
    const uuid = uuidv4()
    const defaultAndor = this.andorList.length ? this.andorList[0].value : ''
    list.push({
      uuid,
      level: level + 1,
      andor: defaultAndor,
      fieldList: [
        { field: '', symbol: '', value: '', caseInsensitive: true, uuid }
      ]
    })
  }

  // 删除条件组
  private deleteCriteriaGroupHandle () {
    this.criteriaList.splice(this.criteriaIndex, 1)
  }

  // 删除条件
  private deleteCriteriaHandle (list: FieldList[], index: number) {
    if (this.criteriaList && this.criteriaList.length && list.length === 1) {
      this.criteriaList.splice(this.criteriaIndex, 1)
    } else {
      list.splice(index, 1)
    }
  }

  // 字段改变
  private fieldChangeHandle (item: FieldItem) {
    item.value = ''
    if (!item.symbol) {
      return
    }
    const hasOptions = !!((this.fieldData[item.field] || {}).options || []).length
    const symbols = (this.fieldData[item.field] || {}).symbols || []
    if (symbols.length && !symbols.includes(item.symbol)) {
      item.symbol = ''
    } else if (hasOptions) {
      const isSelect = this.selectSymbols.includes(item.symbol)
      const isMulti = this.multipleSymbols.includes(item.symbol)
      if (!symbols.length && !isSelect) {
        item.symbol = ''
      } else if (isSelect && isMulti) {
        item.value = []
      }
    }
  }

  // 运算符改变
  private symbolChangeHandle (item: FieldItem) {
    const options: any[] = ((this.fieldData[item.field] || {}).options || []).map((t) => {
      const isOptionItem = Object.prototype.toString.call(t) === '[object Object]'
      return isOptionItem ? (t as any).value : t
    })
    const isSelect = this.selectSymbols.includes(item.symbol)
    const isMulti = this.multipleSymbols.includes(item.symbol)
    if (this.emptySymbols.includes(item.symbol)) {
      item.value = ''
    } else if (options.length) {
      const isArr = Array.isArray(item.value)
      if (!isArr && !options.includes(item.value)) {
        item.value = ''
      }
      if (!isMulti) {
        item.value = !isArr ? item.value : (item.value as any).length ? (item.value as any)[0] : ''
      } else {
        item.value = (isArr ? item.value : item.value !== '' ? [item.value] : []) as any
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.matching-criteria-wrapper {
  .criteria-ipt,
  .criteria-area,
  .criteria-select {
    vertical-align: top;
    margin-right: 10px;
    width: 138px;
    &.big {
      flex: 1;
    }
    &.small {
      width: 80px;
    }
    &.error {
      :deep(.el-input__inner),
      :deep(.el-textarea__inner) {
        border-color: var(--color-danger);
      }
    }
  }
  :deep(.el-input__inner),
  :deep(.el-textarea__inner) {
    padding-left: 10px;
    padding-right: 10px;
  }
  :deep(.el-select .el-input__inner) {
    padding-right: 25px;
  }
  .criteria-area {
    :deep(.el-textarea__inner) {
      min-height: 32px;
      max-height: 115px;
    }
  }
  .andor-box,
  .criteria-item {
    margin-bottom: 10px;
    white-space: nowrap;
  }
  .action-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    min-width: 46px;
    padding: 0 5px;
    color: var(--color-text-link);
    border: 1px solid var(--border-color-base);
    border-radius: 4px;
    line-height: 26px;
    cursor: pointer;
    &+.action-btn{
      margin-left: 10px;
    }
  }
  .delete-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    width: 20px;
    height: 20px;
    border: 1px solid var(--border-color-base);
    border-radius: 4px;
    cursor: pointer;
  }

  .criteria-key {
    box-sizing: border-box;
    display: inline-block;
    vertical-align: top;
    min-width: 32px;
    height: 32px;
    margin-right: 10px;
    padding: 0 4px;
    border: 1px solid var(--border-color-base);
    border-radius: 20px;
    background: var(--bg-color03);
    text-align: center;
    line-height: 30px;
  }

  .connect-line {
    padding-left: 20px;
    position: relative;
    &:first-child::before,
    &::after {
      content: '';
      width: 20px;
      box-sizing: border-box;
      border-left: 1px solid var(--border-color-base);
      border-bottom: 1px solid var(--border-color-base);
      position: absolute;
      left: 0;
    }
    &:first-child::before {
      height: 26px;
      top: -10px;
    }
    &::after {
      height: calc(100% + 10px);
      top: 15px;
    }
    &:last-child::after {
      display: none;
    }
  }
}
</style>
