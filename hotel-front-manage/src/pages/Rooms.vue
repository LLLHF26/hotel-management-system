<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { changeRoomStatus, createRoom, getRoomList, getRoomTypeList } from '../services/roomService'

const rooms = ref<any[]>([])
const roomTypes = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageInput = ref(1)
const size = ref(10)
const roomTypeId = ref('')
const floor = ref('')
const status = ref('')
const keyword = ref('')
const creatingRoom = ref(false)
const roomForm = reactive({
  roomNumber: '',
  roomTypeId: '',
  floor: '',
  description: ''
})

const statusOptions = ['空闲中', '预订中', '入住中', '待清洁中', '打扫中', '维修中']
const floorOptions = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

// 状态流转规则（与后端 RoomStatus.ALLOWED_TRANSITIONS 一致）
const ALLOWED_TRANSITIONS: Record<string, string[]> = {
  '空闲中': ['预订中', '维修中'],
  '预订中': ['入住中', '空闲中'],
  '入住中': ['待清洁中'],
  '待清洁中': ['打扫中'],
  '打扫中': ['空闲中'],
  '维修中': ['空闲中'],
}

// 状态变更相关
const changingRoom = ref<any>(null)
const targetStatus = ref('')
const maintenanceReason = ref('')

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

async function loadRoomTypes() {
  const res = await getRoomTypeList({ page: 1, size: 100 })
  const data = res?.data ?? res
  roomTypes.value = data.records || []
}

async function loadRooms() {
  const res = await getRoomList({
    page: page.value,
    size: size.value,
    roomTypeId: roomTypeId.value || undefined,
    status: status.value || undefined,
    floor: floor.value ? Number(floor.value) : undefined,
    keyword: keyword.value
  })
  const data = res?.data ?? res
  rooms.value = data.records || []
  total.value = data.total || 0
  page.value = data.page || page.value
  pageInput.value = page.value
}

function applyFilters() {
  page.value = 1
  pageInput.value = 1
  loadRooms()
}

function openCreate() {
  creatingRoom.value = true
  roomForm.roomNumber = ''
  roomForm.roomTypeId = ''
  roomForm.floor = ''
  roomForm.description = ''
}

function cancelCreate() {
  creatingRoom.value = false
}

async function saveRoom() {
  if (!roomForm.roomNumber || !roomForm.roomTypeId) {
    alert('请填写房间号和房型')
    return
  }

  await createRoom({
    roomNumber: roomForm.roomNumber,
    roomTypeId: Number(roomForm.roomTypeId),
    floor: roomForm.floor ? Number(roomForm.floor) : undefined,
    description: roomForm.description
  })
  creatingRoom.value = false
  await loadRooms()
}

function openChangeStatus(room: any) {
  changingRoom.value = room
  targetStatus.value = ''
  maintenanceReason.value = ''
}

function cancelChangeStatus() {
  changingRoom.value = null
  targetStatus.value = ''
  maintenanceReason.value = ''
}

async function confirmChangeStatus() {
  if (!changingRoom.value || !targetStatus.value) return
  if (targetStatus.value === '维修中' && !maintenanceReason.value.trim()) {
    alert('请填写维修原因')
    return
  }
  await changeRoomStatus(
    changingRoom.value.id,
    targetStatus.value,
    targetStatus.value === '维修中' ? maintenanceReason.value.trim() : undefined,
  )
  cancelChangeStatus()
  await loadRooms()
}

function prevPage() {
  if (page.value > 1) {
    page.value--
    loadRooms()
  }
}

function nextPage() {
  if (page.value < totalPages.value) {
    page.value++
    loadRooms()
  }
}

function gotoPage() {
  let target = Number(pageInput.value)
  if (Number.isNaN(target) || target < 1) target = 1
  if (target > totalPages.value) target = totalPages.value
  page.value = target
  loadRooms()
}

function onSizeChange() {
  page.value = 1
  pageInput.value = 1
  loadRooms()
}

onMounted(async () => {
  await loadRoomTypes()
  await loadRooms()
})
</script>

<template>
  <div class="page-wrap">
    <div class="page-header">
      <div>
        <h1>房间管理</h1>
        <p class="page-subtitle">管理酒店具体房间信息，包括房号、楼层、房型、状态和价格。</p>
      </div>
      <button class="primary-button" @click="openCreate">新增房间</button>
    </div>

    <div class="filters" style="margin-bottom:18px; display:flex; flex-wrap:wrap; gap:12px; align-items:center;">
      <select v-model="roomTypeId">
        <option value="">全部房型</option>
        <option v-for="type in roomTypes" :key="type.id" :value="String(type.id)">{{ type.name }}</option>
      </select>
      <select v-model="floor">
        <option value="">全部楼层</option>
        <option v-for="fl in floorOptions" :key="fl" :value="String(fl)">{{ fl }}楼</option>
      </select>
      <select v-model="status">
        <option value="">全部状态</option>
        <option v-for="item in statusOptions" :key="item" :value="item">{{ item }}</option>
      </select>
      <input v-model="keyword" placeholder="搜索房间号" />
      <button class="primary-button" @click="applyFilters">查询</button>
      <div style="margin-left:auto; color:#6b7280">共 {{ total }} 条</div>
    </div>

    <div v-if="creatingRoom" class="form-card" style="margin-bottom:18px; padding:16px; background:#fff; border-radius:8px; box-shadow:var(--shadow);">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
        <div>
          <div style="font-size:16px; font-weight:600;">新增房间</div>
          <div style="color:#6b7280; margin-top:4px;">填写房间编号、房型、楼层和备注后保存。</div>
        </div>
        <button class="primary-button" style="background:#64748b" @click="cancelCreate">取消</button>
      </div>
      <div style="display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:16px;">
        <div>
          <label>房间号</label>
          <input v-model="roomForm.roomNumber" placeholder="例如 401" style="width:100%;" />
        </div>
        <div>
          <label>房型</label>
          <select v-model="roomForm.roomTypeId" style="width:100%;">
            <option value="">请选择房型</option>
            <option v-for="type in roomTypes" :key="type.id" :value="String(type.id)">{{ type.name }}</option>
          </select>
        </div>
        <div>
          <label>楼层</label>
          <select v-model="roomForm.floor" style="width:100%;">
            <option value="">请选择楼层</option>
            <option v-for="fl in floorOptions" :key="fl" :value="String(fl)">{{ fl }}楼</option>
          </select>
        </div>
        <div>
          <label>备注</label>
          <input v-model="roomForm.description" placeholder="朝南、靠电梯等" style="width:100%;" />
        </div>
      </div>
      <div style="margin-top:16px; display:flex; gap:12px;">
        <button class="primary-button" @click="saveRoom">保存</button>
      </div>
    </div>

    <div class="table room-table">
      <div class="row header">
        <div>房间号</div>
        <div>房型</div>
        <div>楼层</div>
        <div>状态</div>
        <div>价格</div>
        <div>备注</div>
        <div>操作</div>
      </div>
      <div class="row" v-for="room in rooms" :key="room.id">
        <div>{{ room.roomNumber }}</div>
        <div>{{ room.roomTypeName }}</div>
        <div>{{ room.floor }}楼</div>
        <div>{{ room.status }}</div>
        <div>¥{{ room.price }}</div>
        <div>{{ room.description || '—' }}</div>
        <div>
          <button
            v-if="changingRoom?.id !== room.id"
            class="btn btn-sm btn-secondary"
            @click="openChangeStatus(room)"
          >
            变更状态
          </button>
          <div v-else class="status-change-inline">
            <select v-model="targetStatus" style="width:110px; padding:4px 6px; font-size:13px;">
              <option value="">请选择</option>
              <option
                v-for="opt in ALLOWED_TRANSITIONS[room.status] || []"
                :key="opt"
                :value="opt"
              >{{ opt }}</option>
            </select>
            <input
              v-if="targetStatus === '维修中'"
              v-model="maintenanceReason"
              placeholder="维修原因"
              style="width:110px; padding:4px 6px; font-size:13px;"
            />
            <button class="btn btn-sm btn-primary" @click="confirmChangeStatus" :disabled="!targetStatus">确认</button>
            <button class="btn btn-sm btn-secondary" @click="cancelChangeStatus">取消</button>
          </div>
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
