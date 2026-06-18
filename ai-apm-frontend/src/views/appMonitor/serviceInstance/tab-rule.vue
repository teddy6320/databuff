<template>
  <div class="alarm-cont">
    <div class="alarm-wrapper flex-v">
      <div class="list-wrapper">
        <db-table
          :queryApi='queryApi'
          :queryParams='tableQueryParams'
          :columnConfig='columnConfig'
          @on-table-inited='tableInitedHandle'
          @sort-change='refresh'
          @on-fetch-end='onFetchEnd'
          showSetting
          :formatFunc='formatFunc'
          tableKey='APM_SERVICEINSTANCE_DETAIL_RULE'
          ref='listTable'>

          <template slot='ruleName' slot-scope="{ row }">
            <el-popover v-if="(row.closeMetrics || []).length" trigger="hover" placement="top">
              <div style='font-size: 13px;'>{{ $t('modules.views.appMonitor.serviceDetail.s_9c7f996e', { value0: row.closeMetrics.join('、') }) }}</div>
              <span slot="reference" class="el-icon-warning mr-5 red" />
            </el-popover>
            <span @click="viewSettingHandle(row, 'e')" class="db-blue cphu">{{ row.ruleName || '-' }}</span>
          </template>

          <template slot='classification' slot-scope="{ row }">
            <span>{{ row.ruleName | MonitorTypeFilter }}</span>
          </template>

          <template slot='type' slot-scope="{ row }">
            <template v-if="row._type.length">
              <span v-for="(t, i) in row._type" :key="i">
                {{ i === 0 ? '' : ',' }}
                {{ t | MonitorMethodFilter }}
              </span>
            </template>
            <template v-else>-</template>
          </template>

          <template slot='enabled' slot-scope="{ row }">
            <el-switch
              v-model="row.enabled"
              :disabled="row.loading"
              @change="toggleEnableHandle(row)">
            </el-switch>
          </template>

          <template slot='suffix'>
            <el-table-column key="actions" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" width="150">
              <template slot-scope="{ row }">
                <span
                  @click.stop="viewSettingHandle(row, 'e')"
                  :class="{ 'action-disabled': row.loading }"
                  class="blue cp mr-10">{{ $t('modules.views.hide.advancedConfig.s_95b351c8') }}</span>
                <span
                  @click.stop="viewSettingHandle(row, 'c')"
                  :class="{ 'action-disabled': row.loading }"
                  class="blue cp mr-10">{{ $t('modules.views.aiPlatform.chat.s_79d3abe9') }}</span>

                <span
                  @click.stop="deleteHandle(row)"
                  :class="{ 'action-disabled': row.loading }"
                  class="blue cp">{{ $t('modules.views.hide.advancedConfig.s_2f4aaddd') }}</span>
              </template>
            </el-table-column>
          </template>
        </db-table>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import AlarmApi from '@/api/alarm';
import dayjs from 'dayjs';
import { toAsyncWait } from '@/utils/common';
import MonitorApi from '@/api/monitor';
import { ALARM_DETAIL_PATH, buildAlarmDetailLocation } from '@/views/configManage/alarm/alarm-routes';

@Component
export default class TabAlarm extends Vue {
  @Prop({ default: {} }) private current!: any;

  @Watch('current', { immediate: true })
  private onCurrentChange (val: any, oldVal: any) {
    if (val && val?.serviceId !== oldVal?.serviceId && this.isMounted) {
      this.fetchAllData();
    }
  }

  public $refs!: {
    listTable: any;
  };

  private isMounted = false;
  private showCharts = false;

  get tableQueryParams () {
    return {
      serviceId: this.current?.serviceId || decodeURIComponent(String(this.$route.query.sid)),
      serviceInstance: this.current?.serviceInstance || decodeURIComponent(String(this.$route.query.si)),
    }
  }

  private columnConfig: any = [
    { field: 'ruleName', prop: 'ruleName', label: i18n.t('modules.views.appMonitor.serviceDetail.s_87080256') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_87080256', slot: 'ruleName', minWidth: 150, defaultShow: true },
    { field: 'id', prop: 'id', label: i18n.t('modules.views.appMonitor.serviceDetail.s_779ee11c') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_779ee11c', minWidth: 120, defaultShow: false },
    { field: 'classification', prop: 'classification', label: i18n.t('modules.views.appMonitor.serviceDetail.s_986329a3') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_986329a3', slot: 'classification', minWidth: 120, defaultShow: true },
    { field: 'type', prop: 'type', label: i18n.t('modules.views.appMonitor.serviceDetail.s_2b87411b') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_2b87411b', slot: 'type', minWidth: 100, defaultShow: true },
    { field: 'updateTime', prop: 'updateTime', label: i18n.t('modules.views.appMonitor.serviceDetail.s_921453aa') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_921453aa', unit: 'time', minWidth: 120, defaultShow: true, sortable: true, defaultSort: 'desc' },
    { field: 'editor', prop: 'editor', label: i18n.t('modules.views.appMonitor.serviceDetail.s_1e1bbd89') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_1e1bbd89', minWidth: 100, defaultShow: true },
    { field: 'createTime', prop: 'createTime', label: i18n.t('modules.views.appMonitor.serviceDetail.s_eca37cb0') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_eca37cb0', unit: 'time', minWidth: 120, defaultShow: false, sortable: true },
    { field: 'creator', prop: 'creator', label: i18n.t('modules.views.appMonitor.serviceDetail.s_f4cc0634') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_f4cc0634', minWidth: 100, defaultShow: false },
    { field: 'enabled', prop: 'enabled', label: i18n.t('modules.views.appMonitor.serviceDetail.s_2b82bf9a') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_2b82bf9a', slot: 'enabled', minWidth: 100, defaultShow: true },
  ];

  private queryApi = MonitorApi.getRuleList

  private created () {
    this.$emit('on-created');
  }
  private mounted () {
    if (this.current?.serviceId ) {
      this.refresh();
    }
    this.isMounted = true;
  }

  public refresh () {
    this.fetchAllData();
  }

  private fetchAllData () {
    //
  }

  private formatFunc (data: any) {
    (data || []).forEach((t: any) => {
      t.ruleName = t.ruleName || t.name
      t._type = t.type ? t.type.split(',') : []
      t.enabled = t.enabled === true
    })
  }


  // tableInitedHandle
  private tableInitedHandle () {
    this.$refs.listTable.refresh();
  }

  private onFetchEnd () {
    this.$emit('on-loaded')
  }

    // 编辑|复制
  public viewSettingHandle(row?: any, mode?: 'e' | 'c') {
    this.$router.push(buildAlarmDetailLocation(
      ALARM_DETAIL_PATH.ruleSetting,
      this.$route.query,
      row ? { id: row.id, mode: mode || 'e' } : undefined,
    ));
  }

  // 启停
  private async toggleEnableHandle (row: any) {
    if (row.loading) {
      return;
    }
    const params = {
      ids: [row.id],
      enabled: !!row.enabled,
    }
    this.$set(row, 'loading', true);
    const { result, error } = await toAsyncWait(MonitorApi.toggleRuleEnable(params));
    this.$set(row, 'loading', false);
    if (!error) {
      this.$message.success(params.enabled ? i18n.t('modules.views.appMonitor.serviceDetail.s_c0cd850f') as string : i18n.t('modules.views.appMonitor.serviceDetail.s_8b4e6853') as string);
      this.$set(row, 'enabled', params.enabled);
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message);
    }
  }
}
</script>
<style lang="scss" scoped>
.alarm-cont {
  padding-left: 4px;
  height: 100%;
}
.alarm-wrapper {
  height: 100%;
  display: flex;
}
.chart-wrapper {
  flex: 1 0 auto;
  height: 180px;
}
.list-wrapper {
  flex: 1 1 auto;
  min-height: 286px;
}
</style>