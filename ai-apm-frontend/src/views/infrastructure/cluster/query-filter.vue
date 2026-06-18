<template>
  <el-popover
    v-model="popoverVisible"
    @after-leave="hideFilterPopoverHandle"
    trigger="click"
    width="500"
    placement="bottom-start"
    popper-class="k8s-group-popover-comp">
    <div slot="reference">
      <div @click.stop="() => null" class="query-filter-wrapper flex-h db-icon-search">
        <div v-if="selectedTags.length" class="multi-filter-cont">
          <div
            v-for="tag, tidx in selectedTags"
            :key="tag.value"
            @click="toggleFilterPopoverHandle(tag)"
            class="filter-tag">
            <span>{{ tag.typeLabel }}：{{ tag.labelKey ? $t(tag.labelKey) : tag.label }}</span>
            <span
              @click.stop="removeSelectedTag(tidx)"
              class="el-icon el-icon-error filter-tag-close-btn"></span>
          </div>
        </div>

        <div v-show="hasGroup" class="filter-ipt-wrapper">
          <div @click="toggleFilterPopoverHandle()" class="filter-ipt">{{ $t('modules.views.metrics.list.s_708c9d6d') }}</div>
        </div>

        <span
          v-show="selectedTags.length"
          @click="clearSelectedTags"
          class="el-icon el-icon-circle-close filter-clear-btn"></span>
      </div>
    </div>

    <div class="k8s-filter-group-main">
      <div class="filter-group-main-left">
        <template v-for="item in groupData">
          <div
            :key="item.title"
            v-show="currSelectedTag ? currSelectedTag.group === item.name : groupVisibleMapping[`__group__${item.name}`]"
            class="filter-group-main-title describe">{{ item.titleKey ? $t(item.titleKey) : item.title }}</div>
          <div
            v-for="option in item.options"
            :key="`${item.name}__${option.value}`"
            v-show="currSelectedTag ? currSelectedTag.type === option.value : groupVisibleMapping[`__type__${option.value}`]"
            @click="groupTypeClickHandle(option)"
            :class="['filter-group-main-option', { active: option.value === groupType }]"
          >{{ option.labelKey ? $t(option.labelKey) : option.label }}</div>
        </template>
      </div>

      <template v-for="item in groupData">
        <filter-list
          v-for="option in item.options"
          :key="option.value"
          v-show="option.value === groupType"
          :allList="getTypeData(option.value).list || []"
          :selected="selectedTags.filter(t => t.type === groupType).map(t => t.value)"
          :loading="!!getTypeData(option.value).loading"
          @on-select="filterSelectHandle"
          class="filter-group-main-right" />
      </template>
    </div>
  </el-popover>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator'
import FilterList from './filter-list.vue'

interface LvItem {
  label: string;
  value: string;
}

interface GroupItem {
  title: string;
  name: string;
  options: LvItem[];
}

interface GroupMapping {
  [prop: string]: {
    loading: boolean;
    list: LvItem[];
  };
}

interface TagItem {
  label: string;
  value: string;
  type: string;
  typeLabel: string;
  group: string;
}

interface OptionMapingItem {
  label: string;
  value: string;
  group: string;
  groupLabel: string;
}

interface OptionMapping {
  [prop: string]: OptionMapingItem;
}

@Component({
  components: {
    FilterList,
  }
})
export default class QueryFilter extends Vue {
  @Prop({ default: () => [], required: true }) private groupData!: GroupItem[];
  @Prop({ default: () => ({}), required: true }) private groupMapping!: GroupMapping;

  private selectedTags: TagItem[] = [] // 已选 tags

  private currSelectedTag: TagItem | null = null // 当前选中tag

  private popoverVisible = false // Popover 是否显示

  // 当前选中的 type
  private groupType: string = ''

  get optionMapping() { // option 对应 group 数据
    const data: OptionMapping = {}
    this.groupData.forEach(item => {
      item.options.forEach(t => {
        data[t.value] = {
          ...t,
          group: item.name,
          groupLabel: item.title,
        }
      })
    })
    return data
  }

  // group 是否显示 mapping 数据
  get groupVisibleMapping () {
    const data: any = {}
    this.groupData.forEach(item => {
      let showTitle = false
      item.options.forEach(t => {
        // 在已选tags内
        const selected = !!this.selectedTags.find(g => g.type === t.value)
        data[`__type__${t.value}`] = !selected
        showTitle = showTitle || !selected
      })
      data[`__group__${item.name}`] = showTitle
    })
    return data
  }
  // 是否有group可选
  get hasGroup () {
    return Object.values(this.groupVisibleMapping).some(t => !!t)
  }

  private created () {
    let { tags } = this.$route.query;
    tags = !tags ? [] : !Array.isArray(tags) ? [tags] : tags
    if (tags.filter(t => t && t.includes(':')).length) {
      tags.forEach(t => {
        const [type, value = '', ...labelArr] = (t || '').split(':')
        const label = labelArr.join(':') || value
        if (this.optionMapping[type]) {
          this.selectedTags.push({
            label,
            value,
            type,
            typeLabel: this.optionMapping[type].label,
            group: this.optionMapping[type].group,
          })
        }
      })
      if (this.selectedTags.length) {
        this.queryChangeHandle()
      }
    }
  }

  // type 对应的数据
  private getTypeData (type: string) {
    return (this.groupMapping || {})[type] || {}
  }

  // 显示/隐藏 Popover
  private toggleFilterPopoverHandle (tag?: TagItem) {
    const currTag: any = this.currSelectedTag
    if (!this.popoverVisible) {
      this.currSelectedTag = tag || null
      this.popoverVisible = true
    } else if (!this.currSelectedTag) {
      this.currSelectedTag = tag || null
      this.popoverVisible = !!tag
    } else if (tag && tag.type === currTag.type && tag.value === currTag.value) {
      this.popoverVisible = false
    } else {
      this.currSelectedTag = tag || null
      this.popoverVisible = true
    }
    if (this.popoverVisible) {
      if (this.currSelectedTag) {
        this.groupType = this.currSelectedTag.type
        this.groupTypeClickHandle({
          label: this.currSelectedTag.typeLabel,
          value: this.groupType,
        })
      } else {
        const groupItem = this.groupData.find(item => this.groupVisibleMapping[`__group__${item.name}`]);
        const typeItem = ((groupItem || {}).options || []).find(t => this.groupVisibleMapping[`__type__${t.value}`]);
        if (typeItem) {
          this.groupTypeClickHandle(typeItem)
        } else {
          this.groupType = ''
        }
      }
    }
  }

  // 隐藏 Popover 后触发
  private hideFilterPopoverHandle () {
    this.currSelectedTag = null
  }
  // 切换 type
  private groupTypeClickHandle (option: LvItem) {
    this.groupType = option.value
    this.$emit('type-change', { ...this.optionMapping[option.value] })
  }

  // 添加 tag 事件
  private filterSelectHandle (data: LvItem) {
    this.popoverVisible = false

    // 同type只保留一个
    const index = this.selectedTags.findIndex(t => t.type === this.groupType)
    if (index >= 0) {
      this.selectedTags.splice(index, 1)
    }

    this.selectedTags.push({
      label: data.label,
      value: data.value,
      type: this.groupType,
      typeLabel: this.optionMapping[this.groupType].label,
      group: this.optionMapping[this.groupType].group,
    })
    this.queryChangeHandle()
  }
  // 删除已选 tag 事件
  private removeSelectedTag (idx: number) {
    this.popoverVisible = false
    this.selectedTags.splice(idx, 1)
    this.queryChangeHandle()
  }
  // 清空已选 tags
  private clearSelectedTags () {
    this.popoverVisible = false
    this.selectedTags = []
    this.queryChangeHandle()
  }

  private queryChangeHandle () {
    this.$router.replace({
      path: this.$route.path,
      query: {
        ...this.$route.query,
        tags: this.selectedTags.map((t: any) => `${t.type}:${t.value}${t.label === t.value ? '' : `:${t.label}`}`),
      }
    })
    this.$emit('query-change', [...this.selectedTags])
  }
}
</script>

<style lang="scss" scoped>
.query-filter-wrapper {
  min-height: 32px;
  padding: 0 28px;
  border: 1px solid var(--border-color-base);
  background: var(--bg-color);
  border-radius: 4px;
  flex-wrap: wrap;
  line-height: 24px;
  position: relative;
  color: var(--color-text-primary);
  &::before {
    position: absolute;
    top: 8px;
    left: 8px;
    line-height: 1;
  }

  .multi-filter-cont {
    padding: 2px 0;

    .filter-tag {
      margin: 2px 0 2px 4px;
      display: inline-block;
      padding: 2px 8px;
      font-size: 13px;
      line-height: 16px;
      border: 1px solid var(--border-color-light);
      border-radius: 3px;
      background-color: var(--background-color-base);
      cursor: pointer;

      .filter-tag-close-btn {
        margin-left: 6px;
        cursor: pointer;
        color: var(--color-text-secondary);
        opacity: 0.6;

        &:hover {
          opacity: 1;
        }
      }
    }

    & + .filter-ipt-wrapper {
      margin-left: 10px;
    }
  }

  .filter-ipt-wrapper {
    flex: 1;
    min-width: 40px;

    .filter-ipt {
      height: 30px;
      line-height: 30px;
      color: var(--color-text-placeholder);
      cursor: pointer;
      user-select: none;
      white-space: nowrap;
      font-size: 13px;
    }
  }

  .filter-clear-btn {
    display: none;
    padding: 5px;
    font-size: 14px;
    color: var(--color-text-secondary);
    transition: opacity .2s ease;
    transform: translate(0, -50%);
    opacity: 0.6;
    cursor: pointer;
    position: absolute;
    top: 50%;
    right: 3px;
    &:hover {
      opacity: 1;
    }
  }

  &:hover {
    .filter-clear-btn {
      display: block;
    }
  }
}

.k8s-filter-group-main {
  min-height: 120px;
  max-height: 320px;
  display: flex;

  ::-webkit-scrollbar{width:5px;height:5px;cursor:pointer}

  .filter-group-main-left {
    width: 150px;
    padding: 6px 0;
    overflow-y: auto;
  }
  .filter-group-main-right {
    flex: 1;
    padding-top: 6px;
    border-left: 1px solid var(--border-color-base);
  }

  .filter-group-main-title {
    padding-left: 16px;
    font-size: 12px;
    color: var(--color-text-secondary);
    line-height: 28px;
    &:not(:first-child) {
      margin-top: 6px;
    }
  }
  .filter-group-main-option {
    padding: 0 16px;
    font-size: 14px;
    position: relative;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    color: var(--color-text-primary);
    height: 34px;
    line-height: 34px;
    box-sizing: border-box;
    cursor: pointer;
    transition: all .3s ease;

    &.active,
    &:hover {
      background-color: var(--background-color-base);
    }

    &.active {
      color: var(--color-primary);
    }
  }
}
</style>
<style lang="scss">
.el-popover.k8s-group-popover-comp {
  padding: 0;
}
</style>
