import type { AiChatMessage } from '@/api/aiPlatform';
import type { ThinkingDetailItem, ToolCallItem, ToolStatus } from '../types/thinking';

const resolveCallId = (item: AiChatMessage): string =>
  String(item.metadata?.toolCallId || item.metadata?.callId || item.messageId || '').trim();

const resolveMessageExpertId = (item?: AiChatMessage | null): string | undefined => {
  const expertId = String(
    item?.expertId
    || item?.metadata?.expertId
    || item?.metadata?.sourceExpertId
    || '',
  ).trim();
  return expertId || undefined;
};

const resolveMessageTimestamp = (item?: AiChatMessage | null): number => {
  if (!item) {
    return 0;
  }
  const raw = item.ts || item.timestamp || item.updatedAt;
  if (!raw) {
    return 0;
  }
  const value = new Date(raw).getTime();
  return Number.isFinite(value) && value > 0 ? value : 0;
};

const resolveToolDurationMs = (
  callMessage: AiChatMessage | null,
  resultMessage: AiChatMessage | null,
): number | undefined => {
  const metadataDuration = Number(
    resultMessage?.metadata?.durationMs
    || callMessage?.metadata?.durationMs
    || 0,
  );
  if (metadataDuration > 0) {
    return metadataDuration;
  }
  const startTs = resolveMessageTimestamp(callMessage);
  const endTs = resolveMessageTimestamp(resultMessage);
  if (startTs > 0 && endTs >= startTs) {
    return endTs - startTs;
  }
  return undefined;
};

export const formatToolDurationText = (durationMs?: number): string => {
  const normalized = Number(durationMs || 0);
  if (normalized <= 0) {
    return '';
  }
  if (normalized < 1000) {
    return `${normalized} ms`;
  }
  const seconds = normalized / 1000;
  if (seconds < 10) {
    return `${seconds.toFixed(1)}s`;
  }
  return `${Math.ceil(seconds)}s`;
};

const isFailedToolState = (value: string): boolean => {
  const normalized = value.toUpperCase();
  return normalized === 'FAILED'
    || normalized === 'ERROR'
    || normalized === 'FAILURE'
    || normalized === 'TIMEOUT'
    || normalized === 'CANCELLED';
};

const resolveToolStatus = (item: AiChatMessage): ToolStatus => {
  const toolResultState = String(item.metadata?.toolResultState || '').trim();
  if (isFailedToolState(toolResultState)) {
    return 'failed';
  }
  const messageStatus = String(item.messageStatus || '').toUpperCase();
  if (isFailedToolState(messageStatus)) {
    return 'failed';
  }
  return 'success';
};

const buildToolDetailItem = (
  callMessage: AiChatMessage | null,
  resultMessage: AiChatMessage | null,
  status: ToolStatus,
): ThinkingDetailItem => {
  const sourceMessage = resultMessage || callMessage;
  const metadata = sourceMessage?.metadata || {};
  const toolName = String(
    metadata.toolName
    || callMessage?.metadata?.toolName
    || resultMessage?.metadata?.toolName
    || 'tool',
  ).trim() || 'tool';
  const callId = resolveCallId(callMessage || resultMessage || { metadata: {} } as AiChatMessage);
  const tool: ToolCallItem = {
    id: callId || sourceMessage?.messageId || toolName,
    callId,
    sourceType: String(sourceMessage?.messageType || '').toUpperCase(),
    name: toolName,
    status,
    durationMs: resolveToolDurationMs(callMessage, resultMessage),
    sourceMessage: sourceMessage || undefined,
    callMessage: callMessage || undefined,
    resultMessage: resultMessage || undefined,
  };
  const expertId = resolveMessageExpertId(sourceMessage)
    || resolveMessageExpertId(callMessage)
    || resolveMessageExpertId(resultMessage);
  const taskId = sourceMessage?.metadata?.taskId
    ? String(sourceMessage.metadata.taskId)
    : undefined;
  return {
    type: 'tool',
    messageId: sourceMessage?.messageId,
    expertId,
    taskId,
    tool,
  };
};

export const buildThinkingDetailItems = (items: AiChatMessage[]): ThinkingDetailItem[] => {
  const result: ThinkingDetailItem[] = [];
  const toolIndexByCallId = new Map<string, number>();

  (items || []).forEach((item) => {
    const type = String(item.messageType || '').toUpperCase();
    if (type === 'REASONING' || type === 'SUBTASK' || type === 'ERROR' || (type === 'TEXT' && item.metadata?.isExpertDeliverable)) {
      const content = String(item.content || '').trim();
      if (!content) {
        return;
      }
      result.push({
        type: 'text',
        content,
        messageId: item.messageId,
        messageType: type,
        expertId: resolveMessageExpertId(item),
        taskId: item.metadata?.taskId ? String(item.metadata.taskId) : undefined,
        deliverable: type === 'TEXT',
      });
      return;
    }
    if (type === 'TOOL_CALL') {
      const callId = resolveCallId(item);
      const detail = buildToolDetailItem(item, null, 'running');
      if (callId) {
        toolIndexByCallId.set(callId, result.length);
      }
      result.push(detail);
      return;
    }
    if (type === 'TOOL_RESULT') {
      const callId = resolveCallId(item);
      const status = resolveToolStatus(item);
      const existingIndex = callId ? toolIndexByCallId.get(callId) : undefined;
      if (existingIndex !== undefined) {
        const existing = result[existingIndex];
        const callMessage = existing?.tool?.callMessage || null;
        result[existingIndex] = buildToolDetailItem(callMessage, item, status);
        return;
      }
      if (callId) {
        toolIndexByCallId.set(callId, result.length);
      }
      result.push(buildToolDetailItem(null, item, status));
    }
  });
  return result;
};
