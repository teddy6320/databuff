import http from '../utils/axios';
import { AxiosPromise } from 'axios';

export default {
  /**
   * 判断是否过期
   */
  isOuttime (data?: any) {
    return http.request({
      url: '/saasCustomer/isOutTime',
      method: 'post',
      data
    })
  },

  /**
   * 创建系统用户
   */
  createAdminAduit: (data: any): AxiosPromise => {
    return http.request({
      url: '/user/createAdminAduit',
      method: 'post',
      data,
    });
  },

  /**
   * 获取系统基础设置
   * @return {AxiosPromise}
   */
  getSystemBase: (): AxiosPromise => {
    return http.request({
      url: '/system/systemBase',
      method: 'get',
    });
  },

  /**
   * 获取服务器时间
   * @return {AxiosPromise}
   */
  getsysdate: (): AxiosPromise => {
    return http.request({
      url: '/system/getDate',
      method: 'get',
    });
  },

  /**
   * 设置服务器时间
   * @param {date}
   * @return {AxiosPromise}
   */
  setsysdate: (data: any): AxiosPromise => {
    return http.request({
      url: '/system/modifyDate',
      method: 'post',
      data,
    });
  },

  /**
   * 设置NTP服务器
   * @param {ntpServer}
   * @param {ntpAuto}
   * @return {AxiosPromise}
   */
  setNtpServer: (data: any): AxiosPromise => {
    return http.request({
      url: '/system/ntpServer',
      method: 'post',
      data,
    });
  },

  /**
   * 修改页面超时时间
   * @param {pageTimeOut}
   * @return {AxiosPromise}
   */
  updatePageTimeOut: (data: any): AxiosPromise => {
    return http.request({
      url: '/system/updatePageTimeOut',
      method: 'post',
      data,
    });
  },

  /**
   * 修改系统默认显示语言
   * @param {locale}
   * @return {AxiosPromise}
   */
  updateDisplayLocale: (data: { locale: string }): AxiosPromise => {
    return http.request({
      url: '/system/updateDisplayLocale',
      method: 'post',
      data,
    });
  },

  /* 角色管理 */
  /**
   * 获取角色列表
   * @param {pagenum}
   * @param {pagesize}
   * @param {defineType}
   * @param {keyword}
   * @return {AxiosPromise}
   */
  getRoleList: (data: any): AxiosPromise => {
    return http.request({
      url: '/user/findAllRole',
      method: 'post',
      data,
    });
  },

  /**
   * 新建角色
   * @param {roleName}
   * @param {defineType}
   * @param {description}
   * @return {AxiosPromise}
   */
  createRole: (data: any): AxiosPromise => {
    return http.request({
      url: '/user/createRole',
      method: 'post',
      data,
    });
  },

  /**
   * 编辑角色
   * @param {roleid}
   * @param {roleName}
   * @param {defineType}
   * @param {description}
   * @return {AxiosPromise}
   */
  editRole: (data: any): AxiosPromise => {
    return http.request({
      url: '/user/editRoleById',
      method: 'post',
      data,
    });
  },

  /**
   * 删除角色
   * @param {roleid}
   * @return {AxiosPromise}
   */
  deleteRole: (data: any): AxiosPromise => {
    return http.request({
      url: '/user/delRoleById',
      method: 'post',
      data,
    });
  },

  /**
   * 获取角色权限
   * @param {roleid}
   * @return {AxiosPromise}
   */
  getPermis: (data: any): AxiosPromise => {
    return http.request({
      url: '/user/getPermisByRoleId',
      method: 'post',
      data,
    });
  },

  /**
   * 配置角色权限
   * @param {roleid}
   * @param {permis}
   * @return {AxiosPromise}
   */
  updatePermis: (data: any): AxiosPromise => {
    return http.request({
      url: '/user/updatePermisByRoleId',
      method: 'post',
      data,
    });
  },

  /**
   * 通知接收者
   */
  // 获取全部通知接收者列表
  getAllReceiverList: (data: any): AxiosPromise => {
    return http.request({
      url: '/rcvUser/all',
      method: 'post',
      data,
    });
  },
  // 创建/编辑通知接收者
  saveReceiver (data: any) {
    return http.request({
      url: '/rcvUser/save',
      method: 'post',
      data
    })
  },
  // 创建/编辑通知接收者并绑定通知
  saveReceiverByType (data: any) {
    return http.request({
      url: '/rcvUser/bindByType',
      method: 'post',
      data
    })
  },
  // 解绑接受者的通知
  unbindReceiverByType (data: any) {
    return http.request({
      url: '/rcvUser/unbindByType',
      method: 'post',
      data
    })
  },


  // 操作审计列表
  getOperateAuditList: (data: any) => {
    return http.request({
      url: '/audit/search',
      method: 'post',
      data,
    });
  },
  // 操作审计筛选
  getOperateAuditTags: (data?: any) => {
    return http.request({
      url: '/audit/tags',
      method: 'post',
      data,
    });
  },

  // 获取拓扑设置
  getMidStatus: () => {
    return http.get('/topologySetting/middlewareType/show')
  },
  setMidEnable: () => {
    return http.get('/topologySetting/middlewareType/enable')
  },
  setMidDisabled () {
    return http.get('/topologySetting/middlewareType/disable')
  },
  getMidTypeMetricList (middlewareType: string) {
    return http.get(`/topologySetting/selectedMetrics/list?middlewareType=${middlewareType}`)
  },
  setMidTypeMetric (data: any) {
    return http.post('/topologySetting/selectedMetrics/save', data)
  }
}
