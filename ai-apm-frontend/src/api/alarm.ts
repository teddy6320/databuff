import http from '@/utils/axios';

export default {
  /*
    告警列表
   */
  getAlarmParams: (data: any) => {
    return http.request({
      url: '/alarm/queryParams',
      method: 'post',
      data
    })
  },
  getAlarmTrend: (data: any) => {
    return http.request({
      url: '/alarm/trend',
      method: 'post',
      data
    })
  },
  getAlarmListV2: (data: any): any => {
    return http.request({
      url: '/monitor/findMonitorEventV2',
      method: 'post',
      data
    })
  },
  getEventTrendV2: (params: any): any => {
    return http.request({
      url: `/alarm/detail/${params.alarmId}/trendMap/${params.interval}`,
      method: 'get',
    })
  },
  getAlarmListNew: (data: any): any => {
    return http.request({
      url: '/alarm/list',
      method: 'post',
      data,
      transformResponse: [(resData) => {
        try {
          const _resData = typeof resData === 'string' ? JSON.parse(resData) : resData;
          (((_resData || {}).data || {}).list || []).forEach((item: any) => {
            if ((item.description || '').length > 1500) {
              item._description = item.description
              item.description = item.description.substring(0, 1500) + '...';
            }
          });
          return { ..._resData }
        } catch (err) {
          return resData
        }
      }]
    })
  },
  getAlarmDetail (data: any) {
    return http.request({
      url: '/alarm/detail/' + data.id,
      method: 'get',
    })
  },
  getAlarmCount (data: any) {
    return http.request({
      url: '/alarm/count',
      method: 'post',
      data,
    })
  },


  /*
    响应策略
   */
  // 获取响应策略列表
  getResponseList (data: any) {
    return http.request({
      url: '/respPolicy/list',
      method: 'post',
      data
    })
  },
  // 获取响应策略详情
  getResponseDetail (data: any) {
    return http.request({
      url: '/respPolicy/find',
      method: 'post',
      data
    })
  },
  // 创建/编辑响应策略
  saveResponse (data: any) {
    return http.request({
      url: '/respPolicy/save',
      method: 'post',
      data
    })
  },
  // 删除响应策略
  deleteResponse (data: any) {
    return http.request({
      url: '/respPolicy/delete',
      method: 'post',
      data
    })
  },
  // 启用/停用响应策略
  toggleResponseEnable (data: any) {
    return http.request({
      url: '/respPolicy/publish',
      method: 'post',
      data
    })
  },
  // 导出响应策略
  exportResponse (data: any) {
    return http.request({
      url: '/respPolicy/export',
      method: 'post',
      responseType: 'blob',
      data
    })
  },


  /*
    通知记录
   */
  // 获取通知记录列表
  getNoticeRecordList (data: any) {
    return http.request({
      url: '/notify/records',
      method: 'post',
      data
    })
  },
  // 重新发送通知
  resendNotice (data: any) {
    return http.request({
      url: '/notify/resend',
      method: 'post',
      data
    })
  },

  // 获取系统事件列表
  getSystemEventList: (data: any) => {
    return http.request({
      url: '/eventSystem/findEventsByMultipleConditions',
      method: 'post',
      data
    })
  },
  // 获取事件详情的趋势图
  getEventDetailChartTrend (data: any) {
    return http.request({
      url: '/monitor/getEventChartMap',
      method: 'post',
      data
    })
  },
  // 获取系统事件详情的趋势图
  getSystemEventDetailChartTrend (data: any) {
    return http.request({
      url: '/monitor/system/getEventChartMap',
      method: 'post',
      data
    })
  },
  // 获取事件详情
  getEventDetailV2: (data: any): any => {
    return http.request({
      url: '/monitor/findEventDetailV2',
      method: 'post',
      data
    })
  },
  // 获取系统事件详情
  getSystemEventDetail: (params: any) => {
    return http.request({
      url: `/eventSystem/findEventById/${params.eventId}`,
      method: 'get',
    })
  },

  /*
    DeepSeek 根因分析
   */
  startAiRootAnalyse (data: any) {
    return http.request({
      url: '/webapi/api/ai/rootAnalyse',
      method: 'post',
      data
    })
  },
  getAiResult (data: any) {
    return http.request({
      url: '/webapi/api/ai/fetchResult',
      method: 'post',
      data,
      transformResponse: [(resData) => {
        try {
          const _resData = typeof resData === 'string' ? JSON.parse(resData) : resData;
          _resData.status = _resData.status || 200
          _resData.message = _resData.message || 'success'
          return { ..._resData }
        } catch (err) {
          return resData
        }
      }]
    })
  },
  retrySuggest (params: any) {
    return http.request({
      url: '/webapi/api/ai/retrySuggest',
      method: 'get',
      params,
    })
  },
}
