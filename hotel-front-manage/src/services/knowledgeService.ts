import { authFetch } from './request'

interface BackendDoc {
  id: number
  filename: string
  file_type: string
  file_size: number
  category: string
  chunk_count: number
  is_enabled: boolean
  create_time: string
}

interface FrontendDoc {
  id: number
  name: string
  category: string
  type: string
  size: string
  status: string
  uploadTime: string
  vectorStatus: string
}

function mapDoc(doc: BackendDoc): FrontendDoc {
  return {
    id: doc.id,
    name: doc.filename,
    category: doc.category,
    type: doc.file_type,
    size: formatFileSize(doc.file_size),
    status: doc.is_enabled ? '启用' : '停用',
    uploadTime: doc.create_time?.replace('T', ' ').substring(0, 19) || '',
    vectorStatus: doc.chunk_count > 0 ? '已向量化' : '未向量化'
  }
}

export async function getDocumentList(params: {
  page?: number
  size?: number
  category?: string
  keyword?: string
} = {}) {
  const q = new URLSearchParams()
  q.append('page', String(params.page ?? 1))
  q.append('size', String(params.size ?? 100))
  if (params.category) q.append('category', params.category)
  if (params.keyword) q.append('keyword', params.keyword)
  const res = await authFetch(`/api/ai/knowledge/list?${q.toString()}`)
  const json = await res.json()
  if (json.code === 200 && json.data) {
    return {
      ...json,
      data: (json.data.records || []).map(mapDoc)
    }
  }
  throw new Error(json.msg || '获取文档列表失败')
}

export async function uploadDocument(file: File, category: string) {
  const formData = new FormData()
  formData.append('files', file)
  const res = await authFetch(`/api/ai/knowledge/upload?category=${encodeURIComponent(category)}`, {
    method: 'POST',
    body: formData
  })
  return await res.json()
}

export async function deleteDocument(docId: number) {
  const res = await authFetch(`/api/ai/knowledge/${docId}`, { method: 'DELETE' })
  return await res.json()
}

export async function updateDocumentStatus(docId: number, enabled: boolean) {
  const res = await authFetch(`/api/ai/knowledge/${docId}/status`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ is_enabled: enabled })
  })
  return await res.json()
}

export async function rebuildVectorDB() {
  const res = await authFetch('/api/ai/knowledge/rebuild-vector', { method: 'POST' })
  return await res.json()
}

export async function previewDocument(docId: number) {
  const res = await authFetch(`/api/ai/knowledge/${docId}/preview`)
  if (!res.ok) {
    const json = await res.json().catch(() => ({}))
    throw new Error(json.msg || '预览失败')
  }
  const blob = await res.blob()
  const blobUrl = URL.createObjectURL(blob)
  window.open(blobUrl, '_blank')
  setTimeout(() => URL.revokeObjectURL(blobUrl), 5 * 60 * 1000)
}

export async function getDocumentContent(docId: number): Promise<string> {
  const res = await authFetch(`/api/ai/knowledge/${docId}/content`)
  const json = await res.json()
  if (!res.ok || json.code !== 200) {
    throw new Error(json.msg || '读取文档内容失败')
  }
  return json.data || ''
}

export async function getCategories() {
  const res = await authFetch('/api/ai/knowledge/categories')
  return await res.json()
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / (1024 * 1024)).toFixed(2) + 'MB'
}
