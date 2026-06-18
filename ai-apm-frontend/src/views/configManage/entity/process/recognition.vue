<template>
  <div class="recognition-wrap">
    <div class="search-group flex-h-jc">
      <el-input
        v-model="queryParams.query"
        @change="searchChangeHandle(true)"
        clearable size="small"
        maxlength="100"
        prefix-icon="db-icon-search"
        :placeholder="$t('modules.views.configManage.entity.s_d7a81ce7')"
        class="query-input"
      />

      <el-button
        @click="addHandle"
        type="primary" size="small" class="ml-10 flex-none">
        <i class="db-icon-add"></i> {{ $t('modules.views.configManage.alarm.s_26bb8418') }}</el-button>
    </div>

    <div class="list-cont">
      <div class="list-item-title">{{ $t('modules.views.configManage.entity.s_3d96e600') }}</div>
      <table-list
        v-loading="recLoading"
        :tableList="recRules"
        :isRecognition="true"
        @on-edit="editHandle"
        @on-delete="getRecRules"
        class="list-item-table last" />
    </div>

    <rule-config
      ref="ruleConfig"
      :detail="detail"
      @on-saved="getRecRules"
    />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import TableList from './table-list.vue'
import RuleConfig, { formatProcessText, formatDataToForm } from './rec-config.vue';
import { toAsyncWait } from '@/utils/common';
import ProcessApi from '@/api/process';

@Component({
  components: {
    TableList,
    RuleConfig,
  }
})
export default class Recognition extends Vue {
  public $refs!: {
    ruleConfig: RuleConfig
  }

  private queryParams: any = {
    query: '',
  }

  // 业务进程规则
  private recLoading = false
  private allRecRules: any[] = []
  private recRules: any[] = []

  private detail: any = {}

  private created () {
    const { ct, desc } = this.$route.query
    if (ct === 'recognition' && desc) {
      this.queryParams.query = decodeURIComponent(desc as string || '')
    }
    this.getRecRules()
  }

  private async getRecRules () {
    this.recLoading = true;
    const { result, error } = await toAsyncWait(ProcessApi.getIdentifyRuleList({ keyword: '' }));
    this.recLoading = false;
    if (!error) {
      const { list } = result.data || {};
      this.allRecRules = (list || []).map((t: any) => {
        const formData = formatDataToForm(t)
        return {
          ...t, ...formData,
          describe: formatProcessText(formData),
          isPreset: !t.inner,
          status: !!t.status,
        }
      });
      this.searchChangeHandle();
    }
  }

  private searchChangeHandle (updateRoute?: boolean) {
    const _q = this.queryParams.query.toLocaleLowerCase()
    this.recRules = this.allRecRules.filter(t => t.describe.toLocaleLowerCase().indexOf(_q) > -1)

    if (updateRoute) {
      const _query: any = { ...this.$route.query, desc: encodeURIComponent(this.queryParams.query || '') }
      if (!_query.desc) {
        delete _query.desc
      }
      this.$router.replace({ query: { ..._query }})
    }
  }

  private addHandle () {
    this.detail = {}
    this.$refs.ruleConfig.show()
  }

  private editHandle (row: any) {
    this.detail = row
    this.$refs.ruleConfig.show()
  }
}
</script>

<style lang="scss" scoped>
.recognition-wrap {
  .search-group {
    white-space: nowrap;
  }

  .list-cont {
    margin-top: 16px;
    height: calc(100% - 48px);
    background: var(--bg-color);
    display: flex;
    flex-direction: column;
  }

  .list-item-title {
    display: flex;
    font-size: 13px;
    line-height: 20px;
  }

  .list-item-table {
    margin-top: 10px;
    min-height: 101px;
    &.last {
      flex: 1;
    }
  }
}
</style>
