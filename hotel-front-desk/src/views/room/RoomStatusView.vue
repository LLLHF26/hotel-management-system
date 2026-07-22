<template>
  <div>
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>房态管理</h2>
        <div class="page-subtitle">实时查看所有房间状态</div>
      </div>
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
    <div class="content-card">
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
              @click="openCreate(room)"
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
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!floorGroups.length && !loading" description="暂无房间数据" />

    <!-- 房间详情抽屉 -->
    <el-drawer v-model="drawerVisible" :title="`房间 ${selectedRoom?.roomNumber} 详情`" size="460px">
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

        <!-- 预订时间线 -->
        <div class="timeline-section">
          <div class="section-header" style="margin-top:20px;margin-bottom:12px;">
            <el-icon :size="16"><Calendar /></el-icon>
            <h3>预订时间线</h3>
            <span class="timeline-hint">未来 {{ scheduleDays }} 天</span>
            <el-button link size="small" style="margin-left:auto" @click="loadSchedule">
              刷新
            </el-button>
          </div>

          <!-- 月份导航 + 图例 -->
          <div class="timeline-toolbar">
            <div class="timeline-months">
              <span v-for="m in scheduleMonths" :key="m.label"
                    :class="['month-chip', { active: m.active }]">{{ m.label }}</span>
            </div>
            <div class="timeline-legend">
              <span class="legend-dot legend--free"></span><span>空闲</span>
              <span class="legend-dot legend--booked"></span><span>已订</span>
              <span class="legend-dot legend--today"></span><span>今天</span>
            </div>
          </div>

          <!-- 星期头 -->
          <div class="cal-week">
            <span v-for="w in weekDays" :key="w" class="cal-head">{{ w }}</span>
          </div>

          <!-- 日历格子 -->
          <div v-loading="scheduleLoading" class="cal-grid">
            <div
              v-for="(cell, i) in scheduleCells"
              :key="i"
              :class="['cal-cell',
                       { 'is-today': cell.isToday,
                         'is-other': cell.isOtherMonth,
                         'is-booked': cell.isBooked,
                         'is-checkin': cell.isCheckin }]"
              :title="cell.tooltip"
            >
              <span class="cal-date">{{ cell.day }}</span>
              <span v-if="cell.isBooked" class="cal-badge"></span>
            </div>
          </div>

          <!-- 订单明细 -->
          <div v-if="scheduleOrders.length" class="schedule-orders">
            <div class="so-title">近期订单（{{ scheduleOrders.length }} 条）</div>
            <div v-for="o in scheduleOrders" :key="o.orderNo || o.id" class="so-row">
              <span class="so-status" :class="{ 'so--active': o.status === '已入住' }">
                {{ o.statusName || o.status }}
              </span>
              <span class="so-range">{{ formatDate(o.checkInDate) }} → {{ formatDate(o.checkOutDate) }}</span>
              <span class="so-guest">{{ o.customerName || '-' }}</span>
            </div>
          </div>
          <div v-else-if="!scheduleLoading" class="so-empty">暂无订单记录</div>

          <div style="margin-top: 16px; display: flex; gap: 10px;">
            <el-button v-if="selectedRoom.status === '待清洁中'" type="warning" @click="openAssign(selectedRoom)">
              指派打扫
            </el-button>
            <el-button v-if="selectedRoom.status === '空闲中'" type="primary" @click="openCreate(selectedRoom)">办理入住</el-button>
          </div>
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

    <!-- 新建预订 / 散客入住 -->
    <OrderCreateDialog ref="createRef" @success="loadDashboard" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, InfoFilled, Calendar } from '@element-plus/icons-vue'
import type { RoomVO } from '@/types'
import { getRoomDashboard, getCleanerList, assignCleaning, getRoomSchedule } from '@/api/room'
import type { OrderVO } from '@/types'
import { formatMoney, formatDate } from '@/utils/format'
import { ROOM_STATUS_OPTIONS } from '@/utils/constants'
import OrderCreateDialog from '@/components/order/OrderCreateDialog.vue'

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

/* ========== 预订时间线 ========== */
const scheduleDays = 30
const scheduleLoading = ref(false)
const scheduleOrders = ref<OrderVO[]>([])
const weekDays = ['一', '二', '三', '四', '五', '六', '日']

interface CalCell {
  day: number | null
  dateStr: string
  isToday: boolean
  isOtherMonth: boolean
  isBooked: boolean
  isCheckin: boolean
  tooltip: string
}

const scheduleCells = ref<CalCell[]>([])

const scheduleMonths = computed(() => {
  if (!scheduleCells.value.length) return []
  const months: { label: string; active: boolean }[] = []
  const seen = new Set<string>()
  for (const c of scheduleCells.value) {
    if (!c.dateStr || c.day == null) continue
    const m = c.dateStr.slice(0, 7) // YYYY-MM
    const mo = Number(m.split('-')[1])
    if (!seen.has(m)) {
      seen.add(m)
      months.push({ label: `${mo}月`, active: false })
    }
    // 标记含今天的月份为 active
    if (c.isToday) {
      const idx = months.findIndex(x => x.label === `${mo}月`)
      if (idx >= 0) months[idx].active = true
    }
  }
  return months
})

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
  loadSchedule()
}

async function loadSchedule() {
  if (!selectedRoom.value) return
  scheduleLoading.value = true
  try {
    const list = await getRoomSchedule(selectedRoom.value.id)
    scheduleOrders.value = list
    buildCalendarCells(list)
  } finally {
    scheduleLoading.value = false
  }
}

function buildCalendarCells(orders: OrderVO[]) {
  const today = new Date()
  const start = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  const cells: CalCell[] = []
  // 填充到周一（与 weekDays 对齐，weekDays[0]=周一）
  let offset = start.getDay() // 0=Sun 1=Mon..6=Sat, 转为 0=Mon..6=Sun
  if (offset === 0) {
    offset = 6
  } else {
    offset -= 1
  }
  for (let i = 0; i < offset; i++) {
    cells.push({ day: null, dateStr: '', isToday: false, isOtherMonth: true, isBooked: false, isCheckin: false, tooltip: '' })
  }

  // 构建已预订日期的快速查找 Set
  // 每个订单覆盖 [checkInDate, checkOutDate) 区间内的每一天
  const bookedMap = new Map<string, { status: string; orderNo: string; guest: string }>()
  for (const o of orders) {
    if (!o.checkInDate || !o.checkOutDate) continue
    const ci = new Date(o.checkInDate)
    const co = new Date(o.checkOutDate)
    for (let d = new Date(ci); d < co; d.setDate(d.getDate() + 1)) {
      const key = d.toISOString().slice(0, 10)
      bookedMap.set(key, { status: o.status || '', orderNo: o.orderNo || '', guest: o.customerName || '' })
    }
  }

  // 填充 scheduleDays 天
  for (let i = 0; i < scheduleDays; i++) {
    const d = new Date(start.getTime() + i * 86400000)
    const ds = d.toISOString().slice(0, 10)
    const info = bookedMap.get(ds)
    const isToday = ds === today.toISOString().slice(0, 10)
    cells.push({
      day: d.getDate(),
      dateStr: ds,
      isToday,
      isOtherMonth: false,
      isBooked: !!info,
      isCheckin: info?.status === '已入住',
      tooltip: info
        ? `${ds} — ${info.status}${info.guest ? ' (' + info.guest + ')' : ''}`
        : `${ds} — 空闲`,
    })
  }
  scheduleCells.value = cells
}

const createRef = ref<InstanceType<typeof OrderCreateDialog>>()

function openCreate(room: RoomVO) {
  createRef.value?.open({ roomTypeId: room.roomTypeId })
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

.floor-section:last-child {
  margin-bottom: 0;
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

/* ===== 预订时间线 ===== */
.timeline-section {
  margin-top: 4px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.timeline-hint {
  font-size: 12px;
  color: #94a3b8;
}

.timeline-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.timeline-months {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.month-chip {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
  background: #f1f5f9;
  color: #64748b;
}

.month-chip.active {
  background: #e0edff;
  color: #2563eb;
  font-weight: 600;
}

.timeline-legend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  display: inline-block;
  margin-left: 8px;
}

.legend-dot:first-child {
  margin-left: 0;
}

.legend--free {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
}

.legend--booked {
  background: #dbeafe;
  border: 1px solid #93c5fd;
}

.legend--today {
  background: #fde68a;
  border: 1px solid #f59e0b;
}

.cal-week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 4px;
}

.cal-head {
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
  padding: 2px 0;
}

.cal-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  min-height: 40px;
}

.cal-cell {
  position: relative;
  height: 38px;
  border-radius: 6px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #334155;
}

.cal-cell.is-other {
  background: transparent;
  border-color: transparent;
  color: #cbd5e1;
}

.cal-cell.is-booked {
  background: #dbeafe;
  border-color: #93c5fd;
  color: #1d4ed8;
  font-weight: 600;
}

.cal-cell.is-checkin {
  background: #fef3c7;
  border-color: #fcd34d;
  color: #b45309;
}

.cal-cell.is-today {
  box-shadow: inset 0 0 0 2px #f59e0b;
}

.cal-date {
  line-height: 1;
}

.cal-badge {
  position: absolute;
  right: 4px;
  bottom: 4px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #2563eb;
}

.cal-cell.is-checkin .cal-badge {
  background: #d97706;
}

.schedule-orders {
  margin-top: 14px;
  border-top: 1px dashed #e2e8f0;
  padding-top: 10px;
}

.so-title {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.so-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  padding: 6px 0;
  border-bottom: 1px solid #f1f5f9;
}

.so-status {
  flex: 0 0 auto;
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 10px;
  background: #f1f5f9;
  color: #64748b;
}

.so-status.so--active {
  background: #fef3c7;
  color: #b45309;
}

.so-range {
  flex: 1 1 auto;
  color: #334155;
}

.so-guest {
  flex: 0 0 auto;
  color: #94a3b8;
}

.so-empty {
  margin-top: 14px;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
  padding: 16px 0;
}
</style>
