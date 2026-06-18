import humanFormat from 'human-format';

const humanFormatScales = {
  SI: new humanFormat.Scale({
    // 'y': 1e-24,
    // 'z': 1e-21,
    // 'a': 1e-18,
    // 'f': 1e-15,
    // 'p': 1e-12,
    // 'n': 1e-9,
    // 'µ': 0.000001,
    // 'm': 0.001,
    '': 1,
    'k': 1000,
    'M': 1000000,
    'G': 1e9,
    'T': 1e12,
    'P': 1e15,
    'E': 1e18,
    'Z': 1e21,
    'Y': 1e24,
  }),
  binary: 'binary',
  time: new humanFormat.Scale({
    // fs: 1e-15,
    // ps: 1e-12,
    ns: 1e-9,
    µs: 0.000001,
    ms: 0.001,
    s: 1,
    min: 60,
    h: 3600,
    d: 86400,
    M: 2592000,
    y: 31536000,
  }),
}

const unitList: any[] = [
  {
    family: 'bytes', // 单位体系
    name: 'byte', // 原单位
    plural: 'bytes', // 原单位复数形式
    original_short_name: 'B', // 原单位的简写
    short_name: 'B', // 转换后单位的简写
    scale_factor: 1, // name / short_name 的比值
  },
  {
    family: 'bytes',
    name: 'kibibyte',
    plural: 'kibibytes',
    original_short_name: 'KiB',
    short_name: 'B',
    scale_factor: 1024,
  },
  {
    family: 'bytes',
    name: 'mebibyte',
    plural: 'mebibytes',
    original_short_name: 'MiB',
    short_name: 'B',
    scale_factor: 1024 ** 2,
  },
  {
    family: 'bytes',
    name: 'gibibyte',
    plural: 'gibibytes',
    original_short_name: 'GiB',
    short_name: 'B',
    scale_factor: 1024 ** 3,
  },
  {
    family: 'bytes',
    name: 'tebibyte',
    plural: 'tebibytes',
    original_short_name: 'TiB',
    short_name: 'B',
    scale_factor: 1024 ** 4,
  },
  {
    family: 'bytes',
    name: 'bit',
    plural: 'bits',
    short_name: 'B',
    scale_factor: 1 / 8,
  },
  // {
  //   family: 'percentage',
  //   name: 'fraction',
  //   plural: 'fractions',
  //   short_name: '',
  //   scale_factor: 1,
  // },
  {
    family: 'percentage',
    name: 'percent',
    plural: '%',
    short_name: '%',
    scale_factor: 1,
  },
  {
    family: 'time',
    name: 'year',
    plural: 'years',
    original_short_name: 'y',
    short_name: 's',
    scale_factor: 31536000,
  },
  {
    family: 'time',
    name: 'month',
    plural: 'months',
    original_short_name: 'M',
    short_name: 's',
    scale_factor: 2592000,
  },
  {
    family: 'time',
    name: 'day',
    plural: 'days',
    original_short_name: 'd',
    short_name: 's',
    scale_factor: 86400,
  },
  {
    family: 'time',
    name: 'hour',
    plural: 'hours',
    original_short_name: 'h',
    short_name: 's',
    scale_factor: 3600,
  },
  {
    family: 'time',
    name: 'minute',
    plural: 'minutes',
    original_short_name: 'min',
    short_name: 's',
    scale_factor: 60,
  },
  {
    family: 'time',
    name: 'second',
    plural: 'seconds',
    original_short_name: 's',
    short_name: 's',
    scale_factor: 1,
  },
  {
    family: 'time',
    name: 'millisecond',
    plural: 'milliseconds',
    original_short_name: 'ms',
    short_name: 's',
    scale_factor: 0.001,
  },
  {
    family: 'time',
    name: 'microsecond',
    plural: 'microseconds',
    original_short_name: 'µs',
    short_name: 's',
    scale_factor: 0.000001,
  },
  {
    family: 'time',
    name: 'nanosecond',
    plural: 'nanoseconds',
    original_short_name: 'ns',
    short_name: 's',
    scale_factor: 1e-9,
  },
  {
    family: 'network',
    name: 'request',
    plural: 'requests',
    short_name: 'req',
    scale_factor: 1,
  },
  {
    family: 'network',
    name: 'packet',
    plural: 'packets',
    short_name: 'pkt',
    scale_factor: 1,
  },
  {
    family: 'system',
    name: 'thread',
    plural: 'threads',
    short_name: null,
    scale_factor: 1,
  },
  {
    family: 'system',
    name: 'garbage collection',
    plural: 'garbage collections',
    short_name: 'gc',
    scale_factor: 1,
  },
]

/**
 * 根据单个 unit 获取 unitItem
 * @param u unit
 * @param after 是否为'/'后的单位(eq: request/s)
 * @returns unitItem
 */
const getUnit = (u: string, after = false) => {
  const item = unitList.find(t => t.name === u || t.plural === u || t.original_short_name === u)
  // 不在 unitList 列表
  if (!item) {
    return {
      name: u,
      plural: u,
      short_name: `${after ? '' : ' '}${u}`,
      sub_unit: '',
      original_short_name: '',
      family: '',
      scale_factor: 1,
      scale: {
        scale: humanFormatScales.SI,
        separator: '',
        unit: `${after ? '' : ' '}${u}`,
      },
    }
  }

  // 在 unitList 列表
  const { SI, binary, time } = humanFormatScales
  const { family } = item
  let short_name = item.short_name || item.plural
  if (!after) {
    short_name = family === 'time' ? '' : family === 'bytes' ? short_name : ` ${short_name}`
  } else { // 为'/'后的单位
    short_name = item.original_short_name || short_name
  }
  return {
    ...item,
    short_name,
    sub_unit: '',
    scale: {
      scale: family === 'time' ? time : family === 'bytes' ? binary : SI,
      separator: family === 'bytes' ? ' ' : '',
      unit: short_name,
    },
  }
}

/**
 * 判断 unit 是否有 '/'，获取 unitItem
 * @param unit unit
 * @returns unitItem
 */
const getUnitData = (unit: string) => {
  unit = unit || '' // 参数形式 unit = '' 不能将 null 值改为空字符串
  const splitIdx = unit.indexOf('/')
  if (splitIdx < 0) {
    return getUnit(unit)
  } else { // 有 '/'
    const units = [unit.substring(0, splitIdx), unit.substring(splitIdx + 1)]
    const unitItem0 = getUnit(units[0])
    const unitItem1 = getUnit(units[1], true)
    return {
      ...unitItem0,
      name: `${unitItem0.name}/${unitItem1.name}`,
      plural: `${unitItem0.plural}/${unitItem1.plural}`,
      short_name: `${unitItem0.short_name}/${unitItem1.short_name}`,
      original_short_name: `${unitItem0.original_short_name || unitItem0.name}/${unitItem1.short_name}`,
      sub_unit: unitItem1.short_name ? `/${unitItem1.short_name}` : ''
    }
  }
}

export const getSimlarUnits = (defaultUnit: string) => {
  if (!defaultUnit) {
    return [];
  }
  const { family, scale_factor } = getUnitData(defaultUnit);
  if (['bytes', 'time'].includes(family)) {
    const isPer = defaultUnit.indexOf('/s') !== -1;
    const perPrefix = isPer ? '/s' : '';
    const units: any = {
      time: [
        { label: 'day', value: 'day', scale: 3600 * 24 },
        { label: 'h', value: 'h', scale: 3600 },
        { label: 'min', value: 'min', scale: 60 },
        { label: 's', value: 's', scale: 1 },
        { label: 'ms', value: 'ms', scale: 0.001 / scale_factor },
        { label: 'µs', value: 'µs', scale: 0.000001 / scale_factor },
        { label: 'ns', value: 'ns', scale: 1e-9 / scale_factor },
      ],
      bytes: [
        { label: 'B' + perPrefix, value: 'B' + perPrefix, scale: 1 / scale_factor },
        { label: 'KiB' + perPrefix, value: 'KiB' + perPrefix, scale: 1024 / scale_factor },
        { label: 'MiB' + perPrefix, value: 'MiB' + perPrefix, scale: 1024 ** 2 / scale_factor },
        { label: 'GiB' + perPrefix, value: 'GiB' + perPrefix, scale: 1024 ** 3 / scale_factor },
        { label: 'TiB' + perPrefix, value: 'TiB' + perPrefix, scale: 1024 ** 4 / scale_factor },
      ],
    }
    return units[family] || [];
  } else {
    return [];
  }
}

export default getUnitData;
