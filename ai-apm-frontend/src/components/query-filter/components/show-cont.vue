<template>
  <div class="query-filter-show-cont" :class="disabled ? 'query-filter-disabled' : ''" v-clickout='handleClickout'>
    <div v-show="!tagsList.length" class="query-filter-icon db-icon-search default-text"></div>
    <!-- 显示key || 显示key:value -->
    <div class="query-filter-show-tags">
      <span v-for='tag, idx in tagsList' :key='tag.index'
        @click='reactionTag(idx, tag)'
        :class='["query-filter-show-tag", {
          "query-filter-show-tag-kv": !stringIsEmpty(tag.value),
          "query-filter-show-tag-disable": actionStatus,
          "query-filter-show-tag-no-editable": !tag.editable,
          "query-filter-show-tag-small": size === "small",
        }]'>
        <span :class='["query-filter-show-tag-key", size === "small" ? "font-12" : ""]'>{{ tag.labelKey ? $t(tag.labelKey) : tag.label }}</span>
        <span class="query-filter-show-tag-split mr-5">:</span>
        <span :class='["query-filter-show-tag-value", size === "small" ? "font-12 ell" : ""]' :title='tag.showValue'>{{ tag.showValue }}</span>
        <i class="tag-action-icon db-icon-close" @click.stop="delTag(idx)" v-show='!stringIsEmpty(tag.value) && tag.deletable'></i>
      </span>
    </div>
    <!-- 显示manual输入框 -->
    <div :class='["query-filter-search", actionStatus ? "query-filter-ipt-limit-max-width" : ""]'>
      <el-input v-model='searchModel' @keyup.delete.native="delTag()" ref='searchIpt'
        @input.native="onSearchModelChange"
        @keyup.enter.native="handleSearchModelChange"
        @keyup.esc.native="togglePopover(false)"
        @focus="focusHandle"
        :disabled="disabled"
        maxlength="500"
        :placeholder="tagsList.length ? '' : filterTitle || $t('modules.components.query-filter.components.s_e5f71fc3')"
        :class='["query-filter-no-border-ipt"]'></el-input>
      <div class="query-filter-action-popover" v-show='showPopover'>
        <el-popover placement="bottom" trigger="manual" v-model="showPopover" popper-class="query-filter-action-popover-comp">
          <action-select :filter-list='localFilterList'
            :action-status='actionStatus' :curr-tag='backupCurrTag'
            :search-model='searchModel.toLocaleLowerCase()'
            @on-choose='handleOptionChoose'
            ref='actionSelect' />
        </el-popover>
      </div>
    </div>
    <!-- 操作时分割后的tags -->
    <div class="query-filter-show-tags">
      <span v-for='tag in actionSpliceTagsList' :key='tag.index'
        class="query-filter-show-tag query-filter-show-tag-kv query-filter-show-tag-disable">
        <span>{{ tag.labelKey ? $t(tag.labelKey) : tag.label }}</span>
        <span class="mr-5">:</span>
        <span class="query-filter-show-tag-value">{{ tag.showValue }}</span>
        <i class="tag-action-icon db-icon-close"></i>
      </span>
    </div>

    <!-- 清空筛选 -->
    <span v-show='tagsList.length && allClearable'
      @click="handleClearAll"
      class="query-filter-tags-clear-all">{{ $t('modules.components.query-filter.components.s_1d133031') }}</span>

  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Input} from 'element-ui'
import ActionSelect from './action-select.vue';
import { FilterItemLocal, FilterOptionLocal, TagItem } from '../types/index.types';
import { StringIsEmpty } from '@/utils/common'
import i18n from '@/i18n';
import deepClone from 'lodash/cloneDeep';

@Component({
  components: {
    ActionSelect
  },
})
export default class QueryFilterShowCont extends Vue {
  @Prop({ default: 'default' }) private size!: 'small'|'default';
  @Prop({ default: () => i18n.t('modules.components.query-filter.components.s_e5f71fc3') as string }) private filterTitle!: string;
  @Prop({ default: () => [] }) private filterList!: FilterItemLocal[];
  @Prop({ default: () => [] }) private localFilterList!: FilterItemLocal[];
  @Prop({ default: () => ({}) }) private queryParams!: any;
  @Prop({ default: true }) private allClearable!: boolean;
  @Prop({ default: false }) private disabled!: boolean;

  @Watch('filterList')
  private onFilterListChange (newVal: FilterItemLocal[]) {
    // console.log('show-cont filterList change')
    this.resetHandle()
    this.initTagList()
  }
  @Watch('queryParams', { deep: true })
  private onQueryParamsChange () {
    this.initTagList()
  }

  public $refs!: {
    actionSelect: ActionSelect,
    searchIpt: Input
  }

  private tagsList: TagItem[] = []

  // 当点击已有kv的展示tag时，会显示重新选值的状态，因此将原来的tagsList进行分割，这样无需处理输入组件的位置，分割后的tags放在输入组件后显示
  // remind: 任何操作后，都需要还原actionSpliceTagsList到tagsList
  private actionSpliceTagsList: TagItem[] = [];

  // 输入框的值
  private searchModel = ''
  // 是否为输入状态
  private searchStatus = false;
  // 清楚输入框的定时器，用于输入清空后延迟可删除tags状态
  private searchTimer: any = null;

  // 是否为操作状态
  private actionStatus = false;
  // 当前操作的field
  private actionField = ''

  // 展示输入框的popover
  private showPopover = false;

  // 当前操作的tag的备份，用于还原
  private backupCurrTag: { index: number, tag: TagItem }|null = null

  get actionType () {
    const targetFilterItem = this.localFilterList.find((item) => item.field === this.actionField);
    return targetFilterItem && targetFilterItem.type || ''
  }

  private created () {
    this.initTagList()
  }

  private initTagList () {
    // unknown effect
    // 根据localFilterList每项children中的checked:true初始化tagsList
    const checkedList: TagItem[] = [];
    let actionIndex = -1;
    this.localFilterList.forEach((item, index) => {
      const _children = item.children.filter((child) => child.checked)
      if (_children.length) {
        const firstChild = _children[0]
        checkedList.push({
          key: item.field,
          label: item.label,
          value: item.multiple ? _children.map((child) => child.value) : firstChild.value,
          showValue: item.multiple ? _children.map((child) => child.showValue).join('、') : firstChild.showValue,
          field: item.field,
          type: item.type,
          info: item.multiple ? {} : firstChild.info || {},
          deletable: item.deletable,
          addable: item.addable,
          editable: item.editable,
          multiple: item.multiple,
        })
      } else if (item.checked) {
        checkedList.push({
          key: `${item.field}`,
          label: item.label,
          value: null,
          showValue: null,
          field: `${item.field}`,
          type: item.type,
          info: item.info || {},
          deletable: item.deletable,
          addable: item.addable,
          editable: item.editable,
          multiple: item.multiple,
        })
        actionIndex = checkedList.length - 1;
      }
    })

    this.tagsList = checkedList
    if (actionIndex > -1) {
      this.$nextTick(() => {
        this.reactionTag(actionIndex, this.tagsList[actionIndex])
      })
    }
  }

  private beforeDestroy () {
    window.clearTimeout(this.searchTimer)
    this.searchTimer = null
  }

  private stringIsEmpty (unknown: any) {
    return StringIsEmpty(unknown)
  }

  private handleClickout () {
    // 当操作未完成时
    // 当操作完成时，还原分割的tags
    // if (this.actionField) {
    //   //
    // } else {
    this.restoreTagWhenActionDone()
    // }
    this.togglePopover(false)
    // console.log('click outside')
    const searchIpt = this.$refs.searchIpt.$el.querySelector('input');
    if (searchIpt) {
      // 取消focus
      // 延迟重新focus，解决某些浏览器无法触发focus的问题
      this.$nextTick(() => {
        searchIpt.blur()
      })
    }
  }

  private onSearchModelChange (event: InputEvent) {
    const { data } = event;
    if (!data && !this.searchModel.length) {
      window.clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        this.searchStatus = false
      }, 500)
    } else {
      if (this.searchStatus !== true) {
        this.searchStatus = true
      }
      if (this.showPopover !== true) {
        this.togglePopover(true)
      }
    }
  }

  private handleSearchModelChange () {
    const isCreateSelectModel = this.actionType === 'select' && this.searchModel.length && this.$refs.actionSelect.currFilterItem && this.$refs.actionSelect.currFilterItem.likeable;
    if ((this.actionType === 'input' && this.searchModel.length) || isCreateSelectModel ) {
      let tagItem = this.tagsList[this.tagsList.length - 1]
      const multiple = tagItem.multiple
      if (!multiple) {
        this.$set(tagItem, 'value', this.searchModel)
        this.$set(tagItem, 'showValue', this.searchModel)
      } else {
        const targetFilterItem = this.localFilterList.find((item) => item.field === tagItem.field) as FilterItemLocal
        const _children = targetFilterItem.children.filter((child) => child.checked)
        tagItem = {
          ...deepClone(tagItem),
          value: [..._children.map((child) => child.value), this.searchModel],
          showValue: [..._children.map((child) => child.showValue), this.searchModel].join('、'),
        }
      }
      this.backupCurrTag = null
      this.actionField = ''
      this.searchModel = ''
      this.actionStatus = false
      this.searchStatus = false
      this.restoreTagWhenActionDone()
      this.togglePopover(true)
      this.$emit('on-change', {
        row: { ...tagItem },
        selected: !multiple ? this.tagsList : [...this.tagsList.slice(0, -1), tagItem],
        createSelect: isCreateSelectModel,
        value: this.searchModel,
      });
    }
  }

  private reactionTag (index: number, tag: TagItem) {
    if (this.actionStatus || !tag.editable) {
      return
    }
    this.actionStatus = true
    this.searchModel = ''
    this.searchStatus = false
    const currTag = this.tagsList[index]
    const { field } = currTag
    this.backupCurrTag = {
      index,
      tag: currTag,
    }
    this.$set(this.tagsList, index, {
      ...currTag,
      value: null,
      showValue: null,
    })
    this.spliceTagWhenAction(index)
    this.actionField = field
    this.togglePopover(true)
  }

  private delTag (index?: number) {
    if (!this.tagsList.length) {
      return
    }
    if (this.searchStatus) {
      return
    }
    // 操作状态下只能删除当前操作的tag，其余tag不可操作
    if (this.actionStatus && this.backupCurrTag && typeof index === 'number') {
      return
    }
    // 操作状态下，当前tag属于不可删除类型，则跳出
    if (this.actionStatus && this.backupCurrTag && typeof index !== 'number' && !this.backupCurrTag.tag.deletable) {
      return
    }
    // index为number时，删除指定tag；index为undefined时，删除最后一个tag
    const target = typeof index === 'number' ? this.tagsList[index] : this.tagsList.slice(-1)[0];
    if (!target.deletable) {
      return
    }
    if (typeof index === 'number') {
      this.tagsList.splice(index, 1);
    } else {
      this.tagsList.pop();
    }
    const { field, value } = target;
    this.$emit('on-remove-tag', { field, value, selected: [...this.tagsList] });

    if (this.backupCurrTag) {
      this.backupCurrTag = null
    }
    if (this.actionField) {
      this.actionField = ''
    }
    if (this.actionStatus) {
      this.restoreTagWhenActionDone()
    }
    this.togglePopover(true)
  }

  private spliceTagWhenAction (index: number) {
    const spliceTags = this.tagsList.splice(index + 1)
    this.actionSpliceTagsList = Array.from(spliceTags)
  }
  private restoreTagWhenActionDone () {
    const { index, tag } = this.backupCurrTag || {}
    if (typeof index === 'number' && tag) {
      const matchTag = this.tagsList[index]
      if (matchTag.key === tag.key) {
        const multiple = tag.multiple
        const targetFilterItem = this.localFilterList.find((item) => item.field === tag.field) as FilterItemLocal
        const _children = targetFilterItem.children.filter((child) => child.checked)
        if (!multiple && !StringIsEmpty(tag.value)) {
          this.$set(this.tagsList, index, tag)
        } else if (multiple && _children.length) {
          this.$set(matchTag, 'value', _children.map((child) => child.value))
          this.$set(matchTag, 'showValue', _children.map((child) => child.showValue).join('、'))
        } else {
          this.tagsList.splice(index, 1)
          this.$emit('toggleFilterItemDisable', { field: tag.field, status: false });
        }
      }
    }
    this.tagsList = this.tagsList.concat(this.actionSpliceTagsList)
    this.$nextTick(() => {
      this.actionSpliceTagsList = []
      this.backupCurrTag = null
      this.searchModel = ''
      this.searchStatus = false
      this.actionStatus = false
      const [lastTag] = this.tagsList.slice(-1)
      if (lastTag && StringIsEmpty(lastTag.value)) {
        this.actionField = lastTag.field
      } else {
        this.actionField = ''
      }
    })
  }

  private togglePopover (status?: boolean) {
    // if (status === true && this.showPopover === true) {
    //   return
    // }
    // 如果是input类型，则跳出
    if (this.actionType === 'input' && status === true) {
      this.showPopover = false
      this.manualInput()
      return
    }
    if (typeof status === 'boolean') {
      this.showPopover = status
    } else {
      this.showPopover = !this.showPopover
    }
    if (this.showPopover === true) {
      // 输入框触发焦点
      this.manualInput()
    }
  }

  private manualInput () {
    // 手动触发将光标至于输入框内
    if (this.$refs.searchIpt) {
      const searchIpt = this.$refs.searchIpt.$el.querySelector('input');
      if (searchIpt) {
        searchIpt.focus()
      }
    }
  }

  private handleOptionChoose (row: FilterOptionLocal) {
    const { label, value, kv, info } = row
    if (kv === 'k') {
      const targetFilterItem = this.localFilterList.find(i => i.field === value)
      this.tagsList.push({
        key: `${value}`,
        label,
        value: null,
        showValue: null,
        field: `${value}`,
        type: 'select',
        info: info || {},
        deletable: targetFilterItem!.deletable ?? true,
        addable: targetFilterItem!.addable ?? true,
        editable: targetFilterItem!.editable ?? true,
        multiple: targetFilterItem!.multiple ?? false,
      })
      this.backupCurrTag = {
        index: this.tagsList.length - 1,
        tag: this.tagsList[this.tagsList.length - 1],
      }
      this.actionStatus = true
      this.actionField = `${value}`
      this.$emit('toggleFilterItemDisable', { field: `${value}`, status: true });
      this.searchModel = ''
      this.searchStatus = false
      this.$nextTick(() => {
        this.togglePopover(true)
      })
    } else {
      let lastTag = this.tagsList.slice(-1)[0]
      const { index, tag } = this.backupCurrTag || {}
      const multiple = lastTag.multiple
      if (!multiple) {
        lastTag.value = value
        lastTag.showValue = label
        lastTag.info = info || {}
        if (tag && !StringIsEmpty(tag.value) && tag.value !== value) {
          // 修改状态，将上次的值还原可选状态
          this.$emit('toggleFilterValueItemDisable', {
            field: tag.field,
            label: `${tag.showValue}`,
            status: false,
          })
        }
        this.$emit('toggleFilterValueItemDisable', {
          field: lastTag.field,
          label,
          status: true,
        })
      } else {
        const targetFilterItem = this.localFilterList.find((item) => item.field === lastTag.field) as FilterItemLocal
        this.$emit('toggleFilterValueItemDisable', {
          field: lastTag.field,
          label,
          status: !targetFilterItem.children.find((child) => child.value === value && child.checked),
        })
        const _children = targetFilterItem.children.filter((child) => child.checked)
        lastTag = {
          ...deepClone(lastTag),
          value: _children.map((child) => child.value),
          showValue: _children.map((child) => child.showValue).join('、'),
          info: {},
        }
      }
      this.backupCurrTag = null
      this.actionStatus = false
      this.actionField = ''
      this.restoreTagWhenActionDone()
      this.$emit('on-change', {
        row: lastTag,
        selected: [...this.tagsList.slice(0, -1), lastTag]
      })
      this.searchModel = ''
      this.searchStatus = false
      this.$nextTick(() => {
        this.togglePopover(true)
      })
    }
  }

  private handleClearAll () {
    this.resetHandle()
    this.togglePopover(false)
    this.$emit('on-clear-all')
  }

  private resetHandle () {
    this.tagsList = []
    this.actionSpliceTagsList = []
    this.backupCurrTag = null
    this.searchModel = ''
    this.searchStatus = false
    this.actionField = ''
    this.actionStatus = false
  }

  private focusHandle () {
    // if (this.actionType === 'input') {
    //   this.manualInput()
    // }
    this.togglePopover(true);
  }
}
</script>

<style lang='scss'>
$line-height: 26px;
.query-filter-show-cont {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  border: 1px solid var(--border-color-base);
  background: var(--bg-color);
  border-radius: 4px;
  padding: 2px 60px 0 2px;
  color: var(--color-text-primary);
  position: relative;

  .query-filter-icon {
    flex: none;
    box-sizing: border-box;
    width: 28px;
    height: $line-height;
    padding: 0 7px 0 5px;
    margin-bottom: 2px;
    text-align: center;
    line-height: $line-height;
    font-size: 14px;
  }

  .query-filter-show-tags {
    display: contents;
    .query-filter-show-tag {
      box-sizing: border-box;
      margin: 0 6px 2px 0;
      padding-left: 8px;
      height: $line-height;
      display: flex;
      align-items: center;
      position: relative;
      border-radius: 2px;
      transition: border-color .3s ease, background-color .3s ease;
      font-size: 12px;

      &.query-filter-show-tag-small {
        max-width: 280px;
      }
      
      .query-filter-show-tag-value {
        max-width: 400px;
        text-overflow: ellipsis;
        white-space: nowrap;
        overflow: hidden;
      }

      .tag-action-icon {
        height: 16px;
        width: 16px;
        font-size: 12px;
        font-weight: bold;
        position: absolute;
        right: 6px;
        border-radius: 50%;
        line-height: 16px;
        vertical-align: middle;
        text-align: center;
        transform: scale(0.8);
        transition: background-color .3s ease, color .3s ease;
        opacity: 0.6;
      }

      &.query-filter-show-tag-kv {
        background-color: var(--background-color-base);
        padding-right: 28px;
        cursor: pointer;

        &:not(.query-filter-show-tag-disable) .tag-action-icon:hover {
          opacity: 1;
        }
      }
      &:not(.query-filter-show-tag-kv) {
        margin-right: 0;
        .query-filter-show-tag-key,
        .query-filter-show-tag-split {
          color: var(--color-text-secondary);
        }
      }
      &.query-filter-show-tag-no-editable {
        cursor: default;
        .tag-action-icon {
          cursor: pointer;
        }
      }
      &.query-filter-show-tag-disable {
        cursor: not-allowed;
        .tag-action-icon {
          cursor: not-allowed;
        }
      }

      .query-filter-show-tag-key {
        flex: none;
      }
    }
  }

  .query-filter-search {
    flex: 1;
    margin-bottom: 2px;
    position: relative;
  }

  &:hover .query-filter-tags-clear-all {
    display: block;
  }
}
.query-filter-tags-clear-all {
  font-size: 12px;
  line-height: 24px;
  color: var(--color-primary);
  transform: translate(0, -50%);
  cursor: pointer;
  position: absolute;
  top: 50%;
  right: 10px;
}
.query-filter-no-border-ipt {
  min-width: 100px;
}
.query-filter-ipt-limit-max-width {
  max-width: 300px;
}
.query-filter-no-border-ipt > input {
  display: block;
  border: none;
  outline: none;
  background: transparent;
  padding: 0;
  margin: 0;
  height: $line-height;
  line-height: $line-height;
  color: var(--color-text-primary);
}
.query-filter-action-popover-comp {
  padding: 0;
  z-index: 998;
  border: none;
}
.query-filter-disabled {
  cursor: not-allowed;
  background-color: var(--background-color-base);
  border-color: var(--border-color-light);
  color: var(--color-text-placeholder);
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
  }
}
</style>
