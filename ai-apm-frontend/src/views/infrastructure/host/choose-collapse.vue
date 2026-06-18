<template>
  <div :class="['choose-collapse flex-v', collapsed ? 'is-collapsed' : '']"
    v-loading="filterLoading">
    <div class="choose-collapse-header">
      <div class="choose-collapse-title font-13 flex-h">{{ $t('modules.views.alarmCenter.alarm.s_c0fd0276') }}</div>
      <span
        @click="toggleCollapsed"
        :class="collapsed ? 'db-icon-unfold' : 'db-icon-fold'"
        class="cp font-12 choose-collapse-btn flex-h-cc"></span>
    </div>

    <div class="choose-collapse-body">
      <simplebar style="height: 100%;padding-right: 12px;">
        <el-collapse v-model="activeNames" class="filter-collapse">
          <el-collapse-item
            v-for="item in filterList"
            :key="item.name"
            :title="item.title"
            :name="item.name">
            <simplebar v-if="item.name !== 'ip' && item.children.length" style="max-height: 200px;">
              <el-radio-group v-model="item.model" @change="changeHandle">
                <el-radio
                  v-for="t in item.children"
                  :key="t.value"
                  :label="t.value"
                  class="filter-radio">
                  <template v-if="item.name !== 'statusType' || t.label === '全部'">{{ t.labelKey ? $t(t.labelKey) : t.label }}</template>
                  <template v-else>{{ (t.label === '1' ? 0 : 1) | HealthStatusFilter }}</template>
                </el-radio>
              </el-radio-group>
            </simplebar>
            <div v-else-if="item.name !== 'ip'" class="describe" style="margin-left:30px">{{ $t('modules.views.alarmCenter.alarm.s_d81bb206') }}</div>

            <el-input v-else
              v-model="item.model"
              @change="changeHandle"
              clearable size="small"
              maxlength="100"
              :placeholder="$t('modules.views.infrastructure.host.s_7dfc3e9b')"
              class="filter-input" />
          </el-collapse-item>
        </el-collapse>
      </simplebar>

      <div v-if="!filterLoading && !filterList.length" class="empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import i18n from '@/i18n';
import Simplebar from 'simplebar-vue'
import { toAsyncWait } from '@/utils/common';
import InfraApi from '@/api/infrastructure';

interface FilterType {
  label: string;      // 类型中文名
  value: string;      // 查询时用的参数名
  field?: string;      // 获取的筛选数据对应的字段，默认同value
}

@Component({
  components: {
    Simplebar: Simplebar as any,
  },
})
export default class ChooseCollapse extends Vue {
  @Prop({ default: () => ({}) }) private timeParams!: any;

  private collapsed = false;

  private activeNames: string[] = ['statusType', 'ip', 'os', 'tag', 'app']; // 默认展开

  private filterTypes: FilterType[] = [
    { label: i18n.t('modules.views.infrastructure.host.s_1635bb9b') as string, labelKey: 'modules.views.infrastructure.host.s_1635bb9b', value: 'statusType' },
    { label: i18n.t('modules.views.configManage.entity.s_2dc9105c') as string, labelKey: 'modules.views.configManage.entity.s_2dc9105c', value: 'ip' },
    { label: i18n.t('modules.views.infrastructure.host.s_30d23ef4') as string, labelKey: 'modules.views.appMonitor.traceDetail.s_30d23ef4', value: 'os' },
    { label: i18n.t('modules.views.configManage.entity.s_5e17f4d3') as string, labelKey: 'modules.views.configManage.entity.s_5e17f4d3', value: 'tag' },
    { label: i18n.t('modules.views.alarmCenter.alarm.s_5b0520a9') as string, labelKey: 'modules.views.alarmCenter.alarm.s_5b0520a9', value: 'app' },
  ];

  private filterList: any[] = []
  private filterLoading = true

  private osList: string[] = []
  private tagList: string[] = []
  private appList: string[] = []

  private filterToggleTimer: any = null

  private beforeDestroy() {
    if (this.filterToggleTimer) {
      window.clearTimeout(this.filterToggleTimer);
      this.filterToggleTimer = null;
    }
  }

  public async init () {
    this.filterList = []
    await this.getParams()

    // ⬇⬇⬇根据路由参数还原筛选条件⬇⬇⬇
    const { multisearch } = this.$route.query
    if (multisearch) {
      try {
        const _multisearch = decodeURIComponent(multisearch as string);
        _multisearch.split(';').forEach((item) => {
          const [key, ...remain] = item.split('=');
          const value = remain.join('=');
          const filterItem = this.filterList.find(t => t.name === key)
          if (filterItem) {
            const valueItem = filterItem.children.find((t: any) => t.value === value)
            filterItem.model = filterItem.name === 'ip' || valueItem ? value : ''
          }
        });
      } catch (err) {
        console.log(err);
      }
    }
    // ⬆⬆⬆根据路由参数还原筛选条件⬆⬆⬆

    const _params: any = {}
    this.filterList.filter(t => t.model.length).forEach(t => {
      const { name, model } = t
      _params[name] = model
    })
    return { ..._params }
  }

  private async getParams () {
    const params: any = {
      startTime: this.timeParams.fromTime,
      endTime: this.timeParams.toTime,
    }
    this.filterLoading = true
    return Promise.allSettled([
      this.getSystemList(params),
      this.getTagList(params),
      this.getAppList(params)
    ]).then((rstList) => {
      const resultList = rstList.map((t: any) => t.value || [])
      const data: any = {
        os: resultList[0],
        tag: resultList[1],
        app: resultList[2],
      }
      const filterList: any[] = [];
      this.filterTypes.forEach((t) => {
        const field = t.field || t.value
        // 已选项回填
        const filterItem: any = this.filterList.find((item) => item.name === t.value) || {}
        const model: string = filterItem.model || ''
        let values: string[] = (data[field] || []).filter((v: string) => !!v).sort()
        if (field === 'statusType') {
          values = ['1', '0']
        }
        filterList.push({
          name: t.value,
          title: t.label,
          field,
          model: t.value === 'ip' || values.includes(model) ? model : '',
          children: [
            { label: i18n.t('modules.views.alarmCenter.rootCause.s_a8b0c204') as string, labelKey: 'modules.views.alarmCenter.rootCause.s_a8b0c204', value: '' },
            ...values.map((v: any) => ({ label: v, value: v })),
          ],
        })
      });
      this.filterList = filterList
    }).finally(() => {
      this.filterLoading = false
    })
  }

  private changeHandle () {
    // 设置路由中的参数
    // 格式： multisearch=encodeURIComponent(aaa=xx;bbb=xxx;ccc=xxx)
    // 单选
    const _params: any = {}
    this.filterList.filter(t => !!t.model).forEach(t => {
      const { name, model } = t
      _params[name] = model
    })
    const _multisearch = Object.entries(_params).map(([key, value]: any) => `${key}=${value}`).join(';')
    const __multisearch = decodeURIComponent(this.$route.query.multisearch as string || '')
    if (_multisearch !== __multisearch) {
      this.$router.replace({
        query: {
          ...this.$route.query,
          multisearch: encodeURIComponent(_multisearch)
        }
      })
    }
    this.$emit('on-filter-change', { ..._params })
  }

  private toggleCollapsed () {
    this.collapsed = !this.collapsed;
    this.filterToggleTimer = setTimeout(() => {
      this.$emit('on-filter-toggle', this.collapsed)
    }, 300);
  }

  // 获取主机操作系统
  private async getSystemList (params: any) {
    const { result, error } = await toAsyncWait(InfraApi.findHostOs(params))
    return result?.data || []
  }
  // 获取主机标签
  private async getTagList (params: any) {
    const { result, error } = await toAsyncWait(InfraApi.getHostTag(params))
    return Object.keys(result?.data || {})
  }
  // 获取主机应用
  private async getAppList (params: any) {
    const { result, error } = await toAsyncWait(InfraApi.findHostApps(params))
    return (result?.data || []).filter((a: string) => a && a !== 'kubelet')
  }
}
</script>

<style lang="scss" scoped>
.choose-collapse {
  width: 188px;
  height: 100%;
  padding: 16px 20px 0 0;
  transition: margin .3s ease;
  color: var(--color-text-primary);

  &.is-collapsed {
    margin-left: -188px;
    .choose-collapse-header .choose-collapse-btn {
      transform: translateX(17px);
      box-shadow: 0px 1px 4px 0px rgba(139, 142, 147, 0.3);
    }
  }

  .choose-collapse-header {
    height: 36px;
    position: relative;
    .choose-collapse-title {
      height: 100%;
      border-top: 1px solid var(--border-color-base);
      border-bottom: 1px solid var(--border-color-base);
    }
    .choose-collapse-btn {
      width: 17px;
      height: 24px;
      background: var(--bg-color);
      border-radius: 0 4px 4px 0;
      box-shadow: 0px 1px 4px 0px rgba(139, 142, 147, 0);
      transition: transform .3s ease, box-shadow .3s ease;
      position: absolute;
      top: 6px;
      right: 0;
    }
  }

  .choose-collapse-body {
    flex: 1;
    overflow: hidden;
    position: relative;

    .empty {
      padding-right: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      position: absolute;
      top: 0;
      left: 0;
    }
  }

  .filter-collapse {
    border: none;

    :deep(.el-collapse-item) {
      padding: 2px 0;
      &:not(:last-child) {
        border-bottom: 1px solid var(--border-color-base);
      }
    }
    :deep(.el-collapse-item__header) {
      display: block;
      padding-left: 20px;
      padding-right: 8px;
      height: 28px;
      line-height: 28px;
      background-color: transparent;
      border: none;
      border-radius: 3px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      position: relative;
      font-size: 12px;
      font-weight: normal;
      .el-collapse-item__arrow {
        position: absolute;
        top: 7px;
        left: 0;
      }
      &:hover {
        background-color: var(--bg-color03);
      }
    }
    :deep(.el-collapse-item__wrap) {
      background-color: transparent;
      border: none;
      .el-collapse-item__content {
        padding-bottom: 0;
      }
    }
    :deep(.el-checkbox-group),
    :deep(.el-radio-group) {
      width: 100%;
    }

    .filter-input {
      :deep(.el-input__inner) {
        border: none;
      }
    }

    .filter-checkbox,
    .filter-radio {
      margin: 2px 0;
      padding: 3px 0;
      min-height: 24px;
      border-radius: 3px;
      display: flex;
      align-items: center;
      line-height: 20px;
      color: var(--color-text-regular);
      font-weight: normal;
      &:hover {
        background-color: var(--bg-color03);
      }
      :deep(.el-checkbox__inner),
      :deep(.el-radio__inner) {
        display: block;
      }
      :deep(.el-checkbox__label),
      :deep(.el-radio__label) {
        font-size: 12px;
      }
      :deep(.el-checkbox__input.is-checked+.el-checkbox__label),
      :deep(.el-radio__input.is-checked+.el-radio__label) {
        color: inherit;
      }
    }
  }
}
</style>
