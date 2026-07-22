<script setup lang="ts">
import { onMounted, ref, watch, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getDailyRevenue, getOccupancyByType, getRevenueSummary } from '../services/financeService'
import { getRoomDashboard, getRoomTypeList } from '../services/roomService'
import { getAlertList } from '../services/alertService'
import { getOrderList } from '../services/orderService'
import * as echarts from 'echarts'

const router = useRouter()

function onStatClick(c: { label: string }) {
  if (c.label === '未处理告警' && (alertCount.value || 0) > 0) {
    router.push('/admin/alerts')
  }
}

// --- Data ---
const loading = ref(true)
const dailyRevenue = ref<any>(null)
const occupancyData = ref<any[]>([])
const roomDashboard = ref<any>(null)
// Real-data refs for the summary cards. `null` means "no live data" (show "—"),
// NOT a hardcoded mock — the previous version was showing fake ¥21,860 / 78.5% / 3
// even when the backend had no revenue, which is misleading.
const revenueSummary = ref<any>(null)
const alertCount = ref<number | null>(null)
const roomTypeCount = ref<number | null>(null)

type Trend = 'flat' | 'up' | 'down'

const summaryCards = computed<Array<{ label: string; value: string; sub: string; trend: Trend }>>(() => {
  const today = revenueSummary.value?.today
  const stay   = statusCards.value.find(c => c.label === '入住中')
  const booked = statusCards.value.find(c => c.label === '预订中')
  const totalRooms = statusCards.value.reduce((s, c) => s + c.value, 0)
  const occupied   = (stay?.value || 0) + (booked?.value || 0)
  const liveRate   = totalRooms > 0 ? ((occupied / totalRooms) * 100).toFixed(1) + '%' : '0%'

  return [
    {
      label: '今日营收',
      value: today?.totalRevenue != null ? `¥${Number(today.totalRevenue).toLocaleString()}` : '—',
      sub:   today?.orderCount != null ? `订单 ${today.orderCount} 笔 · 房费 ¥${Number(today.roomRevenue || 0).toLocaleString()}` : '暂无数据',
      trend: 'flat',
    },
    {
      label: '入住率',
      value: today?.occupancyRate || liveRate,
      sub:   `入住 ${stay?.value ?? 0} 间`,
      trend: 'flat',
    },
    {
      label: '在售房型',
      value: roomTypeCount.value != null ? String(roomTypeCount.value) : '—',
      sub:   roomTypeCount.value != null ? '已上架 SKU 数量' : '暂无数据',
      trend: 'flat',
    },
    {
      label: '未处理告警',
      value: alertCount.value != null ? String(alertCount.value) : '—',
      sub:   alertCount.value != null ? (alertCount.value > 0 ? '点击查看详情' : '当前无未读') : '暂无数据',
      trend: alertCount.value != null && alertCount.value > 0 ? 'down' : 'flat',
    },
  ]
})

const statusMeta: Record<string, { color: string; bg: string; order: number }> = {
  '空闲中':   { color: '#10b981', bg: 'rgba(16, 185, 129, 0.10)', order: 1 },
  '预订中':   { color: '#0ea5e9', bg: 'rgba(14, 165, 233, 0.10)', order: 2 },
  '入住中':   { color: '#7c3aed', bg: 'rgba(124, 58, 237, 0.10)', order: 3 },
  '待清洁中': { color: '#d97706', bg: 'rgba(217, 119, 6, 0.10)', order: 4 },
  '打扫中':   { color: '#db2777', bg: 'rgba(219, 39, 119, 0.10)', order: 5 },
  '维修中':   { color: '#dc2626', bg: 'rgba(220, 38, 38, 0.10)', order: 6 },
}
const statusCards = computed(() => {
  const s = roomDashboard.value?.summary || {}
  const total = Object.values(s).reduce((sum: number, v: any) => sum + Number(v || 0), 0)
  return Object.entries(s)
    .map(([label, value]) => {
      const n = Number(value || 0)
      const pct = total > 0 ? Math.round((n / total) * 100) : 0
      const m = statusMeta[label] || { color: '#64748b', bg: 'rgba(100, 116, 139, 0.10)', order: 99 }
      return { label, value: n, percent: pct, color: m.color, bg: m.bg }
    })
    .sort((a, b) => (statusMeta[a.label]?.order ?? 99) - (statusMeta[b.label]?.order ?? 99))
})

// Recent activity — real data from order list + alert list
interface Activity { id: number; ts: string; type: string; target: string; operator: string; status: string; statusKind: 'success' | 'warning' | 'danger' | 'info' | 'neutral' }
const recentOrders = ref<any[]>([])
const recentAlerts = ref<any[]>([])

const statusKindForOrder = (status?: string): Activity['statusKind'] => {
  if (!status) return 'neutral'
  if (['已支付', '已完成', '已确认', '已退房'].some(s => status.includes(s))) return 'success'
  if (['已入住', '入住中'].some(s => status.includes(s))) return 'info'
  if (['待支付', '待确认', '处理中'].some(s => status.includes(s))) return 'warning'
  if (['已取消', '已退款', '退款中'].some(s => status.includes(s))) return 'danger'
  return 'neutral'
}
const typeForOrder = (status?: string): string => {
  if (!status) return '订单'
  if (status.includes('入住')) return '入住'
  if (status.includes('完成') || status.includes('退房')) return '退房'
  return '订单'
}
const statusKindForAlert = (level?: string): Activity['statusKind'] => {
  const l = (level || '').toLowerCase()
  if (l.includes('critical') || l.includes('严重') || l.includes('error')) return 'danger'
  if (l.includes('warning') || l.includes('warn')) return 'warning'
  return 'info'
}

const activities = computed<Activity[]>(() => {
  const orderItems: Activity[] = (recentOrders.value || []).map((o: any, idx: number) => ({
    id: o.id ?? idx + 1,
    ts: o.createTime || o.create_time || o.orderDate || '-',
    type: typeForOrder(o.status),
    target: o.orderNo || o.order_no || `订单 #${o.id}`,
    operator: o.customerName ? `客户 · ${o.customerName}` : (o.channelName || o.source || '到店 · 散客'),
    status: o.status || '未知',
    statusKind: statusKindForOrder(o.status),
  }))

  const alertItems: Activity[] = (recentAlerts.value || []).map((a: any, idx: number) => ({
    id: -((a.id ?? idx) + 1),
    ts: a.time || a.createTime || a.create_time || '-',
    type: '告警',
    target: a.title || a.message || a.content || `告警 #${a.id}`,
    operator: a.source || '系统',
    status: a.level || a.status || 'WARNING',
    statusKind: statusKindForAlert(a.level || a.status),
  }))

  return [...orderItems, ...alertItems]
    .sort((a, b) => new Date(b.ts).getTime() - new Date(a.ts).getTime())
    .slice(0, 10)
})

// --- Charts ---
const lineRef = ref<HTMLDivElement | null>(null)
const barRef = ref<HTMLDivElement | null>(null)
let lineChart: any = null; let barChart: any = null

function formatDate(d: Date) {
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}
function last7() {
  const end = new Date(); const start = new Date(); start.setDate(end.getDate() - 6)
  return { startDate: formatDate(start), endDate: formatDate(end) }
}

async function load() {
  loading.value = true
  const { startDate, endDate } = last7()

  // Room dashboard — real or mock (mock is fine here, it shows 19 空闲 0 入住 etc.)
  const r = await getRoomDashboard()
  roomDashboard.value = r?.data ?? r

  // Revenue summary — only show real data; if service fell back to mock, set null → "—"
  const summary = await getRevenueSummary()
  if (summary?.msg === 'mock') {
    revenueSummary.value = null
  } else {
    revenueSummary.value = summary?.data ?? summary ?? null
  }

  // Alert count (unread) + recent alerts
  const alerts = await getAlertList()
  if (alerts?.msg === 'mock') {
    alertCount.value = null
    recentAlerts.value = []
  } else {
    const list: any[] = alerts?.data || alerts || []
    recentAlerts.value = list.slice(0, 10)
    alertCount.value = list.filter(a => a.unread).length
  }

  // Room type count
  const types = await getRoomTypeList({ page: 1, size: 1000 })
  if (types?.msg === 'mock') {
    roomTypeCount.value = null
  } else {
    const data = types?.data ?? types
    roomTypeCount.value = data?.records?.length ?? 0
  }

  // Recent orders
  const orderRes = await getOrderList({ page: 1, size: 10 })
  if (orderRes?.msg === 'mock') {
    recentOrders.value = []
  } else {
    const data = orderRes?.data ?? orderRes
    recentOrders.value = Array.isArray(data?.records) ? data.records.slice(0, 10) : []
  }

  // Charts data
  const d = await getDailyRevenue({ startDate, endDate })
  dailyRevenue.value = d?.data ?? d
  occupancyData.value = (await getOccupancyByType({ startDate, endDate }))?.data ?? []
  loading.value = false
}

function renderCharts() {
  if (lineRef.value) lineChart = echarts.init(lineRef.value)
  if (barRef.value)  barChart  = echarts.init(barRef.value)

  if (lineChart && dailyRevenue.value) {
    const dates = dailyRevenue.value.dates || []
    const total = dailyRevenue.value.totalRevenue || []
    const room  = dailyRevenue.value.roomRevenue || []
    const extra = dailyRevenue.value.extraRevenue || []
    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['房费', '其他收入', '总营收'], top: 4, textStyle: { color: '#64748b', fontSize: 12 } },
      grid: { left: 8, right: 8, top: 36, bottom: 8, containLabel: true },
      xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#e2e8f0' } }, axisTick: { show: false }, axisLabel: { color: '#94a3b8' } },
      yAxis: { type: 'value', axisLabel: { color: '#94a3b8' }, splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } } },
      series: [
        { name: '房费',     type: 'line', data: room,  smooth: true, lineStyle: { width: 2 }, itemStyle: { color: '#2563eb' }, symbol: 'circle', symbolSize: 6 },
        { name: '其他收入', type: 'line', data: extra, smooth: true, lineStyle: { width: 2 }, itemStyle: { color: '#10b981' }, symbol: 'circle', symbolSize: 6 },
        { name: '总营收',   type: 'line', data: total, smooth: true, lineStyle: { width: 2 }, itemStyle: { color: '#f59e0b' }, symbol: 'circle', symbolSize: 6 },
      ],
    })
  }
  if (barChart) {
    const rows = Array.isArray(occupancyData.value) ? occupancyData.value : []
    const parsed = rows.map((r: any) => {
      const raw = r.occupancyRate ?? r.rate ?? r.occupancy ?? ''
      const num = parseFloat(String(raw).replace(/[^0-9.\-]/g, ''))
      return { name: r.roomTypeName ?? r.name ?? '未知', value: Number.isNaN(num) ? 0 : num }
    })
    const top = parsed.sort((a: any, b: any) => b.value - a.value).slice(0, 5)
    barChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: (p: any) => `${p[0].name}: ${p[0].value}%` },
      grid: { left: 8, right: 32, top: 12, bottom: 8, containLabel: true },
      xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%', color: '#94a3b8' }, splitLine: { show: false } },
      yAxis: { type: 'category', data: top.map((t: any) => t.name), inverse: true, axisTick: { show: false }, axisLine: { show: false }, axisLabel: { color: '#64748b' } },
      series: [{
        type: 'bar', data: top.map((t: any) => t.value), barWidth: 14,
        itemStyle: { color: '#38bdf8', borderRadius: [0, 4, 4, 0] },
        label: { show: true, position: 'right', formatter: '{c}%', color: '#475569', fontSize: 11 },
      }],
    })
  }
}

onMounted(async () => {
  try {
    await load()
    await nextTick()
    renderCharts()
    window.addEventListener('resize', () => { lineChart?.resize(); barChart?.resize() })
  } catch (e) { console.error('Dashboard init failed:', e) }
})
watch([dailyRevenue, occupancyData], () => { if (lineChart) renderCharts() })
</script>

<template>
  <div>
    <div class="page">
      <div class="page-head">
        <div>
          <h1 class="page-title">工作台</h1>
          <p class="page-subtitle">酒店运营总览 · 实时数据与最近活动。</p>
        </div>
        <div class="page-actions">
          <button class="btn">导出报表</button>
          <button class="btn btn-primary" @click="router.push('/admin/orders')">查看订单</button>
        </div>
      </div>

      <!-- KPI stat cards -->
      <div class="stat-grid">
        <div v-for="c in summaryCards" :key="c.label" class="stat" :class="{ 'is-clickable': c.label === '未处理告警' && (alertCount || 0) > 0 }" @click="onStatClick(c)">
          <div class="row row-gap-2">
            <div class="stat-label">{{ c.label }}</div>
            <span class="filterbar-spacer"></span>
            <span v-if="c.trend === 'up'"   class="badge badge-success" style="height: 18px;">▲</span>
            <span v-else-if="c.trend === 'down'" class="badge badge-danger"  style="height: 18px;">▼</span>
            <span v-else class="badge badge-neutral" style="height: 18px;">—</span>
          </div>
          <div class="stat-value">{{ c.value }}</div>
          <div class="stat-sub">{{ c.sub }}</div>
        </div>
      </div>

      <!-- Status distribution (compact progress bars) -->
      <div class="card mt-4">
        <div class="card-head">
          <div>
            <div class="card-title">房态分布</div>
            <div class="card-sub">各状态房间数与占比</div>
          </div>
        </div>
        <div class="card-body" style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: var(--sp-4);">
          <div v-for="c in statusCards" :key="c.label" class="col-gap-2">
            <div class="row row-gap-2">
              <span class="fw-600 fz-sm">{{ c.label }}</span>
              <span class="filterbar-spacer"></span>
              <span class="text-muted fz-xs">{{ c.percent }}%</span>
            </div>
            <div class="row row-gap-2" style="align-items: baseline;">
              <span style="font-size: var(--fz-2xl); font-weight: 700; letter-spacing: -0.02em;">{{ c.value }}</span>
              <span class="text-muted fz-sm">间</span>
            </div>
            <div style="height: 6px; background: var(--bg-muted); border-radius: 999px; overflow: hidden;">
              <div :style="{ width: c.percent + '%', height: '100%', background: c.color, transition: 'width 0.3s' }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Charts row -->
      <div class="two-col mt-4">
        <div class="card">
          <div class="card-head">
            <div>
              <div class="card-title">近 7 日营收趋势</div>
              <div class="card-sub">房费 / 其他 / 合计</div>
            </div>
          </div>
          <div class="card-body"><div ref="lineRef" style="height: 260px; width: 100%;"></div></div>
        </div>
        <div class="card">
          <div class="card-head">
            <div>
              <div class="card-title">房型入住率 Top 5</div>
              <div class="card-sub">按入住率排序</div>
            </div>
          </div>
          <div class="card-body"><div ref="barRef" style="height: 260px; width: 100%;"></div></div>
        </div>
      </div>

      <!-- Recent activity (Stellate-style table) -->
      <div class="card mt-4">
        <div class="card-head">
          <div>
            <div class="card-title">最近活动</div>
            <div class="card-sub">订单与告警实时流</div>
          </div>
          <div class="page-actions">
            <button class="btn btn-sm" @click="router.push('/admin/orders')">查看订单</button>
          </div>
        </div>
        <div style="overflow-x: auto;">
          <table class="table">
            <thead>
              <tr>
                <th>时间</th>
                <th>类型</th>
                <th>对象</th>
                <th>操作人</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="a in activities" :key="a.id">
                <td class="mono fz-xs">{{ a.ts }}</td>
                <td><span class="pill pill-slate">{{ a.type }}</span></td>
                <td class="fw-600">{{ a.target }}</td>
                <td class="text-muted">{{ a.operator }}</td>
                <td><span class="badge" :class="`badge-${a.statusKind}`">{{ a.status }}</span></td>
              </tr>
              <tr v-if="loading">
                <td colspan="5">
                  <div class="loading-wrap">
                    <div class="loading-dual-ring"></div>
                    <span class="loading-text">加载中…</span>
                  </div>
                </td>
              </tr>
              <tr v-if="!loading && activities.length === 0">
                <td colspan="5" class="text-center text-muted" style="padding: var(--sp-10);">暂无最近活动</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
  </div>
</div>
</template>
