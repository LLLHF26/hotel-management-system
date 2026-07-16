<template>
  <div class="login-page">
    <div class="login-bg-pattern"></div>
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">
          <el-icon :size="28" color="#d4a853"><OfficeBuilding /></el-icon>
        </div>
        <h1>云端酒店</h1>
        <p>前台管理系统</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large"
            class="login-input" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            class="login-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
          登 录 系 统
        </el-button>
      </el-form>
      <div class="login-footer">
        &copy; 2024 云端酒店 · 前台管理系统
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await login(form.username, form.password)
    if (data.role && data.role !== 'FRONT_DESK' && data.role !== 'ADMIN') {
      ElMessage.warning('该账号无前台权限')
      return
    }
    auth.setAuth(data)
    const redirect = (route.query.redirect as string) || '/front-desk/dashboard'
    router.push(redirect)
  } catch {
    ElMessage.error('用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e2532 0%, #2d3748 50%, #f5f7fa 100%);
  position: relative;
  overflow: hidden;
}

.login-bg-pattern {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle at 20% 80%, rgba(212, 168, 83, 0.06) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(30, 58, 95, 0.08) 0%, transparent 50%);
}

.login-card {
  width: 400px;
  padding: 40px 36px;
  background: #fff;
  border-radius: 16px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-logo {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  background: linear-gradient(135deg, #d4a853 0%, #c49b48 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(212, 168, 83, 0.3);
}

.login-header h1 {
  margin: 0;
  font-size: 22px;
  color: #111827;
  font-weight: 700;
}

.login-header p {
  margin: 6px 0 0;
  color: #9ca3af;
  font-size: 13px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  padding: 6px 12px;
}

.login-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d4a853 inset;
}

.login-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--fd-primary), 0 0 0 3px rgba(30, 58, 95, 0.08);
}

.login-btn {
  width: 100%;
  margin-top: 8px;
  border-radius: 10px;
  height: 44px;
  font-size: 15px;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a8a 100%);
  border: none;
  transition: all 0.25s ease;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(30, 58, 95, 0.35);
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 11px;
  color: #c0c4cc;
}
</style>
