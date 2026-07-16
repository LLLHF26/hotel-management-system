<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getRoomDashboard, getRoomList } from '../services/roomService'

const statusSummary = ref<Array<{ label: string; value: number; color: string }>>([])
const floors = ref<Array<{ floor: string; rooms: Array<{ number: string; type: string; status: string; color: string }> }>>([])

const statusMeta: Record<string, { color: string; order: number }> = {
  '空闲中': { color: '#10b981', order: 1 },
  '预订中': { color: '#0ea5e9', order: 2 },
  '入住中': { color: '#8b5cf6', order: 3 },
  '待清洁中': { color: '#f59e0b', order: 4 },
  '打扫中': { color: '#ec4899', order: 5 },
  '维修中': { color: '#ef4444', order: 6 }
}

function toFloorLabel(value: number | string | null | undefined) {
  if (value === null || value === undefined || value === '') {
    return '未知楼层'
  }
  return `${value}楼`
}

async function initData() {
  const dashboard = await getRoomDashboard()
  const summary = dashboard?.data?.summary || dashboard?.summary || {}
  statusSummary.value = Object.entries(summary)
    .map(([label, value]) => ({
      label,
      value: Number(value ?? 0),
      color: statusMeta[label]?.color ?? '#64748b'
    }))
    .sort((a, b) => (statusMeta[a.label]?.order ?? 99) - (statusMeta[b.label]?.order ?? 99))

  const roomList = await getRoomList({ page: 1, size: 2000 })
  const records = roomList?.data?.records || roomList?.records || roomList?.data || roomList || []

  const floorMap = new Map<string | number, Array<any>>()
  records.forEach((room: any) => {
    const floorKey = room.floor ?? '未知'
    const key = typeof floorKey === 'number' ? floorKey : floorKey.toString()
    const list = floorMap.get(key) || []
    list.push(room)
    floorMap.set(key, list)
  })

  floors.value = Array.from(floorMap.entries())
    .sort(([a], [b]) => {
      const numA = Number(a)
      const numB = Number(b)
      if (!Number.isNaN(numA) && !Number.isNaN(numB)) {
        return numA - numB
      }
      return String(a).localeCompare(String(b))
    })
    .map(([floor, rooms]) => ({
      floor: toFloorLabel(floor),
      rooms: rooms.map((room: any) => {
        const status = room.status || '未知'
        return {
          number: room.roomNumber || room.roomNum || '未知',
          type: room.roomTypeName || room.type || '未知房型',
          status,
          color: statusMeta[status]?.color || '#64748b'
        }
      })
    }))
}

onMounted(() => {
  initData()
})
</script>

<template>
  <div class="page-wrap">
    <h1>房态看板</h1>
    <p class="page-subtitle">按楼层与状态展示当前房态，支持快速查看房间分布和清洁/维修情况。</p>

    <section class="cards">
      <div class="card status-card" v-for="item in statusSummary" :key="item.label">
        <div class="status-card-label">{{ item.label }}</div>
        <div class="status-card-value">{{ item.value }}</div>
        <div class="status-card-bar" :style="{ background: item.color }"></div>
      </div>
    </section>

    <section class="room-status-grid">
      <div class="room-floor-card" v-for="floor in floors" :key="floor.floor">
        <div class="room-floor-title">{{ floor.floor }}</div>
        <div class="room-grid">
          <div class="room-card" v-for="room in floor.rooms" :key="room.number">
            <div class="room-number">{{ room.number }}</div>
            <div class="room-type">{{ room.type }}</div>
            <div class="room-status-pill" :style="{ background: room.color }">{{ room.status }}</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
