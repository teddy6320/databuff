import type { AiChatMessage } from '@/api/aiPlatform';

export const isExpertDeliverableText = (item: AiChatMessage): boolean => {
  const type = String(item.messageType || '').toUpperCase();
  if (type !== 'TEXT') {
    return false;
  }
  return Boolean(item.metadata?.isExpertDeliverable);
};

export const isRoundFinalText = (item: AiChatMessage): boolean => {
  const type = String(item.messageType || '').toUpperCase();
  if (item.role !== 'assistant' || type !== 'TEXT') {
    return false;
  }
  if (isExpertDeliverableText(item)) {
    return false;
  }
  if (item.metadata?.isRoundFinal === false) {
    return false;
  }
  return true;
};

export const isProcessMessageType = (item: AiChatMessage): boolean => {
  const type = String(item.messageType || '').toUpperCase();
  if (isExpertDeliverableText(item)) {
    return true;
  }
  return ['REASONING', 'TOOL_CALL', 'TOOL_RESULT', 'SUBTASK', 'ERROR', 'TOOL', 'EVENT'].includes(type);
};

/** Non-process assistant TEXT already visible for a round (e.g. brain answer still streaming). */
export const hasVisibleAssistantAnswerForRound = (
  messages: AiChatMessage[],
  roundIndex: number,
): boolean => (messages || []).some(item =>
  item.role === 'assistant'
  && Number(item.roundIndex || 0) === roundIndex
  && !isProcessMessageType(item)
  && String(item.content || '').trim(),
);

export const allUserRoundsHaveFinalAnswer = (messages: AiChatMessage[]): boolean => {
  const userRounds = [...new Set(
    (messages || [])
      .filter(item => item.role === 'user')
      .map(item => Number(item.roundIndex || 0))
      .filter(round => round > 0),
  )];
  if (!userRounds.length) {
    return false;
  }
  return userRounds.every(round => (messages || []).some(item =>
    isRoundFinalText(item) && Number(item.roundIndex || 0) === round,
  ));
};
