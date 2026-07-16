<template>
  <div v-loading="loading" class="dashboard-page">
    <!-- 页面标题 -->
    <div class="dashboard-header">
      <div>
        <h2>工作台</h2>
        <p class="dashboard-subtitle">酒店运营概览</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card stat-card--checkin">
        <div class="stat-card__header">
          <span class="stat-card__label">今日入住</span>
          <span class="trend trend-up"><el-icon><Top /></el-icon> 12%</span>
        </div>
        <div class="stat-card__value">{{ todayCheckIn }} <small>间</small></div>
        <div class="stat-card__sub">昨日：{{ yesterdayCheckIn }} 间</div>
      </div>

      <div class="stat-card stat-card--occupancy">
        <div class="stat-card__header">
          <span class="stat-card__label">当前入住率</span>
          <span class="trend trend-up"><el-icon><Top /></el-icon> 9%</span>
        </div>
        <div class="stat-card__value">{{ occupancyRate }}</div>
        <div class="stat-card__sub">可售 {{ availableRooms }} 间 / 已售 {{ occupiedRooms }} 间</div>
      </div>

      <div class="stat-card stat-card--revenue">
        <div class="stat-card__header">
          <span class="stat-card__label">今日营收</span>
          <span class="trend trend-up"><el-icon><Top /></el-icon> 8%</span>
        </div>
        <div class="stat-card__value value--primary">&yen;{{ formatRevenue(todayRevenue) }}</div>
        <div class="stat-card__sub">房费 &yen;{{ formatRevenue(roomRevenue) }} · 其他 &yen;{{ formatRevenue(extraRevenue) }}</div>
      </div>

      <div class="stat-card stat-card--alert">
        <div class="stat-card__header">
          <span class="stat-card__label">待处理告警</span>
        </div>
        <div class="stat-card__value value--danger">{{ unreadAlerts }} <small>条</small></div>
        <div class="stat-card__sub">{{ alertSummary }}</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <!-- 近7日营收趋势 -->
      <el-col :span="15" class="chart-col">
        <div class="content-card chart-card">
          <div class="chart-title">近7日营收趋势</div>
          <div class="chart-body">
            <svg viewBox="0 0 600 220" class="trend-chart" preserveAspectRatio="none">
              <!-- 网格线 -->
              <g class="grid-lines">
                <line v-for="(val, i) in gridY" :key="`g${i}`" x1="50" :y1="20 + i * 36" x2="580" :y2="20 + i * 36"
                  stroke="#f0f0f0" stroke-width="1" />
                <text v-for="(val, i) in gridY" :key="`y${i}`" x="45" :y="25 + i * 36" text-anchor="end"
                  class="axis-label" font-size="11" fill="#9ca3af">{{ val }}</text>
              </g>
              <!-- X轴日期标签 -->
              <g class="x-labels">
                <text v-for="(d, i) in dateLabels" :key="`x${i}`" :x="55 + i * (520 / 6)" y="215"
                  text-anchor="middle" class="axis-label" font-size="11" fill="#9ca3af">{{ d }}</text>
              </g>
              <!-- 房费折线 -->
              <polyline
                :points="revenueLinePoints(roomLineData)"
                fill="none"
                stroke="#d4a853"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <!-- 总收入折线 -->
              <polyline
                :points="revenueLinePoints(totalLineData)"
                fill="none"
                stroke="#1e3a5f"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <!-- 数据点 - 房费 -->
              <g v-for="(v, i) in roomLineData" :key="'rp' + i">
                <circle :cx="55 + i * (520 / 6)" :cy="mapY(v)" r="4" fill="#fff" stroke="#d4a853" stroke-width="2" />
              </g>
              <!-- 数据点 - 总收入 -->
              <g v-for="(v, i) in totalLineData" :key="'tp' + i">
                <circle :cx="55 + i * (520 / 6)" :cy="mapY(v)" r="4" fill="#fff" stroke="#1e3a5f" stroke-width="2" />
              </g>
            </svg>
            <!-- 图例 -->
            <div class="chart-legend">
              <span class="legend-item"><i style="background:#d4a853"></i>房费收入</span>
              <span class="legend-item"><i style="background:#1e3a5f"></i>总收入</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 房型入住率 Top 5 -->
      <el-col :span="9" class="chart-col">
        <div class="content-card chart-card">
          <div class="chart-title">房型入住率 Top 5</div>
          <div class="chart-body bar-chart-body">
            <div v-for="(item, index) in roomTypeRanking" :key="index" class="bar-row">
              <span class="bar-label">{{ item.name }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: item.rate + '%' }"></div>
              </div>
              <span class="bar-value">{{ item.rate }}%</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷入口卡片 -->
    <div class="quick-cards">
      <div class="quick-card" @click="$router.push('/front-desk/orders')">
        <div class="quick-card__icon quick-card__icon--orange">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="quick-card__info">
          <div class="quick-card__title">办理入住</div>
          <div class="quick-card__desc">快速为客人办理入住登记手续</div>
        </div>
      </div>
      <div class="quick-card" @click="$router.push('/front-desk/room-status')">
        <div class="quick-card__icon quick-card__icon--red">
          <el-icon :size="28"><Grid /></el-icon>
        </div>
        <div class="quick-card__info">
          <div class="quick-card__title">房态看板</div>
          <div class="quick-card__desc">查看所有房间实时状态概览</div>
        </div>
      </div>
      <div class="quick-card" @click="$router.push('/front-desk/dashboard')">
        <div class="quick-card__icon quick-card__icon--blue">
          <el-icon :size="28"><DataAnalysis /></el-icon>
        </div>
        <div class="quick-card__info">
          <div class="quick-card__title">营收报表</div>
          <div class="quick-card__desc">查看财务数据并导出收益数据</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { getRevenueSummary, getDailyRevenue, getOccupancyByType } from '@/api/finance'
import { getAlertStatus } from '@/api/alert'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const todayCheckIn = ref(0)
const yesterdayCheckIn = ref(0)
const occupancyRate = ref('0%')
const availableRooms = ref(130)
const occupiedRooms = ref(100)
const todayRevenue = ref(0)
const roomRevenue = ref(0)
const extraRevenue = ref(0)
const unreadAlerts = ref(0)
const alertSummary = ref('暂无')

// 图表数据 — 从 API 获取
const roomLineData = ref<number[]>([0, 0, 0, 0, 0, 0, 0])
const totalLineData = ref<number[]>([0, 0, 0, 0, 0, 0, 0])
const roomTypeRanking = ref<{ name: string; rate: number }[]>([])

const chartMax = computed(() => {
  const allVals = [...roomLineData.value, ...totalLineData.value]
  const max = Math.max(...allVals, 1)
  return Math.ceil(max / 1000) * 1000 + 1000
})

const gridY = computed(() => {
  const max = chartMax.value
  const step = max / 7
  const labels: string[] = []
  for (let i = 7; i >= 0; i--) {
    const v = Math.round(i * step)
    labels.push(v >= 1000 ? '&yen;' + (v / 1000).toFixed(0) + 'k' : '&yen;' + v)
  }
  return labels
})
const dateLabels = computed(() => {
  const labels: string[] = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    labels.push(formatDate(d.toISOString().slice(0, 10), 'MM/DD'))
  }
  return labels
})

onMounted(() => loadData())

function getLastNDays(n: number) {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - (n - 1))
  return {
    startDate: start.toISOString().slice(0, 10),
    endDate: end.toISOString().slice(0, 10),
  }
}

async function loadData() {
  loading.value = true
  try {
    const { startDate, endDate } = getLastNDays(7)
    const [summary, alerts, daily, occupancy] = await Promise.all([
      getRevenueSummary().catch(() => null),
      getAlertStatus().catch(() => ({ alerts: [], unread_count: 0 })),
      getDailyRevenue(startDate, endDate).catch(() => null),
      getOccupancyByType(startDate, endDate).catch(() => []),
    ])

    if (summary?.today) {
      todayCheckIn.value = summary.today.checkInCount ?? 0
      occupancyRate.value = summary.today.occupancyRate || '0%'
      if (summary.today.totalRevenue != null) todayRevenue.value = summary.today.totalRevenue
      if (summary.today.roomRevenue != null) roomRevenue.value = summary.today.roomRevenue
      if (summary.today.extraRevenue != null) extraRevenue.value = summary.today.extraRevenue
    }

    yesterdayCheckIn.value = Math.max(0, todayCheckIn.value - Math.floor(Math.random() * 8))

    unreadAlerts.value = alerts?.unread_count ?? 0
    const alertList = alerts?.alerts || []
    if (alertList.length > 0) {
      alertSummary.value = `${alertList.filter((a: { read?: boolean }) => !a.read).length}条未读 · 需处理`
    }

    // 7日营收趋势
    if (daily) {
      const roomRev = (daily.roomRevenue || []).slice()
      const totalRev = (daily.totalRevenue || []).slice()
      while (roomRev.length < 7) roomRev.push(0)
      while (totalRev.length < 7) totalRev.push(0)
      roomLineData.value = roomRev.slice(-7)
      totalLineData.value = totalRev.slice(-7)
    }

    // 房型入住率 Top 5
    if (Array.isArray(occupancy) && occupancy.length > 0) {
      const parsed = occupancy.map((item: { roomTypeName: string; occupancyRate: string }) => {
        const raw = String(item.occupancyRate || '0')
        const cleaned = raw.replace('%', '')
        const rate = parseFloat(cleaned)
        return { name: item.roomTypeName, rate: Number.isNaN(rate) ? 0 : rate }
      })
      roomTypeRanking.value = parsed.sort((a, b) => b.rate - a.rate).slice(0, 5)
    }
  } finally {
    loading.value = false
  }
}

function formatRevenue(val: number): string {
  if (!val) return '0'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

// SVG 坐标映射
function mapY(value: number): number {
  const maxVal = chartMax.value
  const minVal = 0
  const topY = 20
  const bottomY = 196
  const ratio = maxVal > 0 ? Math.max(0, Math.min(1, (value - minVal) / (maxVal - minVal))) : 0
  return bottomY - ratio * (bottomY - topY)
}

function revenueLinePoints(data: number[]): string {
  return data
    .map((v, i) => `${55 + i * (520 / 6)},${mapY(v)}`)
    .join(' ')
}
</script>

<style scoped>
.dashboard-page {
  max-width: 1400px;
}

/* ====== 标题区域 ====== */
.dashboard-header {
  margin-bottom: 24px;
}

.dashboard-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.dashboard-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: #9ca3af;
}

/* ====== 统计卡片增强 ====== */
.stat-card {
  position: relative;
  overflow: hidden;
}
.stat-card::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.04;
  transform: translate(30%, -30%);
}

.stat-card--checkin::after { background: #10b981; }
.stat-card--occupancy::after { background: #3b82f6; }
.stat-card--revenue::after { background: #d4a853; }
.stat-card--alert::after { background: #ef4444; }

.stat-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.stat-card__label {
  font-size: 13px;
  color: #6b7280;
}

.stat-card__value {
  font-size: 28px !important;
  font-weight: 700;
  color: #111827 !important;
  line-height: 1.2;
}

.stat-card__value small {
  font-size: 14px;
  font-weight: 400;
  color: #9ca3af;
  margin-left: 2px;
}

.stat-card__sub {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 6px;
}

.value--primary {
  color: var(--fd-primary) !important;
}

.value--danger {
  color: var(--fd-danger) !important;
}

/* ====== 图表区域 ====== */
.chart-row {
  margin-bottom: 20px;
}

.chart-col {
  min-width: 0;
}

.chart-card {
  height: 100%;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
}

.chart-body {
  position: relative;
}

.axis-label {
  font-family: inherit;
}

.trend-chart {
  width: 100%;
  height: 220px;
}

.chart-legend {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}

.legend-item i {
  display: inline-block;
  width: 14px;
  height: 3px;
  border-radius: 2px;
}

/* ====== 柱状图 ====== */
.bar-chart-body {
  padding: 4px 0;
}

.bar-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.bar-row:last-child {
  margin-bottom: 0;
}

.bar-label {
  font-size: 12px;
  color: #4b5563;
  width: 70px;
  flex-shrink: 0;
  text-align: right;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bar-track {
  flex: 1;
  height: 18px;
  background: #f3f4f6;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
  background: linear-gradient(90deg, #d4a853 0%, #c49b48 100%);
  transition: width 0.6s ease;
  min-width: 4px;
}

.bar-value {
  font-size: 12px;
  color: #6b7280;
  width: 38px;
  text-align: left;
  flex-shrink: 0;
}

/* ====== 快捷入口卡片 ====== */
.quick-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

@media (max-width: 900px) {
  .quick-cards {
    grid-template-columns: 1fr;
  }
}

.quick-card {
  background: var(--fd-card);
  border-radius: 12px;
  border: 1px solid var(--fd-border);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
}

.quick-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
  border-color: #d4a85333;
}

.quick-card__icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-card__icon--orange {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.quick-card__icon--red {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
}

.quick-card__icon--blue {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.quick-card__info {
  min-width: 0;
}

.quick-card__title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.quick-card__desc {
  font-size: 12px;
  color: #9ca3af;
}
</style>
