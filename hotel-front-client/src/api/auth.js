import { post } from '../utils/request'

export function login(phone, password) {
  return post('/api/auth/login', { username: phone, password })
}

export function register(data) {
  return post('/api/customer/register', data)
}

export function refreshToken() {
  return post('/api/auth/refresh')
}

export function logout() {
  return post('/api/auth/logout')
}
