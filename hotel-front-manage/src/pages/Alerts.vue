<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getAlertList, markAlertAsRead, markAllAlertsAsRead, getAlertRules } from '../services/alertService'

interface Alert {
  id: number
  type: string
  level: string
  content: string
  time: string
  unread: boolean
}

interface AlertRule {
  id: number
  name: string
  threshold: string
  status: string
  description: string
}

const alertList = ref<Alert[]>([])
const alertRules = ref<AlertRule[]>([])

const unreadCount = computed(() => alertList.value.filter(item => item.unread).length)

async function loadAlerts() {
  const result = await getAlertList()
  alertList.value = result?.data || result
}

async function loadRules() {
  const result = await getAlertRules()
  alertRules.value = result?.data || result
}

async function handleMarkAsRead(alertId: number) {
  await markAlertAsRead(alertId)
  const alert = alertList.value.find(item => item.id === alertId)
  if (alert) {
    alert.unread = false
  }
}

async function handleMarkAllAsRead() {
  await markAllAlertsAsRead()
  alertList.value.forEach(item => {
    item.unread = false
  })
}

function getLevelClass(level: string) {
  switch (level) {
    case 'WARNING':
    case '警告': return 'level-warning'
    case 'INFO':
    case '提醒': return 'level-info'
    case 'ERROR':
    case '严重': return 'level-danger'
    default: return ''
  }
}

onMounted(() => {
  loadAlerts()
  loadRules()
})
</script>

<template>
  <div class="page-wrap">
    <div class="page-header">
      <div>
        <h1>告警中心</h1>
        <p class="page-subtitle">展示系统告警与告警规则，支持快速标记已读。</p>
      </div>
      <div v-if="unreadCount > 0" class="header-actions">
        <button class="btn btn-secondary" @click="handleMarkAllAsRead">
          全部标为已读 ({{ unreadCount }})
        </button>
      </div>
    </div>

    <div class="table" style="margin-bottom: 18px;">
      <div class="row header">
        <div>类型</div>
        <div>级别</div>
        <div>内容</div>
        <div>时间</div>
        <div>状态</div>
        <div>操作</div>
      </div>
      <div class="row" v-for="alert in alertList" :key="alert.id" :class="{ 'unread': alert.unread }">
        <div>{{ alert.type }}</div>
        <div>
          <span class="level-badge" :class="getLevelClass(alert.level)">{{ alert.level }}</span>
        </div>
        <div>{{ alert.content }}</div>
        <div>{{ alert.time }}</div>
        <div>{{ alert.unread ? '未读' : '已读' }}</div>
        <div>
          <button v-if="alert.unread" class="btn btn-sm btn-primary" @click="handleMarkAsRead(alert.id)">
            标为已读
          </button>
          <span v-else class="text-muted">已读</span>
        </div>
      </div>
      <div v-if="alertList.length === 0" class="empty-panel">暂无告警信息</div>
    </div>

    <div class="form-card">
      <h2>告警规则</h2>
      <p class="page-subtitle">规则列表用于自动触发告警。</p>
      <div class="form-grid" style="grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px;">
        <div class="rule-card" v-for="rule in alertRules" :key="rule.id">
          <div class="rule-label">{{ rule.name }}</div>
          <div>阈值：{{ rule.threshold }}</div>
          <div>状态：<span :class="rule.status === '启用' ? 'status-active' : 'status-inactive'">{{ rule.status }}</span></div>
          <div class="rule-desc">{{ rule.description }}</div>
        </div>
      </div>
      <div v-if="alertRules.length === 0" class="empty-panel">暂无告警规则</div>
    </div>
  </div>
</template>