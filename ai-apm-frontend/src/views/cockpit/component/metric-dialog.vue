<template>
  <el-dialog
    :visible.sync="showModel"
    :title="$t('modules.views.cockpit.component.s_4f341b4e')"
    :before-close="() => dialogCancelHandle(null, true)"
    :close-on-click-modal='!dialogPostLoading'
    :close-on-press-escape='!dialogPostLoading'
    :show-close='!dialogPostLoading'
    width='650px'
    append-to-body
  >
    <div>
      <div>
        <el-input v-model='nameQuery' size="mini" :placeholder="$t('modules.views.alarmCenter.alarmDetail.s_e5f71fc3')" class="mb-10"></el-input>
      </div>
      <div class="flex-1 ovy mb-15">
        <el-table
          :data="metricList"
          border
          height="400px"
          @selection-change="selectionChangeHandle"
          size="small"
        >
          <el-table-column type="selection"></el-table-column>
          <el-table-column prop="name" :label="$t('modules.views.appMonitor.dbConnPool.s_b8403584')" min-width="120">
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

      <div slot="footer" class="tr">
        <el-button size="small" :disabled="dialogPostLoading" @click="dialogCancelHandle">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :loading="dialogPostLoading" @click="postHandle">{{ $t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') }}</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { toAsyncWait } from '@/utils/common';
import { cloneDeep } from 'lodash';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import Api from '../api'

@Component({})
export default class DialogComp extends Vue {
  @Prop() private value!: boolean;
  @Prop() private list!: any;
  @Prop() private currentList!: any;

  @Watch('value')
  private onShowChange (newVal: boolean) {
    this.showModel = newVal
  }
  @Watch('list', { immediate: true, deep: true })
  private onListChange (newVal: any[]) {
    this.metricList = newVal
  }

  private nameQuery = '';
  private showModel = false;
  private dialogPostLoading = false;

  private selection: any[] = [];
  private metricList: any = [];


  // 关闭弹窗
  private dialogCancelHandle (payload?: any) {
    this.showModel = false;
    this.dialogPostLoading = false;
    this.$emit('on-close', payload);
    this.$emit('input', false);
  }

  private async postHandle () {
    if (this.selection.length > 0) {
      this.dialogPostLoading = true;
      const addedMetrics = this.selection.map(i => ({
        metric: i.metricName,
        enabled: false,
      }))
      const { error, result } = await toAsyncWait(Api.updateBusinessMetricSelect({
        
        metricSelectConfig: JSON.stringify([
          ...addedMetrics,
          ...this.currentList
        ]),
      }));
      this.dialogPostLoading = false;
      if (!error) {
        this.dialogCancelHandle({ selection: cloneDeep(this.selection) });
      }
    } else {
      this.dialogCancelHandle();
    }
  }
  
  private selectionChangeHandle (data: any[]) {
    this.selection = [...data]
  }
}
</script>

<style lang="scss" scoped>

</style>
