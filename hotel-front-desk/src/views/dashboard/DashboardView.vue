<template>
  <div v-loading="loading" class="dashboard-page">
    <div class="dashboard-layout">
      <!-- ============ 左侧主内容 ============ -->
      <div class="dashboard-main">
        <!-- 1. 欢迎横幅 -->
        <div class="welcome-banner">
          <div class="welcome-banner__text">
            <h1 class="welcome-banner__title">{{ greeting }}，前台接待！</h1>
            <p class="welcome-banner__subtitle">用专业与微笑，迎接每一位旅客</p>
          </div>
        </div>

        <!-- 2. 统计卡片（横向 4 个） -->
        <div class="stat-cards">
          <div class="stat-card">
            <div class="stat-card__icon stat-card__icon--green">
              <el-icon :size="22"><UserFilled /></el-icon>
            </div>
            <div class="stat-card__body">
              <div class="stat-card__label">今日入住</div>
              <div class="stat-card__value">{{ todayCheckIn }}<small> 人</small></div>
              <div v-if="checkInTrend" :class="['stat-card__trend', checkInTrend.startsWith('-') ? 'trend-down' : 'trend-up']"><el-icon><component :is="checkInTrend.startsWith('-') ? Bottom : Top" /></el-icon>{{ checkInTrend }}</div>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-card__icon stat-card__icon--blue">
              <el-icon :size="22"><Switch /></el-icon>
            </div>
            <div class="stat-card__body">
              <div class="stat-card__label">今日退房</div>
              <div class="stat-card__value">{{ todayCheckOut }}<small> 人</small></div>
              <div v-if="checkOutTrend" :class="['stat-card__trend', checkOutTrend.startsWith('-') ? 'trend-down' : 'trend-up']"><el-icon><component :is="checkOutTrend.startsWith('-') ? Bottom : Top" /></el-icon>{{ checkOutTrend }}</div>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-card__icon stat-card__icon--orange">
              <el-icon :size="22"><Tickets /></el-icon>
            </div>
            <div class="stat-card__body">
              <div class="stat-card__label">今日预订</div>
              <div class="stat-card__value">{{ todayBookings }}<small> 单</small></div>
              <div v-if="bookingTrend" :class="['stat-card__trend', bookingTrend.startsWith('-') ? 'trend-down' : 'trend-up']"><el-icon><component :is="bookingTrend.startsWith('-') ? Bottom : Top" /></el-icon>{{ bookingTrend }}</div>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-card__icon stat-card__icon--gold">
              <el-icon :size="22"><Money /></el-icon>
            </div>
            <div class="stat-card__body">
              <div class="stat-card__label">营业收入</div>
              <div class="stat-card__value value--primary">&yen;{{ formatRevenue(todayRevenue) }}</div>
              <div v-if="revenueTrend" :class="['stat-card__trend', revenueTrend.startsWith('-') ? 'trend-down' : 'trend-up']"><el-icon><component :is="revenueTrend.startsWith('-') ? Bottom : Top" /></el-icon>{{ revenueTrend }}</div>
            </div>
          </div>
        </div>

        <!-- 3. 房态概览 -->
        <div class="content-card room-status-card">
          <div class="card-header">
            <div class="card-header__left">
              <h3 class="card-title">房态概览</h3>
              <div class="room-legend">
                <span class="legend-dot legend-dot--empty"></span>空房
                <span class="legend-dot legend-dot--occupied"></span>在住
                <span class="legend-dot legend-dot--reserved"></span>已预订
                <span class="legend-dot legend-dot--maintenance"></span>维修中
              </div>
            </div>
            <span class="link-more" @click="$router.push('/front-desk/room-status')">更多 &gt;</span>
          </div>

          <div class="room-grid">
            <div
              v-for="room in rooms"
              :key="room.id"
              class="room-cell"
              :class="roomStatusClass(room.status)"
            >
              <div class="room-cell__no">{{ room.roomNumber }}</div>
              <div class="room-cell__type">{{ room.roomTypeName || '标准间' }}</div>
              <span class="room-cell__tag">{{ roomStatusLabel(room.status) }}</span>
            </div>
            <div v-if="rooms.length === 0" class="room-empty">暂无房间数据</div>
          </div>
        </div>

        <!-- 4. 最新订单 -->
        <div class="content-card order-card">
          <div class="card-header">
            <h3 class="card-title">最新订单</h3>
            <span class="link-more" @click="$router.push('/front-desk/orders')">更多 &gt;</span>
          </div>

          <el-table :data="orders" style="width: 100%" :show-overflow-tooltip="false">
            <el-table-column prop="orderNo" label="订单号" min-width="150" />
            <el-table-column prop="customerName" label="客人姓名" min-width="100" />
            <el-table-column prop="roomTypeName" label="房型" min-width="110" />
            <el-table-column label="入住日期" min-width="110">
              <template #default="{ row }">{{ formatDate(row.checkInDate) }}</template>
            </el-table-column>
            <el-table-column label="退房日期" min-width="110">
              <template #default="{ row }">{{ formatDate(row.checkOutDate) }}</template>
            </el-table-column>
            <el-table-column label="状态" min-width="90">
              <template #default="{ row }">
                <span class="order-pill" :class="orderStatusClass(row)">{{ orderStatusLabel(row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="80" align="center">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="$router.push(`/front-desk/orders/${row.id}`)">
                  查看
                </el-button>
              </template>
            </el-table-column>
            <template #empty>
              <span class="order-empty">暂无订单数据</span>
            </template>
          </el-table>
        </div>
      </div>

      <!-- ============ 右侧边栏 ============ -->
      <aside class="dashboard-sidebar">
        <!-- 1. 日历 -->
        <div class="content-card calendar-card">
          <div class="calendar-header">
            <span class="calendar-title">{{ calYear }}年{{ calMonth + 1 }}月</span>
            <div class="calendar-nav">
              <el-button text :icon="ArrowLeft" @click="prevMonth" />
              <el-button text :icon="ArrowRight" @click="nextMonth" />
            </div>
          </div>
          <div class="calendar-week">
            <span v-for="w in weekLabels" :key="w" class="calendar-week__day">{{ w }}</span>
          </div>
          <div class="calendar-grid">
            <span
              v-for="(cell, i) in calendarCells"
              :key="i"
              class="calendar-cell"
              :class="{ 'is-empty': cell === null, 'is-today': isToday(cell) }"
            >
              {{ cell ?? '' }}
            </span>
          </div>
        </div>

        <!-- 2. 快捷操作 -->
        <div class="content-card quick-card">
          <h3 class="card-title">快捷操作</h3>
          <div class="quick-grid">
            <div class="quick-item" @click="$router.push('/front-desk/room-status')">
              <div class="quick-item__icon quick-item__icon--green"><el-icon :size="22"><Key /></el-icon></div>
              <span>办理入住</span>
            </div>
            <div class="quick-item" @click="$router.push('/front-desk/room-status')">
              <div class="quick-item__icon quick-item__icon--red"><el-icon :size="22"><Switch /></el-icon></div>
              <span>办理退房</span>
            </div>
            <div class="quick-item" @click="$router.push('/front-desk/orders')">
              <div class="quick-item__icon quick-item__icon--blue"><el-icon :size="22"><Tickets /></el-icon></div>
              <span>新建预订</span>
            </div>
            <div class="quick-item" @click="$router.push('/front-desk/billing')">
              <div class="quick-item__icon quick-item__icon--orange"><el-icon :size="22"><Money /></el-icon></div>
              <span>账单查询</span>
            </div>
          </div>
        </div>

        <!-- 3. 系统通知 -->
        <div class="content-card notice-card">
          <div class="card-header">
            <h3 class="card-title">系统通知</h3>
            <span class="link-more" @click="$router.push('/front-desk/notifications')">更多 &gt;</span>
          </div>
          <el-timeline v-if="alerts.length" class="notice-timeline">
            <el-timeline-item
              v-for="item in alerts"
              :key="item.id"
              :timestamp="formatDate(item.trigger_time, 'YYYY-MM-DD HH:mm')"
              placement="top"
              :color="item.is_read ? '#c1c9d4' : 'var(--fd-danger)'"
            >
              <div class="notice-content">{{ item.content }}</div>
            </el-timeline-item>
          </el-timeline>
          <div v-else class="notice-empty">暂无系统通知</div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  UserFilled,
  Switch,
  Tickets,
  Money,
  Key,
  ArrowLeft,
  ArrowRight,
  Top,
  Bottom,
} from '@element-plus/icons-vue'
import { getRevenueSummary } from '@/api/finance'
import { getAlertStatus } from '@/api/alert'
import { getRoomDashboard } from '@/api/room'
import { getOrderList } from '@/api/order'
import { formatDate } from '@/utils/format'
import type { AlertItem, OrderVO, RoomVO } from '@/types'

const loading = ref(false)

/* ---------- 统计卡片数据 ---------- */
const todayCheckIn = ref(0)
const todayCheckOut = ref(0)
const todayBookings = ref(0)
const todayRevenue = ref(0)
const checkInTrend = ref('')
const checkOutTrend = ref('')
const bookingTrend = ref('')
const revenueTrend = ref('')

/* ---------- 房态 / 订单 / 通知 ---------- */
const rooms = ref<RoomVO[]>([])
const orders = ref<OrderVO[]>([])
const alerts = ref<AlertItem[]>([])

/* ---------- 日历 ---------- */
const weekLabels = ['日', '一', '二', '三', '四', '五', '六']
const now = new Date()
const calYear = ref(now.getFullYear())
const calMonth = ref(now.getMonth())
const calendarCells = ref<(number | null)[]>([])

function buildCalendar() {
  const y = calYear.value
  const m = calMonth.value
  const startWeekday = new Date(y, m, 1).getDay()
  const daysInMonth = new Date(y, m + 1, 0).getDate()
  const cells: (number | null)[] = []
  for (let i = 0; i < startWeekday; i++) cells.push(null)
  for (let d = 1; d <= daysInMonth; d++) cells.push(d)
  while (cells.length % 7 !== 0) cells.push(null)
  calendarCells.value = cells
}

function isToday(cell: number | null): boolean {
  if (cell === null) return false
  return (
    cell === now.getDate() &&
    calMonth.value === now.getMonth() &&
    calYear.value === now.getFullYear()
  )
}

function prevMonth() {
  if (calMonth.value === 0) {
    calMonth.value = 11
    calYear.value -= 1
  } else {
    calMonth.value -= 1
  }
  buildCalendar()
}

function nextMonth() {
  if (calMonth.value === 11) {
    calMonth.value = 0
    calYear.value += 1
  } else {
    calMonth.value += 1
  }
  buildCalendar()
}

/* ---------- 问候语 ---------- */
const greeting = (() => {
  const h = now.getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})()

/* ---------- 房态状态映射 ---------- */
function roomStatusClass(status: string): string {
  switch (status) {
    case '空闲中':
      return 'rs-empty'
    case '入住中':
      return 'rs-occupied'
    case '预订中':
      return 'rs-reserved'
    case '维修中':
    case '待清洁中':
    case '打扫中':
      return 'rs-maintenance'
    default:
      return 'rs-empty'
  }
}

function roomStatusLabel(status: string): string {
  switch (status) {
    case '空闲中':
      return '空房'
    case '入住中':
      return '在住'
    case '预订中':
      return '已预订'
    case '维修中':
      return '维修中'
    case '待清洁中':
      return '待清洁'
    case '打扫中':
      return '打扫中'
    default:
      return status || '空房'
  }
}

/* ---------- 订单状态映射 ---------- */
function orderStatusClass(row: OrderVO): string {
  const status = (row.status || '').toUpperCase()
  const name = row.statusName || ''
  if (status.includes('IN') || name.includes('住')) return 'order-pill--occupied'
  if (status.includes('RESERV') || name.includes('预订')) return 'order-pill--reserved'
  return 'order-pill--default'
}

function orderStatusLabel(row: OrderVO): string {
  return row.statusName || row.status || '未知'
}

/* ---------- 工具函数 ---------- */
function formatRevenue(val: number): string {
  return (val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

function computeTrend(todayVal: number | string | undefined | null, yesterdayVal: number | string | undefined | null): string {
  const t = todayVal == null ? 0 : Number(todayVal)
  const y = yesterdayVal == null ? 0 : Number(yesterdayVal)
  if (y === 0) return t > 0 ? '新高' : ''
  const rate = ((t - y) / y) * 100
  const sign = rate >= 0 ? '' : '-'
  return `${sign}${Math.abs(rate).toFixed(0)}%`
}


/* ---------- 数据加载 ---------- */
onMounted(() => {
  buildCalendar()
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const [summary, alertData, roomData, orderData] = await Promise.all([
      getRevenueSummary().catch(() => null),
      getAlertStatus().catch(() => ({ alerts: [], unread_count: 0 })),
      getRoomDashboard().catch(() => ({ rooms: [] })),
      getOrderList({ page: 1, size: 5 }).catch(() => ({ records: [] })),
    ])

    // 统计卡片：使用真实数据覆盖默认值（graceful 降级）
    if (summary?.today) {
      const t = summary.today
      if (t.checkInCount != null) todayCheckIn.value = t.checkInCount
      if (t.checkOutCount != null) todayCheckOut.value = t.checkOutCount
      if (t.orderCount != null) todayBookings.value = t.orderCount
      if (t.totalRevenue != null) todayRevenue.value = Number(t.totalRevenue)
    }
    if (summary?.yesterday && summary?.today) {
      const y = summary.yesterday
      const t = summary.today
      checkInTrend.value = computeTrend(t.checkInCount, y.checkInCount)
      checkOutTrend.value = computeTrend(t.checkOutCount, y.checkOutCount)
      bookingTrend.value = computeTrend(t.orderCount, y.orderCount)
      revenueTrend.value = computeTrend(
        t.totalRevenue == null ? null : Number(t.totalRevenue),
        y.totalRevenue == null ? null : Number(y.totalRevenue),
      )
    }

    // 系统通知
    alerts.value = (alertData?.alerts || []).slice(0, 5)

    // 房态概览
    rooms.value = roomData?.rooms || []

    // 最新订单
    orders.value = orderData?.records || []
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dashboard-page {
  max-width: 1440px;
}

/* ====== 两栏布局 ====== */
.dashboard-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.dashboard-main {
  flex: 3;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dashboard-sidebar {
  flex: 1;
  min-width: 280px;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

@media (max-width: 1080px) {
  .dashboard-layout {
    flex-direction: column;
  }
  .dashboard-sidebar {
    max-width: none;
    width: 100%;
  }
}

/* ====== 欢迎横幅 ====== */
.welcome-banner {
  position: relative;
  height: 180px;
  border-radius: 16px;
  overflow: hidden;
  background-image: url('https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200&auto=format&fit=crop');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(17, 24, 39, 0.78) 0%, rgba(17, 24, 39, 0.45) 55%, rgba(17, 24, 39, 0.1) 100%);
}

.welcome-banner__text {
  position: relative;
  padding: 0 40px;
  z-index: 1;
}

.welcome-banner__title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}

.welcome-banner__subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

/* ====== 统计卡片 ====== */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

@media (max-width: 760px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  background: var(--fd-card);
  border-radius: 16px;
  border: 1px solid var(--fd-border);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-card__icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card__icon--green { background: #ecfdf5; color: #10b981; }
.stat-card__icon--blue { background: #eff6ff; color: #3b82f6; }
.stat-card__icon--orange { background: #fff7ed; color: #f59e0b; }
.stat-card__icon--gold { background: #fef9ec; color: #d4a853; }

.stat-card__body {
  min-width: 0;
  flex: 1;
}

.stat-card__label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

.stat-card__value {
  font-size: 26px;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
  white-space: nowrap;
}

.stat-card__value small {
  font-size: 13px;
  font-weight: 400;
  color: #9ca3af;
}

.value--primary {
  color: var(--fd-primary) !important;
}

.stat-card__trend {
  font-size: 12px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.trend-up { color: #10b981; }
.trend-down { color: #ef4444; }

/* ====== 内容卡片通用 ====== */
.content-card {
  background: var(--fd-card);
  border-radius: 16px;
  border: 1px solid var(--fd-border);
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-header__left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.link-more {
  font-size: 13px;
  color: #9ca3af;
  cursor: pointer;
  transition: color 0.2s;
}

.link-more:hover {
  color: var(--fd-primary);
}

/* ====== 房态概览 ====== */
.room-legend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}

.legend-dot {
  width: 9px;
  height: 9px;
  border-radius: 3px;
  display: inline-block;
  margin-left: 8px;
}
.legend-dot:first-child { margin-left: 0; }
.legend-dot--empty { background: #fff; border: 1px solid #e5e7eb; }
.legend-dot--occupied { background: #111827; }
.legend-dot--reserved { background: #e5e7eb; }
.legend-dot--maintenance {
  background: repeating-linear-gradient(45deg, #f3f4f6, #f3f4f6 3px, #e5e7eb 3px, #e5e7eb 6px);
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

@media (max-width: 1280px) {
  .room-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 640px) {
  .room-grid { grid-template-columns: repeat(3, 1fr); }
}

.room-cell {
  border-radius: 12px;
  padding: 12px 10px;
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 78px;
}

.room-cell__no {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.room-cell__type {
  font-size: 11px;
  color: #9ca3af;
}

.room-cell__tag {
  align-self: flex-start;
  margin-top: 2px;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
}

/* 空房：白底浅灰字 */
.rs-empty {
  background: #fff;
}
.rs-empty .room-cell__tag {
  background: #fff;
  color: #9ca3af;
  border: 1px solid #e5e7eb;
}

/* 在住：黑底白字 */
.rs-occupied {
  background: #111827;
  border-color: #111827;
}
.rs-occupied .room-cell__no,
.rs-occupied .room-cell__type {
  color: #fff;
}
.rs-occupied .room-cell__type {
  color: rgba(255, 255, 255, 0.6);
}
.rs-occupied .room-cell__tag {
  background: #fff;
  color: #111827;
}

/* 已预订：浅灰底深灰字 */
.rs-reserved {
  background: #f3f4f6;
  border-color: #e5e7eb;
}
.rs-reserved .room-cell__no {
  color: #374151;
}
.rs-reserved .room-cell__tag {
  background: #e5e7eb;
  color: #374151;
}

/* 维修中：斜条纹浅灰背景 */
.rs-maintenance {
  background: repeating-linear-gradient(45deg, #f9fafb, #f9fafb 5px, #f3f4f6 5px, #f3f4f6 10px);
  border-color: #e5e7eb;
}
.rs-maintenance .room-cell__no {
  color: #6b7280;
}
.rs-maintenance .room-cell__tag {
  background: #e5e7eb;
  color: #6b7280;
}

.room-empty {
  grid-column: 1 / -1;
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  padding: 24px 0;
}

/* ====== 最新订单 ====== */
.order-pill {
  display: inline-block;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 6px;
  white-space: nowrap;
}

.order-pill--occupied {
  background: #111827;
  color: #fff;
}

.order-pill--reserved {
  background: #f3f4f6;
  color: #374151;
}

.order-pill--default {
  background: #f3f4f6;
  color: #6b7280;
}

.order-empty {
  color: #9ca3af;
  font-size: 13px;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f8f9fa;
  color: #374151;
  font-weight: 600;
}

/* ====== 日历 ====== */
.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.calendar-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.calendar-nav {
  display: flex;
  gap: 4px;
}

.calendar-week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 6px;
}

.calendar-week__day {
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-cell {
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #374151;
  border-radius: 8px;
}

.calendar-cell.is-empty {
  background: transparent;
}

.calendar-cell.is-today {
  background: var(--fd-primary);
  color: #fff;
  font-weight: 700;
}

/* ====== 快捷操作 ====== */
.quick-card .card-title {
  margin-bottom: 14px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
  color: #374151;
}

.quick-item:hover {
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}

.quick-item__icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-item__icon--green { background: #ecfdf5; color: #10b981; }
.quick-item__icon--red { background: #fef2f2; color: #ef4444; }
.quick-item__icon--blue { background: #eff6ff; color: #3b82f6; }
.quick-item__icon--orange { background: #fff7ed; color: #f59e0b; }

/* ====== 系统通知 ====== */
.notice-timeline {
  padding-left: 4px;
}

.notice-content {
  font-size: 13px;
  color: #374151;
  line-height: 1.5;
}

.notice-empty {
  font-size: 13px;
  color: #9ca3af;
  text-align: center;
  padding: 16px 0;
}

:deep(.el-timeline-item__timestamp) {
  font-size: 11px;
  color: #9ca3af;
}
</style>
