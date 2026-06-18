<template>
  <div class="ts-info-cont p-16">
    <div v-if='current && current.length > 1' class="mb-15 flex-h-jc nosel">
      <span class="describe">{{ $t('modules.views.appMonitor.serviceAnalysis.s_6a2277a2', { value0: current.length }) }}</span>
      <div>
        <span @click="changeTsItemIdx(-1)" class="el-icon el-icon-arrow-left font-16 br-4 describe cphl mr-8"></span>
        <el-input-number v-model='tsItemIdx' :controls='false' :min="1" :max="current.length" size="mini" style='width: 50px' class="mini-ipt-number"></el-input-number>
        <i class="ml-5 mr-5 describe">/</i>
        <span class="describe">{{ current.length }}</span>
        <span @click="changeTsItemIdx(1)" class="el-icon el-icon-arrow-right font-16 br-4 describe cphl ml-8"></span>
      </div>
    </div>
    <div class="mb-15">
      <span class="bg-red text-white p-5 line-height-1 font-12 br-2 mr-10">{{ $t('modules.components.db-table.s_c195df63') }}</span>
      <span>{{ $t('modules.views.appMonitor.serviceAnalysis.s_461fc185') }}</span>
    </div>
    <template v-if='current && current[tsSourceArrayIdx] && current[tsSourceArrayIdx].info'>
      <template v-if='current[tsSourceArrayIdx].info.abnormalStartTime'>
        <div class="flex-h mb-10 font-12">
          <span class="w-80 flex-none describe mr-10">{{ $t('modules.views.appMonitor.serviceAnalysis.s_a1eed33c') }}</span>
          <span class="flex-1">{{ current[tsSourceArrayIdx].info.abnormalStartTime | TimesToDateFilter }} ~ {{ current[tsSourceArrayIdx].info.abnormalEndTime | TimesToDateFilter }}</span>
        </div>
      </template>
      <template v-if='current[tsSourceArrayIdx].info.abnormalReasonAndLinks'>
        <div v-for='(value, key) in current[tsSourceArrayIdx].info.abnormalReasonAndLinks' :key='key'  class="flex-h mb-10 font-12">
          <span class="flex-none w-80 describe mr-10">{{ $t('modules.views.appMonitor.serviceAnalysis.s_6b33cae0') }}</span>
          <span class="flex-1 ell" :title="key">{{ key }}</span>
          <a v-if='value' class="db-icon flex-none cp" :href="value" target="_blank">
            <span class="db-icon db-icon-link2 db-blue"></span>
          </a>
        </div>
      </template>
      <template v-if='current[tsSourceArrayIdx].info.problemId && current[tsSourceArrayIdx].info.problemDesc'>
        <div class="flex-h mb-10 font-12">
          <span class="w-80 flex-none describe mr-10">{{ $t('modules.views.appMonitor.serviceAnalysis.s_21225d31') }}</span>
          <span class="flex-1 ell" :title="current[tsSourceArrayIdx].info.problemDesc">{{ current[tsSourceArrayIdx].info.problemDesc }}</span>
          <span @click="viewProblemDetail(current[tsSourceArrayIdx].info.problemId)" class="db-icon db-icon-link2 db-blue flex-none cp"></span>
        </div>
      </template>
    </template>
    <div>
      <span class="db-icon db-icon-ai db-blue vm mr-10"></span>
      <span class="describe font-12">{{ $t('modules.views.appMonitor.serviceAnalysis.s_31a4e666') }}</span>
    </div>
  </div>
</template>
<script lang='ts'>
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
@Component({})
export default class ChartTsSlot extends Vue {
  @Prop({ default: {} }) private current!: any;

  @Watch('current', { immediate: true })
  private onCurrent () {
    this.tsItemIdx = 1;
  }

  private tsItemIdx = 1;
  get tsSourceArrayIdx () {
    return this.tsItemIdx - 1;
  }

  // 跳转到问题详情
  private viewProblemDetail (problemId: string) {
    const routeData = this.$router.resolve({
      path: '/alarmCenter/problemDetail',
      query: { id: problemId, __nw: 't' },
    });
    window.open(routeData.href, '_blank');
  }

  private changeTsItemIdx (num: 1 | -1) {
    if ((this.tsItemIdx <= 1 && num === -1) || (this.tsItemIdx >= this.current.length && num === 1)) {
      return;
    }
    this.tsItemIdx = this.tsItemIdx + num;
  }

}
</script>
<style scoped lang='scss'>
.ts-info-cont {
  width: 600px;
  z-index: 12;
}
:deep(.mini-ipt-number .el-input__inner) {
  padding: 1px 5px;
}
</style>