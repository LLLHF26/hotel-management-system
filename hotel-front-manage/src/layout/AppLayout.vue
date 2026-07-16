<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const menuItems = [
  { icon: '🏠', label: '工作台', to: '/admin/dashboard' },
  { icon: '🗺️', label: '房态看板', to: '/admin/room-status' },
  { icon: '🧾', label: '订单管理', to: '/admin/orders' },
  { icon: '🛏️', label: '房型管理', to: '/admin/room-types' },
  { icon: '🚪', label: '房间管理', to: '/admin/rooms' },
  { icon: '👥', label: '客户管理', to: '/admin/customers' },
  { icon: '👤', label: '员工管理', to: '/admin/employees' },
  { icon: '💰', label: '财务管理', to: '/admin/finance' },
  { icon: '⚠️', label: '告警中心', to: '/admin/alerts' },
  { icon: '📚', label: '知识库', to: '/admin/knowledge' },
  { icon: '⚙️', label: '系统设置', to: '/admin/settings' },
  { icon: '👤', label: '个人信息', to: '/admin/profile' }
]

const activeLabel = computed(() => {
  const exact = menuItems.find(item => item.to === route.path)
  if (exact) return exact.label
  if (route.path.startsWith('/admin/orders')) return '订单管理'
  if (route.path.startsWith('/admin/room-status')) return '房态看板'
  if (route.path.startsWith('/admin/room-types')) return '房型管理'
  if (route.path.startsWith('/admin/rooms')) return '房间管理'
  if (route.path.startsWith('/admin/customers')) return '客户管理'
  if (route.path.startsWith('/admin/employees')) return '员工管理'
  if (route.path.startsWith('/admin/finance')) return '财务管理'
  if (route.path.startsWith('/admin/alerts')) return '告警中心'
  if (route.path.startsWith('/admin/knowledge')) return '知识库'
  if (route.path.startsWith('/admin/settings')) return '系统设置'
  if (route.path.startsWith('/admin/profile')) return '个人信息'
  return '工作台'
})

const isActiveMenu = (path: string) => {
  if (path === '/') {
    return route.path === '/' || route.path === ''
  }
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<template>
  <div class="dashboard-root">
    <div class="dashboard-container">
      <aside class="sidebar">
        <div class="brand">云端酒店</div>
        <nav>
          <ul>
            <li v-for="item in menuItems" :key="item.to">
              <router-link :to="item.to" :class="['sidebar-link', { active: isActiveMenu(item.to) }]">
                <span class="sidebar-icon">{{ item.icon }}</span>
                {{ item.label }}
              </router-link>
            </li>
          </ul>
        </nav>
        <div class="user-card">
          <div class="user-avatar">李</div>
          <div class="user-meta">
            <div class="user-name">李建国</div>
            <div class="user-role">管理员</div>
          </div>
        </div>
      </aside>

      <div class="main">
        <header class="topbar">
          <div>
            <div class="page-title">{{ activeLabel }}</div>
            <div class="page-subtitle">{{ activeLabel === '工作台' ? '酒店运营概览' : '实时数据与快捷入口' }}</div>
          </div>
          <div class="topbar-actions">
            <button class="notify-button" type="button">
              <span class="notify-dot">3</span>
              <span class="notify-icon">🔔</span>
            </button>
            <div class="topbar-user">
              <div class="topbar-avatar">李</div>
              <div class="topbar-user-info">
                <div class="topbar-user-name">李建国</div>
                <div class="topbar-user-role">管理员</div>
              </div>
            </div>
          </div>
        </header>
        <div class="main-content">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>
