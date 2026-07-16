<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getCustomerList, updateCustomerStatus } from '../services/userService'

const customers = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageInput = ref(1)
const size = ref(10)
const keyword = ref('')
const memberLevel = ref('')
const status = ref('')

const memberLevels = [
  { label: '全部等级', value: '' },
  { label: '普通', value: 'NORMAL' },
  { label: '银卡', value: 'SILVER' },
  { label: '黄金', value: 'GOLD' },
  { label: '钻石', value: 'DIAMOND' }
]
const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '正常', value: '1' },
  { label: '冻结', value: '0' }
]

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

function formatMemberLevel(value: string) {
  const map: Record<string, string> = {
    NORMAL: '普通',
    SILVER: '银卡',
    GOLD: '黄金',
    DIAMOND: '钻石'
  }
  return map[value] || value || '未知'
}

function formatStatus(value: number) {
  return value === 1 ? '正常' : '冻结'
}

async function loadCustomers() {
  const res = await getCustomerList({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
    memberLevel: memberLevel.value || undefined,
    status: status.value !== '' ? Number(status.value) : undefined
  })
  const data = res?.data ?? res
  customers.value = data.records || []
  total.value = data.total || 0
  page.value = data.page || page.value
  pageInput.value = page.value
}

function applyFilters() {
  page.value = 1
  pageInput.value = 1
  loadCustomers()
}

async function toggleStatus(customer: any) {
  const nextStatus = customer.status === 1 ? 0 : 1
  await updateCustomerStatus(customer.id, nextStatus)
  await loadCustomers()
}

function prevPage() {
  if (page.value > 1) {
    page.value--
    loadCustomers()
  }
}

function nextPage() {
  if (page.value < totalPages.value) {
    page.value++
    loadCustomers()
  }
}

function gotoPage() {
  let target = Number(pageInput.value)
  if (Number.isNaN(target) || target < 1) target = 1
  if (target > totalPages.value) target = totalPages.value
  page.value = target
  loadCustomers()
}

function onSizeChange() {
  page.value = 1
  pageInput.value = 1
  loadCustomers()
}

onMounted(loadCustomers)
</script>

<template>
  <div class="page-wrap">
    <div class="page-header">
      <div>
        <h1>客户管理</h1>
        <p class="page-subtitle">会员客户列表，支持按姓名/手机号搜索与冻结状态管理。</p>
      </div>
    </div>

    <div class="filters" style="margin-bottom:18px; display:flex; flex-wrap:wrap; gap:12px; align-items:center;">
      <input v-model="keyword" placeholder="姓名/手机号搜索" />
      <select v-model="memberLevel">
        <option v-for="item in memberLevels" :key="item.value" :value="item.value">{{ item.label }}</option>
      </select>
      <select v-model="status">
        <option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
      </select>
      <button class="primary-button" @click="applyFilters">查询</button>
      <div style="margin-left:auto; color:#6b7280">共 {{ total }} 条</div>
    </div>

    <div class="table customer-table">
      <div class="row header">
        <div>姓名</div>
        <div>手机号</div>
        <div>会员等级</div>
        <div>积分</div>
        <div>累计消费</div>
        <div>注册时间</div>
        <div>状态</div>
        <div>操作</div>
      </div>
      <div class="row" v-for="customer in customers" :key="customer.id">
        <div>{{ customer.realName }}</div>
        <div>{{ customer.phone }}</div>
        <div>{{ formatMemberLevel(customer.memberLevel) }}</div>
        <div>{{ customer.points }}</div>
        <div>¥{{ customer.totalConsumed?.toFixed?.(2) ?? customer.totalConsumed ?? 0 }}</div>
        <div>{{ customer.createTime ? customer.createTime.split('T')[0] : '—' }}</div>
        <div>{{ formatStatus(customer.status) }}</div>
        <div>
          <button class="primary-button" style="background:#2563eb" @click="toggleStatus(customer)">
            {{ customer.status === 1 ? '冻结' : '解冻' }}
          </button>
        </div>
      </div>
    </div>

    <div class="pagination" style="margin-top:16px; display:flex; gap:8px; align-items:center;">
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
    </div>
  </div>
</template>
