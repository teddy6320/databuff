
import i18n from '@/i18n';

/**
 * 告警状态
 * 0-正常 ｜ 1-无数据 ｜ 2-次要 ｜ 3-重要
 * @param {status}
 * @return {statusCn}
 */
export const AlarmStatusFilter = (value: number|string) => {
  if (!value && String(value) !== '0') {
    return '-'
  }
  switch (Number(value)) {
    case 0:
      return i18n.t('modules.components.db-table.s_fd6e80f1') as string;
    case 1:
      return i18n.t('modules.utils.filters.s_01ceb3ed') as string;
    case 2:
      return i18n.t('modules.utils.filters.s_bde77082') as string;
    case 3:
      return i18n.t('modules.utils.filters.s_fc7e3846') as string;
    default:
      return '-'
  }
}

export const AlarmDealStatusFilter = (value: number) => {
  switch (value) {
    case 0:
      return i18n.t('modules.utils.filters.s_047109de') as string;
    case 2:
      return i18n.t('modules.utils.filters.s_5d459d55') as string;
    case 3:
      return i18n.t('modules.utils.filters.s_9c58505d') as string;
    case 4:
      return i18n.t('modules.utils.filters.s_714d98ea') as string;
    default:
      return '-'
  }
}

/**
 * 告警类型
 * @param {type}
 * @return {typeCn}
 */
export const AlarmTypeFilter = (value: string) => {
  switch (value) {
    case 'convergence':
      return i18n.t('modules.utils.filters.s_bc815bb3') as string;
    case 'metric':
      return i18n.t('modules.utils.filters.s_4d7a2b94') as string;
    case 'AI':
      return i18n.t('modules.utils.filters.s_9e2d429a') as string;
    case 'alarm_restore':
      return i18n.t('modules.utils.filters.s_56fa2dea') as string;
    default:
      return '-'
  }
}
