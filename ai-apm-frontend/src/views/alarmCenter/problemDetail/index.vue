<template>
  <div class="db-detail-wrapper">
    <div class="db-detail" :class="`db-detail-${activeName}`" v-loading="isLoading">
      <div class="db-detail-header">
        <text-expand
          @on-toggle="titleToggleHandle"
          :content="detail.problemDesc || '-'"
          :lineHeight="20"
          :maxLines="1"
          class="detail-title mb-20" />
        <div class="detail-info">
          <div class="info-row">
            <div class="info-t flex-h"><span class="label">{{ $t('modules.views.alarmCenter.problemDetail.s_82a9a9ef') }}</span>{{ detail.problemShowId || '-' }}</div>
            <div class="info-t flex-h"><span class="label">{{ $t('modules.views.alarmCenter.problemDetail.s_b90cf2f3') }}</span>{{ $t('modules.views.alarmCenter.problemDetail.s_8fcfc344', { value0: detail.influenceServiceCount | NumberFilter }) }}</div>
          </div>
          <div class="info-row">
            <div class="info-t flex-h"><span class="label">{{ $t('modules.views.configManage.alarm.s_592c5958') }}</span>{{ detail._problemStartTime || '-' }}</div>
            <div class="info-t flex-h"><span class="label">{{ $t('modules.views.alarmCenter.problemDetail.s_df713dfb') }}</span>{{ detail._beginToActionTime || '-' }}</div>
            <div class="info-t flex-h"><span class="label">{{ $t('modules.components.charts.s_f782779e') }}</span>{{ detail._problemEndTime || '-' }}</div>
            <div class="info-t flex-h"><span class="label">{{ $t('modules.views.alarmCenter.problemDetail.s_f102c038') }}</span>{{ detail.problemRepair | NumberFilter }} min</div>
            <div class="info-t flex-h"><span class="label">{{ $t('modules.views.alarmCenter.problemDetail.s_1148b783') }}</span>{{ detail.problemAction | NumberFilter }} min</div>
          </div>

          <div class="ai-watermark">
            <span class="db-icon-intelligence ai-watermark-icon"></span>
            <span>{{ $t('modules.views.alarmCenter.problemDetail.s_c2d3cc0a') }}<br>{{ $t('modules.views.alarmCenter.problemDetail.s_6015b16c') }}</span>
          </div>
        </div>
      </div>

      <div class="db-detail-content">
        <db-tabnav
          v-model="activeName"
          :tabnavs="tabs.filter(item => !item.hide)"
          @on-change="toggleTabHandle"
          :thin="true"
          class="detail-tabs">
          <span slot-scope="{ tab }">{{ tab.labelKey ? $t(tab.labelKey) : tab.label }}<i v-if="tab.icon" :class="tab.icon" class="tab-icon"></i></span>
        </db-tabnav>

        <alarm
          v-if="activeName === 'alarm'"
          ref="alarm"
          :queryParams="getQueryParams"
          :detail="detail"
          class="detail-tabs-pane-wrap" />

        <cause-tree
          v-else-if="activeName === 'causeTree'"
          ref="causeTree"
          :source="detail.influence"
          :suggest="detail.suggest"
          :suggestStatus="detail.suggestStatus"
          :showFeedback="true"
          :feedbackStatus="detail.feedbackStatus"
          :feedbackMessage="detail.feedbackMessage"
          @retry-suggest="retrySuggest"
          @feedback="showDialogHandle"
          class="detail-tabs-pane-wrap" />
      </div>
    </div>

    <!--定位准确性反馈弹窗 -->
    <feedback-dialog
      :showModel="showDialogModel"
      :id="problemId"
      :status="(detail || {}).feedbackStatus"
      :remark="(detail || {}).feedbackMessage"
      @on-saved="dialogSavedHandle"
      @on-close="showDialogModel = false" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { getTimeRange } from '@/utils/timeFormat';
import { toAsyncWait } from '@/utils/common';
import RootCauseApi from '@/api/rootCause';
import AlarmApi from '@/api/alarm';
import CauseTree from './cause-tree.vue';
import Alarm from './alarm.vue';
import TextExpand from '@/components/text-expand/index.vue';
import FeedbackDialog from './feedback-dialog.vue';
import dayjs from 'dayjs';

@Component({
  components: {
    CauseTree,
    Alarm,
    TextExpand,
    FeedbackDialog,
  }
})
export default class ProblemDetail extends Vue {
  public $refs!: {
    causeTree: CauseTree
    alarm: Alarm
  }

  private detail: any = {}
  private isLoading = false

  private problemId: string = ''

  get getQueryParams () {
    const { problemStartTime, problemEndTime } = this.detail
    const startTime = +new Date(problemStartTime) - 10 * 60 * 1000
    const endTime = +new Date(problemEndTime) + 10 * 60 * 1000
    const timeRange = getTimeRange(startTime, endTime, 30 * 60 * 1000);
    return {
      fromTime: dayjs(timeRange.start).format('YYYY-MM-DD HH:mm:ss'),
      toTime: dayjs(timeRange.end).format('YYYY-MM-DD HH:mm:ss'),
      interval: timeRange.interval,
      problemId: this.problemId || '',
    };
  }

  private tabs = [
    { label: i18n.t('modules.views.alarmCenter.problemDetail.s_57565fcf') as string, labelKey: 'modules.views.alarmCenter.problemDetail.s_57565fcf', value: 'alarm' },
    { label: i18n.t('modules.views.alarmCenter.problemDetail.s_e8f54f03') as string, labelKey: 'modules.views.alarmCenter.alarmDetail.s_e8f54f03', value: 'causeTree', icon: 'db-icon-ai' },
  ]
  private activeName: string = 'causeTree'

  private created () {
    const { id = '' } = this.$route.query;
    this.problemId = decodeURIComponent(id as string);

    const type: any = this.$route.query.type
    if (this.tabs.find(t => t.value === type)) {
      this.activeName = type
    }
  }

  private mounted () {
    this.durationChangeHandle();
  }

  // 时间范围改变
  private async durationChangeHandle () {
    if (!this.problemId && !this.$route.query.sn) {
      return;
    }
    await this.getDetail()
    this.$nextTick(() => {
      const $refComp = (this.$refs as any)[this.activeName]
      if ($refComp && $refComp.getData) {
        $refComp.getData();
      }
    })
  }

  private toggleTabHandle(tab: any) {
    const { value } = tab;
    this.activeName = value
    const type = this.$route.query.type
    if (value !== type) {
      this.$router.replace({ query: { ...this.$route.query, type: value } })
    }
    if (!this.problemId) {
      return;
    }
    this.$nextTick(() => {
      const $refComp = (this.$refs as any)[this.activeName]
      if ($refComp && $refComp.getData) {
        $refComp.getData();
      }
    })
  }

  // 展开/收起标题
  private titleToggleHandle () {
    this.$nextTick(async () => {
      const $refComp = (this.$refs as any)[this.activeName]
      if ($refComp && $refComp.resize) {
        $refComp.resize();
      }
    })
  }

  // 获取详情
  private async getDetail () {
    this.isLoading = true;
    let params: any = { id: this.problemId }
    if (!this.problemId) {
      const { sn, abnormalFirstTime, isRoot, abnormalDetail, fromTime, toTime } = this.$route.query
      params = {
        service: decodeURIComponent(sn as string || ''),
        serviceType: 'SERVICE',
        fromTime: +fromTime,
        toTime: +toTime,
        abnormalFirstTime: +(abnormalFirstTime || ''),
        isRoot: +isRoot === 1,
      }
      if (abnormalDetail) {
        params.abnormalDetail = JSON.parse(decodeURIComponent(abnormalDetail as string))
      }
    }
    const fetchDetailUrl = this.problemId ? 'getProblemDetail' : 'getInfluenceAnalysis'
    const { result, error } = await toAsyncWait(RootCauseApi[fetchDetailUrl](params));
    this.isLoading = false;
    const data = (result || {}).data
    if (!error && data?.id) {
      const startTime = data.problemStartTime || '-'
      const endTime = data.problemEndTime || '-'
      const actionTime = data.beginToActionTime || '-'
      this.detail = {
        ...data,
        influenceServiceCount: data.influenceServiceCount || 0,
        _problemStartTime: startTime.slice(0, 16),
        _problemEndTime: endTime.slice(0, 16),
        _beginToActionTime: actionTime.slice(0, 16),
        problemRepair: (+new Date(endTime) - +new Date(startTime)) / 1000 / 60 + 1,
        problemAction: (+new Date(actionTime) - +new Date(startTime)) / 1000 / 60 + 1,
        suggestStatus: +data.suggestStatus,
      }
      if (!this.problemId) {
        this.problemId = data.id;
        this.$router.replace({
          query: { id: encodeURIComponent(this.problemId) }
        })
        this.$store.dispatch('Service/GET_BASIC_GROUP_SERVICE');
        this.$store.dispatch('User/getAIEnabled');
      }
    } else {
      this.detail = {}
      this.$confirm(i18n.t('modules.views.alarmCenter.problemDetail.s_c911686e') as string, i18n.t('common.hint') as string, {
        confirmButtonText: i18n.t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') as string, confirmButtonTextKey: 'modules.views.alarmCenter.alarmDetail.s_38cf16f2',
        closeOnClickModal: false,
        showCancelButton: false,
        showClose: false,
        type: 'warning'
      }).then(() => {
        this.$router.replace({
          path: '/alarmCenter/rootCause',
          query: { ...this.getRouteTimeOrRange }
        })
      })
    }
  }

  // 重试
  private async retrySuggest () {
    this.isLoading = true;
    const params = { problemId: this.problemId }
    const { result, error } = await toAsyncWait(AlarmApi.retrySuggest(params));
    this.isLoading = false;
    if (!error) {
      this.getDetail();
    } else if (error.message !== 'interrupt') {
      this.$message.error(i18n.t('modules.views.alarmCenter.problemDetail.s_41b57859') as string)
    }
  }

  // 定位准确性反馈
  private showDialogModel = false;
  private showDialogHandle () {
    this.showDialogModel = true
  }
  private dialogSavedHandle (payload: any) {
    this.showDialogModel = false
    this.detail.feedbackStatus = payload.status
    this.detail.feedbackMessage = payload.message
  }
}
</script>

<style lang="scss" scoped>
.db-detail-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  font-size: 13px;
  color: var(--color-text-primary);
  overflow: auto;

  .db-detail {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 600px;
  }

  .db-detail-header {
    flex: none;
    padding: 20px;
    margin-bottom: 16px;
    background-color: var(--bg-color);
    border-radius: 4px;
  }

  .db-detail-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    background-color: var(--bg-color);
    border-radius: 4px;
    overflow: hidden;
  }
}

.db-detail-header {
  .detail-title {
    font-size: 16px;
    line-height: 20px;
    font-weight: 500;
  }

  .detail-info {
    padding: 20px 20px 8px;
    border: 1px solid var(--border-color-lighter);
    border-radius: 4px;
    line-height: 14px;
    font-size: 13px;
    position: relative;
    .info-row {
      margin-bottom: 12px;
      display: flex;
      flex-wrap: wrap;
    }
    .info-t {
      margin-right: 30px;
    }
    .label {
      flex: none;
      margin-right: 16px;
      min-width: 52px;
      color: var(--color-text-secondary);
    }

    .ai-watermark {
      display: flex;
      align-items: center;
      font-size: 12px;
      line-height: 18px;
      margin-top: -20px;
      position: absolute;
      top: 50%;
      right: 20px;
      pointer-events: none;
      .ai-watermark-icon {
        margin-right: 14px;
        font-size: 40px;
        color: var(--color-text-regular);
      }
    }
  }
}

.db-detail-content {
  .detail-tabs {
    flex: none;
    padding: 17px 20px 0;
    border-bottom: 1px solid var(--border-color-lighter);
    .tab-icon {
      margin-left: 5px;
      -webkit-text-stroke: initial;
    }
  }

  .detail-tabs-pane-wrap {
    flex: 1;
    padding: 20px;
    overflow: hidden;
  }
}
</style>
