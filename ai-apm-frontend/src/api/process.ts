import http from '../utils/axios'

export default {
  /* 进程采集 */
  // 获取进程采集规则列表
  getCollectRuleList: (data: any) => {
    return http.request({
      url: '/processCollectRules/list',
      method: 'post',
      data
    })
  },
  // 创建进程采集规则
  createCollectRule: (data: any) => {
    return http.request({
      url: '/processCollectRules/insert',
      method: 'post',
      data
    })
  },
  // 更新进程采集规则
  updateCollectRule: (data: any) => {
    return http.request({
      url: '/processCollectRules/edit',
      method: 'post',
      data
    })
  },
  // 更新进程采集规则
  toggleCollectRuleEnable: (data: any) => {
    return http.request({
      url: '/processCollectRules/updateStatus',
      method: 'post',
      data
    })
  },
  // 删除进程采集规则
  deleteCollectRule: (data: any) => {
    return http.request({
      url: '/processCollectRules/delete',
      method: 'post',
      data
    })
  },
  // 设置是否采集所有监控
  setCollectAll: (data: any) => {
    return http.request({
      url: '/processCollectRules/collectAll',
      method: 'post',
      data
    })
  },

  /* 进程识别 */
  // 获取进程识别规则列表
  getIdentifyRuleList: (data: any) => {
    return http.request({
      url: '/processIdentifyRules/list',
      method: 'post',
      data
    })
  },
  // 创建进程采集规则
  createIdentifyRule: (data: any) => {
    return http.request({
      url: '/processIdentifyRules/insert',
      method: 'post',
      data
    })
  },
  // 更新进程采集规则
  updateIdentifyRule: (data: any) => {
    return http.request({
      url: '/processIdentifyRules/edit',
      method: 'post',
      data
    })
  },
  // 更新进程采集规则
  toggleIdentifyRuleEnable: (data: any) => {
    return http.request({
      url: '/processIdentifyRules/updateStatus',
      method: 'post',
      data
    })
  },
  // 删除进程采集规则
  deleteIdentifyRule: (data: any) => {
    return http.request({
      url: '/processIdentifyRules/delete',
      method: 'post',
      data
    })
  },
}
