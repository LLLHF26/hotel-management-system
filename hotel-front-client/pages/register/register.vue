<template>
  <view class="register-page">
    <view class="form-card">
      <view class="section-title">会员注册</view>

      <view class="form-item">
        <text class="form-label">真实姓名</text>
        <input class="form-input" v-model="form.realName" placeholder="请输入真实姓名" maxlength="20" />
      </view>

      <view class="form-item">
        <text class="form-label">手机号</text>
        <input class="form-input" v-model="form.phone" type="number" placeholder="请输入手机号" maxlength="11" />
      </view>

      <view class="form-item">
        <text class="form-label">密码</text>
        <input class="form-input" v-model="form.password" type="password" placeholder="请输入密码" maxlength="20" />
      </view>

      <view class="form-item">
        <text class="form-label">确认密码</text>
        <input class="form-input" v-model="confirmPwd" type="password" placeholder="请再次输入密码" maxlength="20" />
      </view>

      <view class="form-item">
        <text class="form-label">性别</text>
        <view class="gender-row">
          <view
            class="gender-option"
            :class="{ active: form.gender === 1 }"
            @click="form.gender = 1"
          >男</view>
          <view
            class="gender-option"
            :class="{ active: form.gender === 2 }"
            @click="form.gender = 2"
          >女</view>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">身份证号</text>
        <input class="form-input" v-model="form.idCard" placeholder="选填" maxlength="18" />
      </view>

      <button class="submit-btn" :loading="loading" @click="handleRegister">注 册</button>

      <view class="form-footer">
        <text class="link-text" @click="goLogin">已有账号？去登录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { register } from '../../src/api/auth'

export default {
  data() {
    return {
      form: {
        realName: '',
        phone: '',
        password: '',
        gender: 1,
        idCard: ''
      },
      confirmPwd: '',
      loading: false
    }
  },
  methods: {
    validate() {
      if (!this.form.realName.trim()) return '请输入真实姓名'
      if (!/^1\d{10}$/.test(this.form.phone)) return '请输入正确的手机号'
      if (this.form.password.length < 6) return '密码至少6位'
      if (this.form.password !== this.confirmPwd) return '两次密码输入不一致'
      return ''
    },
    async handleRegister() {
      const err = this.validate()
      if (err) {
        uni.showToast({ title: err, icon: 'none' })
        return
      }
      this.loading = true
      try {
        await register({
          realName: this.form.realName.trim(),
          phone: this.form.phone,
          password: this.form.password,
          gender: this.form.gender,
          idCard: this.form.idCard
        })
        uni.showToast({ title: '注册成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 800)
      } catch {
        // handled by interceptor
      } finally {
        this.loading = false
      }
    },
    goLogin() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: #F5F6FA;
  padding: 24rpx 32rpx;
}

.form-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #2C3E50;
  text-align: center;
  margin-bottom: 40rpx;
}

.form-item {
  margin-bottom: 28rpx;
}

.form-label {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 12rpx;
  display: block;
}

.form-input {
  height: 88rpx;
  background: #F8F9FA;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  color: #333333;
  border: 2rpx solid transparent;
  transition: border-color 0.2s;

  &:focus { border-color: #C9A96E; background: #FFFFFF; }
}

.gender-row {
  display: flex;
  gap: 24rpx;
}

.gender-option {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F8F9FA;
  border-radius: 12rpx;
  font-size: 30rpx;
  color: #666666;
  border: 2rpx solid transparent;

  &.active {
    border-color: #C9A96E;
    color: #C9A96E;
    background: rgba(201, 169, 110, 0.06);
    font-weight: 600;
  }
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  color: #FFFFFF;
  font-size: 34rpx;
  font-weight: 600;
  border-radius: 16rpx;
  margin-top: 32rpx;
  letter-spacing: 4rpx;
  border: none;

  &::after { border: none; }
}

.form-footer {
  text-align: center;
  margin-top: 28rpx;

  .link-text {
    color: #C9A96E;
    font-size: 28rpx;
  }
}
</style>
