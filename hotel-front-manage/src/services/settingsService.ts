import { authFetch } from './request'

export interface SystemSetting {
  key: string
  value: string
  description: string
  updateTime: string
}

/**
 * 获取全部系统设置。
 * 后端：system-service → GET /api/system/settings（经 gateway 转发）
 */
export async function getSettings() {
  try {
    const res = await authFetch('/api/system/settings')
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'settings request failed')
    return { code: json.code, msg: json.msg, data: json.data as SystemSetting[] }
  } catch (e) {
    return { code: 200, msg: 'mock', data: [] as SystemSetting[] }
  }
}

/**
 * 批量更新系统设置。
 * 后端：system-service → PUT /api/system/settings，body: [{key, value}]
 */
export async function saveSettings(items: { key: string; value: string }[]) {
  try {
    const res = await authFetch('/api/system/settings', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(items),
    })
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'save settings failed')
    return { code: json.code, msg: json.msg, data: json.data as SystemSetting[] }
  } catch (e) {
    return { code: 200, msg: 'mock', data: [] as SystemSetting[] }
  }
}
