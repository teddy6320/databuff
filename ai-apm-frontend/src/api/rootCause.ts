import http from '../utils/axios'
import { AxiosPromise } from 'axios'

export default {
  // 获取智能根因列表
  getProblemList: (_data: any): AxiosPromise => {
    const data = { ..._data }
    if (data.pageNum && data.pageSize) {
      data.page = data.pageNum
      data.size = data.pageSize
      delete data.pageNum
      delete data.pageSize
    }
    return http.request({
      url: '/webapi/api/influence/search',
      method: 'post',
      data,
    })
  },

  // 获取根因列表查询参数
  getProblemQueryParams: (data: any): AxiosPromise => {
    return http.request({
      url: '/webapi/api/influence/filterTags',
      method: 'post',
      data,
    })
  },

  // 获取根因分析详情
  getRootCauseAnalysisDetail: (params: any): AxiosPromise => {
    return http.request({
      url: '/webapi/api/issues/' + params.id,
      method: 'get',
    })
  },

  // 根因分析
  getRootCauseAnalysis: (data: any): AxiosPromise => {
    return http.request({
      url: '/root/analyse',
      method: 'post',
      data,
    })
  },

  // 获取根因类型分布 { fromTime: '', toTime: '', topN: 10 }
  getRootCauseTypes: (data: any): AxiosPromise => {
    return http.request({
      url: '/webapi/api/influence/chart/problemCauseType',
      method: 'post',
      data,
    })
  },

  // 获取根因节点分布 { fromTime: '', toTime: '', topN: 10 }
  getRootCauseNodes: (data: any): AxiosPromise => {
    return http.request({
      url: '/webapi/api/influence/chart/problemCauseNode',
      method: 'post',
      data,
    })
  },

  // 影响面分析
  getInfluenceAnalysis: (data: any): AxiosPromise => {
    return http.request({
      url: '/webapi/api/influence/analyse',
      method: 'post',
      data,
    })
  },

  // 问题详情
  getProblemDetail: (params: any): AxiosPromise => {
    return http.request({
      url: '/webapi/api/influence/findById',
      method: 'get',
      params,
    })
  },

  // 问题分析，问题收敛
  getInfluenceConvergence: (data: any): AxiosPromise => {
    return http.request({
      url: '/webapi/api/influence/convergence',
      method: 'post',
      data,
    })
  },

  // 问题分析，mttr & mtta
  getInfluenceMtt: (data: any): AxiosPromise => {
    return http.request({
      url: '/webapi/api/influence/mtt',
      method: 'post',
      data,
    })
  },

  // 问题分析，问题趋势
  getInfluenceTrend: (data: any): AxiosPromise => {
    return http.request({
      url: '/webapi/api/influence/metric',
      method: 'post',
      data,
    })
  },

  // 定位准确性反馈
  influenceFeedback: (data: any): AxiosPromise => {
    return http.request({
      url: '/webapi/api/influence/feedback',
      method: 'post',
      data,
    })
  },
}
