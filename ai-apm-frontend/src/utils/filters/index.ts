import * as CommonFilters from './common'
import * as TimesFilters from './times'
import * as AlarmFilters from './alarm'
import * as NumberFilters from './number'
import * as MonitorFilters from './monitor'
import * as InfraFilters from './infra'
import * as NoticeFilters from './notice'
import * as ServiceFilters from './service'
import * as MetricFilters from './metric'
import * as ConfigFilters from './config'

export default {
  ...CommonFilters,
  ...TimesFilters,
  ...AlarmFilters,
  ...NumberFilters,
  ...MonitorFilters,
  ...InfraFilters,
  ...NoticeFilters,
  ...ServiceFilters,
  ...MetricFilters,
  ...ConfigFilters,
}
