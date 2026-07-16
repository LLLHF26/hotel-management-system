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
  <el-dialog v-model="checkOutVisible" title="办理退房" width="420px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="退还押金">
        <el-input-number v-model="refundDeposit" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="checkOutVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submitCheckOut">确认退房</el-button>
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
  <el-dialog v-model="changeRoomVisible" title="换房" width="480px" destroy-on-close>
    <el-form label-width="90px">
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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { OrderVO, RoomVO } from '@/types'
import {
  checkIn,
  checkOut,
  extendOrder,
  changeRoom,
  pay,
  cancelOrder,
  addExtra,
} from '@/api/order'
import { getRoomList } from '@/api/room'
import { formatMoney, dateRangeLabel } from '@/utils/format'
import { PAY_METHOD_OPTIONS, EXTRA_PRESETS } from '@/utils/constants'

const emit = defineEmits<{ success: [] }>()

const order = ref<OrderVO | null>(null)
const loading = ref(false)

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
const availableRooms = ref<RoomVO[]>([])
const payAmount = ref(0)
const payMethod = ref('CASH')
const cancelReason = ref('客户要求取消')
const extraItem = ref('')
const extraQty = ref(1)
const extraPrice = ref(15)

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
}

function openExtend(row: OrderVO) {
  order.value = row
  extendDays.value = 1
  extendVisible.value = true
}

async function openChangeRoom(row: OrderVO) {
  order.value = row
  newRoomId.value = undefined
  await loadAvailableRooms()
  changeRoomVisible.value = true
}

function openPay(row: OrderVO) {
  order.value = row
  payAmount.value = Number(row.totalAmount ?? 0) - Number(row.paidAmount ?? 0)
  payMethod.value = 'CASH'
  payVisible.value = true
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
  loading.value = true
  try {
    await changeRoom(order.value.id, newRoomId.value)
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
