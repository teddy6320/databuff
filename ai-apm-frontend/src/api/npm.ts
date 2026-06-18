import http from '../utils/axios'
import { AxiosPromise } from 'axios'
export default {
  // 获取网络分析筛选数据
  getPerformanceTags: (data: any): AxiosPromise => {
    return http.request({
      url: '/metrics/npm/performance/tags',
      method: 'post',
      data
    })
  },
  // 获取网络分析趋势指标
  getPerformanceMetrics: (): AxiosPromise => {
    return http.request({
      url: '/metrics/npm/performance/metrics',
      method: 'get',
    })
  },
  // 获取指标趋势数据
  getPerformanceMetricsData: (data: any): AxiosPromise => {
    return http.request({
      url: '/metrics/npm/performance',
      method: 'post',
      data
    })
  },
  // 获取网络分析列表
  getPerformanceList: (data: any): AxiosPromise => {
    return http.request({
      url: '/metrics/npm/performance/list',
      method: 'post',
      data
    })
  },
  // 获取网络分析详情，流量列表
  getPerformanceVolumeList: (data: any): AxiosPromise => {
    return http.request({
      url: '/metrics/npm/volume/details',
      method: 'post',
      data
    })
  },
  // 网络拓扑图nodes
  getNpmTopoNodes: (data: any): AxiosPromise => {
    return http.request({
      url: '/metrics/npm/topo/nodes',
      method: 'post',
      data
    })
  },
  // 网络拓扑图edges
  getNpmTopoEdges: (data: any): AxiosPromise => {
    return http.request({
      url: '/metrics/npm/topo/edges',
      method: 'post',
      data
    })
  },

  // 获取DNS分析筛选数据
  getDnsPerformanceTags: (data: any): AxiosPromise => {
    return http.request({
      url: '/metrics/npm/dns/performance/tags',
      method: 'post',
      data
    })
  },
  // 获取DNS网络分析列表
  getDnsPerformanceList: (data: any): AxiosPromise => {
    return http.request({
      url: '/metrics/npm/dns/performance/list',
      method: 'post',
      data
    })
  },
  // 获取DNS分析详情，流量列表
  getDnsPerformanceVolumeList: (data: any): AxiosPromise => {
    return http.request({
      url: '/metrics/npm/dns/volume/details',
      method: 'post',
      data
    })
  },
}
