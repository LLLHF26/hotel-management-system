<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { createUser, deleteUser, getUserList, updateUser, updateUserStatus } from '../services/userService'

const employees = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageInput = ref(1)
const size = ref(10)
const keyword = ref('')
const role = ref('')
const status = ref('')
const editing = ref(false)
const editMode = ref<'create' | 'edit'>('create')
const activeEmployee = ref<any>(null)
const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  role: ''
})

const roleOptions = [
  { label: '全部角色', value: '' },
  { label: '管理员', value: 'ADMIN' },
  { label: '前台', value: 'FRONT_DESK' },
  { label: '保洁员', value: 'CLEANER' }
]
const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '正常', value: '1' },
  { label: '禁用', value: '0' }
]

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

function formatStatus(value: number) {
  return value === 1 ? '正常' : '禁用'
}

async function loadEmployees() {
  const res = await getUserList({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
    role: role.value || undefined,
    status: status.value !== '' ? Number(status.value) : undefined
  })
  const data = res?.data ?? res
  employees.value = data.records || []
  total.value = data.total || 0
  page.value = data.page || page.value
  pageInput.value = page.value
}

function applyFilters() {
  page.value = 1
  pageInput.value = 1
  loadEmployees()
}

function openCreate() {
  editing.value = true
  editMode.value = 'create'
  activeEmployee.value = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.phone = ''
  form.email = ''
  form.role = ''
}

function openEdit(employee: any) {
  editing.value = true
  editMode.value = 'edit'
  activeEmployee.value = employee
  form.username = employee.username
  form.password = ''
  form.realName = employee.realName
  form.phone = employee.phone || ''
  form.email = employee.email || ''
  form.role = employee.role
}

function cancelEdit() {
  editing.value = false
}

async function saveEmployee() {
  if (!form.realName || !form.role || (editMode.value === 'create' && !form.username) || (editMode.value === 'create' && !form.password)) {
    alert('请补全姓名、角色，以及创建时必须填写用户名和密码')
    return
  }

  if (editMode.value === 'create') {
    await createUser({
      username: form.username,
      password: form.password,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      role: form.role
    })
  } else if (activeEmployee.value) {
    await updateUser(activeEmployee.value.id, {
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      role: form.role
    })
  }

  editing.value = false
  await loadEmployees()
}

async function toggleStatus(employee: any) {
  const nextStatus = employee.status === 1 ? 0 : 1
  await updateUserStatus(employee.id, nextStatus)
  await loadEmployees()
}

async function removeEmployee(employee: any) {
  if (!confirm(`确认删除员工 ${employee.username} 吗？`)) return
  await deleteUser(employee.id)
  await loadEmployees()
}

function prevPage() {
  if (page.value > 1) {
    page.value--
    loadEmployees()
  }
}

function nextPage() {
  if (page.value < totalPages.value) {
    page.value++
    loadEmployees()
  }
}

function gotoPage() {
  let target = Number(pageInput.value)
  if (Number.isNaN(target) || target < 1) target = 1
  if (target > totalPages.value) target = totalPages.value
  page.value = target
  loadEmployees()
}

function onSizeChange() {
  page.value = 1
  pageInput.value = 1
  loadEmployees()
}

onMounted(loadEmployees)
</script>

<template>
  <div class="page-wrap">
    <div class="page-header">
      <div>
        <h1>员工管理</h1>
        <p class="page-subtitle">管理员工账号与角色权限，支持新增、编辑和启用/禁用。</p>
      </div>
      <button class="primary-button" @click="openCreate">新增员工</button>
    </div>

    <div class="filters" style="margin-bottom:18px; display:flex; flex-wrap:wrap; gap:12px; align-items:center;">
      <input v-model="keyword" placeholder="用户名/姓名搜索" />
      <select v-model="role">
        <option v-for="option in roleOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
      </select>
      <select v-model="status">
        <option v-for="option in statusOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
      </select>
      <button class="primary-button" @click="applyFilters">查询</button>
      <div style="margin-left:auto; color:#6b7280">共 {{ total }} 条</div>
    </div>

    <div v-if="editing" class="form-card" style="margin-bottom:18px; padding:16px; background:#fff; border-radius:8px; box-shadow:var(--shadow);">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
        <div>
          <div style="font-size:16px; font-weight:600;">{{ editMode === 'create' ? '新增员工' : '编辑员工' }}</div>
          <div style="color:#6b7280; margin-top:4px;">填写员工账号信息后保存。</div>
        </div>
        <button class="primary-button" style="background:#64748b" @click="cancelEdit">取消</button>
      </div>
      <div style="display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:16px;">
        <div>
          <label>用户名</label>
          <input v-model="form.username" :disabled="editMode === 'edit'" placeholder="登录账号" style="width:100%;" />
        </div>
        <div>
          <label>姓名</label>
          <input v-model="form.realName" placeholder="真实姓名" style="width:100%;" />
        </div>
        <div>
          <label>手机号</label>
          <input v-model="form.phone" placeholder="手机号" style="width:100%;" />
        </div>
        <div>
          <label>邮箱</label>
          <input v-model="form.email" placeholder="邮箱" style="width:100%;" />
        </div>
        <div>
          <label>角色</label>
          <select v-model="form.role" style="width:100%;">
            <option value="">请选择角色</option>
            <option value="ADMIN">管理员</option>
            <option value="FRONT_DESK">前台</option>
            <option value="CLEANER">保洁员</option>
          </select>
        </div>
        <div v-if="editMode === 'create'">
          <label>密码</label>
          <input type="password" v-model="form.password" placeholder="登录密码" style="width:100%;" />
        </div>
      </div>
      <div style="margin-top:16px; display:flex; gap:12px;">
        <button class="primary-button" @click="saveEmployee">保存</button>
      </div>
    </div>

    <div class="table employee-table">
      <div class="row header">
        <div>用户名</div>
        <div>姓名</div>
        <div>手机号</div>
        <div>角色</div>
        <div>状态</div>
        <div>创建时间</div>
        <div>操作</div>
      </div>
      <div class="row" v-for="employee in employees" :key="employee.id">
        <div>{{ employee.username }}</div>
        <div>{{ employee.realName }}</div>
        <div>{{ employee.phone || '—' }}</div>
        <div>{{ employee.role }}</div>
        <div>{{ formatStatus(employee.status) }}</div>
        <div>{{ employee.createTime ? employee.createTime.split('T')[0] : '—' }}</div>
        <div style="display:flex; gap:8px; flex-wrap:wrap;">
          <button class="primary-button" style="background:#2563eb" @click="openEdit(employee)">编辑</button>
          <button class="primary-button" style="background:#f97316" @click="toggleStatus(employee)">
            {{ employee.status === 1 ? '禁用' : '启用' }}
          </button>
          <button class="primary-button" style="background:#ef4444" @click="removeEmployee(employee)">删除</button>
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
