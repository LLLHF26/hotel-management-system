<template>
  <div>
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>账单管理</h2>
        <div class="page-subtitle">订单查询与账务处理</div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="query.status" placeholder="订单状态" clearable style="width: 140px">
        <el-option v-for="s in ORDER_STATUS_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="入住开始"
        end-placeholder="入住结束"
        value-format="YYYY-MM-DD"
        style="width: 260px"
      />
      <el-input v-model="query.customerPhone" placeholder="订单号 / 手机号" clearable
        style="width: 180px" :prefix-icon="Search" />
      <el-button type="primary" @click="search">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <!-- 表格区域 -->
    <div class="content-card table-card">
      <el-table v-loading="loading" :data="list" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="160">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户信息" min-width="130">
          <template #default="{ row }">
            <div class="customer-cell">
              <span class="customer-name">{{ row.customerName }}</span>
              <span class="customer-phone">{{ row.customerPhone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="roomTypeName" label="房型" width="110" />
        <el-table-column prop="roomNumber" label="房间" width="78">
          <template #default="{ row }">
            <el-tag v-if="row.roomNumber" size="small" effect="plain" type="info">{{ row.roomNumber }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="入住-退房" width="135">
          <template #default="{ row }">
            <span class="date-range">{{ dateRangeLabel(row.checkInDate, row.checkOutDate) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="105">
          <template #default="{ row }">
            <span class="amount-text">&yen;{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="92">
          <template #default="{ row }">
            <el-tag size="small" :type="orderStatusType(row.status)" effect="light" round>
              {{ row.statusName || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
              <template v-if="row.status === '待支付'">
                <el-button link type="success" @click="actionRef?.openPay(row)">支付</el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @change="loadList"
        />
      </div>
    </div>

    <OrderActions ref="actionRef" @success="loadList" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import type { OrderVO } from '@/types'
import { getOrderList } from '@/api/order'
import OrderActions from '@/components/order/OrderActions.vue'
import { formatMoney, dateRangeLabel } from '@/utils/format'
import { ORDER_STATUS_OPTIONS } from '@/utils/constants'

const router = useRouter()
const actionRef = ref<InstanceType<typeof OrderActions>>()
const loading = ref(false)
const list = ref<OrderVO[]>([])
const total = ref(0)
const dateRange = ref<[string, string] | null>(null)

const query = reactive({
  page: 1,
  size: 10,
  status: '',
  customerPhone: '',
  checkInDate: '',
  startDate: '',
  endDate: '',
})

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try {
    if (dateRange.value) {
      query.startDate = dateRange.value[0]
      query.endDate = dateRange.value[1]
      query.checkInDate = ''
    } else {
      query.startDate = ''
      query.endDate = ''
    }
    const res = await getOrderList({
      page: query.page,
      size: query.size,
      status: query.status || undefined,
      customerPhone: query.customerPhone || undefined,
      checkInDate: query.checkInDate || undefined,
      startDate: query.startDate || undefined,
      endDate: query.endDate || undefined,
    })
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  loadList()
}

function reset() {
  query.status = ''
  query.customerPhone = ''
  query.checkInDate = ''
  dateRange.value = null
  query.page = 1
  loadList()
}

function goDetail(id: number) {
  router.push(`/front-desk/orders/${id}`)
}

function orderStatusType(status: string): '' | 'success' | 'warning' | 'danger' | 'info' {
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
.filter-divider {
  width: 1px;
  height: 24px;
  background: #e5e7eb;
  margin: 0 4px;
}

.table-card {
  overflow: hidden;
}

.order-no {
  font-family: 'SF Mono', 'Consolas', monospace;
  font-size: 12.5px;
  color: #475569;
}

.customer-cell {
  display: flex;
  flex-direction: column;
}

.customer-name {
  font-weight: 500;
  font-size: 13px;
}

.customer-phone {
  font-size: 11.5px;
  color: #9ca3af;
  margin-top: 1px;
}

.date-range {
  font-size: 12px;
  color: #64748b;
}

.amount-text {
  font-weight: 600;
  color: #d97706;
  font-size: 13px;
}

.action-group {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

.text-muted {
  color: #d1d5db;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}
</style>
