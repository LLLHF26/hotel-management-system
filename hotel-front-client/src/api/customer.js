import { get, put, uploadFile } from '../utils/request'

export function getProfile() {
  return get('/api/customer/profile')
}

export function updateProfile(data) {
  return put('/api/customer/profile', data)
}

export function uploadAvatar(filePath) {
  return uploadFile('/api/user/upload/avatar', filePath, 'file')
}

export function changePassword(oldPassword, newPassword) {
  return put('/api/customer/password', { oldPassword, newPassword })
}
