import i18n from '@/i18n';

/** 告警设置筛选条件支持的操作符：等于、不等于、包含、不包含 */
export const alarmFilterSymbolList = [
  { label: i18n.t('modules.components.matching-criteria.s_4c35bf2e') as string, labelKey: 'modules.components.matching-criteria.s_4c35bf2e', value: '=', type: 'select' },
  { label: i18n.t('modules.components.matching-criteria.s_14a8af58') as string, labelKey: 'modules.components.matching-criteria.s_14a8af58', value: '!=', type: 'select' },
  { label: i18n.t('modules.components.matching-criteria.s_e13556bb') as string, labelKey: 'modules.components.matching-criteria.s_e13556bb', value: 'like' },
  { label: i18n.t('modules.components.matching-criteria.s_da0291f4') as string, labelKey: 'modules.components.matching-criteria.s_da0291f4', value: 'notLike' },
];
