import { request } from './request'
import type { ExtraVO, OrderCreateDTO, OrderCreateResult, OrderListParams, OrderVO, PageResult } from '@/types'

export function getOrderList(params: OrderListParams) {
  return request<PageResult<OrderVO>>({
    url: '/api/order/list',
    method: 'GET',
    params,
  })
}

/** 新建预订 / 散客入住（后端要求携带 X-Idempotency-Key 防重复提交） */
export function createOrder(dto: OrderCreateDTO) {
  const idempotencyKey =
    'fd-' + Date.now() + '-' + Math.random().toString(36).slice(2, 10)
  return request<OrderCreateResult>({
    url: '/api/order/create',
    method: 'POST',
    data: dto,
    headers: { 'X-Idempotency-Key': idempotencyKey },
  })
}

export function getOrderDetail(id: string | number) {
  return request<OrderVO>({
    url: `/api/order/${id}`,
    method: 'GET',
  })
}

export function checkIn(id: string | number, deposit?: number) {
  return request<void>({
    url: `/api/order/${id}/check-in`,
    method: 'PUT',
    data: { deposit },
  })
}

export function checkOut(id: string | number, refundDeposit?: number) {
  return request<void>({
    url: `/api/order/${id}/checkout`,
    method: 'PUT',
    data: { refundDeposit },
  })
}

export function extendOrder(id: string | number, extendDays: number) {
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

export function changeRoom(
  id: string | number,
  newRoomId: string | number,
  startDate?: string,
  endDate?: string,
) {
  return request<Record<string, unknown>>({
    url: `/api/order/${id}/change-room`,
    method: 'PUT',
    data: {
      newRoomId,
      ...(startDate ? { startDate } : {}),
      ...(endDate ? { endDate } : {}),
    },
  })
}

export function pay(id: string | number, amount: number, method: string) {
  return request<Record<string, unknown>>({
    url: `/api/order/${id}/pay`,
    method: 'POST',
    data: { amount, method },
  })
}

export function cancelOrder(id: string | number, reason?: string) {
  return request<void>({
    url: `/api/order/${id}/cancel`,
    method: 'PUT',
    data: { reason: reason || '客户要求取消' },
  })
}

export function addExtra(
  id: string | number,
  data: { itemName: string; amount: number; quantity?: number },
) {
  return request<Record<string, unknown>>({
    url: `/api/order/${id}/extra`,
    method: 'POST',
    data,
  })
}

/** 订单额外消费明细（客房商品 / 服务消费） */
export function getOrderExtras(id: string | number) {
  return request<ExtraVO[]>({
    url: `/api/order/${id}/extras`,
    method: 'GET',
  })
}
