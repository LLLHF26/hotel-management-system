import { get, post } from '../utils/request'

// 客户侧：可用商品列表（含积分抵扣配置）
export function getProductsWithConfig() {
  return get('/api/order/products')
}

// 客户侧：提交商品订单（挂到某笔订单下）
// payload: { items: [{ productId, quantity }], pointsUsed, remark }
export function submitProductOrder(orderId, payload) {
  return post('/api/order/' + orderId + '/product-order', payload)
}
