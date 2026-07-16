import { request } from './request'
import type { CleanerVO, DashboardVO, PageResult, RoomListParams, RoomVO } from '@/types'

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
