<script setup lang="ts">
import { onMounted, ref, computed, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getRevenueSummary, getDailyRevenue, getRevenueByRoomType, getOccupancyByType } from '../services/financeService'

const revenueSummary = ref<any>(null)
const dailyRevenue = ref<any>(null)
const loading = ref(true)
const revenueByRoomType = ref<any[]>([])
const occupancyByType = ref<any[]>([])
const lineRef = ref<HTMLDivElement | null>(null)
const barRef = ref<HTMLDivElement | null>(null)
let lineChart: any = null; let barChart: any = null

const cards = computed(() => {
  const t = revenueSummary.value?.today || {}
  const m = revenueSummary.value?.thisMonth || {}
  const y = revenueSummary.value?.thisYear || {}
  return [
    { label: '今日营收',   value: `¥${t.totalRevenue ?? '0.00'}`,  sub: `房费 ¥${t.roomRevenue ?? 0} · 其他 ¥${t.extraRevenue ?? 0}` },
    { label: '本月营收',   value: `¥${m.totalRevenue ?? '0.00'}`,  sub: `订单 ${m.orderCount ?? 0} 笔 · 日均 ¥${m.avgDailyRevenue ?? 0}` },
    { label: '本年营收',   value: `¥${y.totalRevenue ?? '0.00'}`,  sub: `订单 ${y.orderCount ?? 0} 笔 · 房费 ¥${y.roomRevenue ?? 0}` },
    { label: '今日入住率', value: t.occupancyRate ?? '0%',          sub: `入住 ${t.checkInCount ?? '--'} · 退房 ${t.checkOutCount ?? '--'}` },
  ]
})

function formatDate(d: Date) {
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}
function last7() {
  const end = new Date(); const start = new Date(); start.setDate(end.getDate() - 6)
  return { startDate: formatDate(start), endDate: formatDate(end) }
}

async function load() {
  loading.value = true
  const summary = await getRevenueSummary()
  revenueSummary.value = summary?.data ?? summary
  const { startDate, endDate } = last7()
  const d = await getDailyRevenue({ startDate, endDate })
  dailyRevenue.value = d?.data ?? d
  revenueByRoomType.value = (await getRevenueByRoomType({ startDate, endDate }))?.data ?? []
  occupancyByType.value = (await getOccupancyByType({ startDate, endDate }))?.data ?? []
  loading.value = false
}

function renderCharts() {
  if (!dailyRevenue.value) return
  const dates = dailyRevenue.value.dates || []
  const total = dailyRevenue.value.totalRevenue || []
  const room  = dailyRevenue.value.roomRevenue || []
  const extra = dailyRevenue.value.extraRevenue || []

  if (lineChart) {
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
    const sorted = [...revenueByRoomType.value].sort((a: any, b: any) => Number(a.revenue) - Number(b.revenue))
    const names = sorted.map((i: any) => i.roomTypeName)
    const amounts = sorted.map((i: any) => Number(i.revenue) || 0)
    barChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 8, right: 32, top: 12, bottom: 8, containLabel: true },
      xAxis: { type: 'value', axisLine: { show: false }, splitLine: { show: false }, axisLabel: { color: '#94a3b8' } },
      yAxis: { type: 'category', data: names, axisTick: { show: false }, axisLine: { show: false }, axisLabel: { color: '#64748b' } },
      series: [{ type: 'bar', data: amounts, barWidth: 14, itemStyle: { color: '#38bdf8', borderRadius: [0, 4, 4, 0] }, label: { show: true, position: 'right', formatter: '¥{c}', color: '#475569', fontSize: 11 } }],
    })
  }
}

onMounted(async () => {
  await load()
  await nextTick()
  if (lineRef.value) lineChart = echarts.init(lineRef.value)
  if (barRef.value)  barChart  = echarts.init(barRef.value)
  renderCharts()
  window.addEventListener('resize', () => { lineChart?.resize(); barChart?.resize() })
})
watch([dailyRevenue, revenueByRoomType], renderCharts)
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">财务管理</h1>
        <p class="page-subtitle">基于营收与入住率数据，展示当前收入趋势与房型收入排名。</p>
      </div>
    </div>

    <div class="stat-grid">
      <div v-for="c in cards" :key="c.label" class="stat">
        <div class="stat-label">{{ c.label }}</div>
        <div class="stat-value">{{ c.value }}</div>
        <div class="stat-sub">{{ c.sub }}</div>
      </div>
    </div>

    <div class="two-col mt-4">
      <div class="card">
        <div class="card-head"><div class="card-title">近 7 日营收趋势</div></div>
        <div class="card-body"><div ref="lineRef" style="height: 280px; width: 100%;"></div></div>
      </div>
      <div class="card">
        <div class="card-head"><div class="card-title">房型营收排名</div></div>
        <div class="card-body"><div ref="barRef" style="height: 280px; width: 100%;"></div></div>
      </div>
    </div>

    <div class="card mt-4">
      <div class="card-head">
        <div>
          <div class="card-title">房型入住率对比</div>
          <div class="card-sub">按房型展示入住率与入住晚数</div>
        </div>
      </div>
      <div class="card-body" style="padding: 0;">
        <table class="table">
          <thead>
            <tr>
              <th>房型</th>
              <th class="num">总房数</th>
              <th class="num">已入住晚数</th>
              <th class="num">可售晚数</th>
              <th class="num">入住率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in occupancyByType" :key="item.roomTypeName">
              <td class="fw-600">{{ item.roomTypeName }}</td>
              <td class="num">{{ item.totalRooms }}</td>
              <td class="num">{{ item.occupiedNights }}</td>
              <td class="num">{{ item.totalNights }}</td>
              <td class="num"><span class="badge badge-accent">{{ item.occupancyRate }}</span></td>
            </tr>
            <tr v-if="loading">
              <td colspan="5">
                <div class="loading-wrap">
                  <div class="loading-dual-ring"></div>
                  <span class="loading-text">加载中…</span>
                </div>
              </td>
            </tr>
            <tr v-if="!loading && occupancyByType.length === 0"><td colspan="5" class="empty">暂无数据</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
