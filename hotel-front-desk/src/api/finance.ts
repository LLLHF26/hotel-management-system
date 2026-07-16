import { request } from './request'
import type { DailyTrendVO, OccupancyByTypeVO, RevenueSummaryVO } from '@/types'

export function getRevenueSummary() {
  return request<RevenueSummaryVO>({
    url: '/api/finance/revenue/summary',
    method: 'GET',
  })
}

export function getDailyRevenue(startDate: string, endDate: string) {
  return request<DailyTrendVO>({
    url: '/api/finance/revenue/daily',
    method: 'GET',
    params: { startDate, endDate },
  })
}

export function getOccupancyByType(startDate: string, endDate: string) {
  return request<OccupancyByTypeVO[]>({
    url: '/api/finance/analysis/occupancy-by-type',
    method: 'GET',
    params: { startDate, endDate },
  })
}
