<template>
  <view class="login-page">
    <view class="login-bg"></view>
    <view class="login-body">
      <view class="login-header">
        <image class="login-logo" src="/static/logo.png" mode="aspectFit"></image>
        <text class="login-title">云端酒店</text>
        <text class="login-subtitle">欢迎回来</text>
      </view>

      <view class="login-form">
        <view class="input-group">
          <view class="input-icon">📱</view>
          <input
            class="input-field"
            v-model="username"
            placeholder="手机号／用户名"
            placeholder-style="color:#B0B0B0"
            maxlength="20"
          />
        </view>
        <view class="input-group">
          <view class="input-icon">🔒</view>
          <input
            class="input-field"
            v-model="password"
            type="password"
            placeholder="密码"
            placeholder-style="color:#B0B0B0"
            maxlength="20"
          />
        </view>

        <button class="login-btn" :loading="loading" @click="handleLogin">登 录</button>

        <view class="login-footer">
          <text class="link-text" @click="goRegister">没有账号？立即注册</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { login } from '../../src/api/auth'

export default {
  data() {
    return {
      username: '',
      password: '',
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.username.trim()) {
        uni.showToast({ title: '请输入手机号/用户名', icon: 'none' })
        return
      }
      if (!this.password) {
        uni.showToast({ title: '请输入密码', icon: 'none' })
        return
      }
      this.loading = true
      try {
        const res = await login(this.username.trim(), this.password)
        uni.setStorageSync('token', res.data.token)
        uni.setStorageSync('customerId', res.data.customerId || '')
        uni.setStorageSync('memberLevel', res.data.memberLevel || '')
        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/index/index' })
        }, 800)
      } catch {
        // error toast handled by request interceptor
      } finally {
        this.loading = false
      }
    },
    goRegister() {
      uni.navigateTo({ url: '/pages/register/register' })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #F5F6FA;
  position: relative;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 420rpx;
  background: linear-gradient(160deg, #2C3E50 0%, #3D5166 60%, #C9A96E 100%);
  border-radius: 0 0 80rpx 80rpx;
}

.login-body {
  position: relative;
  z-index: 1;
  padding: 0 48rpx;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 120rpx;
  padding-bottom: 60rpx;
}

.login-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 28rpx;
  background: #FFFFFF;
  padding: 12rpx;
  margin-bottom: 24rpx;
}

.login-title {
  font-size: 44rpx;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 4rpx;
  margin-bottom: 8rpx;
}

.login-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
}

.login-form {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 48rpx 36rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
}

.input-group {
  display: flex;
  align-items: center;
  background: #F8F9FA;
  border-radius: 12rpx;
  padding: 0 20rpx;
  margin-bottom: 24rpx;
  border: 2rpx solid transparent;
  transition: border-color 0.2s;

  &:focus-within {
    border-color: #C9A96E;
    background: #FFFFFF;
  }
}

.input-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.input-field {
  flex: 1;
  height: 100rpx;
  font-size: 30rpx;
  color: #333333;
}

.login-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  color: #FFFFFF;
  font-size: 34rpx;
  font-weight: 600;
  border-radius: 16rpx;
  margin-top: 16rpx;
  letter-spacing: 4rpx;
  border: none;

  &::after { border: none; }
}

.login-footer {
  text-align: center;
  margin-top: 32rpx;

  .link-text {
    color: #C9A96E;
    font-size: 28rpx;
  }
}
</style>
