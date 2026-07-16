<template>
  <div>
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <el-row :gutter="20">
      <!-- 个人资料卡片 -->
      <el-col :xs="24" :sm="14">
        <div class="content-card section-card">
          <div class="section-header">
            <el-icon :size="20" color="#d4a853"><User /></el-icon>
            <h3>个人资料</h3>
          </div>
          <el-form v-loading="profileLoading" :model="profileForm" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机号码">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="电子邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" />
            </el-form-item>
            <el-form-item label="当前角色">
              <el-tag effect="plain">{{ profileForm.role }}</el-tag>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" round :loading="saveLoading" @click="saveProfile">
                <el-icon><Check /></el-icon> 保存资料
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <!-- 修改密码卡片 -->
      <el-col :xs="24" :sm="10">
        <div class="content-card section-card">
          <div class="section-header">
            <el-icon :size="20" color="#6366f1"><Lock /></el-icon>
            <h3>修改密码</h3>
          </div>
          <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="profile-form">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位字符" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" round :loading="pwdLoading" @click="submitPassword">
                <el-icon><Key /></el-icon> 修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Check, Key } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, changePassword } from '@/api/user'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const profileLoading = ref(false)
const saveLoading = ref(false)
const pwdLoading = ref(false)
const pwdRef = ref<FormInstance>()

const profileForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  role: '',
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_r, v, cb) => {
        if (v !== pwdForm.newPassword) cb(new Error('两次输入的密码不一致'))
        else cb()
      },
      trigger: 'blur',
    },
  ],
}

onMounted(() => loadProfile())

async function loadProfile() {
  profileLoading.value = true
  try {
    const data = await getProfile()
    profileForm.username = data.username
    profileForm.realName = data.realName || ''
    profileForm.phone = data.phone || ''
    profileForm.email = data.email || ''
    profileForm.role = data.role
  } finally {
    profileLoading.value = false
  }
}

async function saveProfile() {
  saveLoading.value = true
  try {
    await updateProfile({
      realName: profileForm.realName,
      phone: profileForm.phone,
      email: profileForm.email,
    })
    ElMessage.success('资料已更新成功')
    if (auth.user) {
      auth.setAuth({ ...auth.user, realName: profileForm.realName })
    }
  } finally {
    saveLoading.value = false
  }
}

async function submitPassword() {
  const valid = await pwdRef.value?.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    await changePassword(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('密码已修改，请重新登录')
    auth.clearAuth()
    router.push('/login')
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.section-card {
  height: 100%;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.profile-form {
  max-width: 450px;
}
</style>
