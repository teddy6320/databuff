<template>
  <div class="event-detail-baseinfo">
    <div class="info-t flex-h mb-12"><span class="label">{{ $t('modules.views.alarmCenter.alarmDetail.s_97f589d9') }}</span>{{ detail.id || '-' }}</div>
    <div class="info-t flex-h mb-6"><span class="label">{{ $t('modules.views.alarmCenter.eventDetail.s_1a42bdb8') }}</span>
      <div v-if="multiTriggerType.length" class="value">
        <span
          v-for="item in multiTriggerType"
          :key="item.type"
          class="mr-10 mb-6 dib value-t">{{ item.labelKey ? $t(item.labelKey) : item.label }}: 
          <span
            @click="viewDetailByType(item)"
            :class="{ 'blue cp': item.isLink }">{{ item.value || '-' }}</span>
        </span>
      </div>
      <span v-else class="mb-6">-</span>
    </div>
    <div class="info-t flex-h mb-6"><span class="label">{{ $t('modules.views.alarmCenter.eventDetail.s_87ae1624') }}</span>
      <div v-if="detail && (detail.metricsFormat || []).length" class="value">
        <span
          v-for="t, i in detail.metricsFormat"
          :key="i"
          class="mr-10 mb-6 dib value-t">
          <span
            v-if="!t.isCustom">{{ (metricInfoMapping[t.metric] || {}).metricCn || t.metric }}</span>
          <template v-else>{{ t.metric }}</template>
        </span>
      </div>
      <span v-else class="mb-6">-</span>
    </div>
    <div class="info-t flex-h mb-12"><span class="label">{{ $t('modules.views.alarmCenter.alarmDetail.s_858ac2d7') }}</span>{{ detail.startTriggerTime | TimesToDateFilter('YYYY-MM-DD HH:mm') }}</div>
    <div class="info-t flex-h mb-12"><span class="label">{{ $t('modules.views.configManage.alarm.s_87080256') }}</span>
      <span
        v-if="ruleName"
        @click="viewRuleHandle(ruleName)"
        class="blue cp">{{ ruleName }}</span>
      <span v-else>-</span>
    </div>
    <div class="info-t flex-h mb-12"><span class="label">{{ $t('modules.views.alarmCenter.eventDetail.s_986329a3') }}</span>{{ detail.classification | MonitorTypeFilter }}</div>
    <div v-if="getEnableStatus && !isSystemEvent" class="info-t flex-h mb-12"><span class="label">{{ $t('modules.views.alarmCenter.alarm.s_f9d4e244') }}</span>{{ detail.domainName || '-' }}</div>
    <div class="info-t tag-info-t flex-h mb-12"><span class="label">{{ $t('modules.views.infrastructure.hostDetail.s_14d34236') }}</span>
      <div v-if="formatTags.length" class="tag-list">
        <collapse-tags
          :tags="formatTags"
          :minWidth='300'
          :maxLine="2" />
      </div>
      <span v-else>-</span>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import CollapseTags from '@/components/collapse-tags/index.vue';

// 过滤告警/事件的标签key
const filterTagKeys = (keys: string[]) => {
  // 忽略列表
  const ignoreList = ['apiKey', 'level', 'ruleName', 'message', 'group', 'classification', 'serviceCode', 'source'];
  // 过滤掉忽略列表的key
  const _keys = keys.filter(str => !ignoreList.includes(str));
  return _keys.sort();
}

@Component({
  components: {
    CollapseTags,
  }
})
export default class TabBaseinfo extends Vue {
  @Prop({ default: () => ({}) }) private detail!: any;

  get getEnableStatus () {
    return this.$store.getters['User/getGroupEnabled']
  }

  get isSystemEvent () {
    return this.$route.path === '/sysManage/eventDetail'
  }

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

  get metricInfoMapping () {
    const mapping: any = {};
    (this.detail.metricsFormat || []).filter((t: any) => !t.isCustom).forEach((t: any) => {
      const info = this.$store.getters['Common/metricInfoMap'][t.metric]
      mapping[t.metric] = info
    })
    return mapping
  }

  get multiTriggerType () {
    const detail = this.detail || {}
    const keys = Object.keys(detail.trigger || {}).filter(t => !!t).sort();
    if (!keys.length) {
      return []
    }
    const trigger = { ...detail.tags || {}, ...detail.trigger || {} }
    // 格式化为 {key: value}
    Object.keys(trigger).forEach(key => {
      trigger[key] = key ? Array.isArray(trigger[key]) ? trigger[key][0] : trigger[key] : ''
    })
    const linkTypes = [ // 可下钻的类型
      'host', 'process', 'container_name',
      'service', 'serviceInstance',
      'srcService', 'srcServiceInstance'
    ]
    const serviceKeys = [
      'service', 'serviceId', 'serviceInstance',
      'srcService', 'srcServiceId', 'srcServiceInstance'
    ]
    const triggerTypes: any[] = []
    keys.filter(key => !!trigger[key]).forEach((key: string) => {
      const item: any = {
        type: key,
        label: this.tagLabelMapping[key] || key,
        value: trigger[key],
        isLink: linkTypes.includes(key),
      }
      if (serviceKeys.includes(key)) {
        const isSrc = key.indexOf('src') === 0
        const isServiceName = key === 'service' || key === 'srcService'
        const isServiceId = key === 'serviceId' || key === 'srcServiceId'
        const sid = trigger[isSrc ? 'srcServiceId' : 'serviceId']
        if (!isServiceName || !sid) {
          item.isLink = !!sid
          item.serviceId = sid
          if (isServiceId) {
            item.type = isSrc ? 'srcService' : 'service'
            item.value = this.serviceIdNameMapping[sid] || item.value
            item.label = this.serviceIdNameMapping[sid] ? this.tagLabelMapping[item.type] : key
          }
          triggerTypes.push(item)
        }
      } else if (key === 'process') {
        item.host = trigger.host || ''
        triggerTypes.push(item)
      } else {
        triggerTypes.push(item)
      }
    })
    return triggerTypes
  }

  get formatTags () {
    const tags = this.detail?.tags || {}
    const formatTags: string[] = []
    filterTagKeys(Object.keys(tags)).forEach(key => {
      const _key = this.tagLabelMapping[key] || key
      const _values: any[] = tags[key]
      formatTags.push(..._values.map(val => `${_key}: ${val}`))
    })
    return formatTags
  }

  get ruleName () {
    const { ruleName, monitorName, tags }  = this.detail || {}
    return ruleName || monitorName || (tags?.ruleName || [])[0] || (tags?.monitorName || [])[0] || ''
  }

  @Watch('detail.id', { immediate: true })
  private async onEventDetailChange () {
    if (!this.detail?.id) {
      return
    }

    // 获取指标详细信息
    const metrics = this.detail.metricsFormat.filter((t: any) => !t.isCustom).map((t: any) => t.metric)
    this.$store.dispatch('Common/GET_METRIC_INFOS', metrics);

    await this.$store.dispatch('Common/GET_TAG_LABEL_MAP');
  }

  private viewDetailByType (item: any) {
    if (!item.isLink) {
      return
    }
    const { type, value, serviceId, host } = item
    const query: any = {};
    let path = '';
    switch (type) {
      case 'host':
        query.hostName = encodeURIComponent(value)
        path = '/infrastructure/hostDetail';
        break;
      case 'process':
        query.processName = encodeURIComponent(value)
        query.hostName = encodeURIComponent(host)
        path = '/infrastructure/processDetail';
        break;
      case 'container_name':
        query.containerId = encodeURIComponent(item.containerId || '')
        path = '/infrastructure/dockerDetail';
        break;
      case 'service':
      case 'srcService':
        query.sn = encodeURIComponent(value)
        query.sid = encodeURIComponent(serviceId)
        path = '/appMonitor/serviceDetail'
        break;
      case 'serviceInstance':
      case 'srcServiceInstance':
        query.sid = encodeURIComponent(serviceId)
        query.si = encodeURIComponent(value)
        path = '/appMonitor/serviceInstance'
        break;
    }
    this.$router.push({
      path, query
    })
  }

  private viewRuleHandle (name: string) {
    const query: any = { ruleName: encodeURIComponent(name) }
    let path = '/config/rule'
    if (this.isSystemEvent) {
      path = '/sysManage/systemRule'
    }
    this.$router.push({ path, query });
  }
}
</script>

<style lang="scss" scoped>
.event-detail-baseinfo {
  padding-top: 18px !important;
  overflow: auto !important;
  .info-t {
    line-height: 18px;
    font-size: 13px;
    align-items: flex-start;
    word-break: break-all;
    .label {
      flex: none;
      padding-right: 16px;
      min-width: 52px;
      color: var(--color-text-secondary);
    }
    .value {
      flex: 1;
      overflow: hidden;
    }
    .value-t {
      max-width: 100%;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
    }
  }
  .tag-info-t {
    align-items: flex-start;
    .label {
      line-height: 22px;
    }
    .tag-list {
      flex: 1;
    }
  }
}
</style>
