<template>
  <div class="detail-response" v-loading="isLoading">
    <div class="info-t flex-h"><span class="label">{{ $t('modules.views.alarmCenter.alarmDetail.s_d15d9c61') }}</span>{{ $t('modules.api.notice.ts.s_5660bcd2') }}</div>

    <el-input
      v-model="queryText"
      @change="searchChangeHandle"
      clearable size="small"
      maxlength="100"
      prefix-icon="db-icon-search"
      :placeholder="$t('modules.views.alarmCenter.alarmDetail.s_d1f0b009')"
      class="mb-12" />

    <db-table
      ref="listTable"
      :data="tableList"
      :columnConfig="columnConfig"
      :showTotal="false"
      class="detail-response-list">
      <template slot="column-result" slot-scope="{ row }">
        <i v-if="row.result !== 'success'" class="db-icon-close-pie font-12 mr-5 db-red icon-vm"></i>
        <i v-else class="db-icon-right-pie font-12 mr-5 db-green icon-vm"></i>
        <span>{{ row.result | NoticeResultFilter }}</span>
      </template>
      <template slot="column-method" slot-scope="{ row }">
        <i class="db-icon font-14 mr-5 icon-vm">{{ row.method | DbIconFilter }}</i>
        <span v-if="row.method !== 'wechat' || row.isSingle === 1">{{ row.method | NoticeMethodFilter }}</span>
        <span v-else>{{ $t('modules.views.alarmCenter.alarmDetail.s_cfbf6f4c') }}</span>
        <span v-if="['dingtalk', 'wechat'].includes(row.method)">{{ row.isSingle === 1 ? $t('modules.views.alarmCenter.alarmDetail.s_6a0e0419') : $t('modules.views.alarmCenter.alarmDetail.s_1f097591')  }}</span>
      </template>
    </db-table>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { toAsyncWait } from '@/utils/common';
import AlarmApi from '@/api/alarm';
import { NoticeMethodFilter, NoticeResultFilter } from '@/utils/filters/notice';

@Component
export default class DetailResponse extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;

  public $refs!: {
    listTable: any
  }

  private queryText = ''

  private columnConfig = [
    { field: 'result', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_895bd22e') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_895bd22e', slot: 'column-result', minWidth: 80, disabled: true },
    { field: 'noticeTime', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_3f6194f8') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_3f6194f8', minWidth: 140 },
    { field: 'rcvNames', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_88340ead') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_88340ead', minWidth: 150 },
    { field: 'method', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_9fd00f0a') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_9fd00f0a', slot: 'column-method', minWidth: 120 },
    { field: 'errMsg', label: i18n.t('modules.views.alarmCenter.alarmDetail.s_13d5f243') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_13d5f243', minWidth: 250 },
  ]

  private isLoading = false
  private tableList: any[] = []
  private allList: any[] = []

  public getData () {
    this.getAllList()
  }

  public resize () {
    (this.$refs.listTable as any)?.getHeightHandle();
  }

  private async getAllList() {
    if (this.isLoading) {
      return;
    }
    const params: any = {
      alarmId: this.queryParams.alarmId
    }
    this.isLoading = true
    const { result, error } = await toAsyncWait(AlarmApi.getNoticeRecordList(params));
    this.isLoading = false
    if (!error) {
      const list = result?.data?.list || [];
      this.allList = list.map((t: any) => {
        const noticeMethod = NoticeMethodFilter(t.method)
        const noticeResult = NoticeResultFilter(t.result)
        return {
          ...t,
          rcvNames: t.rcvNames || '',
          errMsg: t.errMsg || '',
          noticeMethod: noticeMethod !== '-' ? noticeMethod : '',
          noticeResult: noticeResult !== '-' ? noticeResult : '',
        }
      })
      this.tableList = this.allList
    }
  }

  private searchChangeHandle () {
    let list = this.allList
    if (this.queryText) { // 搜索
      const q = this.queryText.toLocaleLowerCase()
      list = list.filter(t => {
        const rcvNames = t.rcvNames.toLocaleLowerCase()
        const errMsg = t.errMsg.toLocaleLowerCase()
        const noticeMethod = t.noticeMethod.toLocaleLowerCase()
        const noticeResult = t.noticeResult.toLocaleLowerCase()
        return rcvNames.includes(q) || errMsg.includes(q) || noticeMethod.includes(q) || noticeResult.includes(q)
      })
    }
    this.tableList = list
  }
}
</script>

<style lang="scss" scoped>
.detail-response {
  height: 100%;
  padding: 20px;

  .info-t {
    margin-bottom: 12px;
    line-height: 14px;
    font-size: 13px;
    .label {
      flex: none;
      margin-right: 16px;
      min-width: 52px;
      color: var(--color-text-secondary);
    }
  }

  .detail-response-list {
    height: calc(100% - 70px);
  }
}
</style>
