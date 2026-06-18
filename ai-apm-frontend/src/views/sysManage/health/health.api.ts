import http from '@/utils/axios'

export default {
  gethealthConfig (data: any = {}) {
    return http.request({
      url: '/healthConfig/getConfig',
      method: 'post',
      data
    })
  },
  createRule (data: any = {}) {
    return http.request({
      url: '/healthConfig/createPrivateRules',
      method: 'post',
      data
    })
  },
  editRule (data: any = {}) {
    return http.request({
      url: '/healthConfig/editRules',
      method: 'post',
      data
    })
  },
  deleteRule (data: any = {}) {
    return http.request({
      url: '/healthConfig/deleteRules',
      method: 'post',
      data
    })
  },
  setLevel (data: any = {}) {
    return http.request({
      url: '/healthConfig/changeColorSign',
      method: 'post',
      data
    })
  },
  sortRules (data: any = []) {
    return http.request({
      url: '/healthConfig/changePriority',
      method: 'post',
      data
    })
  }
}
