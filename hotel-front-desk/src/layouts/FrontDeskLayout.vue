<template>
  <el-container class="fd-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="fd-aside">
      <!-- Logo 区 -->
      <div class="fd-logo">
        <div class="logo-icon">
          <el-icon :size="24"><OfficeBuilding /></el-icon>
        </div>
        <div v-show="!isCollapse" class="logo-texts">
          <span class="logo-title">云端酒店</span>
          <span class="logo-sub">酒店管理系统·前台端</span>
        </div>
        <el-icon
          class="collapse-btn"
          :size="18"
          @click="isCollapse = !isCollapse"
        >
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </div>

      <!-- 菜单 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
      >
        <el-menu-item index="dashboard" @click="goTo('/front-desk/dashboard')">
          <el-icon><Odometer /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item index="orders" @click="goTo('/front-desk/orders')">
          <el-icon><Document /></el-icon>
          <template #title>订单中心</template>
        </el-menu-item>
        <el-menu-item index="checkout" @click="goTo('/front-desk/checkout')">
          <el-icon><SwitchButton /></el-icon>
          <template #title>退房办理</template>
        </el-menu-item>
        <el-menu-item index="room-status" @click="goTo('/front-desk/room-status')">
          <el-icon><Grid /></el-icon>
          <template #title>房态管理</template>
        </el-menu-item>
        <el-menu-item index="billing" @click="goTo('/front-desk/billing')">
          <el-icon><Money /></el-icon>
          <template #title>账单管理</template>
        </el-menu-item>
        <el-menu-item index="notifications" @click="goTo('/front-desk/notifications')">
          <el-icon><Bell /></el-icon>
          <template #title>消息通知</template>
        </el-menu-item>
        <el-menu-item index="settings" @click="goTo('/front-desk/settings')">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>

      <!-- 底部 Cloud Hotel -->
      <div v-show="!isCollapse" class="fd-sidebar-footer">
        <svg class="lotus-icon" viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
          <path d="M12 4 C12 4 9 7 9 11 C9 11 12 9 12 9 C12 9 15 11 15 11 C15 7 12 4 12 4 Z" fill="#d4a853" />
          <path d="M5 11 C5 11 6 14.5 10.5 15.5 C10.5 15.5 8 11 8 11 C8 11 5 11 5 11 Z" fill="#c49b48" />
          <path d="M19 11 C19 11 18 14.5 13.5 15.5 C13.5 15.5 16 11 16 11 C16 11 19 11 19 11 Z" fill="#c49b48" />
          <path d="M12 9 C12 9 11 13 12 21 C13 13 12 9 12 9 Z" fill="#94a3b8" />
        </svg>
        <div class="footer-texts">
          <span class="footer-brand">Cloud Hotel</span>
          <span class="footer-slogan">舒适·便捷·尊享</span>
        </div>
      </div>
    </el-aside>

    <!-- 右侧区域 -->
    <el-container class="fd-right">
      <!-- 顶部 Header -->
      <el-header class="fd-header">
        <div class="header-left">
          <span class="header-greeting">{{ greeting }}</span>
        </div>
        <div class="header-right">
          <el-popover
            v-model:visible="noticeVisible"
            placement="bottom"
            :width="340"
            trigger="click"
            popper-class="notice-popover"
          >
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notice-badge">
                <el-button circle size="small">
                  <el-icon :size="16"><Bell /></el-icon>
                </el-button>
              </el-badge>
            </template>
            <div class="notice-panel">
              <div class="notice-header">
                <span class="notice-title">通知</span>
                <el-button
                  v-if="unreadCount > 0"
                  link
                  type="primary"
                  size="small"
                  @click="markAllNoticeRead"
                >全部已读</el-button>
              </div>
              <div v-if="loadingNotifications" class="notice-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>加载中…</span>
              </div>
              <div v-else-if="notifications.length === 0" class="notice-empty">暂无通知</div>
              <div v-else class="notice-list">
                <div
                  v-for="n in notifications"
                  :key="n.id"
                  class="notice-item"
                  :class="{ unread: !n.is_read }"
                  @click="markNoticeRead(n.id)"
                >
                  <div class="notice-item-title">
                    <span v-if="!n.is_read" class="notice-dot"></span>
                    <span>{{ n.type }}</span>
                  </div>
                  <div class="notice-item-content">{{ n.content }}</div>
                  <div class="notice-item-time">{{ formatNoticeTime(n.trigger_time) }}</div>
                </div>
              </div>
              <div class="notice-footer">
                <el-button link size="small" @click="noticeVisible = false; router.push('/front-desk/notifications')">
                  查看更多
                </el-button>
              </div>
            </div>
          </el-popover>
          <div class="header-user">
            <div class="avatar-dot">
              {{ (auth.displayName || auth.user?.username || 'U').charAt(0) }}
            </div>
            <span class="username">{{ auth.displayName }}</span>
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="el-dropdown-link">
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon> 个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon> 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="fd-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Fold, Expand, Bell, ArrowDown, SwitchButton, Loading,
  Odometer, Document, User, Grid, Money, Setting, OfficeBuilding,
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { logout } from '@/api/auth'
import { getAlertStatus, markAlertAsRead, markAllAlertsAsRead } from '@/api/alert'
import type { AlertItem } from '@/types'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isCollapse = ref(false)
const unreadCount = ref(0)
const notifications = ref<AlertItem[]>([])
const loadingNotifications = ref(false)
const noticeVisible = ref(false)
let noticeTimer: ReturnType<typeof setInterval> | null = null

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

async function loadNotifications() {
  try {
    loadingNotifications.value = true
    const status = await getAlertStatus()
    notifications.value = status?.alerts || []
    unreadCount.value = status?.unread_count ?? 0
  } catch {
    // axios 拦截器已提示错误
  } finally {
    loadingNotifications.value = false
  }
}

async function markNoticeRead(id: number, event?: Event) {
  event?.stopPropagation()
  try {
    await markAlertAsRead(id)
    const n = notifications.value.find(item => item.id === id)
    if (n && !n.is_read) {
      n.is_read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  } catch {
    // axios 拦截器已提示错误
  }
}

async function markAllNoticeRead() {
  try {
    await markAllAlertsAsRead()
    notifications.value.forEach(n => (n.is_read = true))
    unreadCount.value = 0
  } catch {
    // axios 拦截器已提示错误
  }
}

function formatNoticeTime(time?: string) {
  if (!time) return ''
  const d = new Date(time)
  if (Number.isNaN(d.getTime())) return time
  return d.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  loadNotifications()
  noticeTimer = setInterval(loadNotifications, 30000)
})
onBeforeUnmount(() => {
  if (noticeTimer) clearInterval(noticeTimer)
})

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/front-desk/orders')) return 'orders'
  if (p.startsWith('/front-desk/billing')) return 'billing'
  if (p.startsWith('/front-desk/checkout')) return 'checkout'
  if (p.startsWith('/front-desk/room-status')) return 'room-status'
  if (p.startsWith('/front-desk/notifications')) return 'notifications'
  if (p.startsWith('/front-desk/settings') || p.startsWith('/front-desk/profile')) return 'settings'
  if (p.startsWith('/front-desk/dashboard')) return 'dashboard'
  return p
})

function goTo(path: string) {
  if (route.path !== path) router.push(path)
}

async function handleLogout() {
  await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
  try { await logout() } catch { /* ignore */ }
  auth.clearAuth()
  router.push('/login')
}

function handleCommand(command: string) {
  if (command === 'logout') handleLogout()
  else if (command === 'profile') router.push('/front-desk/settings')
}
</script>

<style scoped>
.fd-layout {
  min-height: 100vh;
}

/* ====== 侧边栏 ====== */
.fd-aside {
  background: var(--fd-sidebar-bg);
  display: flex;
  flex-direction: column;
  transition: width 0.28s;
  overflow: hidden;
  border-right: none;
}

.fd-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  height: 72px;
  flex-shrink: 0;
}

.logo-icon {
  color: #ffffff;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-texts {
  display: flex;
  flex-direction: column;
  min-width: 0;
  line-height: 1.25;
}

.logo-title {
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  white-space: nowrap;
}

.logo-sub {
  color: #94a3b8;
  font-size: 11px;
  white-space: nowrap;
}

.collapse-btn {
  cursor: pointer;
  color: #64748b;
  transition: color 0.2s;
  padding: 4px;
  border-radius: 4px;
  margin-left: auto;
  flex-shrink: 0;
}

.collapse-btn:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.06);
}

/* 菜单样式覆盖 */
.fd-aside :deep(.el-menu) {
  background: transparent;
  border-right: none;
  padding: 10px 0;
  flex: 1;
  overflow-y: auto;
}

.fd-aside :deep(.el-menu-item) {
  color: var(--fd-sidebar-text);
  margin: 2px 10px;
  border-radius: 8px;
  height: 46px;
  line-height: 46px;
}

.fd-aside :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.05);
  color: #e2e8f0;
}

.fd-aside :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  font-weight: 600;
}

.fd-aside :deep(.el-menu-item .el-icon) {
  font-size: 17px;
  margin-right: 10px;
}

/* 折叠模式 */
.fd-aside :deep(.el-menu--collapse) {
  padding: 10px 0;
}

.fd-aside :deep(.el-menu--collapse .el-menu-item) {
  margin: 4px 10px;
  padding: 0 !important;
  display: flex;
  justify-content: center;
}

/* 底部 Cloud Hotel */
.fd-sidebar-footer {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.lotus-icon {
  flex-shrink: 0;
}

.footer-texts {
  display: flex;
  flex-direction: column;
  min-width: 0;
  line-height: 1.3;
}

.footer-brand {
  color: #f1f5f9;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.footer-slogan {
  color: #94a3b8;
  font-size: 11px;
  white-space: nowrap;
}

/* ====== 右侧区域 ====== */
.fd-right {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ====== 头部 ====== */
.fd-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid var(--fd-border);
  height: 64px;
  padding: 0 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.header-greeting {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}

.header-search {
  width: 440px;
  max-width: 42vw;
}

.header-search :deep(.el-input__wrapper) {
  background: #f8f9fa;
  box-shadow: none !important;
  border: 1px solid var(--fd-border);
}

.header-search :deep(.el-input__inner) {
  color: #374151;
}

.header-search :deep(.el-input__inner::placeholder) {
  color: #9ca3af;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-shrink: 0;
}

.notice-badge :deep(.el-button) {
  color: #64748b;
  border-color: #e2e8f0;
}

.notice-badge :deep(.el-button:hover) {
  color: var(--fd-primary);
  border-color: var(--fd-primary);
}

.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.header-user:hover {
  background: #f8fafc;
}

.avatar-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #d4a853 0%, #c49b48 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.username {
  font-size: 13px;
  color: #374151;
  font-weight: 600;
}

.el-dropdown-link {
  color: #94a3b8;
  display: flex;
  align-items: center;
}

.notice-panel {
  max-height: 400px;
  display: flex;
  flex-direction: column;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 1px solid #f1f5f9;
}

.notice-header .notice-title {
  font-weight: 600;
  font-size: 14px;
  color: #111827;
}

.notice-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 28px;
  color: #94a3b8;
  font-size: 13px;
}

.notice-empty {
  padding: 32px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}

.notice-list {
  overflow-y: auto;
  max-height: 300px;
}

.notice-item {
  padding: 12px 14px;
  border-bottom: 1px solid #f8fafc;
  cursor: pointer;
  transition: background 0.2s;
}

.notice-item:hover {
  background: #f8fafc;
}

.notice-item.unread {
  background: #fafafa;
}

.notice-item-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.notice-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ef4444;
  flex-shrink: 0;
}

.notice-item-content {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-item-time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

.notice-footer {
  padding: 10px 14px;
  text-align: center;
  border-top: 1px solid #f1f5f9;
}

/* ====== 主内容 ====== */
.fd-main {
  padding: 24px;
  background: var(--fd-bg);
  overflow-y: auto;
}
</style>
