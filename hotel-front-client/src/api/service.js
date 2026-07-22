import { get, post } from '../utils/request'

// 客户侧：发起服务呼叫（打扫 / 送物 / 维修 / 其他）
export function createServiceRequest(data) {
  return post('/api/order/service-request', data)
}

// 客户侧：我的服务呼叫列表
export function getMyServiceRequests() {
  return get('/api/order/service-request/my')
}
