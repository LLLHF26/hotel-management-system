<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getRoomDashboard, getRoomList } from '../services/roomService'

interface StatusItem { label: string; value: number; color: string; bg: string }
interface Floor { floor: string; rooms: Array<{ number: string; type: string; status: string; color: string; bg: string }> }

const statusSummary = ref<StatusItem[]>([])
const floors = ref<Floor[]>([])
const loading = ref(true)

const statusMeta: Record<string, { color: string; bg: string; order: number }> = {
  '空闲中':   { color: '#10b981', bg: 'rgba(16, 185, 129, 0.10)', order: 1 },
  '预订中':   { color: '#0ea5e9', bg: 'rgba(14, 165, 233, 0.10)', order: 2 },
  '入住中':   { color: '#7c3aed', bg: 'rgba(124, 58, 237, 0.10)', order: 3 },
  '待清洁中': { color: '#d97706', bg: 'rgba(217, 119, 6, 0.10)', order: 4 },
  '打扫中':   { color: '#db2777', bg: 'rgba(219, 39, 119, 0.10)', order: 5 },
  '维修中':   { color: '#dc2626', bg: 'rgba(220, 38, 38, 0.10)', order: 6 },
}

function floorLabel(v: any) {
  if (v === null || v === undefined || v === '') return '未知楼层'
  return `${v} 楼`
}

async function init() {
  loading.value = true
  const dash = await getRoomDashboard()
  const summary = dash?.data?.summary || dash?.summary || {}
  statusSummary.value = Object.entries(summary)
    .map(([label, value]) => {
      const m = statusMeta[label] || { color: '#64748b', bg: 'rgba(100, 116, 139, 0.10)', order: 99 }
      return { label, value: Number(value ?? 0), color: m.color, bg: m.bg }
    })
    .sort((a, b) => (statusMeta[a.label]?.order ?? 99) - (statusMeta[b.label]?.order ?? 99))

  const list = await getRoomList({ page: 1, size: 2000 })
  // Defensive: only treat as array when the value is actually an array.
  // The backend may return { code: 500, data: null } on failure, which is truthy
  // but not iterable — must not fall through to that object.
  const records: any[] = Array.isArray(list?.data?.records)
    ? list.data.records
    : Array.isArray(list?.records)
      ? list.records
      : Array.isArray(list?.data)
        ? list.data
        : []
  const map = new Map<string | number, any[]>()
  records.forEach(room => {
    const k = room.floor ?? '未知'
    const key = typeof k === 'number' ? k : String(k)
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(room)
  })
  floors.value = Array.from(map.entries())
    .sort(([a], [b]) => {
      const na = Number(a), nb = Number(b)
      if (!Number.isNaN(na) && !Number.isNaN(nb)) return na - nb
      return String(a).localeCompare(String(b))
    })
    .map(([f, rs]) => ({
      floor: floorLabel(f),
      rooms: rs.map(room => {
        const m = statusMeta[room.status] || { color: '#64748b', bg: 'rgba(100, 116, 139, 0.10)' }
        return {
          number: room.roomNumber || room.roomNum || '未知',
          type: room.roomTypeName || room.type || '未知房型',
          status: room.status || '未知',
          color: m.color,
          bg: m.bg,
        }
      }),
    }))
  loading.value = false
}
onMounted(init)
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">房态看板</h1>
        <p class="page-subtitle">按楼层与状态展示当前房态，支持快速查看房间分布和清洁/维修情况。</p>
      </div>
    </div>

    <div v-if="loading" class="loading-wrap">
      <div class="loading-dual-ring"></div>
      <span class="loading-text">加载中…</span>
    </div>
    <div v-else class="stat-grid">
      <div v-for="s in statusSummary" :key="s.label" class="stat">
        <div class="row row-gap-2">
          <div class="stat-label">{{ s.label }}</div>
        </div>
        <div class="stat-value">{{ s.value }}</div>
        <div class="stat-bar" :style="{ height: '4px', background: s.color, borderRadius: '999px', marginTop: '4px' }"></div>
      </div>
    </div>

    <div v-if="loading" class="loading-wrap" style="margin-top: var(--sp-4);">
      <div class="loading-dual-ring"></div>
      <span class="loading-text">加载中…</span>
    </div>
    <div v-else-if="floors.length === 0" class="empty" style="margin-top: var(--sp-4);">暂无房间数据</div>
    <div v-else class="mt-4" style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--sp-4);">
      <div v-for="f in floors" :key="f.floor" class="card">
        <div class="card-head">
          <div>
            <div class="card-title">{{ f.floor }}</div>
            <div class="card-sub">共 {{ f.rooms.length }} 间</div>
          </div>
        </div>
        <div class="card-body" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: var(--sp-2);">
          <div v-for="r in f.rooms" :key="r.number" class="col-gap-2" :style="{ padding: '10px 12px', border: '1px solid var(--border)', borderRadius: 'var(--r-md)', background: 'var(--bg-subtle)' }">
            <div class="fw-600 fz-sm">{{ r.number }}</div>
            <div class="text-muted fz-xs" style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ r.type }}</div>
            <span class="pill" :style="{ background: r.bg, color: r.color }">{{ r.status }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
