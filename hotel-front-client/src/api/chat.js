import { get, post } from '../utils/request'

export function chatSync(message, sessionId, history) {
  return post('/api/ai/chat/sync', { message, session_id: sessionId, history }, true)
}

export function getChatHistory(sessionId) {
  return get('/api/ai/chat/history/' + sessionId, {}, true)
}

export function getChatSessions(page = 1, size = 20) {
  return get('/api/ai/chat/sessions', { page, size }, true)
}

export function submitFeedback(sessionId, messageIndex, feedback) {
  return post('/api/ai/chat/feedback', { session_id: sessionId, message_index: messageIndex, feedback }, true)
}
