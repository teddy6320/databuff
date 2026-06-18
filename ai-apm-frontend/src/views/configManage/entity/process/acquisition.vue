<template>
  <div class="acquisition-wrap">
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
      <div class="list-item-title">
        {{ $t('modules.views.configManage.entity.s_40ab6b0f') }}
        <el-switch
          v-model="monitorAll"
          @change="toggleAllHandle"
          :disabled="!isAdmin || ruleLoading"
          class="ml-5" />
      </div>
      <table-list
        v-if="!monitorAll"
        v-loading="ruleLoading"
        :tableList="monitorRules"
        :height="`${monitorRuleHeight}px`"
        @on-edit="editHandle"
        @on-delete="getRules"
        class="list-item-table" />

      <div class="list-item-title mt-20">{{ $t('modules.views.configManage.entity.s_1f415dcf') }}</div>
      <table-list
        v-loading="ruleLoading"
        :tableList="notMonitorRules"
        :height="`calc(100% - ${monitorRuleHeight}px - 82px)`"
        @on-edit="editHandle"
        @on-delete="getRules"
        class="list-item-table last" />
    </div>

    <rule-config
      ref="ruleConfig"
      :detail="detail"
      @on-saved="getRules"
    />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import TableList from './table-list.vue'
import RuleConfig, { formatProcessText, formatHostText, formatDataToForm } from './acq-config.vue';
import { toAsyncWait } from '@/utils/common';
import ProcessApi from '@/api/process';

@Component({
  components: {
    TableList,
    RuleConfig,
  }
})
export default class Acquisition extends Vue {
  public $refs!: {
    ruleConfig: RuleConfig
  }

  private queryParams: any = {
    query: '',
  }

  private monitorAll = true // 是否监控所有进程
  private ruleLoading = false
  private allRules: any[] = [] // 所有规则
  private monitorRules: any[] = [] // 监控规则
  private notMonitorRules: any[] = [] // 不监控规则

  private detail: any = {}

  get monitorRuleHeight () {
    if (this.monitorAll) {
      return 0
    }
    const height = this.monitorRules.length * 40 + 41
    return height > 300 ? 300 : height < 101 ? 101 : height;
  }

  private created () {
    const { ct, desc } = this.$route.query
    if (ct !== 'recognition' && desc) {
      this.queryParams.query = decodeURIComponent(desc as string || '')
    }
    this.getRules()
  }

  private getRules () {
    this.getMonitorRules()
  }

  private async getMonitorRules () {
    this.ruleLoading = true;
    const { result, error } = await toAsyncWait(ProcessApi.getCollectRuleList({ keyword: '' }));
    this.ruleLoading = false;
    if (!error) {
      const { collectAllProcess, whitelist, blacklist, } = result.data || {};
      this.monitorAll = !!collectAllProcess
      this.allRules = [...(whitelist || []), ...(blacklist || [])].map((t: any) => {
        const formData = formatDataToForm(t)
        return {
          ...t, ...formData,
          describe: formatProcessText(formData),
          hostDesc: formatHostText(formData),
          isPreset: !t.inner,
          status: !!t.status,
        }
      });
      this.searchChangeHandle();
    }
  }

  private searchChangeHandle (updateRoute?: boolean) {
    const _q = this.queryParams.query.toLocaleLowerCase()
    const list = this.allRules.filter(t => t.describe.toLocaleLowerCase().indexOf(_q) > -1)
    this.monitorRules = list.filter(t => t.monitorType === 'monitor')
    this.notMonitorRules = list.filter(t => t.monitorType === 'notMonitor')

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

  private async toggleAllHandle () {
    this.ruleLoading = true;
    const { result, error } = await toAsyncWait(ProcessApi.setCollectAll({ collectAllProcess: this.monitorAll }));
    this.ruleLoading = false;
    if (!error) {
      this.$message.success(i18n.t('modules.views.configManage.entity.s_33130f5c') as string);
      this.getRules();
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.configInstall.dataAccess.s_5fa802be') as string);
    }
  }
}
</script>

<style lang="scss" scoped>
.acquisition-wrap {
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
    align-items: center;
    font-size: 13px;
    line-height: 20px;
  }

  .list-item-table {
    margin-top: 10px;
  }
}
</style>
