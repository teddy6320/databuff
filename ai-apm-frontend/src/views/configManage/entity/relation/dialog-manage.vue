<template>
  <el-dialog
    :visible.sync="showModel"
    :title="$t('modules.views.configManage.entity.s_ef6b463a')"
    :before-close="() => dialogCancelHandle(null, true)"
    :close-on-click-modal='!dialogPostLoading'
    :close-on-press-escape='!dialogPostLoading'
    :show-close='!dialogPostLoading'
    width='840px'
    append-to-body
  >
    <div v-loading='isLoading'>
      <div class="mb-15">
        <el-radio-group v-model='params.displayType'>
          <el-radio :label='1'>{{ $t('modules.views.configManage.entity.s_29862ce7') }}</el-radio>
          <el-radio :label='2'>{{ $t('modules.views.configManage.entity.s_88d2a67d') }}</el-radio>
        </el-radio-group>
      </div>

      <template v-if='params.displayType === 1'>
        <div class="mb-15">
          <div v-for='item,index in params.list' :key="index" class="metric-item mb-15 flex-h">
            <div class="mr-6">{{ $t('modules.views.configManage.entity.s_e6311e3b', { value0: index + 1 }) }} </div>
            <div
              class="query-visual-cont-item-select">
              <metric-select
                v-model="item.metricName"
                :key="`${index}-metric`"
                @change="metricChangeHandle($event, index)"
                :options="getMetricList"
                :loading="metricLoading"
                :clearable="true"
                :disabled="dialogPostLoading"
                :placeholder="$t('modules.views.configManage.alarm.s_4837e0e6')"
                size="mini"
                class="w-320"
              />

            </div>
            <div class="ml-15 mr-6">{{ $t('modules.views.dataReport.report.s_66b615ec') }}</div>
            <el-input
              v-model="item.alias"
              size="small"
              :placeholder="$t('modules.views.configManage.entity.s_7dbd7b27')"
              :disabled="dialogPostLoading"
              class="ml-6"
              style="width: 150px;" />
            <div class="w-60 ml-10">
              <span v-if='index === params.list.length - 1 && index !== 2' @click="addItemHandle" class="db-icon db-icon-add2 mr-10 font-16 cphl"></span>
              <span v-if='params.list.length > 1' @click="removeItemHandle(index)" class="db-icon db-icon-delete2 font-16 cphl-r"></span>
            </div>
          </div>
        </div>
      </template>

      <template v-else>
        <div></div>
      </template>
      <div slot="footer" class="tr">
        <el-button size="small" :disabled="dialogPostLoading" @click="dialogCancelHandle">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :loading="dialogPostLoading" @click="postHandle">{{ $t('modules.views.configInstall.plugin.s_be5fbbe3') }}</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import MetricCascader from '@/components/metric-type-cascader.vue';
import MetricSelect from '@/components/metric-select.vue';
import SystemApi from '@/api/system';
import { toAsyncWait } from '@/utils/common';
import { cloneDeep } from 'lodash';

@Component({
  components: {
    MetricCascader,
    MetricSelect
  }
})
export default class DialogComp extends Vue {
  @Prop() private value!: boolean;
  @Prop() private current!: any;
  @Prop() private metricList!: any[];
  @Prop() private metricLoading!: boolean;

  @Watch('value')
  private async onShowChange (newVal: boolean) {
    this.showModel = newVal
    if (newVal) {
      this.isLoading = true;
      await this.getMetricTypeList(this?.current?.app || '');
      this.isLoading = false;
    }
  }

  private params = {
    displayType: 1,
    list: [
      { metricName: '', alias: '' },
    ],
  }

  private detail: any = {};

  private showModel = false;
  private dialogPostLoading = false;
  private isLoading = false;

  get getMetricList () {
    return this.current?.app ? this.metricList.filter((item: any) => item.app === this.current.app).map((it: any) => it.metricList).flat() : [];
  }

  private beforeDestroy () {
    this.dialogCancelHandle();
  }

  // 关闭弹窗
  private dialogCancelHandle (payload?: any) {
    this.showModel = false;
    this.$emit('on-close', payload);
    this.$emit('input', false);
  }

  private async postHandle () {
    this.dialogPostLoading = true;
    // console.log('postHandle', this.params);
    // console.log('postHandle', cloneDeep(this.current), cloneDeep(this.params), cloneDeep(this.detail));
    const params = {
      ...this.detail,
      displayType: this.params.displayType,
      selectedMetrics: this.params.list,
      middlewareType: this?.current?.app || '',
    }
    const { error, result } = await toAsyncWait(SystemApi.setMidTypeMetric(params));
    if (!error) {
      this.$message.success(i18n.t('modules.views.aiPlatform.experts.s_3b108349') as string);
      this.dialogCancelHandle(params);
    } else if (error?.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.cockpit.tab.s_6de920b4') as string);
    }
    this.dialogPostLoading = false;
  }
  private metricChangeHandle () {
    //
  }
  private async getMetricTypeList (middlewareType: string) {
    const { result, error } = await toAsyncWait(SystemApi.getMidTypeMetricList(middlewareType));
    if (!error) {
      this.detail = {...(result?.data || {})} || {};
      this.params.displayType = result?.data?.displayType || 1;
      const list = Array.isArray(result?.data?.selectedMetrics) && result?.data?.selectedMetrics.length > 0 ? result?.data?.selectedMetrics : [{ metricName: '', alias: '' }];
      list.splice(3);
      this.params.list = list;
    }
  }

  private addItemHandle () {
    if (this.params.list.length >= 3) {
      this.$message.warning(i18n.t('modules.views.configManage.entity.s_afc779e3') as string);
      return;
    }
    this.params.list.push({ metricName: '', alias: '' });
  }

  private removeItemHandle (index: number) {
    if (this.params.list.length <= 1) {
      this.$message.warning(i18n.t('modules.views.configManage.entity.s_ceea9990') as string);
      return;
    }
    this.params.list.splice(index, 1);
  }
}
</script>

<style lang="scss" scoped>

</style>
