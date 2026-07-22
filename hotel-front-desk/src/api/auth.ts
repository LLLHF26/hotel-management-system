import { request } from './request'
import type { LoginVO, RefreshVO } from '@/types'

export function login(username: string, password: string) {
  return request<LoginVO>({
    url: '/api/auth/login',
    method: 'POST',
    data: { username, password },
  })
}

export function refreshToken() {
  return request<RefreshVO>({
    url: '/api/auth/refresh',
    method: 'POST',
  })
}

export function logout() {
  return request<void>({
    url: '/api/auth/logout',
    method: 'POST',
  })
}
