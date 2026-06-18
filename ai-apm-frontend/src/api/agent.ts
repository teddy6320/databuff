import http from '../utils/axios'

export default {
  getVersionList () {
    return http.request({
      url: '/agent/packList',
      method: 'get'
    })
  },
  getUpdateInfo () {
    return http.request({
      url: '/agent/updateProgress',
      method: 'get',
    })
  },
  submitUpdate (data: { hosts: string[], packId?: number, operation: number }) {
    // operation: 0更新，1重启，2停止，3启动，4上传日志
    return http.request({
      url: '/agent/submitUpPlan',
      method: 'post',
      data
    })
  },
  configUpdate (data: any) {
    return http.request({
      url: '/agent/modifyConfig',
      method: 'post',
      data
    })
  },
  deletePackage (id: string) {
    return http.request({
      url: `/agent/delPack?id=${id}`,
      method: 'delete'
    })
  },
  versionSpread (params?: any) {
    return http.request({
      url: '/agent/versionSts',
      method: 'get',
    })
  },
  timeDiffTop (params?: any) {
    return http.request({
      url: '/agent/timeDiffTop',
      method: 'get',
    })
  },
  getOnline (data?: any) {
    return http.request({
      url: '/agent/online',
      method: 'post',
      data,
    })
  },
  getList (data: any) {
    return http.request({
      url: '/agent/list',
      method: 'post',
      data,
    })
  },
  getDcSite () {
    return http.request({
      url: '/agent/getDcSite',
      method: 'get',
    })
  },
  getCpuTop (data: any) {
    return http.request({
      url: '/agent/cup_usage_top',
      method: 'post',
      data,
    })
  },
  getUploadTop (data: any) {
    return http.request({
      url: '/agent/upload_num_top',
      method: 'post',
      data,
    })
  },
  readUpdateMsg (data: any) {
    return http.request({
      url: '/agent/delAgentUpdata?id=' + data.id,
      method: 'delete',
    })
  },
  getAgentDetail (params: any) {
    return http.request({
      url: '/agent/info',
      method: 'get',
      params,
    })
  },
  getAgentLogLost (data: any) {
    return http.request({
      url: '/agent/agentLogList',
      method: 'post',
      data,
    })
  },
  updateAgentLogNew (data: any) {
    return http.request({
      url: '/agent/updateAgentLogNew',
      method: 'post',
      data,
    })
  },
  loadAgentLogContent (params: any) {
    return http.request({
      url: '/agent/readAgentLog',
      method: 'get',
      responseType: params.type === 2 ? 'blob' : 'text',
      params,
    })
  },
  uploadPackage (data: any, config?: any) {
    return http.post('/agent/upload', data, config)
  },
  updatePreload (data: any) {
    return http.request({
      url: '/agent/preload',
      method: 'post',
      data,
    })
  },

  // 获取Agent安装配置
  getAgentInstallConfig (data: any) {
    return http.request({
      url: '/agent/getAgentInstallConfig',
      method: 'post',
      params: data,
    })
  },
  // 更新Agent安装配置
  updateAgentInstallConfig (data: any) {
    return http.post('/agent/updateAgentInstallConfig', data)
  },
}
