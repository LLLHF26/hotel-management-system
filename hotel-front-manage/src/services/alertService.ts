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
  try {
    const res = await authFetch('/api/ai/alert/status')
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'alert list request failed')
    const data = json.data
    if (data && Array.isArray(data.alerts)) {
      return { code: json.code, msg: json.msg, data: data.alerts.map(mapAlert) }
    }
    return { code: json.code, msg: json.msg, data: Array.isArray(data) ? data.map(mapAlert) : [] }
  } catch (e) {
    return { code: 200, msg: 'mock', data: [] }
  }
}

export async function markAlertAsRead(alertId: number) {
  const res = await authFetch(`/api/ai/alert/${alertId}/read`, { method: 'PUT' })
  return await res.json()
}

export async function markAllAlertsAsRead() {
  const res = await authFetch('/api/ai/alert/read-all', { method: 'PUT' })
  return await res.json()
}

export async function deleteAlert(alertId: number) {
  const res = await authFetch(`/api/ai/alert/${alertId}`, { method: 'DELETE' })
  return await res.json()
}

export async function batchMarkAlertsAsRead(alertIds: number[]) {
  const res = await authFetch('/api/ai/alert/read-batch', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ids: alertIds }),
  })
  return await res.json()
}

export async function batchDeleteAlerts(alertIds: number[]) {
  const res = await authFetch('/api/ai/alert/batch', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ids: alertIds }),
  })
  return await res.json()
}

export async function createAlertRule(rule: { type: string; threshold: number; is_enabled: boolean }) {
  const res = await authFetch('/api/ai/alert/rule', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(rule),
  })
  return await res.json()
}

export async function updateAlertRule(ruleId: number, rule: { threshold?: number; is_enabled?: boolean }) {
  const res = await authFetch(`/api/ai/alert/rule/${ruleId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(rule),
  })
  return await res.json()
}

export async function deleteAlertRule(ruleId: number) {
  const res = await authFetch(`/api/ai/alert/rule/${ruleId}`, { method: 'DELETE' })
  return await res.json()
}

export async function getAlertRules() {
  try {
    const res = await authFetch('/api/ai/alert/rules')
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'alert rules request failed')
    const data = json.data
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
  } catch (e) {
    return { code: 200, msg: 'mock', data: [] }
  }
}
