<template>
  <view class="home-page">
    <!-- search bar -->
    <view class="search-bar">
      <view class="search-row">
        <view class="search-item">
          <text class="search-label">入住</text>
          <picker mode="date" :value="checkIn" :start="todayStr" @change="onCheckIn">
            <text class="search-val">{{ checkIn || '选择日期' }}</text>
          </picker>
        </view>
        <view class="search-divider"></view>
        <view class="search-item">
          <text class="search-label">退房</text>
          <picker mode="date" :value="checkOut" :start="minCheckOut" @change="onCheckOut">
            <text class="search-val">{{ checkOut || '选择日期' }}</text>
          </picker>
        </view>
        <view class="search-divider"></view>
        <view class="search-item">
          <text class="search-label">人数</text>
          <picker :range="guestRange" :value="guestIdx" @change="onGuestChange">
            <text class="search-val">{{ guests }}人</text>
          </picker>
        </view>
        <view class="search-btn" @click="goRoomList">
          <text class="search-btn-text">搜索</text>
        </view>
      </view>
    </view>

    <!-- banner -->
    <swiper class="banner-swiper" indicator-dots indicator-color="rgba(255,255,255,.5)" indicator-active-color="#C9A96E" autoplay circular>
      <swiper-item v-for="(b, i) in banners" :key="i">
        <view class="banner-card" :style="{ background: b.bg }">
          <text class="banner-title">{{ b.title }}</text>
          <text class="banner-desc">{{ b.desc }}</text>
        </view>
      </swiper-item>
    </swiper>

    <!-- quick entries -->
    <view class="quick-entries">
      <view class="entry-item" @click="goRoomList">
        <view class="entry-icon-box" style="background:rgba(201,169,110,.12)">🏨</view>
        <text class="entry-text">浏览房型</text>
      </view>
      <view class="entry-item" @click="goChat">
        <view class="entry-icon-box" style="background:rgba(52,152,219,.12)">🤖</view>
        <text class="entry-text">智能客服</text>
      </view>
      <view class="entry-item" @click="goRecommend">
        <view class="entry-icon-box" style="background:rgba(39,174,96,.12)">🎯</view>
        <text class="entry-text">智能推荐</text>
      </view>
      <view class="entry-item" @click="goOrders">
        <view class="entry-icon-box" style="background:rgba(243,156,18,.12)">📋</view>
        <text class="entry-text">我的订单</text>
      </view>
    </view>

    <!-- hot rooms -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">热门房型</text>
        <text class="section-more" @click="goRoomList">查看全部 →</text>
      </view>
      <scroll-view scroll-x class="room-scroll" :show-scrollbar="false">
        <view class="room-card" v-for="room in hotRooms" :key="room.id" @click="goRoomDetail(room.id)">
          <image class="room-img" :src="room.coverImage || defaultImg" mode="aspectFill"></image>
          <view class="room-info">
            <text class="room-name line-clamp-1">{{ room.name }}</text>
            <view class="room-tags">
              <text class="tag" v-if="room.bedType">{{ room.bedType }}</text>
              <text class="tag" v-if="room.maxGuests">{{ room.maxGuests }}人</text>
              <text class="tag" v-if="room.area">{{ room.area }}㎡</text>
            </view>
            <view class="room-price-row">
              <text class="price">{{ room.price }}</text>
              <text class="price-unit">/晚</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import { getRoomTypeList, getHotRoomTypes } from '../../src/api/room'

export default {
  data() {
    return {
      checkIn: '',
      checkOut: '',
      guests: 2,
      guestIdx: 1,
      guestRange: ['1', '2', '3', '4', '5', '6'],
      banners: [
        { title: '奢华体验', desc: '五星级服务，尊享非凡旅程', bg: 'linear-gradient(135deg, #2C3E50, #3D5166)' },
        { title: '舒适客房', desc: '多样房型选择，满足商务与休闲', bg: 'linear-gradient(135deg, #C9A96E, #B8953F)' },
        { title: '智能服务', desc: 'AI客服24小时在线，随时为您解答', bg: 'linear-gradient(135deg, #3498DB, #2980B9)' }
      ],
      hotRooms: [],
      defaultImg: '/static/lose.png'
    }
  },
  computed: {
    todayStr() {
      const d = new Date()
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
    },
    minCheckOut() {
      if (!this.checkIn) return this.todayStr
      const d = new Date(this.checkIn)
      d.setDate(d.getDate() + 1)
      return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
    }
  },
  onLoad() {
    this.loadHotRooms()
  },
  onShow() {
    const cIn = uni.getStorageSync('checkIn')
    const cOut = uni.getStorageSync('checkOut')
    if (cIn) this.checkIn = cIn
    if (cOut) this.checkOut = cOut
  },
  methods: {
    async loadHotRooms() {
      try {
        const res = await getHotRoomTypes()
        this.hotRooms = res.data || []
      } catch { /* ignore */ }
    },
    onCheckIn(e) {
      this.checkIn = e.detail.value
      uni.setStorageSync('checkIn', e.detail.value)
      if (this.checkOut && this.checkOut <= this.checkIn) {
        this.checkOut = ''
        uni.removeStorageSync('checkOut')
      }
    },
    onCheckOut(e) {
      this.checkOut = e.detail.value
      uni.setStorageSync('checkOut', e.detail.value)
    },
    onGuestChange(e) {
      this.guestIdx = e.detail.value
      this.guests = Number(this.guestRange[e.detail.value])
    },
    goRoomList() {
      uni.setStorageSync('checkIn', this.checkIn)
      uni.setStorageSync('checkOut', this.checkOut)
      uni.navigateTo({ url: '/pages/room/list' })
    },
    goRoomDetail(id) {
      uni.navigateTo({ url: '/pages/room/detail?id=' + id })
    },
    goChat() {
      uni.switchTab({ url: '/pages/chat/chat' })
    },
    goRecommend() {
      uni.navigateTo({ url: '/pages/recommend/recommend' })
    },
    goOrders() {
      uni.switchTab({ url: '/pages/order/list' })
    }
  }
}
</script>

<style lang="scss" scoped>
.home-page {
  background: #F5F6FA;
  min-height: 100vh;
  padding-bottom: 24rpx;
}

.search-bar {
  background: linear-gradient(160deg, #2C3E50, #3D5166);
  padding: 24rpx 28rpx 32rpx;
}

.search-row {
  display: flex;
  align-items: center;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 8rpx 6rpx 8rpx 20rpx;
}

.search-item {
  flex: 1;
  padding: 8rpx 0;
}

.search-label {
  display: block;
  font-size: 22rpx;
  color: #999999;
  margin-bottom: 4rpx;
}

.search-val {
  font-size: 28rpx;
  color: #333333;
  font-weight: 500;
}

.search-divider {
  width: 2rpx;
  height: 48rpx;
  background: #EEEEEE;
}

.search-btn {
  background: linear-gradient(135deg, #C9A96E, #D4B87A);
  border-radius: 12rpx;
  padding: 18rpx 28rpx;
  margin-left: 6rpx;
}

.search-btn-text {
  color: #FFFFFF;
  font-size: 28rpx;
  font-weight: 600;
}

.banner-swiper {
  height: 320rpx;
  margin: 20rpx;

  swiper-item {
    border-radius: 16rpx;
    overflow: hidden;
  }
}

.banner-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 40rpx;
  border-radius: 16rpx;
}

.banner-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #FFFFFF;
  margin-bottom: 12rpx;
}

.banner-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.quick-entries {
  display: flex;
  padding: 16rpx 20rpx;
  background: #FFFFFF;
  margin: 0 20rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.entry-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 0;
}

.entry-icon-box {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  margin-bottom: 10rpx;
}

.entry-text {
  font-size: 24rpx;
  color: #666666;
}

.section {
  margin-top: 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32rpx;
  margin-bottom: 16rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #2C3E50;
}

.section-more {
  font-size: 26rpx;
  color: #C9A96E;
}

.room-scroll {
  white-space: nowrap;
  padding: 0 20rpx;
}

.room-card {
  display: inline-block;
  width: 440rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  margin-right: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.room-img {
  width: 100%;
  height: 240rpx;
  background: #E8E8E8;
}

.room-info {
  padding: 16rpx 20rpx 20rpx;
}

.room-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333333;
  margin-bottom: 10rpx;
  display: block;
}

.room-tags {
  margin-bottom: 12rpx;
}

.room-price-row {
  display: flex;
  align-items: baseline;
}

.price {
  color: #C9A96E;
  font-weight: 700;
  font-size: 36rpx;

  &::before {
    content: '¥';
    font-size: 24rpx;
    margin-right: 2rpx;
  }
}

.price-unit {
  font-size: 24rpx;
  color: #999999;
  margin-left: 4rpx;
}
</style>
