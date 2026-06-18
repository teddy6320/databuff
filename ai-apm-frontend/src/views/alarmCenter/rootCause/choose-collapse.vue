<template>
  <div :class="['choose-collapse flex-v', collapsed ? 'is-collapsed' : '']">
    <div class="choose-collapse-header">
      <div class="choose-collapse-title font-13 flex-h">{{ $t('modules.views.alarmCenter.alarm.s_c0fd0276') }}</div>
      <span
        @click="toggleCollapsed"
        :class="collapsed ? 'db-icon-unfold' : 'db-icon-fold'"
        class="cp font-12 choose-collapse-btn flex-h-cc"></span>
    </div>

    <div class="choose-collapse-body">
      <simplebar style="height: 100%;padding-right: 12px;">
        <el-collapse v-model="activeNames" class="filter-collapse">
          <el-collapse-item
            v-for="item in filterList"
            :key="item.name"
            :title="item.title"
            :name="item.name">
            <simplebar v-if="item.children.length" style="max-height: 200px;">
              <el-radio-group
                v-if="!item.multiple"
                v-model="item.model"
                @change="changeHandle">
                <el-radio
                  v-for="t in item.children"
                  :key="t.value"
                  :label="t.value"
                  :disabled="t.disabled"
                  class="filter-radio">{{ t.labelKey ? $t(t.labelKey) : t.label }}</el-radio>
              </el-radio-group>
              <el-checkbox-group
                v-else
                v-model="item.model"
                @change="changeHandle">
                <el-checkbox
                  v-for="t in item.children"
                  :key="t.value"
                  :label="t.value"
                  :disabled="t.disabled"
                  class="filter-checkbox">{{ t.labelKey ? $t(t.labelKey) : t.label }}</el-checkbox>
              </el-checkbox-group>
            </simplebar>
            <div v-else class="describe" style="margin-left:30px">{{ $t('modules.views.alarmCenter.alarm.s_d81bb206') }}</div>
          </el-collapse-item>
        </el-collapse>
      </simplebar>

      <div v-if="!filterList.length" class="empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import i18n from '@/i18n';
import Simplebar from 'simplebar-vue'
import { orderBy } from 'lodash';
import { StringIsEmpty } from '@/utils/common';

interface FilterType {
  label: string;        // 类型中文名
  value: string;        // 查询时用的参数名
  field?: string;       // 获取的筛选数据对应的字段，默认同value
  multiple?: boolean;   // 是否为多选
  valueType?: string;   // 值类型 默认string
  collapsed?: boolean;  // 是否折叠 默认false
}

@Component({
  components: {
    Simplebar: Simplebar as any,
  },
})
export default class ChooseCollapse extends Vue {
  @Prop({ default: () => ({}) }) private filterData!: any;

  private collapsed = false;

  private activeNames: string[] = []; // 默认展开项

  private filterTypes: FilterType[] = [
    { label: i18n.t('modules.views.alarmCenter.rootCause.s_d5a57c1c') as string, labelKey: 'modules.views.alarmCenter.problemAnalysis.s_d5a57c1c', value: 'rootCauseTypes', field: 'rootCauseType', multiple: true },
    { label: i18n.t('modules.views.alarmCenter.rootCause.s_ec46bb5e') as string, labelKey: 'modules.views.alarmCenter.problemAnalysis.s_ec46bb5e', value: 'rootCauseNodes', field: 'rootCauseNode', multiple: true },
  ];

  private filterList: any[] = []

  private filterToggleTimer: any = null

  private created () {
    this.activeNames = this.filterTypes.filter(t => t.collapsed !== true).map(t => t.value)
  }

  private beforeDestroy() {
    if (this.filterToggleTimer) {
      window.clearTimeout(this.filterToggleTimer);
      this.filterToggleTimer = null;
    }
  }

  public async init () {
    this.filterList = []
    await this.getParams()

    // 搜索参数回显
    const routerQuery = this.$route.query
    this.filterList.forEach(item => {
      const { name, multiple, children, valueType } = item
      if (routerQuery[name]) {
        const isNumber = valueType === 'number'
        const isArrayValue = Array.isArray(routerQuery[name])
        const values = (isArrayValue ? routerQuery[name] as string[] : [routerQuery[name] as string]).filter(v => v).map(v => {
          return isNumber ? +v : decodeURIComponent(v)
        });
        if (multiple) {
          item.model = values.filter(v => !!children.find((t: any) => t.value === v))
        } else {
          item.model = children.find((t: any) => t.value === values[0]) ? values[0] : ''
        }
      } else {
        item.model = multiple ? [] : ''
      }
    })

    const _params: any = {}
    this.filterList.forEach(t => {
      const { name, multiple, model } = t
      if ((!multiple && model) || (multiple && model.length)) {
        _params[name] = multiple ? [...model] : model
      }
    })
    return { ..._params }
  }

  private async getParams () {
    const data = { ...this.filterData }
    const filterList: any[] = [];
    this.filterTypes.forEach((t) => {
      const field = t.field || t.value
      let values = (data[field] || []).filter((v: any) => !StringIsEmpty(v))
      if (t.valueType !== 'number') {
        values = orderBy(values, [v => String(v || '').toLocaleLowerCase()], ['asc'])
      } else {
        values = orderBy(values, [v => +v], ['asc'])
      }
      const children = values.map((v: any) => ({ label: v, value: v }));
      if (!t.multiple) {
        children.unshift({ label: i18n.t('modules.views.alarmCenter.rootCause.s_a8b0c204') as string, labelKey: 'modules.views.alarmCenter.rootCause.s_a8b0c204', value: '' })
      }
      filterList.push({
        ...t,
        name: t.value,
        title: t.label,
        field,
        multiple: !!t.multiple,
        valueType: t.valueType || 'string',
        model: t.multiple ? [] : '',
        children,
      })
    });
    this.filterList = filterList
  }

  private changeHandle () {
    const _params: any = {}
    this.filterList.forEach(t => {
      const { name, multiple, model } = t
      if ((!multiple && model) || (multiple && model.length)) {
        _params[name] = multiple ? [...model] : model
      }
    })
    this.$emit('on-filter-change', { ..._params })

    // 更新路由中的参数
    const _query = { ...this.$route.query }
    this.filterList.forEach(t => {
      const { name, multiple } = t
      if (!StringIsEmpty(_params[name])) {
        _query[name] = multiple ? [..._params[t.name]].map(v => encodeURIComponent(v)) : encodeURIComponent(_params[t.name])
      } else {
        delete _query[name]
      }
    })
    if (JSON.stringify(_query) !== JSON.stringify(this.$route.query)) {
      this.$router.replace({ query: { ..._query } })
    }
  }

  private toggleCollapsed () {
    this.collapsed = !this.collapsed;
    this.filterToggleTimer = setTimeout(() => {
      this.$emit('on-filter-toggle', this.collapsed)
    }, 300);
  }
}
</script>

<style lang="scss" scoped>
.choose-collapse {
  width: 188px;
  height: 100%;
  padding-right: 20px;
  transition: margin .3s ease;
  color: var(--color-text-primary);

  &.is-collapsed {
    margin-left: -188px;
    .choose-collapse-header .choose-collapse-btn {
      transform: translateX(17px);
      box-shadow: 0px 1px 4px 0px rgba(139, 142, 147, 0.3);
    }
  }

  .choose-collapse-header {
    height: 36px;
    position: relative;
    .choose-collapse-title {
      height: 100%;
      border-top: 1px solid var(--border-color-base);
      border-bottom: 1px solid var(--border-color-base);
    }
    .choose-collapse-btn {
      width: 17px;
      height: 24px;
      background: var(--bg-color);
      border-radius: 0 4px 4px 0;
      box-shadow: 0px 1px 4px 0px rgba(139, 142, 147, 0);
      transition: transform .3s ease, box-shadow .3s ease;
      position: absolute;
      top: 6px;
      right: 0;
    }
  }

  .choose-collapse-body {
    flex: 1;
    overflow: hidden;
    position: relative;

    .empty {
      padding-right: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      position: absolute;
      top: 0;
      left: 0;
    }
  }

  .filter-collapse {
    border: none;

    :deep(.el-collapse-item) {
      padding: 2px 0;
      &:not(:last-child) {
        border-bottom: 1px solid var(--border-color-base);
      }
    }
    :deep(.el-collapse-item__header) {
      display: block;
      padding-left: 20px;
      padding-right: 8px;
      height: 28px;
      line-height: 28px;
      background-color: transparent;
      border: none;
      border-radius: 3px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      position: relative;
      font-size: 12px;
      font-weight: normal;
      .el-collapse-item__arrow {
        position: absolute;
        top: 7px;
        left: 0;
      }
      &:hover {
        background-color: var(--bg-color03);
      }
    }
    :deep(.el-collapse-item__wrap) {
      background-color: transparent;
      border: none;
      .el-collapse-item__content {
        padding-bottom: 0;
      }
    }
    :deep(.el-checkbox-group),
    :deep(.el-radio-group) {
      width: 100%;
    }

    .filter-input {
      :deep(.el-input__inner) {
        border: none;
      }
    }

    .filter-checkbox,
    .filter-radio {
      margin: 2px 0;
      padding: 3px 0;
      min-height: 24px;
      border-radius: 3px;
      display: flex;
      align-items: center;
      line-height: 20px;
      color: var(--color-text-regular);
      font-weight: normal;
      &:hover {
        background-color: var(--bg-color03);
      }
      :deep(.el-checkbox__inner),
      :deep(.el-radio__inner) {
        display: block;
      }
      :deep(.el-checkbox__label),
      :deep(.el-radio__label) {
        font-size: 12px;
      }
      :deep(.el-checkbox__input.is-checked+.el-checkbox__label),
      :deep(.el-radio__input.is-checked+.el-radio__label) {
        color: inherit;
      }
    }
  }
}
</style>
