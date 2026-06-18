import i18n from '@/i18n';
export const FiledDataMap = {
  HOST: {
    host: {
      value: 'host',
      label: i18n.t('modules.views.sysManage.health.s_1c7b2dc6') as string, labelKey: 'modules.views.sysManage.health.s_1c7b2dc6',
      enabled: true,
    },
    // hostIp: {
    //   value: 'hostIp',
    //   label: i18n.t('modules.views.configManage.entity.s_2dc9105c') as string, labelKey: 'modules.views.configManage.entity.s_2dc9105c',
    //   enabled: true,
    // },
    // os: {
    //   value: 'os',
    //   label: i18n.t('modules.views.infrastructure.host.s_30d23ef4') as string, labelKey: 'modules.views.appMonitor.traceDetail.s_30d23ef4',
    //   enabled: true,
    // },
  },
  SERVICE: {
    service: {
      value: 'service',
      label: i18n.t('modules.views.alarmCenter.alarm.s_8f3747c0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_8f3747c0',
      enabled: true,
    },
  },
  DB: {
    service: {
      value: 'service',
      label: i18n.t('modules.views.appMonitor.database.s_d5f399b9') as string, labelKey: 'modules.views.appMonitor.database.s_d5f399b9',
      enabled: true,
    },
  },
  MQ: {
    service: {
      value: 'service',
      label: i18n.t('modules.views.appMonitor.traceDetail.s_6182604d') as string, labelKey: 'modules.views.appMonitor.traceDetail.s_6182604d',
      enabled: true,
    },
  },
  CACHE: {
    service: {
      value: 'service',
      label: i18n.t('modules.views.appMonitor.cache.s_586324c5') as string, labelKey: 'modules.views.appMonitor.cache.s_586324c5',
      enabled: true,
    },
  },
  REMOTECALL: {
    service: {
      value: 'service',
      label: i18n.t('modules.views.appMonitor.external.s_0d496d78') as string, labelKey: 'modules.views.appMonitor.external.s_0d496d78',
      enabled: true,
    },
  },
  ENDPOINTS: {
    resource: {
      value: 'resource',
      label: i18n.t('modules.views.alarmCenter.eventDetail.s_34cab80c') as string, labelKey: 'modules.views.alarmCenter.eventDetail.s_34cab80c',
      enabled: true,
    },
    alias: {
      value: 'alias',
      label: i18n.t('modules.views.appMonitor.resourceDetail.s_7ef19dc0') as string, labelKey: 'modules.views.appMonitor.resourceDetail.s_7ef19dc0',
      enabled: true,
    },
    service: {
      value: 'service',
      label: i18n.t('modules.views.sysManage.health.s_9f71eff6') as string, labelKey: 'modules.views.observe.scene.s_9f71eff6',
      enabled: true,
    },
  },
  // web: {
  //   applicationName: {
  //     value: 'applicationName',
  //     label: i18n.t('modules.views.sysManage.health.s_9d9d8cfb') as string, labelKey: 'modules.views.sysManage.health.s_9d9d8cfb',
  //     enabled: true,
  //   }
  // },
  // ios: {
  //   applicationName: {
  //     value: 'applicationName',
  //     label: i18n.t('modules.views.sysManage.health.s_c63b2622') as string, labelKey: 'modules.views.sysManage.health.s_c63b2622',
  //     enabled: true,
  //   }
  // },
}

export default FiledDataMap;

export const MetricThresholdMap: Record<string, { thresholdMin: number; thresholdMax: number; accMode: string }> = {
  'system.mem.usage': {
    thresholdMin: 0,
    thresholdMax: 90,
    accMode: 'desc',
  },
  'system.cpu.usage': {
    thresholdMin: 20,
    thresholdMax: 70,
    accMode: 'desc',
  },
  'service.avgDuration': {
    thresholdMin: 0,
    thresholdMax: 200,
    accMode: 'desc',
  },
  'service.error.pct': {
    thresholdMin: 0,
    thresholdMax: 5,
    accMode: 'desc',
  },
  'service.db.slow.pct': {
    thresholdMin: 0,
    thresholdMax: 15,
    accMode: 'desc',
  },
  'service.db.error.pct': {
    thresholdMin: 0,
    thresholdMax: 5,
    accMode: 'desc',
  },
  'service.mq.slow.pct': {
    thresholdMin: 0,
    thresholdMax: 15,
    accMode: 'desc',
  },
  'service.mq.error.pct': {
    thresholdMin: 0,
    thresholdMax: 5,
    accMode: 'desc',
  },
  'service.redis.slow.pct': {
    thresholdMin: 0,
    thresholdMax: 15,
    accMode: 'desc',
  },
  'service.redis.error.pct': {
    thresholdMin: 0,
    thresholdMax: 5,
    accMode: 'desc',
  },
  'service.remote.avgDuration': {
    thresholdMin: 0,
    thresholdMax: 200,
    accMode: 'desc',
  },
  'service.remote.error.pct': {
    thresholdMin: 0,
    thresholdMax: 5,
    accMode: 'desc',
  },
  'service.http.avgDuration': {
    thresholdMin: 0,
    thresholdMax: 200,
    accMode: 'desc',
  },
  'service.http.error.pct': {
    thresholdMin: 0,
    thresholdMax: 5,
    accMode: 'desc',
  },
};

export const getMetricThreshold = (metric: string) => {
  return MetricThresholdMap[metric] || {
    thresholdMin: 0,
    thresholdMax: 0,
    accMode: 'asc',
  };
};

export const getKandB = (x0: number, x1: number, asc: boolean) => {
  if (asc) {
    const k = 100 / (x1 - x0);
    const b = -k * x0;
    return { k, b };
  } else {
    const k = 100 / (x0 - x1);
    const b = 100 - k * x0;
    return { k, b };
  }
};
