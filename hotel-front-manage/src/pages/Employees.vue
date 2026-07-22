<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { createUser, deleteUser, getUserList, updateUser, updateUserStatus } from '../services/userService'
import { useToast } from '../composables/useToast'

const toast = useToast()

const employees = ref<any[]>([])
const loading = ref(true)
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
const form = reactive({ username: '', password: '', realName: '', phone: '', email: '', role: '' })

const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '前台',   value: 'FRONT_DESK' },
  { label: '保洁员', value: 'CLEANER' },
]
const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '正常',     value: '1' },
  { label: '禁用',     value: '0' },
]

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

function roleLabel(r: string) { return roleOptions.find(o => o.value === r)?.label || r }
function rolePill(r: string) {
  return ({ ADMIN: 'pill-violet', FRONT_DESK: 'pill-blue', CLEANER: 'pill-emerald' } as Record<string,string>)[r] || 'pill-slate'
}
function statusBadge(v: number) { return v === 1 ? 'badge-success' : 'badge-danger' }

async function load() {
  loading.value = true
  const res = await getUserList({
    page: page.value, size: size.value, keyword: keyword.value,
    role: role.value || undefined,
    status: status.value !== '' ? Number(status.value) : undefined,
  })
  const data = res?.data ?? res
  employees.value = data.records || []
  total.value = data.total || 0
  page.value = data.page || page.value
  pageInput.value = page.value
  loading.value = false
}
onMounted(load)

function openCreate() { editing.value = true; editMode.value = 'create'; activeEmployee.value = null; form.username=''; form.password=''; form.realName=''; form.phone=''; form.email=''; form.role='' }
function openEdit(e: any) { editing.value = true; editMode.value = 'edit'; activeEmployee.value = e; form.username = e.username; form.password=''; form.realName = e.realName; form.phone = e.phone || ''; form.email = e.email || ''; form.role = e.role }
function cancelEdit() { editing.value = false }

async function save() {
  if (!form.realName || !form.role || (editMode.value === 'create' && (!form.username || !form.password))) {
    toast.error('请补全姓名、角色；创建时还需填写用户名和密码')
    return
  }
  try {
    if (editMode.value === 'create') {
      await createUser({ username: form.username, password: form.password, realName: form.realName, phone: form.phone, email: form.email, role: form.role })
    } else if (activeEmployee.value) {
      await updateUser(activeEmployee.value.id, { realName: form.realName, phone: form.phone, email: form.email, role: form.role })
    }
    editing.value = false
    await load()
    toast.success(editMode.value === 'create' ? '员工创建成功' : '员工更新成功')
  } catch (e: any) { toast.error(e?.message || '保存失败') }
}
async function toggleStatus(e: any) { try { await updateUserStatus(e.id, e.status === 1 ? 0 : 1); await load(); toast.success(e.status === 1 ? '已禁用员工' : '已启用员工') } catch (err: any) { toast.error(err?.message || '操作失败') } }
async function remove(e: any) { if (!confirm(`确认删除员工 ${e.username} 吗？`)) return; try { await deleteUser(e.id); await load(); toast.success('已删除员工') } catch (err: any) { toast.error(err?.message || '删除失败') } }

function prevPage() { if (page.value > 1) { page.value--; load() } }
function nextPage() { if (page.value < totalPages.value) { page.value++; load() } }
function gotoPage() { let t = Number(pageInput.value); if (Number.isNaN(t)||t<1) t=1; if (t>totalPages.value) t=totalPages.value; page.value=t; load() }
function onSizeChange() { page.value=1; pageInput.value=1; load() }
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">员工管理</h1>
        <p class="page-subtitle">管理员工账号与角色权限，支持新增、编辑、启用/禁用。</p>
      </div>
      <div class="page-actions">
        <button class="btn btn-primary" @click="openCreate">新增员工</button>
      </div>
    </div>

    <div v-if="editing" class="card mb-4">
      <div class="card-head">
        <div>
          <div class="card-title">{{ editMode === 'create' ? '新增员工' : '编辑员工' }}</div>
          <div class="card-sub">填写员工账号信息后保存。</div>
        </div>
        <div class="page-actions">
          <button class="btn" @click="cancelEdit">取消</button>
          <button class="btn btn-primary" @click="save">保存</button>
        </div>
      </div>
      <div class="card-body">
        <div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: var(--sp-4);">
          <div class="col-gap-2">
            <label class="label-text">用户名</label>
            <input class="input input-block" v-model="form.username" :disabled="editMode==='edit'" placeholder="登录账号" />
          </div>
          <div class="col-gap-2">
            <label class="label-text">姓名</label>
            <input class="input input-block" v-model="form.realName" placeholder="真实姓名" />
          </div>
          <div class="col-gap-2">
            <label class="label-text">手机号</label>
            <input class="input input-block" v-model="form.phone" placeholder="手机号" />
          </div>
          <div class="col-gap-2">
            <label class="label-text">邮箱</label>
            <input class="input input-block" v-model="form.email" placeholder="邮箱" />
          </div>
          <div class="col-gap-2">
            <label class="label-text">角色</label>
            <select class="select input-block" v-model="form.role">
              <option value="">请选择</option>
              <option v-for="o in roleOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
            </select>
          </div>
          <div v-if="editMode==='create'" class="col-gap-2">
            <label class="label-text">密码</label>
            <input class="input input-block" type="password" v-model="form.password" placeholder="登录密码" />
          </div>
        </div>
      </div>
    </div>

    <div class="filterbar">
      <input v-model="keyword" class="input" placeholder="用户名 / 姓名" />
      <select v-model="role" class="select">
        <option value="">全部角色</option>
        <option v-for="o in roleOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
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
            <th>用户名</th>
            <th>姓名</th>
            <th>手机号</th>
            <th>角色</th>
            <th>状态</th>
            <th>创建时间</th>
            <th class="actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in employees" :key="e.id">
            <td class="mono">{{ e.username }}</td>
            <td class="fw-600">{{ e.realName }}</td>
            <td class="mono">{{ e.phone || '—' }}</td>
            <td><span class="pill" :class="rolePill(e.role)">{{ roleLabel(e.role) }}</span></td>
            <td><span class="badge" :class="statusBadge(e.status)">{{ e.status === 1 ? '正常' : '禁用' }}</span></td>
            <td class="mono fz-xs">{{ (e.createTime || '').split('T')[0] || '—' }}</td>
            <td class="actions">
              <button class="btn btn-sm" @click="openEdit(e)">编辑</button>
              <button class="btn btn-sm" @click="toggleStatus(e)">{{ e.status === 1 ? '禁用' : '启用' }}</button>
              <button class="btn btn-sm btn-danger" @click="remove(e)">删除</button>
            </td>
          </tr>
          <tr v-if="loading">
            <td colspan="7">
              <div class="loading-wrap">
                <div class="loading-dual-ring"></div>
                <span class="loading-text">加载中…</span>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && employees.length === 0"><td colspan="7" class="empty">暂无员工</td></tr>
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
