export interface EmailConfig {
  id?: number;
  apiKey: string;
  notifyType: string;
  enable: number;
  mailHost: string;
  mailSender: string;
  mailSenderPwd: string;
  mailSecretCode: string;
  mailPort: number;
  mailSsl: number;
}

export interface TestEmail {
  toEmails: string;
}

export interface SmsConfig {
  id?: number;
  apiKey: string;
  notifyType: string;
  enable: number;
  smsKeyId: string;
  smsKeySecret: string;
  templates: Array<{
    id: number;
    apiKey: string;
    notifyConfigId: number;
    smsNotifyType: string;
    smsTemplateId: string;
    smsTemplateContent: string;
    smsSignName: string;
  }>;
}

export interface TestSms {
  phones: string;
}

export interface DingTalkConfig {
  id?: number;
  apiKey: string;
  notifyType: string;
  enable: number;
  dingWebhook: string;
  dingSecret: string;
}

export interface WeChatConfig {
  id?: number;
  apiKey: string;
  notifyType: string;
  enable: number;
  wechatWebhook: string;
}

export interface CustomWebhookConfig {
  webhookUrl: string;
  webhookMethod: string;
  webhookHeader: string;
}
