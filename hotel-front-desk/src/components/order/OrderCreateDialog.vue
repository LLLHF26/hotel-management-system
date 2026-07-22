<template>
  <el-dialog
    v-model="visible"
    title="新建预订 / 散客入住"
    width="520px"
    destroy-on-close
    @closed="reset"
  >
    <el-form label-width="92px" :model="form">
      <!-- 房型 -->
      <el-form-item label="房型" required>
        <el-select
          v-model="form.roomTypeId"
          placeholder="选择房型"
          filterable
          style="width: 100%"
          @change="onRoomTypeChange"
        >
          <el-option
            v-for="t in roomTypes"
            :key="t.id"
            :label="`${t.name}${t.price != null ? ` · ¥${formatMoney(t.price)}` : ''}`"
            :value="t.id"
          />
        </el-select>
      </el-form-item>

      <!-- 入住 / 退房日期 -->
      <el-form-item label="入住日期" required>
        <el-date-picker
          v-model="form.checkInDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择入住日期"
          style="width: 100%"
          :disabled-date="disablePast"
        />
      </el-form-item>
      <el-form-item label="退房日期" required>
        <el-date-picker
          v-model="form.checkOutDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择退房日期"
          style="width: 100%"
          :disabled-date="disableBeforeCheckIn"
        />
      </el-form-item>

      <!-- 会员识别 -->
      <el-form-item label="会员识别">
        <div class="member-lookup">
          <el-input
            v-model="memberPhone"
            placeholder="输入会员手机号识别老客"
            clearable
            style="flex: 1"
            :prefix-icon="Phone"
            @keyup.enter="lookupMember"
          />
          <el-button :loading="memberLoading" @click="lookupMember">查找</el-button>
        </div>
        <div v-if="foundMember" class="member-card">
          <el-tag type="success" size="small" effect="light">已识别会员</el-tag>
          <span class="member-name">{{ foundMember.realName }}</span>
          <span class="member-level">{{ foundMember.memberLevel || '普通' }}</span>
          <span class="member-phone">{{ foundMember.phone }}</span>
          <el-button link type="info" size="small" @click="clearMember">清除</el-button>
        </div>
      </el-form-item>

      <!-- 住客信息 -->
      <el-form-item label="住客姓名" required>
        <el-input v-model="form.guestName" placeholder="会员自动带出，散客请填写" />
      </el-form-item>
      <el-form-item label="住客手机" :required="!form.customerId">
        <el-input v-model="form.guestPhone" placeholder="会员自动带出，散客请填写" />
      </el-form-item>

      <!-- 押金 / 备注 -->
      <el-form-item label="押金">
        <el-input-number v-model="form.deposit" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" />
      </el-form-item>

      <el-form-item label="立即入住">
        <el-switch v-model="form.immediateCheckIn" />
        <span class="hint">开启后创建订单并直接办理入住</span>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确认提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Phone } from '@element-plus/icons-vue'
import type { CustomerVO, RoomTypeVO } from '@/types'
import { createOrder, checkIn } from '@/api/order'
import { getRoomTypeList } from '@/api/room'
import { getCustomerList } from '@/api/customer'
import { formatMoney } from '@/utils/format'

const emit = defineEmits<{ success: [] }>()

const visible = ref(false)
const submitting = ref(false)
const roomTypes = ref<RoomTypeVO[]>([])
const memberPhone = ref('')
const memberLoading = ref(false)
const foundMember = ref<CustomerVO | null>(null)

function todayStr() {
  const d = new Date()
  return d.toISOString().slice(0, 10)
}
function tomorrowStr() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().slice(0, 10)
}

const form = reactive({
  roomTypeId: undefined as number | undefined,
  checkInDate: todayStr(),
  checkOutDate: tomorrowStr(),
  guestName: '',
  guestPhone: '',
  customerId: undefined as number | undefined,
  deposit: 500,
  remark: '',
  immediateCheckIn: true,
})

function reset() {
  form.roomTypeId = undefined
  form.checkInDate = todayStr()
  form.checkOutDate = tomorrowStr()
  form.guestName = ''
  form.guestPhone = ''
  form.customerId = undefined
  form.deposit = 500
  form.remark = ''
  form.immediateCheckIn = true
  memberPhone.value = ''
  foundMember.value = null
}

function disablePast(date: Date) {
  return date.getTime() < Date.now() - 24 * 60 * 60 * 1000
}
function disableBeforeCheckIn(date: Date) {
  if (!form.checkInDate) return false
  return date.getTime() <= new Date(form.checkInDate).getTime()
}

function onRoomTypeChange() {
  const t = roomTypes.value.find((r) => r.id === form.roomTypeId)
  if (t && t.price != null) form.deposit = t.price // 默认押金参考房价
}

async function lookupMember() {
  const phone = (memberPhone.value || '').trim()
  if (!phone) {
    ElMessage.warning('请输入会员手机号')
    return
  }
  memberLoading.value = true
  try {
    const res = await getCustomerList({ keyword: phone })
    const hit = res.records.find((c) => c.phone === phone) || res.records[0]
    if (hit) {
      foundMember.value = hit
      form.customerId = hit.id
      form.guestName = hit.realName
      form.guestPhone = hit.phone
      ElMessage.success(`已识别会员：${hit.realName}（${hit.memberLevel || '普通'}）`)
    } else {
      foundMember.value = null
      form.customerId = undefined
      ElMessage.info('未匹配到会员，将按散客登记')
    }
  } finally {
    memberLoading.value = false
  }
}

function clearMember() {
  foundMember.value = null
  form.customerId = undefined
  memberPhone.value = ''
}

async function loadRoomTypes() {
  try {
    const res = await getRoomTypeList()
    roomTypes.value = res.records
  } catch {
    /* 拦截器已提示 */
  }
}

function open(prefill?: { roomTypeId?: number }) {
  reset()
  if (prefill?.roomTypeId) {
    form.roomTypeId = prefill.roomTypeId
    const t = roomTypes.value.find((r) => r.id === prefill.roomTypeId)
    if (t && t.price != null) form.deposit = t.price
  }
  visible.value = true
}

async function submit() {
  if (!form.roomTypeId) {
    ElMessage.warning('请选择房型')
    return
  }
  if (!form.checkInDate || !form.checkOutDate) {
    ElMessage.warning('请选择入住/退房日期')
    return
  }
  if (new Date(form.checkOutDate) <= new Date(form.checkInDate)) {
    ElMessage.warning('退房日期必须晚于入住日期')
    return
  }
  if (!form.guestName && !form.customerId) {
    ElMessage.warning('请填写住客姓名或先识别会员')
    return
  }
  if (!form.customerId && !form.guestPhone) {
    ElMessage.warning('散客请填写住客手机号')
    return
  }

  submitting.value = true
  try {
    const result = await createOrder({
      roomTypeId: form.roomTypeId as number,
      customerId: form.customerId,
      checkInDate: form.checkInDate,
      checkOutDate: form.checkOutDate,
      guestName: form.guestName || undefined,
      guestPhone: form.guestPhone || undefined,
      remark: form.remark || undefined,
    })
    ElMessage.success(`预订创建成功，订单号 ${result.orderNo}`)

    if (form.immediateCheckIn && result.orderId) {
      try {
        await checkIn(result.orderId, form.deposit)
        ElMessage.success('已办理入住')
      } catch {
        ElMessage.warning('订单已创建，但入住办理未成功，请稍后在订单中办理')
      }
    }
    visible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open, loadRoomTypes })
</script>

<style scoped>
.member-lookup {
  display: flex;
  gap: 8px;
  width: 100%;
}

.member-card {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  font-size: 13px;
}

.member-name {
  font-weight: 600;
  color: #166534;
}

.member-level {
  color: #15803d;
}

.member-phone {
  color: #64748b;
}

.hint {
  margin-left: 10px;
  font-size: 12px;
  color: #94a3b8;
}
</style>
