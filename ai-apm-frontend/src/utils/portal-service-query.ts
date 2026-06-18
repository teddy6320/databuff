/** Portal metric/trace queries must pass the 16-char service id, not display names. */
export interface PortalServiceMetricQuery {
  serviceId: string
  start?: number
  end?: number
  fromTime?: number
  toTime?: number
  serviceInstance?: string
  srcServiceId?: string
  componentType?: string
}

export interface PortalResourceMetricQuery extends PortalServiceMetricQuery {
  url?: string
  resource?: string
}

export function decodePortalServiceId (serviceId?: string | null): string {
  if (!serviceId) {
    return ''
  }
  try {
    return decodeURIComponent(String(serviceId))
  } catch {
    return String(serviceId)
  }
}
