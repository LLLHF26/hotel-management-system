import { aiRequest } from './request'
import type { AlertStatusVO } from '@/types'

export function getAlertStatus() {
  return aiRequest<AlertStatusVO>({
    url: '/api/ai/alert/status',
    method: 'GET',
  })
}

export function markAlertAsRead(alertId: number) {
  return aiRequest<void>({
    url: `/api/ai/alert/${alertId}/read`,
    method: 'PUT',
  })
}

export function markAllAlertsAsRead() {
  return aiRequest<void>({
    url: '/api/ai/alert/read-all',
    method: 'PUT',
  })
}
