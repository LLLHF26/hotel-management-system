import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { ApiResult } from '@/types'
import { TOKEN_KEY } from '@/utils/constants'

const apiBase = import.meta.env.VITE_API_BASE ?? ''

export const http: AxiosInstance = axios.create({
  baseURL: apiBase,
  timeout: 30000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const result = response.data as ApiResult
    if (result && typeof result.code === 'number' && result.code !== 200) {
      ElMessage.error(result.msg || '请求失败')
      return Promise.reject(new Error(result.msg || '请求失败'))
    }
    return response
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('hotel_fd_user')
      router.push('/login')
      ElMessage.warning('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.msg || error.message || '网络错误')
    }
    return Promise.reject(error)
  },
)

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const res = await http.request<ApiResult<T>>(config)
  return res.data.data
}

const aiBase = import.meta.env.VITE_AI_BASE ?? '/ai-api'

export const aiHttp: AxiosInstance = axios.create({
  baseURL: aiBase,
  timeout: 15000,
})

aiHttp.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export async function aiRequest<T>(config: AxiosRequestConfig): Promise<T> {
  const res = await aiHttp.request<ApiResult<T>>(config)
  const body = res.data
  if (body && typeof body.code === 'number' && body.code !== 200) {
    return body.data
  }
  return (body?.data ?? body) as T
}
