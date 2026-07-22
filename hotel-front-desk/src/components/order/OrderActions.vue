<template>
  <!-- 办理入住 -->
  <el-dialog v-model="checkInVisible" title="办理入住" width="480px" destroy-on-close>
    <el-descriptions v-if="order" :column="1" border size="small">
      <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="客户">{{ order.customerName }} {{ order.customerPhone }}</el-descriptions-item>
      <el-descriptions-item label="房间">{{ order.roomTypeName }} {{ order.roomNumber }}</el-descriptions-item>
      <el-descriptions-item label="入住日期">{{ dateRangeLabel(order.checkInDate, order.checkOutDate) }}</el-descriptions-item>
      <el-descriptions-item label="应付总额">{{ formatMoney(order.totalAmount) }}</el-descriptions-item>
    </el-descriptions>
    <el-form label-width="90px" style="margin-top: 16px">
      <el-form-item label="押金金额">
        <el-input-number v-model="deposit" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="checkInVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submitCheckIn">确认入住</el-button>
    </template>
  </el-dialog>

  <!-- 办理退房 -->
  <el-dialog v-model="checkOutVisible" title="办理退房" width="520px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="退还押金">
        <el-input-number v-model="refundDeposit" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>
    </el-form>

    <el-divider content-position="left">本单商品 / 服务消费</el-divider>
    <div v-loading="extrasLoading" class="checkout-extra">
      <el-empty v-if="!extras.length" description="暂无额外消费" :image-size="48" />
      <el-table v-else :data="extras" size="small" style="width: 100%; margin-bottom: 8px">
        <el-table-column prop="itemName" label="项目" min-width="130" />
        <el-table-column label="单价" width="78">
          <template #default="{ row }">&yen;{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="56" align="center" />
        <el-table-column label="小计" width="92">
          <template #default="{ row }">&yen;{{ formatMoney(row.subtotal ?? row.amount * (row.quantity || 1)) }}</template>
        </el-table-column>
      </el-table>

      <div class="settle-row">
        <span>消费合计：<b class="amount-strong">&yen;{{ formatMoney(extraSum) }}</b></span>
        <span class="outstanding">
          待支付：<b :class="outstanding > 0 ? 'amount-danger' : 'amount-ok'">&yen;{{ formatMoney(outstanding) }}</b>
        </span>
      </div>

      <el-button
        v-if="outstanding > 0"
        type="warning"
        :loading="loading"
        style="width: 100%; margin-top: 8px"
        @click="settleOutstanding"
      >
        结算待付消费 ¥{{ formatMoney(outstanding) }}
      </el-button>
      <el-alert
        v-else-if="extras.length"
        type="success"
        :closable="false"
        show-icon
        title="商品 / 服务消费已结清"
        style="margin-top: 8px"
      />
    </div>

    <template #footer>
      <el-button @click="checkOutVisible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="loading"
        :disabled="outstanding > 0"
        @click="submitCheckOut"
      >确认退房</el-button>
    </template>
  </el-dialog>

  <!-- 续住 -->
  <el-dialog v-model="extendVisible" title="续住" width="400px" destroy-on-close>
    <el-form label-width="90px">
      <el-form-item label="续住天数">
        <el-input-number v-model="extendDays" :min="1" :max="30" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="extendVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submitExtend">确认续住</el-button>
    </template>
  </el-dialog>

  <!-- 换房 -->
  <el-dialog v-model="changeRoomVisible" title="换房" width="520px" destroy-on-close>
    <el-descriptions v-if="order" :column="1" border size="small" style="margin-bottom: 16px">
      <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="当前房间">{{ order.roomTypeName }} {{ order.roomNumber }}</el-descriptions-item>
      <el-descriptions-item label="原入住日期">{{ dateRangeLabel(order.checkInDate, order.checkOutDate) }}</el-descriptions-item>
    </el-descriptions>

    <el-form label-width="90px">
      <el-form-item label="换房时段">
        <el-date-picker
          v-model="changeDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="起始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          :clearable="true"
          style="width: 100%"
        />
        <div class="form-tip">不选则默认整个订单期间换房</div>
      </el-form-item>
      <el-form-item label="新房间">
        <el-select v-model="newRoomId" placeholder="选择空闲房间" filterable style="width: 100%">
          <el-option
            v-for="r in availableRooms"
            :key="r.id"
            :label="`${r.roomNumber} - ${r.roomTypeName}`"
            :value="r.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="changeRoomVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submitChangeRoom">确认换房</el-button>
    </template>
  </el-dialog>

  <!-- 支付 -->
  <el-dialog v-model="payVisible" title="订单支付" width="420px" destroy-on-close>
    <el-form label-width="90px">
      <el-form-item label="支付金额">
        <el-input-number v-model="payAmount" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>
      <el-form-item label="支付方式">
        <el-select v-model="payMethod" style="width: 100%">
          <el-option v-for="m in PAY_METHOD_OPTIONS" :key="m.value" :label="m.label" :value="m.value" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="payVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submitPay">确认支付</el-button>
    </template>
  </el-dialog>

  <!-- 取消 -->
  <el-dialog v-model="cancelVisible" title="取消订单" width="420px" destroy-on-close>
    <el-form label-width="90px">
      <el-form-item label="取消原因">
        <el-input v-model="cancelReason" type="textarea" rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="cancelVisible = false">返回</el-button>
      <el-button type="danger" :loading="loading" @click="submitCancel">确认取消</el-button>
    </template>
  </el-dialog>

  <!-- 添加消费 -->
  <el-dialog v-model="extraVisible" title="添加消费" width="480px" destroy-on-close>
    <el-form label-width="90px">
      <el-form-item label="消费项目">
        <el-select v-model="extraItem" allow-create filterable default-first-option style="width: 100%">
          <el-option v-for="item in EXTRA_PRESETS" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="数量">
        <el-input-number v-model="extraQty" :min="1" />
      </el-form-item>
      <el-form-item label="单价">
        <el-input-number v-model="extraPrice" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="小计">
        <span>{{ formatMoney(extraQty * extraPrice) }}</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="extraVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submitExtra">添加</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { ExtraVO, OrderVO, RoomVO } from '@/types'
import {
  addExtra,
  cancelOrder,
  changeRoom,
  checkIn,
  checkOut,
  extendOrder,
  getOrderDetail,
  getOrderExtras,
  pay,
} from '@/api/order'
import { getRoomList } from '@/api/room'
import { formatMoney, dateRangeLabel } from '@/utils/format'
import { PAY_METHOD_OPTIONS, EXTRA_PRESETS } from '@/utils/constants'

const emit = defineEmits<{ success: [] }>()

const order = ref<OrderVO | null>(null)
const loading = ref(false)
const extras = ref<ExtraVO[]>([])
const extrasLoading = ref(false)

const checkInVisible = ref(false)
const checkOutVisible = ref(false)
const extendVisible = ref(false)
const changeRoomVisible = ref(false)
const payVisible = ref(false)
const cancelVisible = ref(false)
const extraVisible = ref(false)

const deposit = ref(500)
const refundDeposit = ref(500)
const extendDays = ref(1)
const newRoomId = ref<number>()
const changeDateRange = ref<[string, string] | null>(null)
const availableRooms = ref<RoomVO[]>([])
const payAmount = ref(0)
const payMethod = ref('CASH')
const cancelReason = ref('客户要求取消')
const extraItem = ref('')
const extraQty = ref(1)
const extraPrice = ref(15)

/** 本单商品/服务消费合计 */
const extraSum = computed(() =>
  (extras.value || []).reduce(
    (sum, e) => sum + (Number(e.subtotal ?? 0) || Number(e.amount) * (Number(e.quantity) || 1)),
    0,
  ),
)

/** 退房前仍需支付的金额（房款 + 商品/服务消费 - 已付） */
const outstanding = computed(() => {
  if (!order.value) return 0
  const total = Number(order.value.totalAmount ?? 0)
  const paid = Number(order.value.paidAmount ?? 0)
  return Math.max(0, total - paid)
})

async function loadAvailableRooms() {
  const res = await getRoomList({ status: '空闲中', page: 1, size: 200 })
  availableRooms.value = res.records
}

function openCheckIn(row: OrderVO) {
  order.value = row
  deposit.value = Number(row.deposit ?? 500)
  checkInVisible.value = true
}

function openCheckOut(row: OrderVO) {
  order.value = row
  refundDeposit.value = Number(row.deposit ?? 500)
  checkOutVisible.value = true
  loadCheckOutData(row.id)
}

/** 拉取最新订单金额与商品/服务消费明细，用于退房前结算校验 */
async function loadCheckOutData(id: number) {
  extrasLoading.value = true
  try {
    const [detail, extrasRes] = await Promise.all([
      getOrderDetail(id),
      getOrderExtras(id),
    ])
    if (detail) order.value = detail
    extras.value = Array.isArray(extrasRes) ? extrasRes : []
  } catch {
    extras.value = []
  } finally {
    extrasLoading.value = false
  }
}

function openExtend(row: OrderVO) {
  order.value = row
  extendDays.value = 1
  extendVisible.value = true
}

async function openChangeRoom(row: OrderVO) {
  order.value = row
  newRoomId.value = undefined
  // 默认填入订单的入住/退房日期
  changeDateRange.value = (row.checkInDate && row.checkOutDate)
    ? [row.checkInDate, row.checkOutDate]
    : null
  await loadAvailableRooms()
  changeRoomVisible.value = true
}

function openPay(row: OrderVO) {
  order.value = row
  payAmount.value = Number(row.totalAmount ?? 0) - Number(row.paidAmount ?? 0)
  payMethod.value = 'CASH'
  payVisible.value = true
}

/** 退房场景下结算待付的商品 / 服务消费 */
function settleOutstanding() {
  if (!order.value) return
  openPay(order.value)
}

function openCancel(row: OrderVO) {
  order.value = row
  cancelReason.value = '客户要求取消'
  cancelVisible.value = true
}

function openExtra(row: OrderVO) {
  order.value = row
  extraItem.value = EXTRA_PRESETS[0]
  extraQty.value = 1
  extraPrice.value = 15
  extraVisible.value = true
}

async function submitCheckIn() {
  if (!order.value) return
  loading.value = true
  try {
    await checkIn(order.value.id, deposit.value)
    ElMessage.success('入住办理成功')
    checkInVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

async function submitCheckOut() {
  if (!order.value) return
  // 退房前必须先结清本单的商品/服务消费
  if (outstanding.value > 0) {
    ElMessage.warning(
      `请先结算本单待支付的商品 / 服务消费（¥${formatMoney(outstanding.value)}）后再办理退房`,
    )
    return
  }
  loading.value = true
  try {
    await checkOut(order.value.id, refundDeposit.value)
    ElMessage.success('退房成功')
    checkOutVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

async function submitExtend() {
  if (!order.value) return
  loading.value = true
  try {
    const res = await extendOrder(order.value.id, extendDays.value)
    ElMessage.success(`续住成功，新退房日 ${res.newCheckOutDate}，加收 ${formatMoney(res.additionalAmount)}`)
    extendVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

async function submitChangeRoom() {
  if (!order.value || !newRoomId.value) {
    ElMessage.warning('请选择新房间')
    return
  }
  const [startDate, endDate] = changeDateRange.value ?? []
  loading.value = true
  try {
    await changeRoom(order.value.id, newRoomId.value, startDate, endDate)
    ElMessage.success('换房成功')
    changeRoomVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

async function submitPay() {
  if (!order.value) return
  loading.value = true
  try {
    await pay(order.value.id, payAmount.value, payMethod.value)
    ElMessage.success('支付成功')
    payVisible.value = false
    // 若在退房场景中结算，刷新待付金额，便于解除退房限制
    if (checkOutVisible.value) {
      await loadCheckOutData(order.value.id)
    }
    emit('success')
  } finally {
    loading.value = false
  }
}

async function submitCancel() {
  if (!order.value) return
  loading.value = true
  try {
    await cancelOrder(order.value.id, cancelReason.value)
    ElMessage.success('订单已取消')
    cancelVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

async function submitExtra() {
  if (!order.value || !extraItem.value) {
    ElMessage.warning('请填写消费项目')
    return
  }
  loading.value = true
  try {
    await addExtra(order.value.id, {
      itemName: extraItem.value,
      amount: extraPrice.value,
      quantity: extraQty.value,
    })
    ElMessage.success('消费已添加')
    extraVisible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

defineExpose({
  openCheckIn,
  openCheckOut,
  openExtend,
  openChangeRoom,
  openPay,
  openCancel,
  openExtra,
})
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.checkout-extra {
  padding: 0 2px;
  min-height: 60px;
}

.settle-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #475569;
  margin-top: 4px;
}

.amount-strong {
  color: #d97706;
  font-size: 14px;
}

.amount-danger {
  color: #dc2626;
  font-size: 14px;
}

.amount-ok {
  color: #16a34a;
  font-size: 14px;
}
</style>
