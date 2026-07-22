import { authFetch } from './request'

export async function getRoomDashboard(floor?: number) {
  try {
    const query = floor !== undefined ? `?floor=${encodeURIComponent(String(floor))}` : ''
    const res = await authFetch(`/api/room/dashboard${query}`)
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'dashboard request failed')
    return json
  } catch (e) {
    return {
      code: 200,
      msg: 'mock',
      data: {
        summary: {
          total: 80,
          空闲中: 20,
          预订中: 15,
          入住中: 30,
          待清洁中: 8,
          打扫中: 5,
          维修中: 2
        },
        rooms: []
      }
    }
  }
}

export async function getRoomList(params: {
  page?: number
  size?: number
  roomTypeId?: number | string
  status?: string
  floor?: number | string
  keyword?: string
} = {}) {
  try {
    const queryParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        queryParams.append(key, String(value))
      }
    })
    const query = queryParams.toString() ? `?${queryParams.toString()}` : ''
    const res = await authFetch(`/api/room/list${query}`)
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || 'room list request failed')
    return json
  } catch (e) {
    return {
      code: 200,
      msg: 'mock',
      data: {
        total: 6,
        page: 1,
        size: 6,
        records: [
          { id: 5, roomNumber: '301', roomTypeName: '豪华大床房', floor: 3, status: '空闲中', price: 588.0 },
          { id: 6, roomNumber: '302', roomTypeName: '商务双床房', floor: 3, status: '预订中', price: 488.0 },
          { id: 7, roomNumber: '303', roomTypeName: '标准大床房', floor: 3, status: '入住中', price: 368.0 },
          { id: 8, roomNumber: '401', roomTypeName: '豪华大床房', floor: 4, status: '入住中', price: 588.0 },
          { id: 9, roomNumber: '402', roomTypeName: '行政套房', floor: 4, status: '待清洁中', price: 788.0 },
          { id: 10, roomNumber: '403', roomTypeName: '标准大床房', floor: 4, status: '维修中', price: 368.0 }
        ]
      }
    }
  }
}

export async function createRoom(data: {
  roomNumber: string
  roomTypeId: number
  floor?: number
  price?: number
  description?: string
}) {
  try {
    const res = await authFetch('/api/room/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    return await res.json()
  } catch (e) {
    return { code: 200, msg: 'mock', data: { id: Date.now() } }
  }
}

export async function getRoomTypeList(params: {
  page?: number
  size?: number
  keyword?: string
} = {}) {
  try {
    const queryParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        queryParams.append(key, String(value))
      }
    })
    const query = queryParams.toString() ? `?${queryParams.toString()}` : ''
    const res = await authFetch(`/api/room/type/list${query}`)
    return await res.json()
  } catch (e) {
    return {
      code: 200,
      msg: 'mock',
      data: {
        total: 3,
        page: params.page ?? 1,
        size: params.size ?? 10,
        records: [
          { id: 1, name: '豪华大床房', description: '45㎡，独立卫浴，城市景观', area: 45, bedType: '1 大床', maxGuests: 2, price: 588.0, coverImage: 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=200', images: ['https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=200', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=200'], amenities: 'WiFi, 电视, 迷你吧', sortOrder: 1 },
          { id: 2, name: '商务双床房', description: '42㎡，双床，办公空间', area: 42, bedType: '2 单床', maxGuests: 2, price: 488.0, coverImage: 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=200', images: ['https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=200'], amenities: 'WiFi, 书桌, 浴缸', sortOrder: 2 },
          { id: 3, name: '行政套房', description: '65㎡，独立客厅，豪华配置', area: 65, bedType: '1 大床', maxGuests: 3, price: 998.0, coverImage: 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=200', images: ['https://images.unsplash.com/photo-1590490360182-c33d57733427?w=200', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=200', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=200'], amenities: 'WiFi, 沙发, 独立客厅', sortOrder: 3 }
        ]
      }
    }
  }
}

export async function createRoomType(data: any) {
  try {
    const res = await authFetch('/api/room/type/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    return await res.json()
  } catch (e) {
    return { code: 200, msg: 'mock', data: { id: Date.now() } }
  }
}

export async function updateRoomType(id: number | string, data: any) {
  try {
    const res = await authFetch(`/api/room/type/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    return await res.json()
  } catch (e) {
    return { code: 200, msg: 'mock', data: null }
  }
}

export async function uploadRoomImage(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const res = await authFetch('/api/room/upload/image', {
    method: 'POST',
    body: formData,
  })
  const json = await res.json()
  return json?.data?.url || ''
}

export async function uploadRoomCover(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const res = await authFetch('/api/room/upload/cover', {
    method: 'POST',
    body: formData,
  })
  const json = await res.json()
  return json?.data?.url || ''
}

export async function deleteRoomType(id: number | string) {
  try {
    const res = await authFetch(`/api/room/type/${id}`, { method: 'DELETE' })
    return await res.json()
  } catch (e) {
    return { code: 200, msg: 'mock', data: null }
  }
}

export async function updateRoom(
  id: number | string,
  data: {
    roomNumber?: string
    roomTypeId?: number
    floor?: number
    price?: number
    description?: string
  }
) {
  try {
    const res = await authFetch(`/api/room/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return await res.json()
  } catch (e) {
    return { code: 200, msg: 'mock', data: null }
  }
}

export async function changeRoomStatus(id: number, status: string, reason?: string) {
  const res = await authFetch(`/api/room/${id}/status`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status, reason: reason || undefined }),
  })
  return await res.json()
}
