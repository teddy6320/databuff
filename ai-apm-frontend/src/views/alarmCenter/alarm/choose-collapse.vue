<template>
  <div :class="['choose-collapse flex-v', collapsed ? 'is-collapsed' : '']">
    <div class="choose-collapse-header">
      <div class="choose-collapse-title font-13 flex-h">{{ $t('modules.views.alarmCenter.alarm.s_c0fd0276') }}</div>
      <span
        @click="toggleCollapsed"
        :class="collapsed ? 'db-icon-unfold' : 'db-icon-fold'"
        class="cp font-12 choose-collapse-btn flex-h-cc"></span>
    </div>

    <div class="choose-collapse-body" v-loading="filterLoading">
      <simplebar style="height: 100%;padding-right: 12px;">
        <el-collapse v-model="activeNames" class="filter-collapse">
          <el-collapse-item
            v-for="item in filterList"
            :key="item.name"
            :name="item.name">
            <template slot="title">
              <span>{{ item.titleKey ? $t(item.titleKey) : item.title }}</span>
            </template>
            <simplebar v-if="item.children.length" style="max-height: 200px;">
              <el-radio-group
                v-if="!item.multiple"
                v-model="item.model"
                @change="changeHandle">
                <el-radio
                  v-for="t in item.children"
                  :key="t.value"
                  :label="t.value"
                  :disabled="t.disabled"
                  class="filter-radio">{{ t.labelKey ? $t(t.labelKey) : t.label }}</el-radio>
              </el-radio-group>
              <el-checkbox-group
                v-else
                v-model="item.model"
                @change="changeHandle">
                <el-checkbox
                  v-for="t in item.children"
                  :key="t.value"
                  :label="t.value"
                  :disabled="t.disabled"
                  class="filter-checkbox">{{ t.labelKey ? $t(t.labelKey) : t.label }}</el-checkbox>
              </el-checkbox-group>
            </simplebar>
            <div v-else class="describe" style="margin-left:30px">{{ $t('modules.views.alarmCenter.alarm.s_d81bb206') }}</div>
          </el-collapse-item>
        </el-collapse>
      </simplebar>

      <div v-if="!filterList.length" class="empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import Simplebar from 'simplebar-vue'
import { orderBy } from 'lodash';

import { toAsyncWait } from '@/utils/common';
import AlarmApi from '@/api/alarm';
import MetricApi from '@/api/metric';
import { StringIsEmpty } from '@/utils/common';

// 过滤告警/事件的标签key
const filterTagKeys = (keys: string[]) => {
  // 忽略列表
  const ignoreList = ['apiKey', 'level', 'status', 'ruleName', 'service', 'serviceId', 'message', 'group', 'classification', 'serviceCode', 'source'];
  // 过滤掉以id结尾和忽略列表的key
  const _keys = keys.filter(str => !/id$/i.test(str) && !ignoreList.includes(str));
  return _keys.sort();
}

const toServiceFilterOption = (value: any, nameMapping: Record<string, string>) => {
  if (value && typeof value === 'object' && value.serviceId) {
    return {
      label: value.serviceName || nameMapping[value.serviceId] || value.serviceId,
      value: value.serviceId,
    };
  }
  const id = String(value ?? '');
  return {
    label: nameMapping[id] || id,
    value: id,
  };
};

const buildCheckboxFilter = (
  name: string,
  title: string,
  values: any[],
  labelMapper: (value: any) => string,
) => ({
  name,
  title,
  valueType: 'string',
  multiple: true,
  model: [],
  children: orderBy(
    values.map((v: any) => ({
      label: labelMapper(v) || v,
      value: v,
    })),
    [t => String(t.label || '').toLocaleLowerCase()],
    ['asc'],
  ),
});

@Component({
  components: {
    Simplebar: Simplebar as any,
  },
})
export default class ChooseCollapse extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;

  get serviceIdNameMapping () {
    const mapping: any = {}
    const basicServiceMap = this.$store.getters['Service/basicServiceMap']
    Object.keys(basicServiceMap).forEach((t: string) => {
      mapping[t] = basicServiceMap[t].name
    });
    return mapping
  }

  get tagLabelMapping () {
    const mapping: any = {}
    const tagLabelMap = this.$store.getters['Common/tagLabelMap']
    Object.entries(tagLabelMap || {}).forEach(([key, item]: any) => {
      mapping[key] = item.name
    })
    return mapping
  }

  private collapsed = false;

  private activeNames: string[] = []; // 默认展开项

  private staticFilterList: any[] = [
    {
      name: 'level',
      title: i18n.t('modules.views.alarmCenter.alarm.s_ed7094f4') as string, titleKey: 'modules.views.alarmCenter.alarm.s_ed7094f4',
      valueType: 'number',
      multiple: true,
      model: [],
      children: [
        { label: i18n.t('modules.views.alarmCenter.alarm.s_fc7e3846') as string, labelKey: 'modules.utils.filters.s_fc7e3846', value: 3 },
        { label: i18n.t('modules.views.alarmCenter.alarm.s_bde77082') as string, labelKey: 'modules.utils.filters.s_bde77082', value: 2 },
      ],
    },
  ]

  private filterList: any[] = []
  private filterLoading = true

  private filterToggleTimer: any = null

  private beforeDestroy() {
    if (this.filterToggleTimer) {
      window.clearTimeout(this.filterToggleTimer);
      this.filterToggleTimer = null;
    }
  }

  public async init () {
    await Promise.all([
      this.$store.dispatch('Common/GET_TAG_LABEL_MAP'),
      this.$store.dispatch('Service/GET_BASIC_SERVICE'),
    ]);
    await this.getParams()

    this.activeNames = this.filterList.map(t => t.name)

    // 搜索参数回显
    const routerQuery = this.$route.query
    this.filterList.forEach(item => {
      const { name, multiple, children, valueType } = item
      if (routerQuery[name]) {
        const isNumber = valueType === 'number'
        const isArrayValue = Array.isArray(routerQuery[name])
        const values = (isArrayValue ? routerQuery[name] as string[] : [routerQuery[name] as string]).filter(v => v).map(v => {
          return isNumber ? +v : decodeURIComponent(v)
        });
        if (multiple) {
          item.model = values.filter(v => !!children.find((t: any) => t.value === v))
        } else {
          item.model = children.find((t: any) => t.value === values[0]) ? values[0] : ''
        }
      } else {
        item.model = multiple ? [] : ''
      }
    })

    const _params: any = {}
    this.filterList.forEach(t => {
      const { name, multiple, model } = t
      if ((!multiple && model) || (multiple && model.length)) {
        _params[name] = multiple ? [...model] : model
      }
    })
    return { ..._params }
  }

  private async getParams () {
    const params: any = {
      fromTime: this.queryParams.fromTime,
      toTime: this.queryParams.toTime,
    }
    this.filterLoading = true
    const { result, error } = await toAsyncWait(AlarmApi.getAlarmParams(params))
    this.filterLoading = false
    if (!error) {
      const resultData = result.data || {}

      // 下钻进来的服务id合并到数据里，保证一定能在筛选里
      if (this.$route.query.serviceId) {
        const _serviceId = this.$route.query.serviceId
        const _sids = (Array.isArray(_serviceId) ? _serviceId : [_serviceId]).map(String);
        const byId = new Map<string, any>();
        (resultData.service || []).forEach((v: any) => {
          const option = toServiceFilterOption(v, this.serviceIdNameMapping);
          if (option.value) {
            byId.set(option.value, v && typeof v === 'object' ? v : {
              serviceId: option.value,
              serviceName: option.label,
            });
          }
        });
        _sids.forEach((id: string) => {
          if (!byId.has(id)) {
            byId.set(id, {
              serviceId: id,
              serviceName: this.serviceIdNameMapping[id] || id,
            });
          }
        });
        resultData.service = Array.from(byId.values());
      }

      const dedicatedFilters: any[] = [];
      const ruleNames = (resultData.ruleName || []).filter((v: any) => !StringIsEmpty(v));
      if (ruleNames.length) {
        dedicatedFilters.push({
          ...buildCheckboxFilter('ruleName', i18n.t('modules.views.alarmCenter.alarm.s_b4c5a9d9') as string, ruleNames, v => v),
          titleKey: 'modules.views.alarmCenter.alarm.s_b4c5a9d9',
        });
      }
      const serviceOptions = (resultData.service || []).filter((v: any) => {
        const id = v && typeof v === 'object' ? v.serviceId : v;
        return !StringIsEmpty(id);
      });
      if (serviceOptions.length) {
        dedicatedFilters.push({
          name: 'serviceId',
          title: i18n.t('modules.views.alarmCenter.alarm.s_8f3747c0') as string,
          titleKey: 'modules.views.alarmCenter.alarm.s_8f3747c0',
          valueType: 'string',
          multiple: true,
          model: [],
          children: orderBy(
            serviceOptions.map((v: any) => toServiceFilterOption(v, this.serviceIdNameMapping)),
            [t => String(t.label || '').toLocaleLowerCase()],
            ['asc'],
          ),
        });
      }

      const filterList: any[] = [];
      filterTagKeys(Object.keys(resultData)).forEach((key) => {
        const isService = key === 'service'
        key = isService ? 'serviceId' : key
        const values = (resultData[key] || resultData[isService ? 'service' : key] || [])
          .filter((v: any) => !StringIsEmpty(v));
        if (values.length) {
          const children = values.map((v: any) => ({
            label: isService ? this.serviceIdNameMapping[v] || v : v,
            value: v,
          }));
          filterList.push({
            name: key,
            title: this.tagLabelMapping[isService ? 'service' : key] || key,
            valueType: 'string',
            multiple: true,
            model: [],
            children: orderBy(children, [t => String(t.label || '').toLocaleLowerCase()], ['asc']),
          });
        }
      });
      this.filterList = [
        ...this.staticFilterList,
        ...dedicatedFilters,
        ...filterList,
      ]
    }
  }

  private changeHandle () {
    const _params: any = {}
    this.filterList.forEach(t => {
      const { name, multiple, model } = t
      if ((!multiple && model) || (multiple && model.length)) {
        _params[name] = multiple ? [...model] : model
      }
    })
    this.$emit('on-filter-change', { ..._params })

    // 更新路由中的参数
    const _query = { ...this.$route.query }
    this.filterList.forEach(t => {
      const { name, multiple } = t
      if (!StringIsEmpty(_params[name])) {
        _query[name] = multiple ? [..._params[t.name]].map(v => encodeURIComponent(v)) : encodeURIComponent(_params[t.name])
      } else {
        delete _query[name]
      }
    })
    if (JSON.stringify(_query) !== JSON.stringify(this.$route.query)) {
      this.$router.replace({ query: { ..._query } })
    }
  }

  private toggleCollapsed () {
    this.collapsed = !this.collapsed;
    this.filterToggleTimer = setTimeout(() => {
      this.$emit('on-filter-toggle', this.collapsed)
    }, 300);
  }
}
</script>

<style lang="scss" scoped>
.choose-collapse {
  width: 188px;
  height: 100%;
  padding-right: 20px;
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
