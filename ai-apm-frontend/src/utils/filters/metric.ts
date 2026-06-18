import i18n from '@/i18n';
import {
  MetricTypeList,
  MetricBasicJudgmentList,
  MetricAggMethodList
} from '@/utils/static/metric'

/**
 * 指标来源
 * @param {source}
 * @return {sourceCn}
 */
export const MetricSourceFilter = (value: string) => {
  switch (value) {
    case 'SYSTEM_BUILT_IN':
      return i18n.t('modules.utils.filters.s_df9e51f0') as string;
    case 'USER_DEFINED':
      return i18n.t('modules.utils.filters.s_2dbb3e25') as string;
    case 'USER_IMPORTED':
      return i18n.t('modules.utils.filters.s_3a6a7973') as string;
    case 'THIRD_PARTY_CLOUD':
      return i18n.t('modules.utils.filters.s_ef4d1f77') as string;
    case 'THIRD_PARTY_APM':
      return i18n.t('modules.utils.filters.s_e8ef5259') as string;
    case 'THIRD_PARTY_DB':
      return i18n.t('modules.utils.filters.s_747fabc4') as string;
    case 'THIRD_PARTY_NETWORK':
      return i18n.t('modules.utils.filters.s_59805cc8') as string;
    case 'INTEGRATION_API':
      return i18n.t('modules.utils.filters.s_ee67994d') as string;
    case 'INTEGRATION_AGENT':
      return i18n.t('modules.utils.filters.s_19340398') as string;
    case 'INTEGRATION_LOG':
      return i18n.t('modules.utils.filters.s_cf367893') as string;
    case 'INTEGRATION_TRACE':
      return i18n.t('modules.utils.filters.s_331dfd45') as string;
    case 'COMMUNITY_PLUGIN':
      return i18n.t('modules.utils.filters.s_508805fd') as string;
    case 'COMMUNITY_TEMPLATE':
      return i18n.t('modules.utils.filters.s_ae7f8c60') as string;
    case 'PRESET':
      return i18n.t('modules.utils.filters.s_5c888f73') as string;
    case 'CUSTOM':
      return i18n.t('modules.utils.filters.s_f1d4ff50') as string;
    case 'THIRD_PARTY':
      return i18n.t('modules.utils.filters.s_bc7e0703') as string;
    default:
      return value || '-'
  }
}

/**
 * 指标类型
 * @param {metricType}
 * @return {metricTypeCn}
 */
export const MetricTypeFilter = (value: string) => {
  return MetricTypeList.find(t => t.value === value)?.label || value || '-'
}

/**
 * 指标基本判断
 * @param {basicJudgment}
 * @return {basicJudgmentCn}
 */
export const MetricBasicJudgmentFilter = (value: string) => {
  return MetricBasicJudgmentList.find(t => t.value === value)?.label || value || '-'
}

/**
 * 指标聚合方式
 * @param {agg}
 * @return {aggCn}
 */
export const MetricAggMethodFilter = (value: string) => {
  return MetricAggMethodList.find(t => t.value === value)?.label || value || '-'
}
