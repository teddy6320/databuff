<template>
  <div class="chart-group">
    <div class="group-t">
      <div class="cont-item" v-loading="statusLoading">
        <div class="item-title">{{ $t('modules.views.configStatus.agent.s_82a1b6ef') }}</div>
        <div class="item-cont">
          <pie-chart
            :source="statusSource"
            :showEmpty="!statusLoading && !statusSource.length" />
        </div>
      </div>
      <div class="cont-item" v-loading="versionLoading">
        <div class="item-title">{{ $t('modules.views.configStatus.agent.s_88672902') }}</div>
        <div class="item-cont chart">
          <pie-chart
            :source="versionSource"
            :showEmpty="!versionLoading && !versionSource.length" />
        </div>
      </div>
    </div>

    <div class="group-t">
      <div class="cont-item" v-loading="uploadTopLoading">
        <div class="item-title">{{ $t('modules.views.configStatus.agent.s_163a8064') }}</div>
        <div class="item-cont">
          <horizontal-bar
            :source="uploadTopSource"
            :itemGap="8"
            :showEmpty="!uploadTopLoading && !uploadTopSource.length" />
        </div>
      </div>
      <div class="cont-item" v-loading="cpuLoading">
        <div class="item-title">{{ $t('modules.views.configStatus.agent.s_5fc52749') }}</div>
        <div class="item-cont">
          <horizontal-bar
            :source="cpuTopSource"
            :itemGap="8"
            :showEmpty="!cpuLoading && !cpuTopSource.length"
            unit="%" />
        </div>
      </div>
    </div>

    <div class="cont-item group-t high" v-loading="diffTopLoading">
      <div class="item-title">{{ $t('modules.views.configStatus.agent.s_dc2f513a') }}</div>
      <div class="item-cont">
        <horizontal-bar
          :source="diffTopSource"
          :itemGap="17"
          :showEmpty="!diffTopLoading && !diffTopSource.length"
          unit="s" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import HorizontalBar from '@/components/charts/horizontal-bar.vue';
import BasicChart from '@/components/charts/basic-chart.vue'
import PieChart from '@/components/charts/pie-chart-new.vue'
import { sortVersionObj } from '@/utils/compareVersion'
import { toAsyncWait } from '@/utils/common';
import AgentApi from '@/api/agent';

@Component({
  components: {
    HorizontalBar,
    BasicChart,
    PieChart,
  }
})
export default class ChartGroup extends Vue {
  private statusLoading = false;
  private statusSource: any[] = [];

  private versionLoading = false;
  private versionSource: any[] = [];

  private uploadTopLoading = false;
  private uploadTopSource: any[] = [];

  private cpuLoading = false;
  private cpuTopSource: any[] = [];

  private diffTopSource: any[] = [];
  private diffTopLoading = false;

  public getData () {
    this.getAgentStatus();
    this.getVersionSpread();
    this.getUploadTop();
    this.getCpuTop();
    this.getTimeDiffTop();
  }

  private async getAgentStatus() {
    this.statusLoading = true;
    const { result, error } = await toAsyncWait(AgentApi.getOnline({}))
    this.statusLoading = false;
    if (!error) {
      const data = (result || {}).data || {};
      this.statusSource = [
        { key: i18n.t('modules.views.configStatus.agent.s_68905cf3') as string, value: data.online || 0, color: '#08BE7E', },
        { key: i18n.t('modules.views.appMonitor.service.s_c195df63') as string, value: data.anomaly || 0, color: '#E12828', },
        { key: i18n.t('modules.views.configStatus.agent.s_50d4a850') as string, value: data.offline || 0, color: '#B5B7BB', },
      ].filter(item => !!item.value);
    }
  }

  private async getVersionSpread() {
    this.versionLoading = true;
    const { result, error } = await toAsyncWait(AgentApi.versionSpread())
    this.versionLoading = false
    if (!error) {
      const data: any[] = [];
      (result.data || []).map((item: any) => {
        const count = (item || {}).count || 0
        const version = ((item || {}).version || '').match(/(\d+\.?)*/g).filter((v: string) => !!v)[0] || i18n.t('modules.views.configStatus.agent.s_1622dc9b') as string
        const at = data.find(t => t.key === version)
        if (!at) {
          data.push({ key: version, value: count })
        } else {
          at.value += count
        }
      })
      this.versionSource = sortVersionObj(data).reverse();
    }
  }

  private async getCpuTop() {
    this.cpuLoading = true;
    const { result, error } = await toAsyncWait(AgentApi.getCpuTop({ topNum: 5 }))
    this.cpuLoading = false;
    if (!error) {
      const data = (result || {}).data || [];
      this.cpuTopSource = data.map((item: any) => ({
        ...item,
        value: item.value ? Number(String(item.value).replace('%', '')) / 100 : '-',
        name: item.key,
      }))
    }
  }

  private async getUploadTop() {
    this.uploadTopLoading = true;
    const { result, error } = await toAsyncWait(AgentApi.getUploadTop({ topNum: 5 }))
    this.uploadTopLoading = false;
    if (!error) {
      const data = (result || {}).data || [];
      this.uploadTopSource = data.map((item: any) => ({
        ...item,
        name: item.key,
      }))
    }
  }

  private async getTimeDiffTop() {
    this.diffTopLoading = true;
    const { result, error } = await toAsyncWait(AgentApi.timeDiffTop())
    this.diffTopLoading = false
    if (!error) {
      const data = (result || {}).data || [];
      const list: any[] = data.map((item: any) => {
        const { hostName = '未知', timeDiff = 0 } = item || {}
        return {
          name: hostName,
          value: timeDiff
        }
      })
      list.sort((a, b) => b.value - a.value);
      this.diffTopSource = list.slice(0, 10);
    }
  }
}
</script>

<style lang="scss" scoped>
.chart-group {
  margin-bottom: 20px;
  display: flex;
  .group-t {
    width: calc((100% - 32px) / 3);
    &+.group-t {
      margin-left: 16px;
    }
  }
}

.cont-item {
  width: 100%;
  height: 224px;
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  &+.cont-item {
    margin-top: 16px;
  }
  &.high {
    height: 464px;
  }

  .item-title {
    padding: 16px 20px 0;
    font-size: 14px;
    line-height: 22px;
    color: var(--color-text-primary);
  }

  .item-cont {
    height: calc(100% - 38px);
    padding: 12px 20px 0;
    &.chart {
      padding: 5px 10px 10px;
    }
  }
}
</style>
