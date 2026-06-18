import http from '../utils/axios'

export default {
  getGlobalTopology (data: { fromTime?: string; toTime?: string; limit?: number }) {
    return http.post('/globalTopology/graph', data)
  },
  getVerticalTree (data: {
    fromTime?: string
    toTime?: string
    limit?: number
    serviceId?: string
  }) {
    return http.post('/globalTopology/verticalTree', data)
  },
}
