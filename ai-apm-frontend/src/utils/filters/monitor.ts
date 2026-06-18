import i18n from '@/i18n';

/**
 * 检测类型
 * @param {type}
 * @return {typeCn}
 */
export const MonitorTypeFilter = (type: string) => {
  if (!type) {
    return '-'
  }
  switch (type) {
    case 'singleMetric':
      return i18n.t('modules.utils.filters.s_150b3f91') as string;
    default:
      return i18n.t('modules.utils.filters.s_b4aa39f7') as string;
  }
}

/**
 * 检测方法
 * @param {type}
 * @return {typeCn}
 */
export const MonitorMethodFilter = (type: string) => {
  switch (type) {
    case 'threshold':
      return i18n.t('modules.utils.filters.s_f46a02b2') as string
    case 'mutation':
      return i18n.t('modules.utils.filters.s_d64c3683') as string
    default:
      return type || '-'
  }
}
