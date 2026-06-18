<template>
  <div class="aside-detail-wrap">
    <div class="aside-detail-header">
      <div class="aside-detail-title">
        <span class="db-icon font-16 mr-6 icon-vm">{{ detail.serviceIcon | DbIconFilter }}</span>
        <a v-if="detail.serviceUrl && urlPermMap[detail.serviceUrl] && detail.serviceName" :href="locationOrigin + detail.serviceUrl" target="_blank" class="cphu">{{ detail.serviceName || '-' }}</a>
        <template v-else>{{ detail.serviceName || '-' }}</template>
      </div>

      <div v-if="(showAlarmBtn && detail.alarmCount && sidMapping[detail.serviceId]) || showFeedback" class="flex-none">
        <template v-if="showFeedback">
          <el-button
            v-if="!feedbackStatus"
            @click="feedbackHandle"
            size="small" class="ml-10">{{ $t('modules.views.alarmCenter.problemDetail.s_3ed6fd57') }}</el-button>
          <div v-else class="flex-h dif ml-10">
            <span v-if="feedbackStatus === '准确'" class="el-icon-circle-check green mr-4"></span>
            <span v-else class="el-icon-circle-close red mr-4"></span>
            {{ $t('modules.views.alarmCenter.problemDetail.s_5e191e26', { value0: feedbackStatus }) }}
          </div>
          <el-button
            v-if="feedbackStatus && feedbackMessage"
            @click="feedbackHandle"
            size="small" class="ml-10">{{ $t('modules.views.alarmCenter.problemDetail.s_bd090e93') }}</el-button>
        </template>

        <el-button
          v-if="showAlarmBtn && detail.alarmCount && sidMapping[detail.serviceId]"
          @click="showAlarmHandle"
          size="small" class="ml-10">{{ $t('modules.views.alarmCenter.problemDetail.s_26ed08cb') }}</el-button>
      </div>
    </div>

    <div class="aside-detail-content">
      <div class="aside-detail-title mb-10">{{ $t('modules.views.alarmCenter.problemDetail.s_48146693') }}</div>
      <template v-for="(item, index) in rootCaseList">
        <template v-if="index === 0 || showOther">
          <div :key="item.id" :class="{ 'mt-18': index > 0 }" class="aside-detail-sub-title mb-8">
            {{ index === 0 ? $t('modules.views.alarmCenter.problemDetail.s_3f981012') : '' }}{{ $t('modules.views.alarmCenter.problemDetail.s_ded04c07') }}<a v-if="item.url && urlPermMap[item.url]" :href="locationOrigin + item.url" target="_blank" class="blue cp">{{ $t('modules.views.alarmCenter.problemDetail.s_47d68cd0') }}{{ detail.serviceName }}，{{ item.name || '-' }}</a>
            <template v-else>{{ item.name || '-' }}</template>
          </div>
          <div :key="item.id" class="aside-detail-sub-title mb-10">{{ $t('modules.views.alarmCenter.problemDetail.s_f2c37805') }}</div>
          <div :key="`table-${item.id}`" class="aside-detail-list">
            <el-table
              :data="item.list"
              max-height="400"
              :empty-text="$t('modules.components.charts.s_21efd88b')"
              highlight-current-row size="small"
              tooltip-effect="light"
              class="table">
              <el-table-column
                v-for="col in item.columns"
                :key="col.value"
                :label="col.label"
                min-width="120"
                show-overflow-tooltip>
                <template slot-scope="{ row }">{{ row[col.value] || '-' }}</template>
              </el-table-column>

              <el-table-column key="actions" :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" :width="60">
                <template slot-scope="{ row }">
                  <a v-if="row.url && urlPermMap[row.url]" :href="locationOrigin + row.url" target="_blank" class="blue cp">{{ $t('modules.views.alarmCenter.problemDetail.s_f26225bd') }}</a>
                  <span v-else class="describe cn">{{ $t('modules.views.alarmCenter.problemDetail.s_f26225bd') }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
        <template v-if="index === 0 && rootCaseList.length > 1">
          <div :key="item.id"
            class="aside-detail-sub-title mt-10 mb-16">{{ $t('modules.views.alarmCenter.problemDetail.s_871aab11') }}</div>
          <a :key="item.id"
            @click="showOther = !showOther"
            class="dib blue cp font-13 lh-18">{{ $t('modules.views.alarmCenter.problemDetail.s_fba26eab') }}<i :class="{ active: showOther }" class="db-icon-down font-12 ml-5 aside-list-arrow"></i>
          </a>
        </template>
      </template>

      <div v-if="!rootCaseList.length" class="aside-detail-empty describe">{{ $t('modules.views.alarmCenter.problemDetail.s_6c5443ce') }}</div>

      <div v-if="aiEnabled && detail.isRoot && suggest && rootCaseList.length" class="aside-detail-suggest">
        <div class="aside-detail-split"></div>
        <div class="aside-detail-title mb-10">{{ $t('modules.views.alarmCenter.problemDetail.s_95f849eb') }}</div>
        <marked-view
          v-if="suggestStatus === 2"
          :data="suggest || ''"
          :showCopy="false"
          class="item-marked-cont" />
        <div v-else>{{ $t('modules.views.alarmCenter.problemDetail.s_ca14f914') }}<br>
          <el-button
            @click="retrySuggest"
            type="primary" plain size="small" class="mt-10">{{ $t('modules.views.alarmCenter.problemDetail.s_132c5cdc') }}</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import MarkedView from '@/components/marked-view.vue';

const getUrlParam = (name: string, url: string) => {
  const reg = new RegExp(`(^|&)${name}=([^&]*)(&|$)`, 'i');
  const r = (url ? url.split('?')[1] : window.location.search.substring(1)).match(reg);
  return r !== null ? decodeURIComponent(r[2]) : null;
}

@Component({
  components: {
    MarkedView,
  },
})
export default class AsideDetail extends Vue {
  @Prop({ default: () => ({}) }) private detail!: any;
  @Prop({ default: false }) private showFeedback!: boolean
  @Prop({ default: '' }) private feedbackStatus!: string
  @Prop({ default: '' }) private feedbackMessage!: string
  @Prop({ default: true }) private showAlarmBtn!: boolean;
  @Prop({ default: '' }) private suggest!: string;
  @Prop({ default: -1 }) private suggestStatus!: number;

  @Watch('detail.id')
  private onDetailChange () {
    this.showOther = false;
  }

  get isProblemDetail () {
    return this.$route.path === '/alarmCenter/problemDetail';
  }

  get aiEnabled () {
    return this.$store.getters['User/getAIEnabled'];
  }

  get rootCaseList () {
    return (this.detail || {}).rootCaseList || [];
  }

  get affectedApp () {
    return this.detail?.affectedApp || {};
  }

  get locationOrigin () {
    return window.location.origin
  }

  get sidMapping () {
    const mapping: any = {}
    const basicServiceMap = this.$store.getters['Service/basicGroupServiceMap'];
    Object.keys(basicServiceMap || {}).forEach((t: string) => {
      mapping[t] = true
    });
    return mapping;
  }

  get urlPermMap () {
    const mapping: any = {}
    const urls: string[] = [this.detail.serviceUrl]
    this.rootCaseList.forEach((item: any) => {
      urls.push(item.url)
      urls.push(...item.list.map((t: any) => t.url))
    });
    Array.from(new Set(urls.filter(t => !!t))).forEach(url => {
      const sid = getUrlParam('sid', url);
      const srcSid = getUrlParam('srcSid', url);
      const dstSid = getUrlParam('dstSid', url);
      mapping[url] = (!sid || this.sidMapping[sid]) && (!srcSid || this.sidMapping[srcSid]) && (!dstSid || this.sidMapping[dstSid]);
    });
    return mapping;
  }

  private showOther = false;

  private created () {
    this.$store.dispatch('Service/GET_BASIC_GROUP_SERVICE');
    this.$store.dispatch('User/getAIEnabled');
  }

  // 查看告警
  private showAlarmHandle () {
    this.$router.push({
      path: '/alarmCenter/alarm',
      query: {
        serviceId: encodeURIComponent(this.detail.serviceId),
        fromTime: `${this.detail.start}`,
        toTime: `${this.detail.end}`,
      },
    });
  }

  // 重试
  private retrySuggest () {
    this.$emit('retry-suggest');
  }

  // 反馈
  private feedbackHandle () {
    this.$emit('feedback');
  }
}
</script>

<style lang="scss" scoped>
.aside-detail-wrap {
  padding: 20px;
  height: 100%;
  background: var(--bg-color);
  box-shadow: 0 0 6px 0 var(--shadow-color02);
  position: relative;

  .aside-detail-header {
    margin-top: -2px;
    margin-bottom: 16px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .aside-detail-title {
    font-size: 14px;
    line-height: 20px;
    font-weight: 500;
    color: var(--color-text-primary);
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
  }

  .aside-detail-content {
    height: calc(100% - 32px);
    overflow: auto;
  }

  .aside-detail-sub-title {
    height: 18px;
    font-size: 13px;
    line-height: 18px;
    color: var(--color-text-secondary);
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
  }

  .aside-list-arrow {
    display: inline-block;
    transition: transform .3s;
    &.active {
      transform: rotate(180deg);
    }
  }

  .aside-detail-empty {
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .aside-detail-suggest {
    font-size: 13px;
  }

  .aside-detail-split {
    margin: 20px 0;
    border-top: 1px solid var(--border-color-lighter);
  }
}
</style>
