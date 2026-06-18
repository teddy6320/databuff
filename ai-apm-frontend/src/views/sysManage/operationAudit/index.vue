<template>
  <div class="operation-audit-wrapper" v-loading="isLoading">
    <search-group
      ref="searchGroup"
      @on-change="searchChangeHandle"
      class="search-group"
    />

    <div class="mt-12">
      <el-radio-group v-model="timeRangeType" @change="getAllAuditList" size="small">
        <el-radio-button label="all">{{ $t('modules.views.alarmCenter.rootCause.s_a8b0c204') }}</el-radio-button>
        <el-radio-button label="today">{{ $t('modules.views.sysManage.operationAudit.s_800dfdd9') }}</el-radio-button>
        <el-radio-button label="yesterday">{{ $t('modules.views.sysManage.operationAudit.s_2f8d6f15') }}</el-radio-button>
        <el-radio-button label="thisWeek">{{ $t('modules.views.dataReport.report.s_72520580') }}</el-radio-button>
        <el-radio-button label="lastWeek">{{ $t('modules.views.sysManage.operationAudit.s_79abd4ee') }}</el-radio-button>
        <el-radio-button label="thisMonth">{{ $t('modules.views.dataReport.report.s_0ec94a37') }}</el-radio-button>
        <el-radio-button label="lastMonth">{{ $t('modules.views.sysManage.operationAudit.s_c5d35890') }}</el-radio-button>
      </el-radio-group>
    </div>

    <div ref="auditList" class="audit-list mt-16">
      <div v-for="item in auditList" :key="item.date" class="audit-item">
        <div @click="toggleCollapseHandle(item)" class="audit-item-header-time lh-22">
          <i :class="{ collapse: item.collapse }" class="db-icon-down font-12 audit-item-header-icon"></i>
          <span class="font-14 fw-500">{{ item.date | dateFilter }}</span>
        </div>

        <div v-if="!item.collapse" class="audit-item-body">
          <el-timeline>
            <el-timeline-item v-for="t in item.list" :key="t.id" hide-timestamp type="primary">
              <div class="describe mb-8">
                <i class="db-icon-time font-12 mr-4"></i>{{ t.time | TimesToDateFilter('YYYY-MM-DD HH:mm') }}
              </div>
              <div>
                <span class="default-text fw-500 mr-8">{{ t.actor }}</span><span class="mr-8 describe">{{ t.action }}</span>
                <span class="default-text fw-500 mr-8">{{ t.entityType }}</span>
                <span @click="t.hasLink ? entityClickHandle(t) : null"
                  :class="t.hasLink ? 'blue cp' : 'default-text'"
                  class="fw-500">{{ `${t.entityId ? `${t.entityId}: ` : ''}` }}{{ t.entityName }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>

      <div v-show="!isLoading && !auditList.length" class="empty-show">{{ $t('modules.components.charts.s_21efd88b') }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import { orderBy } from 'lodash';
import SearchGroup from './search-group.vue';
import { debounce } from '@/utils/common';
import { TimesToDateFilter } from '@/utils/filters/times';
import { toAsyncWait } from '@/utils/common';
import SystemApi from '@/api/system';

// 支持下钻的实体类型
const DrillDownMapping = {
  account: [i18n.t('modules.views.sysManage.operationAudit.s_1fd02a90') as string],
  role: [i18n.t('modules.views.sysManage.operationAudit.s_464f3d4e') as string],
  group: [i18n.t('modules.views.sysManage.group.s_eab5b13a') as string],
  org: [i18n.t('modules.views.sysManage.operationAudit.s_74fe5f9e') as string],
  license: [i18n.t('modules.views.sysManage.operationAudit.s_98a315c0') as string],
  notice: [i18n.t('modules.views.sysManage.operationAudit.s_44b97674') as string, i18n.t('modules.views.sysManage.operationAudit.s_2e23452d') as string, i18n.t('modules.views.sysManage.operationAudit.s_c2434cb3') as string, i18n.t('modules.views.sysManage.operationAudit.s_f38b915a') as string, i18n.t('modules.views.sysManage.operationAudit.s_5a015278') as string],
  basicSetting: [i18n.t('modules.views.sysManage.operationAudit.s_14097695') as string],
  systemRule: [i18n.t('modules.views.sysManage.operationAudit.s_6a94bf37') as string],
  rule: [i18n.t('modules.views.configInstall.plugin.s_b4c5a9d9') as string],
  response: [i18n.t('modules.views.sysManage.operationAudit.s_8b9699bf') as string],
  serviceEntity: [i18n.t('modules.views.sysManage.operationAudit.s_f1f89bda') as string, i18n.t('modules.views.configManage.entity.s_8e8aaafe') as string, i18n.t('modules.views.configManage.entity.s_d4f806d2') as string],
  businessEntity: [i18n.t('modules.views.sysManage.operationAudit.s_d2f31217') as string],
  processEntity: [i18n.t('modules.views.sysManage.operationAudit.s_de804110') as string, i18n.t('modules.views.configManage.entity.s_52ede196') as string, i18n.t('modules.views.configManage.entity.s_01e9810a') as string],
  requestEntity: [i18n.t('modules.views.sysManage.operationAudit.s_6f8af1dc') as string],
  relationSetting: [i18n.t('modules.views.sysManage.operationAudit.s_d3d76932') as string],
  alarm: [i18n.t('modules.views.cockpit.tab.s_6d105e23') as string],
  ai: [i18n.t('modules.views.sysManage.operationAudit.s_a4de1114') as string],
  metric: [i18n.t('modules.views.sysManage.operationAudit.s_4e63e8d5') as string, i18n.t('modules.views.sysManage.operationAudit.s_5aab3912') as string],
  plugin: [i18n.t('modules.views.configInstall.s_236b0cdd') as string],
  service: [i18n.t('modules.views.sysManage.operationAudit.s_3689d50f') as string],
  // '系统',
  // 'default',
};

@Component({
  components: {
    SearchGroup,
  },
  filters: {
    dateFilter (val: number) {
      const date = TimesToDateFilter(val, 'YYYY-MM-DD');
      const today = dayjs(new Date()).format('YYYY-MM-DD');
      const yesterday = dayjs(+new Date() - 24 * 60 * 60 * 1000).format('YYYY-MM-DD');
      if (date === today) {
        return i18n.t('modules.views.sysManage.operationAudit.s_800dfdd9') as string
      } else if (date === yesterday) {
        return i18n.t('modules.views.sysManage.operationAudit.s_2f8d6f15') as string
      }
      return date;
    },
  },
})
export default class OperationAudit extends Vue {
  public $refs!: {
    searchGroup: SearchGroup
    auditList: HTMLDivElement
  }

  get serviceNameIdMapping () { // 列表服务名称映射
    const mapping: any = {}
    const basicServiceMap = this.$store.getters['Service/basicServiceMap']
    Object.values(basicServiceMap).forEach((t: any) => {
      mapping[t.name] = t.id
      mapping[t.service] = t.id
      mapping[t.id] = t.name || t.service
    });
    return mapping
  }

  get entityTypeMapping () {
    const mapping: any = {}
    Object.entries(DrillDownMapping).forEach(([key, types]) => {
      types.forEach(t => {
        mapping[t] = key
      })
    })
    return mapping
  }

  private timeRangeType: string = 'today';

  private queryParams: any = {}

  private isLoading: boolean = true;
  private auditList: any[] = []; // 已显示的数据
  private allAuditList: any[] = []; // 所有数据

  // 分页滚动加载
  private pageNum = 1;
  private pageSize = 1;
  private timer: any = null;
  private scrollContainer: any = null;
  private scrollHandle: any = null;
  get noMore () {
    return this.auditList.length >= this.allAuditList.length
  }

  private mounted () {
    this.scrollLoop();
    this.$refs.searchGroup.init().then((data: any) => {
      this.queryParams = { ...data }
      this.getAllAuditList();
    });
  }

  private beforeDestroy () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    if (this.scrollContainer) {
      this.scrollContainer.removeEventListener('scroll', this.scrollHandle);
    }
  }

  // 搜索
  private searchChangeHandle (data: any) {
    this.queryParams = { ...data }
    this.getAllAuditList();
  }

  // 展开/收起
  private toggleCollapseHandle (item: any) {
    this.$set(item, 'collapse', !item.collapse);
    this.$nextTick(() => {
      this.loadMoreList();
    });
  }

  // 下钻
  private entityClickHandle (item: any) {
    const { entityType, entityName, entityId } = item
    const urlType = this.entityTypeMapping[entityType]
    const enName = encodeURIComponent(entityName)
    const enId = encodeURIComponent(entityId)
    if (urlType === 'role') {
      this.$router.push({ path: '/sysManage/role', query: { name: enName } })
    } else if (urlType === 'org') {
      this.$router.push({ path: '/sysManage/org', query: { name: enName } })
    } else if (urlType === 'license') {
      this.$router.push({ path: '/sysManage/license', })
    } else if (urlType === 'notice') {
      this.$router.push({ path: '/sysManage/notice', query: { type: enId } })
    } else if (urlType === 'basicSetting') {
      this.$router.push({ path: '/sysManage/basic', })
    } else if (urlType === 'systemRule') {
      this.$router.push({ path: '/sysManage/systemRule', query: { ruleName: enName } })
    } else if (urlType === 'rule') {
      this.$router.push({ path: '/config/rule', query: { ruleName: enName } })
    } else if (urlType === 'response') {
      this.$router.push({ path: '/configManage/alarm/responseSetting', query: { name: enName } })
    } else if (urlType === 'serviceEntity') {
      const _query: any = { ct: 'global' }
      if (entityType === '应用配置') {
        _query.type = 'app'
        _query.service = enId
      }
      this.$router.push({ path: '/config/service', query: _query})
    } else if (urlType === 'processEntity') {
      this.$router.push({ path: '/config/process', query: {
        ct: entityType !== '进程识别' ? 'acquisition' : 'recognition',
        desc: enName,
      }})
    } else if (urlType === 'relationSetting') {
      this.$router.push({ path: '/config/relationest', })
    } else if (urlType === 'alarm') {
      this.$router.push({ path: '/alarmCenter/alarmDetail', query: { aid: enId } })
    } else if (urlType === 'ai') {
      this.$router.push({ path: '/config/ai' })
    } else if (urlType === 'service') {
      this.$router.push({ path: '/appMonitor/serviceDetail', query: {
        sid: enId,
        sn: entityName,
        activeName: 'tab-baseinfo',
      }})
    }
  }

  // 获取列表
  private async getList (page = 1) {
    this.pageNum = page
    if (page === 1 && this.scrollContainer) {
      // 滚动区域 scrollTop 置为 0
      this.scrollContainer.scrollTop = 0
    }
    this.auditList = [
      ...(page === 1 ? [] : this.auditList),
      ...this.allAuditList.slice((page - 1) * this.pageSize, page * this.pageSize),
    ];
    this.$nextTick(() => {
      this.loadMoreList();
    });
  }

  // 判断加载
  private loadMoreList () {
    if (!this.scrollContainer && !this.$refs.auditList) {
      return
    } else if (!this.scrollContainer && this.$refs.auditList) {
      this.scrollContainer = this.$refs.auditList
    }
    const { scrollHeight, clientHeight, scrollTop } = this.scrollContainer;
    if (!this.noMore && !this.isLoading && scrollHeight - clientHeight - scrollTop < 150) {
      this.getList(this.pageNum + 1)
    }
  }

  // 监听滚动
  private scrollLoop () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
    this.timer = setTimeout(() => {
      const scrollContainer = this.$refs.auditList;
      if (scrollContainer) {
        this.scrollContainer = scrollContainer;
        // 滚动到底加载更多
        this.scrollHandle = debounce(() => {
          this.loadMoreList();
        }, 17)
        scrollContainer.addEventListener('scroll', this.scrollHandle)
      }
    }, 100)
  }

  // 获取所有数据
  private async getAllAuditList () {
    const params: any = {
      ...this.getTimeRange(this.timeRangeType),
      ...this.queryParams,
    }
    Object.entries(params).forEach(([key, value]) => {
      if (value === '') {
        delete params[key]
      }
    })
    this.isLoading = true;
    const { result, error } = await toAsyncWait(SystemApi.getOperateAuditList(params));
    this.isLoading = false;
    if (!error) {
      const data = result?.data || [];
      const dateMapping: any = {}
      data.forEach((item: any) => {
        const date = +new Date(dayjs(item.timestamp).format('YYYY-MM-DD'));
        if (!dateMapping[date]) {
          dateMapping[date] = []
        }
        let hasLink = !!this.entityTypeMapping[item.entityType] && !!item.hasLink;
        if (hasLink && item.entityType === i18n.t('modules.views.sysManage.operationAudit.s_22bb82f6') as string && !this.serviceNameIdMapping[item.entityName]) {
          hasLink = false;
        }
        if (item.entityType === i18n.t('modules.views.sysManage.operationAudit.s_4e63e8d5') as string) {
          if ([i18n.t('modules.views.sysManage.operationAudit.s_b58c7549') as string, i18n.t('modules.views.sysManage.operationAudit.s_32ac152b') as string].includes(item.action)) {
            item.entityId = '';
          } else {
            item.entityName = item.entityId
            item.entityId = '';
          }
        } else if (item.entityType === i18n.t('modules.views.sysManage.operationAudit.s_5aab3912') as string) {
          item.entityName = item.entityId
          item.entityId = '';
        } else if (item.entityType === i18n.t('modules.views.sysManage.operationAudit.s_3689d50f') as string && item.action === i18n.t('modules.views.sysManage.operationAudit.s_5c98aece') as string) {
          item.entityId = (item.entityId || '').replace(/\[|\]/g, '');
          item.entityName = this.serviceNameIdMapping[item.entityId] || '';
        }
        dateMapping[date].push({
          id: item.identifier, // 标识
          time: +new Date(item.timestamp), // 时间戳
          actor: item.actor, // 操作人
          action: item.action, // 操作动作
          entityType: item.entityType, // 实体类型
          entityName: item.entityName, // 实体名称
          entityId: item.entityId, // 实体ID
          hasLink, // 是否可下钻
          // url: item.url, // 接口URL
          // origin: item.origin, // 来源
          // beforeValue: item.beforeValue, // 操作前值
          // afterValue: item.afterValue, // 操作后值
          // outcome: item.outcome, // 操作结果
          // errorMessage: item.errorMessage, // 错误信息
        });
      })
      const allAuditList: any[] = []
      const dates = orderBy(Object.keys(dateMapping), [t => +t], ['desc'])
      dates.forEach(key => {
        allAuditList.push({
          date: key,
          list: orderBy(dateMapping[key], ['time'], ['desc'])
        })
      })
      this.allAuditList = allAuditList;
    } else {
      this.allAuditList = [];
    }
    this.getList();
  }

  // 获取时间范围
  private getTimeRange (type: string) {
    const now = new Date(); // 当前时间
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()); // 今天0点
    const weekDay = today.getDay() || 7; // 周日返回 0，修正为 7

    let fromTime = +today; // 今天0点
    let toTime = +now; // 当前时间

    if (type === 'yesterday') {
      fromTime = +new Date(+today - 24 * 60 * 60 * 1000); // 昨天0点
      toTime = +today; // 今天0点
    } else if (type === 'thisWeek') {
      fromTime = +new Date(+today - (weekDay - 1) * 24 * 60 * 60 * 1000); // 本周一0点
    } else if (type === 'lastWeek') {
      fromTime = +new Date(+today - (weekDay + 6) * 24 * 60 * 60 * 1000); // 上周一0点
      toTime = +new Date(+today - (weekDay - 1) * 24 * 60 * 60 * 1000); // 本周一0点
    } else if (type === 'thisMonth') {
      fromTime = +new Date(now.getFullYear(), now.getMonth(), 1); // 本月1号
    } else if (type === 'lastMonth') {
      fromTime = +new Date(now.getFullYear(), now.getMonth() - 1, 1); // 上月1号
      toTime = +new Date(now.getFullYear(), now.getMonth(), 1); // 本月1号
    } else if (type === 'all') {
      fromTime = +new Date(+today - 89 * 24 * 60 * 60 * 1000); // 90天
    }
    return {
      fromTime: dayjs(fromTime).format('YYYY-MM-DD HH:mm:ss'),
      toTime: dayjs(toTime).format('YYYY-MM-DD HH:mm:ss'),
    };
  }
}
</script>

<style lang="scss" scoped>
.operation-audit-wrapper {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-primary);
  overflow: hidden;

  .audit-list {
    flex: 1;
    overflow: auto;
  }

  .audit-item + .audit-item {
    margin-top: 16px;
  }

  .audit-item-header-time {
    display: flex;
    padding: 0 10px 0 20px;
    box-sizing: border-box;
    position: relative;
    cursor: pointer;
  }
  .audit-item-header-icon {
    margin-top: -6px;
    position: absolute;
    left: 2px;
    top: 50%;
    transition: transform .3s ease;
    &.collapse {
      transform: rotate(-90deg);
    }
  }
  .audit-item-body {
    margin-top: 10px;
    padding: 16px 14px;
    border-radius: 4px;
    background: var(--bg-color02);
  }

  .empty-show {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    font-size: 14px;
    color: var(--color-text-secondary);
  }

  :deep(.el-timeline-item:last-child) {
    padding-bottom: 0;
  }
}
</style>
