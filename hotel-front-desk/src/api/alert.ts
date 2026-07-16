import { aiRequest } from './request'
import type { AlertStatusVO } from '@/types'

export function getAlertStatus() {
  return aiRequest<AlertStatusVO>({
    url: '/api/ai/alert/status',
    method: 'GET',
  })
}
