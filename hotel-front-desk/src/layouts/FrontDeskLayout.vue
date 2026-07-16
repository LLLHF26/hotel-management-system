<template>
  <el-container class="fd-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="fd-aside">
      <div class="fd-logo">
        <div class="logo-icon">
          <el-icon :size="24"><OfficeBuilding /></el-icon>
        </div>
        <span v-show="!isCollapse" class="logo-text">云端酒店</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        :collapse="isCollapse"
        :collapse-transition="false"
      >
        <el-menu-item index="/front-desk/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>
        <el-menu-item index="/front-desk/room-status">
          <el-icon><Grid /></el-icon>
          <template #title>房态看板</template>
        </el-menu-item>
        <el-menu-item index="/front-desk/orders">
          <el-icon><Document /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>
        <el-menu-item index="/front-desk/customers">
          <el-icon><User /></el-icon>
          <template #title>客户管理</template>
        </el-menu-item>
        <el-menu-item index="/front-desk/profile">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>

      <!-- 底部用户信息 -->
      <div v-if="!isCollapse" class="fd-sidebar-footer">
        <div class="user-avatar">
          {{ (auth.displayName || auth.username || 'U').charAt(0) }}
        </div>
        <div class="user-info">
          <span class="user-name">{{ auth.displayName || auth.username }}</span>
          <span class="user-role">前台管理</span>
        </div>
      </div>
    </el-aside>

    <!-- 右侧区域 -->
    <el-container class="fd-right">
      <!-- 顶部 Header -->
      <el-header class="fd-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="18"
            @click="isCollapse = !isCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notice-badge">
            <el-button circle size="small" @click="$router.push('/front-desk/dashboard')">
              <el-icon :size="16"><Bell /></el-icon>
            </el-button>
          </el-badge>
          <div class="header-user">
            <div class="avatar-dot">
              {{ (auth.displayName || auth.username || 'U').charAt(0) }}
            </div>
            <span class="username">{{ auth.displayName || auth.username }}</span>
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
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Fold, Expand, Bell, ArrowDown, SwitchButton,
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { logout } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isCollapse = ref(false)
const unreadCount = ref(3)

const activeMenu = computed(() => {
  if (route.path.startsWith('/front-desk/orders')) return '/front-desk/orders'
  if (route.path.startsWith('/front-desk/customers')) return '/front-desk/customers'
  return route.path
})

const pageTitle = computed(() => (route.meta.title as string) || '工作台')

async function handleLogout() {
  await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
  try { await logout() } catch { /* ignore */ }
  auth.clearAuth()
  router.push('/login')
}

function handleCommand(command: string) {
  if (command === 'logout') handleLogout()
  else if (command === 'profile') router.push('/front-desk/profile')
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
  height: 60px;
  flex-shrink: 0;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #d4a853 0%, #c49b48 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.logo-text {
  color: #e8ecf1;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1px;
  white-space: nowrap;
}

/* 菜单样式覆盖 */
.fd-aside :deep(.el-menu) {
  background: transparent;
  border-right: none;
  padding: 8px 0;
  flex: 1;
  overflow-y: auto;
}

.fd-aside :deep(.el-menu-item) {
  color: var(--fd-sidebar-text);
  margin: 2px 8px;
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
}

.fd-aside :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.05);
  color: #e2e8f0;
}

.fd-aside :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(212, 168, 83, 0.15), rgba(212, 168, 83, 0.05));
  color: #d4a853;
  font-weight: 600;
}

.fd-aside :deep(.el-menu-item .el-icon) {
  font-size: 17px;
  margin-right: 10px;
}

/* 折叠模式 */
.fd-aside :deep(.el-menu--collapse) {
  padding: 8px 0;
}

.fd-aside :deep(.el-menu--collapse .el-menu-item) {
  margin: 4px 8px;
  padding: 0 !important;
  display: flex;
  justify-content: center;
}

/* 底部用户 */
.fd-sidebar-footer {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.1);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #d4a853 0%, #c49b48 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  color: #e2e8f0;
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  color: var(--fd-sidebar-text);
  font-size: 11px;
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
  background: #fff;
  border-bottom: 1px solid var(--fd-border);
  height: 56px;
  padding: 0 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  cursor: pointer;
  color: #64748b;
  transition: color 0.2s;
  padding: 4px;
  border-radius: 4px;
}

.collapse-btn:hover {
  color: var(--fd-primary);
  background: #f1f5f9;
}

.header-left :deep(.el-breadcrumb) {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
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
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #d4a853 0%, #c49b48 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.username {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.el-dropdown-link {
  color: #94a3b8;
  display: flex;
  align-items: center;
}

/* ====== 主内容 ====== */
.fd-main {
  padding: 20px 24px;
  background: var(--fd-bg);
  overflow-y: auto;
}
</style>
