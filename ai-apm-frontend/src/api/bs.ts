import http from '../utils/axios'

/** Business system APIs used by portal pages. */
export default {
  getNamespaceList: (data: any) => {
    return http.request({
      url: '/service/k8sNamespaceList',
      method: 'post',
      data
    })
  },
}
