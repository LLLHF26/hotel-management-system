<template>
  <div>
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>客户管理</h2>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="姓名 / 手机号搜索" clearable style="width: 210px"
        :prefix-icon="Search" />
      <el-select v-model="query.memberLevel" placeholder="会员等级" clearable style="width: 140px">
        <el-option v-for="m in MEMBER_LEVEL_OPTIONS" :key="m.value" :label="m.label" :value="m.value" />
      </el-select>
      <el-button type="primary" @click="search">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button type="success" round @click="registerVisible = true">+ 登记散客</el-button>
    </div>

    <!-- 表格区域 -->
    <div class="content-card table-card">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="realName" label="客户姓名" min-width="110">
          <template #default="{ row }">
            <span class="customer-name">{{ row.realName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">
            <span class="phone-text">{{ row.phone }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="memberLevel" label="会员等级" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.memberLevel" size="small" effect="light" round>{{ row.memberLevel }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="积分" width="85" align="center">
          <template #default="{ row }">
            <span class="points-text">{{ row.points ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="累计消费" min-width="110">
          <template #default="{ row }">
            <span class="amount-text">&yen;{{ formatMoney(row.totalConsumed ?? 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">查看详情</el-button>
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

    <!-- 登记散客弹窗 -->
    <el-dialog v-model="registerVisible" title="登记散客信息" width="480px" destroy-on-close>
      <el-form ref="regFormRef" :model="regForm" :rules="regRules" label-width="90px">
        <el-form-item label="客户姓名" prop="realName">
          <el-input v-model="regForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="regForm.phone" placeholder="请输入11位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="regForm.idCard" placeholder="选填" maxlength="18" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="regForm.gender">
            <el-radio-button :value="1">男</el-radio-button>
            <el-radio-button :value="2">女</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerVisible = false">取消</el-button>
        <el-button type="primary" :loading="regLoading" @click="submitRegister">确认登记</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import type { CustomerVO } from '@/types'
import { getCustomerList, registerCustomer } from '@/api/customer'
import { formatMoney } from '@/utils/format'
import { MEMBER_LEVEL_OPTIONS } from '@/utils/constants'

const router = useRouter()
const loading = ref(false)
const list = ref<CustomerVO[]>([])
const total = ref(0)
const registerVisible = ref(false)
const regLoading = ref(false)
const regFormRef = ref<FormInstance>()

const query = reactive({ page: 1, size: 10, keyword: '', memberLevel: '' })
const regForm = reactive({ realName: '', phone: '', idCard: '', gender: 1 })

const regRules: FormRules = {
  realName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
}

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try {
    const res = await getCustomerList({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      memberLevel: query.memberLevel || undefined,
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

function goDetail(id: number) {
  router.push(`/front-desk/customers/${id}`)
}

async function submitRegister() {
  const valid = await regFormRef.value?.validate().catch(() => false)
  if (!valid) return
  regLoading.value = true
  try {
    const res = await registerCustomer({
      realName: regForm.realName,
      phone: regForm.phone,
      idCard: regForm.idCard || undefined,
      gender: regForm.gender,
    })
    ElMessage.success('登记成功')
    registerVisible.value = false
    regForm.realName = ''
    regForm.phone = ''
    regForm.idCard = ''
    await loadList()
    if (res.id) goDetail(res.id)
  } finally {
    regLoading.value = false
  }
}
</script>

<style scoped>
.table-card {
  overflow: hidden;
}

.customer-name {
  font-weight: 500;
}

.phone-text {
  font-family: 'SF Mono', 'Consolas', monospace;
  font-size: 12.5px;
  color: #475569;
}

.points-text {
  color: #d4a853;
  font-weight: 600;
  font-size: 13px;
}

.amount-text {
  font-weight: 600;
  color: #059669;
  font-size: 13px;
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
