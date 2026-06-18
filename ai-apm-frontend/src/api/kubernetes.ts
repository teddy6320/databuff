import http from '../utils/axios'
import { AxiosPromise } from 'axios'

export default {
  // 获取集群列表
  getClusterList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/cluster/list',
      method: 'post',
      data,
    })
  },
  // 获取集群 select 下拉列表 
  getClusterSelectList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/cluster/idNameList',
      method: 'post',
      data,
    })
  },
  // 获取Namespace列表
  getNamespaceList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/ns/list',
      method: 'post',
      data,
    })
  },
  getNamespaceSelectList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/ns/list',
      method: 'post',
      data,
      transformResponse: [(resData) => {
        try {
          const formatedData = typeof resData === 'string' ? JSON.parse(resData) : resData
          const _data = formatedData.data || []
          const mapping: any = {}
          _data.forEach((item: any) => {
            mapping[item.name] = item.name
          })
          return {
            ...formatedData,
            data: mapping
          }
        } catch (err) {
          return resData
        }
      }]
    })
  },
  // 获取Workload列表
  getWorkloadList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/wl/list',
      method: 'post',
      data,
    })
  },
  getWorkloadSelectList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/wl/list',
      method: 'post',
      data,
      transformResponse: [(resData) => {
        try {
          const formatedData = typeof resData === 'string' ? JSON.parse(resData) : resData
          const _data = formatedData.data || []
          const mapping: any = {}
          _data.forEach((item: any) => {
            mapping[item.name] = item.name
          })
          return {
            ...formatedData,
            data: mapping
          }
        } catch (err) {
          return resData
        }
      }]
    })
  },
  // 获取Pod列表
  getPodList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/pod/list',
      method: 'post',
      data,
    })
  },
  getPodSelectList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/pod/list',
      method: 'post',
      data,
      transformResponse: [(resData) => {
        try {
          const formatedData = typeof resData === 'string' ? JSON.parse(resData) : resData
          const _data = formatedData.data || []
          const mapping: any = {}
          _data.forEach((item: any) => {
            mapping[item.name] = item.name
          })
          return {
            ...formatedData,
            data: mapping
          }
        } catch (err) {
          return resData
        }
      }]
    })
  },
  // 获取Node列表
  getNodeList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/node/list',
      method: 'post',
      data,
    })
  },
  getNodeSelectList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/node/list',
      method: 'post',
      data,
      transformResponse: [(resData) => {
        try {
          const formatedData = typeof resData === 'string' ? JSON.parse(resData) : resData
          const _data = formatedData.data || []
          const mapping: any = {}
          _data.forEach((item: any) => {
            mapping[item.name] = item.name
          })
          return {
            ...formatedData,
            data: mapping
          }
        } catch (err) {
          return resData
        }
      }]
    })
  },
  // 获取Service列表
  getServiceList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/svc/list',
      method: 'post',
      data,
    })
  },
  getServiceSelectList: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/svc/list',
      method: 'post',
      data,
      transformResponse: [(resData) => {
        try {
          const formatedData = typeof resData === 'string' ? JSON.parse(resData) : resData
          const _data = formatedData.data || []
          const mapping: any = {}
          _data.forEach((item: any) => {
            mapping[item.name] = item.name
          })
          return {
            ...formatedData,
            data: mapping
          }
        } catch (err) {
          return resData
        }
      }]
    })
  },
  // 获取Node详情
  getNodeDetail: (data: any): AxiosPromise => {
    return http.request({
      url: '/k8s/node/info',
      method: 'post',
      data,
    })
  },
}
