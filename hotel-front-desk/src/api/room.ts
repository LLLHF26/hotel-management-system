import { request } from './request'
import type { CleanerVO, DashboardVO, OrderVO, PageResult, RoomListParams, RoomTypeVO, RoomVO } from '@/types'

export function getRoomDashboard(floor?: number) {
  return request<DashboardVO>({
    url: '/api/room/dashboard',
    method: 'GET',
    params: floor != null ? { floor } : undefined,
  })
}

export function getRoomDetail(id: number) {
  return request<RoomVO>({
    url: `/api/room/${id}`,
    method: 'GET',
  })
}

export function getRoomList(params: RoomListParams) {
  return request<PageResult<RoomVO>>({
    url: '/api/room/list',
    method: 'GET',
    params,
  })
}

export function getCleanerList() {
  return request<CleanerVO[]>({
    url: '/api/user/cleaner/list',
    method: 'GET',
  })
}

export function assignCleaning(roomId: number, cleanerId: number) {
  return request<{ taskId: number }>({
    url: `/api/room/${roomId}/cleaning/assign`,
    method: 'POST',
    data: { cleanerId },
  })
}

export function getRoomTypeList(params?: { page?: number; size?: number; keyword?: string }) {
  return request<PageResult<RoomTypeVO>>({
    url: '/api/room/type/list',
    method: 'GET',
    params: { page: 1, size: 100, ...params },
  })
}

export function getRoomSchedule(roomId: string | number, startDate?: string, endDate?: string) {
  return request<OrderVO[]>({
    url: `/api/order/room/${roomId}/schedule`,
    method: 'GET',
    params: {
      ...(startDate ? { startDate } : {}),
      ...(endDate ? { endDate } : {}),
    },
  })
}
