<template>
  <el-popover
    placement="bottom-end"
    width="450"
    trigger="click"
  >
    <div slot="reference" class="analysis-record-btn">{{ $t('modules.views.alarmCenter.rootCauseAnalysis.s_81c74fc5') }}<span class="db-icon-down"></span></div>

    <div ref="tableWrap" class="record-wrap">
      <el-table
        :data="tableList"
        :max-height="300"
        highlight-current-row size="mini"
        tooltip-effect="light"
        class="record-table"
      >
        <el-table-column :label="$t('modules.views.alarmCenter.eventDetail.s_19fcb9eb')" prop="createTime" show-overflow-tooltip min-width="140">
          <template slot-scope="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>

        <el-table-column :label="$t('modules.views.alarmCenter.rootCauseAnalysis.s_f5edd796')" prop="source" show-overflow-tooltip min-width="75">
          <template slot-scope="{ row }">
            <div class="ell">{{ row.source || '-' }}</div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-popover>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { debounce } from '@/utils/common';

@Component
export default class AnalysisRecord extends Vue {
  @Prop({ default: () => [] }) private list!: any[];

  public $refs!: {
    tableWrap: HTMLDivElement,
  }

  private timer: any = null;
  private scrollContainer: any = null;
  private scrollHandle: any = null;

  private queryParams: any = {
    pageNum: 1,
    pageSize: 20,
  }

  private listTotal = 0
  private tableList: any[] = [];
  get noMore () {
    return this.tableList.length >= this.listTotal
  }

  private mounted () {
    this.listTotal = this.list.length;
    this.getTableList()
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

  private async getTableList(page = 1) {
    if ((page !== 1 && this.noMore)) {
      return;
    }
    this.queryParams.pageNum = page
    const { pageSize, sortOrder, sortField } = this.queryParams
    this.tableList = this.list.slice(0, page * pageSize);

    this.$nextTick(() => {
      if (!this.scrollContainer) {
        this.loop();
      }
    })
  }

  // 滚动加载相关
  private loop () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    this.timer = setTimeout(() => {
      const scrollContainer = this.$refs.tableWrap.querySelector('.el-table__body-wrapper');
      if (!scrollContainer) {
        this.loop();
      } else {
        this.scrollContainer = scrollContainer;
        // 滚动到底加载更多
        this.scrollHandle = debounce(() => {
          const { scrollHeight, scrollTop, clientHeight } = scrollContainer
          if (!this.noMore && scrollHeight - clientHeight - scrollTop < 50) {
            this.getTableList(this.queryParams.pageNum + 1)
          }
        }, 17)
        scrollContainer.addEventListener('scroll', this.scrollHandle)
      }
    }, 100)
  }
}
</script>

<style lang="scss" scoped>
.analysis-record-btn {
  margin: -2px 0;
  font-size: 13px;
  line-height: 18px;
  color: var(--color-text-link);
  cursor: pointer;
}
</style>
