import i18n from '@/i18n';

/**
 * 通知方式
 * @param {method}
 * @return {methodCn}
 */
export const NoticeMethodFilter = (value: string) => {
  switch (value) {
    case 'mail':
    case 'email':
      return i18n.t('modules.utils.filters.s_e9e8054f') as string;
    case 'sms':
      return i18n.t('modules.utils.filters.s_485c3abb') as string;
    case 'dingtalk':
      return i18n.t('modules.utils.filters.s_4a0e9142') as string;
    case 'wechat':
      return i18n.t('modules.utils.filters.s_ff17b9f9') as string;
    case 'webhook':
      return 'Webhook';
    case 'socket':
      return 'Socket';
    default:
      return value || '-'
  }
}

/**
 * 通知方式
 * @param {result}
 * @return {resultCn}
 */
export const NoticeResultFilter = (value: string) => {
  switch (value) {
    case 'success':
      return i18n.t('modules.utils.filters.s_330363df') as string;
    case 'fail':
      return i18n.t('modules.utils.filters.s_acd5cb84') as string;
    default:
      return '-'
  }
}
