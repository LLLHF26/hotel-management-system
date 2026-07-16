import { post } from '../utils/request'

export function queryOrders(query) {
  return post('/api/ai/order/query', { query }, true)
}
