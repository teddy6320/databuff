import http from '../utils/axios'
import { AxiosPromise } from 'axios'
import * as NoticeTypes from './notice.type'
export default {
  /*
    邮件通知
  */
  // 获取邮件通知设置
  getEmailConfig: (): AxiosPromise => {
    return http.request({
      url: '/notify/getEmailConfig',
      method: 'get',
    })
  },

  // 设置邮件通知配置
  setEmailConfig: (data: NoticeTypes.EmailConfig): AxiosPromise => {
    return http.request({
      url: '/notify/setEmailConfig',
      method: 'post',
      data,
    })
  },

  // 测试邮件通知
  testEmail: (data: NoticeTypes.TestEmail): AxiosPromise => {
    return http.request({
      url: '/notify/testEmail',
      method: 'post',
      data,
    })
  },


  /*
    短信通知
  */
  // 获取短信通知设置
  getSmsConfig: (): AxiosPromise => {
    return http.request({
      url: '/notify/getSmsConfig',
      method: 'get',
    })
  },

  // 设置短信通知配置
  setSmsConfig: (data: NoticeTypes.SmsConfig): AxiosPromise => {
    return http.request({
      url: '/notify/setSmsConfig',
      method: 'post',
      data,
    })
  },

  // 测试短信通知
  testSms: (data: NoticeTypes.TestSms): AxiosPromise => {
    return http.request({
      url: '/notify/testSms',
      method: 'post',
      data,
    })
  },


  /*
    钉钉通知
  */
  // 获取钉钉通知设置
  getDingTalkConfig: (): AxiosPromise => {
    return http.request({
      url: '/notify/getDingTalkConfig',
      method: 'get',
    })
  },

  // 设置钉钉通知配置
  setDingTalkConfig: (data: NoticeTypes.DingTalkConfig): AxiosPromise => {
    return http.request({
      url: '/notify/setDingTalkConfig',
      method: 'post',
      data,
    })
  },

  // 测试钉钉通知
  testDingTalk: (data?: any): AxiosPromise => {
    return http.request({
      url: '/notify/testDingTalk',
      method: 'post',
      data
    })
  },
  // 测试钉钉通知
  testDingTalkByPhone: (data: any): AxiosPromise => {
    return http.request({
      url: '/notify/testDingTalkByPhone',
      method: 'post',
      data
    })
  },
  // 测试钉钉通知
  testWechatByPhone: (data: any): AxiosPromise => {
    return http.request({
      url: '/notify/testWeChatByPhone',
      method: 'post',
      data
    })
  },

  /*
    企业微信通知
  */
  // 获取企业微信通知设置
  getWeChatConfig: (): AxiosPromise => {
    return http.request({
      url: '/notify/getWeChatConfig',
      method: 'get',
    })
  },

  // 设置企业微信通知配置
  setWeChatConfig: (data: NoticeTypes.WeChatConfig): AxiosPromise => {
    return http.request({
      url: '/notify/setWeChatConfig',
      method: 'post',
      data,
    })
  },

  // 测试企业微信通知
  testWeChat: (data?: any): AxiosPromise => {
    return http.request({
      url: '/notify/testWeChat',
      method: 'post',
      data
    })
  },


  /*
    Webhook通知
  */
  // 测试Webhook通知
  testCustomWebhook: (data: NoticeTypes.CustomWebhookConfig): AxiosPromise => {
    return http.request({
      url: '/notify/testCustomWebhook',
      method: 'post',
      data,
    })
  },


  /*
    Socket通知
  */
  // 获取Socket通知设置
  getSocketConfig: () => {
    return http.request({
      url: '/notify/getSocketConfig',
      method: 'get',
    })
  },

  // 设置Socket通知配置
  setSocketConfig: (data: any) => {
    return http.request({
      url: '/notify/setSocketConfig',
      method: 'post',
      data,
    })
  },

  // 测试Socket通知
  testSocket: (data?: any) => {
    return http.request({
      url: '/notify/testSocket',
      method: 'post',
      data,
    })
  },
}
