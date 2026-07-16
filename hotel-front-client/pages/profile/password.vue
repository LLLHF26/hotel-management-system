<template>
  <view class="password-page">
    <view class="form-card">
      <view class="form-item">
        <text class="form-label">旧密码</text>
        <input class="form-input" v-model="form.oldPassword" type="password" placeholder="请输入旧密码" />
      </view>
      <view class="form-item">
        <text class="form-label">新密码</text>
        <input class="form-input" v-model="form.newPassword" type="password" placeholder="请输入新密码" />
      </view>
      <view class="form-item" style="border-bottom:none">
        <text class="form-label">确认新密码</text>
        <input class="form-input" v-model="confirmPwd" type="password" placeholder="请再次输入新密码" />
      </view>
    </view>

    <button class="save-btn" :loading="saving" @click="handleSave">保存修改</button>
  </view>
</template>

<script>
import { changePassword } from '../../src/api/customer'

export default {
  data() {
    return {
      form: { oldPassword: '', newPassword: '' },
      confirmPwd: '',
      saving: false
    }
  },
  methods: {
    validate() {
      if (!this.form.oldPassword) return '请输入旧密码'
      if (this.form.newPassword.length < 6) return '新密码至少6位'
      if (this.form.newPassword !== this.confirmPwd) return '两次密码输入不一致'
      return ''
    },
    async handleSave() {
      const err = this.validate()
      if (err) {
        uni.showToast({ title: err, icon: 'none' })
        return
      }
      this.saving = true
      try {
        await changePassword(this.form.oldPassword, this.form.newPassword)
        uni.showToast({ title: '密码修改成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      } catch { /* ignore */ }
      finally { this.saving = false }
    }
  }
}
</script>

<style lang="scss" scoped>
.password-page {
  background: #F5F6FA;
  min-height: 100vh;
  padding: 20rpx;
}

.form-card {
  background: #FFF;
  border-radius: 16rpx;
  padding: 8rpx 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,.03);
}

.form-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F5F5F5;
}

.form-label { font-size: 28rpx; color: #333; display: block; margin-bottom: 12rpx; }

.form-input {
  height: 80rpx;
  background: #F8F9FA;
  border-radius: 10rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  color: #FFF;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 14rpx;
  margin-top: 32rpx;
  border: none;
  &::after { border: none; }
}
</style>
