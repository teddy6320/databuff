import type { AiChatMessage } from '@/api/aiPlatform';
import type { ThinkingDetailItem } from '../types/thinking';
import { isProcessMessageType } from './message-flags';

export interface ExpertProcessSegment {
  expertId: string;
  taskId?: string;
  items: AiChatMessage[];
}

export interface ThinkingExpertSegment {
  expertId: string;
  items: ThinkingDetailItem[];
}

const normalizeExpertId = (expertId?: string): string =>
  String(expertId || 'brain').trim() || 'brain';

const resolveProcessExpertId = (item: AiChatMessage): string =>
  normalizeExpertId(
    String(item.expertId || item.metadata?.expertId || item.metadata?.sourceExpertId || '').trim() || undefined,
  );

export const buildExpertProcessSegments = (items: AiChatMessage[]): ExpertProcessSegment[] => {
  const segments: ExpertProcessSegment[] = [];
  (items || []).forEach((item) => {
    if (!isProcessMessageType(item)) {
      return;
    }
    const expertId = resolveProcessExpertId(item);
    const taskId = item.metadata?.taskId ? String(item.metadata.taskId) : undefined;
    const last = segments[segments.length - 1];
    if (last && last.expertId === expertId) {
      last.items.push(item);
      return;
    }
    segments.push({ expertId, taskId, items: [item] });
  });
  return segments;
};

export const buildThinkingExpertSegments = (items: ThinkingDetailItem[]): ThinkingExpertSegment[] => {
  const segments: ThinkingExpertSegment[] = [];
  (items || []).forEach((item) => {
    const expertId = normalizeExpertId(item.expertId);
    const last = segments[segments.length - 1];
    if (last && last.expertId === expertId) {
      last.items.push(item);
      return;
    }
    segments.push({ expertId, items: [item] });
  });
  return segments;
};
