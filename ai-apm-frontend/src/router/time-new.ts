enum TimeChooseType {
  SELECT = 'select',
  CUSTOM = 'custom'
}
export const TimeRangeMsOptions = [
  { label: '最近15分钟', value: 60 * 15 * 1000, type: TimeChooseType.SELECT, abbr: '15m' },
  { label: '最近30分钟', value: 60 * 30 * 1000, type: TimeChooseType.SELECT, abbr: '30m' },
  { label: '最近1小时', value: 60 * 60 * 1000, type: TimeChooseType.SELECT, abbr: '1h', default: true },
  { label: '最近2小时', value: 60 * 60 * 2 * 1000, type: TimeChooseType.SELECT, abbr: '2h' },
  { label: '最近4小时', value: 60 * 60 * 4 * 1000, type: TimeChooseType.SELECT, abbr: '4h' },
  { label: '最近6小时', value: 60 * 60 * 6 * 1000, type: TimeChooseType.SELECT, abbr: '6h' },
  { label: '最近12小时', value: 60 * 60 * 12 * 1000, type: TimeChooseType.SELECT, abbr: '12h' },
  { label: '最近24小时', value: 60 * 60 * 24 * 1000, type: TimeChooseType.SELECT, abbr: '24h' },
  { label: '最近3天', value: 60 * 60 * 24 * 3 * 1000, type: TimeChooseType.SELECT, abbr: '3d' },
  { label: '最近7天', value: 60 * 60 * 24 * 7 * 1000, type: TimeChooseType.SELECT, abbr: '7d' },
  { label: '最近15天', value: 60 * 60 * 24 * 15 * 1000, type: TimeChooseType.SELECT, abbr: '15d' },
  { label: '最近30天', value: 60 * 60 * 24 * 30 * 1000, type: TimeChooseType.SELECT, abbr: '30d' },
];

export const TimeRangeSOptions = [
  { label: '最近15分钟', value: 60 * 15, type: TimeChooseType.SELECT, abbr: '15m' },
  { label: '最近30分钟', value: 60 * 30, type: TimeChooseType.SELECT, abbr: '30m' },
  { label: '最近1小时', value: 60 * 60, type: TimeChooseType.SELECT, abbr: '1h', default: true },
  { label: '最近2小时', value: 60 * 60 * 2, type: TimeChooseType.SELECT, abbr: '2h' },
  { label: '最近4小时', value: 60 * 60 * 4, type: TimeChooseType.SELECT, abbr: '4h' },
  { label: '最近6小时', value: 60 * 60 * 6, type: TimeChooseType.SELECT, abbr: '6h' },
  { label: '最近12小时', value: 60 * 60 * 12, type: TimeChooseType.SELECT, abbr: '12h' },
  { label: '最近24小时', value: 60 * 60 * 24, type: TimeChooseType.SELECT, abbr: '24h' },
  { label: '最近3天', value: 60 * 60 * 24 * 3, type: TimeChooseType.SELECT, abbr: '3d' },
  { label: '最近7天', value: 60 * 60 * 24 * 7, type: TimeChooseType.SELECT, abbr: '7d' },
  { label: '最近15天', value: 60 * 60 * 24 * 15, type: TimeChooseType.SELECT, abbr: '15d' },
  { label: '最近30天', value: 60 * 60 * 24 * 30, type: TimeChooseType.SELECT, abbr: '30d' },
];
