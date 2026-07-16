<template>
  <div>
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>房态看板</h2>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filterFloor" placeholder="楼层筛选" clearable style="width: 130px" @change="loadDashboard">
        <el-option v-for="f in floors" :key="f" :label="`${f} 楼`" :value="f" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="房间状态" clearable style="width: 140px">
        <el-option v-for="s in ROOM_STATUS_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索房间号..."
        clearable
        style="width: 180px"
        :prefix-icon="Search"
        @keyup.enter="applyFilter"
      />
      <el-button type="primary" @click="applyFilter">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="loadDashboard">刷新</el-button>
    </div>

    <!-- 房间汇总 -->
    <div v-if="summaryText" class="summary-bar">
      <el-icon><InfoFilled /></el-icon>
      {{ summaryText }}
    </div>

    <!-- 楼层分组 -->
    <div v-for="group in floorGroups" :key="group.floor" class="floor-section">
      <div class="floor-header">
        <span class="floor-title">{{ group.floor }}F</span>
        <span class="floor-count">{{ group.rooms.length }} 间</span>
      </div>
      <div class="room-grid">
        <div
          v-for="room in group.rooms"
          :key="room.id"
          class="room-card"
          :class="`room-status-${room.status}`"
          @click="openDrawer(room)"
        >
          <div class="room-top">
            <div class="room-num">{{ room.roomNumber }}</div>
            <el-tag size="small" :type="statusTagType(room.status)" effect="light" round>{{ room.status }}</el-tag>
          </div>
          <div class="room-type">{{ room.roomTypeName }}</div>
          <div v-if="room.status === '空闲中' && room.price" class="room-price">&yen;{{ formatMoney(room.price) }}/晚</div>
          <div v-if="room.cleanerName" class="room-meta">
            <el-icon><User /></el-icon> {{ room.cleanerName }}
          </div>
          <div class="room-actions" @click.stop>
            <el-button
              v-if="room.status === '空闲中'"
              size="small"
              type="primary"
              round
              @click="goOrders"
            >
              办理入住
            </el-button>
            <el-button
              v-if="room.status === '待清洁中'"
              size="small"
              type="warning"
              round
              @click="openAssign(room)"
            >
              指派打扫
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!floorGroups.length && !loading" description="暂无房间数据" />

    <!-- 房间详情抽屉 -->
    <el-drawer v-model="drawerVisible" :title="`房间 ${selectedRoom?.roomNumber} 详情`" size="400px">
      <template v-if="selectedRoom">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="房型">{{ selectedRoom.roomTypeName }}</el-descriptions-item>
          <el-descriptions-item label="楼层">{{ selectedRoom.floor }}F</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(selectedRoom.status)" effect="light">{{ selectedRoom.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="价格">&yen;{{ formatMoney(selectedRoom.price) }}</el-descriptions-item>
          <el-descriptions-item v-if="selectedRoom.cleanerName" label="保洁员">{{ selectedRoom.cleanerName }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px; display: flex; gap: 10px;">
          <el-button v-if="selectedRoom.status === '待清洁中'" type="warning" @click="openAssign(selectedRoom)">
            指派打扫
          </el-button>
          <el-button v-if="selectedRoom.status === '空闲中'" type="primary" @click="goOrders">办理入住</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 指派保洁弹窗 -->
    <el-dialog v-model="assignVisible" title="指派保洁任务" width="420px">
      <el-select v-model="selectedCleanerId" placeholder="请选择保洁员" style="width: 100%" filterable>
        <el-option
          v-for="c in cleaners"
          :key="c.id"
          :label="`${c.realName}${c.currentTaskCount != null ? ` (当前任务${c.currentTaskCount}个)` : ''}`"
          :value="c.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignLoading" @click="submitAssign">确认指派</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, InfoFilled } from '@element-plus/icons-vue'
import type { RoomVO } from '@/types'
import { getRoomDashboard, getCleanerList, assignCleaning } from '@/api/room'
import { formatMoney } from '@/utils/format'
import { ROOM_STATUS_OPTIONS } from '@/utils/constants'

const router = useRouter()
const loading = ref(false)
const allRooms = ref<RoomVO[]>([])
const summary = ref<Record<string, number>>({})
const total = ref(0)
const filterFloor = ref<number>()
const filterStatus = ref('')
const keyword = ref('')
const drawerVisible = ref(false)
const selectedRoom = ref<RoomVO | null>(null)
const assignVisible = ref(false)
const assignLoading = ref(false)
const assignRoomId = ref<number>()
const selectedCleanerId = ref<number>()
const cleaners = ref<{ id: number; realName: string; currentTaskCount?: number }[]>([])

const floors = computed(() => {
  const set = new Set(allRooms.value.map((r) => r.floor).filter((f) => f != null) as number[])
  return [...set].sort((a, b) => a - b)
})

const filteredRooms = computed(() => {
  return allRooms.value.filter((r) => {
    if (filterFloor.value && r.floor !== filterFloor.value) return false
    if (filterStatus.value && r.status !== filterStatus.value) return false
    if (keyword.value && !r.roomNumber.includes(keyword.value)) return false
    return true
  })
})

const floorGroups = computed(() => {
  const map = new Map<number, RoomVO[]>()
  for (const room of filteredRooms.value) {
    const f = room.floor ?? 0
    if (!map.has(f)) map.set(f, [])
    map.get(f)!.push(room)
  }
  return [...map.entries()]
    .sort(([a], [b]) => a - b)
    .map(([floor, rooms]) => ({ floor, rooms }))
})

const summaryText = computed(() => {
  if (!total.value) return ''
  const parts = Object.entries(summary.value)
    .filter(([, n]) => n > 0)
    .map(([k, v]) => `${k}(${v})`)
  return `共 ${total.value} 间 · ${parts.join(' ') || '全部正常'}`
})

onMounted(() => loadDashboard())

async function loadDashboard() {
  loading.value = true
  try {
    const data = await getRoomDashboard(filterFloor.value)
    allRooms.value = data.rooms
    summary.value = data.summary
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function applyFilter() { /* reactive via computed */ }

function statusTagType(status: string) {
  const map: Record<string, string> = {
    空闲中: 'success',
    预订中: '',
    入住中: 'warning',
    待清洁中: 'danger',
    打扫中: 'info',
    维修中: 'danger',
  }
  return (map[status] || 'info') as '' | 'success' | 'warning' | 'info' | 'danger'
}

function openDrawer(room: RoomVO) {
  selectedRoom.value = room
  drawerVisible.value = true
}

function goOrders() {
  router.push('/front-desk/orders')
}

async function openAssign(room: RoomVO) {
  assignRoomId.value = room.id
  selectedCleanerId.value = undefined
  cleaners.value = await getCleanerList()
  assignVisible.value = true
}

async function submitAssign() {
  if (!assignRoomId.value || !selectedCleanerId.value) {
    ElMessage.warning('请选择保洁员')
    return
  }
  assignLoading.value = true
  try {
    await assignCleaning(assignRoomId.value, selectedCleanerId.value)
    ElMessage.success('已成功指派保洁任务')
    assignVisible.value = false
    drawerVisible.value = false
    await loadDashboard()
  } finally {
    assignLoading.value = false
  }
}
</script>

<style scoped>
.summary-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px 18px;
  background: linear-gradient(135deg, #eff6ff 0%, #f0fdf4 100%);
  border-radius: 10px;
  font-size: 13px;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.floor-section {
  margin-bottom: 24px;
}

.floor-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 14px;
}

.floor-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #1e293b;
}

.floor-count {
  font-size: 12px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 10px;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(185px, 1fr));
  gap: 14px;
}

.room-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid var(--fd-border);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.room-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.room-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.room-num {
  font-size: 19px;
  font-weight: 700;
  color: #111827;
}

.room-type {
  font-size: 12px;
  color: #64748b;
  margin: 4px 0 10px;
}

.room-price,
.room-meta {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.room-actions {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}
</style>
