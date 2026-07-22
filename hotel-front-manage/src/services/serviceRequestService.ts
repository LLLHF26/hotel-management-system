import { authFetch } from './request'

export interface ServiceRequest {
  id: number
  customerId: number
  customerName?: string
  roomId?: number
  roomNumber?: string
  type: string
  remark?: string
  status: string
  handlerId?: number
  handleRemark?: string
  handleTime?: string
  createTime?: string
}

export interface ServiceRequestPage {
  total: number
  page: number
  size: number
  records: ServiceRequest[]
}

/** 管理侧：服务呼叫分页列表 */
export async function getServiceRequests(params: { status?: string; page?: number; size?: number } = {}) {
  try {
    const q = new URLSearchParams()
    if (params.status) q.set('status', params.status)
    if (params.page) q.set('page', String(params.page))
    if (params.size) q.set('size', String(params.size))
    const res = await authFetch(`/api/order/service-request/admin?${q.toString()}`)
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || '获取服务呼叫失败')
    return json
  } catch (e: any) {
    return { code: 200, msg: 'mock', data: { total: 0, page: 1, size: 10, records: [] } }
  }
}

/** 管理侧：处理 / 取消服务呼叫 */
export async function handleServiceRequest(id: number, status: string, handleRemark?: string) {
  const q = new URLSearchParams()
  q.set('status', status)
  if (handleRemark) q.set('handleRemark', handleRemark)
  const res = await authFetch(`/api/order/service-request/${id}/handle?${q.toString()}`, { method: 'PUT' })
  const json = await res.json()
  if (json?.code !== 200) throw new Error(json?.msg || '处理失败')
  return json
}
