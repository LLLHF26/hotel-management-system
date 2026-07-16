<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderById } from '../services/orderService'

const route = useRoute()
const order = ref<any>(null)

onMounted(async () => {
  const stateOrder = (route as any).state?.order || window.history.state?.order
  if (stateOrder) {
    order.value = stateOrder
    return
  }

  const id = route.params.id
  if (id) {
    const res = await getOrderById(id as string)
    order.value = res?.data ?? res
  }
})
</script>

<template>
  <div class="page-wrap">
    <div class="page-header">
      <div>
        <h1>订单详情</h1>
        <p class="page-subtitle">查看订单基础信息、房间信息、费用明细与支付记录。</p>
      </div>
      <span class="status-chip">{{ order?.statusName || order?.status || '未知状态' }}</span>
    </div>

    <div v-if="order">
      <section class="order-card">
        <div class="card-title">订单概览</div>
        <div class="order-grid">
          <div class="info-item"><div class="info-label">订单号</div><div class="info-value">{{ order.orderNo }}</div></div>
          <div class="info-item"><div class="info-label">来源</div><div class="info-value">{{ order.sourceName || order.source }}</div></div>
          <div class="info-item"><div class="info-label">客户</div><div class="info-value">{{ order.customerName }} / {{ order.customerPhone }}</div></div>
          <div class="info-item"><div class="info-label">创建时间</div><div class="info-value">{{ order.createTime }}</div></div>
          <div class="info-item"><div class="info-label">备注</div><div class="info-value">{{ order.remark || '无' }}</div></div>
        </div>
      </section>

      <section class="order-card">
        <div class="card-title">房间信息</div>
        <div class="order-grid">
          <div class="info-item"><div class="info-label">房号</div><div class="info-value">{{ order.roomNumber }}</div></div>
          <div class="info-item"><div class="info-label">房型</div><div class="info-value">{{ order.roomTypeName }}</div></div>
          <div class="info-item"><div class="info-label">预计入住</div><div class="info-value">{{ order.checkInDate }} 至 {{ order.checkOutDate }}</div></div>
          <div class="info-item"><div class="info-label">实际入住</div><div class="info-value">{{ order.actualCheckIn || '未办理' }}</div></div>
          <div class="info-item"><div class="info-label">实际退房</div><div class="info-value">{{ order.actualCheckOut || '未退房' }}</div></div>
          <div class="info-item"><div class="info-label">晚数</div><div class="info-value">{{ order.nights }}</div></div>
        </div>
      </section>

      <section class="order-card highlight-card">
        <div class="card-title">费用明细</div>
        <div class="fee-grid">
          <div class="fee-item"><span>房费</span><strong>¥{{ order.roomTotal?.toFixed?.(2) ?? order.roomTotal ?? 0 }}</strong></div>
          <div class="fee-item"><span>额外消费</span><strong>¥{{ order.extraTotal?.toFixed?.(2) ?? order.extraTotal ?? 0 }}</strong></div>
          <div class="fee-item"><span>总金额</span><strong>¥{{ order.totalAmount?.toFixed?.(2) ?? order.totalAmount ?? 0 }}</strong></div>
          <div class="fee-item"><span>已付金额</span><strong>¥{{ order.paidAmount?.toFixed?.(2) ?? order.paidAmount ?? 0 }}</strong></div>
          <div class="fee-item"><span>押金</span><strong>¥{{ order.deposit?.toFixed?.(2) ?? order.deposit ?? 0 }}</strong></div>
        </div>
      </section>

      <section class="order-card" v-if="order.extras?.length">
        <div class="card-title">消费明细</div>
        <ul class="detail-list">
          <li v-for="e in order.extras" :key="e.id" class="detail-list-item">
            <div>
              <div class="detail-label">{{ e.itemName }}</div>
              <div class="detail-meta">数量：{{ e.quantity }} × ¥{{ e.amount?.toFixed?.(2) ?? e.amount }}</div>
            </div>
            <div class="detail-amount">¥{{ ((e.amount ?? 0) * (e.quantity ?? 1)).toFixed?.(2) ?? e.amount }}</div>
          </li>
        </ul>
      </section>

      <section class="order-card" v-if="order.payments?.length">
        <div class="card-title">支付记录</div>
        <table class="detail-table">
          <thead>
            <tr>
              <th>支付单号</th>
              <th>方式</th>
              <th>金额</th>
              <th>状态</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in order.payments" :key="p.id">
              <td>{{ p.paymentNo }}</td>
              <td>{{ p.methodName || p.method }}</td>
              <td>¥{{ p.amount?.toFixed?.(2) ?? p.amount }}</td>
              <td>{{ p.status }}</td>
              <td>{{ p.paidAt }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
    <div v-else>加载中或未找到</div>
  </div>
</template>

<style scoped>
.order-card {
  background: #ffffff;
  border-radius: 20px;
  box-shadow: var(--shadow);
  padding: 24px;
  margin-bottom: 18px;
}
.card-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 18px;
  color: #111827;
}
.order-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}
.info-item {
  display: grid;
  gap: 6px;
  padding: 16px;
  border: 1px solid rgba(15, 23, 36, 0.08);
  border-radius: 16px;
  background: #f8fafc;
}
.info-label {
  color: #6b7280;
  font-size: 13px;
}
.info-value {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}
.highlight-card .fee-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}
.fee-item {
  background: #f8fafe;
  border-radius: 16px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 1px solid rgba(59, 130, 246, 0.12);
}
.fee-item span {
  color: #475569;
  font-size: 14px;
}
.fee-item strong {
  font-size: 18px;
  color: #0f172a;
}
.detail-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}
.detail-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 36, 0.08);
}
.detail-label {
  font-weight: 700;
  color: #111827;
}
.detail-meta {
  color: #6b7280;
  font-size: 13px;
}
.detail-amount {
  font-weight: 700;
  color: #111827;
}
.detail-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 100%;
}
.detail-table th,
.detail-table td {
  padding: 14px 12px;
  text-align: left;
  border-bottom: 1px solid rgba(15, 23, 36, 0.08);
  color: #334155;
}
.detail-table th {
  background: #f8fafc;
  font-weight: 700;
}
.detail-table tr:nth-child(even) {
  background: #fbfbfb;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 18px;
}
.status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 16px;
  border-radius: 999px;
  background: #eef2ff;
  color: #1d4ed8;
  font-weight: 700;
  white-space: nowrap;
}
.page-subtitle {
  margin-top: 6px;
  color: #6b7280;
}
@media (max-width: 900px) {
  .order-grid,
  .highlight-card .fee-grid {
    grid-template-columns: 1fr;
  }
}
</style>
