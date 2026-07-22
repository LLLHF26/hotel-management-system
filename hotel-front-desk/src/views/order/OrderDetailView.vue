<template>
  <div v-loading="loading">
    <div class="page-header">
      <div class="header-left-info">
        <el-button link @click="$router.back()" class="back-btn">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </el-button>
        <div>
          <h2>订单详情</h2>
          <div class="header-sub">
            订单号：<span class="order-no">{{ order?.orderNo }}</span>
            <el-tag v-if="order" size="small" :type="statusTagType(order.status)" effect="light" round
              style="margin-left: 8px">
              {{ order.statusName || order.status }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <el-row v-if="order" :gutter="18">
      <el-col :xs="24" :lg="16">
        <!-- 订单信息 -->
        <div class="content-card block">
          <div class="section-header">
            <el-icon :size="18"><Document /></el-icon>
            <h3>订单信息</h3>
          </div>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="订单号">
              <span class="mono-text">{{ order.orderNo }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="statusTagType(order.status)" effect="light">{{ order.status }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单来源">{{ order.sourceName || order.source || '-' }}</el-descriptions-item>
            <el-descriptions-item label="订单总额">
              <strong class="amount-lg">&yen;{{ formatMoney(order.totalAmount) }}</strong>
            </el-descriptions-item>
            <el-descriptions-item label="入住日期" :span="2">
              {{ dateRangeLabel(order.checkInDate, order.checkOutDate) }}
              <span v-if="order.nights" class="text-muted">（共 {{ order.nights }} 晚）</span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">{{ order.createTime }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 客户信息 -->
        <div class="content-card block">
          <div class="section-header">
            <el-icon :size="18"><User /></el-icon>
            <h3>客户信息</h3>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">{{ order.customerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">
              <span class="phone-text">{{ order.customerPhone || '-' }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 房间信息 -->
        <div class="content-card block">
          <div class="section-header">
            <el-icon :size="18"><OfficeBuilding /></el-icon>
            <h3>房间信息</h3>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="房型">{{ order.roomTypeName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="房间号">
              <el-tag v-if="order.roomNumber" size="small" effect="plain" type="info">{{ order.roomNumber }}
              </el-tag>
              <span v-else class="text-muted">未分配</span>
            </el-descriptions-item>
            <el-descriptions-item label="每晚房价">
              <span class="amount-text">&yen;{{ formatMoney(order.roomPrice) }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 费用明细 -->
        <div class="content-card block">
          <div class="section-header">
            <el-icon :size="18"><Coin /></el-icon>
            <h3>费用明细</h3>
          </div>
          <div class="fee-list">
            <div class="fee-line">
              <span class="fee-label">房费（{{ order.nights ?? 0 }}晚）</span>
              <span class="fee-value">&yen;{{ formatMoney(order.roomTotal) }}</span>
            </div>
            <template v-for="e in order.extras || []" :key="e.id">
              <div class="fee-line fee-extra">
                <span class="fee-label">{{ e.itemName }} &times; {{ e.quantity ?? 1 }}</span>
                <span class="fee-value">&yen;{{ formatMoney((e.amount ?? 0) * (e.quantity ?? 1)) }}</span>
              </div>
            </template>
            <el-divider />
            <div class="fee-line fee-total">
              <span class="fee-label">合计金额</span>
              <span class="fee-value total-amount">&yen;{{ formatMoney(order.totalAmount) }}</span>
            </div>
            <div class="fee-line">
              <span class="fee-label paid-label">已支付</span>
              <span class="fee-value">&yen;{{ formatMoney(order.paidAmount) }}</span>
            </div>
          </div>
        </div>

        <!-- 支付记录 -->
        <div class="content-card block">
          <div class="section-header">
            <el-icon :size="18"><CreditCard /></el-icon>
            <h3>支付记录</h3>
          </div>
          <el-table :data="order.payments || []" size="default" stripe>
            <el-table-column prop="paymentNo" label="流水号" min-width="160">
              <template #default="{ row }">
                <span class="mono-text text-sm">{{ row.paymentNo }}</span>
              </template>
            </el-table-column>
            <el-table-column label="支付方式" width="120">
              <template #default="{ row }">{{ row.methodName || row.method }}</template>
            </el-table-column>
            <el-table-column label="金额" width="110" align="right">
              <template #default="{ row }">
                <span class="amount-text">&yen;{{ formatMoney(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="170" />
          </el-table>
          <el-empty v-if="!order.payments?.length" description="暂无支付记录" :image-size="60" />
        </div>
      </el-col>

      <!-- 右侧操作面板 -->
      <el-col :xs="24" :lg="8">
        <div class="content-card action-panel">
          <div class="section-header">
            <el-icon :size="18" color="#d4a853"><SetUp /></el-icon>
            <h3>快捷操作</h3>
          </div>
          <el-space direction="vertical" fill :size="12" style="width: 100%">
            <el-button
              v-if="order.status === '已支付'"
              type="primary"
              round
              @click="actions?.openCheckIn(order)"
            >
              <el-icon><CircleCheck /></el-icon> 办理入住
            </el-button>
            <el-button
              v-if="order.status === '已入住'"
              type="warning"
              round
              @click="actions?.openCheckOut(order)"
            >
              <el-icon><SwitchButton /></el-icon> 办理退房
            </el-button>
            <el-button v-if="order.status === '已入住'" round plain @click="actions?.openExtend(order)">
              <el-icon><Clock /></el-icon> 续住
            </el-button>
            <el-button v-if="order.status === '已入住'" round plain @click="actions?.openChangeRoom(order)">
              <el-icon><RefreshRight /></el-icon> 换房
            </el-button>
            <el-divider v-if="order.status === '待支付'" />
            <el-button
              v-if="order.status === '待支付'"
              type="success"
              round
              @click="actions?.openPay(order)"
            >
              确认收款
            </el-button>
            <el-button
              v-if="order.status === '待支付'"
              type="danger"
              plain
              round
              @click="actions?.openCancel(order)"
            >
              取消订单
            </el-button>
          </el-space>
        </div>
      </el-col>
    </el-row>

    <OrderActions ref="actions" @success="loadDetail" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  ArrowLeft, Document, User, OfficeBuilding, Coin,
  CreditCard, SetUp, CircleCheck, SwitchButton,
  Clock, RefreshRight,
} from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import type { OrderVO } from '@/types'
import { getOrderDetail } from '@/api/order'
import OrderActions from '@/components/order/OrderActions.vue'
import { formatMoney, dateRangeLabel } from '@/utils/format'

const route = useRoute()
const actions = ref<InstanceType<typeof OrderActions>>()
const loading = ref(false)
const order = ref<OrderVO | null>(null)

onMounted(() => loadDetail())

async function loadDetail() {
  const raw = route.params.id
  // route.params.id 类型为 string | string[]，订单 ID 取第一个元素
  const id = Array.isArray(raw) ? raw[0] : raw
  // 注意：id 是 URL 路径参数（字符串），19 位雪花 ID 不能用 Number() 转换，
  // 否则 JS 双精度浮点会精度丢失导致后端查不到订单。直接以字符串传给 API 即可，
  // 后端 @PathVariable Long 能从字符串精确还原，且 order-service JacksonConfig 已将 Long 序列化为字符串。
  if (!id) return
  loading.value = true
  try {
    order.value = await getOrderDetail(id)
  } finally {
    loading.value = false
  }
}

function statusTagType(status: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    '待支付': 'danger',
    '已支付': '',
    '已入住': 'success',
    '已完成': 'info',
    '已取消': 'info',
  }
  return map[status] || 'info'
}
</script>

<style scoped>
.header-left-info {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.back-btn {
  font-size: 14px;
  margin-top: 4px;
}

.header-sub {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.order-no {
  font-family: 'SF Mono', Consolas, monospace;
  font-size: 12.5px;
  color: #475569;
}

.block {
  margin-bottom: 18px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.section-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

/* 费用明细 */
.fee-list {
  max-width: 480px;
}

.fee-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 9px 0;
  font-size: 14px;
}

.fee-extra .fee-label {
  color: #6b7280;
}

.fee-total {
  font-weight: 700;
  font-size: 15px;
  padding-top: 4px;
}

.paid-label {
  color: var(--fd-success);
}

.fee-value {
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

.total-amount {
  color: var(--fd-danger);
  font-size: 17px;
}

/* 文字样式 */
.mono-text {
  font-family: 'SF Mono', Consolas, monospace;
}

.text-sm {
  font-size: 12px;
}

.text-muted {
  color: #9ca3af;
  margin-left: 6px;
}

.phone-text {
  font-family: SF Mono, Consolas, monospace;
  font-size: 13px;
  color: #475569;
}

.amount-lg {
  color: var(--fd-danger);
  font-size: 16px;
}

.amount-text {
  color: #d97706;
  font-weight: 600;
}

/* 操作面板 */
.action-panel {
  position: sticky;
  top: 20px;
}

.action-panel :deep(.el-space__item) {
  width: 100%;
}

.action-panel :deep(.el-button) {
  width: 100%;
  justify-content: center;
}
</style>
