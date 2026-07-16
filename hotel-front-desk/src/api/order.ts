import { request } from './request'
import type { OrderListParams, OrderVO, PageResult } from '@/types'

export function getOrderList(params: OrderListParams) {
  return request<PageResult<OrderVO>>({
    url: '/api/order/list',
    method: 'GET',
    params,
  })
}

export function getOrderDetail(id: number) {
  return request<OrderVO>({
    url: `/api/order/${id}`,
    method: 'GET',
  })
}

export function checkIn(id: number, deposit?: number) {
  return request<void>({
    url: `/api/order/${id}/check-in`,
    method: 'PUT',
    data: { deposit },
  })
}

export function checkOut(id: number, refundDeposit?: number) {
  return request<void>({
    url: `/api/order/${id}/checkout`,
    method: 'PUT',
    data: { refundDeposit },
  })
}

export function extendOrder(id: number, extendDays: number) {
  return request<{
    newCheckOutDate: string
    additionalAmount: number
    newTotalAmount: number
  }>({
    url: `/api/order/${id}/extend`,
    method: 'PUT',
    data: { extendDays },
  })
}

export function changeRoom(id: number, newRoomId: number) {
  return request<Record<string, unknown>>({
    url: `/api/order/${id}/change-room`,
    method: 'PUT',
    data: { newRoomId },
  })
}

export function pay(id: number, amount: number, method: string) {
  return request<Record<string, unknown>>({
    url: `/api/order/${id}/pay`,
    method: 'POST',
    data: { amount, method },
  })
}

export function cancelOrder(id: number, reason?: string) {
  return request<void>({
    url: `/api/order/${id}/cancel`,
    method: 'PUT',
    data: { reason: reason || '客户要求取消' },
  })
}

export function addExtra(
  id: number,
  data: { itemName: string; amount: number; quantity?: number },
) {
  return request<Record<string, unknown>>({
    url: `/api/order/${id}/extra`,
    method: 'POST',
    data,
  })
}
