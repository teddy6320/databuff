import { remove } from 'lodash';
import http from '../utils/axios';
import { AxiosPromise } from 'axios';

export const loginHandle = (data: any): AxiosPromise => {
  return http.request({
    url: '/user/login',
    method: 'post',
    data,
  });
}

export const getLicenseInfo = (): AxiosPromise => {
  return http.request({
    url: '/user/licenseList',
    method: 'post',
  });
}

export const logoutHandle = (): AxiosPromise => {
  return http.request({
    url: '/user/loginOut',
    method: 'post',
    transformResponse: [() => {
      if (window.localStorage) {
        // 清除高级配置页面的解锁状态
        window.localStorage.removeItem('DATABUFF_ADVANCED_UNLOCKING')
      }
    }]
  });
}

export default {
  /**
   * 获取用户菜单权限
   * @param {null}
   * @return {AxiosPromise}
   */
  getMenus: (): AxiosPromise => {
    return http.request({
      url: '/user/getMenuByAccount',
      method: 'post',
    });
  },

  /**
   * 登录
   * @param {account}
   * @param {password}
   * @return {AxiosPromise}
   */
  loginHandle,
  /**
   * 登出
   * @param {null} 无
   * @return {AxiosPromise} promise
   */
  logoutHandle,

  /**
   * 免登录获取平台版本号
   */
  getProductVersion: (): AxiosPromise => {
    return http.request({
      url: '/user/product/version',
      method: 'get',
    });
  },

  /**
   * 获取用户信息
   */
  getUserInfo: (): AxiosPromise => {
    return http.request({
      url: '/user/getUserInfo',
      method: 'get',
    });
  },

  /**
   * 更新用户密码
   */
  updatePwdInfo: (data: any): AxiosPromise => {
    return http.request({
      url: `/user/updateUserPass`,
      method: 'post',
      data,
    });
  },

  /**
   * 查询license信息
   */
  getLicenseInfo,

  /**
   * 查询license序列号
   */
  getLicenseSerialnum: (): AxiosPromise => {
    return http.request({
      url: '/user/getLicenseSerialnum',
      method: 'get',
    });
  },

  /**
   * 查询agent下载版本
   */
   getDownloadVersion: (): AxiosPromise => {
    return http.request({
      url: '/api6972/getVersions',
      method: 'get',
    });
  },
  /**
   * 查询k8s agent下载版本
   */
   getK8sDownloadVersion: (): AxiosPromise => {
    return http.request({
      url: '/api6972/getK8sVersions',
      method: 'get',
    });
  },
  /**
   * 查询授权语言
   */
  getAuthLangs: (): AxiosPromise => {
    return http.request({
      url: '/user/getAuthLangs',
      method: 'get',
    });
  },
  addOrg (data: { organizeName: string, managerAccount: string, description: string }) {
    return http.post('/organize/add', data)
  },
  editOrg (data: { id: number, name: string, managerId: number, description: string }) {
    return http.post('/organize/edit', data)
  },
  getOrgList () {
    return http.get('/organize/list')
  },
  deleteOrg (id: any) {
    return http.request({
      url: `/organize/delete`,
      method: 'post',
      data: { id }
    })
  },
  getMemberList (data: { organizeId: number, page: number, pageSize: number, keyword?: string }) {
    return http.post('/organize/listMember', data)
  },
  joinUser (data: { organizeId: number, userIds: number[] }) {
    return http.post('/organize/joinUser', data)
  },
  removeUser (data: { organizeId: number, userId: number }) {
    return http.post('/organize/removeUser', data)
  },
  addManager (params: { organizeId: number, userId: number }) {
    return http.get('/organize/addManager', { params })
  },
  removeManager (params: { organizeId: number, userId: number }) {
    return http.get('/organize/removeManager', { params })
  },
  getOrgOptions () {
    return http.get('/organize/options')
  },
  toggleOrgAuth (data: any) {
    return http.post('/meta/config/updateEnable', { enabled: data?.enabled, code: 'ORG_MANAGE_AUTH_CONFIG' })
  },
  getOrgStatus () {
    return http.get('/meta/config/getByCode?code=ORG_MANAGE_AUTH_CONFIG')
  },
  getAllAccountWithoutOrg () {
    return http.post('/user/getAllAccounts')
  },


  /**
   * 单点登录
   */
   singleAuthLogin: (data: any) => {
    return http.request({
      url: '/singleLogin/imc/authlogin',
      method: 'post',
      data,
    });
  },
}
