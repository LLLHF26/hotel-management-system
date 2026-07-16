import { get, post, put } from '../utils/request'

export function createOrder(data) {
  return post('/api/order/create', data)
}

export function getMyOrders(params = {}) {
  return get('/api/order/my', params)
}

export function getMyOrderDetail(id) {
  return get('/api/order/my/' + id)
}

export function cancelOrder(id, reason) {
  return put('/api/order/' + id + '/cancel', { reason })
}

export function payOrder(id, amount, method) {
  return post('/api/order/' + id + '/pay', { amount, method })
}

export function getAvailableRoomCount(roomTypeId, checkIn, checkOut) {
  return get('/api/order/available-rooms', { roomTypeId, checkIn, checkOut })
}
