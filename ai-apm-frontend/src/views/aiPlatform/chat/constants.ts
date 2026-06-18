export const MAX_UPLOAD_FILE_SIZE = 5 * 1024 * 1024
export const MAX_UPLOAD_FILE_SIZE_MB = 5

export const ALLOWED_FILE_EXTENSIONS = [
  '.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx',
  '.md', '.txt', '.json', '.xml', '.yaml', '.yml', '.csv', '.log',
] as const

export const ALLOWED_FILE_ACCEPT = ALLOWED_FILE_EXTENSIONS.join(',')

export interface QuickExpertOption {
  expertId: string
  icon: string
}

export const QUICK_EXPERTS: QuickExpertOption[] = [
  { expertId: 'data', icon: 'el-icon-data-analysis' },
  { expertId: 'inspection', icon: 'el-icon-view' },
]

/** 聊天页默认路由的数字专家（AI 大脑） */
export const DEFAULT_EXPERT_ID = 'brain'

/** 内置专家展示名 i18n key（后端 name 为中文，需前端翻译） */
export const EXPERT_NAME_KEYS: Record<string, string> = {
  brain: 'modules.views.aiPlatform.chat.s_16ffc5ac',
  data: 'modules.views.aiPlatform.chat.s_6e8ecff0',
  inspection: 'modules.views.aiPlatform.chat.s_4dabf8a7',
}
