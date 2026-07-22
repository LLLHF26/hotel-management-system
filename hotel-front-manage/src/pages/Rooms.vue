<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { changeRoomStatus, createRoom, getRoomList, getRoomTypeList, updateRoom } from '../services/roomService'
import { useToast } from '../composables/useToast'

const toast = useToast()

const rooms = ref<any[]>([])
const roomTypes = ref<any[]>([])
const loading = ref(true)
const total = ref(0); const page = ref(1); const pageInput = ref(1); const size = ref(10)
const roomTypeId = ref(''); const floor = ref(''); const status = ref(''); const keyword = ref('')
const creating = ref(false)
const roomForm = reactive({ roomNumber: '', roomTypeId: '', floor: '', price: '', description: '' })

const statusOptions = ['空闲中', '预订中', '入住中', '待清洁中', '打扫中', '维修中']
const floorOptions = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
const ALLOWED: Record<string, string[]> = {
  '空闲中': ['预订中', '维修中'], '预订中': ['入住中', '空闲中'], '入住中': ['待清洁中'],
  '待清洁中': ['打扫中'], '打扫中': ['空闲中'], '维修中': ['空闲中'],
}

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

async function loadTypes() {
  const res = await getRoomTypeList({ page: 1, size: 100 })
  const data = res?.data ?? res
  roomTypes.value = data.records || []
}
async function loadRooms() {
  loading.value = true
  const res = await getRoomList({
    page: page.value, size: size.value,
    roomTypeId: roomTypeId.value || undefined,
    status: status.value || undefined,
    floor: floor.value ? Number(floor.value) : undefined,
    keyword: keyword.value,
  })
  const data = res?.data ?? res
  rooms.value = data.records || []
  total.value = data.total || 0
  page.value = data.page || page.value
  pageInput.value = page.value
  loading.value = false
}
onMounted(async () => { await loadTypes(); await loadRooms() })

function applyFilters() { page.value = 1; pageInput.value = 1; loadRooms() }
function openCreate() { creating.value = true; roomForm.roomNumber=''; roomForm.roomTypeId=''; roomForm.floor=''; roomForm.price=''; roomForm.description='' }
function cancelCreate() { creating.value = false }
function onRoomTypeChange() {
  const type = roomTypes.value.find(t => String(t.id) === roomForm.roomTypeId)
  if (type && type.price !== undefined && roomForm.price === '') {
    roomForm.price = String(type.price)
  }
}
async function saveRoom() {
  if (!roomForm.roomNumber || !roomForm.roomTypeId) { toast.error('请填写房间号和房型'); return }
  const price = roomForm.price ? Number(roomForm.price) : undefined
  if (price !== undefined && (isNaN(price) || price < 0)) { toast.error('价格必须是非负数'); return }
  try {
    await createRoom({ roomNumber: roomForm.roomNumber, roomTypeId: Number(roomForm.roomTypeId), floor: roomForm.floor ? Number(roomForm.floor) : undefined, price, description: roomForm.description })
    creating.value = false
    await loadRooms()
    toast.success('房间创建成功')
  } catch (e: any) { toast.error(e?.message || '创建失败') }
}

const changing = ref<any>(null); const target = ref(''); const reason = ref('')
const editingPrice = ref<any>(null); const newPrice = ref('')
function openChange(r: any) { changing.value = r; target.value = ''; reason.value = ''; editingPrice.value = null }
function cancelChange() { changing.value = null; target.value = ''; reason.value = '' }
function openEditPrice(r: any) { editingPrice.value = r; newPrice.value = String(r.price ?? ''); changing.value = null; target.value = ''; reason.value = '' }
function cancelEditPrice() { editingPrice.value = null; newPrice.value = '' }
async function confirmEditPrice() {
  if (!editingPrice.value) return
  const price = newPrice.value ? Number(newPrice.value) : undefined
  if (price === undefined || isNaN(price) || price < 0) { toast.error('价格必须是非负数'); return }
  try {
    const res = await updateRoom(editingPrice.value.id, { price })
    if (res?.code !== 200) throw new Error(res?.msg || '修改失败')
    editingPrice.value = null
    newPrice.value = ''
    await loadRooms()
    toast.success('价格已更新')
  } catch (e: any) { toast.error(e?.message || '价格更新失败') }
}
async function confirmChange() {
  if (!changing.value || !target.value) return
  if (target.value === '维修中' && !reason.value.trim()) { toast.error('请填写维修原因'); return }
  try {
    await changeRoomStatus(changing.value.id, target.value, target.value === '维修中' ? reason.value.trim() : undefined)
    cancelChange(); await loadRooms()
    toast.success('房态已更新')
  } catch (e: any) { toast.error(e?.message || '状态变更失败') }
}

function statusBadge(s: string) {
  return ({ '空闲中': 'badge-success', '预订中': 'badge-info', '入住中': 'badge-accent', '待清洁中': 'badge-warning', '打扫中': 'badge-accent', '维修中': 'badge-danger' } as Record<string,string>)[s] || 'badge-neutral'
}

function prevPage() { if (page.value > 1) { page.value--; loadRooms() } }
function nextPage() { if (page.value < totalPages.value) { page.value++; loadRooms() } }
function gotoPage() { let t = Number(pageInput.value); if (Number.isNaN(t)||t<1) t=1; if (t>totalPages.value) t=totalPages.value; page.value=t; loadRooms() }
function onSizeChange() { page.value=1; pageInput.value=1; loadRooms() }
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">房间管理</h1>
        <p class="page-subtitle">管理酒店具体房间信息，包括房号、楼层、房型、状态和价格。</p>
      </div>
      <div class="page-actions">
        <button class="btn btn-primary" @click="openCreate">新增房间</button>
      </div>
    </div>

    <div v-if="creating" class="card mb-4">
      <div class="card-head">
        <div>
          <div class="card-title">新增房间</div>
          <div class="card-sub">填写房间编号、房型、楼层、价格和备注</div>
        </div>
        <div class="page-actions">
          <button class="btn" @click="cancelCreate">取消</button>
          <button class="btn btn-primary" @click="saveRoom">保存</button>
        </div>
      </div>
      <div class="card-body">
        <div style="display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: var(--sp-4);">
          <div class="col-gap-2"><label class="label-text">房间号</label><input class="input input-block" v-model="roomForm.roomNumber" placeholder="例如 401" /></div>
          <div class="col-gap-2"><label class="label-text">房型</label>
            <select class="select input-block" v-model="roomForm.roomTypeId" @change="onRoomTypeChange">
              <option value="">请选择</option>
              <option v-for="t in roomTypes" :key="t.id" :value="String(t.id)">{{ t.name }}</option>
            </select>
          </div>
          <div class="col-gap-2"><label class="label-text">楼层</label>
            <select class="select input-block" v-model="roomForm.floor">
              <option value="">请选择</option>
              <option v-for="f in floorOptions" :key="f" :value="String(f)">{{ f }} 楼</option>
            </select>
          </div>
          <div class="col-gap-2"><label class="label-text">价格（元/晚）</label><input class="input input-block" type="number" min="0" step="0.01" v-model="roomForm.price" placeholder="例如 588" /></div>
          <div class="col-gap-2"><label class="label-text">备注</label><input class="input input-block" v-model="roomForm.description" placeholder="朝南、靠电梯等" /></div>
        </div>
      </div>
    </div>

    <div class="filterbar">
      <select v-model="roomTypeId" class="select">
        <option value="">全部房型</option>
        <option v-for="t in roomTypes" :key="t.id" :value="String(t.id)">{{ t.name }}</option>
      </select>
      <select v-model="floor" class="select">
        <option value="">全部楼层</option>
        <option v-for="f in floorOptions" :key="f" :value="String(f)">{{ f }} 楼</option>
      </select>
      <select v-model="status" class="select">
        <option value="">全部状态</option>
        <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
      </select>
      <input v-model="keyword" class="input" placeholder="搜索房间号" />
      <button class="btn btn-primary" @click="applyFilters">查询</button>
      <span class="filterbar-spacer"></span>
      <span class="text-muted fz-sm">共 {{ total }} 条</span>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>房间号</th>
            <th>房型</th>
            <th class="num">楼层</th>
            <th>状态</th>
            <th class="num">价格</th>
            <th>备注</th>
            <th class="actions" style="min-width: 280px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rooms" :key="r.id">
            <td class="fw-600">{{ r.roomNumber }}</td>
            <td>{{ r.roomTypeName }}</td>
            <td class="num">{{ r.floor }} 楼</td>
            <td><span class="badge" :class="statusBadge(r.status)">{{ r.status }}</span></td>
            <td class="num">¥{{ r.price }}</td>
            <td class="text-muted fz-sm">{{ r.description || '—' }}</td>
            <td class="actions" style="min-width: 280px;">
              <template v-if="changing?.id !== r.id && editingPrice?.id !== r.id">
                <button class="btn btn-sm" @click="openChange(r)">变更状态</button>
                <button class="btn btn-sm" @click="openEditPrice(r)">修改价格</button>
              </template>
              <template v-else-if="changing?.id === r.id">
                <select v-model="target" class="select input-sm" style="width: 110px;">
                  <option value="">请选择</option>
                  <option v-for="o in ALLOWED[r.status] || []" :key="o" :value="o">{{ o }}</option>
                </select>
                <input v-if="target==='维修中'" v-model="reason" placeholder="维修原因" class="input input-sm" style="width: 120px;" />
                <button class="btn btn-sm btn-primary" :disabled="!target" @click="confirmChange">确认</button>
                <button class="btn btn-sm" @click="cancelChange">取消</button>
              </template>
              <template v-else-if="editingPrice?.id === r.id">
                <input v-model="newPrice" type="number" min="0" step="0.01" placeholder="价格" class="input input-sm" style="width: 100px;" />
                <button class="btn btn-sm btn-primary" @click="confirmEditPrice">确认</button>
                <button class="btn btn-sm" @click="cancelEditPrice">取消</button>
              </template>
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
          <tr v-if="!loading && rooms.length === 0"><td colspan="7" class="empty">暂无房间</td></tr>
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
        <option :value="10">10 / 页</option><option :value="20">20 / 页</option><option :value="50">50 / 页</option>
      </select>
      <span class="pagination-spacer"></span>
      <span class="text-muted fz-sm">共 {{ total }} 条</span>
    </div>
  </div>
</template>
