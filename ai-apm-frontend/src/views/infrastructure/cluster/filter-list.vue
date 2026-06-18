<template>
  <div
    v-loading="loading"
    class="filter-list-wrapper">
    <el-input
      v-model="queryParams.query"
      @change="searchChangeHandle"
      size="mini" clearable
      :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')"
      maxlength="100"
      class="filter-search-ipt" />

    <div ref="filterList" class="filter-list">
      <div
        v-for="t in list"
        :key="t.value"
        @click="itemClickHandle(t)"
        :title="t.label"
        :class="['filter-t', { active: selectedMapping[t.value], sub: !!t.sub }]">{{ t.labelKey ? $t(t.labelKey) : t.label }}</div>

      <div v-if="!loading && !list.length" class="filter-empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import { debounce } from '@/utils/common'

interface LvItem {
  label: string;
  value: string;
  sub?: boolean;
}

interface SelectedMapping {
  [prop: string]: boolean;
}

@Component
export default class FilterList extends Vue {
  @Prop({ default: () => [], required: true }) private allList!: LvItem[];
  @Prop({ default: () => [] }) private selected!: string[]; // value 数组
  @Prop({ default: false }) private loading!: boolean;

  public $refs!: {
    filterList: HTMLDivElement,
  }

  private queryParams = {
    query: '',
    pageNum: 1,
    pageSize: 20,
  }

  private list: LvItem[] = []
  private total = 0
  get noMore () {
    return this.list.length >= this.total
  }

  private timer: any = null;
  private scrollContainer: any = null;
  private scrollHandle: any = null;

  private selectedMapping: SelectedMapping = {}

  @Watch('allList')
  private onAllListChange() {
    this.queryParams.query = ''
    this.getList()
  }

  @Watch('selected')
  private onSelectedChange() {
    const mapping: SelectedMapping = {};
    (this.selected || []).forEach(t => {
      mapping[t] = true
    })
    this.selectedMapping = mapping
  }

  private mounted () {
    this.getList()
  }

  private beforeDestroy () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    if (this.scrollContainer) {
      this.scrollContainer.removeEventListener('scroll', this.scrollHandle)
    }
  }

  // 选中
  private itemClickHandle (data: LvItem) {
    this.$emit('on-select', { ...data })
  }

  // 分页
  private async getList (page = 1) {
    const { query, pageSize } = this.queryParams
    this.queryParams.pageNum = page
    if (page === 1 && this.scrollContainer) {
      // 滚动区域 scrollTop 置为 0
      this.scrollContainer.scrollTop = 0
    }
    let list = this.allList
    if (query) { // 搜索
      const q = query.toLocaleLowerCase()
      list = list.filter(t => t.label.toLocaleLowerCase().includes(q))
    }
    this.total = list.length
    this.list = [
      ...(page === 1 ? [] : this.list),
      ...list.slice((page - 1) * pageSize, page * pageSize)
    ];
    this.$nextTick(() => {
      if (!this.scrollContainer) {
        this.loop();
      } else {
        this.fillListInData()
      }
    })
  }
  // 搜索
  private searchChangeHandle() {
    this.queryParams.query = this.queryParams.query.trim();
    this.getList()
  }

  // 继续加载数据以填充容器
  private fillListInData () {
    this.$nextTick(() => {
      const { scrollHeight, clientHeight } = this.scrollContainer
      if (clientHeight && scrollHeight - 50 < clientHeight && !this.noMore) {
        this.getList(this.queryParams.pageNum + 1)
      }
    })
  }
  private loop () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    this.timer = setTimeout(() => {
      const scrollContainer = this.$refs.filterList;
      if (!scrollContainer) {
        this.loop();
      } else {
        this.scrollContainer = scrollContainer;
        // 滚动到底加载更多
        this.scrollHandle = debounce(() => {
          const { scrollHeight, scrollTop, clientHeight } = scrollContainer
          if (scrollHeight - clientHeight - scrollTop < 50 && !this.noMore) {
            this.getList(this.queryParams.pageNum + 1)
          }
        }, 17)
        scrollContainer.addEventListener('scroll', this.scrollHandle)
        this.fillListInData()
      }
    }, 100)
  }
}
</script>

<style lang="scss" scoped>
.filter-list-wrapper {
  display: flex;
  flex-direction: column;
  border-radius: 0 4px 4px 0;
  overflow: hidden;

  .filter-search-ipt {
    border-color: transparent;
    background: transparent;
    width: calc( 100% - 20px );
    margin: 0 10px;

    :deep(.el-input__inner) {
      border-radius: 0;
      border-left: none;
      border-top: none;
      border-right: none;
      padding: 1px 6px 0;
      background: transparent;
    }
  }

  .filter-list {
    padding: 6px 0;
    height: 100%;
    overflow-y: auto;

    .filter-t {
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

      &.sub {
        padding-left: 30px;
      }
    }

    .filter-empty {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      color: var(--color-text-regular);
    }
  }
}
</style>
