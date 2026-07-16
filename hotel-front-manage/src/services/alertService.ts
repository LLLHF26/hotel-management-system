import { authFetch } from './request'

function mapAlert(a: any) {
  return {
    id: a.id,
    type: a.type,
    level: a.level,
    content: a.content,
    time: a.trigger_time || a.time,
    unread: a.is_read !== undefined ? !a.is_read : a.unread,
  }
}

export async function getAlertList() {
  const res = await authFetch('/api/ai/alert/status')
  const json = await res.json()
  const data = json?.data
  if (data && Array.isArray(data.alerts)) {
    return { code: json.code, msg: json.msg, data: data.alerts.map(mapAlert) }
  }
  return { code: json.code, msg: json.msg, data: Array.isArray(data) ? data.map(mapAlert) : [] }
}

export async function markAlertAsRead(alertId: number) {
  const res = await authFetch(`/api/ai/alert/${alertId}/read`, { method: 'PUT' })
  return await res.json()
}

export async function markAllAlertsAsRead() {
  const res = await authFetch('/api/ai/alert/read-all', { method: 'PUT' })
  return await res.json()
}

export async function getAlertRules() {
  const res = await authFetch('/api/ai/alert/rules')
  const json = await res.json()
  const data = json?.data
  if (Array.isArray(data)) {
    return { code: json.code, msg: json.msg, data: data.map((r: any) => ({
      id: r.id,
      name: r.type,
      threshold: String(r.threshold),
      status: r.is_enabled ? '启用' : '停用',
      description: '',
    }))}
  }
  return json
}
