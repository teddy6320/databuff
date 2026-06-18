<template>
  <div v-if="!empty" class="event-detail-log">
    <el-input
      v-model="queryText"
      @change="tableRefresh"
      clearable size="small"
      maxlength="100"
      prefix-icon="db-icon-search"
      :placeholder="$t('modules.views.alarmCenter.alarmDetail.s_d1f0b009')" />

    <db-table
      ref="listTable"
      :queryApi="queryApi"
      :queryParams="getQueryParams"
      :timeMode="false"
      :autoRefresh="false"
      :offsetMode="true"
      :columnConfig="columnConfig"
      :formatFunc="formatFunc"
      :row-style="{ cursor: 'pointer' }"
      @sort-change="tableRefresh"
      @row-click="rowClickHandle"
      class="detail-log-list">
      <el-table-column slot="suffix" type="expand" width="60" class-name="table-expand-col-items">
        <template slot-scope="{ row }">
          <div class="describe" style='font-size: 13px;'>{{ $t('modules.views.alarmCenter.eventDetail.s_a19a72d2') }}</div>
          <pre class="log-pre">{{ row.message || '-' }}</pre>
        </template>
      </el-table-column>
    </db-table>
  </div>
  <div v-else class="event-detail-log empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import LogApi from '@/api/log';

@Component
export default class TabLog extends Vue {
  @Prop({ default: {} }) private detail!: any;

  private queryText = ''

  private queryApi = LogApi.getLogList
  private columnConfig = [
    { field: 'timestamp', label: i18n.t('modules.views.alarmCenter.eventDetail.s_19fcb9eb') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_19fcb9eb', unit: 'time', width: 140 },
    { field: 'hostname', label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369', minWidth: 80 },
    { field: 'service', label: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_47d68cd0', minWidth: 120 },
    { field: 'status', label: i18n.t('modules.views.alarmCenter.eventDetail.s_3fea7ca7') as string, labelKey: 'modules.views.aiPlatform.experts.s_3fea7ca7', minWidth: 60 },
    { field: 'message', label: i18n.t('modules.views.alarmCenter.eventDetail.s_2d711b09') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_2d711b09', minWidth: 200 },
  ]

  get getQueryParams () {
    const { _start, _end, trigger } = this.detail || {}
    return {
      query: this.queryText,
      hosts: trigger?.host ? [trigger.host] : [],
      services: trigger?.service ? [trigger.service] : [],
      fromTimeNs: `${_start * 1000 * 1000}`,
      toTimeNs: `${_end * 1000 * 1000}`,
    }
  }

  get empty () {
    const { hosts, services } = this.getQueryParams
    return !hosts.length && !services.length
  }

  public getData () {
    if (!this.empty) {
      this.tableRefresh();
    }
  }

  public resize () {
    if (!this.empty) {
      (this.$refs.listTable as any)?.getHeightHandle();
    }
  }

  private formatFunc (data: any) {
    return data.map((t: any) => ({
      ...t,
      timestamp: t.timestamp ? +t.timestamp.substring(0, 13) : '',
    }));
  }
  private tableRefresh () {
    (this.$refs.listTable as any)?.refresh();
  }

  private rowClickHandle (row: any) {
    (this.$refs.listTable as any)?.toggleRowExpansion(row)
  }
}
</script>

<style lang="scss" scoped>
.event-detail-log {
  height: 100%;
  &.empty {
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 13px;
    color: var(--color-text-secondary);
  }

  .detail-log-list {
    height: calc(100% - 32px);
  }

  :deep(.table-expand-col-items .el-table__expand-icon .el-icon) {
    color: var(--color-text-secondary);
    font-weight: bold;
    &:hover {
      color: var(--color-text-primary);
    }
  }

  .log-pre {
    font-family: Roboto,Helvetica Neue,Arial,sans-serif;
    margin: 8px 0;
    color: var(--color-text-primary);
    font-size: 13px;
    word-break: break-all;
    white-space:break-spaces;
  }
}
</style>
