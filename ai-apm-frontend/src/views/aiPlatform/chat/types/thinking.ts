import type { AiChatMessage } from '@/api/aiPlatform';

export type ToolStatus = 'running' | 'success' | 'failed' | 'view';

export interface ToolCallItem {
  id: string;
  callId?: string;
  sourceType?: string;
  name: string;
  status: ToolStatus;
  durationMs?: number;
  sourceMessage?: AiChatMessage;
  callMessage?: AiChatMessage;
  resultMessage?: AiChatMessage;
}

export interface ThinkingDetailItem {
  type: 'text' | 'tool';
  content?: string;
  messageId?: string;
  messageType?: string;
  expertId?: string;
  taskId?: string;
  deliverable?: boolean;
  tool?: ToolCallItem;
}
