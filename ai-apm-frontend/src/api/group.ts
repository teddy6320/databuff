import http from '@/utils/axios';

export default {
  /*
    管理域列表
   */
  getGroupList: (data?: any) => {
    return http.request({
      url: '/group/list',
      method: 'post',
      data
    })
  },
  getGroupListByUser () {
    return http.get('/user/findRoleGroupByUser')
  },
  getGroupListByRole (roleId: any) {
    return http.get(`/group/getRoleGroup/${roleId}`)
  },
  addGroup: (data: any) => {
    return http.request({
      url: '/group/add',
      method: 'post',
      data
    })
  },
  updateGroup: (data: any) => {
    return http.request({
      url: '/group/update',
      method: 'post',
      data
    })
  },
  deleteGroup: (data: any) => {
    return http.request({
      url: '/group/delete',
      method: 'post',
      data
    })
  },
  getRuleList: (data: any) => {
    return http.request({
      url: '/group/rules',
      method: 'post',
      data
    })
  },
  addRule: (data: any) => {
    return http.request({
      url: '/group/addRule',
      method: 'post',
      data
    })
  },
  updateRule: (data: any) => {
    return http.request({
      url: '/group/updateRule',
      method: 'post',
      data
    })
  },
  deleteRule: (data: any) => {
    return http.request({
      url: '/group/deleteRule',
      method: 'post',
      data
    })
  },
  getGroupStatus: (data: any) => {
    return http.request({
      url: '/group/status',
      method: 'post',
      data
    })
  },
  updateStatus: (data: any) => {
    return http.request({
      url: '/group/updateStatus',
      method: 'post',
      data
    })
  },
  bindRoleGroup (data: any) {
    return http.post('/group/roleBind', data)
  },
  unbindRoleGroup (data: any) {
    return http.post('/group/roleUnbind', data)
  },
  getUngroupEntity () {
    return http.get('/group/unGroupObjsStat')
  },
  getUngroupList (params: any) {
    const _params = { ...params };
    delete _params.fromTime
    delete _params.toTime
    delete _params.pageSize
    delete _params.pageNum
    return http.request({
      url: '/group/unGroupObjsList',
      method: 'post',
      data: _params
    })
  },
  updateAutoBsStatus (enabled: number) {
    return http.post('/meta/config/updateEnable', { enabled, code: 'AUTO_GENERATED_GROUP' })
  },
  getAutoBsStatus () {
    return http.get('/meta/config/getByCode?code=AUTO_GENERATED_GROUP')
  },
  setCustomGroup (data: any) {
    return http.post('/group/dataSource/update', data)
  },
}
