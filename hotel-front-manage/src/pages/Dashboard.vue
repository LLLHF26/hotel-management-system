<script setup lang="ts">
import { onMounted, ref, watch, nextTick, computed } from 'vue'
import { getDailyRevenue, getOccupancyByType } from '../services/financeService'
import { getRoomDashboard } from '../services/roomService'
import * as echarts from 'echarts'

const dailyRevenue = ref<any>(null)
const occupancyData = ref<any[]>([])
const roomDashboard = ref<any>(null)

const summaryCards = computed(() => {
  const summary = roomDashboard.value?.summary || {}
  const currentStay = statusCards.value.find(card => card.label === '入住中')
  const stayCount = currentStay?.value ?? 0
  const stayRate = `${currentStay?.percent ?? 0}%`

  return [
    {
      label: '当前房态',
      value: `空闲 ${summary['空闲中'] ?? 0} · 预订 ${summary['预订中'] ?? 0}`,
      sub: `入住 ${summary['入住中'] ?? 0} · 维修 ${summary['维修中'] ?? 0}`
    },
    {
      label: '待清洁/打扫',
      value: `待清洁 ${summary['待清洁中'] ?? 0}`,
      sub: `打扫中 ${summary['打扫中'] ?? 0}`
    },
    {
      label: '今日入住',
      value: `${stayCount} 间`,
      sub: `占比 ${stayRate}`
    },
    {
      label: '当前入住率',
      value: stayRate,
      sub: `已入住 ${stayCount} 间`
    }
  ]
})

const statusCards = computed(() => {
  const summary = roomDashboard.value?.summary || {}
  const total = [
    summary['空闲中'],
    summary['预订中'],
    summary['入住中'],
    summary['待清洁中'],
    summary['打扫中'],
    summary['维修中']
  ].reduce((sum, cur) => sum + Number(cur || 0), 0)

  const statusMeta: Record<string, { color: string; desc: string; tip: string }> = {
    '空闲中': { color: '#10b981', desc: '可直接分配和预订', tip: '可售' },
    '预订中': { color: '#f59e0b', desc: '已预订，待客人入住', tip: '锁定' },
    '入住中': { color: '#3b82f6', desc: '客人正在入住', tip: '入住中' },
    '待清洁中': { color: '#8b5cf6', desc: '退房后等待清洁', tip: '待处理' },
    '打扫中': { color: '#0ea5e9', desc: '保洁正在清理房间', tip: '进行中' },
    '维修中': { color: '#ef4444', desc: '设备故障或维护中', tip: '不可售' }
  }

  return Object.entries(summary).map(([label, value]) => {
    const count = Number(value || 0)
    const percent = total > 0 ? Math.round((count / total) * 100) : 0
    const meta = statusMeta[label] || { color: '#64748b', desc: '暂无描述', tip: '状态' }
    return {
      label,
      value: count,
      percent,
      desc: meta.desc,
      tip: meta.tip,
      color: meta.color
    }
  })
})

const lineRef = ref<HTMLDivElement | null>(null)
const barRef = ref<HTMLDivElement | null>(null)
let lineChart: any = null
let barChart: any = null

function normalizeArray(value: any) {
  if (Array.isArray(value)) return value
  if (Array.isArray(value?.data)) return value.data
  if (Array.isArray(value?.records)) return value.records
  return []
}
 
function formatDate(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function getLastNDaysRange(n = 7) {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - (n - 1))
  return { startDate: formatDate(start), endDate: formatDate(end) }
}
async function initData() {
  // 获取近 7 天的日期范围（动态）
  const { startDate, endDate } = getLastNDaysRange(7)
  const daily = await getDailyRevenue({ startDate, endDate })
  // normalize daily data shape
  if (daily?.data) {
    dailyRevenue.value = daily.data
  } else {
    dailyRevenue.value = daily
  }
  const occupancyRes = await getOccupancyByType({ startDate, endDate })
  occupancyData.value = normalizeArray(occupancyRes?.data ?? occupancyRes)
  const roomDash = await getRoomDashboard()
  roomDashboard.value = roomDash?.data ?? roomDash
}

function initCharts() {
  if (lineRef.value) {
    lineChart = echarts.init(lineRef.value)
  }
  if (barRef.value) {
    barChart = echarts.init(barRef.value)
  }
  renderCharts()
}

function renderCharts() {
  if (!dailyRevenue.value) return
  const dates = Array.isArray(dailyRevenue.value.dates) ? dailyRevenue.value.dates : []
  const total = Array.isArray(dailyRevenue.value.totalRevenue) ? dailyRevenue.value.totalRevenue : []

  if (lineChart) {
    lineChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: {
        type: 'category',
        data: dates,
        boundaryGap: false,
        axisLine: { lineStyle: { color: '#d4d4d8' } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: '#d4d4d8' } },
        splitLine: { lineStyle: { type: 'dashed', color: '#ececec' } }
      },
      series: [{
        type: 'line',
        data: total,
        name: '总营收',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#0f766e' },
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(15,118,110,0.45)' },
            { offset: 1, color: 'rgba(15,118,110,0.05)' }
          ])
        }
      }]
    })
  }

  // 使用 occupancyRate 字段，解析为数值并取 Top 5
  if (barChart) {
    const rows = Array.isArray(occupancyData.value) ? occupancyData.value : []
    const parsed = rows.map((row:any) => {
      const raw = row.occupancyRate ?? row.rate ?? row.occupancy ?? ''
      const cleaned = String(raw).trim().replace(/[^0-9.\-]/g, '')
      const num = parseFloat(cleaned)
      return { name: row.roomTypeName ?? row.name ?? '未知', value: Number.isNaN(num) ? 0 : num }
    })
    const top = parsed.sort((a:any,b:any) => b.value - a.value).slice(0, 5)
    const names = top.map((r:any) => r.name)
    const values = top.map((r:any) => r.value)

    barChart.setOption({
      xAxis: {
        type: 'value',
        max: 100,
        axisLabel: { formatter: '{value}%' }
      },
      yAxis: { type: 'category', data: names, axisTick: { show: false }, inverse: true },
      grid: { left: '22%', right: '10%', top: '18%', bottom: '12%' },
      series: [{
        type: 'bar',
        data: values,
        barWidth: 22,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: '#f8d177' },
            { offset: 1, color: '#caa46b' }
          ])
        },
        label: { show: true, position: 'right', formatter: '{c}%' }
      }],
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params: any) => `${params[0].name}: ${params[0].value}%`
      }
    })
  }
}

onMounted(async () => {
  try {
    await initData()
    await nextTick()
    initCharts()
  } catch (e) {
    console.error('Dashboard init failed:', e)
  }
  window.addEventListener('resize', () => {
    try { lineChart?.resize(); barChart?.resize() } catch { /* ignore */ }
  })
})

watch([dailyRevenue, occupancyData], () => { renderCharts() })
</script>

<template>
  <div>
    <section class="cards">
      <div class="card" v-for="card in summaryCards" :key="card.label">
        <div class="label">{{ card.label }}</div>
        <div class="value">{{ card.value }}</div>
        <div class="sub">{{ card.sub }}</div>
      </div>
    </section>

    <section class="cards status-cards">
      <div class="card status-card" v-for="card in statusCards" :key="card.label">
        <div class="status-head">
          <div>
            <div class="label">{{ card.label }}</div>
            <div class="value">{{ card.value }} 间</div>
          </div>
          <span class="status-badge" :style="{ background: card.color }">{{ card.tip }}</span>
        </div>
        <div class="status-sub">{{ card.desc }} · 占比 {{ card.percent }}%</div>
        <div class="status-progress">
          <div class="status-bar" :style="{ width: `${card.percent}%`, background: card.color }"></div>
        </div>
      </div>
    </section>

    <section class="charts">
        <div class="chart big">
          <div class="chart-title">近7日营收趋势</div>
          <div class="chart-body">
            <div ref="lineRef" style="height:220px;width:100%"></div>
          </div>
        </div>

        <div class="chart side">
          <div class="chart-title">房型入住率 Top 5</div>
          <div class="chart-body">
            <div ref="barRef" style="height:220px;width:100%"></div>
          </div>
        </div>
      </section>

      <section class="shortcut-grid">
        <router-link class="shortcut-card" to="/admin/orders">
          <div class="shortcut-icon">📝</div>
          <div>
            <div class="shortcut-title">订单管理</div>
            <div class="shortcut-sub">查看订单列表与订单明细</div>
          </div>
        </router-link>
        <router-link class="shortcut-card" to="/admin/room-status">
          <div class="shortcut-icon">🏨</div>
          <div>
            <div class="shortcut-title">房态看板</div>
            <div class="shortcut-sub">查看所有房态实时状态概览</div>
          </div>
        </router-link>
        <router-link class="shortcut-card" to="/admin/finance">
          <div class="shortcut-icon">📊</div>
          <div>
            <div class="shortcut-title">营收报表</div>
            <div class="shortcut-sub">查看营收数据并导出营业数据</div>
          </div>
        </router-link>
      </section>
  </div>
</template>
