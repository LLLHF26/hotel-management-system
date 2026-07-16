import { request } from './request'
import type { UserVO } from '@/types'

export function getProfile() {
  return request<UserVO>({
    url: '/api/user/profile',
    method: 'GET',
  })
}

export function updateProfile(data: {
  realName?: string
  phone?: string
  email?: string
}) {
  return request<void>({
    url: '/api/user/profile',
    method: 'PUT',
    data,
  })
}

export function changePassword(oldPassword: string, newPassword: string) {
  return request<void>({
    url: '/api/user/password',
    method: 'PUT',
    data: { oldPassword, newPassword },
  })
}
