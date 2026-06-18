import dayjs from 'dayjs'
import humanFormat from 'human-format';

const secondScale = new humanFormat.Scale({
  s: 1,
  min: 60,
  hours: 3600,
  days: 86400,
  months: 2592000,
})
const nsScale = new humanFormat.Scale({
  ns: 1,
  µs: 1000,
  ms: 1000000,
  s: 1e9,
  min: 60 * 1e9,
  h: 60 * 60 * 1e9,
  day: 60 * 60 * 24 * 1e9,
  y: 60 * 60 * 24 * 365 * 1e9,
})
const msScale = new humanFormat.Scale({
  ms: 1,
  s: 1000,
  min: 60 * 1000,
  h: 60 * 60 * 1000,
  day: 24 * 60 * 60 * 1000,
})

/**
 * @method TimesToDateFilter
 * @param {string|number} times
 * @return {string} 'YYYY-MM-DD HH:mm:ss'
 */
export const TimesToDateFilter = (value: string|number|Date, format: string = 'YYYY-MM-DD HH:mm:ss') => {
  if (!value) {
    return '-'
  }
  // times - Date对象
  if (Object.prototype.toString.call(value) === '[object Date]') {
    // 处理不合法日期
    return value.toString() === 'Invalid Date' ? '-' : dayjs(value).format(format)
  }
  // times - 秒级 或 毫秒级
  const _value = String(value);
  const _times = _value.length === 10 ? +`${value}000` : _value.length === 13 ? +value : 0;
  if (isNaN(_times) || !_times) {
    return '-'
  }
  // 格式化时间
  return dayjs(_times).format(format)
}

/**
 * @method SecondFilter
 * @param {second}
 * @param {zeroIgnore} 是否忽略0
 * @return {formatStr}
 */
export const SecondFilter = (second: number, zeroIgnore?: boolean) => {
  if (zeroIgnore && (!second || +second === 0)) {
    return '-'
  } else if ((!second && String(second) !== '0') || isNaN(+second)) {
    return '-'
  }
  return humanFormat(Number(second), { scale: secondScale })
};

/**
 * @method NsFilter
 * @param {ns}
 * @param {zeroIgnore} 是否忽略0
 * @return {formatStr}
 */
export const NsFilter = (ns: number, zeroIgnore?: boolean) => {
  if (zeroIgnore && (!ns || +ns === 0)) {
    return '-'
  } else if ((!ns && String(ns) !== '0') || isNaN(+ns)) {
    return '-'
  }
  return humanFormat(Number(ns), { scale: nsScale })
};

/**
 * @method MsFilter
 * @param {ms}
 * @param {zeroIgnore} 是否忽略0
 * @return {formatStr}
 */
export const MsFilter = (ms: number, zeroIgnore?: boolean) => {
  if (zeroIgnore && (!ms || +ms === 0)) {
    return '-'
  } else if ((!ms && String(ms) !== '0') || isNaN(+ms)) {
    return '-'
  }
  return humanFormat(Number(ms), { scale: msScale })
};

/**
 * @method DurationFilter
 * @param {second}
 * @param {zeroIgnore} 是否忽略0
 * @return {formatStr}
 */
export const DurationFilter = (time: number, zeroIgnore?: boolean) => {
  if (zeroIgnore && (!time || +time === 0)) {
    return '-'
  } else if ((!time && String(time) !== '0') || isNaN(+time)) {
    return '-'
  } else if (+time === 0) {
    return '0'
  }
  return MsDurationFilter(time * 1000, zeroIgnore)
};

/**
 * @method MsDurationFilter
 * @param {ms}
 * @param {zeroIgnore} 是否忽略0
 * @return {formatStr}
 */
export const MsDurationFilter = (time: number, zeroIgnore?: boolean) => {
  if (zeroIgnore && (!time || +time === 0)) {
    return '-'
  } else if ((!time && String(time) !== '0') || isNaN(+time)) {
    return '-'
  } else if (+time === 0) {
    return '0'
  }
  time = Number(time)
  let sign = 1
  if (time < 0) {
    time = Math.abs(time)
    sign = -1
  }
  const { y, M, d, h, min, s } = {
    y: Math.floor(time / 1000 / 86400 / 365),
    M: Math.floor((time / 1000 / 86400 % 365) / 30),
    d: Math.floor((time / 1000 / 86400 % 365) % 30),
    h: Math.floor(time / 1000 / 60 / 60 % 24),
    min: Math.floor(time / 1000 / 60 % 60),
    s: Math.floor(time / 1000 % 60),
  }
  let ms = Math.floor(time % 1000)
  let µs = 0
  let ns = 0
  if (time < 0.001) {
    ns = +(time * 1000 * 1000).toFixed(2)
  } else if (time < 1) {
    µs = +(time * 1000).toFixed(2)
  } else if (time < 1000) {
    ms = +time.toFixed(2)
  }
  const list = [
    { name: 'y', value: y },
    { name: 'M', value: M },
    { name: 'd', value: d },
    { name: 'h', value: h },
    { name: 'min', value: min },
    { name: 's', value: s },
    { name: 'ms', value: ms },
    { name: 'µs', value: µs },
    { name: 'ns', value: ns },
  ]
  return list.filter(t => !!t.value).slice(0, 2).map(t => (t.value * sign) + t.name).join(' ') || '0'
}

/**
 * @method NsDurationFilter
 * @param {ns}
 * @param {zeroIgnore} 是否忽略0
 * @return {formatStr}
 */
export const NsDurationFilter = (time: number, zeroIgnore?: boolean) => {
  if (zeroIgnore && (!time || +time === 0)) {
    return '-'
  } else if ((!time && String(time) !== '0') || isNaN(+time)) {
    return '-'
  } else if (+time === 0) {
    return '0'
  }
  return MsDurationFilter(time / 1000 / 1000, zeroIgnore)
};
