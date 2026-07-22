<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getCustomerList, updateCustomerStatus } from '../services/userService'
import { useToast } from '../composables/useToast'

const toast = useToast()

const customers = ref<any[]>([])
const loading = ref(true)
const total = ref(0)
const page = ref(1)
const pageInput = ref(1)
const size = ref(10)
const keyword = ref('')
const memberLevel = ref('')
const status = ref('')

const memberLevels = [
  { label: '全部等级', value: '' },
  { label: '普通',     value: 'NORMAL'  },
  { label: '银卡',     value: 'SILVER'  },
  { label: '黄金',     value: 'GOLD'    },
  { label: '钻石',     value: 'DIAMOND' },
]
const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '正常',     value: '1' },
  { label: '冻结',     value: '0' },
]

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

function levelLabel(v: string) {
  return ({ NORMAL: '普通', SILVER: '银卡', GOLD: '黄金', DIAMOND: '钻石' } as Record<string,string>)[v] || v || '未知'
}
function levelPill(v: string) {
  return ({ NORMAL: 'pill-slate', SILVER: 'pill-blue', GOLD: 'pill-amber', DIAMOND: 'pill-violet' } as Record<string,string>)[v] || 'pill-slate'
}
function statusBadge(v: number) {
  return v === 1 ? 'badge-success' : 'badge-danger'
}

async function load() {
  loading.value = true
  const res = await getCustomerList({
    page: page.value, size: size.value, keyword: keyword.value,
    memberLevel: memberLevel.value || undefined,
    status: status.value !== '' ? Number(status.value) : undefined,
  })
  const data = res?.data ?? res
  customers.value = data.records || []
  total.value = data.total || 0
  page.value = data.page || page.value
  pageInput.value = page.value
  loading.value = false
}

onMounted(load)

async function toggleStatus(c: any) {
  const next = c.status === 1 ? 0 : 1
  try {
    await updateCustomerStatus(c.id, next)
    await load()
    toast.success(next === 1 ? '已解冻客户' : '已冻结客户')
  } catch (e: any) { toast.error(e?.message || '操作失败') }
}
function prevPage() { if (page.value > 1) { page.value--; load() } }
function nextPage() { if (page.value < totalPages.value) { page.value++; load() } }
function gotoPage() {
  let t = Number(pageInput.value)
  if (Number.isNaN(t) || t < 1) t = 1
  if (t > totalPages.value) t = totalPages.value
  page.value = t; load()
}
function onSizeChange() { page.value = 1; pageInput.value = 1; load() }
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">客户管理</h1>
        <p class="page-subtitle">会员客户列表，支持按姓名/手机号搜索与冻结管理。</p>
      </div>
      <div class="page-actions">
        <button class="btn">导出</button>
      </div>
    </div>

    <div class="filterbar">
      <input v-model="keyword" class="input" placeholder="姓名 / 手机号" />
      <select v-model="memberLevel" class="select">
        <option v-for="o in memberLevels" :key="o.value" :value="o.value">{{ o.label }}</option>
      </select>
      <select v-model="status" class="select">
        <option v-for="o in statusOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
      </select>
      <button class="btn btn-primary" @click="(page=1, load())">查询</button>
      <span class="filterbar-spacer"></span>
      <span class="text-muted fz-sm">共 {{ total }} 条</span>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>姓名</th>
            <th>手机号</th>
            <th>会员等级</th>
            <th class="num">积分</th>
            <th class="num">累计消费</th>
            <th>注册时间</th>
            <th>状态</th>
            <th class="actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in customers" :key="c.id">
            <td class="fw-600">{{ c.realName }}</td>
            <td class="mono">{{ c.phone }}</td>
            <td><span class="pill" :class="levelPill(c.memberLevel)">{{ levelLabel(c.memberLevel) }}</span></td>
            <td class="num">{{ c.points }}</td>
            <td class="num">¥{{ Number(c.totalConsumed || 0).toFixed(2) }}</td>
            <td class="mono fz-xs">{{ (c.createTime || '').split('T')[0] || '—' }}</td>
            <td><span class="badge" :class="statusBadge(c.status)">{{ c.status === 1 ? '正常' : '冻结' }}</span></td>
            <td class="actions">
              <button class="btn btn-sm" @click="toggleStatus(c)">{{ c.status === 1 ? '冻结' : '解冻' }}</button>
            </td>
          </tr>
          <tr v-if="loading">
            <td colspan="8">
              <div class="loading-wrap">
                <div class="loading-dual-ring"></div>
                <span class="loading-text">加载中…</span>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && customers.length === 0"><td colspan="8" class="empty">暂无客户</td></tr>
        </tbody>
      </table>
    </div>

    <div class="pagination">
      <button class="btn" :disabled="page<=1" @click="prevPage">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button class="btn" :disabled="page>=totalPages" @click="nextPage">下一页</button>
      <label class="label-text">跳转</label>
      <input type="number" min="1" :max="totalPages" v-model.number="pageInput" class="input" style="width:72px" />
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
