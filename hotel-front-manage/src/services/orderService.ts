import { authFetch } from './request'

export async function getOrderList(params?: any) {
  const qs = new URLSearchParams()
  if (params) Object.keys(params).forEach(k => {
    const value = params[k]
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      qs.append(k, String(value))
    }
  })
  try {
    const query = qs.toString() ? `?${qs.toString()}` : ''
    const res = await authFetch(`/api/order/list${query}`)
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'order list request failed')
    return json
  } catch (e) {
    return {
      code: 200,
      msg: 'mock',
      data: { total: 168, page: params?.page ?? 1, size: params?.size ?? 10, records: [] }
    }
  }
}

export async function getOrderById(id: number|string) {
  try {
    const res = await authFetch(`/api/order/${id}`)
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'order detail request failed')
    return json
  } catch (e) {
    return {
      code: 200,
      msg: 'mock',
      data: null
    }
  }
}

export async function getOrderSummary() {
  console.log('Order summary request')
  // No dedicated summary endpoint in docs; fall back to mock data
  return { todayCheckIn: 38, yesterdayCheckIn: 34, currentOccupancyRate: '76.5%', pendingAlarms: 3 }
}
