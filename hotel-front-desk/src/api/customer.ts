import { request } from './request'
import type { CustomerVO, PageResult } from '@/types'

/** 会员列表（前台按手机号/姓名识别老客，驱动散客转会员） */
export function getCustomerList(params: {
  page?: number
  size?: number
  keyword?: string
  memberLevel?: string
} = {}) {
  return request<PageResult<CustomerVO>>({
    url: '/api/customer/list',
    method: 'GET',
    params: { page: 1, size: 20, ...params },
  })
}
