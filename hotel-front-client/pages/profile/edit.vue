<template>
  <view class="edit-page">
    <view class="form-card">
      <!-- avatar -->
      <view class="avatar-row">
        <text class="section-label">头像</text>
        <view class="avatar-box" @click="handleChangeAvatar">
          <image v-if="form.avatar" class="avatar-img" :src="form.avatar" mode="aspectFill"></image>
          <text v-else class="avatar-icon">👤</text>
          <text class="avatar-hint">{{ form.avatar ? '更换头像' : '点击上传' }}</text>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">姓名</text>
        <input class="form-input" v-model="form.realName" placeholder="请输入姓名" />
      </view>

      <view class="form-item">
        <text class="form-label">手机号</text>
        <input class="form-input" v-model="form.phone" disabled placeholder="手机号" />
        <text class="form-hint">手机号不可修改</text>
      </view>

      <view class="form-item">
        <text class="form-label">性别</text>
        <view class="gender-row">
          <view class="gender-opt" :class="{ active: form.gender === 1 }" @click="form.gender = 1">男</view>
          <view class="gender-opt" :class="{ active: form.gender === 2 }" @click="form.gender = 2">女</view>
        </view>
      </view>

      <view class="form-item">
        <text class="form-label">身份证号</text>
        <input class="form-input" v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
      </view>
    </view>

    <button class="save-btn" :loading="saving" @click="handleSave">保存修改</button>
  </view>
</template>

<script>
import { getProfile, updateProfile, uploadAvatar } from '../../src/api/customer'

export default {
  data() {
    return {
      form: { realName: '', phone: '', gender: 1, idCard: '', avatar: '' },
      saving: false
    }
  },
  onLoad() {
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      try {
        const res = await getProfile()
        const data = res.data || {}
        this.form.realName = data.realName || ''
        this.form.phone = data.phone || ''
        this.form.gender = data.gender ?? 1
        this.form.idCard = data.idCard || ''
        this.form.avatar = data.avatar || ''
      } catch { /* ignore */ }
    },
    handleChangeAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: async (res) => {
          const filePath = res.tempFilePaths[0]
          uni.showLoading({ title: '上传中...' })
          try {
            const result = await uploadAvatar(filePath)
            const url = (result.data && result.data.url) || ''
            if (url) {
              this.form.avatar = url
            }
          } catch { /* error handled by request */ }
          finally { uni.hideLoading() }
        }
      })
    },
    async handleSave() {
      if (!this.form.realName.trim()) {
        uni.showToast({ title: '请输入姓名', icon: 'none' })
        return
      }
      this.saving = true
      try {
        await updateProfile({
          realName: this.form.realName.trim(),
          gender: this.form.gender,
          idCard: this.form.idCard,
          avatar: this.form.avatar
        })
        uni.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      } catch { /* ignore */ }
      finally { this.saving = false }
    }
  }
}
</script>

<style lang="scss" scoped>
.edit-page {
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

.avatar-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #F5F5F5;
}

.section-label { font-size: 28rpx; color: #333; }
.avatar-box { display: flex; flex-direction: column; align-items: center; }
.avatar-img {
  width: 120rpx; height: 120rpx; border-radius: 50%;
  background: #E8E8E8;
}
.avatar-icon { font-size: 60rpx; }
.avatar-hint { font-size: 22rpx; color: #999; margin-top: 4rpx; }

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
  &[disabled] { color: #999; }
}

.form-hint { font-size: 22rpx; color: #CCC; margin-top: 6rpx; display: block; }

.gender-row { display: flex; gap: 24rpx; }
.gender-opt {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F8F9FA;
  border-radius: 10rpx;
  font-size: 28rpx;
  color: #666;
  &.active { background: rgba(201,169,110,.1); color: #C9A96E; font-weight: 600; }
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
