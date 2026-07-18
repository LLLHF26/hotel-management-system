import { get } from '../utils/request'

export function getRoomTypeList(params = {}) {
  return get('/api/room/type/list', params)
}

export function getRoomTypeDetail(id) {
  return get('/api/room/type/' + id)
}

export function getHotRoomTypes() {
  return get('/api/room/type/hot')
}

export function getAvailableRooms(roomTypeId) {
  return get('/api/room/list', { roomTypeId, status: '空闲中' })
}
