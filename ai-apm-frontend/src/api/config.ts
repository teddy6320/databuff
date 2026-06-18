import http from '@/utils/axios';

export default {
  /*
    高级配置
   */
  // 获取配置分类
  getConfigTypes () {
    return http.request({
      url: '/dConfigManage/getChildrenPaths',
      method: 'get',
    })
  },
  // 根据分类获取配置列表
  getConfigList (type: string) {
    return http.request({
      url: `/dConfigManage/getPathData?path=${type}`,
      method: 'get',
    })
  },
  // 保存配置
  saveConfig (data: any) {
    return http.request({
      url: `/dConfigManage/saveNode`,
      method: 'POST',
      data,
    })
  },
  // 删除配置
  deleteConfig (path: string) {
    return http.request({
      url: `/dConfigManage/deleteNode?path=${path}`,
      method: 'delete',
    })
  },

  // 全局配置保存
  saveGlobalConfig (data: any[]) {
    return http.request({
      url: '/serviceConfig/global/saveNode',
      method: 'POST',
      data,
    })
  },
  // 全局配置重置为默认
  resetGlobalConfig () {
    return http.request({
      url: '/serviceConfig/global/reset',
      method: 'delete',
    })
  },
  // 应用配置保存
  saveServiceConfig (data: any) {
    return http.request({
      url: '/serviceConfig/service/saveNode',
      method: 'POST',
      data,
    })
  },
  // 应用配置重置为默认
  resetServiceConfig (path: string) {
    return http.request({
      url: `/serviceConfig/service/reset?path=${path}`,
      method: 'delete',
    })
  },

  // 获取AI配置
  getAIConfig: () => http.get('/api/ai/getAIConfig'),
  // 更新AI配置
  updateAIConfig: (data: any) => http.post('/api/ai/updateAIConfig', data),
  // 测试AI配置
  testAIConfig: (data: any) => http.post('/api/ai/testAIConfig', data),

  /*
    平台Logo、名称、版权等配置
   */
  // 获取Logo配置
  getLogoConfig: () => http.get('/system/logoConfig'),
  // 保存Logo配置
  saveLogoConfig: (data: any) => http.post('/system/saveLogoConfig', data),
  // 重置Logo配置
  resetLogoConfig: () => http.post('/system/resetLogoConfig'),
}
