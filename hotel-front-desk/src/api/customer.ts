import { request } from './request'
import type { CustomerListParams, CustomerVO, PageResult } from '@/types'

export function getCustomerList(params: CustomerListParams) {
  return request<PageResult<CustomerVO>>({
    url: '/api/customer/list',
    method: 'GET',
    params,
  })
}

export function getCustomerDetail(id: number) {
  return request<CustomerVO>({
    url: `/api/customer/${id}`,
    method: 'GET',
  })
}

export function registerCustomer(data: {
  realName: string
  phone: string
  idCard?: string
  gender?: number
}) {
  return request<{ id: number }>({
    url: '/api/customer/register',
    method: 'POST',
    data,
  })
}
