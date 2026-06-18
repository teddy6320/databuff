import i18n from '@/i18n';
import humanFormat from 'human-format';
import getUnitData from '@/utils/getUnitData';

/**
 * @param {number}
 * @return {}
 */
export const NumberFilter = (value: number|string, lessZeroOne?: boolean, suffix?: string) => {
  if (!value && String(value) !== '0' || isNaN(+value) || !isFinite(+value)) {
    return '-'
  }
  const _val = Number(value)
  if (_val === 0 && !lessZeroOne) {
    return '0' + (suffix || '');
  }
  if (_val < 0.1) {
    return '< 0.1' + (suffix || '');
  }
  if (_val < 1) {
    return `${+_val.toFixed(2)}` + (suffix || '');
  }
  return humanFormat(_val || 0, { decimals: _val > 1000 ? 1 : 2 }) + (suffix || '');
}

/* 废弃，使用 NumberFilter */
// export const FloatFilter2 = (value: number|string, precision: number = 2) => {
//   if (!value && String(value) !== '0' || isNaN(+value) || !isFinite(+value)) {
//     return '-'
//   }
//   const _val = Number(value)
//   if (_val === 0) {
//     return '0'
//   }
//   // 超过 1000 不显示小数点
//   if (_val > 1000) {
//     return humanFormat(_val || 0, { decimals: 1 })
//   }
//   // 判断数值是否有小数点
//   const [prefix, suffix] = String(_val).split('.');
//   if (!suffix) {
//     return prefix
//   }
//   const precisionVal = suffix.substring(0, precision);
//   // 如果保留精度后为'00..'，则末尾精度上取整为1
//   if (precisionVal === Array(precision).fill('0').join('')) {
//     return `${prefix}.${suffix.substring(0, precision - 1)}1`
//   }
//   return `${prefix}.${precisionVal}`
// }

export const PercentFilter = (value: any, lessZeroOne?: boolean) => {
  if (!value && String(value) !== '0' || isNaN(+value) || !isFinite(+value)) {
    return '-'
  }
  const _val = Number(value)
  if (_val === 0 && !lessZeroOne) {
    return '0%'
  }
  if (_val < 0.001) {
    return '< 0.1%'
  }
  return `${+(_val * 100).toFixed(2)}%`
}

export const BytesFilter = (value: number|string, lessZeroOne?: boolean, suffix?: string) => {
  if (!value && String(value) !== '0' || isNaN(+value) || !isFinite(+value)) {
    return '-'
  }
  const _val = Number(value)
  if (_val === 0 && !lessZeroOne) {
    return '0 B' + (suffix || '');
  }
  if (_val < 0.1) {
    return '< 0.1 B' + (suffix || '');
  }
  return humanFormat.bytes(_val || 0) + (suffix || '');
}

export const ValueFormatByUnit = (value: number, unit: string = '', lessZeroOne?: boolean) => {
  if (!value && String(value) !== '0' || isNaN(+value) || !isFinite(+value)) {
    return '-'
  }
  const { scale_factor, scale, sub_unit, family } = getUnitData(unit);
  if (family === 'percentage') {
    return PercentFilter(Number(value) / 100, lessZeroOne);
  } else if (!['time', 'bytes'].includes(family)) {
    return NumberFilter(value, lessZeroOne, unit);
  } else {
    const vData = humanFormat.raw(Number(value) * scale_factor, {
      ...scale,
      decimals: 2,
    })
    return `${vData.value} ${vData.prefix}${scale.unit}${sub_unit}`
  }
}
export const NumberIntlFormat = (value: number) => {
  return Intl.NumberFormat().format(Number(value));
}

const numCnMap: { [key: string]: string } = {
  1: i18n.t('modules.utils.filters.s_7941da94') as string,
  2: i18n.t('modules.utils.filters.s_2d8be272') as string,
  3: i18n.t('modules.utils.filters.s_e662ff59') as string,
  4: i18n.t('modules.utils.filters.s_21716cf3') as string,
  5: i18n.t('modules.utils.filters.s_1fcc29d0') as string,
  6: i18n.t('modules.utils.filters.s_61b45352') as string,
  7: i18n.t('modules.utils.filters.s_aad6914f') as string,
  8: i18n.t('modules.utils.filters.s_edf1acd5') as string,
  9: i18n.t('modules.utils.filters.s_9b4851f8') as string,
  10: i18n.t('modules.utils.filters.s_18124572') as string
}

// 1-> '一', 2-> '二', ..., 10-> '十', 20-> '二十', 21-> '二十一'
export const NumberToCnFilter = (num: number) => {
  if (num < 1 || num > 99) {
    return String(num);
  }
  if (num <= 10) {
    return numCnMap[String(num)];
  }
  const tens = Math.floor(num / 10);
  const units = num % 10;
  let result = '';
  if (tens > 0) {
    result += numCnMap[tens] + '十';
  }
  if (units > 0) {
    result += numCnMap[units];
  }
  return result;
}
