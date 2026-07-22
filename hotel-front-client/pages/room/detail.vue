<template>
  <view class="room-detail-page" v-if="room">
    <!-- image swiper with gradient overlay -->
    <view class="swiper-wrap">
      <swiper
        class="img-swiper"
        indicator-dots
        indicator-color="rgba(255,255,255,.4)"
        indicator-active-color="#FFFFFF"
        autoplay
        circular
        :interval="4000"
      >
        <swiper-item v-for="(img, i) in images" :key="i">
          <image class="swiper-img" :src="img" mode="aspectFill"></image>
        </swiper-item>
      </swiper>
      <view class="swiper-gradient"></view>
    </view>

    <!-- room info card - overlaps swiper -->
    <view class="info-card">
      <view class="info-top">
        <text class="info-name">{{ room.name }}</text>
        <view class="price-badge">
          <text class="price-symbol">¥</text>
          <text class="price-num">{{ room.price }}</text>
          <text class="price-unit">/晚</text>
        </view>
      </view>

      <text class="info-desc">{{ room.description || '暂无描述，欢迎体验' }}</text>

      <!-- meta grid -->
      <view class="meta-grid">
        <view class="meta-cell">
          <view class="meta-icon-box" style="background:rgba(245,158,11,.12)">
            <image class="meta-emoji" src="/static/icons/area.png" mode="aspectFit"></image>
          </view>
          <view class="meta-texts">
            <text class="meta-label">面积</text>
            <text class="meta-val">{{ room.area || '-' }}㎡</text>
          </view>
        </view>
        <view class="meta-cell">
          <view class="meta-icon-box" style="background:rgba(59,130,246,.12)">
            <image class="meta-emoji" src="/static/quick/room.png" mode="aspectFit"></image>
          </view>
          <view class="meta-texts">
            <text class="meta-label">床型</text>
            <text class="meta-val">{{ room.bedType || '-' }}</text>
          </view>
        </view>
        <view class="meta-cell">
          <view class="meta-icon-box" style="background:rgba(16,185,129,.12)">
            <image class="meta-emoji" src="/static/icons/people.png" mode="aspectFit"></image>
          </view>
          <view class="meta-texts">
            <text class="meta-label">人数</text>
            <text class="meta-val">{{ room.maxGuests || '-' }}人</text>
          </view>
        </view>
        <view class="meta-cell">
          <view class="meta-icon-box" style="background:rgba(236,72,153,.12)">
            <image class="meta-emoji" src="/static/icons/check.png" mode="aspectFit"></image>
          </view>
          <view class="meta-texts">
            <text class="meta-label">可订</text>
            <text class="meta-val" style="color:#10B981">{{ availableCountText }}</text>
          </view>
        </view>
      </view>

      <!-- amenities -->
      <view class="amenities-bar" v-if="room.amenities">
        <text class="amenity-tag" v-for="a in amenitiesList" :key="a">{{ a }}</text>
      </view>
    </view>

    <!-- booking card -->
    <view class="book-card">
      <text class="book-title">选择日期</text>
      <view class="date-row">
        <view class="date-cell" @click="$refs.pickerCheckIn && $refs.pickerCheckIn.$el ? null : ''">
          <text class="date-label">入住</text>
          <picker mode="date" :value="form.checkIn" :start="todayStr" @change="onCheckIn" ref="pickerCheckIn">
            <text class="date-val">{{ form.checkIn || '请选' }}</text>
          </picker>
        </view>
        <view class="date-arrow">
          <text>→</text>
        </view>
        <view class="date-cell">
          <text class="date-label">退房</text>
          <picker mode="date" :value="form.checkOut" :start="minCheckOut" @change="onCheckOut">
            <text class="date-val">{{ form.checkOut || '请选' }}</text>
          </picker>
        </view>
        <view class="night-count" v-if="nights > 0">
          <text>{{ nights }} 晚</text>
        </view>
      </view>

      <!-- qty -->
      <view class="qty-row" v-if="datesSelected && availableCount > 0">
        <text class="qty-label">房间数量</text>
        <view class="qty-controls">
          <view class="qty-btn" :class="{ disabled: form.roomCount <= 1 }" @click="decrQty">−</view>
          <text class="qty-val">{{ form.roomCount }}</text>
          <view class="qty-btn" :class="{ disabled: form.roomCount >= availableCount }" @click="incrQty">+</view>
        </view>
      </view>

      <!-- summary -->
      <view class="summary-bar" v-if="datesSelected && nights > 0">
        <text class="summary-left">共 <text class="hl">{{ nights }}</text> 晚 · <text class="hl">{{ form.roomCount }}</text> 间</text>
        <text class="summary-price">¥{{ totalPrice.toFixed(2) }}</text>
      </view>

      <!-- book button -->
      <button class="book-btn" @click="goCreateOrder">
        <text class="book-btn-text">立即预订</text>
      </button>
    </view>

    <view class="bottom-spacer"></view>
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
      if (!this.datesSelected) return '请选日期'
      if (this.loadingAvailable) return '查询中...'
      if (this.availableCount <= 0) return '已满房'
      return '可订 ' + this.availableCount + '间'
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
    if (id) this.loadDetail(id)
    const cIn = uni.getStorageSync('checkIn')
    const cOut = uni.getStorageSync('checkOut')
    if (cIn) this.form.checkIn = cIn
    if (cOut) this.form.checkOut = cOut
    if (cIn && cOut) this.loadAvailableByDate(id)
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
      } catch { this.availableCount = 0 }
      finally { this.loadingAvailable = false }
    },
    incrQty() {
      if (this.form.roomCount < this.availableCount) this.form.roomCount++
      else uni.showToast({ title: '最多可订' + this.availableCount + '间', icon: 'none' })
    },
    decrQty() { if (this.form.roomCount > 1) this.form.roomCount-- },
    onCheckIn(e) {
      this.form.checkIn = e.detail.value
      if (this.form.checkOut && this.form.checkOut <= this.form.checkIn) { this.form.checkOut = ''; return }
      if (this.room && this.datesSelected) this.loadAvailableByDate(this.room.id)
    },
    onCheckOut(e) {
      this.form.checkOut = e.detail.value
      if (this.room && this.datesSelected) this.loadAvailableByDate(this.room.id)
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
      const qs = Object.entries({
        roomTypeId: this.room.id, roomName: this.room.name,
        coverImg: (this.room.coverImage || ''), checkIn: this.form.checkIn,
        checkOut: this.form.checkOut, guests: this.room.maxGuests || 2,
        price: this.room.price || 0, roomCount: this.form.roomCount
      }).map(([k, v]) => encodeURIComponent(k) + '=' + encodeURIComponent(v)).join('&')
      uni.navigateTo({ url: '/pages/order/create?' + qs })
    },
    goChat() { uni.switchTab({ url: '/pages/chat/chat' }) },
    goRecommend() { uni.navigateTo({ url: '/pages/recommend/recommend' }) }
  }
}
</script>

<style lang="scss" scoped>
$bg: #F8F6F3;
$card-bg: #FFFFFF;
$text-primary: #1A1A2E;
$text-secondary: #6B7280;
$text-light: #9CA3AF;
$accent-orange: #EA580C;
$accent-gold: #D97706;
$radius-sm: 14rpx;
$radius-md: 22rpx;
$radius-lg: 30rpx;

.room-detail-page {
  background: $bg;
  min-height: 100vh;
}

/* ====== Swiper ====== */
.swiper-wrap { position: relative; }

.img-swiper {
  height: 480rpx;

  swiper-item { border-radius: 0; overflow: hidden; }
}

.swiper-img { width: 100%; height: 100%; background: linear-gradient(135deg, #E8E8E8, #D4D4D4); }

.swiper-gradient {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 120rpx;
  background: linear-gradient(to top, rgba(248,246,243,1), transparent);
  pointer-events: none;
}

/* ====== Info Card ====== */
.info-card {
  background: $card-bg;
  margin: -40rpx 20rpx 0;
  border-radius: $radius-lg;
  padding: 32rpx 28rpx 28rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,.08);
  position: relative;
  z-index: 2;
}

.info-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14rpx;
}

.info-name {
  font-size: 38rpx;
  font-weight: 800;
  color: $text-primary;
  flex: 1;
  line-height: 1.3;
}

.price-badge {
  display: flex;
  align-items: baseline;
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  padding: 10rpx 22rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.price-symbol { font-size: 26rpx; color: $accent-gold; font-weight: 700; margin-right: 2rpx; }
.price-num { font-size: 38rpx; color: $accent-orange; font-weight: 800; }
.price-unit { font-size: 22rpx; color: $text-light; margin-left: 4rpx; }

.info-desc {
  font-size: 26rpx;
  color: $text-secondary;
  line-height: 1.7;
  display: block;
  margin-bottom: 24rpx;
}

/* ====== Meta Grid ====== */
.meta-grid {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.meta-cell {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10rpx;
  background: #FAFAFA;
  border-radius: $radius-sm;
  padding: 16rpx 12rpx;
}

.meta-icon-box {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.meta-emoji { width: 24rpx; height: 24rpx; }

.meta-texts { display: flex; flex-direction: column; }
.meta-label { font-size: 21rpx; color: $text-light; }
.meta-val { font-size: 27rpx; color: $text-primary; font-weight: 700; }

/* ====== Amenities ====== */
.amenities-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid #F0EDE8;
}

.amenity-tag {
  font-size: 23rpx;
  color: $text-secondary;
  background: #F3F0EB;
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
}

/* ====== Booking Card ====== */
.book-card {
  background: $card-bg;
  margin: 20rpx 20rpx 0;
  border-radius: $radius-lg;
  padding: 28rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,.04);
}

.book-title {
  font-size: 30rpx;
  font-weight: 700;
  color: $text-primary;
  display: block;
  margin-bottom: 18rpx;
}

.date-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.date-cell {
  flex: 1;
  background: #F8F6F3;
  border-radius: $radius-sm;
  padding: 16rpx 18rpx;
}

.date-label {
  font-size: 21rpx;
  color: $text-light;
  display: block;
  margin-bottom: 4rpx;
}

.date-val {
  font-size: 28rpx;
  color: $text-primary;
  font-weight: 600;
}

.date-arrow {
  font-size: 28rpx;
  color: $text-light;
  flex-shrink: 0;
}

.night-count {
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  border-radius: 20rpx;
  padding: 8rpx 18rpx;
  flex-shrink: 0;
}
.night-count text { font-size: 23rpx; color: $accent-gold; font-weight: 600; }

.qty-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #F0EDE8;
}

.qty-label { font-size: 27rpx; color: $text-secondary; }

.qty-controls {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.qty-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #F3F0EB;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: 300;
  color: $text-primary;

  &:active { background: $accent-gold; color: #FFF; }
  &.disabled { opacity: .35; }
}

.qty-val { font-size: 32rpx; font-weight: 700; color: $text-primary; min-width: 44rpx; text-align: center; }

.summary-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 18rpx;
  padding: 16rpx 20rpx;
  background: #FAFAFA;
  border-radius: $radius-sm;
}

.summary-left { font-size: 27rpx; color: $text-secondary; }
.hl { color: $accent-orange; font-weight: 700; }
.summary-price { font-size: 34rpx; color: $accent-orange; font-weight: 800; }

.book-btn {
  width: 100%;
  height: 92rpx;
  background: linear-gradient(135deg, #F97316, #EA580C);
  color: #FFF;
  font-size: 33rpx;
  font-weight: 700;
  border-radius: $radius-md;
  margin-top: 20rpx;
  letter-spacing: 4rpx;
  border: none;
  &::after { border: none; }

  &:active { opacity: .9; transform: scale(.99); }
}

.book-btn-text { text-shadow: 0 2rpx 4rpx rgba(0,0,0,.1); }

/* ====== Quick Actions ====== */
.quick-actions {
  display: flex;
  gap: 16rpx;
  margin: 20rpx 20rpx 0;
}

.quick-action {
  flex: 1;
  background: $card-bg;
  border-radius: $radius-md;
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,.03);

  &:active { opacity: .85; }
}

.qa-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.qa-img { width: 36rpx; height: 36rpx; }
.qa-text { font-size: 25rpx; color: $text-secondary; font-weight: 500; }

.bottom-spacer { height: 48rpx; }
</style>
