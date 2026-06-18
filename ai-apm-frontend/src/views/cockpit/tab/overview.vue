<template>
  <div class="cont">
    <div class="bg-color p-16 br-4 pb-0 mb-20">
      <div class="section-title">{{ $t('modules.views.cockpit.tab.s_c98d6419') }}</div>
      <el-row :gutter="16">
        <template v-for='item in entityDatas'>
          <el-col :key="item.value" :span='8' v-loading='item.loading' v-if="!item.disabled">
            <CardTrend :value="item" class="mb-16" />
          </el-col>
        </template>
      </el-row>
    </div>

    <div class="bg-color p-16 br-4 pb-0">
      <div class="section-title flex-h-jc">
        <span>{{ $t('modules.views.cockpit.tab.s_14924bdf') }}</span>
      </div>
      <el-row :gutter="16">
        <el-col :span='12'>
          <el-table :data='alarmData' v-loading='alarmLoading' size='small'>
            <el-table-column :label="$t('modules.views.alarmCenter.alarm.s_fc7e3846')" prop='3'>
              <template slot-scope="{ row }">
                <span @click='viewAlarmList(3)' :class='["fw-500 font-20 cp alarm-num-item", currClickLevel === 3 ? "active" : "", "red"]'>{{ row['3'] }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('modules.views.alarmCenter.alarm.s_bde77082')" prop='2'>
              <span @click='viewAlarmList(2)' slot-scope="{ row }" :class='["fw-500 font-20 cp alarm-num-item", currClickLevel === 2 ? "active" : "", "yellow"]'>{{ row['2'] }}</span>
            </el-table-column>
            <el-table-column :label="$t('modules.views.alarmCenter.alarm.s_01ceb3ed')" prop='1'>
              <span @click='viewAlarmList(1)' slot-scope="{ row }" :class='["fw-500 font-20 cp alarm-num-item", currClickLevel === 1 ? "active" : ""]'>{{ row['1'] }}</span>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span='12'>
          <div v-loading='tableLoading'>
            <div
              v-for="(item, index) in alarmList"
              :key="index"
              class="alarm-item">
              <el-tooltip effect="light" :content='item.level | AlarmStatusFilter' placement="top">
                <span class="alarm-status" :data-status='item.level'></span>
              </el-tooltip>
              <div class="flex-h">
                <div
                  @click="viewEventHandle(item)"
                  class="name">{{ item.descriptionKey ? $t(item.descriptionKey) : item.description }}</div>
              </div>
              <div class="flex-h-jc">
                <div class="time">{{ item.timestamp | TimesToDateFilter }}</div>
              </div>
            </div>

            <div class="tc">
              <span v-show='alarmList.length > 0' @click="viewMoreAlarmList" class="blue cphu">{{ $t('modules.views.appMonitor.relationMap.s_90ef7c48') }} <i class="el-icon-arrow-right"></i></span>
              <span v-show="!alarmList.length" class="describe mt-40 dib">{{ $t('modules.components.charts.s_21efd88b') }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import CardTrend from '../component/card-trend.vue';
import i18n from '@/i18n';
import { toAsyncWait } from '@/utils/common';
import Api from '../api'
import AlarmApi from '@/api/alarm'
import dayjs from 'dayjs';

@Component({
  components: {
    CardTrend,
  },
})
export default class OverviewComp extends Vue {
  @Watch('globalTimeV2', { deep: true })
  private onGlobalTimeV2Change () {
    this.durationChange();
  }
  private entityDatas = [
    { title: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, value: 'SERVICE', color: '#2962ff', data: {}, loading: false, },
    // { title: i18n.t('modules.views.cockpit.tab.s_6aa2fa41') as string, titleKey: 'modules.views.cockpit.tab.s_6aa2fa41', value: 'tech', color: '#00AFF4', data: {} },
    // { title: i18n.t('modules.views.cockpit.tab.s_7cb69f1f') as string, titleKey: 'modules.views.cockpit.tab.s_7cb69f1f', value: 'docker', color: '#00AFF4', data: {} },
    // { title: 'Kubernetes', value: 'k8s', color: '#2962ff', data: {} },
    // { title: i18n.t('modules.views.alarmCenter.alarm.s_f88522cf') as string, value: 'process', color: '#2962ff', data: {} },
  ]

  private currClickLevel = 3;

  private alarmData = [
    { 3: 0, 2: 0, 1: 0, loading: false },
  ]

  private tableLoading = false;
  private alarmLoading = false;
  private alarmList = [];
  
  private async created () {
    this.durationChange();
  }

  private mounted () {
    // 监听全局的手动刷新事件，created中会有不触发的情况
    // 子组件绑定会被覆盖
    this.$eventBus.$on('GlobalRefresh', this, () => {
      this.durationChange()
    });
  }

  private beforeDestroy () {
    this.$eventBus.$off('GlobalRefresh')
  }

  private async getData (type: string) {
    const { fromTime, toTime, interval } = this.getGlobalTime();
    const { error, result } = await toAsyncWait(Api.getEntityData({ type, fromTime: fromTime.valueOf(), toTime: toTime.valueOf(), interval }));
    if (!error) {
      const { data = {} } = result || {};
      const dataIndex = this.entityDatas.findIndex(item => item.value === type);
      if (dataIndex !== -1) {
        const scoreList = Array.isArray(data?.healthRangeScoreList) ? data.healthRangeScoreList : [];
        this.$set(this.entityDatas[dataIndex], 'data', {
          errCnt: scoreList.slice(-1)[0]?.unhealthyCount ?? 0,
          total: data?.total ?? 0,
          trend: scoreList.map((item: any) => {
            return {
              key: dayjs(item.timestamp).format('MM-DD HH:mm'),
              value: item.unhealthyCount,
            }
          })
        });
      }
    }
  }

  private async durationChange () {
    this.alarmLoading = true;
    Promise.allSettled(this.entityDatas.filter(item => !item.disabled).map(item => {
      item.data = {};
      item.loading = true;
      return this.getData(item.value)
    })).finally(() => {
      this.alarmLoading = false;
      this.entityDatas.forEach(item => {
        item.loading = false;
      });
      this.viewAlarmList(this.currClickLevel)
    });
    this.getAlarmTableList();
  }

  private async getAlarmTableList () {
    const { fromTime, toTime, interval } = this.getGlobalTime();
    const { error, result } = await toAsyncWait(Api.getAlarmData({ fromTime: fromTime.valueOf(), toTime: toTime.valueOf(), interval }));
    if (!error) {
      const { data = [] } = result || {};
      const target = data[0];
      if (target) {
        this.alarmData[0]['3'] = target.matterData || 0;
        this.alarmData[0]['2'] = target.minorData || 0;
        this.alarmData[0]['1'] = target.noData || 0;
      }
    }
  }
  private async viewAlarmList (level: number) {
    const { fromTime, toTime } = this.getGlobalTimeV2();
    this.tableLoading = true;

    this.currClickLevel = Number(level);
    const { error, result } = await toAsyncWait(AlarmApi.getAlarmListNew({
      fromTime, toTime,
      offset: 0,
      size: 4,
      needCount: false,
      sortField: 'timestamp', sortOrder: 'desc',
      level: [level],
    }));
    this.tableLoading = false;

    if (!error) {
      const { data = {} } = result || {};
      this.alarmList = Array.isArray(data.list) ? data.list : [];
    }
  }


  // 跳转至告警详情
  private viewEventHandle (row: any, type?: string) {
    const query: any = { aid: row.id }
    if (type) {
      query.type = type
    }
    this.$router.push({
      path: '/alarmCenter/alarmDetail',
      query
    })
  }

  private viewMoreAlarmList () {
    this.$router.push({
      path: '/alarmCenter/alarm',
      query: {
        level: this.currClickLevel as any,
      }
    })
  }

}
</script>

<style lang="scss" scoped>
.cont{
  height: 100%;
}
.section-title {
  font-weight: 500;
  font-size: 14px;
  line-height: 1;
  margin-bottom: 16px;
}
.alarm-num-item {
  display: inline-block;
  min-width: 70px;
  height: 36px;
  line-height: 36px;
  padding: 2px 8px;
  border-radius: 4px;
  &.active {
    background: rgba(225, 40, 40, 0.12);
  }
  
}
.alarm-item {
  position: relative;
  font-size: 13px;
  line-height: 18px;
  padding: 12px 0 12px 8px;
  
  .alarm-status {
    width: 2px;
    height: 36px;
    border-radius: 5px;
    position: absolute;
    top: 12px;
    left: 0;
    background-color: #B5B7BB;
    &[data-status="3"] {
      background-color: #E12828;
    }
    &[data-status="2"] {
      background-color: #F79532;
    }
  }
  .time {
    transform-origin: center left;
    color: #777A7E;
  }
  .name {
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    cursor: pointer;
    &:hover {
      color: var(--color-text-link);
    }
  }
}
.mt-40 {
  margin-top: 40px;
}
</style>