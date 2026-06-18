import http from '../utils/axios'
import { AxiosPromise } from 'axios'
import * as Infrastructure from './infrastructure.types'

export default {
  /**
   * 获取主机列表
   */
  getHostList: (data: Infrastructure.ListInterface | Infrastructure.ScrollId): AxiosPromise => {
    return http.request({
      url: '/base/host/list',
      method: 'post',
      data
    })
  },
  /**
   * 获取主机分组列表
   */
  getHostGroupList: (data: Infrastructure.ListInterface): AxiosPromise => {
    return http.request({
      url: '/base/host/group_list',
      method: 'post',
      data
    })
  },

  /**
   * 获取容器列表
   */
  getContainerList: (data: Infrastructure.ListInterface | Infrastructure.ScrollId): AxiosPromise => {
    return http.request({
      url: '/base/container/list',
      method: 'post',
      data
    })
  },
  /**
   * 获取容器分组列表
   */
  getContainerGroupList: (data: Infrastructure.ListInterface): AxiosPromise => {
    return http.request({
      url: '/base/container/group_list',
      method: 'post',
      data
    })
  },

  /**
   * 获取进程列表
   */
  getProcessList: (data: Infrastructure.ProcessGroupList): AxiosPromise => {
    return http.request({
      url: '/base/processGroup/v2/list',
      method: 'post',
      data
    })
  },
  /**
   * 获取进程详情
   */
  getProcessDetail: (data: any): AxiosPromise => {
    return http.request({
      url: '/base/processGroup/info',
      method: 'post',
      data
    })
  },


  /**
   * 获取主机hostMap列表
   */
  getHostmapList: (data: Infrastructure.HostmapInterface): AxiosPromise => {
    return http.request({
      url: '/base/host/hostmap',
      method: 'post',
      data
    })
  },

  /**
   * 获取分组数据
   */
  getGroupList: (data: Infrastructure.GroupInterface): AxiosPromise => {
    return http.request({
      url: '/base/findMetrics',
      method: 'post',
      data
    })
  },

  /**
   * 自定义主机标签
   */
  customHostTag: (data: any): AxiosPromise => {
    return http.request({
      url: '/base/customHostTag',
      method: 'post',
      data
    })
  },

  /**
   * 获取主机标签
   */
  getHostTag: (data: Infrastructure.GetHostTag): AxiosPromise => {
    return http.request({
      url: '/base/getHostTag',
      method: 'post',
      data
    })
  },

  /**
   * 获取主机列表应用筛选项
   */
   findHostApps: (data: Infrastructure.GetHostTag): AxiosPromise => {
    return http.request({
      url: '/base/findHostApps',
      method: 'post',
      data
    })
  },

  /**
   * 获取主机列表操作系统筛选项
   */
   findHostOs: (data: Infrastructure.GetHostTag): AxiosPromise => {
    return http.request({
      url: '/base/findHostOs',
      method: 'post',
      data
    })
  },

  /**
   * 根据主机名获取主机详情
   */
  getHostInfo: (params: Infrastructure.HostnameInterface) => {
    return http.request({
      url: '/base/host/info',
      method: 'get',
      params
    })
  },

  /**
   * 获取进程指标趋势
   */
  getProcessGraph: (data: any) => {
    return http.request({
      url: '/base/v2/processGroup/graph',
      method: 'post',
      data
    })
  },

  /**
   * 设置主机管理IP
   */
   setHostManagerIp: (data: any) => {
    return http.request({
      url: '/base/setManagerIp',
      method: 'post',
      data
    })
  },

  /**
   * 获取主机基础信息
   */
   getHostObjs: (data: any) => {
    return http.request({
      url: '/base/objs',
      method: 'post',
      data
    })
  },
}
