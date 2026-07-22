<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAlertList } from '../services/alertService'

const route = useRoute()
const router = useRouter()

const showNotifications = ref(false)
const notifications = ref<any[]>([])
const loadingNotifications = ref(false)
const unreadCount = computed(() => notifications.value.filter((n) => n.unread).length)

const ICONS = {
  dashboard:  '<path d="M3 12l9-9 9 9"/><path d="M5 10v10a1 1 0 001 1h3v-6h6v6h3a1 1 0 001-1V10"/>',
  building:   '<path d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16"/><path d="M3 21h18"/><path d="M9 7h.01M9 11h.01M9 15h.01M14 7h.01M14 11h.01M14 15h.01"/>',
  receipt:    '<path d="M9 5h6a2 2 0 012 2v12a2 2 0 01-2 2H9a2 2 0 01-2-2V7a2 2 0 012-2z"/><path d="M9 11h6M9 15h4"/>',
  bed:        '<path d="M3 18v-6a2 2 0 012-2h14a2 2 0 012 2v6"/><path d="M3 18v2M21 18v2"/><path d="M7 10V8a2 2 0 012-2h6a2 2 0 012 2v2"/><path d="M7 14h.01M7 17h.01"/>',
  door:       '<path d="M6 21V5a2 2 0 012-2h8a2 2 0 012 2v16"/><path d="M6 21h12"/><path d="M10 12h.01"/>',
  users:      '<path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/>',
  user:       '<path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/>',
  currency:   '<path d="M12 1v22"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>',
  bell:       '<path d="M15 17h5l-1.4-1.4A2 2 0 0118 14V11a6 6 0 10-12 0v3a2 2 0 01-.6 1.6L4 17h5"/><path d="M9 17a3 3 0 006 0"/>',
  book:       '<path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M4 4.5A2.5 2.5 0 016.5 2H20v20H6.5A2.5 2.5 0 004 19.5v-15z"/>',
  cog:        '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 11-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 11-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 11-2.83-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H3a2 2 0 110-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 112.83-2.83l.06.06a1.65 1.65 0 001.82.33H9a1.65 1.65 0 001-1.51V3a2 2 0 114 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 112.83 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V9a1.65 1.65 0 001.51 1H21a2 2 0 110 4h-.09a1.65 1.65 0 00-1.51 1z"/>',
  box:        '<path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/><path d="M3.27 6.96L12 12.01l8.73-5.05M12 22.08V12"/>',
  phone:      '<path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72c.13.96.36 1.9.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.91.34 1.85.57 2.81.7A2 2 0 0122 16.92z"/>',
} as const

type IconKey = keyof typeof ICONS

const sections = computed(() => [
  {
    title: '运营',
    items: [
      { icon: 'dashboard' as IconKey, label: '工作台',     to: '/admin/dashboard',  badge: '' },
      { icon: 'building'  as IconKey, label: '房态看板',   to: '/admin/room-status', badge: '' },
      { icon: 'receipt'   as IconKey, label: '订单管理',   to: '/admin/orders',      badge: '' },
      { icon: 'bell'      as IconKey, label: '告警中心',   to: '/admin/alerts',      badge: unreadCount.value > 9 ? '9+' : String(unreadCount.value || '') },
    ],
  },
  {
    title: '管理',
    items: [
      { icon: 'bed'       as IconKey, label: '房型管理',   to: '/admin/room-types',  badge: '' },
      { icon: 'door'      as IconKey, label: '房间管理',   to: '/admin/rooms',       badge: '' },
      { icon: 'users'     as IconKey, label: '客户管理',   to: '/admin/customers',   badge: '' },
      { icon: 'user'      as IconKey, label: '员工管理',   to: '/admin/employees',   badge: '' },
      { icon: 'currency'  as IconKey, label: '财务管理',   to: '/admin/finance',     badge: '' },
      { icon: 'book'      as IconKey, label: '知识库',     to: '/admin/knowledge',   badge: '' },
      { icon: 'box'       as IconKey, label: '商品管理',   to: '/admin/products',    badge: '' },
      { icon: 'phone'     as IconKey, label: '服务呼叫',   to: '/admin/service-requests', badge: '' },
    ],
  },
  {
    title: '系统',
    items: [
      { icon: 'cog'        as IconKey, label: '系统设置',  to: '/admin/settings',    badge: '' },
    ],
  },
])

const isActive = (path: string) => route.path === path || route.path.startsWith(path + '/')

// --- Tabs (multi-tab like IDE) ---
const PATH_TITLE: Record<string, string> = {
  '/admin/dashboard': '工作台',
  '/admin/room-status': '房态看板',
  '/admin/orders': '订单管理',
  '/admin/room-types': '房型管理',
  '/admin/rooms': '房间管理',
  '/admin/customers': '客户管理',
  '/admin/employees': '员工管理',
  '/admin/finance': '财务管理',
  '/admin/alerts': '告警中心',
  '/admin/knowledge': '知识库',
  '/admin/settings': '系统设置',
  '/admin/products': '商品管理',
  '/admin/service-requests': '服务呼叫',
}
const openTabs = ref<Array<{ path: string; title: string }>>([])

function getTabTitle(p: string): string | null {
  const np = p.replace(/\/$/, '') || '/'
  if (PATH_TITLE[np]) return PATH_TITLE[np]
  if (np.startsWith('/admin/orders/')) return '订单详情'
  return null
}

watch(() => route.path, (p) => {
  const title = getTabTitle(p)
  if (!title) return
  const np = p.replace(/\/$/, '') || '/'
  if (!openTabs.value.find(t => t.path === np)) {
    openTabs.value.push({ path: np, title })
  }
}, { immediate: true })

function selectTab(path: string) {
  if (route.path !== path) router.push(path)
}
function closeTab(path: string) {
  const idx = openTabs.value.findIndex(t => t.path === path)
  if (idx === -1) return
  const wasActive = route.path.replace(/\/$/, '') === path
  openTabs.value.splice(idx, 1)
  if (wasActive) {
    if (openTabs.value.length === 0) router.push('/admin/dashboard')
    else router.push(openTabs.value[Math.min(idx, openTabs.value.length - 1)].path)
  }
}

async function loadNotifications() {
  loadingNotifications.value = true
  try {
    const { code, msg, data } = await getAlertList() as any
    if (code !== 200 || !Array.isArray(data)) {
      notifications.value = []
      return
    }
    notifications.value = (msg === 'mock' ? mockAlerts : data)
      .slice(0, 5)
      .map((a: any) => ({ ...a, unread: a.unread !== false }))
  } finally {
    loadingNotifications.value = false
  }
}

const mockAlerts = [
  { id: 1, type: '满房预警', level: 'warning', content: '今日 20:00 后大床房余量不足 5 间', time: new Date(Date.now() - 1000 * 60 * 15).toISOString(), unread: true },
  { id: 2, type: '订单异常', level: 'danger', content: '订单 #1024 支付超时，请人工跟进', time: new Date(Date.now() - 1000 * 60 * 45).toISOString(), unread: true },
  { id: 3, type: '系统通知', level: 'info', content: 'ai-service 向量库已迁移至 Milvus', time: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(), unread: false },
]

function toggleNotifications() {
  showNotifications.value = !showNotifications.value
  if (showNotifications.value) loadNotifications()
}

function closeNotifications() {
  showNotifications.value = false
}

function formatTime(iso: string) {
  const d = new Date(iso)
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

function levelClass(level: string) {
  if (level === 'danger' || level === 'critical') return 'danger'
  if (level === 'warning') return 'warning'
  return 'info'
}

function onClickNotification(_item: any) {
  router.push('/admin/alerts')
  closeNotifications()
}

function onDocumentClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  const bell = document.querySelector('.topbar-btn') as HTMLElement | null
  const panel = document.querySelector('.notification-panel') as HTMLElement | null
  if (bell?.contains(target) || panel?.contains(target)) return
  closeNotifications()
}

onMounted(() => {
  document.addEventListener('click', onDocumentClick)
  loadNotifications()
})

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick)
})
</script>

<template>
  <div class="app-shell">
    <!-- Top bar (full-width dark) -->
    <header class="topbar">
      <div class="topbar-left">
        <div class="topbar-brand">
          <svg class="topbar-brand-mark" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 20h20M4 20V8l8-5 8 5v12" />
            <path d="M9 20v-4h6v4" />
            <path d="M8 11h.01M12 11h.01M16 11h.01" />
          </svg>
          云端酒店
        </div>
        <button class="topbar-org" type="button" title="切换组织">
          <span class="topbar-org-name">云端酒店</span>
          <span class="topbar-org-sep">·</span>
          <span class="topbar-org-url">hotel.stellate.cn</span>
          <span class="topbar-org-arrow">↗</span>
        </button>
      </div>

      <div class="topbar-right">
        <button
          class="topbar-btn"
          :class="{ 'is-open': showNotifications }"
          type="button"
          title="通知"
          @click.stop="toggleNotifications"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M15 17h5l-1.4-1.4A2 2 0 0118 14V11a6 6 0 10-12 0v3a2 2 0 01-.6 1.6L4 17h5"/><path d="M9 17a3 3 0 006 0"/></svg>
          <span v-if="unreadCount > 0" class="topbar-dot">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
        </button>

        <div v-if="showNotifications" class="notification-panel">
          <div class="notification-header">
            <span class="notification-title">预警通知</span>
            <button class="notification-close" type="button" @click="closeNotifications" title="关闭">×</button>
          </div>
          <div v-if="loadingNotifications" class="notification-loading">加载中…</div>
          <div v-else-if="notifications.length === 0" class="notification-empty">暂无未读预警</div>
          <ul v-else class="notification-list">
            <li
              v-for="item in notifications"
              :key="item.id"
              class="notification-item"
              :class="{ 'is-unread': item.unread }"
              @click="onClickNotification(item)"
            >
              <div class="notification-row">
                <span class="notification-badge" :class="levelClass(item.level)">{{ item.type }}</span>
                <span class="notification-time">{{ formatTime(item.time) }}</span>
              </div>
              <div class="notification-content">{{ item.content }}</div>
            </li>
          </ul>
          <div class="notification-footer">
            <button class="notification-link" type="button" @click="router.push('/admin/alerts'); closeNotifications()">查看全部</button>
          </div>
        </div>
      </div>
    </header>

    <!-- Body: sidebar + content -->
    <div class="app-body">
      <aside class="sidebar">
        <div v-for="sec in sections" :key="sec.title" class="sidebar-section">
          <div class="sidebar-section-title">{{ sec.title }}</div>
          <router-link
            v-for="item in sec.items"
            :key="item.to"
            :to="item.to"
            :class="['sidebar-item', { 'is-active': isActive(item.to) }]"
          >
            <svg class="sidebar-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" v-html="ICONS[item.icon]"></svg>
            <span class="sidebar-label">{{ item.label }}</span>
            <span v-if="item.badge" class="sidebar-badge">{{ item.badge }}</span>
          </router-link>
        </div>
      </aside>

      <main class="content">
        <div v-if="openTabs.length > 0" class="tabsbar">
          <div class="tabsbar-scroll">
            <div
              v-for="t in openTabs"
              :key="t.path"
              :class="['tab', { 'is-active': route.path.replace(/\/$/, '') === t.path }]"
              @click="selectTab(t.path)"
            >
              <span class="tab-title">{{ t.title }}</span>
              <button class="tab-close" type="button" @click.stop="closeTab(t.path)" title="关闭">×</button>
            </div>
          </div>
        </div>
        <div class="content-scroll">
          <router-view v-slot="{ Component }">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>
