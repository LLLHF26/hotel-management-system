import { authFetch } from './request'

export interface Product {
  id: number
  category: string
  name: string
  price: number
  image?: string
  unit?: string
  stock?: number
  status: string
  description?: string
  sortOrder?: number
}

export interface ProductPage {
  total: number
  page: number
  size: number
  records: Product[]
}

/** 管理侧：商品分页列表 */
export async function getProducts(params: { keyword?: string; page?: number; size?: number } = {}) {
  try {
    const q = new URLSearchParams()
    if (params.keyword) q.set('keyword', params.keyword)
    if (params.page) q.set('page', String(params.page))
    if (params.size) q.set('size', String(params.size))
    const res = await authFetch(`/api/order/products/admin?${q.toString()}`)
    const json = await res.json()
    if (json?.code !== 200) throw new Error(json?.msg || '获取商品列表失败')
    return json
  } catch (e: any) {
    return { code: 200, msg: 'mock', data: { total: 0, page: 1, size: 10, records: [] } }
  }
}

/** 管理侧：新建商品 */
export async function createProduct(data: Partial<Product>) {
  const res = await authFetch('/api/order/products', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  const json = await res.json()
  if (json?.code !== 200) throw new Error(json?.msg || '创建商品失败')
  return json
}

/** 管理侧：编辑商品 */
export async function updateProduct(id: number, data: Partial<Product>) {
  const res = await authFetch(`/api/order/products/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  const json = await res.json()
  if (json?.code !== 200) throw new Error(json?.msg || '更新商品失败')
  return json
}

/** 管理侧：删除商品 */
export async function deleteProduct(id: number) {
  const res = await authFetch(`/api/order/products/${id}`, { method: 'DELETE' })
  const json = await res.json()
  if (json?.code !== 200) throw new Error(json?.msg || '删除商品失败')
  return json
}

/** 管理侧：上传商品封面图，返回图片 URL */
export async function uploadProductImage(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const res = await authFetch('/api/room/upload/image', {
    method: 'POST',
    body: formData,
    // 不设 Content-Type，让浏览器自动带 multipart boundary
  } as any)
  const json = await res.json()
  return json?.data?.url || ''
}
