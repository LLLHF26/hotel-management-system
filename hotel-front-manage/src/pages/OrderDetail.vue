<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById } from '../services/orderService'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)

function statusBadge(s: string) {
  const map: Record<string, string> = {
    '待支付': 'badge-warning', '已支付': 'badge-info', '已入住': 'badge-success',
    '已完成': 'badge-neutral', '已取消': 'badge-danger',
  }
  return map[s] || 'badge-neutral'
}

function fmt(v: any) {
  if (v == null) return '0.00'
  const n = Number(v)
  return Number.isNaN(n) ? String(v) : n.toFixed(2)
}

onMounted(async () => {
  const stateOrder = (route as any).state?.order || window.history.state?.order
  if (stateOrder) { order.value = stateOrder; return }
  const id = route.params.id
  if (id) {
    const res = await getOrderById(id as string)
    order.value = res?.data ?? res
  }
})
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="row row-gap-2">
          <button class="btn btn-ghost btn-sm" @click="router.push('/admin/orders')">← 返回</button>
          <h1 class="page-title">订单详情</h1>
        </div>
        <p class="page-subtitle">订单基础信息、房间信息、费用明细与支付记录。</p>
      </div>
      <span v-if="order" class="badge" :class="statusBadge(order.statusName || order.status)">{{ order.statusName || order.status || '未知状态' }}</span>
    </div>

    <div v-if="!order" class="card card-pad text-center text-muted">加载中…</div>

    <template v-else>
      <div class="card mb-4">
        <div class="card-head">
          <div class="card-title">订单概览</div>
        </div>
        <div class="card-body">
          <div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--sp-4);">
            <div v-for="(item, i) in [
              { l: '订单号',   v: order.orderNo },
              { l: '来源',     v: order.sourceName || order.source },
              { l: '客户',     v: `${order.customerName} / ${order.customerPhone}` },
              { l: '创建时间', v: order.createTime },
              { l: '备注',     v: order.remark || '无' },
            ]" :key="i" class="col-gap-2">
              <div class="label-text">{{ item.l }}</div>
              <div class="fw-600">{{ item.v }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="card mb-4">
        <div class="card-head"><div class="card-title">房间信息</div></div>
        <div class="card-body">
          <div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--sp-4);">
            <div v-for="(item, i) in [
              { l: '房号',     v: order.roomNumber },
              { l: '房型',     v: order.roomTypeName },
              { l: '入住 / 退房', v: `${order.checkInDate} 至 ${order.checkOutDate}` },
              { l: '实际入住', v: order.actualCheckIn || '未办理' },
              { l: '实际退房', v: order.actualCheckOut || '未退房' },
              { l: '晚数',     v: order.nights },
            ]" :key="i" class="col-gap-2">
              <div class="label-text">{{ item.l }}</div>
              <div class="fw-600">{{ item.v }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="card mb-4">
        <div class="card-head"><div class="card-title">费用明细</div></div>
        <div class="card-body">
          <div style="display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: var(--sp-3);">
            <div v-for="(item, i) in [
              { l: '房费',     v: order.roomTotal },
              { l: '额外消费', v: order.extraTotal },
              { l: '总金额',   v: order.totalAmount },
              { l: '已付金额', v: order.paidAmount },
              { l: '押金',     v: order.deposit },
            ]" :key="i" class="col-gap-2" style="padding: var(--sp-3); background: var(--bg-subtle); border-radius: var(--r-md);">
              <div class="label-text">{{ item.l }}</div>
              <div class="fw-600" style="font-size: var(--fz-lg);">¥{{ fmt(item.v) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="order.extras?.length" class="card mb-4">
        <div class="card-head"><div class="card-title">消费明细</div></div>
        <div class="card-body" style="padding: 0;">
          <table class="table">
            <thead><tr><th>项目</th><th class="num">数量</th><th class="num">单价</th><th class="num">小计</th></tr></thead>
            <tbody>
              <tr v-for="e in order.extras" :key="e.id">
                <td>{{ e.itemName }}</td>
                <td class="num">{{ e.quantity }}</td>
                <td class="num">¥{{ fmt(e.amount) }}</td>
                <td class="num">¥{{ fmt((Number(e.amount)||0) * (Number(e.quantity)||1)) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="order.payments?.length" class="card">
        <div class="card-head"><div class="card-title">支付记录</div></div>
        <div class="card-body" style="padding: 0;">
          <table class="table">
            <thead><tr><th>支付单号</th><th>方式</th><th class="num">金额</th><th>状态</th><th>时间</th></tr></thead>
            <tbody>
              <tr v-for="p in order.payments" :key="p.id">
                <td class="mono">{{ p.paymentNo }}</td>
                <td>{{ p.methodName || p.method }}</td>
                <td class="num">¥{{ fmt(p.amount) }}</td>
                <td><span class="badge badge-success">{{ p.status }}</span></td>
                <td class="mono fz-xs">{{ p.paidAt }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
