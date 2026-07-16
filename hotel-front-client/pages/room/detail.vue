<template>
  <view class="room-detail-page" v-if="room">
    <!-- image swiper -->
    <swiper class="img-swiper" indicator-dots indicator-color="rgba(255,255,255,.5)" indicator-active-color="#C9A96E" autoplay circular>
      <swiper-item v-for="(img, i) in images" :key="i">
        <image class="swiper-img" :src="img" mode="aspectFill"></image>
      </swiper-item>
    </swiper>

    <!-- room info -->
    <view class="info-card">
      <view class="info-header">
        <text class="info-name">{{ room.name }}</text>
        <view class="info-price">
          <text class="price">{{ room.price }}</text>
          <text class="price-label">/晚</text>
        </view>
      </view>
      <text class="info-desc">{{ room.description || '暂无描述' }}</text>
      <view class="info-meta-grid">
        <view class="meta-item">
          <text class="meta-label">面积</text>
          <text class="meta-value">{{ room.area || '-' }}㎡</text>
        </view>
        <view class="meta-item">
          <text class="meta-label">床型</text>
          <text class="meta-value">{{ room.bedType || '-' }}</text>
        </view>
        <view class="meta-item">
          <text class="meta-label">人数</text>
          <text class="meta-value">{{ room.maxGuests || '-' }}人</text>
        </view>
        <view class="meta-item">
          <text class="meta-label">可订</text>
          <text class="meta-value" style="color:#27AE60">{{ availableCountText }}</text>
        </view>
      </view>
      <view class="amenities-row" v-if="room.amenities">
        <text class="tag" v-for="a in amenitiesList" :key="a">{{ a }}</text>
      </view>
    </view>

    <!-- booking card -->
    <view class="book-card">
      <view class="book-row">
        <view class="book-item">
          <text class="book-label">入住日期</text>
          <picker mode="date" :value="form.checkIn" :start="todayStr" @change="onCheckIn">
            <text class="book-val">{{ form.checkIn || '请选择' }}</text>
          </picker>
        </view>
        <view class="book-item">
          <text class="book-label">退房日期</text>
          <picker mode="date" :value="form.checkOut" :start="minCheckOut" @change="onCheckOut">
            <text class="book-val">{{ form.checkOut || '请选择' }}</text>
          </picker>
        </view>
      </view>
      <view class="book-row" v-if="datesSelected && availableCount > 0">
        <view class="book-item">
          <text class="book-label">房间数量</text>
          <view class="qty-row">
            <view class="qty-btn" @click="decrQty">−</view>
            <text class="qty-val">{{ form.roomCount }}</text>
            <view class="qty-btn" @click="incrQty">+</view>
          </view>
        </view>
      </view>
      <view class="book-summary" v-if="datesSelected && nights > 0">
        <text>共 <text class="hl">{{ nights }}</text> 晚 · <text class="hl">{{ form.roomCount }}</text> 间</text>
        <text class="total-price">预计总价 ¥{{ totalPrice.toFixed(2) }}</text>
      </view>
      <button class="book-btn" @click="goCreateOrder">立即预订</button>
    </view>

    <!-- quick links -->
    <view class="quick-row">
      <view class="quick-btn" @click="goChat">🤖 咨询客服</view>
      <view class="quick-btn" @click="goRecommend">🎯 智能推荐</view>
    </view>
  </view>
</template>

<script>
import { getRoomTypeDetail } from '../../src/api/room'
import { getAvailableRoomCount } from '../../src/api/order'

export default {
  data() {
    return {
      room: null,
      availableCount: 0,
      loadingAvailable: false,
      form: { checkIn: '', checkOut: '', roomCount: 1 }
    }
  },
  computed: {
    datesSelected() {
      return !!(this.form.checkIn && this.form.checkOut)
    },
    availableCountText() {
      if (!this.datesSelected) return '请选择日期'
      if (this.loadingAvailable) return '查询中...'
      return this.availableCount + '间'
    },
    todayStr() {
      const d = new Date()
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
    },
    minCheckOut() {
      if (!this.form.checkIn) return this.todayStr
      const d = new Date(this.form.checkIn)
      d.setDate(d.getDate() + 1)
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
    },
    images() {
      if (!this.room) return []
      const imgs = this.room.images || []
      return imgs.length > 0 ? imgs : [this.room.coverImage || '/static/lose.png']
    },
    amenitiesList() {
      if (!this.room || !this.room.amenities) return []
      return typeof this.room.amenities === 'string'
        ? this.room.amenities.split(/[,，、]/)
        : this.room.amenities
    },
    nights() {
      if (!this.form.checkIn || !this.form.checkOut) return 0
      const d1 = new Date(this.form.checkIn)
      const d2 = new Date(this.form.checkOut)
      const diff = (d2 - d1) / 86400000
      return diff > 0 ? diff : 0
    },
    totalPrice() {
      if (!this.room) return 0
      return (this.room.price || 0) * (this.nights || 1) * (this.form.roomCount || 1)
    }
  },
  onLoad(options) {
    const id = options.id
    if (id) {
      this.loadDetail(id)
    }
    const cIn = uni.getStorageSync('checkIn')
    const cOut = uni.getStorageSync('checkOut')
    if (cIn) this.form.checkIn = cIn
    if (cOut) this.form.checkOut = cOut
    if (cIn && cOut) {
      this.loadAvailableByDate(id)
    }
  },
  methods: {
    async loadDetail(id) {
      try {
        const res = await getRoomTypeDetail(id)
        this.room = res.data || res
        if (!this.room) {
          uni.showToast({ title: '房型不存在', icon: 'none' })
          setTimeout(() => uni.navigateBack(), 1000)
        }
      } catch { /* ignore */ }
    },
    async loadAvailableByDate(id) {
      if (!this.datesSelected) return
      this.loadingAvailable = true
      try {
        const res = await getAvailableRoomCount(id, this.form.checkIn, this.form.checkOut)
        this.availableCount = (res.data && res.data.availableCount) || 0
        if (this.form.roomCount > this.availableCount) {
          this.form.roomCount = this.availableCount > 0 ? this.availableCount : 1
        }
      } catch {
        this.availableCount = 0
      } finally {
        this.loadingAvailable = false
      }
    },
    incrQty() {
      if (this.form.roomCount < this.availableCount) {
        this.form.roomCount++
      } else {
        uni.showToast({ title: '最多可订' + this.availableCount + '间', icon: 'none' })
      }
    },
    decrQty() {
      if (this.form.roomCount > 1) this.form.roomCount--
    },
    onCheckIn(e) {
      this.form.checkIn = e.detail.value
      if (this.form.checkOut && this.form.checkOut <= this.form.checkIn) {
        this.form.checkOut = ''
        return
      }
      if (this.room && this.datesSelected) {
        this.loadAvailableByDate(this.room.id)
      }
    },
    onCheckOut(e) {
      this.form.checkOut = e.detail.value
      if (this.room && this.datesSelected) {
        this.loadAvailableByDate(this.room.id)
      }
    },
    goCreateOrder() {
      if (!this.form.checkIn || !this.form.checkOut) {
        uni.showToast({ title: '请选择入住和退房日期', icon: 'none' })
        return
      }
      if (this.nights <= 0) {
        uni.showToast({ title: '退房日期须晚于入住日期', icon: 'none' })
        return
      }
      const params = new URLSearchParams({
        roomTypeId: this.room.id,
        roomName: this.room.name,
        coverImg: (this.room.coverImage || ''),
        checkIn: this.form.checkIn,
        checkOut: this.form.checkOut,
        guests: this.room.maxGuests || 2,
        price: this.room.price || 0,
        roomCount: this.form.roomCount
      }).toString()
      uni.navigateTo({ url: '/pages/order/create?' + params })
    },
    goChat() {
      uni.switchTab({ url: '/pages/chat/chat' })
    },
    goRecommend() {
      uni.navigateTo({ url: '/pages/recommend/recommend' })
    }
  }
}
</script>

<style lang="scss" scoped>
.room-detail-page {
  background: #F5F6FA;
  min-height: 100vh;
  padding-bottom: 48rpx;
}

.img-swiper {
  height: 480rpx;

  swiper-item { border-radius: 0 0 24rpx 24rpx; overflow: hidden; }
}

.swiper-img {
  width: 100%;
  height: 100%;
  background: #E8E8E8;
}

.info-card {
  background: #FFFFFF;
  margin: 20rpx;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12rpx;
}

.info-name {
  font-size: 38rpx;
  font-weight: 700;
  color: #2C3E50;
  flex: 1;
}

.info-price {
  display: flex;
  align-items: baseline;
  flex-shrink: 0;

  .price {
    color: #C9A96E;
    font-weight: 700;
    font-size: 40rpx;
    &::before { content: '¥'; font-size: 28rpx; margin-right: 2rpx; }
  }
  .price-label { font-size: 24rpx; color: #999; margin-left: 4rpx; }
}

.info-desc {
  font-size: 26rpx;
  color: #888888;
  line-height: 1.7;
  display: block;
  margin-bottom: 20rpx;
}

.info-meta-grid {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 16rpx;
}

.meta-item {
  width: 50%;
  display: flex;
  justify-content: space-between;
  padding: 10rpx 0;
  padding-right: 32rpx;
}

.meta-label {
  font-size: 26rpx;
  color: #999999;
}

.meta-value {
  font-size: 28rpx;
  color: #333333;
  font-weight: 500;
}

.amenities-row {
  padding-top: 12rpx;
  border-top: 1rpx solid #F0F0F0;
}

.book-card {
  background: #FFFFFF;
  margin: 0 20rpx 20rpx;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.book-row {
  display: flex;
  gap: 20rpx;
  margin-bottom: 16rpx;
}

.book-item {
  flex: 1;
  background: #F8F9FA;
  border-radius: 12rpx;
  padding: 20rpx;
}

.book-label {
  display: block;
  font-size: 24rpx;
  color: #999999;
  margin-bottom: 6rpx;
}

.book-val {
  font-size: 28rpx;
  color: #333333;
  font-weight: 500;
}

.qty-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.qty-btn {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #E8E8E8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;

  &:active { background: #C9A96E; color: #FFF; }
}

.qty-val {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  min-width: 40rpx;
  text-align: center;
}

.book-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  font-size: 28rpx;
  color: #666666;

  .hl { color: #E74C3C; font-weight: 600; }
  .total-price { color: #C9A96E; font-weight: 700; font-size: 32rpx; }
}

.book-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 14rpx;
  margin-top: 16rpx;
  letter-spacing: 2rpx;
  border: none;

  &::after { border: none; }
}

.quick-row {
  display: flex;
  gap: 20rpx;
  margin: 0 20rpx;
}

.quick-btn {
  flex: 1;
  text-align: center;
  background: #FFFFFF;
  padding: 24rpx;
  border-radius: 14rpx;
  font-size: 28rpx;
  color: #666666;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}
</style>
