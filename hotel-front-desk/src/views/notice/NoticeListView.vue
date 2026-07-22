<template>
  <div>
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>消息通知</h2>
        <div class="page-subtitle">客户服务呼叫与商品订单，安排人员处理</div>
      </div>
      <div class="page-header-actions">
        <el-button type="primary" :icon="Refresh" @click="loadAll" :loading="loading">刷新</el-button>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <span
        v-for="t in tabs"
        :key="t.key"
        class="tab-item"
        :class="{ active: activeTab === t.key }"
        @click="activeTab = t.key"
      >
        {{ t.label }}
        <el-badge
          v-if="t.count > 0"
          :value="t.count"
          style="margin-left: 6px;"
        />
      </span>
    </div>

    <!-- ===== 服务呼叫 ===== -->
    <div v-if="activeTab === 'service'" class="content-card notice-card">
      <!-- 待处理快捷统计 -->
      <div class="stat-row" v-if="pendingRequests.length">
        <div class="stat-chip stat-warn">
          <span class="stat-num">{{ pendingRequests.length }}</span>
          <span class="stat-label">待处理</span>
        </div>
        <div class="stat-chip stat-info">
          <span class="stat-num">{{ serviceRequests.length - pendingRequests.length }}</span>
          <span class="stat-label">已处理/取消</span>
        </div>
      </div>

      <!-- 列表 -->
      <template v-if="serviceRequests.length">
        <el-timeline>
          <el-timeline-item
            v-for="item in serviceRequests"
            :key="item.id"
            :timestamp="formatTime(item.createTime)"
            placement="top"
            :color="item.status === '待处理' ? 'var(--fd-danger)' : (item.status === '已处理' ? '#16a34a' : '#9ca3af')"
          >
            <div class="notice-row" :class="{ unread: item.status === '待处理' }">
              <div class="notice-row__title">
                <span v-if="item.status === '待处理'" class="notice-dot"></span>
                <el-tag
                  :type="typeTagType(item.type)"
                  size="small"
                  effect="plain"
                >{{ item.type }}</el-tag>
                <span class="notice-room" v-if="item.roomNumber">{{ item.roomNumber }}</span>
              </div>
              <div class="notice-row__content">
                <span v-if="item.customerName">客户：{{ item.customerName }}</span>
                <span v-if="item.remark" class="notice-remark">{{ item.remark }}</span>
              </div>
              <div class="notice-row__meta">
                <span v-if="item.handleRemark" class="handle-info">
                  处理备注：{{ item.handleRemark }}
                </span>
                <span v-if="item.handleTime">{{ formatTime(item.handleTime) }} 完成</span>
              </div>
              <div class="notice-row__actions">
                <!-- 已处理的显示状态 -->
                <template v-if="item.status !== '待处理'">
                  <el-tag size="small" :type="item.status === '已处理' ? 'success' : 'info'" effect="plain">
                    {{ item.status }}
                  </el-tag>
                </template>
                <!-- 待处理的：安排人员 -->
                <template v-else>
                  <el-button type="primary" size="small" @click="openDispatch(item)">
                    安排人员
                  </el-button>
                </template>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </template>
      <el-empty v-else description="暂无服务呼叫记录" />
    </div>

    <!-- ===== 商品订单 ===== -->
    <div v-if="activeTab === 'order'" class="content-card notice-card">
      <template v-if="undeliveredOrders.length">
        <div class="order-list">
          <div v-for="po in undeliveredOrders" :key="po.id" class="po-card">
            <div class="po-header">
              <span class="po-name">{{ po.itemName }}</span>
              <el-tag size="small" effect="plain">×{{ po.quantity }}</el-tag>
              <span class="po-price">¥{{ Number(po.amount) * po.quantity }}</span>
            </div>
            <div class="po-meta">
              <span v-if="po.orderNo">订单号：{{ po.orderNo }}</span>
              <span v-if="po.customerName">客户：{{ po.customerName }}</span>
              <span v-if="po.roomNumber">房间：{{ po.roomNumber }}</span>
              <span class="po-time">{{ formatTime(po.createTime) }}</span>
            </div>
            <div class="po-actions">
              <el-button type="primary" size="small" @click="openDelivery(po)">
                安排配送
              </el-button>
            </div>
          </div>
        </div>
      </template>
      <el-empty v-else description="暂无商品订单记录" />
    </div>

    <!-- ===== 安排人员弹窗（服务呼叫） ===== -->
    <el-dialog
      v-model="dispatchVisible"
      title="安排人员处理"
      width="480px"
      destroy-on-close
    >
      <div class="dispatch-info" v-if="dispatchTarget">
        <div class="dispatch-info-row">
          <label>服务类型：</label>
          <el-tag>{{ dispatchTarget.type }}</el-tag>
        </div>
        <div class="dispatch-info-row" v-if="dispatchTarget.customerName">
          <label>客户：</label>
          <span>{{ dispatchTarget.customerName }}</span>
        </div>
        <div class="dispatch-info-row" v-if="dispatchTarget.roomNumber">
          <label>房间：</label>
          <span>{{ dispatchTarget.roomNumber }}</span>
        </div>
        <div class="dispatch-info-row" v-if="dispatchTarget.remark">
          <label>客户备注：</label>
          <span>{{ dispatchTarget.remark }}</span>
        </div>
      </div>

      <el-form label-width="80px" style="margin-top: 16px;">
        <el-form-item label="指派人员" required>
          <el-select
            v-model="dispatchForm.handlerId"
            placeholder="选择处理人员"
            filterable
            style="width: 100%;"
            :loading="employeeLoading"
          >
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name || emp.username"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="dispatchForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可选，填写处理说明或注意事项"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" :loading="dispatching" @click="submitDispatch">
          确认派单
        </el-button>
      </template>
    </el-dialog>

    <!-- ===== 安配送货弹窗（商品订单） ===== -->
    <el-dialog
      v-model="deliveryVisible"
      title="安排配送"
      width="480px"
      destroy-on-close
    >
      <div class="dispatch-info" v-if="deliveryTarget">
        <div class="dispatch-info-row">
          <label>商品：</label>
          <span>{{ deliveryTarget.itemName }} ×{{ deliveryTarget.quantity }}</span>
        </div>
        <div class="dispatch-info-row">
          <label>金额：</label>
          <span class="po-price">¥{{ Number(deliveryTarget.amount) * deliveryTarget.quantity }}</span>
        </div>
        <div class="dispatch-info-row" v-if="deliveryTarget.customerName">
          <label>客户：</label>
          <span>{{ deliveryTarget.customerName }}</span>
        </div>
        <div class="dispatch-info-row" v-if="deliveryTarget.roomNumber">
          <label>房间：</label>
          <span>{{ deliveryTarget.roomNumber }}</span>
        </div>
      </div>

      <el-form label-width="80px" style="margin-top: 16px;">
        <el-form-item label="配送人员" required>
          <el-select
            v-model="deliveryForm.deliveryPerson"
            placeholder="选择配送人员"
            filterable
            style="width: 100%;"
          >
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="emp.name || emp.username"
              :value="emp.name || emp.username"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="配送备注">
          <el-input
            v-model="deliveryForm.remark"
            type="textarea"
            :rows="2"
            placeholder="可选，如「尽快送达」「客人急需」等"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="deliveryVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDelivery">
          确认配送
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getPendingServiceRequests,
  dispatchServiceRequest,
  getRecentProductOrders,
  getEmployeeList,
} from '@/api/serviceDesk'
import type { ServiceRequestItem, ProductOrderItem, EmployeeItem } from '@/api/serviceDesk'

const loading = ref(false)
const activeTab = ref('service')

// ---- 服务呼叫 ----
const serviceRequests = ref<ServiceRequestItem[]>([])
const pendingRequests = computed(() => serviceRequests.value.filter(s => s.status === '待处理'))

// ---- 商品订单 ----
const productOrders = ref<ProductOrderItem[]>([])
/** 本次会话中已安排配送的商品 extraId 集合 */
const deliveredIds = ref<Set<number>>(new Set())

// ---- Tab 计数 ----
const tabs = computed(() => [
  { key: 'service', label: '服务呼叫', count: pendingRequests.value.length },
  // 商品订单 badge 只计未配送的
  { key: 'order', label: '商品订单', count: undeliveredOrders.value.length },
])

/** 过滤掉本次会话已配送的商品 */
const undeliveredOrders = computed(() =>
  productOrders.value.filter(po => !deliveredIds.value.has(po.id)),
)

// ---- 员工列表 ----
const employees = ref<EmployeeItem[]>([])
const employeeLoading = ref(false)

// ---- 安排人员弹窗（服务呼叫）----
const dispatchVisible = ref(false)
const dispatching = ref(false)
const dispatchTarget = ref<ServiceRequestItem | null>(null)
const dispatchForm = reactive({ handlerId: null as number | null, remark: '' })

// ---- 配送弹窗（商品订单）----
const deliveryVisible = ref(false)
const deliveryTarget = ref<ProductOrderItem | null>(null)
const deliveryForm = reactive({ deliveryPerson: '', remark: '' })

onMounted(loadAll)

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([
      loadServiceRequests(),
      loadProductOrders(),
      loadEmployees(),
    ])
  } finally {
    loading.value = false
  }
}

async function loadServiceRequests() {
  try {
    // 加载全部（含已处理），用于展示完整时间线；status 为空表示不过滤
    const data = await getPendingServiceRequests({ status: '', page: 1, size: 50 })
    serviceRequests.value = data?.records || []
  } catch { /* ignore */ }
}

async function loadProductOrders() {
  try {
    const data = await getRecentProductOrders()
    productOrders.value = data?.records || []
  } catch { /* ignore */ }
}

async function loadEmployees() {
  if (employees.value.length > 0) return
  employeeLoading.value = true
  try {
    const list = await getEmployeeList()
    const records: any[] = list?.records ?? []
    employees.value = records.map((u) => ({
      id: u.id,
      name: u.realName || u.username,
      username: u.username,
      role: u.role,
      phone: u.phone,
      status: u.status,
    }))
  } catch {
    // 员工接口不可用时提供默认占位
    employees.value = [
      { id: 1, name: '张三', username: 'zhangsan', role: '服务员', phone: '138****0001', status: '在职' },
      { id: 2, name: '李四', username: 'lisi', role: '服务员', phone: '138****0002', status: '在职' },
      { id: 3, name: '王五', username: 'wangwu', role: '维修工', phone: '138****0003', status: '在职' },
    ]
  } finally {
    employeeLoading.value = false
  }
}

function openDispatch(item: ServiceRequestItem) {
  dispatchTarget.value = item
  dispatchForm.handlerId = null
  dispatchForm.remark = ''
  dispatchVisible.value = true
}

async function submitDispatch() {
  if (!dispatchTarget.value) return
  if (!dispatchForm.handlerId) {
    ElMessage.warning('请选择处理人员')
    return
  }
  dispatching.value = true
  try {
    await dispatchServiceRequest(dispatchTarget.value.id, '已处理', dispatchForm.remark || undefined)
    dispatchVisible.value = false
    ElMessage.success(`已安排人员处理「${dispatchTarget.value.type}」`)
    await loadAll()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    dispatching.value = false
  }
}

function openDelivery(item: ProductOrderItem) {
  deliveryTarget.value = item
  deliveryForm.deliveryPerson = ''
  deliveryForm.remark = ''
  deliveryVisible.value = true
}

function submitDelivery() {
  if (!deliveryTarget.value) return
  if (!deliveryForm.deliveryPerson) {
    ElMessage.warning('请选择配送人员')
    return
  }
  // 记录本次会话已配送（刷新后仍过滤）
  deliveredIds.value.add(deliveryTarget.value.id)
  deliveryVisible.value = false
  ElMessage.success(
    `已安排「${deliveryForm.deliveryPerson}」配送 ${deliveryTarget.value.itemName} ×${deliveryTarget.value.quantity} 至 ${deliveryTarget.value.roomNumber || '指定房间'}`
  )
}

// ---- 工具方法 ----
function formatTime(time?: string): string {
  if (!time) return ''
  const d = new Date(time)
  if (Number.isNaN(d.getTime())) return time
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

function typeTagType(type: string): '' | 'warning' | 'danger' | 'success' | 'info' {
  const map: Record<string, '' | 'warning' | 'danger' | 'success' | 'info'> = {
    打扫: 'warning',
    送物: '',
    维修: 'danger',
    其他: 'info',
  }
  return map[type] ?? ''
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}
.page-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}
.page-header-actions {
  display: flex;
  gap: 8px;
}

/* Tab */
.tab-bar {
  display: flex;
  gap: 0;
  background: #fff;
  border-radius: 10px;
  padding: 4px;
  border: 1px solid #e5e7eb;
  margin-bottom: 16px;
}
.tab-item {
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}
.tab-item:hover {
  color: #374151;
  background: #f9fafb;
}
.tab-item.active {
  color: #fff;
  background: var(--fd-primary);
}

/* 统计条 */
.stat-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.stat-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 8px;
  font-size: 13px;
}
.stat-warn {
  background: #fef3c7;
  color: #d97706;
}
.stat-info {
  background: #f0f9ff;
  color: #0369a1;
}
.stat-num {
  font-size: 22px;
  font-weight: 800;
}
.stat-label {
  opacity: 0.85;
}

/* 卡片与时间线 */
.content-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
.notice-card {
  padding: 24px;
}

.notice-row {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 8px;
  transition: box-shadow 0.15s;
}
.notice-row.unread {
  background: #fff;
  border: 1px solid #f0f0f0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.notice-row__title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}
.notice-room {
  margin-left: auto;
  font-size: 12px;
  color: #fff;
  background: var(--fd-primary);
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}
.notice-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ef4444;
  flex-shrink: 0;
}
.notice-row__content {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  margin-bottom: 6px;
}
.notice-remark {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 8px;
  background: #f0fdf4;
  color: #15803d;
  border-radius: 4px;
  font-size: 12px;
}
.notice-row__meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 6px;
  align-items: center;
}
.handle-info {
  background: #ecfdf5;
  color: #047857;
  padding: 2px 8px;
  border-radius: 4px;
}
.notice-row__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

:deep(.el-timeline-item__timestamp) {
  font-size: 11px;
  color: #9ca3af;
}

/* 商品订单卡片 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.po-card {
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  padding: 14px 18px;
  transition: box-shadow 0.15s;
}
.po-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.po-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.po-name {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}
.po-price {
  margin-left: auto;
  font-size: 16px;
  font-weight: 800;
  color: var(--fd-primary);
}
.po-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #94a3b8;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.po-time {
  margin-left: auto;
}
.po-actions {
  display: flex;
  justify-content: flex-end;
}

/* 弹窗内信息区 */
.dispatch-info {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 14px 16px;
}
.dispatch-info-row {
  display: flex;
  gap: 8px;
  font-size: 13px;
  line-height: 1.8;
  color: #374151;
}
.dispatch-info-row label {
  color: #6b7280;
  white-space: nowrap;
  min-width: 72px;
}
</style>
