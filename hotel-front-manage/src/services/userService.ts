import { authFetch } from './request'

export async function getCustomerList(params: {
  page?: number
  size?: number
  keyword?: string
  memberLevel?: string
  status?: number | string
} = {}) {
  try {
    const queryParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        queryParams.append(key, String(value))
      }
    })
    const query = queryParams.toString() ? `?${queryParams.toString()}` : ''
    const res = await authFetch(`/api/customer/list${query}`)
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
          {
            id: 101,
            realName: '张三',
            phone: '13900000001',
            memberLevel: 'GOLD',
            points: 3600,
            totalConsumed: 18000.0,
            status: 1,
            createTime: '2025-06-01T12:00:00'
          },
          {
            id: 102,
            realName: '李四',
            phone: '13900000002',
            memberLevel: 'SILVER',
            points: 1250,
            totalConsumed: 7200.0,
            status: 1,
            createTime: '2026-02-04T09:20:00'
          },
          {
            id: 103,
            realName: '王五',
            phone: '13900000003',
            memberLevel: 'NORMAL',
            points: 490,
            totalConsumed: 3120.0,
            status: 0,
            createTime: '2025-12-18T16:30:00'
          }
        ]
      }
    }
  }
}

export async function updateCustomerStatus(id: number | string, status: number) {
  try {
    const res = await authFetch(`/api/customer/${id}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status })
    })
    return await res.json()
  } catch (e) {
    return { code: 200, msg: 'mock', data: null }
  }
}

export async function getUserList(params: {
  page?: number
  size?: number
  keyword?: string
  role?: string
  status?: number | string
} = {}) {
  try {
    const queryParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        queryParams.append(key, String(value))
      }
    })
    const query = queryParams.toString() ? `?${queryParams.toString()}` : ''
    const res = await authFetch(`/api/user/list${query}`)
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
          {
            id: 1,
            username: 'admin',
            realName: '李建国',
            phone: '13911112222',
            role: 'ADMIN',
            status: 1,
            createTime: '2025-08-01T10:00:00'
          },
          {
            id: 2,
            username: 'front01',
            realName: '赵敏',
            phone: '13911113333',
            role: 'FRONT_DESK',
            status: 1,
            createTime: '2025-09-12T09:20:00'
          },
          {
            id: 3,
            username: 'clean01',
            realName: '王强',
            phone: '13911114444',
            role: 'CLEANER',
            status: 0,
            createTime: '2026-01-20T08:15:00'
          }
        ]
      }
    }
  }
}

export async function createUser(data: {
  username: string
  password: string
  realName: string
  phone?: string
  email?: string
  role: string
}) {
  try {
    const res = await authFetch('/api/user/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    return await res.json()
  } catch (e) {
    return { code: 200, msg: 'mock', data: { id: Date.now() } }
  }
}

export async function updateUser(id: number | string, data: {
  realName?: string
  phone?: string
  email?: string
  role?: string
}) {
  try {
    const res = await authFetch(`/api/user/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    return await res.json()
  } catch (e) {
    return { code: 200, msg: 'mock', data: null }
  }
}

export async function deleteUser(id: number | string) {
  try {
    const res = await authFetch(`/api/user/${id}`, { method: 'DELETE' })
    return await res.json()
  } catch (e) {
    return { code: 200, msg: 'mock', data: null }
  }
}

export async function updateUserStatus(id: number | string, status: number) {
  try {
    const res = await authFetch(`/api/user/${id}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status })
    })
    return await res.json()
  } catch (e) {
    return { code: 200, msg: 'mock', data: null }
  }
}
