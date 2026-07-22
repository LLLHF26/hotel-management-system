import { request } from './request'

// ===== 服务呼叫（客户呼叫人员） =====

export interface ServiceRequestItem {
  id: number
  customerId: number
  customerName?: string
  roomId?: number
  roomNumber?: string
  type: string          // 打扫 / 送物 / 维修 / 其他
  remark?: string
  status: string        // 待处理 / 已处理 / 已取消
  handlerId?: number
  handleRemark?: string
  handleTime?: string
  createTime?: string
}

export interface ServiceRequestPage {
  total: number
  page: number
  size: number
  records: ServiceRequestItem[]
}

/** 获取待处理服务呼叫（前台用，默认只拉待处理） */
export function getPendingServiceRequests(params?: { status?: string; page?: number; size?: number }) {
  return request<ServiceRequestPage>({
    url: '/api/order/service-request/admin',
    method: 'GET',
    params: params ?? { status: '待处理', page: 1, size: 50 },
  })
}

/** 安排人员处理服务呼叫 */
export function dispatchServiceRequest(
  id: number,
  status: string,
  handleRemark?: string,
) {
  const qs = new URLSearchParams()
  qs.set('status', status)
  if (handleRemark) qs.set('handleRemark', handleRemark)
  return request({
    url: `/api/order/service-request/${id}/handle?${qs.toString()}`,
    method: 'PUT',
  })
}

// ===== 商品订单（客房商品下单记录） =====

export interface ProductOrderItem {
  id: number
  orderId: number
  itemName: string
  amount: number | string
  quantity: number
  operatorId?: number
  createTime?: string
  // 冗余：订单号、房间号、客户名
  orderNo?: string
  roomNumber?: string
  customerName?: string
}

/** 获取最近的商品订单（需要配送的）—— order_extra 跨订单查询 */
export function getRecentProductOrders(params?: { page?: number; size?: number }) {
  return request<{ total: number; records: ProductOrderItem[] }>({
    url: '/api/order/product-orders/recent',
    method: 'GET',
    params: params ?? { page: 1, size: 20 },
  })
}

// ===== 员工列表（用于安排人员） =====

export interface EmployeeItem {
  id: number
  name: string
  username: string
  role?: string
  phone?: string
  status?: string
}

/** 获取可用员工列表（后台 /api/user/list，role 留空返回全部员工） */
export function getEmployeeList(params?: { page?: number; size?: number }) {
  return request<{ total: number; records: any[] }>({
    url: '/api/user/list',
    method: 'GET',
    params: params ?? { page: 1, size: 100 },
  })
}
