<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getOrderList } from '../services/orderService'
import { useRouter, useRoute } from 'vue-router'

const orders = ref<any[]>([])
const loading = ref(true)
const total = ref(0)
const page = ref(1)
const pageInput = ref(1)
const size = ref(10)
const status = ref('')
const keyword = ref('')
const checkInDate = ref('')
const source = ref('')
const sortKey = ref<keyof any>('createTime')
const sortDir = ref<'asc' | 'desc'>('desc')

const router = useRouter()
const route = useRoute()
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

function statusBadge(s: string) {
  const map: Record<string, string> = {
    '待支付': 'badge-warning',
    '已支付': 'badge-info',
    '已入住': 'badge-success',
    '已完成': 'badge-neutral',
    '已取消': 'badge-danger',
  }
  return map[s] || 'badge-neutral'
}

async function load() {
  loading.value = true
  const params: any = { page: page.value, size: size.value }
  if (status.value) params.status = status.value
  if (keyword.value) {
    params.orderNo = keyword.value
    params.customerPhone = keyword.value
    params.roomNumber = keyword.value
  }
  if (checkInDate.value) params.checkInDate = checkInDate.value
  if (source.value) params.source = source.value
  const res = await getOrderList(params)
  if (res?.data) {
    orders.value = res.data.records || []
    total.value = res.data.total || 0
    page.value = res.data.page || page.value
  }
  loading.value = false
}

onMounted(() => {
  if (route.query.keyword) keyword.value = String(route.query.keyword)
  load()
})

function openDetail(id: number) {
  router.push(`/admin/orders/${id}`)
}

function sortBy(key: keyof any) {
  if (sortKey.value === key) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortDir.value = 'asc'
  }
}

const sortedOrders = computed(() => {
  const k = sortKey.value
  const dir = sortDir.value === 'asc' ? 1 : -1
  return [...orders.value].sort((a, b) => {
    const av = a[k], bv = b[k]
    if (av == null) return 1
    if (bv == null) return -1
    return av > bv ? dir : av < bv ? -dir : 0
  })
})

function sortClass(key: keyof any) {
  if (sortKey.value !== key) return 'sortable'
  return sortDir.value === 'asc' ? 'sortable is-asc' : 'sortable is-desc'
}

function prevPage() { if (page.value > 1) { page.value--; pageInput.value = page.value; load() } }
function nextPage() { if (page.value < totalPages.value) { page.value++; pageInput.value = page.value; load() } }
function onSizeChange() { page.value = 1; pageInput.value = 1; load() }
function gotoPage() {
  let t = Number(pageInput.value)
  if (Number.isNaN(t) || t < 1) t = 1
  if (t > totalPages.value) t = totalPages.value
  page.value = t; load()
}
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">订单管理</h1>
        <p class="page-subtitle">查看所有订单、来源与状态，支持筛选与跳转到详情。</p>
      </div>
      <div class="page-actions">
        <button class="btn">导出</button>
        <button class="btn btn-primary">新建订单</button>
      </div>
    </div>

    <div class="filterbar">
      <select v-model="status" class="select">
        <option value="">全部状态</option>
        <option value="待支付">待支付</option>
        <option value="已支付">已支付</option>
        <option value="已入住">已入住</option>
        <option value="已完成">已完成</option>
        <option value="已取消">已取消</option>
      </select>
      <input v-model="keyword" class="input" placeholder="订单号 / 手机号 / 房号" />
      <label class="label-text">入住</label>
      <input v-model="checkInDate" type="date" class="input" />
      <select v-model="source" class="select">
        <option value="">全部来源</option>
        <option value="ONLINE">线上</option>
        <option value="WALK_IN">到店</option>
        <option value="FRONT_DESK">前台</option>
      </select>
      <button class="btn btn-primary" @click="load">查询</button>
      <span class="filterbar-spacer"></span>
      <span class="text-muted fz-sm">共 {{ total }} 条</span>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th><span :class="sortClass('orderNo')" @click="sortBy('orderNo')">订单号</span></th>
            <th>客户</th>
            <th>手机号</th>
            <th>房间 / 房型</th>
            <th>入住 / 退房</th>
            <th class="num">晚数</th>
            <th class="num">房费 / 支付</th>
            <th>来源</th>
            <th>状态</th>
            <th><span :class="sortClass('createTime')" @click="sortBy('createTime')">创建时间</span></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in sortedOrders" :key="r.id" @click="openDetail(r.id)">
            <td class="mono">{{ r.orderNo }}</td>
            <td>{{ r.customerName }}</td>
            <td class="mono">{{ r.customerPhone }}</td>
            <td>{{ r.roomNumber }} <span class="text-muted">/</span> {{ r.roomTypeName }}</td>
            <td>{{ r.checkInDate }} → {{ r.checkOutDate }}</td>
            <td class="num">{{ r.nights }}</td>
            <td class="num">¥{{ r.roomTotal ?? r.totalAmount }}<br /><span class="text-muted fz-xs">已付 ¥{{ r.paidAmount ?? 0 }}</span></td>
            <td>{{ r.sourceName || r.source }}</td>
            <td><span class="badge" :class="statusBadge(r.status)">{{ r.statusName || r.status }}</span></td>
            <td class="mono fz-xs">{{ r.createTime }}</td>
          </tr>
          <tr v-if="loading">
            <td colspan="10">
              <div class="loading-wrap">
                <div class="loading-dual-ring"></div>
                <span class="loading-text">加载中…</span>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && sortedOrders.length === 0">
            <td colspan="10" class="empty">暂无订单</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination">
      <button class="btn" :disabled="page <= 1" @click="prevPage">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button class="btn" :disabled="page >= totalPages" @click="nextPage">下一页</button>
      <label class="label-text">跳转</label>
      <input type="number" min="1" :max="totalPages" v-model.number="pageInput" class="input" style="width: 72px;" />
      <button class="btn" @click="gotoPage">前往</button>
      <select v-model.number="size" @change="onSizeChange" class="select">
        <option :value="10">10 / 页</option>
        <option :value="20">20 / 页</option>
        <option :value="50">50 / 页</option>
      </select>
      <span class="pagination-spacer"></span>
      <span class="text-muted fz-sm">共 {{ total }} 条</span>
    </div>
  </div>
</template>
