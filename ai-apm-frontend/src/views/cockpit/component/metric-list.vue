<template>
  <div class="metric-cont flex-v ovh">
    <div class="pl-20 mt-15 border-bottom-1 mb-10">
      <db-tabnav v-model='active' :tabnavs='options' @on-change='changeHandle'></db-tabnav>
    </div>
    <div class="alert-info br-4 mb-10 ml-20 mr-20">
      <span class="el-icon-warning blue"></span>
      {{ $t('modules.views.cockpit.component.s_3b362261') }}
    </div>
    <div class="mb-10 flex-h ovh pl-20 pr-20">
      <el-input v-model='nameQuery' size="mini" :placeholder="$t('modules.views.alarmCenter.alarmDetail.s_e5f71fc3')" class="mr-10 flex-1"></el-input>
      <el-button @click="dialogShowHandle" size="mini" icon='el-icon-plus'>{{ $t('modules.views.cockpit.component.s_4f341b4e') }}</el-button>
    </div>
    <div class="pl-20 pr-20 flex-1 ovy">
      <el-table
        :data="metricList"
        border :select-on-indeterminate='false'
        @selection-change="selectionChangeHandle"
        size="small" ref='table'
        :row-style="getRowStyleByFilter"
      >
        <el-table-column type="selection" :selectable='selectableFunc'></el-table-column>
        <el-table-column prop="name" :label="$t('modules.views.appMonitor.dbConnPool.s_b8403584')" min-width="140">
          <template slot-scope="{ row }">
            <div class="flex-h ovh">
              <span class="ell">{{ row.metricCn || row.metricName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="$t('modules.views.cockpit.component.s_b11ec6fe')" min-width="200">
          <template slot-scope="{ row }">
            <span>{{ row.desc }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unit" :label="$t('modules.components.charts.s_f2996845')" width="100">
          <template slot-scope="{ row }">
            <span>{{ row.unit }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="cont-footer">
      <el-button @click="postHandle" class="" size="small" type="primary">{{ $t('modules.views.configInstall.plugin.s_be5fbbe3') }}</el-button>
      <el-button @click="cancelHandle" class="" size="small">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
    </div>

    <MetricDialog v-model='showDialog' :list="getAvailChooseMetrics" :currentList='getCurrentList' @on-close='dialogCloseHandle' />
  </div>
</template>

<script lang="ts">
import { toAsyncWait } from '@/utils/common';
import i18n from '@/i18n';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import MetricApi from '@/api/metric';
import MetricDialog from './metric-dialog.vue'
import Api from '../api'
import { Table } from 'element-ui';
import { cloneDeep } from 'lodash';

@Component({
  components: {
    MetricDialog,
  }
})
export default class MetricComp extends Vue {
  @Prop({}) private detail!: any;
  @Prop({}) private options!: any;
  @Prop({}) private type!: any;
  
  private active = '';
  private selectLoading = false;

  private selection: any[] = [];

  private metricList: any = [];
  private cacheMap: Record<string, any[]> = {}

  private showDialog = false;

  private nameQuery = '';

  @Watch('nameQuery')
  private onNameQueryChange (newVal: string) {
    // js控制Dom隐藏
    if (!this.$refs.table) {
      return;
    }
    if (!newVal) {
      (this.$refs.table as Table).clearFilter();

    } else {

    }
  }

  get getAvailChooseMetrics () {
    const cachedList = this.cacheMap[this.active] || [];
    const selectedMetricNames = this.getCurrentList.map((i: any) => i.metric);
    const availList = cachedList.filter(i => !selectedMetricNames.includes(i.metricName));
    return availList;
  }

  get getCurrentList () {
    return this.metricList.map((i: any) => ({
      metric: i.metricName,
      enabled: this.selection.some(s => s.metricName === i.metricName),
    }));
  }

  private async created () {
    this.active = this.options[0]?.value || '';
    await this.changeHandle(this.options[0]);
  }

  private getRowStyleByFilter ({ row }: { row: any }) {
    return (row?.metricCn || row?.metricName).indexOf(this.nameQuery) === -1 ? { display: 'none' } : { display: '' }
  }

  private async changeHandle (option: any) {
    if (!option) {
      return;
    }
    // 全量的
    const metricList = await this.fetchMetricList(option.value, option?.metricTypes, option?.metricFilter)
    const selectedMetrics = await this.getShowList();
    const selectedMetricsNames = (selectedMetrics || []).filter((i: any) => i.enabled).map((i: any) => i.metric);
    this.selection = metricList.filter((i: any) => selectedMetricsNames.includes(i.metricName));
    this.metricList = metricList.filter((i: any) => selectedMetrics.some((s: any) => s.metric === i.metricName));
    if (this.selection.length) {
      this.$nextTick(() => {
        this.selection.forEach((item) => {
          (this.$refs.table as Table).toggleRowSelection(item, true)
        })
      })
    }
  }

  private async fetchMetricList (type: string, metricTypes: any, metricFilter: any) {
    const { error, result } = await toAsyncWait(MetricApi.getAllMetricListByQuery(metricTypes))
    if (!error) {
      const _fullList = Object.entries(result?.data || {}).map(([key, value]: [string, any]) => ({
        metricName: key,
        ...value,
      }))
      const metricList = metricFilter ? metricFilter(_fullList) : _fullList
      this.cacheMap[type] = cloneDeep(metricList);
      return cloneDeep(metricList);
    } else {
      return [];
    }
  }

  private selectionChangeHandle (data: any[]) {
    this.selection = [...data]
  }
  private selectableFunc (row: any, index: number) {
    return this.selection.length < 3 || this.selection.find(item => item.metricName === row.metricName)

  }

  private dialogShowHandle () {
    this.showDialog = true
  }
  private dialogCloseHandle (payload?: any) {
    this.showDialog = false
    if (Array.isArray(payload?.selection)) {
      const newSelections = cloneDeep(payload.selection);
      this.metricList.push(...newSelections);
    }
  }

  private async postHandle () {
    // this.$emit('on-post', this.selection);
    this.selectLoading = true;
    const { error, result } = await toAsyncWait(Api.updateBusinessMetricSelect({
      metricSelectConfig: JSON.stringify([...this.getCurrentList]),
    }));
    this.selectLoading = false;
    if (!error) {
      this.$message.success(i18n.t('modules.views.aiPlatform.experts.s_3b108349') as string);
      this.$emit('on-post', this.selection);
      this.cancelHandle()
    } else if (error.message !== 'interrupt') {
      this.$message.error(i18n.t('modules.views.cockpit.component.s_6de920b4') as string);
    }
  }
  private cancelHandle () {
    this.$emit('on-cancel');
  }

  private async getShowList (): Promise<{ metric: string, enabled: boolean }[]> {
    this.selectLoading = true;
    const { error, result } = await toAsyncWait(Api.getBusinessMetricSelect());
    this.selectLoading = false;
    if (!error) {
      let selectedMetrics = result.data || [];
      try {
        selectedMetrics = JSON.parse(selectedMetrics || '[]');
      } catch {
        return [];
      }
      if (!Array.isArray(selectedMetrics)) {
        return [];
      }
      return selectedMetrics
    }
    return [];
  }

}
</script>

<style lang="scss" scoped>
.metric-cont {
  width: 100%;
  height: 100%;
  position: relative;
}
.border-bottom-1 {
  border-bottom: 1px solid var(--border-color-lighter);
}
.alert-info {
  line-height: 1;
  background: #E1E9FF;
  width: calc( 100% - 40px );
  padding: 12px;
}
.pb-64 {
  padding-bottom: 64px;
}
.cont-footer {
  height: 64px;
  padding: 16px 20px;
}
.ovy {
  overflow-y: auto;
}
:deep(.el-table th.el-table__cell .el-checkbox) {
  display: none;
}
</style>