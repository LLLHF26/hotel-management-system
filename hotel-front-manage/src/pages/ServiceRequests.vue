<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { getServiceRequests, handleServiceRequest } from '../services/serviceRequestService'
import { useToast } from '../composables/useToast'

const toast = useToast()

const list = ref<any[]>([])
const loading = ref(true)
const total = ref(0)
const page = ref(1); const pageInput = ref(1); const size = ref(10)
const statusFilter = ref('')

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const statusOptions = ['待处理', '已处理', '已取消']

async function load() {
  loading.value = true
  const res = await getServiceRequests({ page: page.value, size: size.value, status: statusFilter.value || undefined })
  const data = res?.data ?? res
  list.value = data.records || []
  total.value = data.total || 0
  page.value = data.page || page.value
  pageInput.value = page.value
  loading.value = false
}
onMounted(load)

function onSearch() { page.value = 1; pageInput.value = 1; load() }
function prevPage() { if (page.value > 1) { page.value--; load() } }
function nextPage() { if (page.value < totalPages.value) { page.value++; load() } }
function gotoPage() { let t = Number(pageInput.value); if (Number.isNaN(t) || t < 1) t = 1; if (t > totalPages.value) t = totalPages.value; page.value = t; load() }

function statusClass(s: string) {
  if (s === '待处理') return 'badge-warning'
  if (s === '已处理') return 'badge-success'
  return 'badge-neutral'
}

function formatTime(iso?: string) {
  if (!iso) return '-'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

// --- 处理 / 取消 弹窗 ---
const showHandle = ref(false)
const current = ref<any>(null)
const handleForm = reactive({ status: '已处理', handleRemark: '' })

function openHandle(item: any) {
  current.value = item
  handleForm.status = '已处理'
  handleForm.handleRemark = ''
  showHandle.value = true
}
function closeHandle() { showHandle.value = false; current.value = null }

async function submitHandle() {
  if (!current.value) return
  try {
    await handleServiceRequest(current.value.id, handleForm.status, handleForm.handleRemark || undefined)
    closeHandle()
    await load()
    toast.success(handleForm.status === '已处理' ? '已处理完成' : '已取消该服务呼叫')
  } catch (e: any) {
    toast.error(e?.message || '处理失败')
  }
}
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">服务呼叫</h1>
        <p class="page-subtitle">处理客户发起的人员呼叫（打扫 / 送物 / 维修等）。</p>
      </div>
    </div>

    <div class="card mb-4">
      <div style="display:flex; gap:8px; flex-wrap:wrap; align-items:center;">
        <select class="input" v-model="statusFilter" @change="onSearch">
          <option value="">全部状态</option>
          <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
        </select>
        <button class="btn" @click="onSearch">查询</button>
      </div>
    </div>

    <div class="card">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>客户</th>
            <th>房间</th>
            <th>类型</th>
            <th>备注</th>
            <th>状态</th>
            <th>处理信息</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="9" class="text-center text-muted">加载中…</td></tr>
          <tr v-else-if="!list.length"><td colspan="9" class="text-center text-muted">暂无服务呼叫</td></tr>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.customerName || '-' }}</td>
            <td>{{ item.roomNumber || '-' }}</td>
            <td>{{ item.type }}</td>
            <td class="text-ellipsis" :title="item.remark">{{ item.remark || '-' }}</td>
            <td><span :class="['badge', statusClass(item.status)]">{{ item.status }}</span></td>
            <td>
              <template v-if="item.handleRemark || item.handleTime">
                <div class="text-xs">{{ item.handleRemark || '（无备注）' }}</div>
                <div class="text-muted text-xs">{{ formatTime(item.handleTime) }}</div>
              </template>
              <span v-else class="text-muted">—</span>
            </td>
            <td>{{ formatTime(item.createTime) }}</td>
            <td>
              <button v-if="item.status === '待处理'" class="btn btn-sm btn-primary" @click="openHandle(item)">处理</button>
              <span v-else class="text-muted">已结束</span>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pager">
        <button class="btn btn-sm" :disabled="page <= 1" @click="prevPage">上一页</button>
        <span class="pager-info">第 {{ page }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
        <button class="btn btn-sm" :disabled="page >= totalPages" @click="nextPage">下一页</button>
        <input class="input" type="number" v-model.number="pageInput" @keyup.enter="gotoPage" style="width:64px" />
        <button class="btn btn-sm" @click="gotoPage">跳转</button>
      </div>
    </div>

    <div v-if="showHandle" class="card mb-4">
      <div class="card-head">
        <div>
          <div class="card-title">处理服务呼叫 #{{ current?.id }}</div>
          <div class="card-sub">{{ current?.type }} · {{ current?.roomNumber || '-' }} · {{ current?.customerName || '-' }}</div>
        </div>
        <div class="page-actions">
          <button class="btn" @click="closeHandle">取消</button>
          <button class="btn btn-primary" @click="submitHandle">提交</button>
        </div>
      </div>
      <div class="card-body">
        <div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--sp-4);">
          <div class="col-gap-2">
            <label class="label-text">处理结果</label>
            <select class="input input-block" v-model="handleForm.status">
              <option value="已处理">已处理</option>
              <option value="已取消">已取消</option>
            </select>
          </div>
          <div class="col-gap-2" style="grid-column: span 2;">
            <label class="label-text">处理备注</label>
            <textarea class="input input-block" rows="3" v-model="handleForm.handleRemark" placeholder="可选，填写处理说明"></textarea>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
