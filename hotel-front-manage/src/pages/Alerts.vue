<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getAlertList, markAlertAsRead, markAllAlertsAsRead, getAlertRules, createAlertRule, updateAlertRule, deleteAlertRule, deleteAlert, batchMarkAlertsAsRead, batchDeleteAlerts } from '../services/alertService'
import { useToast } from '../composables/useToast'

const toast = useToast()

interface Alert { id: number; type: string; level: string; content: string; time: string; unread: boolean }
interface Rule  { id: number; name: string; threshold: string; status: string; description: string }

const alerts  = ref<Alert[]>([])
const rules   = ref<Rule[]>([])
const loading = ref(true)
const unread  = computed(() => alerts.value.filter(a => a.unread).length)
const filter  = ref<'all' | 'unread'>('all')
const selectedAlertIds = ref<number[]>([])
const allSelected = computed(() => visibleAlerts.value.length > 0 && visibleAlerts.value.every(a => selectedAlertIds.value.includes(a.id)))
const someSelected = computed(() => selectedAlertIds.value.length > 0 && !allSelected.value)

const showRuleModal = ref(false)
const editingRule = ref<Rule | null>(null)
const ruleForm = ref({ type: '', threshold: 0.01, is_enabled: true })
const isSavingRule = ref(false)

const ruleTypeOptions = [
  { value: '价格异常', label: '价格异常' },
  { value: '异常退单', label: '异常退单' },
  { value: '满房预警', label: '满房预警' },
]

const visibleAlerts = computed(() => filter.value === 'unread' ? alerts.value.filter(a => a.unread) : alerts.value)

function openEditRule(r: Rule) {
  editingRule.value = r
  ruleForm.value = {
    type: r.name,
    threshold: Number(r.threshold) || 0,
    is_enabled: r.status === '启用',
  }
  showRuleModal.value = true
}

function closeRuleModal() {
  showRuleModal.value = false
  editingRule.value = null
}

async function saveRule() {
  const threshold = Number(ruleForm.value.threshold)
  if (isNaN(threshold) || threshold < 0 || threshold > 1) {
    toast.error('阈值必须在 0 ~ 1 之间（对应 0% ~ 100%）')
    return
  }
  isSavingRule.value = true
  try {
    if (editingRule.value) {
      const result = await updateAlertRule(editingRule.value.id, {
        threshold,
        is_enabled: ruleForm.value.is_enabled,
      })
      if (result.code !== 200) throw new Error(result.msg || '更新失败')
      toast.success('规则已更新')
    } else {
      const result = await createAlertRule({
        type: ruleForm.value.type,
        threshold,
        is_enabled: ruleForm.value.is_enabled,
      })
      if (result.code !== 200) throw new Error(result.msg || '创建失败')
      toast.success('规则已创建')
    }
    closeRuleModal()
    await load()
  } catch (e: any) {
    toast.error(e?.message || '保存失败')
  } finally {
    isSavingRule.value = false
  }
}

async function handleDeleteRule(r: Rule) {
  if (!confirm(`确定删除告警规则「${r.name}」吗？`)) return
  try {
    const result = await deleteAlertRule(r.id)
    if (result.code !== 200) throw new Error(result.msg || '删除失败')
    toast.success('规则已删除')
    await load()
  } catch (e: any) {
    toast.error(e?.message || '删除失败')
  }
}

async function handleToggleStatus(r: Rule) {
  const enabled = r.status !== '启用'
  try {
    const result = await updateAlertRule(r.id, { is_enabled: enabled })
    if (result.code !== 200) throw new Error(result.msg || '操作失败')
    r.status = enabled ? '启用' : '停用'
    toast.success(enabled ? '已启用' : '已停用')
  } catch (e: any) {
    toast.error(e?.message || '操作失败')
  }
}

function levelBadge(l: string) {
  const k = (l || '').toUpperCase()
  if (k === 'WARNING' || k === '警告') return 'badge-warning'
  if (k === 'ERROR'   || k === '严重') return 'badge-danger'
  if (k === 'INFO'    || k === '提醒') return 'badge-info'
  return 'badge-neutral'
}
function levelLabel(l: string) {
  const k = (l || '').toUpperCase()
  if (k === 'WARNING' || k === '警告') return '警告'
  if (k === 'ERROR'   || k === '严重') return '严重'
  if (k === 'INFO'    || k === '提醒') return '提醒'
  return l
}

async function load() {
  loading.value = true
  const r = await getAlertList()
  alerts.value = r?.data || r || []
  selectedAlertIds.value = []
  const rr = await getAlertRules()
  rules.value = rr?.data || rr || []
  loading.value = false
}
onMounted(load)

async function markRead(id: number) {
  try {
    await markAlertAsRead(id)
    const a = alerts.value.find(x => x.id === id)
    if (a) a.unread = false
    toast.success('已标记为已读')
  } catch (e: any) { toast.error(e?.message || '操作失败') }
}
async function markAll() {
  try {
    await markAllAlertsAsRead()
    alerts.value.forEach(a => (a.unread = false))
    toast.success('已全部标记为已读')
  } catch (e: any) { toast.error(e?.message || '操作失败') }
}

function toggleSelect(id: number) {
  const idx = selectedAlertIds.value.indexOf(id)
  if (idx >= 0) selectedAlertIds.value.splice(idx, 1)
  else selectedAlertIds.value.push(id)
}
function toggleSelectAll() {
  if (allSelected.value) {
    visibleAlerts.value.forEach(a => {
      const idx = selectedAlertIds.value.indexOf(a.id)
      if (idx >= 0) selectedAlertIds.value.splice(idx, 1)
    })
  } else {
    visibleAlerts.value.forEach(a => {
      if (!selectedAlertIds.value.includes(a.id)) selectedAlertIds.value.push(a.id)
    })
  }
}
async function deleteSingleAlert(a: Alert) {
  if (!confirm(`确定删除这条「${a.type}」告警吗？`)) return
  try {
    const result = await deleteAlert(a.id)
    if (result.code !== 200) throw new Error(result.msg || '删除失败')
    toast.success('告警已删除')
    await load()
  } catch (e: any) { toast.error(e?.message || '删除失败') }
}
async function handleBatchRead() {
  const ids = selectedAlertIds.value.slice()
  if (ids.length === 0) return
  try {
    const result = await batchMarkAlertsAsRead(ids)
    if (result.code !== 200) throw new Error(result.msg || '操作失败')
    alerts.value.forEach(a => { if (ids.includes(a.id)) a.unread = false })
    selectedAlertIds.value = []
    toast.success('已批量标记为已读')
  } catch (e: any) { toast.error(e?.message || '操作失败') }
}
async function handleBatchDelete() {
  const ids = selectedAlertIds.value.slice()
  if (ids.length === 0) return
  if (!confirm(`确定删除选中的 ${ids.length} 条告警吗？`)) return
  try {
    const result = await batchDeleteAlerts(ids)
    if (result.code !== 200) throw new Error(result.msg || '删除失败')
    toast.success(`已删除 ${ids.length} 条告警`)
    await load()
  } catch (e: any) { toast.error(e?.message || '删除失败') }
}
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">告警中心</h1>
        <p class="page-subtitle">系统告警与告警规则，支持快速标记已读。</p>
      </div>
      <div class="page-actions" v-if="unread > 0">
        <button class="btn" @click="markAll">全部标为已读 ({{ unread }})</button>
      </div>
    </div>

    <div class="card mb-4">
      <div class="card-head">
        <div>
          <div class="card-title">告警列表</div>
          <div class="card-sub">最近告警事件</div>
        </div>
        <div class="page-actions">
          <div class="filterbar" style="padding: 0; border: 0; margin: 0; background: transparent;">
            <button :class="['btn btn-sm', filter==='all' ? 'btn-primary' : '']" @click="filter='all'">全部</button>
            <button :class="['btn btn-sm', filter==='unread' ? 'btn-primary' : '']" @click="filter='unread'">未读 ({{ unread }})</button>
          </div>
        </div>
      </div>
      <div class="batch-bar" v-if="selectedAlertIds.length > 0">
        <span class="text-muted fz-sm">已选 {{ selectedAlertIds.length }} 条</span>
        <button class="btn btn-sm" @click="handleBatchRead">批量已读</button>
        <button class="btn btn-sm btn-danger" @click="handleBatchDelete">批量删除</button>
      </div>
      <div style="overflow-x: auto;">
        <table class="table">
          <thead>
            <tr>
              <th class="checkbox-col"><input type="checkbox" :checked="allSelected" :indeterminate.prop="someSelected" @change="toggleSelectAll" /></th>
              <th>类型</th>
              <th>级别</th>
              <th>内容</th>
              <th>时间</th>
              <th>状态</th>
              <th class="actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in visibleAlerts" :key="a.id" :style="a.unread ? 'background: var(--accent-soft-2);' : ''">
              <td class="checkbox-col"><input type="checkbox" :checked="selectedAlertIds.includes(a.id)" @change="toggleSelect(a.id)" /></td>
              <td class="fw-600">{{ a.type }}</td>
              <td><span class="badge" :class="levelBadge(a.level)">{{ levelLabel(a.level) }}</span></td>
              <td>{{ a.content }}</td>
              <td class="mono fz-xs">{{ a.time }}</td>
              <td>
                <span v-if="a.unread" class="badge badge-info">未读</span>
                <span v-else class="badge badge-neutral">已读</span>
              </td>
              <td class="actions">
                <div class="row" style="gap: 8px; justify-content: flex-end;">
                  <button v-if="a.unread" class="btn btn-sm btn-primary" @click="markRead(a.id)">标为已读</button>
                  <button class="btn btn-sm btn-danger" @click="deleteSingleAlert(a)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="loading">
              <td colspan="7">
                <div class="loading-wrap">
                  <div class="loading-dual-ring"></div>
                  <span class="loading-text">加载中…</span>
                </div>
              </td>
            </tr>
            <tr v-if="!loading && visibleAlerts.length === 0"><td colspan="7" class="empty">暂无告警</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <div class="card-head">
        <div>
          <div class="card-title">告警规则</div>
          <div class="card-sub">规则用于自动触发告警</div>
        </div>
      </div>
      <div class="card-body">
        <div v-if="loading" class="loading-wrap">
          <div class="loading-dual-ring"></div>
          <span class="loading-text">加载中…</span>
        </div>
        <div v-else-if="rules.length === 0" class="empty">暂无告警规则</div>
        <div v-else style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--sp-3);">
          <div v-for="r in rules" :key="r.id" class="rule-card">
            <div class="row row-gap-2" style="align-items: center;">
              <span class="fw-600">{{ r.name }}</span>
              <span class="filterbar-spacer"></span>
              <button
                class="badge rule-status-badge"
                :class="r.status === '启用' ? 'badge-success' : 'badge-neutral'"
                @click="handleToggleStatus(r)"
              >{{ r.status }}</button>
              <button class="btn btn-sm" @click="openEditRule(r)">编辑</button>
              <button class="btn btn-sm btn-danger" @click="handleDeleteRule(r)">删除</button>
            </div>
            <div class="text-muted fz-sm">阈值：{{ (Number(r.threshold) * 100).toFixed(0) }}%</div>
            <div v-if="r.description" class="text-muted fz-sm">{{ r.description }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 规则新增/编辑弹窗 -->
    <div v-if="showRuleModal" class="modal-overlay" @click.self="closeRuleModal">
      <div class="modal" style="max-width: 420px;">
        <div class="modal-head">
          <div class="modal-title">{{ editingRule ? '编辑告警规则' : '新增告警规则' }}</div>
          <button class="modal-close" @click="closeRuleModal" aria-label="关闭">✕</button>
        </div>
        <div class="modal-body" style="display: flex; flex-direction: column; gap: 16px;">
          <div class="form-group">
            <label class="form-label">规则类型</label>
            <div v-if="editingRule" class="form-static">{{ ruleForm.type }}</div>
            <template v-else>
              <select v-model="ruleForm.type" class="form-input">
                <option v-for="o in ruleTypeOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
              </select>
            </template>
          </div>

          <div class="form-group">
            <label class="form-label">阈值（0 ~ 1，即 0% ~ 100%）</label>
            <input
              v-model.number="ruleForm.threshold"
              type="number"
              step="0.01"
              min="0"
              max="1"
              class="form-input"
              placeholder="如 0.01 表示 1%"
            />
            <div class="form-hint">当前：{{ (ruleForm.threshold * 100).toFixed(0) }}%</div>
          </div>

          <div class="form-group">
            <label class="form-label">状态</label>
            <div class="row" style="gap: 12px;">
              <label class="radio-label">
                <input v-model="ruleForm.is_enabled" type="radio" :value="true" /> 启用
              </label>
              <label class="radio-label">
                <input v-model="ruleForm.is_enabled" type="radio" :value="false" /> 停用
              </label>
            </div>
          </div>
        </div>
        <div class="modal-foot">
          <button class="btn" @click="closeRuleModal">取消</button>
          <button class="btn btn-primary" :disabled="isSavingRule" @click="saveRule">
            {{ isSavingRule ? '保存中…' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.rule-card {
  padding: var(--sp-4);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
}
.rule-status-badge {
  cursor: pointer;
  border: 0;
  background: transparent;
}
.rule-status-badge:hover { opacity: 0.85; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 14px; font-weight: 500; color: var(--text); }
.form-input {
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  font-size: 14px;
  background: var(--panel);
  color: var(--text);
}
.form-input:focus { outline: none; border-color: var(--accent); }
.form-input:disabled { background: var(--bg); color: var(--muted); }
.form-hint { font-size: 12px; color: var(--muted); }
.form-static {
  padding: 8px 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
}
.batch-bar {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-3) var(--sp-4);
  border-bottom: 1px solid var(--border);
  background: var(--accent-soft);
}
.checkbox-col {
  width: 40px;
  text-align: center;
}
.radio-label {
  display: flex; align-items: center; gap: 6px;
  font-size: 14px; color: var(--text); cursor: pointer;
}
</style>
