<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSettings, saveSettings } from '../services/settingsService'
import { useToast } from '../composables/useToast'

const toast = useToast()

interface SettingItem {
  key: string
  label: string
  value: string
}

const META: { key: string; label: string }[] = [
  { key: 'hotelName',          label: '酒店名称' },
  { key: 'phone',              label: '联系电话' },
  { key: 'address',            label: '酒店地址' },
  { key: 'checkInTime',        label: '入住时间' },
  { key: 'checkOutTime',       label: '退房时间' },
  { key: 'fullHouseThreshold', label: '满房预警阈值' },
  { key: 'points_discount_enabled', label: '启用积分抵扣（true/false）' },
  { key: 'points_discount_ratio',   label: '积分抵扣比例（每积分抵¥，如 0.02=50积分抵1元）' },
]

const DEFAULTS: Record<string, string> = {
  hotelName: '云端酒店',
  phone: '010-88882222',
  address: '北京市朝阳区示例路 88 号',
  checkInTime: '14:00',
  checkOutTime: '12:00',
  fullHouseThreshold: '90%',
  points_discount_enabled: 'true',
  points_discount_ratio: '0.1',
}

const STORAGE_KEY = 'hotel-settings'

const settings = ref<SettingItem[]>(
  META.map((m) => ({ ...m, value: DEFAULTS[m.key] ?? '' }))
)
const saving = ref(false)

function applyFromServer(rows: { key: string; value: string }[]) {
  const map = new Map(rows.map((r) => [r.key, r.value]))
  settings.value = META.map((m) => ({
    ...m,
    value: map.get(m.key) ?? DEFAULTS[m.key] ?? '',
  }))
}

onMounted(async () => {
  const { code, data } = await getSettings()
  if (code === 200 && Array.isArray(data) && data.length > 0) {
    applyFromServer(data)
  } else {
    // 后端不可用：从 localStorage 兜底恢复
    try {
      const raw = localStorage.getItem(STORAGE_KEY)
      if (raw) {
        const stored = JSON.parse(raw) as Record<string, string>
        settings.value = META.map((m) => ({
          ...m,
          value: stored[m.key] ?? DEFAULTS[m.key] ?? '',
        }))
      }
    } catch {
      // ignore
    }
  }
})

async function onSave() {
  saving.value = true
  const items = settings.value.map((s) => ({ key: s.key, value: s.value }))
  try {
    const { code, msg } = await saveSettings(items)
    if (code === 200 && msg !== 'mock') {
      toast.success('设置已保存')
    } else {
      // 后端不可用：降级写入 localStorage，避免用户编辑丢失
      localStorage.setItem(STORAGE_KEY, JSON.stringify(Object.fromEntries(items.map((i) => [i.key, i.value]))))
      toast.error('后端未连接，已暂存本地')
    }
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">系统设置</h1>
        <p class="page-subtitle">配置酒店基础信息与业务参数（保存至数据库，供各端共享）。</p>
      </div>
      <div class="page-actions">
        <button
          class="btn btn-primary"
          :disabled="saving"
          @click="onSave"
        >
          {{ saving ? '保存中…' : '保存设置' }}
        </button>
      </div>
    </div>

    <div class="card">
      <div class="card-head">
        <div>
          <div class="card-title">基础信息</div>
          <div class="card-sub">酒店展示与运营参数</div>
        </div>
      </div>
      <div class="card-body">
        <div class="col-gap-4" style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--sp-4);">
          <div v-for="s in settings" :key="s.key" class="col-gap-2">
            <label class="label-text">{{ s.label }}</label>
            <input v-model="s.value" class="input input-block" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
