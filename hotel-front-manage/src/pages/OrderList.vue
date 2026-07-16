<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getOrderList } from '../services/orderService'
import { useRouter } from 'vue-router'

const orders = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageInput = ref(1)
const size = ref(10)
const status = ref('')
const keyword = ref('')
const checkInDate = ref('')
const source = ref('')
const router = useRouter()
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

async function load() {
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
  } else if (Array.isArray(res)) {
    orders.value = res
  }
}

onMounted(load)

function openDetail(id: number, orderRow: any) {
  router.push({
    path: `/admin/orders/${id}`,
    state: { order: orderRow }
  })
}

function prevPage() {
  if (page.value > 1) { page.value--; pageInput.value = page.value; load() }
}
function nextPage() {
  if (page.value < totalPages.value) { page.value++; pageInput.value = page.value; load() }
}
function onSizeChange() {
  page.value = 1
  pageInput.value = 1
  load()
}
function gotoPage() {
  let target = Number(pageInput.value)
  if (Number.isNaN(target) || target < 1) target = 1
  if (target > totalPages.value) target = totalPages.value
  page.value = target
  load()
}

</script>

<template>
  <div class="page-wrap">
    <h1>订单列表</h1>
    <div class="filters" style="margin-bottom:12px;display:flex;flex-wrap:wrap;gap:8px;align-items:center">
      <select v-model="status">
        <option value="">全部状态</option>
        <option value="待支付">待支付</option>
        <option value="已支付">已支付</option>
        <option value="已入住">已入住</option>
        <option value="已完成">已完成</option>
        <option value="已取消">已取消</option>
      </select>
      <input v-model="keyword" placeholder="订单号/手机号/房号" />
      <label style="display:flex;align-items:center;gap:6px">
        入住日期
        <input type="date" v-model="checkInDate" />
      </label>
      <select v-model="source">
        <option value="">全部来源</option>
        <option value="ONLINE">线上</option>
        <option value="WALK_IN">到店</option>
        <option value="FRONT_DESK">前台</option>
      </select>
      <button @click="load">查询</button>
      <div style="margin-left:auto">共 {{ total }} 条</div>
    </div>

    <div class="table">
      <div class="row header">
        <div>订单号</div>
        <div>客户</div>
        <div>手机号</div>
        <div>房间 / 房型</div>
        <div>入住 / 退房</div>
        <div>晚数</div>
        <div>房费 / 支付</div>
        <div>来源</div>
        <div>状态</div>
        <div>创建时间</div>
      </div>
      <div v-for="r in orders" :key="r.id" class="row" @click="openDetail(r.id, r)">
        <div>{{ r.orderNo }}</div>
        <div>{{ r.customerName }}</div>
        <div>{{ r.customerPhone }}</div>
        <div>{{ r.roomNumber }} / {{ r.roomTypeName }}</div>
        <div>{{ r.checkInDate }} → {{ r.checkOutDate }}</div>
        <div>{{ r.nights }}</div>
        <div>¥{{ r.roomTotal ?? r.totalAmount }} / ¥{{ r.paidAmount ?? 0 }}</div>
        <div>{{ r.sourceName || r.source }}</div>
        <div>{{ r.statusName || r.status }}</div>
        <div>{{ r.createTime }}</div>
      </div>
    </div>

    <div class="pagination" style="margin-top:12px;display:flex;gap:8px;align-items:center">
      <button @click="prevPage">上一页</button>
      <div>第 {{ page }} / {{ totalPages }} 页</div>
      <button @click="nextPage">下一页</button>
      <label>跳转到
        <input type="number" min="1" :max="totalPages" v-model.number="pageInput" style="width:72px; margin:0 6px" />页
      </label>
      <button @click="gotoPage">前往</button>
      <select v-model.number="size" @change="onSizeChange">
        <option :value="10">10</option>
        <option :value="20">20</option>
        <option :value="50">50</option>
      </select>
      <div style="margin-left:auto">共 {{ total }} 条</div>
    </div>
  </div>
</template>
