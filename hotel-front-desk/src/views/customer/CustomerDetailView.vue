<template>
  <div v-loading="loading">
    <div class="page-header">
      <div class="header-left-info">
        <el-button link @click="$router.back()" class="back-btn">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </el-button>
        <h2>客户详情</h2>
      </div>
    </div>

    <template v-if="customer">
      <el-row :gutter="18">
        <el-col :xs="24" :lg="16">
          <!-- 基本信息 -->
          <div class="content-card block">
            <div class="section-header">
              <el-icon :size="18"><UserFilled /></el-icon>
              <h3>基本信息</h3>
            </div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="客户姓名">
                <span class="customer-name">{{ customer.realName }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="手机号码">
                <span class="phone-text">{{ customer.phone }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="身份证号">{{ customer.idCard || '-' }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ genderText(customer.gender) }}</el-descriptions-item>
              <el-descriptions-item label="会员等级">
                <el-tag v-if="customer.memberLevel" size="small" effect="light" round>{{ customer.memberLevel }}
                </el-tag>
                <span v-else class="text-muted">普通会员</span>
              </el-descriptions-item>
              <el-descriptions-item label="积分余额">
                <span class="points-text">{{ customer.points ?? 0 }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="注册时间" :span="2">{{ customer.createTime }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 消费统计 -->
          <div class="content-card block">
            <div class="section-header">
              <el-icon :size="18"><TrendCharts /></el-icon>
              <h3>消费统计</h3>
            </div>
            <div class="stat-summary">
              <div class="stat-item">
                <div class="stat-val">&yen;{{ formatMoney(customer.totalConsumed ?? 0) }}</div>
                <div class="stat-lbl">累计消费</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <div class="stat-val">{{ orderList.length }}</div>
                <div class="stat-lbl">入住次数</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <div class="stat-val">&yen;{{ formatMoney(avgConsume) }}</div>
                <div class="stat-lbl">平均消费</div>
              </div>
            </div>
          </div>

          <!-- 最近订单 -->
          <div class="content-card block">
            <div class="section-header">
              <el-icon :size="18"><Tickets /></el-icon>
              <h3>最近订单</h3>
              <span class="count-badge">{{ orderList.length }}</span>
            </div>
            <el-table :data="orderList" size="default" stripe>
              <el-table-column prop="orderNo" label="订单号" min-width="150">
                <template #default="{ row }">
                  <span class="mono-text">{{ row.orderNo }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="roomTypeName" label="房型" width="110" />
              <el-table-column label="入住日期" width="110">
                <template #default="{ row }">{{ formatDate(row.checkInDate) }}</template>
              </el-table-column>
              <el-table-column label="退房日期" width="110">
                <template #default="{ row }">{{ formatDate(row.checkOutDate) }}</template>
              </el-table-column>
              <el-table-column label="金额" width="105" align="right">
                <template #default="{ row }">
                  <span class="amount-text">&yen;{{ formatMoney(row.totalAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="90">
                <template #default="{ row }">
                  <el-tag size="small" effect="light" round>{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small"
                    @click="$router.push(`/front-desk/orders/${row.id}`)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!orderList.length" description="暂无订单记录" :image-size="60" />
          </div>
        </el-col>

        <!-- 右侧快速操作 -->
        <el-col :xs="24" :lg="8">
          <div class="content-card action-panel">
            <div class="section-header">
              <el-icon :size="18" color="#d4a853"><Operation /></el-icon>
              <h3>快速操作</h3>
            </div>
            <el-space direction="vertical" fill :size="12" style="width: 100%">
              <el-button type="primary" round @click="$router.push(`/front-desk/orders?phone=${customer.phone}`)">
                <el-icon><Plus /></el-icon> 新建订单
              </el-button>
              <el-button round plain @click="$router.push('/front-desk/customers')">返回列表</el-button>
            </el-space>
          </div>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  ArrowLeft, UserFilled, TrendCharts, Tickets,
  Operation, Plus,
} from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import type { CustomerVO, OrderVO } from '@/types'
import { getCustomerDetail } from '@/api/customer'
import { getOrderList } from '@/api/order'
import { formatMoney, formatDate } from '@/utils/format'

const route = useRoute()
const loading = ref(false)
const customer = ref<CustomerVO | null>(null)
const orderList = ref<OrderVO[]>([])

const avgConsume = computed(() => {
  if (!orderList.value.length) return 0
  const sum = orderList.value.reduce((a, o) => a + Number(o.totalAmount ?? 0), 0)
  return sum / orderList.value.length
})

onMounted(() => loadData())

function genderText(g?: number) {
  if (g === 1) return '男'
  if (g === 2) return '女'
  return '-'
}

async function loadData() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    customer.value = await getCustomerDetail(id)
    if (customer.value.phone) {
      const orders = await getOrderList({ customerPhone: customer.value.phone, size: 50, page: 1 })
      orderList.value = orders.records
    }
  } finally {
    loading.value = false
  }
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

.count-badge {
  font-size: 11px;
  background: #f1f5f9;
  color: #64748b;
  padding: 2px 8px;
  border-radius: 10px;
  margin-left: auto;
}

/* 消费统计 */
.stat-summary {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-val {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.stat-lbl {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #e5e7eb;
}

/* 文字样式 */
.customer-name {
  font-weight: 600;
  font-size: 15px;
}

.phone-text {
  font-family: SF Mono, Consolas, monospace;
  font-size: 13px;
  color: #475569;
}

.points-text {
  color: #d4a853;
  font-weight: 600;
  font-size: 15px;
}

.text-muted {
  color: #d1d5db;
}

.mono-text {
  font-family: SF Mono, Consolas, monospace;
  font-size: 12.5px;
  color: #475569;
}

.amount-text {
  color: #d97706;
  font-weight: 600;
}

/* 操作面板 */
.action-panel {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.action-panel :deep(.el-space__item) {
  width: 100%;
}

.action-panel :deep(.el-button) {
  width: 100%;
  justify-content: center;
}
</style>
