<template>
  <view class="create-page">
    <!-- room summary -->
    <view class="room-card">
      <image class="room-img" :src="coverImg || defaultImg" mode="aspectFill"></image>
      <view class="room-text">
        <text class="room-name">{{ roomName || '加载中...' }}</text>
        <text class="room-price">¥{{ price }} / 晚</text>
      </view>
    </view>

    <!-- order details -->
    <view class="info-card">
      <view class="info-row">
        <text class="info-label">入住日期</text>
        <text class="info-val">{{ formatDate(checkIn) }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">退房日期</text>
        <text class="info-val">{{ formatDate(checkOut) }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">入住天数</text>
        <text class="info-val">{{ nights }}晚</text>
      </view>
      <view class="info-row">
        <text class="info-label">房间数量</text>
        <text class="info-val">{{ roomCount }}间</text>
      </view>
      <view class="info-row total">
        <text class="info-label">合计金额</text>
        <text class="info-val price">¥{{ totalPrice.toFixed(2) }}</text>
      </view>
    </view>

    <!-- guest info -->
    <view class="info-card">
      <view class="section-title">入住人信息</view>
      <view class="form-item">
        <text class="form-label">姓名</text>
        <input class="form-input" v-model="form.guestName" placeholder="请输入姓名" />
      </view>
      <view class="form-item">
        <text class="form-label">手机号</text>
        <input class="form-input" v-model="form.guestPhone" type="number" placeholder="请输入手机号" maxlength="11" />
      </view>
      <view class="form-item">
        <text class="form-label">备注</text>
        <input class="form-input" v-model="form.remark" placeholder="选填，如安静高层房间" />
      </view>
    </view>

    <!-- actions -->
    <view class="action-row">
      <button class="cancel-btn" @click="goBack">取消</button>
      <button class="confirm-btn" :loading="submitting" @click="submitOrder">确认预订</button>
    </view>
  </view>
</template>

<script>
import { createOrder } from '../../src/api/order'

export default {
  data() {
    return {
      roomTypeId: 0,
      roomName: '',
      price: 0,
      roomCount: 1,
      checkIn: '',
      checkOut: '',
      guests: 2,
      coverImg: '',
      form: { guestName: '', guestPhone: '', remark: '' },
      submitting: false,
      defaultImg: '/static/lose.png'
    }
  },
  computed: {
    nights() {
      if (!this.checkIn || !this.checkOut) return 0
      const d = (new Date(this.checkOut) - new Date(this.checkIn)) / 86400000
      return d > 0 ? d : 0
    },
    totalPrice() {
      return this.price * (this.nights || 1) * (this.roomCount || 1)
    }
  },
  onLoad(options) {
    this.roomTypeId = Number(options.roomTypeId) || 0
    this.roomName = decodeURIComponent(options.roomName || '')
    this.coverImg = decodeURIComponent(options.coverImg || '')
    this.checkIn = options.checkIn || ''
    this.checkOut = options.checkOut || ''
    this.guests = Number(options.guests) || 2
    this.price = Number(options.price) || 0
    this.roomCount = Number(options.roomCount) || 1
    this.roomName = decodeURIComponent(options.roomName || '')
    this.coverImg = decodeURIComponent(options.coverImg || '')
  },
  methods: {
    formatDate(d) {
      if (!d) return '-'
      const parts = d.split('-')
      if (parts.length === 3) return parts[0] + '年' + parts[1] + '月' + parts[2] + '日'
      return d
    },
    async submitOrder() {
      if (!this.form.guestName.trim()) {
        uni.showToast({ title: '请输入入住人姓名', icon: 'none' })
        return
      }
      if (!/^1\d{10}$/.test(this.form.guestPhone)) {
        uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
        return
      }
      if (this.nights <= 0) {
        uni.showToast({ title: '日期信息有误', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        const customerId = uni.getStorageSync('customerId')
        const res = await createOrder({
          roomTypeId: this.roomTypeId,
          customerId: Number(customerId) || undefined,
          roomCount: this.roomCount,
          checkInDate: this.checkIn,
          checkOutDate: this.checkOut,
          guestName: this.form.guestName.trim(),
          guestPhone: this.form.guestPhone,
          remark: this.form.remark
        })
        uni.showToast({ title: '预订成功', icon: 'success' })
        setTimeout(() => {
          uni.redirectTo({ url: '/pages/order/detail?id=' + (res.data && res.data.id) })
        }, 800)
      } catch {
        // handled
      } finally {
        this.submitting = false
      }
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.create-page {
  background: #F5F6FA;
  min-height: 100vh;
  padding-bottom: 48rpx;
}

.room-card {
  display: flex;
  background: #FFFFFF;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.room-img {
  width: 160rpx;
  height: 120rpx;
  border-radius: 12rpx;
  background: #E8E8E8;
  flex-shrink: 0;
}

.room-text {
  flex: 1;
  padding-left: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.room-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 6rpx;
}

.room-price {
  font-size: 26rpx;
  color: #C9A96E;
}

.info-card {
  background: #FFFFFF;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 14rpx 0;
  border-bottom: 1rpx solid #F5F5F5;

  &:last-child { border-bottom: none; }
  &.total { padding-top: 18rpx; }
}

.info-label { font-size: 28rpx; color: #666; }
.info-val { font-size: 28rpx; color: #333; font-weight: 500; }
.info-val.price { color: #C9A96E; font-weight: 700; font-size: 34rpx; }

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
}

.form-item { margin-bottom: 18rpx; }
.form-label { font-size: 26rpx; color: #666; display: block; margin-bottom: 8rpx; }
.form-input {
  height: 80rpx;
  background: #F8F9FA;
  border-radius: 10rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
}

.action-row {
  display: flex;
  gap: 20rpx;
  padding: 0 20rpx;
  margin-top: 12rpx;
}

.cancel-btn {
  flex: 1;
  height: 88rpx;
  background: #F5F6FA;
  color: #666;
  font-size: 30rpx;
  border-radius: 14rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;

  &::after { border: none; }
}

.confirm-btn {
  flex: 2;
  height: 88rpx;
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  color: #FFF;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 14rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;

  &::after { border: none; }
}
</style>
