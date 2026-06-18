
import i18n from '@/i18n';

/**
 * 进程状态
 * @param {state}
 * @return {stateCn}
 */
export const ProcessStateFilter = (state: number) => {
  switch (state) {
    case 0:
      return i18n.t('modules.utils.filters.s_68905cf3') as string
    case 1:
      return i18n.t('modules.utils.filters.s_4805dd77') as string
    default:
      return i18n.t('modules.utils.filters.s_68905cf3') as string
  }
}

/**
 * 进程原始状态转换为进程状态
 * @param {state}
 * @return {state}
 */
export const ProcessOriginalStateFilter = (state: number) => {
  switch (state) {
    case 0: // Unknown
    case 1: // Disk sleep
    case 2: // Running
    case 3: // Sleep
    case 5: // Wait
      return 0; // 在线
    case 4: // Stop
    case 6: // Dead
    case 7: // Zombie
      return 1; // 下线
    default:
      return 0; // 在线
  }
}

/**
 * 集群相关类型
 * @param {type}
 * @return {typeCn}
 */
export const ClusterTypeFilter = (type: number) => {
  const typeMapping: any = {
    41: 'Pod',
    42: 'ReplicaSet',
    43: 'Deployment',
    44: 'Service',
    45: 'Node',
    46: 'Cluster',
    47: 'Job',
    48: 'CronJob',
    49: 'DaemonSet',
    50: 'StatefulSet',
    51: 'Namespace',
  }
  return typeMapping[+type] || type
}

/**
 * 健康状态
 * @param {status}
 * @return {statusCn}
 */
export const HealthStatusFilter = (status: number|boolean) => {
  switch (status) {
    case 0:
    case false:
      return i18n.t('modules.components.db-table.s_fd6e80f1') as string
    case 1:
    case true:
      return i18n.t('modules.components.db-table.s_c195df63') as string
    default:
      return '-'
  }
}
