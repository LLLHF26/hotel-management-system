<script setup lang="ts">
import { onMounted, ref, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  getRevenueSummary,
  getDailyRevenue,
  getRevenueByRoomType,
  getOccupancyByType
} from '../services/financeService'

const revenueSummary = ref<any>(null)
const dailyRevenue = ref<any>(null)
const revenueByRoomType = ref<any[]>([])
const occupancyByType = ref<any[]>([])
const lineRef = ref<HTMLDivElement | null>(null)
const barRef = ref<HTMLDivElement | null>(null)
let lineChart: any = null
let barChart: any = null

const summaryCards = computed(() => {
  const today = revenueSummary.value?.today || {}
  const month = revenueSummary.value?.thisMonth || {}
  const year = revenueSummary.value?.thisYear || {}

  return [
    {
      label: '今日营收',
      value: `¥${today.totalRevenue ?? '0.00'}`,
      sub: `房费 ${today.roomRevenue ?? '0.00'} · 其他 ${today.extraRevenue ?? '0.00'}`
    },
    {
      label: '本月营收',
      value: `¥${month.totalRevenue ?? '0.00'}`,
      sub: `订单 ${month.orderCount ?? 0} 笔 · 日均 ${month.avgDailyRevenue ?? '0.00'}`
    },
    {
      label: '本年营收',
      value: `¥${year.totalRevenue ?? '0.00'}`,
      sub: `订单 ${year.orderCount ?? 0} 笔 · 房费 ${year.roomRevenue ?? '0.00'}`
    },
    {
      label: '今日入住率',
      value: `${today.occupancyRate ?? '0%'}`,
      sub: `入住 ${today.checkInCount ?? '--'} 间 · 退房 ${today.checkOutCount ?? '--'} 间`
    }
  ]
})

function formatDate(date: Date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

function getLast7DaysRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 6)
  return {
    startDate: formatDate(start),
    endDate: formatDate(end)
  }
}

async function loadData() {
  const summary = await getRevenueSummary()
  revenueSummary.value = summary?.data ?? summary

  const { startDate, endDate } = getLast7DaysRange()
  const daily = await getDailyRevenue({ startDate, endDate })
  dailyRevenue.value = daily?.data ?? daily

  const roomType = await getRevenueByRoomType({ startDate, endDate })
  revenueByRoomType.value = roomType?.data ?? roomType

  const occupancy = await getOccupancyByType({ startDate, endDate })
  occupancyByType.value = occupancy?.data ?? occupancy
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
  const dates = dailyRevenue.value.dates || []
  const totalRevenue = dailyRevenue.value.totalRevenue || []
  const roomRevenue = dailyRevenue.value.roomRevenue || []
  const extraRevenue = dailyRevenue.value.extraRevenue || []

  if (lineChart) {
    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['房费', '其他收入', '总营收'], top: 10 },
      xAxis: {
        type: 'category',
        data: dates,
        axisLine: { lineStyle: { color: '#d4d4d8' } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: '#d4d4d8' } },
        splitLine: { lineStyle: { type: 'dashed', color: '#ececec' } }
      },
      series: [
        {
          name: '房费',
          type: 'line',
          data: roomRevenue,
          smooth: true,
          lineStyle: { width: 3 },
          itemStyle: { color: '#2563eb' }
        },
        {
          name: '其他收入',
          type: 'line',
          data: extraRevenue,
          smooth: true,
          lineStyle: { width: 3 },
          itemStyle: { color: '#10b981' }
        },
        {
          name: '总营收',
          type: 'line',
          data: totalRevenue,
          smooth: true,
          lineStyle: { width: 3 },
          itemStyle: { color: '#f59e0b' }
        }
      ]
    })
  }

  if (barChart) {
    // 按营收从小到大排序，确保条形图从低到高排列
    const sortedRoomType = [...revenueByRoomType.value].sort((a: any, b: any) => {
      const revenueA = Number(a.revenue) || 0
      const revenueB = Number(b.revenue) || 0
      return revenueA - revenueB
    })
    const names = sortedRoomType.map((item: any) => item.roomTypeName)
    const amounts = sortedRoomType.map((item: any) => Number(item.revenue) || 0)
    barChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: '#d4d4d8' } },
        splitLine: { show: false }
      },
      yAxis: {
        type: 'category',
        data: names,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#d4d4d8' } }
      },
      grid: { left: '28%', right: '10%', top: '18%', bottom: '10%' },
      series: [
        {
          name: '营收',
          type: 'bar',
          data: amounts,
          barWidth: 18,
          itemStyle: { color: '#38bdf8' },
          label: { show: true, position: 'right', formatter: '¥{c}' }
        }
      ]
    })
  }
}

onMounted(async () => {
  await loadData()
  await nextTick()
  initCharts()
  window.addEventListener('resize', () => {
    lineChart?.resize()
    barChart?.resize()
  })
})
</script>

<template>
  <div class="page-wrap">
    <div class="page-header">
      <div>
        <h1>财务管理</h1>
        <p class="page-subtitle">基于营收与退款文档，展示当前收入趋势与房型收入排名。</p>
      </div>
    </div>

    <section class="cards">
      <div class="card" v-for="card in summaryCards" :key="card.label">
        <div class="label">{{ card.label }}</div>
        <div class="value">{{ card.value }}</div>
        <div class="sub">{{ card.sub }}</div>
      </div>
    </section>

    <section class="charts">
      <div class="chart big">
        <div class="chart-title">近7日营收趋势</div>
        <div class="chart-body">
          <div ref="lineRef" style="height:260px; width:100%"></div>
        </div>
      </div>

      <div class="chart side">
        <div class="chart-title">房型营收排名</div>
        <div class="chart-body">
          <div ref="barRef" style="height:260px; width:100%"></div>
        </div>
      </div>
    </section>

    <section class="charts" style="grid-template-columns: 1fr;">
      <div class="chart">
        <div class="chart-title">房型入住率对比</div>
        <div class="chart-body" style="flex-direction: column; align-items: flex-start; justify-content: flex-start; padding: 18px; gap: 14px;">
          <div v-if="occupancyByType.length === 0" class="empty-panel">暂无数据</div>
          <div v-for="item in occupancyByType" :key="item.roomTypeName" style="width:100%; display:flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #f3f4f6;">
            <div>
              <div style="font-weight:700">{{ item.roomTypeName }}</div>
              <div style="color:#6b7280; font-size:13px">入住率 {{ item.occupancyRate }} · 入住 {{ item.occupiedNights }} / {{ item.totalNights }} 晚</div>
            </div>
            <div style="color:#111827; font-weight:700">{{ item.occupancyRate }}</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>