<template>
  <show-cont :filter-list='filterList'
    :local-filter-list='localFilterList'
    :queryParams='queryParams'
    :filterTitle='filterTitle'
    :size='size'
    :allClearable='allClearable'
    :disabled='disabled'
    @toggleFilterItemDisable='toggleFilterItemDisable'
    @toggleFilterValueItemDisable='toggleFilterValueItemDisable'
    @on-clear-all='handleClearAll'
    @on-remove-tag='handleRemoveTag'
    @on-change='handleChange' />
</template>

<script lang="ts">
import { StringIsEmpty } from '@/utils/common';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import ShowCont from './components/show-cont.vue';
import { FilterItem, FilterOptionBase, FilterItemLocal, FilterOptionLocal, TagItem } from './types/index.types';
import deepClone from 'lodash/cloneDeep';

@Component({
  components: {
    ShowCont
  },
})
export default class QueryFilter extends Vue {
  @Prop({ default: 'default' }) private size!: 'small'|'default';
  @Prop({ default: {} }) private value!: any;
  @Prop({ default: false }) private updateRoute!: boolean;
  @Prop({ default: () => i18n.t('modules.components.query-filter.components.s_e5f71fc3') as string }) private filterTitle!: string;
  @Prop({ default: () => [] }) private filterList!: FilterItem[];
  @Prop({ default: true }) private allClearable!: boolean;
  @Prop({ default: false }) private disabled!: boolean;

  @Watch('filterList', { deep: true })
  private onFilterListChange (newVal: FilterItem[]) {
    this.queryParams = this.formatValue(this.value);
    this.formatLocalFilterList();
  }
  @Watch('value', { deep: true })
  private onValueChange (newVal: any) {
    this.queryParams = this.formatValue(this.value);
    this.formatLocalFilterList();
  }

  private localFilterList: FilterItemLocal[] = [];
  private queryParams: any = {};
  private backupQueryParams: any = {};

  private mounted () {
    if (this.value) {
      this.backupQueryParams = this.formatValue(this.value);
    }
    this.formatLocalFilterList();
  }

  private formatLocalFilterList () {
    // 需要注意对象里的引用问题导致watch循环触发
    const hasFieldValue = (field: string) => this.value && Object.prototype.hasOwnProperty.call(this.queryParams, field) && !StringIsEmpty(this.queryParams[field])
    this.localFilterList = this.filterList.map((item: FilterItem) => {
      let hasChildSameValue = false;
      const likeableNeedCreateChild: FilterOptionLocal[] = []
      const staticChilds = item.children.map((child: FilterOptionBase) => {
        return {
          ...child,
          showValue: child.showValue ?? child.label,
          checked: child.checked || false,
          disabled: child.disabled || false,
          kv: 'v'
        }
      });

      if (hasFieldValue(item.field)) {
        const qVals = item.multiple ? this.queryParams[item.field] : [this.queryParams[item.field]]
        qVals.forEach((qVal: any) => {
          const targetChild = staticChilds.find((c) => c.value === qVal);
          if (targetChild) {
            targetChild.checked = true;
            hasChildSameValue = true;
          } else if (!targetChild && (item.likeable || item.type === 'input')) {
            likeableNeedCreateChild.push({ label: qVal, value: qVal, showValue: qVal, checked: true, disabled: false, kv: 'v', custom: false });
            hasChildSameValue = true;
          }
        })
      }
      return {
        ...item,
        checked: hasChildSameValue || item.checked || false,
        disabled: hasChildSameValue || item.disabled || false,
        multiple: item.multiple ?? false,
        deletable: item.deletable ?? true,
        likeable: item.likeable ?? false,
        addable: item.addable ?? true,
        editable: item.editable ?? true,
        children: [...staticChilds, ...likeableNeedCreateChild]
      }
    }).filter((item) => item.addable || item.checked) as FilterItemLocal[];
  }

  // 切换FilterItem的选中状态
  private toggleFilterItemDisable ({field, status}: {field: string, status: boolean}) {
    const targetFilterItem = this.localFilterList.find((item) => item.field === field)
    if (targetFilterItem) {
      this.$set(targetFilterItem, 'checked', status);
      this.$set(targetFilterItem, 'disabled', status);
    }
    if (status) {
      this.$emit('field-choose', field)
    }
  }

  // 切换FilterItem下选项的选中状态
  private toggleFilterValueItemDisable ({field, label, status}: {field: string, label: string, status: boolean}) {
    const targetFilterItem = this.localFilterList.find((item) => item.field === field)
    if (targetFilterItem) {
      const targetFilterValueItem = targetFilterItem.children.find((item) => item.label === label)
      if (targetFilterValueItem) {
        this.$set(targetFilterValueItem, 'checked', status);
        this.$set(targetFilterValueItem, 'disabled', status);
      }
    }
  }

  // 清空FilterItem下所有选项的已选状态
  private clearFilterValueDisable (field: string) {
    const targetFilterItem = this.localFilterList.find((item) => item.field === field)
    if (targetFilterItem) {
      targetFilterItem.children.forEach((item) => {
        this.$set(item, 'checked', false);
        this.$set(item, 'disabled', false);
      })
    }
  }

  // 删除FilterItem
  private deleteFilterItem (field: string) {
    const targetFilterItemIdx = this.localFilterList.findIndex((item) => item.field === field)
    if (targetFilterItemIdx !== -1) {
      this.localFilterList.splice(targetFilterItemIdx, 1)
    }
  }

  // 删除tag
  private handleRemoveTag (payload: { field: string, value: any, selected: any[] }) {
    this.toggleFilterItemDisable({ field: payload.field, status: false });
    this.clearFilterValueDisable(payload.field);
    const filterItem = this.localFilterList.find(item => item.field === payload.field)
    if (!filterItem?.addable) {
      this.deleteFilterItem(payload.field);
    }
    if (this.value) {
      this.queryParams[payload.field] = this.backupQueryParams[payload.field]
      if (!filterItem?.addable) {
        delete this.queryParams[payload.field]
      }
      this.$emit('input', { ...this.queryParams })
    }
    const query: any = { ...this.$route.query };
    if (filterItem?.addable && filterItem?.multiple && this.queryParams[payload.field]?.length) {
      query[payload.field] = [...this.queryParams[payload.field]].map(v => encodeURIComponent(v))
    } else {
      delete query[payload.field]
    }
    if (this.updateRoute) {
      this.$router.replace({ query: { ...query } })
    }
    this.$emit('on-remove-tag', {
      ...payload,
      selected: this.formatSelectedOutput(payload.selected || []),
      routerQuery: query,
    });
  }

  // 清空所有
  private handleClearAll () {
    if (this.value) {
      const query = {...this.queryParams}
      for (const qk in query) {
        query[qk] = this.backupQueryParams[qk]
      }
      this.queryParams = query;
      this.$emit('input', { ...this.queryParams })
    }
    const _query = {...this.$route.query};
    for (const qk in _query) {
      delete _query[qk]
    }
    if (this.updateRoute) {
      this.$router.replace({ query: { ..._query } })
    }
    this.formatLocalFilterList()
    this.$emit('on-change', {
      row: null,
      selected: [],
      routerQuery: _query,
    })
  }

  private handleChange ({ row, selected, createSelect, value }: { row: TagItem, selected: TagItem[], createSelect?: boolean, value: string }) {
    if (createSelect && row) {
      // 需要创建自定义项
      const targetFilterItem = this.localFilterList.find((item) => item.field === row.field)
      if (targetFilterItem) {
        targetFilterItem.children.push({
          label: value,
          value,
          custom: true,
          kv: 'v',
          checked: true,
          disabled: true,
          showValue: value,
        })
      }
    }
    const multiple = this.localFilterList.find(item => item.field === row.field)?.multiple || false;
    if (this.value) {
      if (multiple) {
        this.queryParams[row.field] = [...row.value as any]
      } else {
        this.queryParams[row.field] = row.value
      }
      this.$emit('input', { ...this.queryParams })
    }
    const query: any = { ...this.$route.query };
    if (multiple) {
      query[row.field] = [...this.queryParams[row.field]].map(v => encodeURIComponent(v))
    } else {
      query[row.field] = encodeURIComponent(row.value as string)
    }
    if (this.updateRoute) {
      this.$router.replace({ query: { ...query } })
    }
    this.$emit('on-change', {
      row,
      selected: this.formatSelectedOutput(selected),
      routerQuery: query,
    });
  }

  private formatSelectedOutput (selected: TagItem[]) {
    const kvMap: any = {}
    selected.forEach((item) => {
      const { field, value } = item
      kvMap[field] = [
        ...(kvMap[field] || []),
        ...(Array.isArray(value) ? value : [value]),
      ]
    })
    return Object.entries(kvMap).map(([field, value]) => ({ field, value }))
  }

  private formatValue (value: any) {
    const params: any = {};
    this.filterList.forEach(item => {
      const { field, multiple } = item
      const isArray = Array.isArray(value[field])
      if (multiple) {
        params[field] = isArray ? [...value[field]] : [value[field]].filter(t => !StringIsEmpty(t))
      } else {
        const v = isArray ? value[field][0] : value[field]
        params[field] = !StringIsEmpty(v) ? v : ''
      }
    })
    return { ...this.queryParams, ...params };
  }
}
</script>
